package com.myronmarston.synth;

public abstract class Sequencer {
    protected double bpm = 140.0D;
    protected double vol = 0f;

    public abstract void tick();

    public void setVolume(double vol) {
        this.vol = vol;
    }

    public double getVolume() {
        return vol;
    }

    public double getBpm() {
        return bpm;
    }

    public void setBpm(double bpm) {
        this.bpm = bpm;
    }

    public abstract void randomizeSequence();
    public abstract void randomizeRhythm();
}

