 package com.klemstine.synth;
 
 public class RhythmSampler
 {
   private boolean cyclic = true;
   private double[] wave;
   private double phase;
   private double phaseDelta;
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
 
   public RhythmSampler()
   {
     this.wavePhase = 0.0D;
     this.phase = 1.0D;
   }
 
   public void setPhase(double value) {
     this.wavePhase = value;
   }
 
   public void setSample(double[] wave) {
     this.x = 1;
     this.wave = wave;
     setRate(1.0D);
     this.waveLength = (wave.length - 3);
     this.wavePhase = 0.0D;
     this.phase = this.waveLength;
   }
 
   public void trigger() {
     this.x = 1;
     this.phase = 1.0D;
     this.wavePhase = 0.0D;
   }
 
   public double tick()
   {
     if (this.phase >= this.waveLength - 2.0D) {
       return 0.0D;
     }
 
     this.phase += this.phaseDelta;
 
     this.wavePhase = this.phase;
 
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
 
   public void setRate(double rate)
   {
     this.phaseDelta = rate;
   }
 }

