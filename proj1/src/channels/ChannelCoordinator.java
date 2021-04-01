package channels;

import peer.Peer;
import utils.AddressList;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ChannelCoordinator {
    private boolean mcReceiving = true;
    private ScheduledThreadPoolExecutor executor;
    private BackupChannel backupChannel;
    private ControlChannel controlChannel;
    private RestoreChannel restoreChannel;
    private Peer peer;

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

    public void closeMcIn1Second() {
        executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(this::closeMc, 1, TimeUnit.SECONDS);
    }

    public void closeMc() {
        this.controlChannel.closeMcChannel();
    }
}
