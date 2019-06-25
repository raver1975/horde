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

package com.myronmarston.music;

import com.myronmarston.music.notation.NotationNote;
import com.myronmarston.music.notation.PartSection;
import com.myronmarston.music.scales.Scale;
import com.myronmarston.music.settings.VoiceSection;
import com.myronmarston.util.Fraction;
import com.myronmarston.util.MathHelper;
import org.simpleframework.xml.*;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a note, relative to a particular scale.  This note will need to be
 * converted to midi events using the MidiNote class before it can be used in
 * a Midi track.  
 * 
 * @author Myron
 */
@Root
public class Note implements Cloneable {
    /**
     * Number of scale steps above the tonic; 0 = tonic, 7 = octave, 9 = third 
     * an octave above, etc.
     */
    @Attribute
    private int scaleStep;
    
    /**
     * Which octave the note should be in.  0 begins the first octave in Midi 
     * that contains the tonic.
     */
    @Attribute
    private int octave; 
    
    /**
     * The number of half steps to adjust from the diatonic note; used if this 
     * note is an accidental
     */
    @Attribute
    private int chromaticAdjustment; 
    
    /**
     * How long the note should last, in fractions of whole notes.        
     */
    @Element(required=false)    
    private Fraction duration; 
    
    /**
     * How loud the note should be on a scale from 0 to 127.    
     */
    @Attribute
    private int volume = MidiNote.DEFAULT_VELOCITY; 
        
    /**
     * The scale that should be used with this scale.  This is optional, as
     * methods that use a scale take one as a parameter.  Specifying it here
     * overrides the scale passed to a method.
     */
    @Element(required=false)
    private Scale scale;
    
    /**
     * Chromatic adjustment applied to a segment of notes.  This is used to
     * create self-similarity when the germ contains a chromatic note.
     */
    @Attribute
    private int segmentChromaticAdjustment;
    
    /**
     * The letter number (0-6), in reference to the tonic.  For example, 0 in 
     * the key of D means 'D', 3 means 'G', etc.
     */
    @Attribute
    private int letterNumber;
 
    
    @Element(required=false)
    private VoiceSection sourceVoiceSection;
    
    @Attribute
    private boolean isFirstNoteOfGermCopy = false;
       
    /**
     * Regular expression pattern for matching and parsing a note string.
     */
    public final static Pattern REGEX_PATTERN;
    
    /**
     * Regular expression string for matching and parsing a note string.
     */
    public final static String REGEX_STRING;
    
    /**
     * The flags used by the regular expression.
     */
    public final static int REGEX_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.CANON_EQ;
    
    static {
        // We put the dynamic regex in this one to use it in a negative lookahead.
        // This prevents a dynamic from being used with a rest as that is nonsensical.
        String noteNameOrRestWithOctave = String.format("(([A-G](?:bb|b||#|x))(\\d)|R(?!\\S*(?:%s)))", Dynamic.REGEX_STRING);
        
        String optionalCommaPatternRegex = "(?:,(%s))?";
        String duration = String.format(optionalCommaPatternRegex, Fraction.POSITIVE_FRACTION_REGEX_STRING_WITHOUT_EDGE_ASSERTIONS);
        String dynamic = String.format(optionalCommaPatternRegex, Dynamic.REGEX_STRING);
                
        REGEX_STRING = noteNameOrRestWithOctave + duration + dynamic;
        REGEX_PATTERN = Pattern.compile("^" + REGEX_STRING + "$", REGEX_FLAGS);
    }
    
    /**
     * Default constructor.
     */
    public Note() {}
     
