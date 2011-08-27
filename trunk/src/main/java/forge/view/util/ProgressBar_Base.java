package forge.view.util;

import java.util.Date;
import java.util.Hashtable;
import javax.swing.JProgressBar;
import com.esotericsoftware.minlog.Log;

/**
 * This base class also acts as a "null" progress monitor; it doesn't display
 * anything when updated.
 * 
 * Absolute times are measured in seconds, in congruence with ProgressMonitor.
 * 
 * @see forge.view.util.ProgressBar_Interface
 */
@SuppressWarnings("serial")
public class ProgressBar_Base extends JProgressBar implements ProgressBar_Interface  {
    private int numPhases;
	private int currentPhase;
	private long totalUnitsThisPhase;
	private long unitsCompletedSoFarThisPhase;
	private float minUIUpdateIntervalSec;
	private long lastUIUpdateTime;
	private long phaseOneStartTime;
	private long currentPhaseStartTime;
	private float currentPhaseExponent;
	private long[] phaseDurationHistorySecList;
	private float[] phaseWeights;
	private Hashtable<Integer,String> phaseNames;

	public final int SECONDS_PER_MINUTE = 60;
	public final int SECONDS_PER_HOUR = 60 * SECONDS_PER_MINUTE;
	public final int SECONDS_PER_DAY = 24 * SECONDS_PER_HOUR;
	

	/**
	 * Convenience for 
	 * ProgressBar_Base(numPhases, totalUnitsFirstPhase, 2.0f, null).
	 * 
     * @see #ProgressBar_Base(int,long,float,float[])
     */
    public ProgressBar_Base(int numPhases, long totalUnitsFirstPhase) {
    	this(numPhases, totalUnitsFirstPhase, 2.0f, null);
    }

    /**
	 * Convenience for 
	 * ProgressBar_Base(numPhases, totalUnitsFirstPhase, 
	 * minUIUpdateIntervalSec, null).
	 * 
     * @see #ProgressBar_Base(int,long,float,float[])
     */
    public ProgressBar_Base(int numPhases, long totalUnitsFirstPhase, 
            float minUIUpdateIntervalSec) 
    {
    	this(numPhases, totalUnitsFirstPhase, minUIUpdateIntervalSec, null);
    }

    /**
     * Initializes fields and starts the timers.
     * 
     * @param numPhases  the total number of phases we will monitor
     * 
     * @param totalUnitsFirstPhase  how many units to expect in phase 1
     * 
     * @param minUIUpdateIntervalSec  the approximate interval at which we
     * update the user interface, in seconds
     * 
     * @param phaseWeights  may be null; if not null, this indicates the 
     * relative weight of each phase in terms of time to complete all phases.  
     * Index 0 of this array indicates phase 1's weight, index 1 indicates
     * the weight of phase 2, and so on.  If null, all phases are considered to
     * take an equal amount of time to complete, which is equivalent to setting
     * all phase weights to 1.0f.  For example, if there are two phases, and
     * the phase weights are set to {2.0f, 1.0f}, then the methods that compute
     * the final ETA (Estimated Time of Arrival or completion) will assume that
     * phase 2 takes half as long as phase 1. In other words, the operation
     * will spend 67% of its time in phase 1, and 33% of its time in phase 2.
     */
    public ProgressBar_Base(int numPhases, long totalUnitsFirstPhase, 
	                 float minUIUpdateIntervalSec, float[] phaseWeights) 
    {
        super();
        
    	this.numPhases = numPhases;
    	this.currentPhase = 1;
    	this.unitsCompletedSoFarThisPhase = 0L;
    	this.minUIUpdateIntervalSec = minUIUpdateIntervalSec;
    	this.lastUIUpdateTime = 0L;
    	this.phaseOneStartTime = new Date().getTime()/1000;
    	this.currentPhaseStartTime = this.phaseOneStartTime;
    	this.currentPhaseExponent = 1;
    	this.phaseDurationHistorySecList = new long[numPhases];

    	if (phaseWeights == null) {
    		this.phaseWeights = new float[numPhases];
    		for (int ix = 0; ix < numPhases; ix++) {
    			this.phaseWeights[ix] = 1.0f;
    		}
    	}
    	else {
    		this.phaseWeights = phaseWeights;
    	}
    	
    	if (phaseNames == null) {
    	    this.phaseNames = new Hashtable<Integer, String>();
    	    for(int i=1;i<=numPhases;i++) { 
    	        this.phaseNames.put(i, "Phase "+i); 
    	    }
    	}
    	
    	setTotalUnitsThisPhase(totalUnitsFirstPhase);
    }
    
