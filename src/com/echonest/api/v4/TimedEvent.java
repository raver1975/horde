package com.echonest.api.v4;

import com.echonest.api.v4.util.MQuery;

import java.io.Serializable;
import java.util.Map;

public class TimedEvent implements Serializable {

    public double start;
    public double duration;
    private double confidence;

    public TimedEvent(Map map) {
        MQuery mq = new MQuery(map);
        start = mq.getDouble("start");
        duration = mq.getDouble("duration");
        confidence = mq.getDouble("confidence");
    }

    public TimedEvent(double start, double duration, double confidence) {
        this.start = start;
        this.duration = duration;
        this.confidence = confidence;
    }

    /**
     * @return the start
     */
    public double getStart() {
        return start;
    }

    /**
     * @return the duration
     */
    public double getDuration() {
        return duration;
    }

    /**
     * @return the confidence
     */
    public double getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return String.format("%.2f %.2f %.2f", start, duration, confidence);
    }
}
