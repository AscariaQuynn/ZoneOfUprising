/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.utils;

/**
 *
 * @author Ascaria Quynn
 */
public class DeltaTimer {

    final public static float SECONDS = 1f;
    final public static float MINUTES = 60f;
    final public static float HOURS = 3600f;

    private float interval = 1f;
    private float delta = 0f;
    private int lastSecond = -1;
    private boolean secondChanged = false;

    /**
     * Constructs new delta timer.
     * @param interval
     * @throws IllegalArgumentException when interval is zero or negative
     */
    public DeltaTimer(float interval) {
        this(interval, SECONDS);
    }
    /**
     * Constructs new delta timer.
     * @param interval
     * @param multiplier
     * @throws IllegalArgumentException when interval is zero or negative
     */
    public DeltaTimer(float interval, float multiplier) {
        if(interval <= 0f || multiplier <= 0f) {
            throw new IllegalArgumentException("Interval and multiplier must be greater than zero.");
        }
        this.interval = interval * multiplier;
    }

    /**
     * Sets new interval.
     * @param interval
     * @throws IllegalArgumentException when interval is zero or negative
     */
    public void setInterval(float interval) {
        if(interval <= 0f) {
            throw new IllegalArgumentException("Interval must be greater than zero.");
        }
        this.interval = interval;
        lastSecond = -1;
    }

    public boolean isIntervalReached() {
        return delta >= interval;
    }

    public boolean isSecondChanged() {
        return secondChanged;
    }

    public float getRemainingTime() {
        return Math.max(interval - delta, 0f);
    }

    public void update(float tpf) {
        if(lastSecond != (int)delta) {
            lastSecond = (int)delta;
            secondChanged = true;
        } else if(secondChanged != false) {
            secondChanged = false;
        }
        if(delta < interval) {
            delta += tpf;
        }
    }

    public boolean updateIsReached(float tpf) {
        update(tpf);
        if(isIntervalReached()) {
            reset();
            return true;
        } else {
            return false;
        }
    }

    public void reset() {
        delta = 0f;
    }
}
