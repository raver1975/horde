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

package com.myronmarston.music.scales;

import java.util.Arrays;

/**
 * Abstract class representing a diatonic scale--a scale with 7 pitches, one
 * for each letter.
 * 
 * @author Myron
 */
public abstract class AbstractDiatonicScale extends Scale {
    private final static int[] LETTER_NUMBERS = new int[] {0, 1, 2, 3, 4, 5, 6};
    
    /**
     * Constructor.
     * 
     * @param keySignature the key signature
     */
    public AbstractDiatonicScale(KeySignature keySignature) {
        super(keySignature);
    }
    
    @Override
    public int[] getLetterNumberArray() {
        return Arrays.copyOf(LETTER_NUMBERS, LETTER_NUMBERS.length);        
    } 
}
