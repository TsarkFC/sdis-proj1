package channels;

import messages.*;
import peer.Peer;
import protocol.RestoreProtocol;
import utils.AddressList;
import utils.FileHandler;
import utils.ThreadHandler;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static utils.FileHandler.restoreChunk;

public class ControlChannel extends Channel {

    public ControlChannel(AddressList addressList, Peer peer){
        super(addressList, peer);
        super.currentAddr = addressList.getMcAddr();
    }

    @Override
    public void handle(DatagramPacket packet) throws IOException {
        String rcvd = new String(packet.getData(), 0, packet.getLength());
        parseMsg(rcvd);

    }

    public void parseMsg(String msgString) throws IOException {
        String msgType = Message.getTypeStatic(msgString);
        if(msgType.equals("STORED")) handleBackup(msgString);
        else if(msgType.equals("DELETE")) handleDelete(msgString);
        else if(msgType.equals("GETCHUNK")) handleRestore(msgString);
        else System.out.println("\nERROR NOT PARSING THAT MESSAGE " +  msgType);

    }

    public void handleBackup(String msgString) throws IOException{
        System.out.println("Control Channel received Stored Msg: " + msgString);
        Stored msg = new Stored(msgString);
        //Guardar em memoria nao volatil a quantidade de mensagens stored que recebeu de cada chunk
        peer.getPeerMetadata().updateChunkInfo(msg.getFileId(), msg.getChunkNo(), 1);
    }
    public void handleDelete(String msgString){
        System.out.println("Control Channel received Delete Msg: " + msgString);
        Delete msg = new Delete(msgString);
        FileHandler.deleteFile(msg,peer.getFileSystem());
        //Delete from metadata and from file
    }

    public void handleRestore(String msgString){
        System.out.println("Control Channel received Restore Msg: " + msgString);
        GetChunk msg = new GetChunk(msgString);
        RestoreProtocol.handleGetChunk(msg,peer);
    }

    public void closeMcChannel(){
        System.out.println("Mc Channel stops receiving after 1 second");
    }




}
