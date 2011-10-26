package net.slightlymagic.braids.util.progress_monitor;

import java.util.Date;

import com.esotericsoftware.minlog.Log;

/**
 * This base class also acts as a "null" progress monitor; it doesn't display
 * anything when updated.
 * 
 * Absolute times are measured in seconds, in congruence with ProgressMonitor.
 * 
 * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor
 */
public class BaseProgressMonitor implements BraidsProgressMonitor {
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

    /** The SECOND s_ pe r_ minute. */
    public final int SECONDS_PER_MINUTE = 60;

    /** The SECOND s_ pe r_ hour. */
    public final int SECONDS_PER_HOUR = 60 * SECONDS_PER_MINUTE;

    /** The SECOND s_ pe r_ day. */
    public final int SECONDS_PER_DAY = 24 * SECONDS_PER_HOUR;

    /**
     * Convenience for BaseProgressMonitor(1, 1, 2.0f, null).
     * 
     * @see #BaseProgressMonitor(int,long,float,float[])
     */
    public BaseProgressMonitor() {
        this(1, 1L, 2.0f, null);
    }

    /**
     * Convenience for BaseProgressMonitor(numPhases, 1, 2.0f, null).
     * 
     * @param numPhases
     *            the num phases
     * @see #BaseProgressMonitor(int,long,float,float[])
     */
    public BaseProgressMonitor(final int numPhases) {
        this(numPhases, 1L, 2.0f, null);
    }

    /**
     * Convenience for BaseProgressMonitor(numPhases, totalUnitsFirstPhase,
     * 2.0f, null).
     * 
     * @param numPhases
     *            the num phases
     * @param totalUnitsFirstPhase
     *            the total units first phase
     * @see #BaseProgressMonitor(int,long,float,float[])
     */
    public BaseProgressMonitor(final int numPhases, final long totalUnitsFirstPhase) {
        this(numPhases, totalUnitsFirstPhase, 2.0f, null);
    }

    /**
     * Convenience for BaseProgressMonitor(numPhases, totalUnitsFirstPhase,
     * minUIUpdateIntervalSec, null).
     * 
     * @param numPhases
     *            the num phases
     * @param totalUnitsFirstPhase
     *            the total units first phase
     * @param minUIUpdateIntervalSec
     *            the min ui update interval sec
     * @see #BaseProgressMonitor(int,long,float,float[])
     */
    public BaseProgressMonitor(final int numPhases,
            final long totalUnitsFirstPhase, final float minUIUpdateIntervalSec) {
        this(numPhases, totalUnitsFirstPhase, minUIUpdateIntervalSec, null);
    }

    /**
     * Initializes fields and starts the timers.
     * 
     * @param numPhases
     *            the total number of phases we will monitor
     * 
     * @param totalUnitsFirstPhase
     *            how many units to expect in phase 1
     * 
     * @param minUIUpdateIntervalSec
     *            the approximate interval at which we update the user
     *            interface, in seconds
     * 
     * @param phaseWeights
     *            may be null; if not null, this indicates the relative weight
     *            of each phase in terms of time to complete all phases. Index 0
     *            of this array indicates phase 1's weight, index 1 indicates
     *            the weight of phase 2, and so on. If null, all phases are
     *            considered to take an equal amount of time to complete, which
     *            is equivalent to setting all phase weights to 1.0f. For
     *            example, if there are two phases, and the phase weights are
     *            set to {2.0f, 1.0f}, then the methods that compute the final
     *            ETA (Estimated Time of Arrival or completion) will assume that
     *            phase 2 takes half as long as phase 1. In other words, the
     *            operation will spend 67% of its time in phase 1, and 33% of
     *            its time in phase 2.
     */
    public BaseProgressMonitor(final int numPhases, final long totalUnitsFirstPhase,
            final float minUIUpdateIntervalSec, final float[] phaseWeights) {
        this.numPhases = numPhases;
        this.currentPhase = 1;
        this.unitsCompletedSoFarThisPhase = 0L;
        this.minUIUpdateIntervalSec = minUIUpdateIntervalSec;
        this.lastUIUpdateTime = 0L;
        this.phaseOneStartTime = new Date().getTime() / 1000;
        this.currentPhaseStartTime = this.phaseOneStartTime;
        this.currentPhaseExponent = 1;
        this.phaseDurationHistorySecList = new long[numPhases];

        if (phaseWeights == null) {
            this.phaseWeights = new float[numPhases];
            for (int ix = 0; ix < numPhases; ix++) {
                this.phaseWeights[ix] = 1.0f;
            }
        } else {
            this.phaseWeights = phaseWeights;
        }

        setTotalUnitsThisPhase(totalUnitsFirstPhase);

    }