    /**
     * Constructor.
     * 
     * @param letterNumber the letter number, relative to the scale
     * @param scaleStep number of scale steps above the tonic; 0 = tonic,
     *        7 = octave, 9 = third an octave above, etc.
     * @param octave which octave the note should be in.  0 begins the first 
     *        octave in Midi that contains the tonic.
     * @param chromaticAdjustment the number of half steps to adjust from the
     *        diatonic note; used if this note is an accidental
     * @param duration how long the note should last, in whole notes
     * @param volume how loud the note should be (0-127)
     * @param scale the scale to use for this note; if this is non-null, it
     *        will be used rather than the scale passed to any of this object's
     *        methods
     * @param segmentChromaticAdjustment additional chromatic adjustment for a
     *        segment of the piece; used to deal with chromatic self-similarity     
     */ 
    public Note(int letterNumber, int scaleStep, int octave, int chromaticAdjustment, Fraction duration, int volume, Scale scale, int segmentChromaticAdjustment) {        
        this.setLetterNumber(letterNumber);
        this.setScaleStep(scaleStep);
        this.setOctave(octave);
        this.setChromaticAdjustment(chromaticAdjustment);
        this.setDuration(duration);
        this.setVolume(volume);            
        this.setScale(scale); 
        this.setSegmentChromaticAdjustment(segmentChromaticAdjustment);
    }        
     
    /**
     * Creates a note that is a rest.
     * 
     * @param duration how long the rest should last, in whole notes
     * @return the rest     
     */
    static public Note createRest(Fraction duration) {
        Note rest = new Note();
                
        rest.setDuration(duration); // use the setter so we get an exception if the duration is 0
        rest.setVolume(MidiNote.MIN_VELOCITY);              
        
        assert rest.isRest() : rest;
        return rest;
    }

    /**
     * Gets the letter number for this note.
     * 
     * @return the letter number for this note
     */
    public int getLetterNumber() {
        return letterNumber;
    }

    /**
     * Sets the letter number for this note.
     * 
     * @param letterNumber the letter number
     * @throws UnsupportedOperationException if the note is a rest
     */
    public void setLetterNumber(int letterNumber) throws UnsupportedOperationException {                
        throwUnsupportedOperationExceptionIfRest("letterNumber", letterNumber);
        this.letterNumber = letterNumber;
    }   
    
    /**
     * Gets the number of scale steps above the tonic; In an 7-note scale, 
     * 0 = tonic, 7 = octave, 9 = third an octave above, etc.
     * 
     * @return the scale step
     */
    public int getScaleStep() {
        return scaleStep;
    }

    /**
     * Sets the number of scale steps above the tonic; In an 7-note scale, 
     * 0 = tonic, 7 = octave, 9 = third an octave above, etc.
     * 
     * @param scaleStep the scale step
     * @throws UnsupportedOperationException if the note is a rest
     */
    public void setScaleStep(int scaleStep) throws UnsupportedOperationException {
        throwUnsupportedOperationExceptionIfRest("scaleStep", scaleStep);
        this.scaleStep = scaleStep;        
    }

    /**
     * Gets which octave the note should be in.  0 begins the first octave in 
     * Midi that contains the tonic.
     * 
     * @return the octave
     */
    public int getOctave() {
        return octave;
    }

    /**
     * Sets which octave the note should be in.  0 begins the first octave in 
     * Midi that contains the tonic.
     * 
     * @param octave the octave
     * @throws UnsupportedOperationException if the note is a rest
     */
    public void setOctave(int octave) throws UnsupportedOperationException {
        throwUnsupportedOperationExceptionIfRest("octave", octave);        
        this.octave = octave;        
    }

    /**
     * Gets the number of half steps to adjust from the diatonic note; used if 
     * this note is an accidental.
     * 
     * @return the chromatic adjustment
     */
    public int getChromaticAdjustment() {
        return chromaticAdjustment;
    }

    /**
     * Sets the number of half steps to adjust from the diatonic note; used if 
     * this note is an accidental.
     * 
     * @param chromaticAdjustment the chromatic adjustment
     * @throws UnsupportedOperationException if the note is a rest
     */
    public void setChromaticAdjustment(int chromaticAdjustment) throws UnsupportedOperationException {
        throwUnsupportedOperationExceptionIfRest("chromaticAdjustment", chromaticAdjustment);
        this.chromaticAdjustment = chromaticAdjustment;
    }
    
