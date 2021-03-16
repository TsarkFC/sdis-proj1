import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class ClientLab2 {

    public static final String REGISTER_ARG= "register";
    public static final String LOOKUP_ARG = "lookup";
    public static final String REGISTER_MSG= "REGISTER";
    public static final String LOOKUP_MSG  = "LOOKUP";
    public static final String BROADCAST_LOCAL_ADDR  = "255.255.255.255";
    private static final int FAILED_REGISTER = -1;
    private static final String NOT_FOUND = "NOT_FOUND";
    private static final int MCAST_ADDR_IDX = 0;
    private static final int MCAST_PORT_IDX = 1;
    private static final int OPERATION_INDEX = 2;
    private static final int DNS_INDEX = 3;
    //Vou assumir que o cliente pede na mesma pelo IP correspondente
    private static final int IP_INDEX = 4;
    private static final int SERVER_PORT_IDX = 0;
    private static final int SERVER_ADDR_IDX = 1;
    private static String mcast_addr;
    private static String mcast_port;
    private static String oper;
    private static String dns;
    private static String ipAddr;
    private static boolean isReg;

    public static void main(String[] args) throws IOException {
        if(args.length < 3){
            System.out.println("Error please insert the following arguments\n\njava client <mcast_addr> <mcast_port> <oper> <opnd> *\n");
            System.out.println("<mcast_addr> is the IP address of the multicast group used by the server to advertise its service;\n" +
                    "<mcast_port> is the port number of the multicast group used by the server to advertise its service;n" +
                    "<oper> is the operation to request from the server, either \"register\" or \"lookup\"\n" +
                    "<opnd>*\n" +
                    "\tis the list of operands of that operation\n" +
                    "\t<DNS name> <IP address> for register\n" +
                    "\t<DNS name> for lookup");
            return;
        }

        String request = getRequest(args);
        if (request == "") return;
        String[] serverMsg = getServiceMsg();
        sendCommand(request,Integer.parseInt(serverMsg[SERVER_PORT_IDX]),serverMsg[SERVER_ADDR_IDX]);

    }


    private static String[] getServiceMsg() throws IOException {
        InetAddress group = InetAddress.getByName(mcast_addr);
        MulticastSocket mcastSocket = new MulticastSocket(Integer.parseInt(mcast_port));
        mcastSocket.joinGroup(group);

        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        mcastSocket.receive(packet);

        mcastSocket.leaveGroup(group);
        mcastSocket.close();

        return new String(packet.getData()).trim().split(" ");
    }

    private static void sendCommand(String request,int serverPort,String serverAddr) throws IOException {
        System.out.println("Recieved Port: " + serverPort + " and Addr " + serverAddr);
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(2000);
        InetAddress address = InetAddress.getByName(serverAddr);
        byte[] buf = request.getBytes();
        DatagramPacket packet = new DatagramPacket(buf,buf.length,address,serverPort);
        socket.send(packet);

        buf = new byte[256];
        packet = new DatagramPacket(buf,buf.length);
        socket.receive(packet);
        parseRecieved(new String(packet.getData()));
        socket.close();

    }


    private static void parseRecieved(String received) {
        received = received.trim();
        if (oper.equals("register")) {
            //System.out.println("Client: "+args[2]+" "+args[3]+" "+args[4]);
            if (received.equals(String.valueOf(FAILED_REGISTER))) System.out.println("Failed: Already registered");
            else System.out.println("Number of bindings in the service: " + received);
        } else if (oper.equals("lookup")) {
            //System.out.println("Client: "+args[2]+" "+args[3]);
            if(received.equals(NOT_FOUND)) System.out.println(NOT_FOUND);
            else {
                System.out.println("Ip Adress: " + received);
            }
        }else{
            System.out.println(oper + "RIP");
        }
        System.out.println("\n");
    }



    private static  String buildRegMsg(String dnsName, String ipAddr){
        return REGISTER_MSG + " " + dnsName + " " + ipAddr;
    }
    private static  String buildLookupMsg(String dnsName){
        return LOOKUP_MSG + " " + dnsName;
    }
    private void printSubmittingRequest(String oper,String[] opnds,String result){
        //<result>is result returned by the server or "ERROR", if an error occurs
        String msg = "Client: " + oper;
        for (String opnd : opnds)  msg += " " + opnd;
        msg += " : " + result;
        System.out.println(msg);
    }

    private static String getRequest(String[] args) {

        mcast_addr = args[MCAST_ADDR_IDX];
        mcast_port  = args[MCAST_PORT_IDX];
        oper = args[OPERATION_INDEX];
        String request = "";

        if (oper.equals(REGISTER_ARG)) {
            if (args.length != 5) {
                System.out.println("Usage: java Client <host> <port> register <DNS name> <IP address>");
                return "";
            }
            dns = args[DNS_INDEX];
            ipAddr = args[IP_INDEX];
            isReg = true;
            request =  buildRegMsg(args[DNS_INDEX] , args[IP_INDEX]);
        } else if (oper.equals("lookup")) {
            if (args.length != 4) {
                System.out.println("Usage: java Client <host> <port> lookup <DNS name>");
                return "";
            }
            dns = args[DNS_INDEX];
            isReg = false;
            request = buildLookupMsg(args[DNS_INDEX]);
        } else {
            System.out.println("Invalid operation! (Can either be 'register' or 'lookup')");
        }
        return request;
    }

    //If the objective was to broadcast
    public static void broadcast(
            String broadcastMessage, InetAddress address) throws IOException {
        DatagramSocket socket;
        socket = new DatagramSocket();
        socket.setBroadcast(true);

        byte[] buffer = broadcastMessage.getBytes();

        DatagramPacket packet
                = new DatagramPacket(buffer, buffer.length, address, 4445);
        socket.send(packet);
        socket.close();
    }
    //If the objective was to iterate through all NetworkInterfaces to find their broadcast address
    List<InetAddress> listAllBroadcastAddresses() throws SocketException {
        List<InetAddress> broadcastList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces
                = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            networkInterface.getInterfaceAddresses().stream()
                    .map(a -> a.getBroadcast())
                    .filter(Objects::nonNull)
                    .forEach(broadcastList::add);
        }
        return broadcastList;
    }
}