    /**
     * Does nothing.
     */
    public void dispose() {
    }

    /**
     * Gets the num phases.
     * 
     * @return the num phases
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#getNumPhases()
     */
    public final int getNumPhases() {
        return this.numPhases;
    }

    /**
     * Gets the min update interval sec.
     * 
     * @return the min update interval sec
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#getMinUpdateIntervalSec()
     */
    public final float getMinUpdateIntervalSec() {
        return this.minUIUpdateIntervalSec;
    }

    /**
     * Gets the current phase.
     * 
     * @return the current phase
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#getCurrentPhase()
     */
    public final int getCurrentPhase() {
        return this.currentPhase;
    }

    /**
     * Gets the units completed so far this phase.
     * 
     * @return the units completed so far this phase
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#getUnitsCompletedSoFarThisPhase()
     */
    public final long getUnitsCompletedSoFarThisPhase() {
        return this.unitsCompletedSoFarThisPhase;
    }

    /**
     * Gets the total units this phase.
     * 
     * @return the total units this phase
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#getTotalUnitsThisPhase()
     */
    public final long getTotalUnitsThisPhase() {
        return this.totalUnitsThisPhase;
    }

    /**
     * Gets the last ui update time.
     * 
     * @return the last ui update time
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#getLastUIUpdateTime()
     */
    public final long getLastUIUpdateTime() {
        return this.lastUIUpdateTime;
    }

    /**
     * Gets the phase one start time.
     * 
     * @return the phase one start time
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#getPhaseOneStartTime()
     */
    public final long getPhaseOneStartTime() {
        return this.phaseOneStartTime;
    }

    /**
     * Gets the current phase start time.
     * 
     * @return the current phase start time
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#getCurrentPhaseStartTime()
     */
    public final long getCurrentPhaseStartTime() {
        return this.currentPhaseStartTime;
    }

    /**
     * Sets the min update interval sec.
     * 
     * @param value
     *            the new min update interval sec
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#setMinUpdateIntervalSec(float)
     */
    public final void setMinUpdateIntervalSec(final float value) {
        this.minUIUpdateIntervalSec = value;
    }

    /**
     * Sets the total units this phase.
     * 
     * @param value
     *            the new total units this phase
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#setTotalUnitsThisPhase(long)
     */
    public void setTotalUnitsThisPhase(final long value) {
        this.totalUnitsThisPhase = value;
    }

    /**
     * Gets the percent complete of this phase as string.
     * 
     * @return the percent complete of this phase as string
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#getPercentCompleteOfThisPhaseAsString()
     */
    public final String getPercentCompleteOfThisPhaseAsString() {

        Float percent = getPercentCompleteOfThisPhaseAsFloat();

        if (percent != null) {
            return Integer.toString((int) (float) percent);
        } else {
            return "??";
        }
    }

    /**
     * Gets the total percent complete as string.
     * 
     * @return the total percent complete as string
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#getTotalPercentCompleteAsString()
     */
    public final String getTotalPercentCompleteAsString() {
        Float percent = getTotalPercentCompleteAsFloat();

        if (percent == null) {
            return "??";
        } else {
            return Integer.toString((int) (float) percent);
        }
    }

    /**
     * Convenience for getRelativeETAAsString(false), meaning to compute the
     * value for the end of the last phase.
     * 
     * @return the relative eta as string
     * @see #getRelativeETAAsString(boolean)
     */
    public final String getRelativeETAAsString() {
        return getRelativeETAAsString(false);
    }