    /**
     * Gets the chromatic adjustment for a segment of notes.  This is used to
     * apply self-similarity to a germ that has chromatic notes.
     * 
     * @return the segment chromatic adjustment
     */
    public int getSegmentChromaticAdjustment() {
        return this.segmentChromaticAdjustment;
    }

    /**
     * Sets the chromatic adjustment for a segment of notes.  This is used to
     * apply self-similarity to a germ that has chromatic notes.
     * 
     * @param segmentChromaticAdjustment the segment chromatic adjustment
     * @throws UnsupportedOperationException if the note is a rest
     */
    public void setSegmentChromaticAdjustment(int segmentChromaticAdjustment) throws UnsupportedOperationException {        
        this.segmentChromaticAdjustment = segmentChromaticAdjustment;
    }

    /**
     * Gets the scale for this note.  
     * 
     * @return the scale
     */
    public Scale getScale() {
        assert (this.isRest() || scale != null) : "The note did not have a scale as expected.";
        return scale;
    }

    /**
     * Sets the scale for this note. 
     * 
     * @param scale the scale
     */
    public void setScale(Scale scale) {
        assert (this.isRest() || scale != null) : "The note did not have a scale as expected.";
        this.scale = scale;
    }        
    
    /**
     * Gets how long the note should last, in whole notes.
     * 
     * @return the duration
     */
    public Fraction getDuration() {
        return duration;
    }       

    /**
     * Sets how long the note should last, in whole notes.
     * 
     * @param duration the duration
     * @throws IllegalArgumentException if the duration is less than zero
     */
    public void setDuration(Fraction duration) throws IllegalArgumentException {
        if (duration != null && duration.asDouble() <= 0) throw new IllegalArgumentException("The duration must be greater than zero.");
        this.duration = duration;
    }

    /**
     * Gets how loud the note should be (0-127).  0 makes it a rest.
     * 
     * @return the volume
     */
    public int getVolume() {
        return volume;
    }
    
    /**
     * Sets how loud the note should be (0-127).  0 makes it a rest.
     * 
     * @param volume the volume
     * @throws IllegalArgumentException if the note is outside of the range 0-127
     */
    public void setVolume(int volume) throws IllegalArgumentException {
        if (volume < MidiNote.MIN_VELOCITY || volume > MidiNote.MAX_VELOCITY) {
            throw new IllegalArgumentException(String.format("The volume must be between %d and %d.  The passed volume was %d.", MidiNote.MIN_VELOCITY, MidiNote.MAX_VELOCITY, volume));
        }
        
        this.volume = volume;
        
        // set the pitch fields to default values. We do this so that our
        // hashCode and equals work properly.  All rests of the same length
        // should be equal.
        if (this.isRest()) { 
            this.setScaleStep(0);
            this.setOctave(0);
            this.setChromaticAdjustment(0);
        }
    }

    /**
     * Gets the voice section that generated this note.
     * 
     * @return the voice section that is the source of this note
     */
    public VoiceSection getSourceVoiceSection() {
        return sourceVoiceSection;
    }

    /**
     * Sets the voice section that generated this note.
     * 
     * @param sourceVoiceSection the voice section that is the source of this
     *        note
     */
    public void setSourceVoiceSection(VoiceSection sourceVoiceSection) {
        this.sourceVoiceSection = sourceVoiceSection;
    }
            
    /**
     * Gets whether or not this note is a rest (i.e., has a volume of 0).
     * 
     * @return true if the volume is 0
     */
    public boolean isRest() {
        return (this.getVolume() == 0);
    }

    /**
     * Returns true if this is the first note of the germ, or of a derived copy
     * of the germ that may have gone through some transformations.
     * 
     * @return true if this is the first note of a germ copy.
     */
    public boolean isFirstNoteOfGermCopy() {
        return isFirstNoteOfGermCopy;
    }

    /**
     * Sets whether or not this is the first note of a germ or derived copy.
     * 
     * @param isFirstNoteOfGermCopy value to set
     */
    public void setIsFirstNoteOfGermCopy(boolean isFirstNoteOfGermCopy) {
        this.isFirstNoteOfGermCopy = isFirstNoteOfGermCopy;
    }   
    
