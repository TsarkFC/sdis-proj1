#python3 run_peers.py <peers_num>

import subprocess, sys, os

# java Peer <protocol_version> <peer_id> <service_access_point> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>

peers_num = int(sys.argv[1])
protocol_version = "1.0"
service_access_point = "access"
mc_addr = ("228.25.25.25", "4445")
mdb_addr = ("228.25.25.25", "4446")
mdr_addr = ("228.25.25.25", "4447")

def peer():
    subprocess.run(["java", "-cp", ".", "Peer", "access"])
    #subprocess.run(["java", "Peer", "1.0", str(i), mc_addr[0], mc_addr[1], mdb_addr[0], mdb_addr[1], mdr_addr[0], mdr_addr[1]])
    os._exit(0)  

def start():
    subprocess.run(["fuser", "-k", "1099/tcp"], env=dict(CLASSPATH='', **os.environ))
    subprocess.run("rmiregistry &", shell=True)
    subprocess.run(["javac", "-cp", ".", "Peer.java"])

    for i in range(peers_num):
        newpid = os.fork()
        if newpid == 0:
            peer()

start()