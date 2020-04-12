 package com.klemstinegroup.synth;
 
 public class Reverb
   implements Effect
 {
   private final int numcombs = 8;
   private final int numallpasses = 4;
   private final CombFilter[] combL;
   private final CombFilter[] combR;
   private final AllPassFilter[] allpassL;
   private final AllPassFilter[] allpassR;
   private double[] input = new double[2];
   private double inputL;
   private double inputR;
   private double gain;
   private double roomsize;
   private double roomsize1;
   private double damp;
   private double damp1;
   private double wet;
   private double wet1;
   private double wet2;
   private double dry;
   private double width;
   private double mode;
   private final double muted = 0.0D;
   private final double fixedgain = 0.015D;
   private final double scalewet = 3.0D;
   private final double scaledry = 2.0D;
   private final double scaledamp = 0.4D;
   private final double scaleroom = 0.28D;
   private final double offsetroom = 0.7D;
   private final double initialroom = 0.5D;
   private final double initialdamp = 0.5D;
   private final double initialwet = 0.3333333333333333D;
   private final double initialdry = 0.0D;
   private final double initialwidth = 1.0D;
   private final double initialmode = 0.0D;
   private final double freezemode = 0.5D;
   private final int stereospread = 23;
 
   private final int combtuningL1 = 1116;
   private final int combtuningR1 = 1139;
   private final int combtuningL2 = 1188;
   private final int combtuningR2 = 1211;
   private final int combtuningL3 = 1277;
   private final int combtuningR3 = 1300;
   private final int combtuningL4 = 1356;
   private final int combtuningR4 = 1379;
   private final int combtuningL5 = 1422;
   private final int combtuningR5 = 1445;
   private final int combtuningL6 = 1491;
   private final int combtuningR6 = 1514;
   private final int combtuningL7 = 1557;
   private final int combtuningR7 = 1580;
   private final int combtuningL8 = 1617;
   private final int combtuningR8 = 1640;
   private final int allpasstuningL1 = 556;
   private final int allpasstuningR1 = 579;
   private final int allpasstuningL2 = 441;
   private final int allpasstuningR2 = 464;
   private final int allpasstuningL3 = 341;
   private final int allpasstuningR3 = 364;
   private final int allpasstuningL4 = 225;
   private final int allpasstuningR4 = 248;
   private double outL;
   private double outR;
   private double in;
 
   public Reverb()
   {
     this.combL = new CombFilter[8];
     this.combR = new CombFilter[8];
     this.allpassL = new AllPassFilter[4];
     this.allpassR = new AllPassFilter[4];
 
     for (int i = 0; i < 8; i++) {
       this.combL[i] = new CombFilter();
       this.combR[i] = new CombFilter();
     }
     for (int i = 0; i < 4; i++) {
       this.allpassL[i] = new AllPassFilter();
       this.allpassR[i] = new AllPassFilter();
     }
 
     this.combL[0].setbuffer(1116);
     this.combR[0].setbuffer(1139);
     this.combL[1].setbuffer(1188);
     this.combR[1].setbuffer(1211);
     this.combL[2].setbuffer(1277);
     this.combR[2].setbuffer(1300);
     this.combL[3].setbuffer(1356);
     this.combR[3].setbuffer(1379);
     this.combL[4].setbuffer(1422);
     this.combR[4].setbuffer(1445);
     this.combL[5].setbuffer(1491);
     this.combR[5].setbuffer(1514);
     this.combL[6].setbuffer(1557);
     this.combR[6].setbuffer(1580);
     this.combL[7].setbuffer(1617);
     this.combR[7].setbuffer(1640);
     this.allpassL[0].setbuffer(556);
     this.allpassR[0].setbuffer(579);
     this.allpassL[1].setbuffer(441);
     this.allpassR[1].setbuffer(464);
     this.allpassL[2].setbuffer(341);
     this.allpassR[2].setbuffer(364);
     this.allpassL[3].setbuffer(225);
     this.allpassR[3].setbuffer(248);
 
     this.allpassL[0].setfeedback(0.5D);
     this.allpassR[0].setfeedback(0.5D);
     this.allpassL[1].setfeedback(0.5D);
     this.allpassR[1].setfeedback(0.5D);
     this.allpassL[2].setfeedback(0.5D);
     this.allpassR[2].setfeedback(0.5D);
     this.allpassL[3].setfeedback(0.5D);
     this.allpassR[3].setfeedback(0.5D);
     setwet(0.3333333333333333D);
     setRoomSize(0.5D);
     setdry(0.0D);
     setdamp(0.5D);
     setwidth(1.0D);
     setmode(0.0D);
 
     mute();
   }
 
   public void setRoomSize(double value)
   {
     this.roomsize = (value * 0.28D + 0.7D);
     update();
   }
 
   public double getroomsize() {
     return (this.roomsize - 0.7D) / 0.28D;
   }
 
   public void setdamp(double value) {
     this.damp = (value * 0.4D);
     update();
   }
 
   public double getdamp() {
     return this.damp / 0.4D;
   }
 
   public void setwet(double value) {
     this.wet = (value * 3.0D);
     update();
   }
 
   public double getwet() {
     return this.wet / 3.0D;
   }
 
   public void setdry(double value) {
     this.dry = (value * 2.0D);
   }
 
   public double getdry() {
     return this.dry / 2.0D;
   }
 
   public void setwidth(double value) {
     this.width = value;
     update();
   }
 
   public double getwidth() {
     return this.width;
   }
 
   public void setmode(double value) {
     this.mode = value;
     update();
   }
 
   public double getmode() {
     if (this.mode >= 0.5D) {
       return 1.0D;
     }
     return 0.0D;
   }
 
   public final void input(double sample) {
     this.inputL += sample;
     this.inputR += sample;
   }
 
   public final void input(double[] sample) {
     this.inputL += sample[0];
     this.inputR += sample[1];
   }
 
   public final double[] process()
   {
     this.outL = (this.outR = 0.0D);
 
     for (int i = 0; i < 8; i++) {
       this.outL += this.combL[i].process(this.inputL * this.gain);
       this.outR += this.combR[i].process(this.inputR * this.gain);
     }
 
     for (int i = 0; i < 4; i++) {
       this.outL = this.allpassL[i].process(this.outL);
       this.outR = this.allpassR[i].process(this.outR);
     }
 
     double[] rev = { this.outL, this.outR };
 
     this.inputL = (this.inputR = 0.0D);
 
     return rev;
   }
 
   public void update()
   {
     this.wet1 = (this.wet * (this.width / 2.0D + 0.5D));
     this.wet2 = (this.wet * ((1.0D - this.width) / 2.0D));
 
     if (this.mode >= 0.5D) {
       this.roomsize1 = 1.0D;
       this.damp1 = 0.0D;
       this.gain = 0.0D;
     } else {
       this.roomsize1 = this.roomsize;
       this.damp1 = this.damp;
       this.gain = 0.015D;
     }
 
     for (int i = 0; i < 8; i++) {
       this.combL[i].setfeedback(this.roomsize1);
       this.combR[i].setfeedback(this.roomsize1);
     }
 
     for (int i = 0; i < 8; i++) {
       this.combL[i].setdamp(this.damp1);
       this.combR[i].setdamp(this.damp1);
     }
   }
 
   public void mute() {
     if (getmode() >= 0.5D) {
       return;
     }
     for (int i = 0; i < 8; i++) {
       this.combL[i].mute();
       this.combR[i].mute();
     }
     for (int i = 0; i < 4; i++) {
       this.allpassL[i].mute();
       this.allpassR[i].mute();
     }
   }
 }

