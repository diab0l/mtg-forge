package forge.gui.match.controllers;

import java.util.Observable;
import java.util.Observer;

import forge.Command;
import forge.FThreads;
import forge.gui.framework.ICDoc;
import forge.gui.match.views.VLog;

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
    public Command getCommandOnSelect() {
        return null;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#initialize()
     */
    @Override
    public void initialize() {

    }
    
    private static Runnable updateConsole = new Runnable() {
        
        @Override
        public void run() {
            VLog.SINGLETON_INSTANCE.updateConsole();
        }
    };

    @Override
    public void update() {
        FThreads.invokeInEdtNowOrLater(updateConsole);
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(Observable arg0, Object arg1) {
        update();
    }

}