    /**
     * Gets the relative eta as string.
     * 
     * @param thisPhaseOnly
     *            the this phase only
     * @return the relative eta as string
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#getRelativeETAAsString(boolean)
     */
    public final String getRelativeETAAsString(final boolean thisPhaseOnly) {

        Integer etaSec = getRelativeETASec(thisPhaseOnly);

        if (etaSec == null) {
            return "unknown";
        }

        String result = "";
        if (etaSec > SECONDS_PER_DAY) {
            result += Integer.toString(etaSec / SECONDS_PER_DAY);
            result += " da, ";
            etaSec %= SECONDS_PER_DAY; // Shave off the portion recorded.
        }
        if (result.length() > 0 || etaSec > SECONDS_PER_HOUR) {
            result += Integer.toString(etaSec / SECONDS_PER_HOUR);
            result += " hr, ";
            etaSec %= SECONDS_PER_HOUR; // Shave off the portion recorded.
        }
        if (result.length() > 0 || etaSec > SECONDS_PER_MINUTE) {
            result += Integer.toString(etaSec / SECONDS_PER_MINUTE);
            result += " min, ";
            etaSec %= SECONDS_PER_MINUTE; // Shave off the portion recorded.
        }

        result += Integer.toString(etaSec);
        result += " sec";

        return result;
    }

    /**
     * Convenience for getAbsoluteETAAsLocalTimeString(false), meaning to
     * compute the value for the end of the last phase.
     * 
     * @return the absolute eta as local time string
     * @see #getAbsoluteETAAsLocalTimeString(boolean)
     */
    public final String getAbsoluteETAAsLocalTimeString() {
        return getAbsoluteETAAsLocalTimeString(false);
    }

    /**
     * Gets the absolute eta as local time string.
     * 
     * @param thisPhaseOnly
     *            the this phase only
     * @return the absolute eta as local time string
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#getAbsoluteETAAsLocalTimeString(boolean)
     */
    public final String getAbsoluteETAAsLocalTimeString(final boolean thisPhaseOnly) {
        Long etaTime = getAbsoluteETATime(thisPhaseOnly);

        if (etaTime == null) {
            return "unknown";
        }

        return (new Date(etaTime * 1000).toString());
    }

    /**
     * Increment units completed this phase.
     * 
     * @param numUnits
     *            the num units
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#incrementUnitsCompletedThisPhase(long)
     */
    public void incrementUnitsCompletedThisPhase(final long numUnits) {
        this.unitsCompletedSoFarThisPhase += numUnits;
    }

    /**
     * Subclasses must call this immediately after updating the UI, to preserve
     * the integrity of the shouldUpdateUI method.
     */
    public final void justUpdatedUI() {
        this.lastUIUpdateTime = new Date().getTime() / 1000;
    }