    /**
     * Throws an exception if the changing field is not being changed to zero
     * and the note is a rest.  Called from the setters.
     * 
     * @param changingField the field being changed
     * @param value the value the field is being changed to
     * @throws UnsupportedOperationException if the note is a rest
     */
    private void throwUnsupportedOperationExceptionIfRest(String changingField, int value) throws UnsupportedOperationException {
        if (this.isRest() && value != 0) { // each of the fields can be zero...
            throw new UnsupportedOperationException(String.format("The Note is a rest.  The %s field cannot be changed on a rest.", changingField));
        }
    }
    
    /**
     * Adjusts the scale step, letter number and octave for a transformer.  The
     * passed values should be given as deltas, rather than as new values.  The
     * letter number will be appropriately adjusted if a scale with less than 7
     * notes is used so that diatonic notes use the correct letter number.
     * 
     * @param scaleStepAdjustment a value to add to the scale step
     * @param letterNumberAdjustment a value to add to the letter number
     * @param octaveAdjustment a value to add to the octave
     */
    public void performTransformerAdjustment(int scaleStepAdjustment, int letterNumberAdjustment, int octaveAdjustment) {
        assert !this.isRest() : "The performTransformationAdjustment should not be called on a rest.";
        this.setScaleStep(this.getScaleStep() + scaleStepAdjustment);
        this.setLetterNumber(this.getLetterNumber() + letterNumberAdjustment);
        this.setOctave(this.getOctave() + octaveAdjustment);
        
        if (this.getScale().getScaleStepArray().length < NoteName.NUM_LETTER_NAMES && 
            this.getChromaticAdjustment() == 0 && this.getSegmentChromaticAdjustment() == 0) {

            int normalizedScaleStep = this.getNormalizedNote().getScaleStep();
            this.setLetterNumber(this.getScale().getLetterNumberArray()[normalizedScaleStep]);
        }         
    }
            
    /**
     * Parses a note string such as F#4,1/8,MF.
     * 
     * @param noteString string to parse
     * @param scale the scale to use to determine the scaleStep and 
     *        chromaticAdjustment
     * @param defaultDuration duration to use if the noteString does not contain
     *        a duration
     * @param defaultVolume volume to use if the noteString does not contain a 
     *        volume
     * @return a new Note with fields set based on the parsed noteString
     * @throws com.myronmarston.music.NoteStringParseException thrown when the
     *         noteString cannot be parsed
     */
    public static Note parseNoteString(String noteString, Scale scale, Fraction defaultDuration, Integer defaultVolume) throws NoteStringParseException {        
        if (defaultDuration == null) defaultDuration = new Fraction(1, 4);
        if (defaultVolume == null) defaultVolume = Dynamic.MF.getMidiVolume();
        
        Matcher match = Note.REGEX_PATTERN.matcher(noteString);        
        if (match.matches()) {     
            // Matche groups example: Gbb4,3/8,MF
            //0. Gbb4,3/8,MF
            //1. Gbb4
            //2. Gbb
            //3. 4
            //4. 3/8
            //5. 3
            //6. 8
            //7. MF
            
            Fraction duration = parseNoteString_getDuration(match, 5, 6, defaultDuration);
            Note newNote;
            
            if (match.group(1).toUpperCase(Locale.ENGLISH).startsWith("R")) { // indicates the note is a rest...
                newNote = Note.createRest(duration);
            } else {                
                NoteName noteName = parseNoteString_getNoteName(match, 2);   
                int octave = parseNoteString_getOctave(match, 3);                
                int volume = parseNoteString_getVolume(match, 7, defaultVolume);                
                
                // adjust the octave for a note like Cb, Cbb, B# and Bx
                octave += (noteName.getNoteNumber() - noteName.getNormalizedNoteNumber()) / Scale.NUM_CHROMATIC_PITCHES_PER_OCTAVE;
                
                newNote = new Note();
                newNote.setScale(scale);
                newNote.setDuration(duration);
                newNote.setVolume(volume);
                scale.setNotePitchValues(newNote, noteName);
                newNote.setLetterNumber(scale.getKeyName().getPositiveIntervalSize(noteName));
                                
                // The octave number is dependent on the scale.  For example, 
                // the note C4 (middle C) should parse as scale step 2, octave 3 
                // for the A minor scale, because the A octave is higher than 
                // the C octave.
                // I tried figuring out the logic for this, but it's very 
                // complicated and I couldn't figure it out after a few hours.
                // So, instead we use the result of parsing the note using
                // the chromatic scale as our baseline, and compare to that,
                // using the difference between the baseline midi pitch number
                // and the midi pitch number we get from our scale to calculate
                // the correct octave.                
                if (scale == Scale.DEFAULT) {
                    // this is the chromatic scale, so just set the octave directly...
                    newNote.setOctave(octave);                                
                } else {                    
                    Note testNote = parseNoteString(noteString, Scale.DEFAULT, defaultDuration, defaultVolume);
                    int midiPitchNum = testNote.getMidiPitchNumber(true); 
                    int midiPitchNumWithoutOctave = newNote.getMidiPitchNumber(true);
                    int difference = midiPitchNum - midiPitchNumWithoutOctave;
                    
                    assert difference % Scale.NUM_CHROMATIC_PITCHES_PER_OCTAVE == 0 : difference;
                    octave = difference / Scale.NUM_CHROMATIC_PITCHES_PER_OCTAVE;
                    newNote.setOctave(octave);
                    
                    assert newNote.getMidiPitchNumber(true) == midiPitchNum;
                }                
            }            
            
            return newNote;
        } else {
            throw new NoteStringParseException(noteString);            
        }                
    }             
    
