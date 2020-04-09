 package com.klemstinegroup.synth;
 
 public class Decimator
 {
   private double R1;
   private double R2;
   private double R3;
   private double R4;
   private double R5;
   private double R6;
   private double R7;
   private double R8;
   private double R9;
   static final double h0 = 0.5D;
   static final double h1 = 0.3077392578125D;
   static final double h3 = -0.07794189453125D;
   static final double h5 = 0.02618408203125D;
   static final double h7 = -0.007080078125D;
   static final double h9 = 0.0010986328125D;
 
   public double calc(double x0, double x1)
   {
     double h9x0 = h9 * x0;
     double h7x0 = h7 * x0;
     double h5x0 = h5 * x0;
     double h3x0 = h3 * x0;
     double h1x0 = h1 * x0;
     double R10 = this.R9 + h9x0;
     this.R9 = (this.R8 + h7x0);
     this.R8 = (this.R7 + h5x0);
     this.R7 = (this.R6 + h3x0);
     this.R6 = (this.R5 + h1x0);
     this.R5 = (this.R4 + h1x0 + h0 * x1);
     this.R4 = (this.R3 + h3x0);
     this.R3 = (this.R2 + h5x0);
     this.R2 = (this.R1 + h7x0);
     this.R1 = h9x0;
     return R10;
   }
 }

