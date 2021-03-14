fuser -k 1099/tcp
rmiregistry &
javac -cp . Server.java
java -cp . Server access