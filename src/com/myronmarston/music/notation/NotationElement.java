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

package com.myronmarston.music.notation;

import java.util.List;

/**
 * Interface that represents an element of musical notation.
 * 
 * @author Myron
 */
public interface NotationElement {    
    
    /**
     * Gets the representation of this element in the string format used by
     * lilypond.
     * 
     * @return the lilypond representation of this element
     */
    public String toLilypondString();
    
    /**
     * Gets the representation of this element in the string format used by 
     * Guido.
     * 
     * @return the guido representation of this element
     */
    public String toGuidoString();
    
    /**
     * Indicates whether or not duration scaling is supported.  If true is 
     * returned, getLargestDurationDenominator() and scaleDurations() should 
     * both work properly without throwing an UnsupportedOperationException.
     * 
     * @return whether or not duration scaling is supported on this element
     */
    public boolean supportsDurationScaling();
    
    /**
     * Gets the largest duration denominator of this element.
     * 
     * @return the largest duration denominator
     */
    public long getLargestDurationDenominator();
        
    /**
     * Scales the durations of this element by multiplying the durations by the
     * given factor.  
     * @param scaleFactor the factor to multiply the durations by; should be a
     *        power of two
     */
    public void scaleDurations(long scaleFactor);
    
    /**
     * Gets a list of notation notes owned by this element and all owned
     * elements.
     * 
     * @return list of notation notes
     */
    public List<NotationNote> getNotationNotes();
}
