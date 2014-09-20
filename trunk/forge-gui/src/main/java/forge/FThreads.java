package forge;

import java.io.PrintStream;

import forge.interfaces.IGuiBase;
import forge.util.ThreadUtil;

public class FThreads {
    private FThreads() { } // no instances supposed

    /** Checks if calling method uses event dispatch thread.
     * Exception thrown if method is on "wrong" thread.
     * A boolean is passed to indicate if the method must be EDT or not.
     * 
     * @param methodName &emsp; String, part of the custom exception message.
     * @param mustBeEDT &emsp; boolean: true = exception if not EDT, false = exception if EDT
     */
    public static void assertExecutedByEdt(final IGuiBase gui, final boolean mustBeEDT) {
        if (isGuiThread(gui) != mustBeEDT) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            final String methodName = trace[2].getClassName() + "." + trace[2].getMethodName();
            String modalOperator = mustBeEDT ? " must be" : " may not be";
            throw new IllegalStateException(methodName + modalOperator + " accessed from the event dispatch thread.");
        }
    }

    public static void invokeInEdtLater(final IGuiBase gui, final Runnable runnable) {
        gui.invokeInEdtLater(runnable);
    }

    public static void invokeInEdtNowOrLater(final IGuiBase gui, final Runnable proc) {
        if (isGuiThread(gui)) {
            proc.run();
        }
        else {
            invokeInEdtLater(gui, proc);
        }
    }

    /**
     * Invoke the given Runnable in an Event Dispatch Thread and wait for it to
     * finish; but <B>try to use SwingUtilities.invokeLater instead whenever
     * feasible.</B>
     * 
     * Exceptions generated by SwingUtilities.invokeAndWait (if used), are
     * rethrown as RuntimeExceptions.
     * 
     * @param proc
     *            the Runnable to run
     * @see fgd.SwingUtilities#invokeLater(Runnable)
     */
    public static void invokeInEdtAndWait(final IGuiBase gui, final Runnable proc) {
        gui.invokeInEdtAndWait(proc);
    }

    private static int backgroundThreadCount;
    public static void invokeInBackgroundThread(final Runnable proc) {
        //start thread name with "Game" so isGuiThread() returns false on GuiMobile
        new Thread(proc, "Game BT" + backgroundThreadCount).start();
        backgroundThreadCount++;
    }

    public static boolean isGuiThread(IGuiBase gui) {
        return gui.isGuiThread();
    }

    public static void delayInEDT(final IGuiBase gui, final int milliseconds, final Runnable inputUpdater) {
        Runnable runInEdt = new Runnable() {
            @Override
            public void run() {
                FThreads.invokeInEdtNowOrLater(gui, inputUpdater);
            }
        };
        ThreadUtil.delay(milliseconds, runInEdt);
    }

    public static String debugGetCurrThreadId(final IGuiBase gui) {
        return isGuiThread(gui) ? "EDT" : Thread.currentThread().getName();
    }

    public static String prependThreadId(final IGuiBase gui, String message) {
        return debugGetCurrThreadId(gui) + " > " + message;
    }

    public static void dumpStackTrace(final IGuiBase gui, final PrintStream stream) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        stream.printf("%s > %s called from %s%n", debugGetCurrThreadId(gui),
                trace[2].getClassName() + "." + trace[2].getMethodName(), trace[3].toString());
        int i = 0;
        for (StackTraceElement se : trace) {
            if (i<2) { i++; }
            else { stream.println(se.toString()); }
        }
    }

    public static String debugGetStackTraceItem(final IGuiBase gui, final int depth, final boolean shorter) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        String lastItem = trace[depth].toString();
        if (shorter) {
            int lastPeriod = lastItem.lastIndexOf('.');
            lastPeriod = lastItem.lastIndexOf('.', lastPeriod-1);
            lastPeriod = lastItem.lastIndexOf('.', lastPeriod-1);
            lastItem = lastItem.substring(lastPeriod+1);
            return String.format("%s > from %s", debugGetCurrThreadId(gui), lastItem);
        }
        return String.format("%s > %s called from %s", debugGetCurrThreadId(gui),
                trace[2].getClassName() + "." + trace[2].getMethodName(), lastItem);
    }

    public static String debugGetStackTraceItem(final IGuiBase gui, final int depth) {
        return debugGetStackTraceItem(gui, depth, false);
    }
}