 package com.klemstine.synth;
 
 public class Delay2
 {
   private double[] buffer_l;
   private double[] buffer_r;
   private int length = 16050;
   private int wpos = 16049;
   private double feedback = 0.75D;
   private double input = 0.5D;
   private HPFilter hpL;
   private HPFilter hpR;
   private double spreadL = 1.0D;
   private double spreadR = 0.0D;
   double[] sample = new double[2];
 
   public Delay2() {
     this.hpL = new HPFilter();
     this.hpL.setSamplingFrequency(Output.SAMPLE_RATE);
     this.hpL.setCutoff(200.0D);
     this.hpR = new HPFilter();
     this.hpR.setSamplingFrequency(Output.SAMPLE_RATE);
     this.hpR.setCutoff(200.0D);
     this.buffer_l = new double[(int) Output.SAMPLE_RATE];
     this.buffer_r = new double[(int) Output.SAMPLE_RATE];
   }
 
   public void input(double[] sample) {
     this.buffer_l[this.wpos] += sample[0] * this.input * this.spreadL;
     this.buffer_r[this.wpos] += sample[1] * this.input * this.spreadR;
   }
 
   public double[] output()
   {
     this.wpos += 1;
 
     if (this.wpos >= this.length) {
       this.wpos = 0;
     }
 
     this.sample[0] = this.buffer_l[this.wpos];
     this.sample[1] = this.buffer_r[this.wpos];
 
     this.buffer_l[this.wpos] = this.hpL.filter(this.sample[1] * this.feedback * this.spreadL + this.sample[0] * this.feedback * this.spreadR);
     this.buffer_r[this.wpos] = this.hpR.filter(this.sample[1] * this.feedback * this.spreadR + this.sample[0] * this.feedback * this.spreadL);
 
     return this.sample;
   }
 }

