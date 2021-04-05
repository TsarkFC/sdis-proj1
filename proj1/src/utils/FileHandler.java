package utils;

import messages.Delete;
import messages.GetChunk;
import messages.Message;
import messages.PutChunk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileHandler {
    private File file;
    private static final int CHUNK_SIZE = 64000;

    public FileHandler(File file) {
        this.file = file;
    }


    public File createFileFromBytes(byte[] chunk, String name, int counter) {
        //Substituir por SHA
        File newFile = new File(file.getParent(), name + "."
                + String.format("%03d", counter++));
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(newFile);
            fos.write(chunk);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFile;

    }

    public String createFileId() {
        //Is not thread safe
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String id = file.getName() + file.lastModified() + file.length();
        byte[] encodedHash = digest.digest(id.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte c : encodedHash) {
            sb.append(String.format("%02X", c));
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

    // filesystem
    //   peer1
    //       file1
    //           chunk1
    //           chunk2
    //       file2
    //           chunk1
    //           chunk2
    //   peer2
    public static String getFilePath(String peerDir, Message message) {
        return peerDir.concat("/" + message.getFileId() + "/");
    }
    public static String getFilePath(String peerDir, String fileId) {
        return peerDir.concat("/" + fileId + "/");
    }

    public static String getFilePath(String peerDir, String fileId,String chunkNo) {
        return peerDir.concat("/" + fileId + "/"+chunkNo);
    }


    public static void saveChunk(PutChunk message, String peerDir) {
        // create directory if it does not exist
        String dirPath = getFilePath(peerDir, message);
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdir();

        // write message body to file
        File file = new File(dirPath + message.getChunkNo());
        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(message.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] restoreChunk(GetChunk message, String peerDir){
        String dirPath = getFilePath(peerDir, message);
        Path path = Paths.get(dirPath + message.getChunkNo());
        if (!Files.exists(path)){
            return null;
        }
        File file = new File(dirPath + message.getChunkNo());
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return fileContent;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    /*public static byte[] restoreChunk(GetChunk message, String peerDir){
        String dirPath = getFilePath(peerDir, message);
        Path path = Paths.get(dirPath + message.getChunkNo());
        if (!Files.exists(path)){
            return null;
        }
        //File file = new File(dirPath + message.getChunkNo());
        try (FileInputStream fis = new FileInputStream(dirPath + message.getChunkNo())) {
            byte[] buf = new byte[64000];
            fis.read(buf);
            return buf;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }*/


    /*public static byte[] restoreChunk(GetChunk message, String peerDir){
        String dirPath = getFilePath(peerDir, message);
        Path path = Paths.get(dirPath + message.getChunkNo());
        if (!Files.exists(path)){
            return null;
        }
        File file = new File(dirPath + message.getChunkNo());
        FileInputStream fin = null;
        try {
            // create FileInputStream object
            fin = new FileInputStream(file);

            byte fileContent[] = new byte[(int)file.length()];

            // Reads up to certain bytes of data from this input stream into an array of bytes.
            fin.read(fileContent);
            return fileContent;
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        }
        catch (IOException ioe) {
            System.out.println("Exception while reading file " + ioe);
        }
        finally {
            // close the streams using close method
            try {
                if (fin != null) {
                    fin.close();
                }
            }
            catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }

        return null;

    }*/


    public static void deleteFile(String fileId,String peerDir){
        String dirPath = getFilePath(peerDir, fileId);
        File folder = new File(dirPath);
        if (!folder.exists()) System.out.println("Tried to delete directory that does not exist");
        else {
            if(FileHandler.deleteDirectory(folder)){
                System.out.println("Deleted directory");
            } else{
                System.out.println("Error deleting directory");
            }
        }
    }


    public static boolean deleteFile(File myObj){
        if (myObj.delete()) {
            System.out.println("Deleted the file: " + myObj.getName());
            return true;
        } else {
            System.out.println("Failed to delete the file.");
            return false;
        }
    }

    public boolean checkFileExists(String path){
        File tempFile = new File(path);
        return tempFile.exists();
    }

    public static List<Integer> getChunkNoStored(String fileId,String peerDir){
        List<Integer> storedChunks = new ArrayList<>();
        String dirPath = getFilePath(peerDir, fileId);
        File folder = new File(dirPath);
        if (!folder.exists()){
            System.out.println("Tried to see stored Chunks but file folder does not exist");
            return null;
        }
        File[] allContents = folder.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                storedChunks.add(Integer.parseInt(file.getName()));
            }
            return  storedChunks;
        }
        return null;
    }

    public static File[] getFolderFiles(String peerDir){
        File folder = new File(peerDir);
        if (!folder.exists()){
            System.out.println("Peer folder does not exist");
            return null;
        }
        return folder.listFiles();
    }

    public static void reclaimDiskSpace(double maxDiskSpace, double currentSize,String peerDir){

    }


    private static double getFolderSize(String dirPath) {

        File folder = new File(dirPath);
        float length = 0;
        File[] files = folder.listFiles();
        int count = files.length;

        for (int i = 0; i < count; i++) {
            if (files[i].isFile()) {
                length += files[i].length();
            }
            else {
                length += getFolderSize(files[i].getPath());
            }
        }
        return length;
    }

    public static double getFolderKbSize(String dirPath){
        return FileHandler.getFolderSize(dirPath)/1000.0;
    }







    static boolean  deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                FileHandler.deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public List<byte[]> splitFile() throws IOException {
        List<byte[]> chunks = new ArrayList<>();
        //String name = file.getName();
        //String content = new String(Files.readAllBytes(Paths.get(file.getPath())));

        try {
            FileInputStream inputStream = new FileInputStream(file);
            System.out.println("File size: " + file.length());
            byte[] chunk = new byte[CHUNK_SIZE];

            while (inputStream.read(chunk) != -1) {
                byte[] chunkClone = chunk.clone();
                chunks.add(chunkClone);

            }
        } catch (FileNotFoundException fnfE) {
            // file not found, handle case
        } catch (IOException ioE) {

        }
        return chunks;
    }

}