    /**
     * Gets the note name for the parseNoteString method.
     *      
     * @param match the regex match object
     * @param matchGroupIndex the index for the captured group containing 
     *        noteName
     * @return the NoteName, or null if the note should be a rest     
     */
    static private NoteName parseNoteString_getNoteName(Matcher match, int matchGroupIndex) {
        assert match.matches() : match;
        String noteNameStr = match.group(matchGroupIndex);
        assert noteNameStr != null;

        if (noteNameStr.toUpperCase(Locale.ENGLISH).contentEquals("R")) return null;
        
        NoteName noteName = NoteName.getNoteName(noteNameStr);
        assert noteName != null;        
        return noteName;         
    }
    
    /**
     * Gets the octave for the parseNoteString method.
     * 
     * @param match the regex match object
     * @param matchGroupIndex the index for the captured group containing the
     *        octave
     * @return the octave
     */
    private static int parseNoteString_getOctave(Matcher match, int matchGroupIndex) {
        assert match.matches() : match;
        String octaveStr = match.group(matchGroupIndex);
        assert octaveStr != null;
                
        return Integer.parseInt(octaveStr);        
    }
    
    /**
     * Gets the duration fraction for the parseNoteString method.
     * 
     * @param match the regex match object
     * @param numeratorMatchGroupIndex the index for the captured group 
     *        containing the duration fraction numerator
     * @param denominatorMatchGroupIndex the index for the captured group 
     *        containing the duration fraction denominator
     * @param defaultDuration duration to use if the noteString does not have a
     *        duration
     * @return the duration fraction
     */
    private static Fraction parseNoteString_getDuration(Matcher match, int numeratorMatchGroupIndex, int denominatorMatchGroupIndex, Fraction defaultDuration) {        
        assert match.matches() : match;
        String durationNumStr = match.group(numeratorMatchGroupIndex);
        String durationDenStr = match.group(denominatorMatchGroupIndex);
        
        if (durationNumStr == null && durationDenStr == null) {
            // This note doesn't have a duration; instead use the default duration
            return defaultDuration;       
        } else {
            assert durationNumStr != null;            
            int durationNum = Integer.parseInt(durationNumStr);
            int durationDen = durationDenStr == null ? 1 : Integer.parseInt(durationDenStr);
            assert durationDen != 0;
            assert durationNum != 0;
            
            return new Fraction(durationNum, durationDen);
        }
    }
    
