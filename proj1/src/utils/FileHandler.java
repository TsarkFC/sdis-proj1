package utils;

import messages.GetChunk;
import messages.Message;
import messages.PutChunk;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FileHandler {
    private final File file;
    private static final int CHUNK_SIZE = 64000;

    public FileHandler(File file) {
        this.file = file;
    }

    public static File getFile(String path) {
        if (Files.exists(Paths.get(path))) {
            File file = new File(path);
            if (file.exists() && file.canRead()) return file;
        }
        return null;
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

    public static String getFilePath(String peerDir, Message message) {
        return peerDir.concat("/" + message.getFileId() + "/");
    }

    public static String getFilePath(String peerDir, String fileId) {
        return peerDir.concat("/" + fileId + "/");
    }

    public static String getChunkPath(String peerDir, String fileId, int chunkNo) {
        return peerDir.concat("/" + fileId + "/" + chunkNo);
    }

    //TODO: asynchronousfilechannel
    public static void saveChunk(PutChunk message, String peerDir) {
        // create directory if it does not exist
        String dirPath = getFilePath(peerDir, message);
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdir();

        File chunk = new File(dirPath + message.getChunkNo());

        // write message body to file
        /*
        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(message.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        ByteBuffer buffer = ByteBuffer.allocate(CHUNK_SIZE);
        buffer.put(message.getBody());
        buffer.rewind();

        AsynchronousFileChannel writeFileChannel = null;
        try {
            writeFileChannel = AsynchronousFileChannel.open(chunk.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("WRITING...");
        writeFileChannel.write(buffer, 0, buffer, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                System.out.println("Total bytes written: " + result);
            }

            @Override
            public void failed(Throwable ex, ByteBuffer attachment) {
                System.out.println("Write operation failed: " + ex.getMessage());
            }
        });
    }


    //TODO: asynchronousfilechannel
    public static void restoreFile(String path, Map<Integer, byte[]> content) throws IOException {
        File file = new File(path);
        FileOutputStream stream = new FileOutputStream(file);
        List<Integer> keys = new ArrayList<>(content.keySet());
        Collections.sort(keys);
        for (Integer key : keys) {
            stream.write(content.get(key));
        }
    }


    //TODO: asynchronousfilechannel
    public static byte[] getChunk(GetChunk message, String peerDir) {
        String chunkPath = getFilePath(peerDir, message) + message.getChunkNo();
        Path path = Paths.get(chunkPath);
        if (!Files.exists(path)) {
            return null;
        }

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void deleteFile(String fileId, String peerDir) {
        String dirPath = getFilePath(peerDir, fileId);
        File folder = new File(dirPath);
        if (!folder.exists()) System.out.println("Tried to delete directory that does not exist");
        else {
            if (FileHandler.deleteDirectory(folder)) {
                System.out.println("Deleted directory");
            } else {
                System.out.println("Error deleting directory");
            }
        }
    }

    public static boolean deleteFile(File myObj) {
        if (myObj.delete()) {
            System.out.println("Deleted the file: " + myObj.getName());
            return true;
        } else {
            System.out.println("Failed to delete the file.");
            return false;
        }
    }

    public static File[] getFolderFiles(String peerDir) {
        File folder = new File(peerDir);
        if (!folder.exists()) {
            System.out.println("Peer folder does not exist");
            return null;
        }
        return folder.listFiles();
    }

    public static double getFolderKbSize(String dirPath) {
        return FileHandler.getFolderSize(dirPath) / 1000.0;
    }

    private static double getFolderSize(String dirPath) {
        File folder = new File(dirPath);
        float length = 0;
        File[] files = folder.listFiles();

        for (File value : files) {
            if (value.isFile()) {
                length += value.length();
            } else {
                length += getFolderSize(value.getPath());
            }
        }
        return length;
    }

    static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                FileHandler.deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public ConcurrentHashMap<Integer, byte[]> splitFile() {
        ConcurrentHashMap<Integer, byte[]> chunks = new ConcurrentHashMap<>();

        for (int chunkNo = 0; chunkNo < getNumberOfChunks(); chunkNo++) {
            asyncChunkRead(chunks, chunkNo);
        }
        return chunks;
    }

    private void asyncChunkRead(ConcurrentHashMap<Integer, byte[]> chunks, int chunkNo) {
        ByteBuffer buffer = ByteBuffer.allocate(CHUNK_SIZE);
        AsynchronousFileChannel readFileChannel;
        try {
            readFileChannel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        readFileChannel.read(buffer, (long) chunkNo * CHUNK_SIZE, buffer, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, ByteBuffer bufferRead) {
                System.out.println("Number of bytes read: " + result);
                buffer.rewind();
                byte[] chunk = new byte[result];
                buffer.get(chunk);
                chunks.put(chunkNo, chunk);
                bufferRead.clear();
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                System.out.println("Read operation failed: " + exc.getMessage());

            }
        });
        System.out.println("Waiting for the asynchronous file read operation");
    }

    public byte[] getChunkFileData() {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            System.out.println("File size: " + file.length());
            byte[] chunk = new byte[CHUNK_SIZE];
            int read = inputStream.read(chunk);
            return Arrays.copyOf(chunk, read);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public int getNumberOfChunks() {
        int size = (int) file.length();
        if (size % CHUNK_SIZE == 0)
            return size / CHUNK_SIZE;
        return size / CHUNK_SIZE + 1;
    }
}
