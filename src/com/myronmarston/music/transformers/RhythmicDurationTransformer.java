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

package com.myronmarston.music.transformers;

import com.myronmarston.music.Note;
import com.myronmarston.music.NoteList;
import com.myronmarston.util.Fraction;

/**
 * Transformer that scales the the rhythmic duration of the notes by some scale
 * factor.
 * Example: 1/4 1/8 1/4 -> 1/2 1/4 1/2
 * 
 * @author Myron
 */
public class RhythmicDurationTransformer implements Transformer {  
    private final Fraction scaleFactor;
    
    /**
     * Gets the scale factor.  Factors less than 1 will increase the speed of
     * the NoteList; factors greater than 1 will slow it down.
     * 
     * @return the scale factor
     */
    public Fraction getScaleFactor() {        
        return scaleFactor;
    }    
    /**
     * Constructor.
     * 
     * @param scaleFactor the scale factor. Factors greater than 1 will increase 
     *        the speed of the NoteList; factors less than 1 will slow it 
     *        down.
     */
    public RhythmicDurationTransformer(Fraction scaleFactor) {        
        checkScaleFactorValidity(scaleFactor);
        this.scaleFactor = scaleFactor;
    }
    
    /**
     * Checks if the given scale factor is valid, throwing an exception if it
     * is not valid.
     * 
     * @param scaleFactor the scale factor to check
     * @throws IllegalArgumentException if the scale factor is invalid
     */
    public static void checkScaleFactorValidity(Fraction scaleFactor) throws IllegalArgumentException {
        if (scaleFactor.compareTo(0) <= 0) {
            throw new IllegalArgumentException("The scale factor must be greater than 0.");
        }            
    }
    
     public NoteList transform(NoteList input) {
        Note newNote;
        NoteList output = new NoteList(input.size());
        
        for (Note inputNote : input) {
            newNote = inputNote.clone();
            
            // if the scale factor is zero, we'll get a div-by-zero exception.  
            // our code should prevent it from ever reaching here if it's zero...
            assert this.scaleFactor.asDouble() != 0d : this.scaleFactor;
            newNote.setDuration(newNote.getDuration().dividedBy(this.scaleFactor));            
            
            output.add(newNote);
        }
        
        return output;
    }
}
