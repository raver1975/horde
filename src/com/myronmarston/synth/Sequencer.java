 package com.myronmarston.synth;
 
 public abstract class Sequencer
 {
   protected static double bpm = 140.0D;
 
   public abstract void tick();
 
   public double getBpm() { return bpm; }
 
   public void setBpm(double bpm)
   {
     this.bpm = bpm;
   }

     public abstract void randomizeSequence();
 }

