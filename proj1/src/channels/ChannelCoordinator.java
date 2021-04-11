package channels;

import peer.Peer;
import protocol.BackupProtocolInitiator;
import utils.AddressList;

import java.util.concurrent.*;

public class ChannelCoordinator {
    private final boolean mcReceiving = true;
    private ScheduledThreadPoolExecutor executor;
    private final Peer peer;
    private BackupProtocolInitiator backupInitiator;
    private final ConcurrentHashMap<String, Integer> receivedStoredMsg = new ConcurrentHashMap<>();

    public ChannelCoordinator(Peer peer) {
        this.peer = peer;
        AddressList addressList = peer.getArgs().getAddressList();
        this.createMDBChannel(addressList);
        this.createMCChannel(addressList);
        this.createMDRChannel(addressList);
    }

    public void createMDBChannel(AddressList addressList) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        BackupChannel backupChannel = new BackupChannel(addressList, peer);
        executor.schedule(backupChannel, 0, TimeUnit.SECONDS);
    }

    public void createMCChannel(AddressList addressList) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        ControlChannel controlChannel = new ControlChannel(addressList, peer);
        executor.schedule(controlChannel, 0, TimeUnit.SECONDS);
    }

    public void createMDRChannel(AddressList addressList) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        RestoreChannel restoreChannel = new RestoreChannel(addressList, peer);
        executor.schedule(restoreChannel, 0, TimeUnit.SECONDS);
    }

    public BackupProtocolInitiator getBackupInitiator() {
        return backupInitiator;
    }

    public void setBackupInitiator(BackupProtocolInitiator backupInitiator) {
        this.backupInitiator = backupInitiator;
    }


}
