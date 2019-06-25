 package com.myronmarston.synth;
 
 public class CubicOscillator extends Oscillator
 {
   private boolean cyclic = true;
   private double[] wave;
   private double wavePhase;
   private double waveLength;
   private int x;
   private double mu;
   private double mu2;
   private double y0;
   private double y1;
   private double y2;
   private double y3;
   private double a0;
   private double a1;
   private double a2;
   private double a3;
 
   public CubicOscillator()
   {
     this.wavePhase = 0.0D;
     this.phase = 1.0D;
   }
 
   public void setSample(double[] wave) {
     this.wave = wave;
     this.wavePhase = wave.length;
     setFrequency(Output.SAMPLE_RATE/ wave.length);
     this.waveLength = (wave.length - 3);
   }
 
   public void trigger() {
     this.phase = 0.0D;
     this.wavePhase = 0.0D;
   }
 
   public final double tick()
   {
     this.phase += this.phaseDelta;
     if (this.phase >= 1.0D) {
       this.phase -= 1.0D;
     }
 
     this.wavePhase = (this.phase * this.waveLength + 1.0D);
 
     this.x = (int)this.wavePhase;
     this.mu = (this.wavePhase - this.x);
     this.mu2 = (this.mu * this.mu);
 
     this.y0 = this.wave[(this.x - 1)];
     this.y1 = this.wave[this.x];
     this.y2 = this.wave[(this.x + 1)];
     this.y3 = this.wave[(this.x + 2)];
 
     this.a0 = (this.y3 - this.y2 - this.y0 + this.y1);
     this.a1 = (this.y0 - this.y1 - this.a0);
     this.a2 = (this.y2 - this.y0);
     this.a3 = this.y1;
 
     return this.a0 * this.mu * this.mu2 + this.a1 * this.mu2 + this.a2 * this.mu + this.a3;
   }
 
   public void setFrequency(double frequency)
   {
     this.frequency = frequency;
     this.phaseDelta = (frequency * this.oneHzPhaseDelta);
   }
 }

