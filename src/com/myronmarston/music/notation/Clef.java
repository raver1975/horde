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
import com.myronmarston.util.Fraction;
import com.myronmarston.util.MathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enumerates the clef possibilities.
 * 
 * @author Myron
 */
public enum Clef implements NotationElement {
    /**
     * Treble clef, transposed two octaves higher.
     */
    TREBLE_15VA("g2+15", "treble^15", 'b', 6), // middleLine = 48
    
    /**
     * Treble clef, transposed one octave higher.
     */
    TREBLE_8VA("g2+8", "treble^8", 'b', 5),   // middleLine = 41
    
    /**
     * Treble clef.
     */
    TREBLE("g2", "treble", 'b', 4),         // middleLine = 34    
    
    /**
     * Treble clef, transposed one octave lower.
     */
    //TODO: this doesn't seem to work with the guido note server
    TREBLE_8VB("g2-8", "treble_8", 'b', 3),   // middleLine = 27    
    
    /**
     * Bass clef.
     */
    BASS("f4", "bass", 'd', 3),             // middleLine = 22
    
    /**
     * Bass clef, transposed one octave lower.
     */
    BASS_8VB("f4-8", "bass_8", 'd', 2),       // middleLine = 15
    
    /**
     * Bass clef, transposed two octaves lower.
     */
    BASS_15VB("f4-15", "bass_15", 'd', 1);     // middleLine = 8
  
    private final int middleLineNoteNumber;    
    private final String lilypondString;    
    private final String guidoString;
    
    /**
     * Constructor.
     * 
     * @param guidoName the name of the clef in guido notation
     * @param lilypondName the name of the clef in lilypond notation
     * @param middleLineLetterName the letter on the middle line of this clef
     * @param middleLineOctaveNumber the octave of the middle line note
     */
    private Clef(String guidoName, String lilypondName, char middleLineLetterName, int middleLineOctaveNumber) {
        NotationNote nn = new NotationNote(null, middleLineLetterName, middleLineOctaveNumber, 0, new Fraction(1, 4), new Fraction(4, 4), MidiNote.DEFAULT_VELOCITY, false);
        this.middleLineNoteNumber = nn.getNotationStaffNoteNumber();        
        this.guidoString = "\\clef<\"" + guidoName + "\">";
        this.lilypondString = "\\clef \"" + lilypondName + "\"";
    }
    
    /**
     * Throws an unsupported operation exception.
     * 
     * @return throws an exception
     * @throws UnsupportedOperationException always
     */
    public long getLargestDurationDenominator() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("This method is not supported on clefs.");
    }

    /**
     * Returns an empty list.
     *
     * @return an empty list
     */
    public List<NotationNote> getNotationNotes() {
        return Collections.emptyList();
    }

    /**
     * Throws an unsupported operation exception.
     *
     * @param scaleFactor ignored
     * @throws UnsupportedOperationException always
     */    
    public void scaleDurations(long scaleFactor) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("This method is not supported on clefs.");
    }

    /**
     * Returns false as this is not supported on a clef.
     * 
     * @return false
     */
    public boolean supportsDurationScaling() {
        return false;
    }
        
    /**
     * The representation of this clef in Lilypond notation.
     * 
     * @return the lilypond string
     */
    public String toLilypondString() {
        return lilypondString;
    }        
    
    /**
     * The representation of this clef in Guido notation.
     * 
     * @return the guido string
     */
    public String toGuidoString() {
        return guidoString;
    }
    
    /**
     * Gets the clef that is the best match for the given of notes.  The "best
     * match" algorithm tries to reduce the number of ledger lines needed.
     * 
     * @param notes the list of notation notes
     * @return the clef that is the best match for this list
     */
    public static Clef getBestMatchForNoteList(List<NotationNote> notes) {
        int medianNoteNumber = getMedianNoteNumber(notes);
        return getClosestClefToNoteNumber(medianNoteNumber);
    }
    
    /**
     * Gets the median notation staff note number for the given list of
     * notation notes.
     * 
     * @param notes list of notation notes
     * @return median note number
     */
    protected static int getMedianNoteNumber(List<NotationNote> notes) {
        List<Integer> notationStaffNoteNumbers = new ArrayList<Integer>();
        for (NotationNote note : notes) {
            if (!note.isRest()) notationStaffNoteNumbers.add(note.getNotationStaffNoteNumber());
        }            
        return MathHelper.getMedianValue(notationStaffNoteNumbers);        
    }
    
    /**
     * Gets the clef whose middle line is the shortest distance from the 
     * given note number.
     * 
     * @param noteNumber the note number
     * @return the clef that is closest to this note number
     */    
    protected static Clef getClosestClefToNoteNumber(int noteNumber) {
        int numOfClefs = Clef.values().length;
        Map<Integer, Clef> clefs = new HashMap<Integer, Clef>(numOfClefs);        
        List<Integer> distances = new ArrayList<Integer>(numOfClefs);
        int distance;
        
        for (Clef clef : Clef.values()) {            
            distance = Math.abs(noteNumber - clef.middleLineNoteNumber);
            distances.add(distance);
            clefs.put(distance, clef);            
        }        
        
        Collections.sort(distances);
        int shortestDistance = distances.get(0);
        
        // we should never have two clefs that are equally distant...
        assert shortestDistance < distances.get(1);
        
        return clefs.get(shortestDistance);
    }
        
}
