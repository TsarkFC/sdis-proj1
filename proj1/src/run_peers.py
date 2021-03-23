#python3 run_peers.py <peers_num>

from signal import signal, SIGINT
import subprocess, sys, os

# java peer.Peer <protocol_version> <peer_id> <service_access_point> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>

peers_num = int(sys.argv[1])
protocol_version = "1.0"
service_access_point = "access"
mc_addr = ("228.25.25.25", "4445")
mdb_addr = ("228.25.25.25", "4446")
mdr_addr = ("228.25.25.25", "4447")

def handler(signal_received, frame):
    print('SIGINT or CTRL-C detected. Exiting gracefully')
    os._exit(0)

def peer(i):
    subprocess.run(["java", "peer/Peer", "1.0", str(i), "access" + str(i), mc_addr[0], mc_addr[1], mdb_addr[0], mdb_addr[1], mdr_addr[0], mdr_addr[1]])
    os._exit(0)  

def start():
	signal(SIGINT, handler)
	subprocess.run(["fuser", "-k", "1099/tcp"])
	subprocess.run("rmiregistry &", shell=True)
	subprocess.run(["javac", "peer/Peer.java"])

	for i in range(peers_num):
		newpid = os.fork()
		if newpid == 0:
			peer(i)

	print('Running. Press CTRL-C to exit.')
	while True:
		# Do nothing and hog CPU forever until SIGINT received.
		pass

start()
