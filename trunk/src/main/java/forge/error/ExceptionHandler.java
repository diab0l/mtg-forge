/**
 * ExceptionHandler.java
 *
 * Created on 27.09.2009
 */

package forge.error;


import com.esotericsoftware.minlog.Log;

import java.lang.Thread.UncaughtExceptionHandler;


/**
 * This class handles all exceptions that weren't caught by showing the error to the user.
 *
 * @author Forge
 * @version $Id$
 */
public class ExceptionHandler implements UncaughtExceptionHandler {
    static {
        //Tells Java to let this class handle any uncaught exception
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        //Tells AWT to let this class handle any uncaught exception
        System.setProperty("sun.awt.exception.handler", ExceptionHandler.class.getName());
    }

    /**
     * Call this at the beginning to make sure that the class is loaded and the static initializer has run
     */
    public static void registerErrorHandling() {
        Log.debug("Error handling registered!");
    }


    /** {@inheritDoc} */
    public void uncaughtException(Thread t, Throwable ex) {
        ErrorViewer.showError(ex);
    }

    /**
     * This Method is called by AWT when an error is thrown in the event dispatching thread and not caught.
     *
     * @param ex a {@link java.lang.Throwable} object.
     */
    public void handle(Throwable ex) {
        ErrorViewer.showError(ex);
    }
}
