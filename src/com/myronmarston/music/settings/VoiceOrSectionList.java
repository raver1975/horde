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

import org.simpleframework.xml.*;
import org.simpleframework.xml.core.Commit;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * List of Voices or Sections that manages the creation and deletion of the 
 * VoiceSection objects as necessitated by add() and remove().
 * 
 * @param <M> the main type for this list (Voice or Section)
 * @param <O> the other type (Voice or Section)
 * @author Myron
 */
@Root
public class VoiceOrSectionList<M extends AbstractVoiceOrSection, O extends AbstractVoiceOrSection> extends AbstractList<M> {
    @ElementList(type=AbstractVoiceOrSection.class)
    private ArrayList<M> internalList = new ArrayList<M>();
    
    @Element
    private FractalPiece fractalPiece;
    
    @Attribute
    private int lastUniqueIndex = 0;
    
    private boolean isDeserializing = false;
    

    /**
     * Constructor.
     * 
     * @param fractalPiece the fractal piece that owns this list
     */
    public VoiceOrSectionList(FractalPiece fractalPiece) {
        this.fractalPiece = fractalPiece;
    }
    
    /**
     * Provided for xml deserialization.
     */
    private VoiceOrSectionList() {
        isDeserializing = true;
    }

    /**
     * Gets the hash table of voice sections for the entire fractal piece.
     * 
     * @return the hash table of voice sections
     */
    private Map<VoiceSectionHashMapKey, VoiceSection> getVoiceSections() {
        return fractalPiece.getVoiceSections();
    }
            
    @Override
    public M get(int index) {
        return internalList.get(index);
    }
    
    /**
     * Gets an item using the unique index.  This is primarily provided to 
     * support fractalcomposer.com, which stores the unique index in the 
     * webpage and then uses it for later retrieval.
     * 
     * @param uniqueIndex the unique index of the item to get
     * @return the item matching the given unique index
     * @throws IndexOutOfBoundsException if there is no item that matches the
     *         given unique index
     */
    public M getByUniqueIndex(int uniqueIndex) throws IndexOutOfBoundsException {        
        for (M vOrS : this.internalList) {
            if (vOrS.getUniqueIndex() == uniqueIndex) {
                return vOrS;
            }
        }
        
        throw new IndexOutOfBoundsException("No item with unique index " + uniqueIndex + " could be found.");
    }

    @Override
    public int size() {
        return internalList.size();
    }

    @Override        
    public boolean add(M mainVorS) {
        this.add(this.size(), mainVorS);
        return true; // return true since our collection changed...
    }

    /**
     * Insert the given Voice or Section into the list at the given index.
     * 
     * @param index the insertion index
     * @param mainVorS the voice or section to add
     */
    @Override
    @SuppressWarnings("unchecked")
    public void add(int index, M mainVorS) {               
        internalList.add(index, mainVorS); 
                
        // the rest of this method is only intended to be run during normal
        // object usage, not during deserialization.
        if (isDeserializing) return;
        
        int otherTypeIndex = 0; 
        VoiceSection vs;             
        VoiceSectionHashMapKey key;
        
        // create the necessary Voice Sections...
        for (AbstractVoiceOrSection otherVOrS : (List<O>) mainVorS.getListOfOtherType()) {
            vs = mainVorS.instantiateVoiceSection(otherVOrS);
            
            key = vs.createHashMapKey();
            assert !this.getVoiceSections().containsKey(key) : this.getVoiceSections().get(key);
            this.getVoiceSections().put(key, vs);
            
            // assert that our lists have it...
            assert mainVorS.getVoiceSections().get(otherTypeIndex++) == vs;
            assert otherVOrS.getVoiceSections().get(index) == vs;
            
            // notify the VoiceSectionLists that they have been modified...
            vs.getVoice().getVoiceSections().incrementModCount();
            vs.getSection().getVoiceSections().incrementModCount();
        }
        
        this.modCount++;        
    }
            
    @Override
    public M remove(int index) {
        M itemToRemove = this.internalList.get(index);
        int voiceSectionListSize = itemToRemove.getVoiceSections().size();                
        
        // remove the related voice sections...
        // we can't modify the list while iterating over it or we'll get a
        // ConcurrentModificationException, so we temporarily store the ones
        // to remove in another list.
        ArrayList<VoiceSection> voiceSectionsToRemove = new ArrayList<VoiceSection>(voiceSectionListSize);
        for (VoiceSection vs : itemToRemove.getVoiceSections()) {
            voiceSectionsToRemove.add((vs));
        }
        
        this.modCount++;
        this.internalList.remove(index);
        
        for (VoiceSection vs : voiceSectionsToRemove) {
            this.getVoiceSections().remove(vs.createHashMapKey());
                                               
            // notify the VoiceSectionLists that they have been modified...
            vs.getVoice().getVoiceSections().incrementModCount();
            vs.getSection().getVoiceSections().incrementModCount();                        
        }      
        
        assert itemToRemove.getVoiceSections().size() == 0 : itemToRemove.getVoiceSections().size();
        
        return itemToRemove;
    }       
    
    /**
     * Removes the item that matches the given unique index.  This is primarily 
     * provided to support fractalcomposer.com, which stores the unique index 
     * in the webpage and then uses it for later retrieval.
     * 
     * @param uniqueIndex the unique index
     * @return the item that was removed.
     * @throws IndexOutOfBoundsException if there is no item that matches the
     *         given unique index
     */
    public M removeByUniqueIndex(int uniqueIndex) throws IndexOutOfBoundsException {        
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).getUniqueIndex() == uniqueIndex) {
                return this.remove(i);
            }
        }
        
        throw new IndexOutOfBoundsException("No item with unique index " + uniqueIndex + " could be found.");
    }

    /**
     * Gets the last unique index used by this list.
     * 
     * @return the last unique index
     */
    protected int getLastUniqueIndex() {        
        return this.lastUniqueIndex;
    }

    /**
     * Sets the value for the last unique index.
     * 
     * @param lastUniqueIndex the value
     */
    public void setLastUniqueIndex(int lastUniqueIndex) {
        this.lastUniqueIndex = lastUniqueIndex;
    }        
    
    /**
     * Gets the next available unique index, and increments the unique index
     * counter.
     *   
     * @return the next available unique index
     */
    protected int getNextUniqueIndex() {        
        lastUniqueIndex++;        
        return this.lastUniqueIndex;
    }
    
    /**
     * Normalizes the unique indices so as to number them in natural order.
     */
    protected void normalizeUniqueIndices() {
        int index = 1;
        for (M vOrS : this) {
            vOrS.setUniqueIndex(index++);
        }
        
        this.lastUniqueIndex = index - 1;
    }

    @Commit
    private void deserializationComplete() {
        this.isDeserializing = false;
    }
}
