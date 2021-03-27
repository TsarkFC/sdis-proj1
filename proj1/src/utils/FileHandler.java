package utils;

import messages.Message;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

    public static void saveChunk(Message message, String peerDir) {
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
