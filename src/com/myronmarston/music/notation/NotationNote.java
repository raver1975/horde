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

package com.myronmarston.music.notation;

import com.myronmarston.music.MidiNote;
import com.myronmarston.music.NoteName;
import com.myronmarston.util.Fraction;
import com.myronmarston.util.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents one note of music notation.
 * 
 * @author Myron
 */
public class NotationNote extends AbstractNotationElement {    
    
    /**
     * The character used for a flat in lilypond notation.
     */
    public static final char LILYPOND_FLAT_CHAR = 'f';
    
    /**
     * The character used for a flat in Guido notation.
     */
    public static final char GUIDO_FLAT_CHAR = '&';
    
    /**
     * The character used for a sharp in lilypond notation.
     */
    public static final char LILYPOND_SHARP_CHAR = 's';    
    
    /**
     * The character used for a sharp in Guido notation.
     */
    public static final char GUIDO_SHARP_CHAR = '#';
    
    /**
     * The character used to increase the octave in lilypond notation.
     */
    private static final char LILYPOND_POSITIVE_OCTAVE_CHAR = '\'';
    
    /**
     * The character used to decrease the octave in lilypond notation.
     */
    private static final char LILYPOND_NEGATIVE_OCTAVE_CHAR = ',';     
    
    /**
     * The symbol used to indicate a rest in lilypond notation.
     */
    private static final String LILYPOND_REST = "r";
    
    /**
     * The symbol used to indicate a rest in guido notation.
     */
    private static final String GUIDO_REST = "_";
    
    /**
     * A string that can be used as a note placeholder in a java format string.
     */
    public static final String NOTE_PLACEHOLDER = "%1$s";      
    
    /**
     * A string that can be used as a second note placeholder in a java format
     * string.
     */
    public static final String NOTE_PLACEHOLDER_2 = "%2$s";                  
        
    private final PartSection partSection;
    private final boolean rest;
    private final char letterName;
    private final int octave;
    private final int accidental;
    private final int volume;
    private final Fraction tupletMultiplier;
    private boolean isFirstNoteInGermCopy;
    private Fraction timeLeftInBar;
    private NotationDynamic dynamic;    
    private Fraction duration;            
    
    /**
     * Constructor.  Creates a rest.  
     * 
     * @param partSection the partSection that owns this notation note
     * @param duration the duration of this notation note
     * @param timeLeftInBar the amount of time left in the bar
     * @param isFirstNoteInGermCopy whether or not this is the first note of a 
     *        copy of the germ
     */
    private NotationNote(PartSection partSection, Fraction duration, Fraction timeLeftInBar, boolean isFirstNoteInGermCopy) {                       
        this(partSection, 'r', 0, 0, duration, timeLeftInBar, new Fraction(1, 1), 0, isFirstNoteInGermCopy, null);        
    }
    
    /**
     * Constructor.  Creates a notation note having the same values as the
     * given notation note, using the tuplet multiplier to scale the duration.
     * 
     * @param note the notation note from which to copy values
     * @param tupletMultiplier the tuplet multiplier for this notation note
     */
    private NotationNote(NotationNote note, Fraction tupletMultiplier) {
        this(note.partSection, note.letterName, note.octave, note.accidental, note.duration.dividedBy(tupletMultiplier), note.timeLeftInBar, tupletMultiplier, note.volume, note.isFirstNoteInGermCopy, note.dynamic);        
    }    
    
    /**
     * Constructor.
     * 
     * @param partSection the partSection that owns this notation note
     * @param letterName the letter name for this notation note (a-g)
     * @param octave the octave for this notation note
     * @param accidental the accidental for this notation note; positive 
     *        indicates a number of sharps and negative indicates a number of
     *        flats
     * @param duration the rhythmic duration of this notation note
     * @param timeLeftInBar the amount of time left in the current bar
     * @param volume the raw midi volume of the note
     * @param isFirstNoteInGermCopy whether or not this is the first note in a 
     *        copy of the germ
     * @throws IllegalArgumentException if the accidental outside of the range
     *         -2 to 2, the letterName is invalid, the duration is negative, or 
     *         the volume is not in the allowed range
     */
    public NotationNote(PartSection partSection, char letterName, int octave, int accidental, Fraction duration, Fraction timeLeftInBar, int volume, boolean isFirstNoteInGermCopy) throws IllegalArgumentException {   
        this(partSection, letterName, octave, accidental, duration, timeLeftInBar, new Fraction(1, 1), volume, isFirstNoteInGermCopy, null);
    }
    
