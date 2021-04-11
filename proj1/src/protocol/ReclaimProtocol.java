package protocol;

import filehandler.FileHandler;
import messages.Removed;
import peer.Peer;
import peer.PeerArgs;
import peer.metadata.ChunkMetadata;
import peer.metadata.StoredChunksMetadata;
import utils.ThreadHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReclaimProtocol extends Protocol {
    private final Double maxDiskSpace;
    //TODO Talvez este null de merda, mas sera que vale a pena?
    public ReclaimProtocol(Double maxDiskSpace, Peer peer) {
        super((File) null, peer);
        this.maxDiskSpace = maxDiskSpace;
    }

    @Override
    public void initialize() {
        System.out.println("[RECLAIM] Initializing Reclaim protocol");

        //TODO O Reclaim quem é que elimina o espaço, é o initiator peer?
        PeerArgs peerArgs = peer.getArgs();

        double currentStoredSize =  FileHandler.getDirectoryKbSize(peer.getFileSystem());
        System.out.println(String.format("[RECLAIM] Peer %d has %f Kb allocated and a max size of %f",peerArgs.getPeerId(),currentStoredSize,maxDiskSpace));
        if(currentStoredSize > maxDiskSpace){
            reclaimSpace(maxDiskSpace,currentStoredSize);
        }

    }

    public void reclaimSpace(double maxDiskSpace, double currentSize){
        List<byte[]> messages = new ArrayList<>();
        File[] fileFolders = FileHandler.getDirectoryFiles(peer.getFileSystem());
        peer.getMetadata().setMaxSpace(maxDiskSpace);
        if (fileFolders != null) {

            System.out.println("[RECLAIM] Eliminating only chunks with Perceived Rep degree > Rep degree");
            for (File file : fileFolders) {
                if (currentSize <= maxDiskSpace) break;
                currentSize = reclaimFileSpace(file,currentSize,messages,true);
            }
            fileFolders = FileHandler.getDirectoryFiles(peer.getFileSystem());

            //Eliminate every file until it has size < maxSize
            if(currentSize > maxDiskSpace){
                System.out.println("[RECLAIM] Eliminating the ones with bigger rep degree than desired was not enough...");
                System.out.println("[RECLAIM] Eliminating other files");
                for (File file : fileFolders) {
                    if (currentSize <= maxDiskSpace) break;
                    currentSize = reclaimFileSpace(file,currentSize,messages,false);
                }
            }
            PeerArgs peerArgs = peer.getArgs();
            ThreadHandler.startMulticastThread(peerArgs.getAddressList().getMcAddr().getAddress(),
                    peerArgs.getAddressList().getMcAddr().getPort(), messages);
        }else{
            System.out.println("The peer does not have any stored files");
        }
    }



    private double reclaimFileSpace(File fileId,double currentSize,List<byte[]> messages, boolean onlyBiggerPercDgr){
        StoredChunksMetadata storedChunksMetadata = peer.getMetadata().getStoredChunksMetadata();
        String name = fileId.getName();
        if(!name.equals("metadata") && !name.equals("restored")){
            System.out.println("[RECLAIM] Analysing file: " + name);
            File[] chunks = FileHandler.getDirectoryFiles(fileId.getPath());
            if (chunks!= null){
                for (File chunkFile : chunks){
                    ChunkMetadata chunkMetadata = storedChunksMetadata.getChunk(fileId.getName(), Integer.valueOf(chunkFile.getName()));
                    if(!onlyBiggerPercDgr || chunkMetadata.biggerThanDesiredRep()){
                            PeerArgs peerArgs = peer.getArgs();
                            double size = chunkFile.length() / 1000;
                            System.out.println("[RECLAIM] Eliminating chunk: " + chunkFile.getPath() + " size: " + size);
                            System.out.println("          With perceived dgr = " + chunkMetadata.getPerceivedRepDgr() + " and rep = "+chunkMetadata.getRepDgr());
                            if (FileHandler.deleteFile(chunkFile)) {
                                Removed removedMsg = new Removed(peerArgs.getVersion(), peerArgs.getPeerId(), fileId.getName(), Integer.parseInt(chunkFile.getName()));
                                messages.add(removedMsg.getBytes());
                                currentSize -= size;
                                System.out.println("[RECLAIM] Current Size = " + currentSize);
                                if (currentSize <= maxDiskSpace) break;
                            }
                    }
                }
            }
        }
        return currentSize;
    }

}