    /**
     * Gets the volume for the parseNoteString method.
     * 
     * @param match the regex match object
     * @param matchGroupIndex the index for the captured group containing the
     *        dynamic
     * @param defaultVolume volume to use if the noteString does not have a 
     *        dynamic
     * @return the volume
     */
    private static int parseNoteString_getVolume(Matcher match, int matchGroupIndex, int defaultVolume) {        
        String dynamicStr = match.group(matchGroupIndex);
                
        if (dynamicStr == null || dynamicStr.isEmpty()) {
            return defaultVolume;
        } else {             
            return Dynamic.valueOf(dynamicStr.toUpperCase(Locale.ENGLISH)).getMidiVolume();                        
        }
    }
            
    /**
     * Sometimes after transformations are applied to a note, the scale step is
     * less than 0 or greater than the number of scale steps in an octave.  
     * This method "normalizes" the note--adjusts its scaleStep and octave so 
     * that it is the same note, but the scaleStep is between 0 and the number 
     * of scale steps.
     *      
     * @return the normalized note
     */
    public Note getNormalizedNote() {        
        Note tempNote = this.clone();
        if (tempNote.isRest()) return tempNote; // rests are always normalized!
        int numScaleStepsInOctave = this.getScale().getScaleStepArray().length; // cache it
        
        // put the note's scaleStep into the normal range (0..numScaleSteps - 1), adjusting the octaves.
        while(tempNote.getScaleStep() < 0) {
            tempNote.setScaleStep(tempNote.getScaleStep() + numScaleStepsInOctave);
            tempNote.setOctave(tempNote.getOctave() - 1);
        }                
        
        while(tempNote.getScaleStep() > numScaleStepsInOctave - 1) {
            tempNote.setScaleStep(tempNote.getScaleStep() - numScaleStepsInOctave);
            tempNote.setOctave(tempNote.getOctave() + 1);
        }
        
        assert tempNote.getScaleStep() >= 0 && tempNote.getScaleStep() < numScaleStepsInOctave : tempNote.getScaleStep();
        tempNote.setLetterNumber(MathHelper.getNormalizedValue(tempNote.getLetterNumber(), NoteName.NUM_LETTER_NAMES));
        
        return tempNote;
    }
    
    /**
     * Converts from whole notes to ticks, based on the midi tick resolution.
     * 
     * @param wholeNotes the number of whole notes
     * @param midiTickResolution the number of ticks per whole note
     * @return the number of ticks
     */
    private static long convertWholeNotesToTicks(Fraction wholeNotes, int midiTickResolution) {
        Fraction converted = wholeNotes.times(midiTickResolution);        
        
        // converting to midi ticks should result in an integral number of ticks
        // because our tick resolution should be chosen based on what will
        // produce this.  Test that this is in fact the case...
        assert converted.denominator() == 1 : converted;
     
        // since the denominator is 1, the converted value is equal to the numerator...
        return converted.numerator();
    }        
    
