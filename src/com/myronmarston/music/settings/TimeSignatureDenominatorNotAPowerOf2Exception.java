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

package com.myronmarston.music.settings;

/**
 * Exception that is thrown when the time signature denominator is not a power 
 * of 2.
 * 
 * @author Myron
 */
public class TimeSignatureDenominatorNotAPowerOf2Exception extends InvalidTimeSignatureException {
    // Used to serialize the class.  Change this if the class has a change significant enough to change the way the class is serialized.
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * 
     * @param denominator the denominator
     */
    public TimeSignatureDenominatorNotAPowerOf2Exception(int denominator) {
        super(String.format("The given time signature denominator (%d) is invalid.  It must be a power of 2.", denominator));
    }        
}
