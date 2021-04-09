package channels;

import peer.Peer;
import protocol.BackupProtocolInitiator;
import utils.AddressList;
import utils.FileHandler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class ChannelCoordinator {
    private boolean mcReceiving = true;
    private ScheduledThreadPoolExecutor executor;
    private BackupChannel backupChannel;
    private ControlChannel controlChannel;
    private RestoreChannel restoreChannel;
    private Peer peer;
    private BackupProtocolInitiator backupInitiator;
    private ConcurrentHashMap<String,Integer> receivedStoredMsg = new ConcurrentHashMap<>();
;
    public ChannelCoordinator( Peer peer){
        this.peer = peer;
        AddressList addressList = peer.getPeerArgs().getAddressList();
        this.createMDBChannel(addressList);
        this.createMCChannel(addressList);
        this.createMDRChannel(addressList);
    }

    public BackupChannel createMDBChannel(AddressList addressList) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        backupChannel = new BackupChannel(addressList, peer);
        executor.schedule(backupChannel, 0, TimeUnit.SECONDS);
        return backupChannel;
    }

    public ControlChannel createMCChannel(AddressList addressList) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        controlChannel = new ControlChannel(addressList, peer);
        executor.schedule(controlChannel, 0, TimeUnit.SECONDS);
        return controlChannel;
    }

    public RestoreChannel createMDRChannel(AddressList addressList) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        restoreChannel = new RestoreChannel(addressList, peer);
        executor.schedule(restoreChannel, 0, TimeUnit.SECONDS);
        return restoreChannel;
    }

    public BackupProtocolInitiator getBackupInitiator() {
        return backupInitiator;
    }

    public void setBackupInitiator(BackupProtocolInitiator backupInitiator) {
        this.backupInitiator = backupInitiator;
    }


}