    /**
     * Should update ui.
     * 
     * @return true, if successful
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#shouldUpdateUI()
     */
    public final boolean shouldUpdateUI() {

        doctorStartTimes();
        long nowTime = (new Date().getTime() / 1000);

        if (nowTime - this.lastUIUpdateTime >= this.minUIUpdateIntervalSec
                || (this.getUnitsCompletedSoFarThisPhase() == this.getTotalUnitsThisPhase())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Mark current phase as complete.
     * 
     * @param totalUnitsNextPhase
     *            the total units next phase
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#markCurrentPhaseAsComplete(long)
     */
    public final void markCurrentPhaseAsComplete(final long totalUnitsNextPhase) {

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

        long nowTime = (new Date().getTime() / 1000);
        long durationOfThisPhaseSec = nowTime - this.currentPhaseStartTime;
        if (durationOfThisPhaseSec < 0) {
            durationOfThisPhaseSec = 0;
        }

        if (0 <= currentPhase - 2 && currentPhase - 2 < phaseDurationHistorySecList.length) {
            this.phaseDurationHistorySecList[currentPhase - 2] = durationOfThisPhaseSec;
        }
        this.currentPhaseStartTime = nowTime;

        if (this.currentPhase >= this.numPhases) {
            String message = "Actual individual phase durations: [";
            for (int ix = 0; ix < phaseDurationHistorySecList.length; ix++) {
                message += phaseDurationHistorySecList[ix] + ", ";
            }

            Log.info(message + ']');
        }
    }

    /**
     * Send message.
     * 
     * @param message
     *            the message
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#sendMessage(java.lang.String)
     */
    public final void sendMessage(final String message) {
    }

    /**
     * Sets the current phase as exponential.
     * 
     * @param value
     *            the new current phase as exponential
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#setCurrentPhaseAsExponential(float)
     */
    public final void setCurrentPhaseAsExponential(final float value) {
        this.currentPhaseExponent = value;
    }

    /**
     * Gets the current phase exponent.
     * 
     * @return the current phase exponent
     * @see net.slightlymagic.braids.util.progress_monitor.BraidsProgressMonitor#getCurrentPhaseExponent()
     */
    public final float getCurrentPhaseExponent() {
        return this.currentPhaseExponent;
    }

    /**
     * Gets the percent complete of this phase as float.
     * 
     * @return number in range [0.0, 100.0] or null.
     */
    protected final Float getPercentCompleteOfThisPhaseAsFloat() {
        if (this.totalUnitsThisPhase < 1 || this.unitsCompletedSoFarThisPhase > this.totalUnitsThisPhase) {
            return null;
        } else {
            float ratio = ((float) (this.unitsCompletedSoFarThisPhase)) / ((float) this.totalUnitsThisPhase);

            ratio = (float) Math.pow(ratio, this.getCurrentPhaseExponent());

            return (ratio * 100.0f);
        }
    }

    /**
     * Returns number in range [0.0, 100.0] or null.
     * 
     * @return the total percent complete as float
     */
    protected final Float getTotalPercentCompleteAsFloat() {
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
        for (int ix = 0; ix < this.currentPhase - 1; ix++) {
            // We get full points for (all the phases completed prior to this
            // one.
            pointsSoFar += phaseWeights[ix] * 100;
        }

        pointsSoFar += percentThisPhase * this.phaseWeights[this.currentPhase - 1];

        if (totalPoints <= 0.0 || pointsSoFar > totalPoints) {
            return null;
        } else {
            return (100.0f * pointsSoFar) / totalPoints;
        }
    }

    /**
     * Convenience for getRelativeETASec(false), meaning to compute the value
     * for the end of the last phase.
     * 
     * @return the relative eta sec
     * @see #getRelativeETASec(boolean)
     */
    protected final Integer getRelativeETASec() {
        return getRelativeETASec(false);
    }

    /**
     * Gets the relative eta sec.
     * 
     * @param thisPhaseOnly
     *            the this phase only
     * @return estimated seconds until completion for either thisPhaseOnly or
     *         for the entire operation. May return null if unknown.
     */
    protected final Integer getRelativeETASec(final boolean thisPhaseOnly) {

        Long absoluteETATime = getAbsoluteETATime(thisPhaseOnly);
        if (absoluteETATime == null) {
            return null;
        }
        return (int) (absoluteETATime - (new Date().getTime() / 1000));
    }

    /**
     * Convenience for getAbsoluteETATime(false), meaning to compute the value
     * for the end of all phases.
     * 
     * @return the absolute eta time
     * @see #getAbsoluteETATime(boolean)
     */
    protected final Long getAbsoluteETATime() {
        return getAbsoluteETATime(false);
    }

    /**
     * Gets the absolute eta time.
     * 
     * @param thisPhaseOnly
     *            the this phase only
     * @return the estimated time (in absolute seconds) at which thisPhaseOnly
     *         or the entire operation will be completed. May return null if
     *         (unknown.
     */
    protected final Long getAbsoluteETATime(boolean thisPhaseOnly) {
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
        } else {
            percentDone = getTotalPercentCompleteAsFloat();
            startTime = this.phaseOneStartTime;
        }

        if (percentDone == null || percentDone <= 0.001) {
            return null;
        }

        // Elapsed time is to percent done as total time is to total done =>
        // elapsed/percentDone == totalTime/100.0 =>
        long totalTime = (long) (100.0f * ((new Date().getTime() / 1000) - startTime) / percentDone);

        return totalTime + startTime;
    }

    /**
     * Repair the start times in case the system clock has been moved backwards.
     */
    protected final void doctorStartTimes() {

        long nowTime = (new Date().getTime() / 1000);

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
