 package com.klemstine.synth;
 
 public class BasslineFilter2
 {
   private double sfrq;
   private double y1;
   private double y2;
   private double y3;
   private double y4;
   private double s1;
   private double s2;
   private double s3;
   private double s4;
   private double b0;
   private double k;
   private static final double sqrt2 = Math.sqrt(2.0D);
   private static final double r6 = 0.1666666666666667D;
   private static final double pi = 3.141592653589793D;
   private HPFilter hpf1;
   private HPFilter hpf2;
   double b02;
 
   public BasslineFilter2(double cutoff, double resonance)
   {
     setCoefficients(cutoff, resonance);
     this.hpf1 = new HPFilter();
     this.hpf2 = new HPFilter();
     this.hpf1.setCutoff(150.0D);
     this.hpf2.setCutoff(58.0D);
   }
 
   public void setCoefficients(double cutoff, double resonance)
   {
     double w_c = 6.283185307179586D * cutoff / this.sfrq;
     double s = Math.sin(w_c);
     double c = Math.cos(w_c);
     double t = Math.tan((w_c - 3.141592653589793D) / 4.0D);
     double a1 = -t / (c * t - s);
     this.b0 = (1.0D + a1);
     double g = this.b0 * this.b0 / (1.0D + a1 * a1 + 2.0D * a1 * c);
     this.k = (resonance / (g * g));
   }
   public final double filter(double input) {
     double x0 = this.hpf2.filter(input);
     double fb = this.hpf1.filter(x0 - this.k * this.y4);
 
     this.y1 += this.b0 * (shape(fb) - this.s1); this.s1 = shape(this.y1);
     this.y2 += this.b0 * (this.s1 - this.s2); this.s2 = shape(this.y2);
     this.y3 += this.b0 * (this.s2 - this.s3); this.s3 = shape(this.y3);
     this.y4 += this.b0 * (this.s3 - this.s4); this.s4 = shape(this.y4);
     return this.y4;
   }
 
   public void setSamplingFrequency(double sfrq) {
     this.sfrq = sfrq;
     setCoefficients(22050.0D, 0.0D);
     this.hpf1.setSamplingFrequency(sfrq);
     this.hpf1.setCutoff(50.0D);
     this.hpf2.setSamplingFrequency(sfrq);
     this.hpf2.setCutoff(58.0D);
   }
 
   private double zshape(double x) {
     if (x > 1.0D)
       x = 1.0D;
     else if (x < -1.0D)
       x = -1.0D;
     return x;
   }
   private final double shape(double x) {
     x = x < -sqrt2 ? -sqrt2 : x > sqrt2 ? sqrt2 : x;
     x -= x * x * x * 0.1666666666666667D;
     return x;
   }
 
   private final double xshape(double x)
   {
     if (x < -3.0D)
       return -1.0D;
     if (x > 3.0D) {
       return 1.0D;
     }
     return x * (27.0D + x * x) / (27.0D + 9.0D * x * x);
   }
 }

