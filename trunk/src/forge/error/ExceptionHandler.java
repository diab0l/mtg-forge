/**
* ExceptionHandler.java
*
* Created on 27.09.2009
*/

package forge.error;


import java.lang.Thread.UncaughtExceptionHandler;

import com.esotericsoftware.minlog.Log;


/**
* This class handles all exceptions that weren't caught by showing the error to the user.
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
   
    
    public void uncaughtException(Thread t, Throwable ex) {
        ErrorViewer.showError(ex);
    }
   
    /**
     * This Method is called by AWT when an error is thrown in the event dispatching thread and not caught.
     */
    public void handle(Throwable ex) {
        ErrorViewer.showError(ex);
    }
}
