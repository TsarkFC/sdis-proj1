javac -cp . TestApp.java
# If you choose to use RMI in the communication between the test application and the peer,
#you should use as access point the name of the remote object providing the "testing" service.
java -cp . TestApp access1 BACKUP files/321.txt 3