    /**
     * Does nothing.
     */
    public void dispose() {
    	;
    }
    
    /**
	 * @see forge.view.util.ProgressBar_Interface#getNumPhases()
	 */
    public int getNumPhases() {
        return this.numPhases;
    }

    /**
	 * @see forge.view.util.ProgressBar_Interface#getMinUpdateIntervalSec()
	 */
    public float getMinUpdateIntervalSec() {
        return this.minUIUpdateIntervalSec;
    }

    /**
	 * @see forge.view.util.ProgressBar_Interface#getCurrentPhase()
	 */
    public int getCurrentPhase() {
        return this.currentPhase;
    }

    /**
	 * @see forge.view.util.ProgressBar_Interface#getUnitsCompletedSoFarThisPhase()
	 */
    public long getUnitsCompletedSoFarThisPhase() {
        return this.unitsCompletedSoFarThisPhase;
    }

    /**
	 * @see forge.view.util.ProgressBar_Interface#getTotalUnitsThisPhase()
	 */
    public long getTotalUnitsThisPhase() {
        return this.totalUnitsThisPhase;
    }

    /**
	 * @see forge.view.util.ProgressBar_Interface#getLastUIUpdateTime()
	 */
    public long getLastUIUpdateTime() {
        return this.lastUIUpdateTime;
    }

    /**
	 * @see forge.view.util.ProgressBar_Interface#getPhaseOneStartTime()
	 */
    public long getPhaseOneStartTime() {
        return this.phaseOneStartTime;
    }

    /**
	 * @see forge.view.util.ProgressBar_Interface#getCurrentPhaseStartTime()
	 */
    public long getCurrentPhaseStartTime() {
        return this.currentPhaseStartTime;
    }


    /**
	 * @see forge.view.util.ProgressBar_Interface#setMinUpdateIntervalSec(float)
	 */
    public void setMinUpdateIntervalSec(float value) {
        this.minUIUpdateIntervalSec = value;
    }


    /**
	 * @see forge.view.util.ProgressBar_Interface#setTotalUnitsThisPhase(long)
	 */
    public void setTotalUnitsThisPhase(long value) {
        // DS - why is this called twice?
        if (value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("numUnits must be <= " + Integer.MAX_VALUE);
        }
        else {
            this.totalUnitsThisPhase = value;
            
            // (Temporary solution until I know a better way) 
            this.setProgressRange(0,(int)value);
        }
    }
    
    /**
     * <p>setProgressRange.</p>
     *
     * @param min an int.
     * @param max an int.
     */
    public void setProgressRange(int min, int max) {
        this.setMinimum(min);
        this.setMaximum(max);
    }

    /**
	 * @see forge.view.util.ProgressBar_Interface#getPercentCompleteOfThisPhaseAsString()
	 */
    public String getPercentCompleteOfThisPhaseAsString() {
        
        Float percent = getPercentCompleteOfThisPhaseAsFloat();
        
        if (percent != null) {
            return Integer.toString((int) (float) percent);
        }
        else {
            return "??";
        }
    }

        
    /**
	 * @see forge.view.util.ProgressBar_Interface#getTotalPercentCompleteAsString()
	 */
    public String getTotalPercentCompleteAsString() {
        Float percent = getTotalPercentCompleteAsFloat();

        if (percent == null) {
            return "??";
        }
        else {
            return Integer.toString((int) (float) percent);
        }
    }
    
    
    /**
	 * Convenience for getRelativeETAAsString(false), meaning to compute the
	 * value for the end of the last phase.
	 * 
	 * @see #getRelativeETAAsString(boolean)
	 */
    public String getRelativeETAAsString() {
    	return getRelativeETAAsString(false);
    }

    /**
	 * @see forge.view.util.ProgressBar_Interface#getRelativeETAAsString(boolean)
	 */
    public String getRelativeETAAsString(boolean thisPhaseOnly) {
        
        Integer etaSec = getRelativeETASec(thisPhaseOnly);
        
        if (etaSec == null) {
            return "unknown";
        }
        
        String result = "";
        if (etaSec > SECONDS_PER_DAY) {
            result += Integer.toString(etaSec / SECONDS_PER_DAY);
            result += " da, ";
            etaSec %= SECONDS_PER_DAY;  // Shave off the portion recorded.
        }
        if (result.length() > 0 || etaSec > SECONDS_PER_HOUR) {
            result += Integer.toString(etaSec / SECONDS_PER_HOUR);
            result +=  " hr, ";
            etaSec %= SECONDS_PER_HOUR;  // Shave off the portion recorded.
        }   
        if (result.length() > 0 || etaSec > SECONDS_PER_MINUTE) {
            result += Integer.toString(etaSec / SECONDS_PER_MINUTE);
            result +=  " min, ";
            etaSec %= SECONDS_PER_MINUTE;  // Shave off the portion recorded.
        }
        
        result += Integer.toString(etaSec);
        result += " sec";
        
        return result;
    }    

