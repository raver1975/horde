 package com.klemstinegroup.synth;
 
 public abstract class Oscillator
 {
   protected double frequency = 55.0D;
   protected double phase;
   protected double phaseDelta;
   protected double SAMPLE_RATE = Output.SAMPLE_RATE;
   protected double oneHzPhaseDelta = 1.0D / this.SAMPLE_RATE;
   protected static final double PI2 = 6.283185307179586D;
 
   private void setFrequency(double frequency)
   {
     this.frequency = frequency;
     this.phaseDelta = (frequency * this.oneHzPhaseDelta);
   }
 
   public double getFrequency() {
     return this.frequency;
   }
 
   public void setSamplingFrequency(double value) {
     this.SAMPLE_RATE = value;
     this.oneHzPhaseDelta = (1.0D / this.SAMPLE_RATE);
   }
 }

