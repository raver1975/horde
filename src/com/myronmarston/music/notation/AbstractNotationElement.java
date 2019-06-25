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

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class providing a partial implementation of the NotationElement
 * interface.  Classes that should implement the NotationElement interface and 
 * don't extend another class can extend this class.
 * 
 * @author Myron
 */
public abstract class AbstractNotationElement implements NotationElement {

    /**
     * Throws an UnsupportedOperationException.  This should be overriden
     * if the sub class supports duration scaling.
     * 
     * @return throws an UnsupportedOperationException
     * @throws UnsupportedOperationException always
     */
    public long getLargestDurationDenominator() throws UnsupportedOperationException {
        if (!this.supportsDurationScaling()) {
            throw new UnsupportedOperationException("This element does not support this operation.");
        }

        throw new UnsupportedOperationException("The operation must be overriden in a sub class.");
    }

    /**
     * Throws an UnsupportedOperationException.  This should be overriden
     * if the sub class supports duration scaling.
     *
     * @param scaleFactor the scale factor
     * @throws UnsupportedOperationException always
     */
    public void scaleDurations(long scaleFactor) throws UnsupportedOperationException {
        if (!this.supportsDurationScaling()) {
            throw new UnsupportedOperationException("This element does not support this operation.");
        }        
        
        throw new UnsupportedOperationException("The operation must be overriden in a sub class.");
    }

    /**
     * Returns an empty list of notation notes.
     * 
     * @return an empty list of notation notes
     */
    public List<NotationNote> getNotationNotes() {
        return new ArrayList<NotationNote>();
    }
   
}
