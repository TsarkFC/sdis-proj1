package protocol;

import messages.Delete;
import messages.Removed;
import peer.Peer;
import peer.PeerArgs;
import utils.FileHandler;
import utils.ThreadHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReclaimProtocol extends Protocol {
    private final Double maxDiskSpace;
    //TODO Talvez este null de merda, mas sera que vale a pena?
    public ReclaimProtocol(Double maxDiskSpace, Peer peer) {
        super(null, peer);
        this.maxDiskSpace = maxDiskSpace;
    }

    @Override
    public void initialize() {
        System.out.println("Initializing reclaim");
        //TODO O Reclaim quem é que elimina o espaço, é o initiator peer?
        PeerArgs peerArgs = peer.getPeerArgs();
        List<byte[]> messages = new ArrayList<>();

        double currentStoredSize =  FileHandler.getFolderKbSize(peer.getFileSystem());
        System.out.println(String.format("Peer %d has %f Kb allocated and a max size of %f",peerArgs.getPeerId(),currentStoredSize,maxDiskSpace));
        reclaimSpace(maxDiskSpace,currentStoredSize);
        if(currentStoredSize > maxDiskSpace){

            //Delete files
            //Como vamos escolher que files damos delete? talvez os mais antigos, ou so por ordem alfabetica
            //Talvez faça mais sentido ir eliminando o maior que couber
            //Removed msg = new Removed(peerArgs.getVersion(), peerArgs.getPeerId(), )
            //ThreadHandler.startMulticastThread(peerArgs.getAddressList().getMcAddr().getAddress(),
            //        peerArgs.getAddressList().getMcAddr().getPort(), messages);
        }

        /*Delete msg = new Delete(peerArgs.getVersion(), peerArgs.getPeerId(), fileHandler.createFileId());
        messages.add(msg.getBytes());
        ThreadHandler.startMulticastThread(peerArgs.getAddressList().getMcAddr().getAddress(),
                peerArgs.getAddressList().getMcAddr().getPort(), messages);*/
    }

    public void reclaimSpace(double maxDiskSpace, double currentSize){
        FileHandler.reclaimDiskSpace(maxDiskSpace,currentSize,peer.getFileSystem());
    }
}
