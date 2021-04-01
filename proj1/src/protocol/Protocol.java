package protocol;

import messages.Delete;
import peer.Peer;
import peer.PeerArgs;
import utils.FileHandler;
import utils.ThreadHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class Protocol {
    protected File file;
    protected Peer peer;

    public Protocol(File file, Peer peer) {
        this.file = file;
        this.peer = peer;
    }
    public abstract void initialize();
}
