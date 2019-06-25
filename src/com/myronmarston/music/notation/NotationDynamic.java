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

import com.myronmarston.music.Dynamic;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Locale;

/**
 * Represents the dynamic and articulation notation for one notation note.  The 
 * presence or absence of a particular dynamic or articulation indicates what 
 * should be notated in the context of the notation note, not simply the 
 * absolute dynamic level of the notation note.
 * 
 * @author Myron
 */
public class NotationDynamic extends AbstractNotationElement implements Cloneable {
    /**
     * Represents the possible articulations.
     */
    public enum Articulation {
        /**
         * No special articulation.
         */
        NONE("", ""),
        
        /**
         * An accent mark (>)--indicates that a note should be emphasized.
         */
        ACCENT("->", "\\accent"),
        
        /**
         * A marcato mark (^)--indicates a heavy accent.
         */
        MARCATO("-^", "\\marcato");
                
        private final String lilypondString;
        private final String guidoString;

        /**
         * Constructor.
         * 
         * @param lilypondString the lilypond string for this articulation
         * @param guidoTagName the name of the guido tag for this articulation
         */
        private Articulation(String lilypondString, String guidoTagName) {
            this.lilypondString = lilypondString;
            this.guidoString = 
                (guidoTagName.isEmpty() ? NotationNote.NOTE_PLACEHOLDER : 
                 guidoTagName + "(" + NotationNote.NOTE_PLACEHOLDER + ")");
        }
        
        /**
         * Gets the lilypond representation of this articulation.
         * 
         * @return the lilypond string
         */
        public String toLilypondString() {
            return this.lilypondString;
        }
        
        /**
         * Gets the guido representation of this articulation.
         * 
         * @return the guido string
         */
        public String toGuidoString() {            
            return this.guidoString;
        }                
    }
    private final Dynamic dynamic;
    private final Articulation articulation;
    private String lilypondString;
    private String guidoString;
    
    /**
     * A default empty notation dynamic.
     */
    public final static NotationDynamic DEFAULT_EMPTY = new NotationDynamic(null, Articulation.NONE);

    /**
     * Constructor.
     * 
     * @param dynamic the dynamic to mark on the notation
     * @param articulation the articulation to mark on the notation
     */
    public NotationDynamic(Dynamic dynamic, Articulation articulation) {
        assert articulation != null;
        this.dynamic = dynamic;
        this.articulation = articulation;
    }
            
    /**
     * Returns false since this element does not support duration scaling.
     * 
     * @return false
     */
    public boolean supportsDurationScaling() {
        return false;
    }

    /**
     * Gets the representation of this notation dynamic in guido notation.
     * 
     * @return the guido string
     */
    public String toGuidoString() {        
        if (guidoString == null) {
            //TODO: the version of Guido I've installed on my dev machine
            // does not support ppp dynamics (but does support fff).  Check to
            // see if this is also the case with the guido note server, and if
            // so, replace ppp with pp. Update: the notation server also can't
            // handle PPP
            guidoString = 
                (this.dynamic == null ? 
                 this.articulation.toGuidoString() : 
                 "\\intens<\"" + this.dynamic.toString().toLowerCase(Locale.ENGLISH) + "\"> " + this.articulation.toGuidoString());        
        }        
        
        return guidoString;
    }

    /**
     * Gets the representation of this notation dynamic in lilypond notation.
     * 
     * @return the lilypond string
     */
    public String toLilypondString() {
        if (lilypondString == null) {
            lilypondString = 
                (this.dynamic == null ? 
                 this.articulation.toLilypondString() : 
                 "\\" + this.dynamic.toString().toLowerCase(Locale.ENGLISH) + this.articulation.toLilypondString());
        }
        
        return lilypondString;        
    }

    @Override
    public String toString() {
        String dynamicStr = (this.dynamic == null ? "(No dynamic)" : this.dynamic.toString());
        return dynamicStr + " " + this.articulation.toString();
    }        

    // equals() and hashCode() were generated by Netbeans IDE
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NotationDynamic other = (NotationDynamic) obj;
        if (this.dynamic != other.dynamic) {
            return false;
        }
        if (this.articulation != other.articulation) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.dynamic != null ? this.dynamic.hashCode() : 0);
        hash = 97 * hash + (this.articulation != null ? this.articulation.hashCode() : 0);
        return hash;
    }

    @Override
    protected NotationDynamic clone() {
        try {
            return (NotationDynamic) super.clone();
        } catch (CloneNotSupportedException ex) {
            // We have implemented the Cloneable interface, so we should never
            // get this exception.  If we do, there's something very, very wrong...
            throw new UndeclaredThrowableException(ex, "Unexpected error while cloning.  This indicates a programming or JVM error.");
        }                 
    }
   
}
