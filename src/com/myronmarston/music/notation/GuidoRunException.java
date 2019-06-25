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

import com.myronmarston.util.FileHelper;

/**
 * Exception that is thrown when an error occurs while running Guido.
 * 
 * @author Myron
 */
public class GuidoRunException extends Exception {
    // Used to serialize the class.  Change this if the class has a change significant enough to change the way the class is serialized.
    private static final long serialVersionUID = 1L;
    private final String guidoOutput;

    /**
     * Constructor.
     * 
     * @param guidoOutput the output from running guido
     */
    public GuidoRunException(String guidoOutput) {
        super("An error occurred while running Guido: " + FileHelper.NEW_LINE + guidoOutput);
        this.guidoOutput = guidoOutput;
    }

    /**
     * Gets the output from running guido.
     * 
     * @return the output from running guido
     */
    public String getGuidoOutput() {
        return guidoOutput;
    }            
}