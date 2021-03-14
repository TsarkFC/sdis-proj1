package utils;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    private File file;
    private static final int CHUNK_SIZE = 64000;

    public FileHandler(File file) {
        this.file = file;
    }


    public File createFileFromBytes(byte[] chunk,String name, int counter){
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



    public List<File> splitFile()  {
        List<File> chunks = new ArrayList<>();
        String name = file.getName();
        int counter = 0;
        try {
            FileInputStream inputStream = new FileInputStream(file);
            byte[] chunk = new byte[CHUNK_SIZE];
            int chunkLen = 0;
            while ((chunkLen = inputStream.read(chunk)) != -1) {
                counter++;
                File chunkFile =createFileFromBytes(chunk,name,counter);
                chunks.add(chunkFile);
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
