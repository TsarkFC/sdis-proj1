package channels;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ChannelCoordinator {
    private boolean mcReceiving = true;
    private ScheduledThreadPoolExecutor executor;
    private BackupChannel backupChannel;
    private ControlChannel controlChannel;

    public ChannelCoordinator(BackupChannel backupChannel,ControlChannel controlChannel){
        this.backupChannel=backupChannel;
        this.controlChannel = controlChannel;
    }


    public void closeMcIn1Second(){
        executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(this::closeMc,1, TimeUnit.SECONDS);
    }

    public void closeMc(){
        this.controlChannel.closeMcChannel();
    }
}
