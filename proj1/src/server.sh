export CLASSPATH=
fuser -k 1099/tcp
rmiregistry &
javac -cp . Peer.java
java -cp . Peer access
