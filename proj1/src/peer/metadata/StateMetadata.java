package peer.metadata;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateMetadata implements Serializable {



    Map<String, FileMetadata> fileInfo = new HashMap<>();
    String path;


    public StateMetadata(String path) {
        this.path = path;
        System.out.println("State METADATA PATH: " + path);
    }

    public void updateInfo(FileMetadata fileMetadata) throws IOException {
        fileInfo.put(fileMetadata.getId(), fileMetadata);
        writeMetadata();
    }
    public void updateInfo(FileMetadata fileMetadata,ChunkMetadata chunkMetadata) throws IOException {
        fileMetadata.addChunk(chunkMetadata);
        fileInfo.put(fileMetadata.getId(), fileMetadata);
        writeMetadata();
    }

    public void updateInfo(String filePath,String fileId,int repDgr,int chunkId,int percRepDgr,int chunkKbSize) throws IOException {
        ChunkMetadata chunkMetadata = new ChunkMetadata(chunkKbSize,chunkId,repDgr,percRepDgr);
        FileMetadata fileMetadata = new FileMetadata(filePath,fileId,repDgr,chunkMetadata);
        fileInfo.put(fileMetadata.getId(),fileMetadata);
        writeMetadata();
    }

    public void deleteFile(String fileId) throws IOException {
        if (!fileInfo.containsKey(fileId)) {
            System.out.println("Cannot delete File from Metadata");
        }else{
            fileInfo.remove(fileId);
        }
        writeMetadata();
    }

    private void writeMetadata() throws IOException {
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(path));
        os.writeObject(this);
        os.close();
    }

    public StateMetadata readMetadata(){
        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(path));
            StateMetadata stateMetadata = (StateMetadata) is.readObject();
            fileInfo = stateMetadata.getFileInfo();
            is.close();
            printState();
            return stateMetadata;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No data to read from peer");
            System.out.println("Creating new one...");
            return new StateMetadata(path);
        }
    }

    public void printState(){
        for (String fileId : fileInfo.keySet()) {
            FileMetadata fileMeta = fileInfo.get(fileId);
            System.out.println("********************************************");
            System.out.println("************* State Metadata  **************");
            System.out.println("File:");
            System.out.println(String.format("\tPathname: %s\n\tID: %s\t\nReplication Degree: %d",
                    fileMeta.getPathname(),fileMeta.getId(),fileMeta.getRepDgr()));
            System.out.println("\tChunks:");
            for (ChunkMetadata chunkMetadata : fileMeta.getChunks()){
                System.out.println("\t\tID: " + chunkMetadata.getId());
                System.out.println("\t\tSize: " + chunkMetadata.getSizeKb());
                System.out.println("\t\tDesired Rep: " + chunkMetadata.getRepDgr());
                System.out.println("\t\tPerceived Rep: " + chunkMetadata.getPerceivedRepDgr());
            }

            System.out.println(String.format("\t\tID: "));
        }
    }



    public String getPath() {
        return path;
    }

    public Map<String, FileMetadata> getFileInfo() {
        return fileInfo;
    }
}
