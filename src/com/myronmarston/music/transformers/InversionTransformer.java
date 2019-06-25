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
 * Transformer that applies inversion to the given note list.
 * Example: C5 G5 D5 C5 -> C5 F4 B4 C5
 * 
 * @author Myron
 */
public class InversionTransformer implements Transformer {
        
    public NoteList transform(NoteList input) {
        Note newNote;
        Note firstNote = input.getFirstAudibleNote();
        NoteList output = new NoteList(input.size());
        
        for (Note inputNote : input) {
            newNote = inputNote.clone();
            
            if (!newNote.isRest()) {
                
                // adjust the scale step, letter number and octave.
                // we double the values because we need to return to the first note
                // and then go that distance past it--which is the same as double the distance
                newNote.performTransformerAdjustment(
                    2 * (firstNote.getScaleStep() - newNote.getScaleStep()), 
                    2 * (firstNote.getLetterNumber() - newNote.getLetterNumber()), 
                    2 * (firstNote.getOctave() - newNote.getOctave()));

                // invert the chromatic adjustment...
                newNote.setChromaticAdjustment(newNote.getChromaticAdjustment() * -1);
                
                // invert the segment chromatic adjustment...
                newNote.setSegmentChromaticAdjustment(newNote.getSegmentChromaticAdjustment() * -1);
            }
            
            output.add(newNote);
        }
        
        return output;
    }
}
