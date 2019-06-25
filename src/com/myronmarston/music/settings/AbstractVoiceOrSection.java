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

import com.myronmarston.music.GermIsEmptyException;
import com.myronmarston.music.OutputManager;
import com.myronmarston.util.Subscriber;
import org.simpleframework.xml.*;

/**
 * An abstract class containing a common interface and common logic shared
 * by the Voice and Section classes.
 *   
 * @param <M> the main type--Voice or Section
 * @param <O> the other type--Voice, if this is a Section, or Section,
 *        if this is a voice
 * @author Myron
 */
@Root
public abstract class AbstractVoiceOrSection<M extends AbstractVoiceOrSection, O extends AbstractVoiceOrSection> implements Subscriber {
    @Element
    private FractalPiece fractalPiece;
    
    @Attribute
    private int uniqueIndex;
    private VoiceSectionList voiceSections;
    private String className = null;

    /**
     * Constructor.
     * 
     * @param fractalPiece The FractalPiece that this Voice or 
     *        Section is a part of
     * @param uniqueIndex unique index for this voice or section
     */
    protected AbstractVoiceOrSection(FractalPiece fractalPiece, int uniqueIndex) {
        this.uniqueIndex = uniqueIndex;
        this.fractalPiece = fractalPiece;
    } 
    
    /**
     * Gets the FractalPiece that this Voice or Section is a part of.
     * 
     * @return The FractalPiece that this Voice or Section is a part of
     */
    public FractalPiece getFractalPiece() {        
        return this.fractalPiece;
    }

    /**
     * Gets an index that is unique for each voice or section in the list.  This
     * is also 1-based so as to be human readable.  This is intended to support
     * fractalcomposer.com so as to provide a unique div id.  The regular index
     * cannot be used because adding/removing items via AJAX can result in 
     * duplicate div ids.
     * 
     * @return the unique index
     */
    public int getUniqueIndex() {
        return uniqueIndex;
    }        
    
    /**
     * Sets the unique index.
     * 
     * @param uniqueIndex the unique index
     */
    protected void setUniqueIndex(int uniqueIndex) {
        this.uniqueIndex = uniqueIndex;        
    }
    
    /**
     * Gets a list of VoiceSections.  This is guarenteed to never return null.
     * The list of VoiceSections will be a subset of all the VoiceSections for
     * the entire FractalPiece.  Specifically, if this is a Voice, the list will
     * contain each VoiceSection for this Voice, and for each Section.  If this
     * is a Section, the list will contain each VoiceSection for this Section, 
     * and for each Voice.
     * 
     * @return the list of VoiceSections
     */
    public VoiceSectionList getVoiceSections() {
        if (voiceSections == null) voiceSections = new VoiceSectionList(this.getFractalPiece().getVoiceSections(), this);                           
        return voiceSections;
    }
    
    /**
     * Returns the FractalPiece's list of this type.  When implemented by the 
     * Voice class, this should return a list of all Voices in the fractal 
     * piece.  When implemented by the Section class, this should return a list
     * of al the Sections in the fractal piece.
     * 
     * @return a List of Voices (if the implementing type is Voice) or a list
     *         of Sections (if the implementing type is Section)
     */
    protected abstract VoiceOrSectionList<M, O> getListOfMainType();
    
    /**
     * When implemented by the Voice class, this should return a list of all the 
     * Sections in the fractal piece.  When implemented by the Section class, 
     * this should return a list of all the Voices in the fractal piece.
     * 
     * @return a List of Voices (if the implementing type is Section) or a List 
     *         of Sections (if the implementing type is Voice)
     */
    protected abstract VoiceOrSectionList<O, M> getListOfOtherType();    
    
    /**
     * Creates a VoiceSectionHashMapKey, combining this object with an object 
     * of the other type, based on the index.
     * 
     * @param index The index in the list of the other type to combine 
     *        with to create the hash map key
     * @return A VoiceSectionHashMapKey that can be used to get a 
     *         specific VoiceSection
     */
    protected abstract VoiceSectionHashMapKey getHashMapKeyForOtherTypeIndex(int index);
    
    /**
     * Creates a VoiceSectionHashMapKey, combining this object with an object 
     * of the other type, based on the unique index.
     * @param uniqueIndex The unique index in the list of the other type to 
     *        combine with to create the hash map key
     * @return A VoiceSectionHashMapKey that can be used to get a 
     *         specific VoiceSection
     */
    protected abstract VoiceSectionHashMapKey getHashMapKeyForOtherTypeUniqueIndex(int uniqueIndex);
    
    /**
     * Instantiates and returns a VoiceSection, using this and the passed voice 
     * or section.
     * @param vOrS a Voice or Section to use in the instantiating
     * @return a new VoiceSection
     */
    protected abstract VoiceSection instantiateVoiceSection(O vOrS);
    
    /**
     * Creates the output manager for this voice or section.
     * 
     * @return the output manager
     * @throws com.myronmarston.music.GermIsEmptyException if the germ is empty
     */
    public abstract OutputManager createOutputManager() throws GermIsEmptyException;

    /**
     * Gets the name of this class--e.g., "Voice" or "Section". 
     * 
     * @return the name of this type
     */
    public String getClassName() {                
        if (className == null) className = this.getClass().getSimpleName();
        return className;
    }
    
    /**
     * Sets all voice sections for this voice or section to rest.
     * 
     * @param val true to rest; false to not rest
     */
    public void setRestOnAllVoiceSections(boolean val) {
        for (VoiceSection vs : this.getVoiceSections()) {
            vs.setRest(val);
        }
    }

    /**
     * Clears the cached voice section results.  Should be called whenever a
     * setting is changed that is used by the voice sections.
     */
    protected void clearVoiceSectionResults() {
        for (VoiceSection vs : this.getVoiceSections()) {
            vs.clearVoiceSectionResult();
        }
    }
}
