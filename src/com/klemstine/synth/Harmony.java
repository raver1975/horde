 package com.klemstine.synth;
 
 public class Harmony
 {
   public static final String[] notes = { "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B" };
   public static final int C = 0;
   public static final int Db = 1;
   public static final int D = 2;
   public static final int Eb = 3;
   public static final int E = 4;
   public static final int F = 5;
   public static final int Gb = 6;
   public static final int G = 7;
   public static final int Ab = 8;
   public static final int A = 9;
   public static final int Bb = 10;
   public static final int B = 11;
   public static final int OCTAVE = 12;
   public static final int CHORD_TRIAD = 3;
   public static final int CHORD_SEVENTH = 4;
   public static final int CHORD_EXTENDED = 5;
   public static final int[] SCALE_NATURAL_MINOR = { 0, 2, 3, 5, 7, 8, 10 };
   public static final int[] SCALE_MELODIC_MINOR = { 0, 2, 3, 5, 7, 8, 11 };
   public static final int[] SCALE_MAJOR = { 0, 2, 4, 5, 7, 9, 11 };
 
   public static final int[] SCALE_HUNGARIAN_MINOR = { 0, 2, 3, 6, 7, 10, 11 };
 
   public static final int[] SCALE_CHROMATIC = { 0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
 
   public static final int[][] SCALE_ALL = { SCALE_NATURAL_MINOR, SCALE_MELODIC_MINOR, SCALE_MAJOR, SCALE_HUNGARIAN_MINOR };
 
   public void printNotesInScale(int root, int[] scale)
   {
   }
 
   public int[] getNotesInChord(int key, int[] scale, int degree, int type, int inversion)
   {
     int[] chord = new int[type];
     for (int i = 0; i < type; i++) {
       chord[i] = (key + getFromScale(degree + i * 2, scale));
     }
     switch (inversion) {
     case 2:
       chord[1] -= 12;
       break;
     case 3:
       chord[2] -= 12;
     }
 
     return chord;
   }
 
   public int[] arpeggiate(int[] chord, int octaves) {
     int[] arpeggio = new int[chord.length * octaves];
 
     for (int octave = 0; octave < octaves; octave++) {
       for (int note = 0; note < chord.length; note++) {
         arpeggio[(octave * chord.length + note)] = (chord[note] + octave * 12);
       }
     }
 
     return arpeggio;
   }
 
   public int getFromScale(int i, int[] scale) {
     return scale[(i % scale.length)] + 12 * (i / scale.length);
   }
 }

