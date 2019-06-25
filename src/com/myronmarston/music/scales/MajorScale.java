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
 * The most common scale used in western music.  This scale uses the following
 * whole step (W) / half step (H): WWHWWWH
 * 
 * @author Myron
 */
@Root
public class MajorScale extends AbstractDiatonicScale {     
    private final static int[] SCALE_STEPS = new int[] {0, 2, 4, 5, 7, 9, 11};    
    
    /**
     * Constructor.
     * 
     * @param keyName the name of the tonal center
     * @throws com.myronmarston.music.scales.InvalidKeySignatureException thrown
     *         when the key is invalid
     */
    public MajorScale(NoteName keyName) throws InvalidKeySignatureException {        
        super(new KeySignature(Tonality.Major, keyName));        
    }
    
    /**
     * Provided to allow xml deserialization.
     * 
     * @throws com.myronmarston.music.scales.InvalidKeySignatureException thrown
     *         when the key is invalid.
     */
    public MajorScale() throws InvalidKeySignatureException {
        this(Tonality.Major.getDefaultKey());
    }

    @Override
    public int[] getScaleStepArray() {
        return Arrays.copyOf(SCALE_STEPS, SCALE_STEPS.length);
    }    
}
