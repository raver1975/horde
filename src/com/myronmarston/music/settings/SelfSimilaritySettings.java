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

import com.myronmarston.util.AbstractPublisher;
import org.simpleframework.xml.*;

/**
 * Specifies settings for what to apply self-similarity to.
 * 
 * @author Myron
 */
@Root
public class SelfSimilaritySettings extends AbstractPublisher {
    @Attribute
    private boolean applyToPitch;
    
    @Attribute
    private boolean applyToRhythm;
    
    @Attribute
    private boolean applyToVolume;
        
    @Attribute
    private int selfSimilarityIterations = 1;
    
    @Attribute
    private boolean readOnly = false;
      
    /**
     * Default Constructor.  Initializes all fields to the value of false.
     */
    public SelfSimilaritySettings() {
        this(false, false, false, 1);
    }            
    
    /**
     * Constructor.
     * 
     * @param applyToPitch whether or not to apply self-similarity to the 
     *        pitches
     * @param applyToRhythm whether or not to apply self-similarity to the 
     *        drums
     * @param applyToVolume whether or not to apply self-similarity to the 
     *        volume
     * @param selfSimilarityIterations number of times to iteratively apply
     *        self-similarity
     */    
    public SelfSimilaritySettings(boolean applyToPitch, boolean applyToRhythm, boolean applyToVolume, int selfSimilarityIterations) {
        this.applyToPitch = applyToPitch;
        this.applyToRhythm = applyToRhythm;
        this.applyToVolume = applyToVolume;
        this.setSelfSimilarityIterations(selfSimilarityIterations);
    }
    
    /**
     * Gets whether or not to apply self-similarity to the pitch of the germ
     * notes.  For example, if true, a germ of G A B G would become 
     * G A B G, A B C A, B C D B, G A B G.
     * 
     * @return whether or not to apply self-similarity to the pitch
     */
    public boolean getApplyToPitch() {
        return applyToPitch;
    }
    
    /**
     * Sets whether or not to apply self-similarity to the pitch of the germ
     * notes.  For example, if true, a germ of G A B G would become 
     * G A B G, A B C A, B C D B, G A B G.
     * 
     * @param val whether or not to apply self-similarity to the pitch
     */
    public void setApplyToPitch(boolean val) {
        if (this.readOnly) throw new UnsupportedOperationException("Cannot change values on a read-only object.");
        this.applyToPitch = val;    
        this.notifySubscribers(null);        
    }
    
    /**
     * Gets whether or not to apply self-similarity to the drums of the germ.
     * For example, if true, a germ drums of 1/4 1/8 1/4 would become
     * 1/4 1/8 1/4, 1/8 1/16 1/8, 1/4 1/8 1/4.
     * 
     * @return whether or not to apply self-similarity to the pitch
     */
    public boolean getApplyToRhythm() {
        return applyToRhythm;
    }

    /**
     * Sets whether or not to apply self-similarity to the drums of the germ.
     * For example, if true, a germ drums of 1/4 1/8 1/4 would become
     * 1/4 1/8 1/4, 1/8 1/16 1/8, 1/4 1/8 1/4.
     * 
     * @param val whether or not to apply self-similarity to the pitch
     */
    public void setApplyToRhythm(boolean val) {
        if (this.readOnly) throw new UnsupportedOperationException("Cannot change values on a read-only object.");
        this.applyToRhythm = val;
        this.notifySubscribers(null);
    }

    /**
     * Gets whether or not to apply self-similarity to the volume of the germ
     * notes.  For example, if true, a germ with a middle note accent would 
     * generate a middle section louder than the surrounding sections, with
     * the middle note of that section still louder.
     * 
     * @return whether or not to apply self-similarity to the volume of the germ
     *         notes
     */
    public boolean getApplyToVolume() {
        return applyToVolume;
    }

    /**
     * Sets whether or not to apply self-similarity to the volume of the germ
     * notes.  For example, if true, a germ with a middle note accent would 
     * generate a middle section louder than the surrounding sections, with
     * the middle note of that section still louder.
     * 
     * @param val whether or not to apply self-similarity to the volume of the 
     *        germ notes
     */
    public void setApplyToVolume(boolean val) {
        if (this.readOnly) throw new UnsupportedOperationException("Cannot change values on a read-only object.");
        this.applyToVolume = val;
        this.notifySubscribers(null);
    }

    /**
     * Whether or not this object is read-only.
     *  
     * @return true if this object is read-only
     */
    public boolean isReadOnly() {
        return readOnly;
    }        

    /**
     * Gets the number of self-similarity iterations.  The germ will be applied 
     * to itself for the first iteration, and then applied to the previous 
     * result for each successive iteration.
     * 
     * @return number of iterations
     */
    public int getSelfSimilarityIterations() {
        return selfSimilarityIterations;
    }

    /**
     * Sets the number of self-similarity iterations.  The germ will be applied 
     * to itself for the first iteration, and then applied to the previous 
     * result for each successive iteration.
     * 
     * @param selfSimilarityIterations number of iterations
     * @throws IllegalArgumentException if the value is less than 1
     */
    public void setSelfSimilarityIterations(int selfSimilarityIterations) throws IllegalArgumentException {
        if (this.readOnly) throw new UnsupportedOperationException("Cannot change values on a read-only object.");
        if (selfSimilarityIterations < 1) throw new IllegalArgumentException("The self-similarity must be greater than zero.");        
        this.selfSimilarityIterations = selfSimilarityIterations;
        this.notifySubscribers(null);
    }        
    
    /**
     * Gets whether or not self-similarity should be applied to something 
     * (pitch, drums or volume).
     * 
     * @return true if self-similarity should be applied to something
     */
    public boolean selfSimilarityShouldBeAppliedToSomething() {
        return this.getApplyToPitch() || this.getApplyToRhythm() || this.getApplyToVolume();
    }
    
    /**
     * Gets a read-only copy of this object.
     * 
     * @return a read-only copy of the object
     */
    public SelfSimilaritySettings getReadOnlyCopy() {
        SelfSimilaritySettings sss = this.clone();                                
        sss.readOnly = true;
        return sss;     
    }

    @Override
    public SelfSimilaritySettings clone() {
        return (SelfSimilaritySettings) super.clone();
    }
        
    // equals and hashCode were generated by Netbeans IDE  
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SelfSimilaritySettings other = (SelfSimilaritySettings) obj;
        if (this.applyToPitch != other.applyToPitch) {
            return false;
        }
        if (this.applyToRhythm != other.applyToRhythm) {
            return false;
        }
        if (this.applyToVolume != other.applyToVolume) {
            return false;
        }
        if (this.selfSimilarityIterations != other.selfSimilarityIterations) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.applyToPitch ? 1 : 0);
        hash = 89 * hash + (this.applyToRhythm ? 1 : 0);
        hash = 89 * hash + (this.applyToVolume ? 1 : 0);
        hash = 89 * hash + this.selfSimilarityIterations;
        return hash;
    }         
}