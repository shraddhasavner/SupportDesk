package com.supportdesk.controller;

/**
 * DTO providing ticket counts grouped by priority.
 */
public class PrioritySummaryResponse {

    private long low;
    private long medium;
    private long high;
    private long critical;

    public PrioritySummaryResponse() {
    }

    public PrioritySummaryResponse(long low, long medium, long high, long critical) {
        this.low = low;
        this.medium = medium;
        this.high = high;
        this.critical = critical;
    }

    public long getLow() {
        return low;
    }

    public void setLow(long low) {
        this.low = low;
    }

    public long getMedium() {
        return medium;
    }

    public void setMedium(long medium) {
        this.medium = medium;
    }

    public long getHigh() {
        return high;
    }

    public void setHigh(long high) {
        this.high = high;
    }

    public long getCritical() {
        return critical;
    }

    public void setCritical(long critical) {
        this.critical = critical;
    }
}
