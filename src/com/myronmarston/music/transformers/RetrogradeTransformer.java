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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Transformer that applies retrograde to the given NoteList.
 * Example: C5 G5 D5 C5 -> C5 D5 G5 C5
 * 
 * @author Myron
 */
public class RetrogradeTransformer implements Transformer {
        
    public NoteList transform(NoteList input) {
        // make a copy to reverse...
        NoteList copy = input.clone();
        
        // reverse the copy, rather than the original input...
        Collections.reverse(copy);        

        // set our first germ note properly.
        // we might have multiple copies of the germ, so we have to iterate over
        // the list and fix it
        int listSize = copy.size();
        List<Note> notesThatShouldBeFirstInGermCopy = new ArrayList<Note>(copy.size());
        for (int i = 0; i < listSize; i++) {
            Note note = copy.get(i);
            
            if (note.isFirstNoteOfGermCopy()) {
                // if this note was the first note of a germ copy, it is now the
                // last note of a germ copy, and that means the next note will
                // be the first note of a germ copy, or, in the case of the last
                // note of the entire list, using mod listSize gives us the
                // first note of the entire list
                notesThatShouldBeFirstInGermCopy.add(copy.get((i + 1) % listSize));                
            }
            
            // set all notes to false; we will set the ones that should be true
            // below based on our list
            note.setIsFirstNoteOfGermCopy(false);
        }
        
        for (Note note : notesThatShouldBeFirstInGermCopy) note.setIsFirstNoteOfGermCopy(true);
        
        return copy;
    }
}