    private NotationNote(PartSection partSection, char letterName, int octave, int accidental, Fraction duration, Fraction timeLeftInBar, Fraction tupletMultiplier, int volume, boolean isFirstNoteInGermCopy, NotationDynamic dynamic) throws IllegalArgumentException {   
        if (Math.abs(accidental) > 2) throw new IllegalArgumentException("The accidental is outside the allowable range.");
        if (letterName != 'r' && (letterName < 'a' || letterName > 'g')) throw new IllegalArgumentException("The letterName is outside the allowable range.");
        if (duration.asDouble() <= 0) throw new IllegalArgumentException("The duration must be positive.");        
        if (volume < MidiNote.MIN_VELOCITY || volume > MidiNote.MAX_VELOCITY) throw new IllegalArgumentException("The volume is not in the allowed range.");

        this.partSection = partSection;
        this.letterName = letterName;
        this.octave = octave;
        this.accidental = accidental;
        this.duration = duration;
        this.timeLeftInBar = timeLeftInBar;
        this.rest = (letterName == 'r');
        this.tupletMultiplier = tupletMultiplier;
        this.volume = volume;
        this.dynamic = dynamic; 
        this.isFirstNoteInGermCopy = isFirstNoteInGermCopy;
    }
    
    /**
     * Creates a rest.
     * 
     * @param partSection the partSection that will own the rest
     * @param duration the duration of the rest
     * @param timeLeftInBar the amount of time left in the bar
     * @param isFirstNoteInGermCopy whether or not this is the first note in 
     *        a series of notes that makes a germ copy
     * @return the rest
     */
    static public NotationNote createRest(PartSection partSection, Fraction duration, Fraction timeLeftInBar, boolean isFirstNoteInGermCopy) {
        return new NotationNote(partSection, duration, timeLeftInBar, isFirstNoteInGermCopy);
    }    

    /**
     * Gets the part section that owns this notation note.
     * 
     * @return the part section
     */
    public PartSection getPartSection() {
        return partSection;
    }        

    /**
     * Gets the accidental.  Negative indicates flats and positive indicates
     * sharps.
     * 
     * @return the accidental
     */
    public int getAccidental() {
        return accidental;
    }

    /**
     * Gets the rhythmic duration of this notation note. 
     * 
     * @return the rhythmic duration
     */
    public Fraction getDuration() {
        return duration;
    }

    /**
     * Gets the letter name of this notation note (a-g).
     * 
     * @return the letter name of this notation note
     */
    public char getLetterName() {
        return letterName;
    }

    /**
     * Gets the octave number of this notation note.
     * 
     * @return the octave number
     */
    public int getOctave() {
        return octave;
    }

    /**
     * Gets the amount of time left in this bar.  This is needed to be able
     * to split a long rest across a bar lines.
     * 
     * @return the amount of time tha this is left in the bar
     */
    public Fraction getTimeLeftInBar() {
        return timeLeftInBar;
    }

    /**
     * Gets the tuplet multiplier that has been applied to this note.
     * 
     * @return the tuplet multiplier
     */
    public Fraction getTupletMultiplier() {
        return tupletMultiplier;
    }

    /**
     * Gets the raw midi volume of this note.
     * 
     * @return the raw midi volume of this note
     */
    public int getVolume() {
        return volume;
    }

    /**
     * Indicates whether or not this is the first note of a germ copy.
     * 
     * @return true if this is the first note of a germ copy
     */
    public boolean isFirstNoteInGermCopy() {
        return isFirstNoteInGermCopy;
    }
        
    /**
     * Gets the notation dynamic for this notation note.  If none has been set,
     * the default empty dynamic will be returned.
     * 
     * @return the dynamic
     */
    public NotationDynamic getDynamic() {
        if (dynamic == null) dynamic = NotationDynamic.DEFAULT_EMPTY;
        return dynamic;
    }

    /**
     * Sets the notation dynamic for this notation note.
     * 
     * @param dynamic the dynamic
     */
    public void setDynamic(NotationDynamic dynamic) {
        this.dynamic = dynamic;
    }        
                
    /**
     * Checks to see if this notation note is a rest.
     * 
     * @return true if this notation note is a rest
     */
    public boolean isRest() {
        return rest;
    }        
      
    /**
     * Gets a string representing the octave in Lilypond notation.
     * 
     * @return the lilypond octave string
     */
    protected String getLilypondOctaveString() {
        // In lilypond, octave 3 is treated as the central octave with no 
        // special mark.
        int adjustedOct = this.octave - 3;        
        return createCharCopyString(adjustedOct, LILYPOND_NEGATIVE_OCTAVE_CHAR, LILYPOND_POSITIVE_OCTAVE_CHAR);        
    }        
    
    /**
     * Gets the octave number used for guido notation.
     * 
     * @return the octave number used by guido notation
     */
    protected int getGuidoOctave() {
        return this.getOctave() - 3;
    }
    
