 package com.myronmarston.synth;
 
 public class OnePole
 {
   private double samplingFrequency = Output.SAMPLE_RATE;
   private double out;
   private double x;
   private double a0;
   private double b1;
   private double cutoff;
 
   public final double lp(double in)
   {
     this.out = (this.a0 * in - this.b1 * this.out);
     return this.out;
   }
 
   public final double hp(double in) {
     this.out = (this.a0 * in - this.b1 * this.out);
     return in - this.out;
   }
 
   public final double bp(double in) {
     this.out = (this.a0 * in - this.b1 * this.out);
     double lp = this.out;
     double hp = in - this.out;
     return hp - lp;
   }
 
   public final void setSamplingFrequency(double freq) {
     this.samplingFrequency = freq;
     setCutoff(this.cutoff);
   }
 
   public final void setCutoff(double cutoff) {
     this.cutoff = cutoff;
     this.x = Math.exp(-6.283185307179586D * cutoff / this.samplingFrequency);
     this.a0 = (1.0D - this.x);
     this.b1 = (-this.x);
   }
 }

