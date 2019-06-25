 package com.myronmarston.synth;
 
 public class CombFilter
 {
   private double feedback;
   private double filterstore;
   private double damp1;
   private double damp2;
   private double[] buffer;
   private int bufsize;
   private int bufidx;
 
   public final double process(double input)
   {
     double output = this.buffer[this.bufidx];
     output += 1.0E-038D;
 
     this.filterstore = (output * this.damp2 + this.filterstore * this.damp1);
     this.filterstore += 1.0E-038D;
 
     this.buffer[this.bufidx] = (input + this.filterstore * this.feedback);
     if (++this.bufidx >= this.bufsize) this.bufidx = 0;
 
     return output;
   }
 
   void setbuffer(int size) {
     this.buffer = new double[size];
     this.bufsize = size;
   }
 
   void mute() {
     for (int i = 0; i < this.bufsize; i++)
       this.buffer[i] = 0.0D;
   }
 
   void setdamp(double val)
   {
     this.damp1 = val;
     this.damp2 = (1.0D - val);
   }
 
   double getdamp()
   {
     return this.damp1;
   }
 
   void setfeedback(double val)
   {
     this.feedback = val;
   }
 
   double getfeedback()
   {
     return this.feedback;
   }
 }

