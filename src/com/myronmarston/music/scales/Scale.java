/*
 * Copyright 2008, Myron Marston <myron DOT marston AT gmail DOT com>
 *
 * This file is part of Fractal Composer.
 *
 * Fractal Composer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option any later version.
 *
 * Fractal Composer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Fractal Composer.  If not, see <http://www.gnu.org/licenses/>. 
 */

package com.myronmarston.music.scales;

import com.myronmarston.music.Note;
import com.myronmarston.music.NoteName;
import com.myronmarston.util.ClassHelper;
import com.myronmarston.util.MathHelper;
import org.simpleframework.xml.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Scales are used to convert a Note to a MidiNote and to provide the midi file
 * with a time signature.  Since each Note contains data on its identity 
 * relative to a given scale, the Scale must be used to convert it to a concrete 
 * MidiNote (e.g., pitch, etc).
 * 
 * All extending classes need to have the @Root annotation to be serializable
 * by the simple XML framework and also need to provide two constructors:
 * a no-argument one (which should set the scale with some reasonable default
 * key) and one that takes a note name to specify the key.
 * 
 * @author Myron 
 */
@Root
public abstract class Scale implements Cloneable {    
    @Element
    private KeySignature keySignature;
    
    /**
     * A list of all valid scale types, mapped to a list of valid key names for
     * that scale type.
     */
    public final static Map<Class, List<NoteName>> SCALE_TYPES;    
    
    /**
     * The scale that should be used before the user specifies one--a chromatic scale.
     */
    public final static Scale DEFAULT;
    
    /**
     * Initializes the default scale and the scale types list.
     */
    static {
        try {
            DEFAULT = new ChromaticScale();
        } catch (InvalidKeySignatureException ex) {
            throw new UndeclaredThrowableException(ex, "An exception occurred while instantiating a ChromaticScale.  This indicates a programming error.");
        }
                     
        List<Class> scaleTypeList;
        try {                
            scaleTypeList = ClassHelper.getSubclassesInPackage(Scale.class.getPackage().getName(), Scale.class);            
        } catch (ClassNotFoundException ex) {
            // our code above is guarenteed to pass a valid package name, so we 
            // should never get this exception; if we do, there is a bug in the
            // code somewhere, so throw an assertion error.
            throw new UndeclaredThrowableException(ex, "An exception occurred while getting the scale types.  This indicates a programming error.");
        } 
        
        //TODO: make SCALE_TYPES unmodifiable
        SCALE_TYPES = new LinkedHashMap<Class, List<NoteName>>(scaleTypeList.size());
        for (Class scaleType : scaleTypeList) {   
            try {                
                if (Modifier.isAbstract(scaleType.getModifiers())) continue;
                
                @SuppressWarnings("unchecked")
                Constructor constructor = scaleType.getDeclaredConstructor();                
                Scale s = (Scale) constructor.newInstance();
                SCALE_TYPES.put(scaleType, s.getValidKeyNames());    
            } catch (Exception ex) {
                // the reflection code above has lots of declared exceptions,
                // but they should only occur if we have a programming error,
                // so we'll just wrap them in an undeclared exception if they 
                // occur...
                throw new UndeclaredThrowableException(ex, "An exception occurred while getting the valid key names.  This indicates a programming error.");
            }                    
        }        
    }
    
    /**
     * The number of chromatic pitches in one octave: 12.
     */
    public final static int NUM_CHROMATIC_PITCHES_PER_OCTAVE = 12;         
    
    /**
     * Constructor.
     * 
     * @param keySignature the key signature of the scale
     */
    protected Scale(KeySignature keySignature) {
        this.keySignature = keySignature;
    }

    /**
     * Gets the tonal center of the scale.
     * 
     * @return the key name
     */
    public NoteName getKeyName() {        
        return keySignature.getKeyName();
    }

    /**
     * Gets the key signature of the scale.
     * 
     * @return the key signature
     */
    public KeySignature getKeySignature() {
        return keySignature;
    }         
    
    /**
     * Gets an array of integers representing the number of half steps above
     * the tonic for each scale step.
     * 
     * @return array of integers
     */
    abstract public int[] getScaleStepArray();
    
    /**
     * Gets an array of integers of letter numbers above the tonic letter number.
     * 
     * @return letter number array.
     */
    abstract public int[] getLetterNumberArray();     
    
    /**
     * Normalizes a chromatic adjustment, putting it in the range -6 to 6.
     * 
     * @param chromaticAdjustment the chromatic adjustment to normalize
     * @return the normalized value
     */
    static public int getNormalizedChromaticAdjustment(int chromaticAdjustment) {
        // put it in the range -6..6 if it is outside of that...
        if (chromaticAdjustment > (Scale.NUM_CHROMATIC_PITCHES_PER_OCTAVE / 2)) {            
            return chromaticAdjustment - Scale.NUM_CHROMATIC_PITCHES_PER_OCTAVE;
        }
        
        if (chromaticAdjustment < -(Scale.NUM_CHROMATIC_PITCHES_PER_OCTAVE / 2)) {
            return chromaticAdjustment + Scale.NUM_CHROMATIC_PITCHES_PER_OCTAVE;
        }
        
        return chromaticAdjustment;
    }
    
    /**
     * Normalizes the given scale step value to be between 0 and the number of
     * scale steps for this scale minus 1.  For example, for diatonic scales, 
     * the value is put in the range 0 to 7.
     * 
     * @param scaleStep the scale step to normalize
     * @return the normalized scale step
     */
    public int getNormalizedScaleStep(int scaleStep) {
        return MathHelper.getNormalizedValue(scaleStep, this.getScaleStepArray().length);
    }
    