    /**
     * Gets the midi pitch number for this note, taking into account the scale
     * step, octave and chromatic adjustment.
     *      
     * @param keepExactPitch true to keep the exact pitch specified by the note
     *        parameters; false to allow the chromaticAdjustment to be changed
     *        if it would create a note that is already found in the scale
     * @return the midi pitch number
     */
    private int getMidiPitchNumber(boolean keepExactPitch) {
        if (this.isRest()) return 0;
                
        int[] scaleSteps = this.getScale().getScaleStepArray(); // cache it
        Note normalizedNote = this.getNormalizedNote();        
        int chromaticAdj = normalizedNote.getChromaticAdjustment();
        int chromaticAdjDecrementer = chromaticAdj > 0 ? 1 : -1;
        int testPitchNum;
        
        // Figure out the chromatic adjustment to use for the note.  We attempt to
        // use the given chromatic adjustment, but if it results in producing a pitch
        // that is already present in our scale, we reduce the chromatic adjustment
        // until we get a pitch not present in our scale or we reach 0.
        // We do this because of cases such as a phrase F# G in the key of C.
        // When our fractal algorithm transposes this and it becomes B# C, we
        // wind up with two of the same pitches in a row.  This loses the "shape"
        // that we are dealing with, so we adjust the accidental as necessary.        
        while (!keepExactPitch && Math.abs(chromaticAdj) > 0) {
            testPitchNum = (scaleSteps[normalizedNote.getScaleStep()] // the raw (natural) pitch number                    
                    + chromaticAdj                                    // the chromatic adjustment to test...                    
                    + Scale.NUM_CHROMATIC_PITCHES_PER_OCTAVE)         // necessary for notes like Cb, to prevent testPitchNum from being negative
                    % Scale.NUM_CHROMATIC_PITCHES_PER_OCTAVE;         // mod it, to put it in the range 0-12
            
            assert testPitchNum >= 0 && testPitchNum < Scale.NUM_CHROMATIC_PITCHES_PER_OCTAVE : testPitchNum;                        
            
            // stop decrementing our chromatic adjustment if our scale lacks this pitch...
            if (Arrays.binarySearch(scaleSteps, testPitchNum) < 0) break;
            
            // move the chromatic adjustment towards zero...
            chromaticAdj -= chromaticAdjDecrementer;
        }
        
        int pitchNum = scaleSteps[normalizedNote.getScaleStep()]       // half steps above tonic
                + chromaticAdj                                 // the note's chromatic adjustment
                + this.getSegmentChromaticAdjustment() // chromatic adjustment for the segment
                + this.getScale().getKeyName().getMidiPitchNumberAtOctave(normalizedNote.getOctave()); // take into account the octave
                
        // if the pitch number is outside of the allowed midi range, transpose it by some number of octaves
        // until it is valid...
        while (pitchNum < MidiNote.MIN_PITCH_NUM) pitchNum += Scale.NUM_CHROMATIC_PITCHES_PER_OCTAVE;
        while (pitchNum > MidiNote.MAX_PITCH_NUM) pitchNum -= Scale.NUM_CHROMATIC_PITCHES_PER_OCTAVE;
        return pitchNum;
    }
    
    /**
     * Converts the note to a Midi Note, that can then be used to get the
     * actual Midi note on and note off events.
     *      
     * @param startTime the time this note should be played
     * @param midiTickResolution the number of ticks per whole note for the
     *        midi sequence
     * @param channel the midi channel for this note, 0-15
     * @param keepExactPitch true to keep the exact pitch specified by the note
     *        parameters; false to allow the chromaticAdjustment to be changed
     *        if it would create a note that is already found in the scale
     * @return the MidiNote
     */
    public MidiNote convertToMidiNote(Fraction startTime, int midiTickResolution, int channel, boolean keepExactPitch) {        
        MidiNote midiNote = new MidiNote();       
            
        midiNote.setDuration(convertWholeNotesToTicks(this.getDuration(), midiTickResolution));
        midiNote.setStartTime(convertWholeNotesToTicks(startTime, midiTickResolution) + MidiNote.MIDI_SEQUENCE_START_SILENCE_TICK_OFFSET);
        midiNote.setVelocity(this.getVolume());        
        midiNote.setPitch(this.getMidiPitchNumber(keepExactPitch));
        midiNote.setChannel(channel);
        
        return midiNote;
    }      
    
