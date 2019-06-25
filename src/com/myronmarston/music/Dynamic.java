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

package com.myronmarston.music;

/**
 * Contains pre-defined volume levels based on musical dynamics.
 * 
 * @author Myron
 */
public enum Dynamic {    
    // Be sure to update the regex below if you add or remove enum values.
    PPP(1),
    PP(2),
    P(3),
    MP(4),
    MF(5),
    F(6),
    FF(7),
    FFF(8);
    
    private final int dynamicLevel;
    private final int midiVolume;   
    private static final int MIDI_VOLUME_PER_LEVEL = 15;
    private static final int ADDITIONAL_OFFSET = -4;
    
    /**
     * Regular expression pattern that can be used to parse a dynamic in a 
     * note string.  "FFF" must come before "FF and "F" so that the regex
     * consumes as many characters as possible rather than stopping at the first match.
     */
    public final static String REGEX_STRING = "PPP|FFF|PP|FF|MP|MF|P|F";
    
    private Dynamic(int dynamicLevel) {
        this.dynamicLevel = dynamicLevel;
        this.midiVolume = (dynamicLevel * MIDI_VOLUME_PER_LEVEL) + ADDITIONAL_OFFSET;
    }
    
    /**
     * Gets the midi volume for this dynamic.
     * 
     * @return midi volume
     */
    public int getMidiVolume() {
        return this.midiVolume;
    }

    /**
     * Gets the dynamic level for this dynamic.
     * 
     * @return this dynamic level
     */
    public int getDynamicLevel() {
        return dynamicLevel;
    }        
    
    /**
     * Gets the dynamic that matches the given dynamic level.
     * 
     * @param dynamicLevel the dynamic level
     * @return the dynamic that matches the given dynamic level
     */
    private static Dynamic getDynamicForDynamicLevel(int dynamicLevel) {
        Dynamic d = Dynamic.values()[dynamicLevel - 1];
        assert d.dynamicLevel == dynamicLevel : d;
        return d;
    }
    
    /**
     * Gets the Dynamic in whose range the given midiVolume falls.
     * 
     * @param midiVolume the midi volume
     * @return the dynamic for this volume, or null if the midiVolume falls 
     *         outside of the acceptable range
     */
    public static Dynamic getDynamicForMidiVolume(int midiVolume) {
        if (midiVolume <= MidiNote.MIN_VELOCITY || midiVolume > MidiNote.MAX_VELOCITY) return null;
        
        int dynamicLevel = (midiVolume - (MIDI_VOLUME_PER_LEVEL + ADDITIONAL_OFFSET)) / MIDI_VOLUME_PER_LEVEL + 1;
        return getDynamicForDynamicLevel(dynamicLevel);
    }
    
    /**
     * Gets a list of possible dynamic values.
     * 
     * @return a string containing a list of possible dynamic values
     */
    public static String getDynamicExampleString() {
        StringBuilder str = new StringBuilder();
        Dynamic[] values = Dynamic.values(); // cache the array...
        
        for (int i = 0; i < values.length; i++) {
            str.append(values[i].toString());            
            
            if (i == values.length - 2) str.append(" or ");                
            else if (i < values.length - 2) str.append(", ");            
        }
        
        return str.toString();        
    }            
}
