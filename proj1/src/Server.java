import java.io.*;
import java.util.HashMap;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

//java Server <remote_object_name>
public class Server implements RemoteObject {
    private final HashMap<String, String> dnsIp = new HashMap<>();

    public Server() {
        dnsIp.put("tsark.com", "128.0.0.0");
    }

    // <oper> <opnd> * :: <out>
    public String processRequest(String request) {
        String[] parsed = request.split(" ");
        if (parsed[0].equals("REGISTER")) {
            String out = register(parsed);
            System.out.println(parsed[0] + " " + parsed[1] + " " + parsed[2] + " :: " + out);
            return out;
        } else if (parsed[0].equals("LOOKUP")) {
            String out = lookup(parsed);
            System.out.println(parsed[0] + " " + parsed[1] + " :: " + out);
            return out;
        }
        System.out.println(parsed[0] + " " + parsed[1] + " :: null");
        return "";
    }

    private String register(String[] parsed) {
        String dns = parsed[1];
        String ip = parsed[2];
        if (dnsIp.get(dns) != null) {
            return "Entry already exists";
        }
        dnsIp.put(dns, ip);
        return "Number of DNS names registered: " + dnsIp.size();
    }

    private String lookup(String[] parsed) {
        String dns = parsed[1];
        String response = dnsIp.get(dns);
        if (response == null) {
            return "No entry for DNS address: " + dns;
        }
        return dns + " " + response;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Server <remote_object_name>");
            return;
        }

        String remote_object_name = args[0];

        try {
            Server obj = new Server();
            RemoteObject stub = (RemoteObject) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(remote_object_name, stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}