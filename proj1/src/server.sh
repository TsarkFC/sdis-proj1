export CLASSPATH=
fuser -k 1099/tcp
rmiregistry &
javac -cp . peer/Peer.java
java -cp . Peer access


#java peer.Peer <protocol_version> <peer_id> <service_access_point> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>