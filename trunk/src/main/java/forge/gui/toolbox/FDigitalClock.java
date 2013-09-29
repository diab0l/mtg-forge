package forge.gui.toolbox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JLabel;
import javax.swing.Timer;

/** 
 * Digital clock label that displays current time
 *
 */
@SuppressWarnings("serial")
public class FDigitalClock extends JLabel {
    private final Calendar now;
    private final SimpleDateFormat format = new SimpleDateFormat("h:mm a");

    public FDigitalClock() {
        Timer timer = new Timer(60000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                now.add(Calendar.MINUTE, 1);
                updateDisplay();
            }
        });
        now = Calendar.getInstance();
        updateDisplay();
        //ensure timer starts when current minute ends
        timer.setInitialDelay(60000 - (now.get(Calendar.MILLISECOND) + now.get(Calendar.SECOND) * 1000));
        timer.start();
    }
    
    private void updateDisplay() {
        setText(format.format(now.getTime()));
    }
}
