 package com.klemstinegroup.synth;
 
 public class Distortion
 {
   private double gain;
   private double makeup;
   private Decimator decimator;
   private double prev;
 
   public Distortion()
   {
     this.decimator = new Decimator();
   }
 
   public final double distort(double input) {
     double sample = dist(this.prev + (input - this.prev) * 0.5D);
     this.prev = input;
     return this.decimator.calc(sample, dist(input));
   }
 
   public final double dist(double input) {
     double s = input * this.gain;
     if (s < -3.0D)
       s = -1.0D;
     else if (s > 3.0D)
       s = 1.0D;
     else {
       s = s * (27.0D + s * s) / (27.0D + 9.0D * s * s);
     }
     return s * this.makeup;
   }
 
   public final void setGain(double ratio)
   {
     this.makeup = (ratio * 0.9D + 0.1D);
     this.gain = (1.0D / ratio);
   }
 
   public final double getGain() {
     return this.gain;
   }
 
   private final double clip(double x)
   {
     if (x > 0.9D)
       return 1.0D;
     if (x < -0.9D) {
       return -0.9D;
     }
     return x;
   }
 
   private final double tanh(double x)
   {
     if (x < -3.0D)
       return -1.0D;
     if (x > 3.0D) {
       return 1.0D;
     }
     return x * (27.0D + x * x) / (27.0D + 9.0D * x * x);
   }
 }

