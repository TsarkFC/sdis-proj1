package peer;

import utils.MulticastAddress;

public class PeerArgs {
    public static final Integer VERSION = 0;
    public static final Integer PEER_ID = 1;
    public static final Integer ACCESS_POINT = 2;
    public static final Integer MC_ADDR = 3;
    public static final Integer MC_PORT = 4;
    public static final Integer MDB_ADDR = 5;
    public static final Integer MDB_PORT = 6;
    public static final Integer MDR_ADDR = 7;
    public static final Integer MDR_PORT = 8;

    public String getVersion() {
        return version;
    }

    public Integer getPeerId() {
        return peerId;
    }

    public String getAccessPoint() {
        return accessPoint;
    }

    public MulticastAddress getMcAddr() { return mcAddr; }

    public MulticastAddress getMdbAddr() { return mdbAddr; }

    public MulticastAddress getMdrAddr() { return mdrAddr; }

    //java Peer <protocol_version> <peer_id> <service_access_point> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>
    String version;
    Integer peerId;
    String accessPoint;

    MulticastAddress mcAddr;
    MulticastAddress mdbAddr;
    MulticastAddress mdrAddr;

    public PeerArgs(String[] args) {
        version = args[VERSION];
        peerId = Integer.parseInt(args[PEER_ID]);
        accessPoint = args[ACCESS_POINT];
        mcAddr = new MulticastAddress(args[MC_ADDR], Integer.parseInt(args[MC_PORT]));
        mdbAddr = new MulticastAddress(args[MDB_ADDR], Integer.parseInt(args[MDB_PORT]));
        mdrAddr = new MulticastAddress(args[MDR_ADDR], Integer.parseInt(args[MDR_PORT]));
    }
}