    /**
     * Converts this Note to a Notation Note that can be used to produce GUIDO
     * or Lilypond notation.
     * 
     * @param partSection the Notation partSection
     * @param midiNote the midi note produced as output from this note
     * @param timeLeftInBar the amount of time that is left in the bar
     * @return the notation note
     */
    public NotationNote toNotationNote(PartSection partSection, MidiNote midiNote, Fraction timeLeftInBar) {
        if (this.isRest()) return NotationNote.createRest(partSection, duration, timeLeftInBar, this.isFirstNoteOfGermCopy);
        
        Note normalizedNote = this.getNormalizedNote();        
        
        // get the letter
        NoteName letterNoteName = this.getScale().getKeyName().getNaturalNoteNameForLetterNumber(normalizedNote.getLetterNumber());        
        char letter = letterNoteName.getLetter(true);
                
        // get the chromatic adjustment
        int chromAdjustment = (midiNote.getPitch() % Scale.NUM_CHROMATIC_PITCHES_PER_OCTAVE)  - letterNoteName.getNormalizedNoteNumber();                
        chromAdjustment = Scale.getNormalizedChromaticAdjustment(chromAdjustment);        

        if (Math.abs(chromAdjustment) > 2) {
            // lilypond does not support triple flats or sharps, so we have to
            // normalize this to a different letter name
            
            int letterNumberAdjustment = (chromAdjustment > 0 ? 1 : -1);
            normalizedNote.performTransformerAdjustment(0, letterNumberAdjustment, 0);
            return normalizedNote.toNotationNote(partSection, midiNote, timeLeftInBar);
        } else {
            // get the octave, taking into account the chromatic adjustment for notes like B# and Cb
            int notationOctave = ((midiNote.getPitch() - chromAdjustment) / Scale.NUM_CHROMATIC_PITCHES_PER_OCTAVE) - 1;

            return new NotationNote(partSection, letter, notationOctave, chromAdjustment, this.duration, timeLeftInBar, this.volume, this.isFirstNoteOfGermCopy);        
        }                        
    }
    
    @Override
    public String toString() {
        return String.format(
            "Note = LN(%d), SS(%d), O(%d), CA(%d), D(%s), V(%d), S(%s), SCA(%d), SVS(%s), IFN(%s)", 
            this.letterNumber, this.scaleStep, this.octave, this.chromaticAdjustment, 
            (this.duration == null ? "null" : this.duration.toString()), 
            this.volume, (this.scale == null ? "null": this.scale.toString()), 
            this.segmentChromaticAdjustment,
            (this.sourceVoiceSection == null ? "null" : this.sourceVoiceSection.toString()),
            this.isFirstNoteOfGermCopy);
    }

    @Override
    public Note clone() {
        try {
            return (Note) super.clone();
        } catch (CloneNotSupportedException ex) {
            // We have implemented the Cloneable interface, so we should never
            // get this exception.  If we do, there's something very, very wrong...
            throw new UndeclaredThrowableException(ex, "Unexpected error while cloning.  This indicates a programming or JVM error.");
        }         
    }

    // equals() and hashCode() were auto-generated by the Netbeans IDE
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Note other = (Note) obj;
        if (this.scaleStep != other.scaleStep) {
            return false;
        }
        if (this.octave != other.octave) {
            return false;
        }
        if (this.chromaticAdjustment != other.chromaticAdjustment) {
            return false;
        }
        if (this.duration != other.duration && (this.duration == null || !this.duration.equals(other.duration))) {
            return false;
        }
        if (this.volume != other.volume) {
            return false;
        }
        if (this.scale != other.scale && (this.scale == null || !this.scale.equals(other.scale))) {
            return false;
        }
        if (this.segmentChromaticAdjustment != other.segmentChromaticAdjustment) {
            return false;
        }
        if (this.letterNumber != other.letterNumber) {
            return false;
        }
        if (this.sourceVoiceSection != other.sourceVoiceSection && (this.sourceVoiceSection == null || !this.sourceVoiceSection.equals(other.sourceVoiceSection))) {
            return false;
        }
        if (this.isFirstNoteOfGermCopy != other.isFirstNoteOfGermCopy) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + this.scaleStep;
        hash = 41 * hash + this.octave;
        hash = 41 * hash + this.chromaticAdjustment;
        hash = 41 * hash + (this.duration != null ? this.duration.hashCode() : 0);
        hash = 41 * hash + this.volume;
        hash = 41 * hash + (this.scale != null ? this.scale.hashCode() : 0);
        hash = 41 * hash + this.segmentChromaticAdjustment;
        hash = 41 * hash + this.letterNumber;
        hash = 41 * hash + (this.sourceVoiceSection != null ? this.sourceVoiceSection.hashCode() : 0);
        hash = 41 * hash + (this.isFirstNoteOfGermCopy ? 1 : 0);
        return hash;
    }       
                             
}
