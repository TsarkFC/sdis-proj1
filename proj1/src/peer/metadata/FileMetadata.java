package peer.metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileMetadata {
    private final String pathname;
    private final String id;
    private final int repDgr;
    private  List<ChunkMetadata> chunks = new ArrayList<>();

    public FileMetadata(String pathname, String id, int repDgr) {
        this.pathname = pathname;
        this.id = id;
        this.repDgr = repDgr;
    }

    public FileMetadata(String pathname, String id, int repDgr,ChunkMetadata chunkMetadata) {
        this.pathname = pathname;
        this.id = id;
        this.repDgr = repDgr;
        addChunk(chunkMetadata);
    }

    public void addChunk(int sizeKb,int chunkNo,int percRepDgr){
        addChunk(new ChunkMetadata(sizeKb,chunkNo,repDgr,percRepDgr));
    }

    public void addChunk(ChunkMetadata chunkMetadata){
        if(!chunks.contains(chunkMetadata)){
            chunks.add(chunkMetadata);
        }
    }

    public String getString(){
        String fileText = String.format("%s:%s;%d;",id,pathname,repDgr);
        for (ChunkMetadata chunk : chunks){
            fileText.concat(chunk.getString() + " ");
        }
        return fileText;
    }

    public static FileMetadata readFile(Scanner in){
        in.useDelimiter(":");
        String fileId = in.next();
        Scanner fileScanner = new Scanner(in.nextLine());
        fileScanner.useDelimiter(";");
        String pathname = fileScanner.next();
        int repDgr = Integer.parseInt(fileScanner.next());
        FileMetadata fileMetadata = new FileMetadata(pathname,fileId,repDgr);

        String[] chunkList = in.nextLine().split(" ");
        for (String chunk: chunkList) {
            fileMetadata.addChunk(ChunkMetadata.readFile(chunk));
        }
        return  fileMetadata;

    }

    public String getPathname() {
        return pathname;
    }

    public String getId() {
        return id;
    }

    public int getRepDgr() {
        return repDgr;
    }

    public List<ChunkMetadata> getChunks() {
        return chunks;
    }

    public void setChunks(List<ChunkMetadata> chunks) {
        this.chunks = chunks;
    }
}
