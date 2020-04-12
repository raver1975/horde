 package com.kg.synth;
 
 public class BasicSampler extends Oscillator
 {
   private double[] wave;
   private int wavePhase;
   private int waveLength;
 
   public BasicSampler()
   {
     this.wavePhase = 0;
   }
 
   public void setSample(double[] wave) {
     this.wavePhase = 0;
     this.wave = wave;
     this.wavePhase = (wave.length - 2);
     this.waveLength = (wave.length - 1);
   }
 
   public void trigger() {
     this.wavePhase = 0;
   }
 
   public final double tick()
   {
     if (this.wavePhase < this.waveLength)
     {
       this.wavePhase += 1;
 
       return this.wave[this.wavePhase];
     }
     return 0.0D;
   }
 }

