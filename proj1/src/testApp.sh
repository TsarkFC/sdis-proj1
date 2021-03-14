javac -cp . Server.java TestApp.java
#If you choose to use RMI in the communication between the test application and the peer,
# you should use as access point the name of the remote object providing the "testing" service.
java -cp . TestApp access BACKUP test1.pdf 3