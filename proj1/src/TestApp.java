import utils.SubProtocol;

import java.io.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;


// java Client <host_name> <remote_object_name> <oper> <opnd>*
// java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>
public class TestApp {

    private final int PEER_APP_IDX = 0;
    private final int SUB_PROTOCOL_IDX = 1;
    private final int PATH_IDX = 2;
    private final int DISK_SPACE_IDX = 2;
    private final int REPLICATION_DEGREE_IDX = 3;
    private String peerAp;
    private SubProtocol subProtocol;
    private String path;
    private float diskSpace; //RECLAIM
    private int replicationDegree; //Backup protocol

    private RemoteObject stub;


    private void parseArguments(String[] args) {

        if (args.length < 3) {
            System.out.println("Usage: <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            return;
        }
        this.peerAp = args[this.PEER_APP_IDX];
        this.subProtocol = SubProtocol.valueOf(args[this.SUB_PROTOCOL_IDX]);


        if(SubProtocol.RECLAIM == this.subProtocol) {
            diskSpace = Float.parseFloat(args[this.DISK_SPACE_IDX]);
        } else path = args[this.PATH_IDX];

        if (this.subProtocol == SubProtocol.BACKUP) {
            if (args.length != 4) {
                System.out.println("Usage: <peer_ap> BACKUP <path_name> <replication_degree>");
                return;
            }
            this.replicationDegree = Integer.parseInt(args[this.REPLICATION_DEGREE_IDX]);
        }else {
            System.out.println("Only BACKUP has 4 arguments!");
            System.out.println("Usage: <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            return;
        }
        //this.subProtocol = this.

    }

    private void processRequest(SubProtocol protocol,File file){
        try {
            switch (protocol) {
                case STATE -> stub.state(file);
                case BACKUP -> stub.backup(file);
                case DELETE -> stub.delete(file);
                case RECLAIM -> stub.reclaim(file);
                case RESTORE -> stub.restore(file);
            }
        }

        catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void connectRmi(){
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            this.stub = (RemoteObject) registry.lookup(this.peerAp);
            System.out.println("Connected!");
        } catch (Exception e) {
            System.err.println("TestApp exception: " + e.toString());
            e.printStackTrace();
        }
    }
    private File getFile(){
        File file = new File(this.path);
        if(file.exists() && file.canRead()) return file;
        else return null;
    }

    public static void main(String[] args) throws IOException {
        TestApp testApp = new TestApp();
        testApp.parseArguments(args);
        testApp.connectRmi();
        File file = testApp.getFile();
        if(file != null) testApp.processRequest(testApp.subProtocol,file);
        else System.out.println("Error getting file");

    }
}