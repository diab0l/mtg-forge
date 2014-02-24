package forge.toolbox;

import java.util.Date;

import forge.Forge.Graphics;

public class FProgressBar extends FDisplayObject {
    private long startMillis = 0;
    private int tempVal = 0, etaSecs = 0, maximum = 0, value = 0;
    private String desc = "";
    private String tempMsg, message;
    private boolean showETA = true;
    private boolean showCount = true;
    
    private boolean percentMode = false;

    /** */
    public FProgressBar() {
        super();
        this.reset();
    }

    /**
     * Sets description on bar.
     * 
     * @param s0 &emsp; A description to prepend before statistics.
     */
    public void setDescription(final String s0) {
        this.desc = s0;
    }

    private final Runnable barIncrementor = new Runnable() {
        @Override
        public void run() {
            value = tempVal;
            message = tempMsg;
        }
    };

    /** Increments bar, thread safe. Calculations executed on separate thread. */
    public void setValueThreadSafe(int value) {
        //GuiUtils.checkEDT("FProgressBar$increment", false);
        tempVal = value;

        // String.format leads to StringBuilder anyway. Direct calls will be faster
        StringBuilder sb = new StringBuilder(desc);
        if (showCount) {
            sb.append(" ");
            if (percentMode)
                sb.append(100 * tempVal / maximum).append("%");
            else
                sb.append(tempVal).append(" of ").append(maximum);
        }

        if (showETA) {
            calculateETA(tempVal);
            sb.append(", ETA").append(String.format("%02d:%02d:%02d", etaSecs / 3600, (etaSecs % 3600) / 60, etaSecs % 60 + 1));
        }
        tempMsg = sb.toString();

        // When calculations finished; EDT can be used.
        //SwingUtilities.invokeLater(barIncrementor);
        barIncrementor.run();
    }

    /** Resets the various values required for this class. Must be called from EDT. */
    public void reset() {
        //FThreads.assertExecutedByEdt(true);
        this.startMillis = new Date().getTime();
        this.setShowETA(true);
        this.setShowCount(true);
    }

    /** @param b0 &emsp; Boolean, show the ETA statistic or not */
    public void setShowETA(boolean b0) {
        this.showETA = b0;
    }

    /** @param b0 &emsp; Boolean, show the ETA statistic or not */
    public void setShowCount(boolean b0) {
        this.showCount = b0;
    }

    /** */
    private void calculateETA(int v0) {
        float tempMillis = new Date().getTime();
        float timePerUnit = (tempMillis - startMillis) / v0;
        etaSecs = (int) ((this.maximum - v0) * timePerUnit) / 1000;
    }

    public boolean isPercentMode() {
        return percentMode;
    }

    public void setPercentMode(boolean value) {
        this.percentMode = value;
    }

    @Override
    public void draw(Graphics g) {
        // TODO Auto-generated method stub
        
    }
}
