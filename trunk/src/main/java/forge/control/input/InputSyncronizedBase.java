package forge.control.input;

import java.util.concurrent.CountDownLatch;

import forge.FThreads;
import forge.error.BugReporter;
import forge.game.player.Player;

public abstract class InputSyncronizedBase extends InputBase implements InputSynchronized { 
    private static final long serialVersionUID = 8756177361251703052L;
    
    private final CountDownLatch cdlDone;
    
    public InputSyncronizedBase(Player player) {
        super(player);
        cdlDone = new CountDownLatch(1);
    }
    
    public void awaitLatchRelease() {
        FThreads.assertExecutedByEdt(false);
        try{
            cdlDone.await();
        } catch (InterruptedException e) {
            BugReporter.reportException(e);
        }
    }
    

    @Override
    protected void afterStop() {
        cdlDone.countDown();
    }
}