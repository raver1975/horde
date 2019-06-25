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

import com.myronmarston.music.NoteName;
import org.simpleframework.xml.*;

import java.util.Arrays;

/**
 * A Mixolydian mode scale: WWHWWHW.
 * 
 * @author Myron
 */
@Root
public class MixolydianScale extends AbstractDiatonicScale {
    private final static int[] SCALE_STEPS = new int[] {0, 2, 4, 5, 7, 9, 10};
    
    /**
     * Constructor.
     * 
     * @param keyName the name of the tonal center
     * @throws com.myronmarston.music.scales.InvalidKeySignatureException thrown
     *         when the key is invalid
     */
    public MixolydianScale(NoteName keyName) throws InvalidKeySignatureException {        
        super(new KeySignature(Tonality.Mixolydian, keyName));        
    }    
    
    /**
     * Provided to allow xml deserialization.
     * 
     * @throws com.myronmarston.music.scales.InvalidKeySignatureException thrown
     *         when the key is invalid.
     */
    public MixolydianScale() throws InvalidKeySignatureException {
        this(Tonality.Mixolydian.getDefaultKey());
    }

    @Override
    public int[] getScaleStepArray() {
        return Arrays.copyOf(SCALE_STEPS, SCALE_STEPS.length);
    }       
}
