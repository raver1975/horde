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

import com.myronmarston.music.NoteList;

/**
 * Implementations of this interface are used to take a NoteList and transform 
 * it into a new NoteList, using some kind of musical operation.  These will be 
 * combined in various ways to generate the fractal music.
 * 
 * @author Myron
 */
public interface Transformer {
    
    /**
     * Transforms the given NoteList to a new note list.
     * 
     * @param input the NoteList to transform
     * @return the transformed NoteList
     */
    NoteList transform(NoteList input);
}