    /**
	 * Convenience for getAbsoluteETAAsLocalTimeString(false), meaning to 
	 * compute the value for the end of the last phase.
	 * 
	 * @see #getAbsoluteETAAsLocalTimeString(boolean)
	 */
    public String getAbsoluteETAAsLocalTimeString() {
    	return getAbsoluteETAAsLocalTimeString(false);
    }

    /**
	 * @see forge.view.util.ProgressBar_Interface#getAbsoluteETAAsLocalTimeString(boolean)
	 */
    public String getAbsoluteETAAsLocalTimeString(boolean thisPhaseOnly) {
        Long etaTime = getAbsoluteETATime(thisPhaseOnly);
        
        if (etaTime == null) {
            return "unknown";
        }
        
        return (new Date(etaTime*1000).toString());
    }


    /**
	 * @see forge.view.util.ProgressBar_Interface#incrementUnitsCompletedThisPhase(long)
	 */
    public void incrementUnitsCompletedThisPhase(long numUnits) {
        this.unitsCompletedSoFarThisPhase += numUnits;
    }
    
    public void increment() {
        setValue(getValue() + 1);
        if (getValue() % 10 == 0) { repaint(); }
    }
        
    /**
     * Subclasses must call this immediately after updating the UI, to 
     * preserve the integrity of the shouldUpdateUI method.
     */
    protected void justUpdatedUI() {
    	this.lastUIUpdateTime = new Date().getTime()/1000;
    }
        
    /**
	 * @see forge.view.util.ProgressBar_Interface#shouldUpdateUI()
	 */
    public boolean shouldUpdateUI() {

        doctorStartTimes();
        long nowTime = (new Date().getTime()/1000);
        
        if (nowTime - this.lastUIUpdateTime >= this.minUIUpdateIntervalSec || 
        	(this.getUnitsCompletedSoFarThisPhase() == 
             this.getTotalUnitsThisPhase())) 
        {
            return true;
        }
        else {
            return false;
        }
    }

    
    /**
	 * @see forge.view.util.ProgressBar_Interface#markCurrentPhaseAsComplete(long)
	 */
    public void markCurrentPhaseAsComplete(long totalUnitsNextPhase) {

        if ((this.currentPhase > this.numPhases)) {
            String message = "The phase just completed (";
            message += this.currentPhase;
            message += ") is greater than the total number ";
            message += "of anticipated phases (";
    		message += this.numPhases;
    		message += "); the latter is probably incorrect.";
              
            Log.warn(message);
        }
        
        this.currentPhase += 1;
        this.unitsCompletedSoFarThisPhase = 0;
        setTotalUnitsThisPhase(totalUnitsNextPhase);
        this.currentPhaseExponent = 1;

        long nowTime = (new Date().getTime()/1000);
        long durationOfThisPhaseSec = nowTime - this.currentPhaseStartTime;
        if (durationOfThisPhaseSec < 0) {
            durationOfThisPhaseSec = 0;
        }
        
        if (0 <= currentPhase-2 && currentPhase-2 < phaseDurationHistorySecList.length) {
        	this.phaseDurationHistorySecList[currentPhase-2] = durationOfThisPhaseSec;
        }
        this.currentPhaseStartTime = nowTime;
        
        if (this.currentPhase >= this.numPhases) {
        	String message = "Actual individual phase durations: [";
        	for (int ix = 0 ; ix < phaseDurationHistorySecList.length ; ix++) {
        		message += phaseDurationHistorySecList[ix] + ", ";
        	}

        	Log.info(message + ']');
        }
    }


    /**
	 * @see forge.view.util.ProgressBar_Interface#sendMessage(java.lang.String)
	 */
    public void sendMessage(String message) {
        ;
    }


    /**
	 * @see forge.view.util.ProgressBar_Interface#setCurrentPhaseAsExponential(float)
	 */
    public void setCurrentPhaseAsExponential(float value) {
        this.currentPhaseExponent = value;
    }


    /**
	 * @see forge.view.util.ProgressBar_Interface#getCurrentPhaseExponent()
	 */
    public float getCurrentPhaseExponent() {
        return this.currentPhaseExponent;
    }
    
