 package com.myronmarston.synth;
 
 public class ADREnvelope
 {
   private double SAMPLE_RATE = Output.SAMPLE_RATE;
   public static final int STAGE_OFF = 3;
   public static final int STAGE_ATTACK = 0;
   public static final int STAGE_DECAY = 1;
   public static final int STAGE_RELEASE = 2;
   private int stage = 3;
 
   private double level = 1.0D;
   private static final double PI = 3.141592653589793D;
   private double attack;
   private double attackLS;
   private double decay;
   private double release;
   private double pos;
   private double x;
   private double a0;
   private double b1;
   private double out;
   private double in;
 
   public ADREnvelope()
   {
     setAttack(200.0D);
     setDecay(0.25D);
     setRelease(10.0D);
   }
 
   public final double tick()
   {
     if (this.stage == 3)
       return 0.0D;
     if (this.stage == 0) {
       this.in = this.level;
       if (this.pos >= this.attackLS) {
         this.stage = 1;
         setCoefficients(this.decay);
       }
       this.pos += 1.0D;
     }
     else {
       this.in = 0.0D;
     }
     this.out = (this.a0 * this.in + this.b1 * this.out);
 
     if (this.out < 9.99999991097579E-038D) {
       this.stage = 3;
     }
     return this.out;
   }
 
   public int getStage() {
     return this.stage;
   }
 
   public void attack() {
     this.stage = 0;
     setCoefficients(this.attack);
     this.pos = 0.0D;
   }
 
   public void release() {
     this.stage = 2;
     setCoefficients(this.release);
   }
 
   public void setAttack(double freq) {
     this.attack = freq;
     this.attackLS = (int)(this.SAMPLE_RATE / this.attack);
     if (this.stage == 0)
       setCoefficients(freq);
   }
 
   public void setDecay(double freq) {
     this.decay = freq;
     if (this.stage == 1)
       setCoefficients(freq);
   }
 
   public void setRelease(double freq) {
     this.release = freq;
     if (this.stage == 2)
       setCoefficients(freq);
   }
 
   public void setLevel(double level) {
     this.level = level;
   }
 
   public void setSamplingFrequency(double value) {
     this.SAMPLE_RATE = value;
   }
 
   public final void setCoefficients(double freq)
   {
     this.x = Math.exp(-6.283185307179586D * freq / this.SAMPLE_RATE);
     this.a0 = (1.0D - this.x);
     this.b1 = this.x;
   }
 }

