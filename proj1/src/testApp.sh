# If you choose to use RMI in the communication between the test application and the peer,
#you should use as access point the name of the remote object providing the "testing" service.
export CLASSPATH=
cd build
################# Simple ###############################
#../../scripts/test.sh access0 BACKUP ../files/321.txt 3
../../scripts/test.sh access0 RESTORE ../files/321.txt
#../../scripts/test.sh access0 BACKUP ../files/file.txt 2
#../../scripts/test.sh access0 DELETE ../files/321.txt
#../../scripts/test.sh access0 RESTORE ../files/321.txt
#../../scripts/test.sh access1 RECLAIM 70
#../../scripts/test.sh access1 STATE

#Big image
#../../scripts/test.sh access1 BACKUP ../files/bigimage.jpg 3
#../../scripts/test.sh access0 RESTORE ../files/bigimage.jpg
#../../scripts/test.sh access0 DELETE ../files/bigimage.jpg
#../../scripts/test.sh access0 BACKUP ../files/5mb.jpg 3
#../../scripts/test.sh access0 RESTORE ../files/5mb.jpg

#Big text file
#../../scripts/test.sh access0 BACKUP ../files/bigtextfile.txt 3
#../../scripts/test.sh access0 RESTORE ../files/bigtextfile.txt


################# TEST ENHANCEMENT DELETE ###################
#Run backup 10 Peers - python3 run_peers.py 10 1.1 yes
#../../scripts/test.sh access0 BACKUP ../files/321.txt 9
#Run Delete with 2 peers - python3 run_peers.py 2 1.1 yes
#../../scripts/test.sh access0 DELETE ../files/321.txt
#Start 10 peers and see if deletes the remaining files

################# TEST RECLAIM ###################
#Run with 4 peers, so the perceived rep degree is 3
#../../scripts/test.sh access0 BACKUP ../files/file.txt 4
#../../scripts/test.sh access0 BACKUP ../files/bigimage.jpg 2
#../../scripts/test.sh access1 RECLAIM 70
# It should delete the image and keep the file

#Test reclaim and following backup
#TODO Se ele for o unico que tem o ficheiro, o que e suposto acontecer?
#Run backup with 3 peers
../../scripts/test.sh access0 BACKUP ../files/bigimage.jpg 2
#Run reclaim with 4 peers
#../../scripts/test.sh access1 RECLAIM 70
#It should backup the other chunks in 2
#../../scripts/test.sh access1 STATE
#Verify if peer 1 is NOT hosting anything

#Test RECLAIM 0
#../../scripts/test.sh access0 BACKUP ../files/bigimage.jpg 2
#../../scripts/test.sh access1 RECLAIM 0
#../../scripts/test.sh access0 RESTORE ../files/bigimage.jpg
#../../scripts/test.sh access0 RECLAIM 0
