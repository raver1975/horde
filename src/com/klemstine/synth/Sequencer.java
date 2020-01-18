package com.klemstine.synth;

import com.klemstine.SequencerData;

public abstract class Sequencer {
    public double bpm = 120.0D;
    public double vol = 0f;
    private BasslinePattern bassline;
    private int[][] rhythm;
    public boolean shuffle;
    public int samplesPerSequencerUpdate;
    public int tick = 0;
    public int step = 0;
    public boolean sixteenth_note = true;
    public int patternLength = 16;
    public int pitch_offset;


    public BasslinePattern getBassline() {
        return bassline;
    }

    public void setBassline(BasslinePattern bassline) {
        this.bassline = bassline;
    }

    public int[][] getRhythm() {
        return rhythm;
    }

    public void setRhythm(int[][] rhythm) {
        this.rhythm = rhythm;
    }

    public abstract void tick();

    public abstract void reset();

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

    public void setSequence(SequencerData sd) {
        BasslinePattern bp=new BasslinePattern(sd.note.length);
        bp.accent=sd.accent.clone();
        bp.note=sd.note.clone();
        bp.pause=sd.pause.clone();
        bp.slide=sd.slide.clone();
        this.setBassline(bp);
        this.setRhythm(sd.rhythm.clone());
    }
}

