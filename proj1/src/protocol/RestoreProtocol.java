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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RestoreProtocol extends Protocol{
    private static final String NO_CHUNK_MSG ="NO_CHUNK";
    //TODO APAGAR ISTO QUANDO JA ESTIVER A FUNCIONAR
    private static final String SENT_CHUNK = "SENT_CHUNK";

    private Map< Integer,byte[]> chunksMap = new HashMap();

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
            GetChunk getChunk = new GetChunk(peerArgs.getVersion(), peerArgs.getPeerId(), fileId, i);
            byte[] msg = getChunk.getBytes();
            messages.add(msg);
        }
        ThreadHandler.startMulticastThread(peerArgs.getAddressList().getMcAddr().getAddress(),
                peerArgs.getAddressList().getMcAddr().getPort(), messages);
    }

    public static void handleGetChunk(GetChunk msg, Peer peerStatic) {
        byte[] chunk = FileHandler.restoreChunk(msg, peerStatic.getFileSystem());
        List<byte[]> msgs = new ArrayList<>();

        if (chunk != null) {
            //TODO UNCOMMENT
            //Send chunk body
            //Chunk chunkMsg = new Chunk(msg.getVersion(), peerStatic.getPeerArgs().getPeerId(),msg.getFileId() , msg.getChunkNo(), chunk);
            //msgs.add(chunkMsg.getBytes());
            //System.out.println("BYTES :" + chunkMsg.getBytes());

            //TODO TIRAR ISTO
            msgs.add(SENT_CHUNK.getBytes());
            System.out.println("Recovered chunk from: " + peerStatic.getPeerArgs().getPeerId());

        } else {
            //TODO Ele se nao adicionarmos nada a lista ele da erro e nao chega ao restore
            msgs.add(NO_CHUNK_MSG.getBytes());
            System.out.println("Tried to restore chunk that does not exist " + peerStatic.getPeerArgs().getPeerId());
        }
        AddressList addrList = peerStatic.getPeerArgs().getAddressList();
        ThreadHandler.startMulticastThread(addrList.getMdrAddr().getAddress(), addrList.getMdrAddr().getPort(), msgs);
    }

    public void handleChunkMsg(String rcvd){
        if (rcvd.equals(NO_CHUNK_MSG)) return;

        System.out.println();
        System.out.println("\nHandling Chunk message ");

        //Como tenho acesso ao numero de chunks necessários?
       /* int fileChunkNum = 2;
        Chunk chunkMsg = new Chunk(rcvd);
        chunkMsg.getFileId();
        chunksMap.put(chunkMsg.getChunkNo(),chunkMsg.getBody());
        if (chunksMap.size() == fileChunkNum){
            //Backup file?
            //Stop receiving messages
        }*/

        //System.out.println("Received in Restore Channel Chunk " + chunkMsg.getChunkNo());
    }
}
