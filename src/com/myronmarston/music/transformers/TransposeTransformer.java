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

/**
 * Transformer that transposes the notes of the NoteList to a different pitch
 * level.  This does not imply a key change as the term "transpose" is normally
 * used in music theory.  Rather, each note in the given NoteList is moved the
 * same number of scale steps to a different pitch level.  
 * Example: G5 A5 B5 G5 -> A5 B5 C6 A5
 * 
 * @author Myron
 */
public class TransposeTransformer implements Transformer {   
    private final int transposeSteps;
    private final int transposeLetterNumbers;           
               
    /**
     * Gets the number of scale steps to transpose the given NoteList.
     * 
     * @return the transpose steps
     */
    public int getTransposeSteps() {
        return transposeSteps;
    }

    /**
     * Gets the number of letter numbers to transpose the given NoteList.
     * 
     * @return the transpose letter number
     */
    public int getTransposeLetterNumbers() {
        return transposeLetterNumbers;
    }   
    
    /**
     * Constructor.
     * 
     * @param transposeSteps the number of scale steps to transpose the given NoteList
     * @param transposeLetterNames the number of letter 
     */
    public TransposeTransformer(int transposeSteps, int transposeLetterNames) {
        this.transposeSteps = transposeSteps;
        this.transposeLetterNumbers = transposeLetterNames;
    }  
    
    public NoteList transform(NoteList input) {
        Note newNote;
        NoteList output = new NoteList(input.size());
        
        for (Note inputNote : input) {
            newNote = inputNote.clone();
            
            if (!newNote.isRest()) { // don't change a rest...
                newNote.performTransformerAdjustment(this.transposeSteps, this.transposeLetterNumbers, 0);
            }            
            
            output.add(newNote);
        }
        
        return output;
    }
}
