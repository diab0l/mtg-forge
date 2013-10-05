package forge.gui.framework;

import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import forge.FThreads;

/** 
 * Experimental static factory for generic operations carried out
 * onto specific members of the framework. Doublestrike 11-04-12
 * 
 * <br><br><i>(S at beginning of class name denotes a static factory.)</i>
 */
public class SDisplayUtil {
    private static boolean remindIsRunning = false;
    private static int counter = 0;
    private static int[] newA = null, newR = null, newG = null, newB = null;
    private static Timer timer1 = null;

    /** Flashes animation on input panel if play is currently waiting on input.
     * 
     * @param tab0 &emsp; {@link java.gui.framework.IVDoc}
     */
    public static void remind(final IVDoc<? extends ICDoc> tab0) {
        showTab(tab0);
        final JPanel pnl = tab0.getParentCell().getBody();

        // To adjust, only touch these two values.
        final int steps = 5;    // Number of delays
        final int delay = 80;  // Milliseconds between steps

        if (remindIsRunning) { return; }
        if (pnl == null) { return; }

        remindIsRunning = true;
        final int oldR = pnl.getBackground().getRed();
        final int oldG = pnl.getBackground().getGreen();
        final int oldB = pnl.getBackground().getBlue();
        final int oldA = pnl.getBackground().getAlpha();
        counter = 0;
        newR = new int[steps];
        newG = new int[steps];
        newB = new int[steps];
        newA = new int[steps];

        for (int i = 0; i < steps; i++) {
            newR[i] = ((255 - oldR) / steps * i);
            newG[i] = (oldG / steps * i);
            newB[i] = (oldB / steps * i);
            newA[i] = ((255 - oldA) / steps * i);
        }

        final TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                counter++;
                if (counter != (steps - 1)) {
                    SwingUtilities.invokeLater(new Runnable() { @Override
                        public void run() {
                            int r = newR == null ? oldR : newR[counter];
                            int a = newA == null ? oldA : newR[counter];
                            pnl.setBackground(new Color(r, oldG, oldB, a));
                        }
                    });
                }
                else {
                    SwingUtilities.invokeLater(new Runnable() { @Override
                        public void run() { pnl.setBackground(new Color(oldR, oldG, oldB, oldA)); } });
                    remindIsRunning = false;
                    timer1.cancel();
                    newR = null;
                    newG = null;
                    newB = null;
                    newA = null;
                }
            }
        };

        timer1 = new Timer();
        timer1.scheduleAtFixedRate(tt, 0, delay);
    }

    /** @param tab0 &emsp; {@link java.gui.framework.IVDoc} */
    public static void showTab(final IVDoc<? extends ICDoc> tab0) {
        
        Runnable showTabRoutine = new Runnable() {
            @Override
            public void run() {
                // FThreads.assertExecutedByEdt(true); - always true
                Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
                DragCell dc = tab0.getParentCell();
                if (dc != null)
                    dc.setSelected(tab0);
                // set focus back to previous owner, if any
                if (null != c) {
                    c.requestFocusInWindow();
                }
            }
        };
        FThreads.invokeInEdtLater(showTabRoutine);
    }
    
    public static Rectangle getScreenBoundsForPoint(Point point) {
        Rectangle bounds;
        for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            for (GraphicsConfiguration config : device.getConfigurations()) {
                bounds = config.getBounds();
                if (bounds.contains(point)) {
                    return bounds;
                }
            }
        }
        //return bounds of default monitor if point not on any screen
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    }
    
    public static Rectangle getScreenMaximizedBounds(Point point) {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration graphicsConfiguration = null;
        for (GraphicsDevice gd : env.getScreenDevices()) {
            if (gd.getDefaultConfiguration().getBounds().contains(point)) {
                graphicsConfiguration = gd.getDefaultConfiguration();
                break;
            }
        }
        if (graphicsConfiguration == null) {
            return env.getMaximumWindowBounds();
        }

        Rectangle bounds = graphicsConfiguration.getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration);

        bounds.x += screenInsets.left;
        bounds.y += screenInsets.top;
        bounds.height -= screenInsets.bottom;
        bounds.width -= screenInsets.right;
        return bounds;
    }
}