    /**
     * Gets a string representing the accidental in lilypond notation.
     * 
     * @return the lilypond accidental string
     */
    protected String getLilypondAccidentalString() {
        return createCharCopyString(this.accidental, LILYPOND_FLAT_CHAR, LILYPOND_SHARP_CHAR); 
    }
    
    /**
     * Gets a string representing the accidental in guido notation.
     * 
     * @return the guido accidental string
     */
    protected String getGuidoAccidentalString() {
        return createCharCopyString(this.accidental, GUIDO_FLAT_CHAR, GUIDO_SHARP_CHAR); 
    }
    
    /**
     * Creates a string containing copies of the given negChar or posChar based
     * on the value of count.
     * 
     * @param count number of characters; if positive, posChar will be used; if
     *        negative, negChar will be used
     * @param negChar the character to use if count is negative
     * @param posChar the character to use if count is positive
     * @return a string containing copies of the appropriate characters
     */
    private static String createCharCopyString(int count, char negChar, char posChar) {
        if (count == 0) return "";        
        char[] chars = new char[Math.abs(count)];
        Arrays.fill(chars, (count < 0 ? negChar : posChar));
        return String.copyValueOf(chars);        
    }    

    /**
     * Gets the representation of this note in lilypond notation.
     * 
     * @return the representation of this note in lilypond notation
     */
    public String toLilypondString() {        
        if (this.duration.denomIsPowerOf2()) {
            String pitchInfo = (
                this.rest ? LILYPOND_REST : 
                this.getLetterName() + this.getLilypondAccidentalString() + this.getLilypondOctaveString());

            String durationFormatString = this.getDuration().toLilypondString(this.getTimeLeftInBar(), this.getPartSection().getPart().getPiece().getTimeSignatureFraction(), this.getTupletMultiplier());
            return String.format(durationFormatString, pitchInfo, this.getDynamic().toLilypondString());
        } else {
            Tuplet tuplet = new Tuplet(Arrays.asList((NotationElement)this));
            return tuplet.toLilypondString();
        }  
    }

    /**
     * Gets the representation of this note in GUIDO notation.
     * 
     * @return the representation of this note in GUIDO notation
     */
    public String toGuidoString() {        
        String pitchInfo = (
            this.rest ? GUIDO_REST : 
            this.getLetterName() + this.getGuidoAccidentalString() + this.getGuidoOctave());

        String noteWithoutDynamic = pitchInfo + this.getDuration().toGuidoString();        
        return String.format(this.getDynamic().toGuidoString(), noteWithoutDynamic);
    }
            
    /**
     * Creates a new notation note with a duration that is scaled by the 
     * given multiplier, which can be used when grouping tuplets.
     * 
     * @param multiplier the tuplet multiplier
     * @return a new notation note
     * @throws IllegalArgumentException if the multiplier is note
     *         positive
     */
    public NotationNote applyTupletMultiplier(Fraction multiplier) throws IllegalArgumentException {
        return new NotationNote(this, multiplier);        
    }

    /**
     * Gets the denominator of the duration.
     * 
     * @return the duration denominator
     */
    @Override
    public long getLargestDurationDenominator() {
        return this.getDuration().denominator();
    }

    /**
     * Scales the durations and timeTimeLeftInBar by the given scale factor.
     * 
     * @param scaleFactor the factor to use to scale the durations
     */
    @Override
    public void scaleDurations(long scaleFactor) {        
        assert MathHelper.numIsPowerOf2(scaleFactor) : scaleFactor;
        this.duration = this.duration.times(scaleFactor);
        this.timeLeftInBar = this.timeLeftInBar.times(scaleFactor);
    }        
    
    /**
     * Indicates that duration scaling is supported by this element.
     * 
     * @return true
     */
    public boolean supportsDurationScaling() {
        return true;
    }

    /**
     * Returns a list containing one item: this notation note.
     * 
     * @return list of notation notes
     */
    @Override
    public List<NotationNote> getNotationNotes() {
        return new ArrayList<NotationNote>(Arrays.asList(this));
    }
            
    /**
     * Gets a number indicating the line or space on the staff used by this       
     * notation note.  C0 is 0, and each successive letter is one more.          
     * 
     * @return the notation staff note number
     * @throws UnsupportedOperationException if this note is a rest
     */
    public int getNotationStaffNoteNumber() throws UnsupportedOperationException {
        if (this.isRest()) throw new UnsupportedOperationException("This is a rest, so there is no notation staff note number.");
        assert this.letterName >= 'a' && this.letterName <= 'g' : this.letterName;
        int letterNumber = MathHelper.getNormalizedValue(Character.getNumericValue(this.letterName) - Character.getNumericValue('a') - 2, NoteName.NUM_LETTER_NAMES);
        return letterNumber + (this.getOctave() * NoteName.NUM_LETTER_NAMES);
    }
}