    /**
     * Sets the scaleStep and chromaticAdjustment on the given note, based on
     * the passed noteName.  
     * 
     * @param note the scaleStep and chromatic adjustment will be set on this 
     *        note
     * @param noteName the noteName of the pitch that should be used to
     *        determine the pitch values
     */
    public void setNotePitchValues(Note note, NoteName noteName) {
        int intervalAboveTonic = this.getKeyName().getPositiveIntervalSize(noteName);
        int scaleStepIndex = Arrays.binarySearch(this.getLetterNumberArray(), intervalAboveTonic);
        
        if (scaleStepIndex >= 0) { 
            // our scale contains this letter, so we can use it to determine our scale step
            // and then figure out our necessary chromaticAdjustment
            setNotePitchValues_Helper(note, noteName, scaleStepIndex);
        } else {
            // our scale does not contain this letter (example: F# for C major 
            // pentatonic-C D E G A).  We need to figure out which of our scale
            // steps this is closest to, and use that, modifying the chromatic
            // adjustment accordingly            
            
            // find the two closest note letters...
            int letterNumberArrayInsertionPt = -scaleStepIndex - 1;
            int nearestLowerScaleStepIndex = (letterNumberArrayInsertionPt == 0 ? this.getLetterNumberArray().length - 1 : letterNumberArrayInsertionPt - 1);
            int nearestHigherScaleStepIndex = (letterNumberArrayInsertionPt == this.getLetterNumberArray().length ?  0 : letterNumberArrayInsertionPt);
            
            // figure out which of these two is closest to our given note...
            int lowerScaleStepNumHalfStepsAboveTonic = this.getScaleStepArray()[nearestLowerScaleStepIndex];
            int higherScaleStepNumHalfStepsAboveTonic = this.getScaleStepArray()[nearestHigherScaleStepIndex];
            int givenNoteHalfStepsAboveTonic = this.getKeyName().getPositiveChromaticSteps(noteName);
                        
            int distanceFromLowerScaleStep = Math.abs(getNormalizedChromaticAdjustment(givenNoteHalfStepsAboveTonic - lowerScaleStepNumHalfStepsAboveTonic));
            int distanceFromHigherScaleStep = Math.abs(getNormalizedChromaticAdjustment(higherScaleStepNumHalfStepsAboveTonic - givenNoteHalfStepsAboveTonic));
                        
            if (distanceFromLowerScaleStep < distanceFromHigherScaleStep) {
                setNotePitchValues_Helper(note, noteName, nearestLowerScaleStepIndex);
            } else {
                setNotePitchValues_Helper(note, noteName, nearestHigherScaleStepIndex);
            }    
        }    
    }

    /**
     * Helper method for setNotePitchValues().  Sets the scaleStep and 
     * chromatic adjustment on the note, given a noteName and a scaleStepIndex.
     * 
     * @param note the scaleStep and chromatic adjustment will be set on this 
     *        note
     * @param noteNameForPitch the noteName of the pitch that should be used to
     *        determine the pitch values
     * @param scaleStepIndex the index of the scaleStep withing this scales's
     *        scaleStepArray that should be used
     */
    protected void setNotePitchValues_Helper(Note note, NoteName noteNameForPitch, int scaleStepIndex) {        
        note.setScaleStep(scaleStepIndex);

        int givenNoteHalfStepAboveTonic = this.getKeyName().getPositiveChromaticSteps(noteNameForPitch);
        int diatonicNoteHalfStepsAboveTonic = this.getScaleStepArray()[scaleStepIndex];
        int chromaticAdjustment = getNormalizedChromaticAdjustment(givenNoteHalfStepAboveTonic - diatonicNoteHalfStepsAboveTonic);
                
        note.setChromaticAdjustment(chromaticAdjustment);
    }        
    
    /**
     * Gets a list of valid keys for this scale type.
     * 
     * @return a list of valid keys
     */
    public List<NoteName> getValidKeyNames() {
        return this.getKeySignature().getTonality().getValidKeyNames();
    }

    /**
     * Gets a new scale that is of the same type, but uses a different key.
     * 
     * @param key the new key
     * @return the new scale
     * @throws com.myronmarston.music.scales.InvalidKeySignatureException if
     *         the given key produces a KeySignature with more than 7 sharps or
     *         flats
     */
    public Scale getCopyWithDifferentKey(NoteName key) throws InvalidKeySignatureException {
        Scale scale = this.clone();
        scale.keySignature = new KeySignature(this.keySignature.getTonality(), key);
        return scale;
    }
    
    /**
     * Gets a recommended number of letter numbers to transpose, based on the 
     * given number of scale steps.
     * 
     * @param transposeScaleSteps the number of scale steps
     * @return the recommended number of letter numbers to transpose
     */
    public int getRecommendedTransposeLetterNumber(int transposeScaleSteps) {        
        return this.getLetterNumberArray()[this.getNormalizedScaleStep(transposeScaleSteps)];        
    }
        
    @Override
    public Scale clone() {
        try {
            return (Scale) super.clone();
        } catch (CloneNotSupportedException ex) {
            // We have implemented the Cloneable interface, so we should never
            // get this exception.  If we do, there's something very, very wrong...
            throw new UndeclaredThrowableException(ex, "Unexpected error while cloning.  This indicates a programming or JVM error.");
        }         
    }   
    
    @Override
    public String toString() {
        return this.getKeyName().toString() + this.getClass().getSimpleName().replaceAll("([A-Z])", " $1");
    }        
        
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Scale other = (Scale) obj;
        if (this.keySignature != other.keySignature && (this.keySignature == null || !this.keySignature.equals(other.keySignature))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;        
        hash = 79 * hash + (this.keySignature != null ? this.keySignature.hashCode() : 0);
        hash = 79 * hash + this.getClass().hashCode();
        return hash;
    }        
}
