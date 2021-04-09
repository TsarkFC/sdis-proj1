import peer.RemoteObject;
import filehandler.FileHandler;
import utils.SubProtocol;

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
    private String peerAp;
    private SubProtocol subProtocol;
    private String path;
    private double diskSpace; //RECLAIM
    private int replicationDegree; //Backup protocol

    private RemoteObject stub;

    public static void main(String[] args) throws IOException, InterruptedException {
        TestApp testApp = new TestApp();
        if (!testApp.parseArguments(args)) return;
        testApp.connectRmi();
        if (testApp.path != null) {
            File file = FileHandler.getFile(testApp.path);
            if (file != null) testApp.processRequest(testApp.subProtocol, file);
        } else testApp.processRequest(testApp.subProtocol);
    }

    private boolean parseArguments(String[] args) {

        if (args.length < 2) {
            System.out.println("Usage: <peer_ap> <sub_protocol> [<opnd_1>] [<opnd_2>]");
            return false;
        }
        this.peerAp = args[this.PEER_APP_IDX];
        this.subProtocol = SubProtocol.valueOf(args[this.SUB_PROTOCOL_IDX]);

        switch (this.subProtocol) {
            case BACKUP: {
                if (args.length != 4) {
                    System.out.println("Usage: <peer_ap> BACKUP <path_name> <replication_degree>");
                    return false;
                }
                this.replicationDegree = Integer.parseInt(args[this.REPLICATION_DEGREE_IDX]);
                if (this.replicationDegree > 9) {
                    System.out.println("Replication degree must be one digit!");
                }
                this.path = args[this.PATH_IDX];
                break;
            }
            case RESTORE: {
                if (args.length != 3) {
                    System.out.println("Usage: <peer_ap> RESTORE <path_name>");
                    return false;
                }
                this.path = args[this.PATH_IDX];
            }
            case DELETE: {
                if (args.length != 3) {
                    System.out.println("Usage: <peer_ap> DELETE <path_name>");
                    return false;
                }
                this.path = args[this.PATH_IDX];
                break;
            }
            case RECLAIM: {
                if (args.length != 3) {
                    System.out.println("Usage: <peer_ap> RECLAIM <path_name>");
                    return false;
                }
                diskSpace = Double.parseDouble(args[this.DISK_SPACE_IDX]);
                break;
            }
            case STATE: {
                if (args.length != 2) {
                    System.out.println("Usage: <peer_ap> STATE");
                    return false;
                }
                break;
            }
            default: {
                System.out.println("Usage: <peer_ap> <sub_protocol> [<opnd_1>] [<opnd_2>]");
                return false;
            }
        }
        return true;
    }

    private void connectRmi() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            this.stub = (RemoteObject) registry.lookup(this.peerAp);
            System.out.println("Connected!");
        } catch (Exception e) {
            System.err.println("TestApp exception: " + e);
            e.printStackTrace();
        }
    }

    private void processRequest(SubProtocol protocol, File file) throws IOException, InterruptedException {
        String result = "";
        switch (protocol) {
            case STATE -> result = stub.state();
            case BACKUP -> result = stub.backup(file, replicationDegree);
            case DELETE -> result = stub.delete(file);
            case RECLAIM -> result = stub.reclaim(diskSpace);
            case RESTORE -> result = stub.restore(file);
        }
        System.out.println(result);
    }

    private void processRequest(SubProtocol protocol) throws IOException {
        String result = "";
        switch (protocol) {
            case RECLAIM -> result = stub.reclaim(diskSpace);
            case STATE -> result = stub.state();
            default -> System.out.println("File was null");
        }
        System.out.println(result);
    }


}