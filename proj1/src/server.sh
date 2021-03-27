export CLASSPATH=
fuser -k 1099/tcp
rmiregistry &
javac -cp . peer/Peer.java
java -cp . peer/Peer 1.0 1 access 228.25.25.25 4445 228.25.25.25 4446 228.25.25.25 4447




#java peer.Peer <protocol_version> <peer_id> <service_access_point> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>