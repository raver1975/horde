 package com.klemstinegroup.synth;
 
 public class Control
 {
   private double value;
   private double instance;
   double x;
   double a0;
   double b1;
   double out;
   double pi = 3.141592653589793D;
   double samplingFrequency;
 
   public Control(double value)
   {
     this.out = value;
     this.value = value;
     this.instance = value;
     this.samplingFrequency = Output.SAMPLE_RATE;
   }
 
   public void setValue(double value) {
     this.value = value;
   }
 
   public double getValue() {
     return this.value;
   }
 
   public double getInstancedValue() {
     this.out = (this.a0 * this.value + this.b1 * this.out);
     return this.out;
   }
 
   public void setCoefficients(double freq) {
     this.x = Math.exp(-2.0D * this.pi * freq / this.samplingFrequency);
     this.a0 = (1.0D - this.x);
     this.b1 = this.x;
   }
 
   public void setSamplingFrequency(double value) {
     this.samplingFrequency = value;
     setCoefficients(5.0D);
   }
 }

