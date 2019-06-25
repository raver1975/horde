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

import com.myronmarston.util.Fraction;
import com.myronmarston.util.MathHelper;

import java.util.List;

/**
 * Represents a tuplet (e.g. triplet, quintuplet, etc) using a tuplet multiplier
 * and a list of notes within the tuplet.  This models the way the Lilypond
 * format works.
 * 
 * @author Myron
 */
public class Tuplet extends AbstractNotationElement {
    private final Fraction tupletMultiplier;
    private final NotationElementList notes;
    private String lilypondString;

    /**
     * Constructor.
     * 
     * @param notes the notes to put into this tuplet
     */
    public Tuplet(List<NotationElement> notes) {
        this.notes = new NotationElementList(notes);        
        this.tupletMultiplier = this.calculateTupletMultiplier();
        this.updateNoteDurations();
    }
    
    /**
     * Updates each note's duration based on the tuplet multiplier.  This method 
     * should only be called in the constructor after the tuplet multiplier has been set.     
     */
    private void updateNoteDurations() {
        assert tupletMultiplier != null : "This method should only be called after tupletMultiplier has a value.";
        NotationElement element;
        NotationNote note;
        
        for (int i = 0; i < this.notes.size(); i++) {
            element = this.notes.get(i);
            if (element instanceof NotationNote) {
                note = (NotationNote) element;                                
                this.notes.set(i, note.applyTupletMultiplier(tupletMultiplier));
            }
        }        
    }
    
    /**
     * Calculates the tuplet multiplier.
     * 
     * @return the tuplet multiplier
     */
    private Fraction calculateTupletMultiplier() {        
        NotationElementList clonedList = new NotationElementList(this.notes.clone());
        
        // ignore notes that already have a power of 2 denominator and any nested
        // tuplets that add up to a power of two on their own
        clonedList.removeConsecutiveNotesWhoseDenomsAddToPowerOf2();
        
        long smallestDenom = clonedList.getSmallestNoteDurationDenominator();
        long nextSmallestPowerOf2 = MathHelper.getLargestPowerOf2LessThanGivenNumber(smallestDenom);
        
        return new Fraction(nextSmallestPowerOf2, smallestDenom);
    }

    /**
     * Gets the tuplet multiplier.
     * 
     * @return the tuplet multiplier
     */
    public Fraction getTupletMultiplier() {
        return tupletMultiplier;
    }        
    
    /**
     * Gets the notes included in this tuplet.
     * 
     * @return the notes included in this tuplet
     */
    public NotationElementList getNotes() {
        return notes;
    }
            
    /**
     * Constructs the lilypond string for this tuplet.
     * 
     * @return the lilypond string
     */
    public String toLilypondString() {
        if (lilypondString == null) {
            this.getNotes().setElementSeperator(" ");
            lilypondString = "\\times " + tupletMultiplier.toString() + " { " + notes.toLilypondString() + " }";
        }
        return lilypondString;
    }

    /**
     * Tuplet is not supported for guido notation.
     * 
     * @return an UnsupportedOperationException is thrown
     * @throws UnsupportedOperationException always thrown
     */
    public String toGuidoString() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Tuplet is only needed for Lilypond notation.  There is no equivalent in Guido notation.");
    }

    /**
     * Returns true to indicate that this element supports duraiton scaling.
     * 
     * @return true
     */
    public boolean supportsDurationScaling() {
        return this.getNotes().supportsDurationScaling();
    }

    @Override
    public long getLargestDurationDenominator() {
        return this.getNotes().getLargestDurationDenominator();
    }

    @Override
    public void scaleDurations(long scaleFactor) {
        assert this.getNotes().supportsDurationScaling();
        this.getNotes().scaleDurations(scaleFactor);
    }
    
    @Override
    public List<NotationNote> getNotationNotes() {
        return this.getNotes().getNotationNotes();
    }
}
