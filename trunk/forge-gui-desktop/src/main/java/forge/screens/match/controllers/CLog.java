package forge.screens.match.controllers;

import forge.UiCommand;
import forge.FThreads;
import forge.gui.framework.ICDoc;
import forge.screens.match.views.VLog;
import java.util.Observable;
import java.util.Observer;

/** 
 * Controls the combat panel in the match UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
public enum CLog implements ICDoc, Observer {
    /** */
    SINGLETON_INSTANCE;

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public UiCommand getCommandOnSelect() {
        return null;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#initialize()
     */
    @Override
    public void initialize() {

    }
    
    private final Runnable r = new Runnable() {
        @Override
        public void run() {
            VLog.SINGLETON_INSTANCE.updateConsole();
        }
    };

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(final Observable o, final Object arg) {
        update();
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#update()
     */
    @Override
    public void update() {
        FThreads.invokeInEdtNowOrLater(r);
    }
}
