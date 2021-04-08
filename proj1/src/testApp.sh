# If you choose to use RMI in the communication between the test application and the peer,
#you should use as access point the name of the remote object providing the "testing" service.
export CLASSPATH=
cd build
#../../scripts/test.sh access0 BACKUP ../files/321.txt 3
#../../scripts/test.sh access0 BACKUP ../files/3212.txt 5
#../../scripts/test.sh access1 BACKUP ../files/file.txt 3
#../../scripts/test.sh access0 DELETE ../files/321.txt
#../../scripts/test.sh access0 RESTORE ../files/321.txt
../../scripts/test.sh access2 RECLAIM 130
