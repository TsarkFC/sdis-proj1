# If you choose to use RMI in the communication between the test application and the peer,
#you should use as access point the name of the remote object providing the "testing" service.
export CLASSPATH=
cd build
################# Simple ###############################
#../../scripts/test.sh access0 BACKUP ../files/321.txt 3
#../../scripts/test.sh access0 RESTORE ../files/321.txt
#../../scripts/test.sh access1 RECLAIM 70
#../../scripts/test.sh access0 STATE
#../../scripts/test.sh access1 STATE
#../../scripts/test.sh access0 DELETE ../files/321.txt

#81 chunk Image TODO ver se nos da feup tb corre bem
#../../scripts/test.sh access0 BACKUP ../files/5mb.jpg 3
#../../scripts/test.sh access0 RESTORE ../files/5mb.jpg
#../../scripts/test.sh access1 RECLAIM 70
#../../scripts/test.sh access0 STATE
#../../scripts/test.sh access1 STATE
#../../scripts/test.sh access0 DELETE ../files/5mb.jpg

################# TEST MANY SAME TIME BACKUP ###################
#Run all backups at same time
#../../scripts/test.sh access0 BACKUP ../files/file.txt 1
#../../scripts/test.sh access0 BACKUP ../files/321.txt 1
#../../scripts/test.sh access0 BACKUP ../files/bigimage.jpg 1
#../../scripts/test.sh access0 BACKUP ../files/5mb.jpg 1

#../../scripts/test.sh access0 RESTORE ../files/file.txt
#../../scripts/test.sh access0 RESTORE ../files/321.txt
#../../scripts/test.sh access0 RESTORE ../files/bigimage.jpg
#../../scripts/test.sh access0 RESTORE ../files/5mb.jpg

#../../scripts/test.sh access0 DELETE ../files/file.txt
#../../scripts/test.sh access0 DELETE ../files/321.txt
#../../scripts/test.sh access0 DELETE ../files/bigimage.jpg
#../../scripts/test.sh access0 DELETE ../files/5mb.jpg

################# TEST RECLAIM ###################
#Backup and following reclaim
  #Run with 4 peers, so the perceived rep degree is 3
  #../../scripts/test.sh access0 BACKUP ../files/file.txt 4
  #../../scripts/test.sh access0 BACKUP ../files/bigimage.jpg 2
  #../../scripts/test.sh access1 RECLAIM 70
  # It should delete the image and keep the file since RepDgrImg(4) > PercDgrImg (3) > RepDgrFile(2)

#Test reclaim and following backup
  #Run backup with 3 peers
  #../../scripts/test.sh access0 BACKUP ../files/bigimage.jpg 2
  #Run reclaim with 4 peers
  #../../scripts/test.sh access1 RECLAIM 70
  #It should backup the other chunks in 3
  #Verify if peer 2 is NOT hosting anything
  #../../scripts/test.sh access2 STATE
  #Verify if peer 1 is NOT storing the 3 chunks
  ../../scripts/test.sh access1 STATE

#Testar se todos os peers, mm que nao guardem o chunk nao estao a guardar no stored
#O backup enhanced nao esta a guardar os chunks que nao sao suposto
#O reclaim normal nao guarda no 1 os outros stored

#O problema é o backup protocol initiator com enhanced



################# TEST ENHANCEMENT BACKUP ###################
# Run backup with 10 peers version 1.1
#../../scripts/test.sh access0 BACKUP ../files/file.txt 4
#../../scripts/test.sh access0 RESTORE ../files/file.txt


################# TEST ENHANCEMENT DELETE ###################
#Run backup 10 Peers - python3 run_peers.py 10 1.1 yes
#../../scripts/test.sh access0 BACKUP ../files/321.txt 9
#Run Delete with 2 peers - python3 run_peers.py 2 1.1 yes
#../../scripts/test.sh access0 DELETE ../files/321.txt
#Start 10 peers and see if deletes the remaining files



#Test RECLAIM 0
#../../scripts/test.sh access0 BACKUP ../files/bigimage.jpg 2
#../../scripts/test.sh access1 RECLAIM 0
#../../scripts/test.sh access0 RESTORE ../files/bigimage.jpg
#../../scripts/test.sh access0 RECLAIM 0