    /**
     * Sets the name of a phase in the process (e.g. "Phase 1" becomes "Loading XML")
     */
    public void setPhaseName(int i, String name) {
           
    }
    
    public String getPhaseName(int i) {
        return phaseNames.get(i);
    }
    
    /**
     * @return number in range [0.0, 100.0] or null.
     */
    protected Float getPercentCompleteOfThisPhaseAsFloat() {
        if (this.totalUnitsThisPhase < 1 || 
            this.unitsCompletedSoFarThisPhase > this.totalUnitsThisPhase) {
            return null;
        }
        else {
            float ratio = ((float) (this.unitsCompletedSoFarThisPhase)) / 
                     ((float) this.totalUnitsThisPhase);
            
            ratio = (float) Math.pow(ratio, this.getCurrentPhaseExponent());
                
            return (ratio * 100.0f);
        }
    }


    /**
     * Returns number in range [0.0, 100.0] or null.
     */
    protected Float getTotalPercentCompleteAsFloat() {
        long totalPoints = 0;
        for (float weight : this.phaseWeights) {
            totalPoints += weight * 100;
        }
        
        Float percentThisPhase = getPercentCompleteOfThisPhaseAsFloat();
        
        if (percentThisPhase == null) {
            // If we can't know the percentage for this phase, use a
            // conservative estimate.
            percentThisPhase = 0.0f;
        }
        
        long pointsSoFar = 0;
        for (int ix = 0; ix < this.currentPhase-1; ix++) {
            // We get full points for (all the phases completed prior to this one.
            pointsSoFar += phaseWeights[ix] * 100;
        }
            
        pointsSoFar += percentThisPhase * this.phaseWeights[this.currentPhase-1];
        
        if (totalPoints <= 0.0 || pointsSoFar > totalPoints) {
            return null;
        }
        else {
            return (100.0f * pointsSoFar) / totalPoints;
        }
    }


    /**
     * Convenience for getRelativeETASec(false), meaning to compute the value
     * for the end of the last phase. 
     * 
     * @see #getRelativeETASec(boolean)
     */
    protected Integer getRelativeETASec() {
    	return getRelativeETASec(false);
    }

    /**
     * @return estimated seconds until completion for either thisPhaseOnly
     * or for the entire operation.  May return null if unknown.
     */
    protected Integer getRelativeETASec(boolean thisPhaseOnly) {

        Long absoluteETATime = getAbsoluteETATime(thisPhaseOnly);
        if (absoluteETATime == null) {
            return null;
        }
        return (int) (absoluteETATime - (new Date().getTime()/1000));
    }
    

    /**
     * Convenience for getAbsoluteETATime(false), meaning to compute the value
     * for the end of all phases. 
     * 
     * @see #getAbsoluteETATime(boolean)
     */
    protected Long getAbsoluteETATime() {
    	return getAbsoluteETATime(false);
    }
    
    /**
     * @return the estimated time (in absolute seconds) at which thisPhaseOnly
     * or the entire operation will be completed.  May return null if (unknown.
     */
    protected Long getAbsoluteETATime(boolean thisPhaseOnly) {
        doctorStartTimes();
        
        // If we're in the last phase, the overall ETA is the same as the ETA
        // for (this particular phase.
        if (this.getCurrentPhase() >= this.getNumPhases()) {
            thisPhaseOnly = true;
        }
        
        Float percentDone = null;
        long startTime = 0L;
        
        if (thisPhaseOnly) {
            percentDone = getPercentCompleteOfThisPhaseAsFloat();
            startTime = this.currentPhaseStartTime;
        }
        else {
            percentDone = getTotalPercentCompleteAsFloat();
            startTime = this.phaseOneStartTime;
        }

        if (percentDone == null || percentDone <= 0.001) {
            return null;
        }
        
        // Elapsed time is to percent done as total time is to total done =>
        // elapsed/percentDone == totalTime/100.0 =>
        long totalTime = (long) (100.0f * ((new Date().getTime()/1000) - startTime) / percentDone);
        
        return totalTime + startTime;
    }


    /**
     * Repair the start times in case the system clock has been moved 
     * backwards.
     */
    protected void doctorStartTimes() {
        
        long nowTime = (new Date().getTime()/1000);
        
        if (this.lastUIUpdateTime > nowTime) {
            this.lastUIUpdateTime = 0;
        }
        
        if (this.phaseOneStartTime > nowTime) {
            this.phaseOneStartTime = nowTime;
        }
        
        if (this.currentPhaseStartTime > nowTime) {
            this.currentPhaseStartTime = nowTime;
        }
    }

}
