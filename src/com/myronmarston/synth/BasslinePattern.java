 package com.myronmarston.synth;
 
 public class BasslinePattern
 {
   public byte[] note;
   public boolean[] pause;
   public boolean[] accent;
   public boolean[] slide;
   public boolean[] transUp;
   public boolean[] transDown;
 
   public BasslinePattern(int length)
   {
     this.note = new byte[length];
     this.pause = new boolean[length];
     this.accent = new boolean[length];
     this.slide = new boolean[length];
     this.transUp = new boolean[length];
     this.transDown = new boolean[length];
     clear();
 
     this.note = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
 
     Harmony mt = new Harmony();
     int[] arpeggio = mt.arpeggiate(mt.getNotesInChord(0, Harmony.SCALE_ALL[(int)(Math.random() * Harmony.SCALE_ALL.length)], 2, 4, 0), 3);
   }
 
   public void clear()
   {
     for (int i = 0; i < this.note.length; i++) {
       this.note[i] = 0;
       this.pause[i] = false;
       this.accent[i] = false;
       this.slide[i] = false;
       this.transUp[i] = false;
       this.transDown[i] = false;
     }
   }
 
   public byte getNote(int index) {
     return this.note[index];
   }
 
   public void setNote(int index, byte value) {
     this.note[index] = value;
   }
 
   public boolean isPaused(int index) {
     return this.pause[index];
   }
 
   public void setPaused(int index, boolean value) {
     this.pause[index] = value;
   }
 
   public boolean isSlided(int index) {
     return this.slide[index];
   }
 
   public void setSlide(int index, boolean value) {
     this.slide[index] = value;
   }
 
   public boolean isAccented(int index) {
     return this.accent[index];
   }
 
   public void setAccent(int index, boolean value) {
     this.accent[index] = value;
   }
 
   public boolean isTransDown(int index) {
     return this.transDown[index];
   }
 
   public void setTransDown(int index, boolean value) {
     this.transDown[index] = value;
   }
 
   public boolean isTransUp(int index) {
     return this.transUp[index];
   }
 
   public void setTransUp(int index, boolean value) {
     this.transUp[index] = value;
   }
 }

