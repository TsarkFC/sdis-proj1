import java.net.*;
import java.io.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

// java Client <host_name> <remote_object_name> <oper> <opnd>*
// java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>
public class TestApp {

    private final int PEER_APP_IDX = 0;
    private final int SUB_PROTOCOL_IDX = 1;
    private final int PATH_IDX = 2;
    private final int DISK_SPACE_IDX = 2;
    private final int REPLICATION_DEGREE_IDX = 3;
    private int peerAp;
    private SubProtocol subProtocol;
    private String path;
    private float diskSpace; //RECLAIM
    int replicationDegree; //Backup protocol


    private void parseArguments(String[] args) {

        if (args.length < 3) {
            System.out.println("Usage: <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            return;
        }
        this.peerAp = Integer.parseInt(args[this.PEER_APP_IDX]);
        this.subProtocol = SubProtocol.valueOf(args[SUB_PROTOCOL_IDX]);


        if(this.subProtocol == SubProtocol.RECLAIM) diskSpace = Float.parseFloat(args[this.DISK_SPACE_IDX]);
        else path = args[this.PATH_IDX];
        if (this.subProtocol == SubProtocol.BACKUP) {
            if (args.length < 3) {
                System.out.println("Usage: <peer_ap> BACKUP <path_name> <replication_degree>");
                return;
            }
            this.replicationDegree = Integer.parseInt(args[this.REPLICATION_DEGREE_IDX]);
        }else{

        }



        //this.subProtocol = this.

    }


    private static String getRequest(String[] args) {
        String oper = args[2];
        String request = "";

        switch (oper) {
            case "register":
                if (args.length != 5) {
                    System.out.println("Usage: java Client <host> <remote_object_name> register <DNS name> <IP address>");
                    return "";
                }
                request = "REGISTER " + args[3] + " " + args[4];
                break;
            case "lookup":
                if (args.length != 4) {
                    System.out.println("Usage: java Client <host> <remote_object_name> lookup <DNS name>");
                    return "";
                }
                request = "LOOKUP " + args[3];
                break;
            default:
                System.out.println("Invalid operation! (Can either be 'register' or 'lookup')");
                break;
        }
        return request;
    }

    // Client: <oper> <opnd>* : <result>
    private static void responseReceived(String received, String[] args) {
        String oper = args[2];
        switch (oper) {
            case "register":
                System.out.println(args[2] + " " + args[3] + " " + args[4] + " :: " + received);
                break;
            case "lookup":
                System.out.println(args[2] + " " + args[3] + " :: " + received);
                break;
        }
    }

    public static void main(String[] args) throws IOException {
        TestApp testApp = new TestApp();
        testApp.parseArguments(args);

        String host = args[0];
        String remote_object_name = args[1];
        String request = getRequest(args);
        if (request.equals(""))
            return;

        try {
            Registry registry = LocateRegistry.getRegistry(host);
            RemoteObject stub = (RemoteObject) registry.lookup(remote_object_name);
            String response = stub.processRequest(request);
            responseReceived(response, args);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}