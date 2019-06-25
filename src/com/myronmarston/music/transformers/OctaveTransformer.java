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
 * Transforms the given NoteList by moving it up or down some number of octaves.
 * Example: C5 G5 D5 C5 -> C3 G3 D3 C3
 * 
 * @author Myron
 */
public class OctaveTransformer implements Transformer {
    private final int octaveChange;
    
    /**
     * Constructor.
     * 
     * @param octaveChange the number of octaves to transform
     */
    public OctaveTransformer(int octaveChange) {
        this.octaveChange = octaveChange;
    }
    
    /**
     * Gets the number of octaves to move the given NoteList.  Positive values 
     * move it up; negative values more it down.
     * 
     * @return the number of octaves to transform
     */
    public int getOctaveChange() {
        return octaveChange;
    }
        
    public NoteList transform(NoteList input) {
        Note newNote;
        NoteList output = new NoteList(input.size());
        
        for (Note inputNote : input) {
            newNote = inputNote.clone();
                        
            if (!newNote.isRest()) { // don't change the octave on a rest...
                newNote.setOctave(newNote.getOctave() + this.octaveChange);
            }                        
            
            output.add(newNote);
        }
        
        return output;
    }
}
