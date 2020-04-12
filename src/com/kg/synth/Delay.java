package com.kg.synth;

public class Delay {
    private double[] buffer_l;
    private double[] buffer_r;
    private int length = (int) Output.SAMPLE_RATE;
    private int wpos = 16049;
    private double rpos;
    private double rdelta;
    private double feedback = 0.75D;
    private double input = 0.5D;
    private double time;
    private double goal;
    private HPFilter hpL;
    private HPFilter hpR;
    private double spreadL = 1.0D;
    private double spreadR = 0.0D;
    double[] sample = new double[2];
    private int x;
    private double mu;
    private double mu2;
    private double y0L;
    private double y1L;
    private double y2L;
    private double y3L;
    private double a0L;
    private double a1L;
    private double a2L;
    private double a3L;
    private double y0R;
    private double y1R;
    private double y2R;
    private double y3R;
    private double a0R;
    private double a1R;
    private double a2R;
    private double a3R;
    boolean adjusting = false;
    double countDown;

    public Delay() {
        this.hpL = new HPFilter();
        this.hpL.setSamplingFrequency(Output.SAMPLE_RATE);
        this.hpL.setCutoff(200.0D);
        this.hpR = new HPFilter();
        this.hpR.setSamplingFrequency(Output.SAMPLE_RATE);
        this.hpR.setCutoff(200.0D);
        this.buffer_l = new double[this.length];
        this.buffer_r = new double[this.length];
        this.wpos = 11000;
        this.rpos = 1.0D;
        this.time = (this.wpos - this.rpos);
        this.rdelta = 1.0D;
    }

    public void input(double sample) {
        this.buffer_l[this.wpos] += sample * this.input * this.spreadL;
        this.buffer_r[this.wpos] += sample * this.input * this.spreadR;
    }

    public void input(double[] sample) {
        this.buffer_l[this.wpos] += sample[0] * this.input * this.spreadL;
        this.buffer_r[this.wpos] += sample[1] * this.input * this.spreadR;
    }

    public void setTime(double newTime) {
        while (newTime > this.length) {
            newTime *= 0.5D;
        }
        this.countDown = (this.rpos > this.wpos ? this.wpos + Output.SAMPLE_RATE - this.rpos : this.wpos - this.rpos);
        this.rdelta = (this.countDown / newTime);
        this.countDown /= this.rdelta;
        this.time = newTime;
        this.adjusting = true;
    }

    public double getTime() {
        return this.time;
    }

    public void setFeedback(double value) {
        this.feedback = value;
    }

    public double getFeedback() {
        return feedback;
    }

    public double[] output() {
        this.wpos += 1;
        this.rpos += this.rdelta;

        if (this.adjusting) {
            this.countDown -= 1.0D;
            if (this.countDown <= 0.0D) {
                this.rdelta = 1.0D;
                this.adjusting = false;
            }
        }

        if (this.wpos >= this.length) {
            this.wpos = 0;
        }

        if (this.rpos >= this.length) {
            this.rpos -= this.length;
        }

        if (this.buffer_l.length>this.rpos)this.sample[0] = this.buffer_l[(int) this.rpos];
        if (this.buffer_r.length>this.rpos)this.sample[1] = this.buffer_r[(int) this.rpos];

        this.buffer_l[this.wpos] = this.hpL.filter(this.sample[1] * this.feedback * this.spreadL + this.sample[0] * this.feedback * this.spreadR);
        this.buffer_r[this.wpos] = this.hpR.filter(this.sample[1] * this.feedback * this.spreadR + this.sample[0] * this.feedback * this.spreadL);

        return this.sample;
    }

    public void controlChange(int controller, int value) {
        double newValue = 0;
        switch (controller) {
            case 40: //delay time
                if (value>0 && value*245f<44100) {
                    newValue = value *245f;
                    this.setTime(newValue);
                }
                break;
            case 41: //delay feedback
                newValue = value / 127.0D;
                if (newValue > 0d && newValue <= 1.0d) {
                    this.setFeedback(newValue);
                }
                break;
        }
    }
}


