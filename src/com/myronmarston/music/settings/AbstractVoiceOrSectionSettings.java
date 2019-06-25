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

import com.myronmarston.music.NoteList;
import com.myronmarston.music.scales.Scale;
import com.myronmarston.music.transformers.OctaveTransformer;
import com.myronmarston.music.transformers.RhythmicDurationTransformer;
import com.myronmarston.music.transformers.TransposeTransformer;
import com.myronmarston.music.transformers.VolumeTransformer;
import com.myronmarston.util.AbstractPublisher;
import com.myronmarston.util.Fraction;
import org.simpleframework.xml.*;

/**
 * Class that has settings that can be set either on a voice or a section.  This
 * also contains some common logic used by both the VoiceSettings and 
 * SectionSetttings classes.
 * 
 * @author Myron
 */
@Root
public abstract class AbstractVoiceOrSectionSettings extends AbstractPublisher {
    
    @Element
    private Fraction volumeAdjustment; 
    
    @Attribute
    private int scaleStepOffset;
    
    @Attribute
    private int octaveAdjustment;
    
    @Element
    private Fraction speedScaleFactor;    
    
    @Attribute
    private boolean readOnly;

    /**
     * Default constructor.  Initializes the fields to default values.
     */
    protected AbstractVoiceOrSectionSettings() {
        this(new Fraction(0, 1), 0, 0, new Fraction(1, 1));
    }
    
    /**
     * Constructor.
     * 
     * @param volumeAdjustment the volume adjustment
     * @param scaleStepOffset the scale step offset
     * @param octaveAdjustment the octave adjustment     
     * @param speedScaleFactor the speed scale factor
     */
    protected AbstractVoiceOrSectionSettings(Fraction volumeAdjustment, int scaleStepOffset, int octaveAdjustment, Fraction speedScaleFactor) {
        this.setVolumeAdjustment(volumeAdjustment);
        this.setScaleStepOffset(scaleStepOffset);
        this.setOctaveAdjustment(scaleStepOffset);
        this.setSpeedScaleFactor(speedScaleFactor);        
    }        
    
    /**
     * Gets the scale step offset.  This can be used to move the music to a 
     * different pitch level.
     * 
     * @return the scale step offset
     */
    public int getScaleStepOffset() {
        return scaleStepOffset;
    }

    /**
     * Sets the scale step offset.  This can be used to move the music to a 
     * different pitch level.
     * 
     * @param scaleStepOffset the scale step offset
     * @throws IllegalArgumentException if the scale step offset is outside of
     *         the range -11 to 11
     * @throws UnsupportedOperationException if this object is read-only
     */
    public void setScaleStepOffset(int scaleStepOffset) throws IllegalArgumentException, UnsupportedOperationException {
        this.readOnlyException();
        if (Math.abs(scaleStepOffset) >= Scale.NUM_CHROMATIC_PITCHES_PER_OCTAVE) 
            throw new IllegalArgumentException("The scaleStepOffset must be between -11 and 11.");
        
        this.scaleStepOffset = scaleStepOffset;
        this.notifySubscribers(null);
    }

    /**
     * Gets the volume adjustment.  This will be used to scale the volume of
     * this voice or section.  Should be between -1 and 1.  Negative values
     * decrease the volume; positive values increase it.
     * 
     * @return the volume adjustment
     */
    public Fraction getVolumeAdjustment() {
        return volumeAdjustment;
    }

    /**
     * Sets the volume adjustment.  This will be used to scale the volume of
     * this voice or section.  Should be between -1 and 1.  Negative values
     * decrease the volume; positive values increase it.
     * 
     * @param volumeAdjustment the volume adjustment
     * @throws IllegalArgumentException if the volume adjustment is outside of 
     *         the range -1 to 1
     * @throws UnsupportedOperationException if this is a read-only object
     */
    public void setVolumeAdjustment(Fraction volumeAdjustment) throws IllegalArgumentException, UnsupportedOperationException {        
        this.readOnlyException();
        VolumeTransformer.checkVolumeScaleFactorValidity(volumeAdjustment);        
        this.volumeAdjustment = volumeAdjustment;
        this.notifySubscribers(null);
    }
    
    /**
     * Gets how many octaves to adjust the germ for use by this voice.
     * 
     * @return number of octaves to adjust
     */
    public int getOctaveAdjustment() {
        return octaveAdjustment;
    }
    
