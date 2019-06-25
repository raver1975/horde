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

import java.util.AbstractList;
import java.util.Map;

/**
 * An instance of this contains a subset of all the VoiceSections of the
 * entire fractal piece.  Specifically, it contains all the VoiceSections
 * for a particular voice or for a particular section.
 * None of the VoiceSections are actually stored here; instead, all voice 
 * sections for a fractal piece are stored in a hash table, and this delegates 
 * to that.  This list is unmodifiable by design.  
 * According to the javadocs, I only have to implement get(int index) 
 * and size() to implement a unmodifiable list.  The AbstractList implements 
 * the other methods by using these.
 * 
 * @author Myron
 */
public class VoiceSectionList extends AbstractList<VoiceSection> {       
    private Map<VoiceSectionHashMapKey, VoiceSection> voiceSectionHash;
    private AbstractVoiceOrSection constantVoiceOrSection;

    /**
     * Constructor.
     * 
     * @param voiceSectionHash HashMap that stores all the VoiceSections for
     *        the entire FractalPiece
     * @param constantVoiceOrSection the Voice or Section that is constant for
     *        all VoiceSections in this list
     */
    public VoiceSectionList(Map<VoiceSectionHashMapKey, VoiceSection> voiceSectionHash, AbstractVoiceOrSection constantVoiceOrSection) {
        this.voiceSectionHash = voiceSectionHash;
        this.constantVoiceOrSection = constantVoiceOrSection;
    }    
    
    @Override
    public VoiceSection get(int index) {
        VoiceSectionHashMapKey key = this.constantVoiceOrSection.getHashMapKeyForOtherTypeIndex(index);
        return this.voiceSectionHash.get(key);
    }
    
    /**
     * Gets the voice section that is for the voice or section of the other type
     * with the given unique index.
     * 
     * @param uniqueIndex the unique index of the other voice or section
     * @return the appropriate voiceSection
     */
    public VoiceSection getByOtherTypeUniqueIndex(int uniqueIndex) {        
        VoiceSectionHashMapKey key = this.constantVoiceOrSection.getHashMapKeyForOtherTypeUniqueIndex(uniqueIndex);
        return this.voiceSectionHash.get(key);
    }

    @Override
    public int size() {
        if (this.constantVoiceOrSection.getListOfMainType().contains(this.constantVoiceOrSection)) {
            return this.constantVoiceOrSection.getListOfOtherType().size();
        }        
        
        return 0;
    }
    
    /**
     * Increments the modCount, in order to properly trigger a 
     * ConcurrentModificationException.  Should be called when a voice section 
     * is added or removed from the hash map.  This is a bit hack-ish, but with 
     * Java's lack of built in event support (a la .NET), this is the easiest 
     * work around.  I would have liked to use Observable and Observer, but with
     * Observable being a class rather than an interface, and Java's lack of
     * multiple inheritance, that's not feasible.
     */
    protected void incrementModCount() {
        this.modCount++;
    }         
}
