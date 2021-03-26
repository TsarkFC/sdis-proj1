package utils;

import messages.Message;
import messages.PutChunk;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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

    public static String getFilePath(Message message) {
        //files
        //  sender1
        //      file1
        //          chunk1
        //          chunk2
        //      file2
        //          chunk1
        //          chunk2
        //  sender2
        String PATH = "files/";
        String dirSenderID = PATH.concat(String.valueOf(message.getSenderId()));
        String dirFileId = dirSenderID.concat("/" + message.getFileId() + "/");
        //String dirChunkNo = dirFileId.concat("/"+message.getChunkNo() +"/");
        System.out.println(dirFileId);
        return dirFileId;
    }

    public static void saveChunk(Message message) {
        try {

            Files.createDirectories(Paths.get(getFilePath(message)));
            File file = new File(getFilePath(message) + message.getChunkNo());
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            //TODO É Assim que e suposto guardar?
            //TODO O body esta a null!
            bw.write(new String(message.getBody()));
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*String PATH = getFilePath(message);
        //String fileName = id + getTimeStamp() + ".txt";
        File dirChunk = new File(PATH);
        if (!dirChunk.exists()){
            dirChunk.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }*/



        /*File file = new File(dirSenderID + "/" + fileName);
        try{
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(value);
            bw.close();
        }
        catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }*/
    }


    public List<byte[]> splitFile() {
        List<byte[]> chunks = new ArrayList<>();
        String name = file.getName();
        int counter = 0;
        try {
            FileInputStream inputStream = new FileInputStream(file);
            System.out.println("File size: " + file.length());
            byte[] chunk = new byte[CHUNK_SIZE];
            int chunkLen = 0;
            while ((chunkLen = inputStream.read(chunk)) != -1) {
                counter++;
                //System.out.println(chunkLen + "ZAS");
                //File chunkFile =createFileFromBytes(chunk,name,counter);
                //chunks.add(chunkFile);
                chunks.add(chunk);
            }
        } catch (FileNotFoundException fnfE) {
            // file not found, handle case
        } catch (IOException ioE) {

        }
        return chunks;
        // problem reading, handle case
        /*int counter = 1;
        List<File> files = new ArrayList<File>();
        int sizeOfChunk = FileHandler.chunkSize;
        String eof = System.lineSeparator();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String name = file.getName();
            String line = br.readLine();
            while (line != null) {
                File newFile = new File(file.getParent(), name + "."
                        + String.format("%03d", counter++));
                try (OutputStream out = new BufferedOutputStream(new FileOutputStream(newFile))) {
                    int fileSize = 0;
                    while (line != null) {
                        byte[] bytes = (line + eof).getBytes(Charset.defaultCharset());
                        if (fileSize + bytes.length > sizeOfChunk)
                            break;
                        out.write(bytes);
                        fileSize += bytes.length;
                        line = br.readLine();
                    }
                }
                files.add(newFile);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;*/

    }

}
