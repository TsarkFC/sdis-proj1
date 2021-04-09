package protocol;

import messages.Delete;
import messages.Removed;
import peer.Peer;
import peer.PeerArgs;
import peer.metadata.ChunkMetadata;
import peer.metadata.StoredChunksMetadata;
import utils.FileHandler;
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
        System.out.println("Initializing reclaim");
        //TODO O Reclaim quem é que elimina o espaço, é o initiator peer?
        PeerArgs peerArgs = peer.getPeerArgs();

        double currentStoredSize =  FileHandler.getFolderKbSize(peer.getFileSystem());
        System.out.println(String.format("Peer %d has %f Kb allocated and a max size of %f",peerArgs.getPeerId(),currentStoredSize,maxDiskSpace));
        if(currentStoredSize > maxDiskSpace){
            reclaimSpace(maxDiskSpace,currentStoredSize);
        }

    }

    public void reclaimSpace(double maxDiskSpace, double currentSize){
        List<byte[]> messages = new ArrayList<>();
        System.out.println("Current size: " + currentSize + " Max Size: " + maxDiskSpace);
        File[] fileFolders = FileHandler.getFolderFiles(peer.getFileSystem());
        peer.getPeerMetadata().setMaxSpace(maxDiskSpace);
        if (fileFolders != null) {

            //TODO primeiro percorrer aqueles com perceived degree > rep degree
            for (File file : fileFolders) {
                if (currentSize <= maxDiskSpace) break;
                //if (storedChunksMetadata.getChunkId(fi))
                currentSize = reclaimFileSpace(file,currentSize,messages,true);
            }
            fileFolders = FileHandler.getFolderFiles(peer.getFileSystem());

            //Eliminate every file until it has size < maxSize
            if(currentSize > maxDiskSpace){
                System.out.println("Eliminating the ones with bigger rep degree than desired was not enough...");
                System.out.println("Eliminating other files");
                for (File file : fileFolders) {
                    if (currentSize <= maxDiskSpace) break;
                    currentSize = reclaimFileSpace(file,currentSize,messages,false);
                }
            }

            PeerArgs peerArgs = peer.getPeerArgs();
            ThreadHandler.startMulticastThread(peerArgs.getAddressList().getMcAddr().getAddress(),
                    peerArgs.getAddressList().getMcAddr().getPort(), messages);
        }else{
            System.out.println("The peer does not have any stored files");
        }
    }



    private double reclaimFileSpace(File fileId,double currentSize,List<byte[]> messages, boolean onlyBiggerPercDgr){
        StoredChunksMetadata storedChunksMetadata = peer.getPeerMetadata().getStoredChunksMetadata();


        //TODO por por replication degree em vez de por ordem alfabetica
        String name = fileId.getName();
        if(name != "metadata"){
            File[] chunks = FileHandler.getFolderFiles(fileId.getPath());
            if (chunks!= null){
                for (File chunkFile : chunks){
                    System.out.println("\n\n\nFOR EACH CHUNK");
                    System.out.println("FILE ID: " + fileId.getName());
                    System.out.println("Chunk no: " + chunkFile.getName());
                    ChunkMetadata chunkMetadata = storedChunksMetadata.getChunk(fileId.getName(), Integer.valueOf(chunkFile.getName()));
                    if((onlyBiggerPercDgr && chunkMetadata.biggerThanDesiredRep()) || !onlyBiggerPercDgr ){
                            PeerArgs peerArgs = peer.getPeerArgs();
                            double size = chunkFile.length() / 1000;
                            System.out.println("Eliminating chunk: " + chunkFile.getPath() + " size: " + size);
                            if (FileHandler.deleteFile(chunkFile)) {
                                Removed removedMsg = new Removed(peerArgs.getVersion(), peerArgs.getPeerId(), fileId.getName(), Integer.parseInt(chunkFile.getName()));
                                messages.add(removedMsg.getBytes());
                                currentSize -= size;
                                System.out.println("Current Size = " + currentSize);
                                if (currentSize <= maxDiskSpace) break;
                            }
                    }
                }
            }
        }
        return currentSize;
    }

    /*private void eliminateChunk(File chunkFile,List<byte[]> messages,double currentSize,File fileId){
        PeerArgs peerArgs = peer.getPeerArgs();
        double size = chunkFile.length()/1000;
        System.out.println("Eliminating chunk: " + chunkFile.getPath() + " size: " + size);
        if(FileHandler.deleteFile(chunkFile)){
            Removed removedMsg = new Removed(peerArgs.getVersion(), peerArgs.getPeerId(), fileId.getName(),Integer.parseInt(chunkFile.getName()));
            messages.add(removedMsg.getBytes());
            currentSize -= size;
            System.out.println("Current Size = "  + currentSize);
            if(currentSize <= maxDiskSpace) break;
        }

    }*/
}