    /**
     * Sets how many octaves to adjust the germ for use by this voice.
     * 
     * @param val number of octaves to adjust
     * @throws UnsupportedOperationException if this object is read-only
     */
    public void setOctaveAdjustment(int val) throws UnsupportedOperationException {     
        this.readOnlyException();
        // TODO: is there a way to tell if the octave adjustment value would 
        // give us a note outside of the standard midi range? should we 
        // test and throw an IllegalArgumentException here?        
        this.octaveAdjustment = val;
        this.notifySubscribers(null);  
    }
    
    /**
     * Gets the speed scale factor to apply to the germ for use by this voice.
     * 
     * @return the speed scale factor
     */
    public Fraction getSpeedScaleFactor() {
        return speedScaleFactor;
    }
    
    /**
     * Sets the speed scale factor to apply to the germ for use by this voice.
     * 
     * @param val the speed scale factor
     * @throws UnsupportedOperationException if this object is read-only
     */
    public void setSpeedScaleFactor(Fraction val) throws UnsupportedOperationException {   
        this.readOnlyException();
        RhythmicDurationTransformer.checkScaleFactorValidity(val);
        this.speedScaleFactor = val;
        this.notifySubscribers(null);  
    }
    
    /**
     * Gets a value indicating whether or not this settings object is read-only.
     * 
     * @return true if this object is read-only
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * If the object is read-only, throws an UnsupportedOperationException. This
     * method should be called at the start of every setter in this class and
     * all sub classes.     
     * 
     * @throws UnsupportedOperationException if this object is read-only
     */
    protected void readOnlyException() throws UnsupportedOperationException {
        if (this.readOnly) 
            throw new UnsupportedOperationException("Cannot change values on a read-only object.");
    }
    
    /**
     * Applies these settings to the given note list.
     * 
     * @param noteList the note list to apply the settings to
     * @param scale the scale to use in conjunction with the settings
     * @return the result of applying the settings to the note list
     */
    public NoteList applySettingsToNoteList(NoteList noteList, Scale scale) {                        
        // we want the offset to be in the range -NumScaleSteps to +NumScaleSteps
        int offsetToUse = scale.getNormalizedScaleStep(scaleStepOffset);
        offsetToUse -= (scaleStepOffset < 0 ? scale.getScaleStepArray().length : 0);
        
        OctaveTransformer octaveT = new OctaveTransformer(this.getOctaveAdjustment());
        RhythmicDurationTransformer rhythmT = new RhythmicDurationTransformer(this.getSpeedScaleFactor());                        
        TransposeTransformer transposeT = new TransposeTransformer(offsetToUse, scale.getRecommendedTransposeLetterNumber(this.scaleStepOffset));
        VolumeTransformer volumeT = new VolumeTransformer(this.getVolumeAdjustment());                
               
        NoteList temp = octaveT.transform(noteList);
        temp = volumeT.transform(temp);                
        temp = rhythmT.transform(temp);                          
        temp = transposeT.transform(temp);
        return temp;
    }
    
    /**
     * Gets a read-only copy of this object.  
     * 
     * @return a read-only copy of this object
     */
    public AbstractVoiceOrSectionSettings getReadOnlyCopy() {        
        AbstractVoiceOrSectionSettings settings = this.clone();                                            
        settings.readOnly = true;
        return settings;
    }

    @Override
    public AbstractVoiceOrSectionSettings clone() {
        return (AbstractVoiceOrSectionSettings) super.clone();
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
        final AbstractVoiceOrSectionSettings other = (AbstractVoiceOrSectionSettings) obj;
        if (this.volumeAdjustment != other.volumeAdjustment && (this.volumeAdjustment == null || !this.volumeAdjustment.equals(other.volumeAdjustment))) {
            return false;
        }
        if (this.scaleStepOffset != other.scaleStepOffset) {
            return false;
        }
        if (this.octaveAdjustment != other.octaveAdjustment) {
            return false;
        }
        if (this.speedScaleFactor != other.speedScaleFactor && (this.speedScaleFactor == null || !this.speedScaleFactor.equals(other.speedScaleFactor))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.volumeAdjustment != null ? this.volumeAdjustment.hashCode() : 0);
        hash = 97 * hash + this.scaleStepOffset;
        hash = 97 * hash + this.octaveAdjustment;
        hash = 97 * hash + (this.speedScaleFactor != null ? this.speedScaleFactor.hashCode() : 0);
        return hash;
    }            

}
