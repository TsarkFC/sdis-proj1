javac -cp . TestApp.java
# If you choose to use RMI in the communication between the test application and the peer,
#you should use as access point the name of the remote object providing the "testing" service.
#java -cp . TestApp access0 BACKUP files/321.txt 3
#java -cp . TestApp access1 BACKUP files/file.txt 3
#java -cp . TestApp access0 STATE
#java -cp . TestApp access0 DELETE files/321.txt
#java -cp . TestApp access0 RESTORE files/321.txt
java -cp . TestApp access1 RECLAIM 70
