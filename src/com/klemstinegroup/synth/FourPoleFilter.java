 package com.klemstinegroup.synth;
 
 public final class FourPoleFilter
 {
   private double samplingFrequency = Output.SAMPLE_RATE;
   private double out_1;
   private double x;
   private double a0;
   private double b1;
   private double out_2;
   private double out_3;
   private double out_4;
 
   public final double lp(double in)
   {
     this.out_1 = (this.a0 * in - this.b1 * this.out_1);
     this.out_2 = (this.a0 * this.out_1 - this.b1 * this.out_2);
     this.out_3 = (this.a0 * this.out_2 - this.b1 * this.out_3);
     this.out_4 = (this.a0 * this.out_3 - this.b1 * this.out_4);
     return this.out_4;
   }
 
   public final void setSamplingFrequency(double freq) {
     this.samplingFrequency = freq;
     setCutoff(freq);
   }
 
   public final void setCutoff(double cutoff) {
     this.x = Math.exp(-6.283185307179586D * cutoff / this.samplingFrequency);
     this.a0 = (1.0D - this.x);
     this.b1 = (-this.x);
   }
 }

