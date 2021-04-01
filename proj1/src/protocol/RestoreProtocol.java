package protocol;

import messages.Chunk;
import messages.GetChunk;
import peer.Peer;
import peer.PeerArgs;
import utils.AddressList;
import utils.FileHandler;
import utils.ThreadHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RestoreProtocol extends Protocol{
    public RestoreProtocol(File file, Peer peer) {
        super(file, peer);
    }

    @Override
    public void initialize() {
        List<byte[]> messages = new ArrayList<>();
        FileHandler fileHandler = new FileHandler(file);
        PeerArgs peerArgs = peer.getPeerArgs();
        //TODO Aqui i guess que nao e preciso dividir o file, so ter os ids
        List<byte[]> chunks = null;
        try {
            chunks = fileHandler.splitFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fileId = fileHandler.createFileId();

        for (int i = 0; i < chunks.size(); i++) {
            GetChunk getChunk = new GetChunk(peerArgs.getVersion(), peerArgs.getPeerId(),fileId,i);
            byte[] msg = getChunk.getBytes();
            messages.add(msg);
        }
        ThreadHandler.startMulticastThread(peerArgs.getAddressList().getMcAddr().getAddress(),
                peerArgs.getAddressList().getMcAddr().getPort(), messages);
    }

    public static void handleGetChunk(GetChunk msg,Peer peerStatic){
        byte[] chunk = FileHandler.restoreChunk(msg,peerStatic.getFileSystem());
        if (chunk!=null){
            //Send chunk body
            Chunk chunkMsg = new Chunk(msg.getVersion(), msg.getSenderId(),msg.getFileId() , msg.getChunkNo(), chunk);
            List<byte[]> msgs = new ArrayList<>();
            msgs.add(chunkMsg.getBytes());
            AddressList addrList = peerStatic.getPeerArgs().getAddressList();
            ThreadHandler.startMulticastThread(addrList.getMdrAddr().getAddress(), addrList.getMdrAddr().getPort(), msgs);
        }

    }

}
