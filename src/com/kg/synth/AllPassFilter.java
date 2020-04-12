 package com.kg.synth;
 
 public class AllPassFilter
 {
   private double feedback;
   private double[] buffer;
   private int bufsize;
   private int bufidx;
 
   public AllPassFilter()
   {
     this.bufidx = 0;
   }
 
   public final double process(double input)
   {
     double bufout = this.buffer[this.bufidx];
 
     double output = -input + bufout;
     this.buffer[this.bufidx] = (input + bufout * this.feedback);
 
     if (++this.bufidx >= this.bufsize) {
       this.bufidx = 0;
     }
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
 
   void setfeedback(double val)
   {
     this.feedback = val;
   }
 
   double getfeedback() {
     return this.feedback;
   }
 }

