 package com.klemstinegroup.synth;
 
 public class HPFilter
 {
   private double samplingFrequency = Output.SAMPLE_RATE;
   private double out;
   private double tmp;
   private double x;
   private double p;
 
   public final double filter(double in)
   {
     this.tmp = ((1.0D - this.p) * in + this.p * this.tmp);
     return in - this.tmp;
   }
 
   public final void setSamplingFrequency(double freq)
   {
     this.samplingFrequency = freq;
     setCutoff(22050.0D);
   }
 
   public final void setCutoff(double cutoff) {
     this.x = (6.283185307179586D * cutoff / this.samplingFrequency);
     this.p = (2.0D - Math.cos(this.x) - Math.sqrt(Math.pow(2.0D - Math.cos(this.x), 2.0D) - 1.0D));
   }
 }

