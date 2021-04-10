# If you choose to use RMI in the communication between the test application and the peer,
#you should use as access point the name of the remote object providing the "testing" service.
export CLASSPATH=
cd build
../../scripts/test.sh access0 BACKUP ../files/321.txt 4
#../../scripts/test.sh access0 BACKUP ../files/file.txt 2
#../../scripts/test.sh access0 BACKUP ../files/file.txt 3
#../../scripts/test.sh access1 BACKUP ../files/file.txt 3
#../../scripts/test.sh access1 DELETE ../files/file.txt
#../../scripts/test.sh access0 DELETE ../files/321.txt
#../../scripts/test.sh access0 RESTORE ../files/321.txt
#../../scripts/test.sh access1 RECLAIM 70

#../../scripts/test.sh access0 BACKUP ../files/bigimage.jpg 3
#../../scripts/test.sh access0 RESTORE ../files/bigimage.jpg
#../../scripts/test.sh access0 DELETE ../files/bigimage.jpg

#../../scripts/test.sh access0 BACKUP ../files/bigtextfile.txt 3
#../../scripts/test.sh access0 RESTORE ../files/bigtextfile.txt

#../../scripts/test.sh access0 STATE
