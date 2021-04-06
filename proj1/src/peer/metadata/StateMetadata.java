package peer.metadata;

import java.io.*;
import java.util.*;

public class StateMetadata implements Serializable {

    Map<String, FileMetadata> fileInfo = new HashMap<>();
    String path;


    public StateMetadata(String path) {
        this.path = path;
        System.out.println("State METADATA PATH: " + path);
    }

    public void updateInfo(FileMetadata fileMetadata) throws IOException {
        fileInfo.put(fileMetadata.getId(), fileMetadata);
        writeState();
    }
    public void updateInfo(FileMetadata fileMetadata,ChunkMetadata chunkMetadata) throws IOException {
        fileMetadata.addChunk(chunkMetadata);
        fileInfo.put(fileMetadata.getId(), fileMetadata);
        writeState();
    }

    public void updateInfo(String filePath,String fileId,int repDgr,int chunkId,int percRepDgr,int chunkKbSize) throws IOException {
        ChunkMetadata chunkMetadata = new ChunkMetadata(chunkKbSize,chunkId,repDgr,percRepDgr);
        FileMetadata fileMetadata = new FileMetadata(filePath,fileId,repDgr,chunkMetadata);
        fileInfo.put(fileMetadata.getId(),fileMetadata);
        writeState();
    }

    public void deleteFile(String fileId) throws IOException {
        if (!fileInfo.containsKey(fileId)) {
            System.out.println("Cannot delete File from Metadata");
        }else{
            fileInfo.remove(fileId);
        }
        writeState();
    }

    /*private void writeMetadata() throws IOException {
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(path));
        os.writeObject(this);
        os.close();
    }*/

    private void writeState(){

        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(path));
            for (String fileID : fileInfo.keySet()){
                FileMetadata fm = fileInfo.get(fileID);
                out.write(fm.getString());
            }
            out.write("TODO NAO ESTA A ESCREVER");
            out.close();
            System.out.printf("Created file yo");
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*try (PrintWriter out = new PrintWriter(path)) {
            for (String fileID : fileInfo.keySet()){
                FileMetadata fm = fileInfo.get(fileID);
                out.println(fm.getString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
    }

    public void readState(){
        File file = new File(path);
        Scanner input = null;
        try {
            input = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("State file does not exist, creating new one...");
            writeState();
            e.printStackTrace();
        }
        while (input.hasNextLine()){
            Scanner in = new Scanner(input.nextLine());
            FileMetadata fm = FileMetadata.readFile(in);
            fileInfo.put(fm.getId(),fm);
        }
    }

    /*public StateMetadata readMetadata(){
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
    }*/

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
