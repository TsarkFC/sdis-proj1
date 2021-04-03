package protocol;

import messages.Delete;
import peer.Peer;
import peer.PeerArgs;
import utils.FileHandler;
import utils.ThreadHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DeleteProtocol extends Protocol {

    public DeleteProtocol(File file, Peer peer) {
        super(file,peer);
    }

    //Send on the MC Channel
    //A file may be deleted, and it should delete all the chunks of that file
    //When the file is modified, it should also delete the old copy
    //An implementation may send this message as many times as it is deemed necessary to ensure that all space used
    // by chunks of the deleted file are deleted in spite of the loss of some messages.
    @Override
    public void initialize(){
        PeerArgs peerArgs = peer.getPeerArgs();
        List<byte[]> messages = new ArrayList<>();
        FileHandler fileHandler = new FileHandler(file);
        Delete msg = new Delete(peerArgs.getVersion(),peerArgs.getPeerId(),fileHandler.createFileId());
        messages.add(msg.getBytes());
        ThreadHandler.startMulticastThread(peerArgs.getAddressList().getMcAddr().getAddress(),
                peerArgs.getAddressList().getMcAddr().getPort(), messages);
    }
}
