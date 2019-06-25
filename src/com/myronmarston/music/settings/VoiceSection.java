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
import com.myronmarston.music.Instrument;
import com.myronmarston.music.Note;
import com.myronmarston.music.NoteList;
import com.myronmarston.music.OutputManager;
import com.myronmarston.music.scales.Scale;
import com.myronmarston.util.Fraction;
import com.myronmarston.util.Publisher;
import com.myronmarston.util.Subscriber;
import org.simpleframework.xml.*;

import java.util.Arrays;

/**
 * Represents the smallest unit of the fractal piece for which the user can
 * specify settings.  One of these exists for each combination of a Voice and
 * a Section.  
 * 
 * @author Myron
 */
@Root
public class VoiceSection implements Subscriber, Cloneable {

    @Attribute
    private boolean rest = false;
    
    @Attribute
    private boolean overrideVoiceSettings = false;
    
    @Element(required=false)
    private VoiceSettings voiceSettings;
    
    @Attribute
    private boolean overrideSectionSettings = false;        
    
    @Element(required=false)
    private SectionSettings sectionSettings;
            
    @Element
    private Voice voice;
    
    @Element
    private Section section;
    
    private NoteList voiceSectionResult;

    /**
     * Constructor.
     * 
     * @param voice the voice this is a section of
     * @param section the section this VoiceSection plays with simultaneously
     */
    protected VoiceSection(Voice voice, Section section) {
        this.voice = voice;
        this.section = section;    
    }      
    
    /**
     * Provided for xml deserialization.
     */
    private VoiceSection() { }
        
    /**
     * The Section this VoiceSection plays with simultaneously.
     * 
     * @return the Section this VoiceSection plays with simultaneously
     */
    public Section getSection() {
        return section;
    }

    /**
     * The voice this is a section of.
     * 
     * @return the voice this is a section of
     */
    public Voice getVoice() {
        return voice;
    }      
    
    /**
     * Gets the other voice or section--the one that is not passed as a
     * parameter to this method.
     * 
     * @param vOrS a voice or section
     * @return the other voice or section
     */
    public AbstractVoiceOrSection getOtherVoiceOrSection(AbstractVoiceOrSection vOrS) {        
        if (vOrS.getClass() == Voice.class) {
            return this.getSection();
        }
        else {
            assert vOrS.getClass() == Section.class : vOrS;
            return this.getVoice();
        }        
    }

    /**
     * Gets a value indicating whether or not this voice section will override
     * the section settings.
     * 
     * @return false if the default settings will be used, true if the
     *         settings will be overriden
     */
    public boolean getOverrideSectionSettings() {
        return overrideSectionSettings;
    }

    /**
     * Sets a value indicating whether or not this voice section will override 
     * the section settings.
     * 
     * @param overrideSectionSettings false if the default settings will be 
     *        used, true if the settings will be overriden
     */
    public void setOverrideSectionSettings(boolean overrideSectionSettings) {
        if (!this.overrideSectionSettings && overrideSectionSettings) {
            // the value is changing from using the default settings to overriding them,
            // so create a local settings object that is initially identical to
            // the default            
            this.setSectionSettings(this.getSection().getSettings().clone());            
        }
        
        this.overrideSectionSettings = overrideSectionSettings;
    }

    /**
     * Gets a value indicating whether or not this voice section will override
     * the voice settings.
     * 
     * @return false if the default settings will be used, true if the
     *         settings will be overriden
     */
    public boolean getOverrideVoiceSettings() {
        return overrideVoiceSettings;
    }

    /**
     * Sets a value indicating whether or not this voice section will override 
     * the voice settings.
     * 
     * @param overrideVoiceSettings false if the default settings will be 
     *        used, true if the settings will be overriden
     */
    public void setOverrideVoiceSettings(boolean overrideVoiceSettings) {
        if (!this.overrideVoiceSettings && overrideVoiceSettings) {
            // the value is changing from using the default settings to overriding them,
            // so create a local settings object that is initially identical to
            // the default            
            this.setVoiceSettings(this.getVoice().getSettings().clone());            
        }
        
        this.overrideVoiceSettings = overrideVoiceSettings;
    }

    /**
     * Gets the section settings for this voice section.  If the 
     * overrideSectionSettings flag is false, a read-only copy of the section's
     * settings will be returned.  Otherwise, a local, editable copy will be
     * returned.
     * 
     * @return the section settings for this voice section
     */
    public SectionSettings getSectionSettings() {
        if (!this.getOverrideSectionSettings()) return this.getSection().getSettings().getReadOnlyCopy();
        return sectionSettings;
    }
    
    private void setSectionSettings(SectionSettings sectionSettings) {
        if (this.sectionSettings != null) this.sectionSettings.removeSubscriber(this);
        this.sectionSettings = sectionSettings;
        if (this.sectionSettings != null) this.sectionSettings.addSubscriber(this);
    }

    /**
     * Gets the voice settings for this voice section.  If the 
     * overrideVoiceSettings flag is false, a read-only copy of the voice's
     * settings will be returned.  Otherwise, a local, editable copy will be
     * returned.
     * 
     * @return the voice settings for this voice section
     */
    public VoiceSettings getVoiceSettings() {
        if (!this.getOverrideVoiceSettings()) return this.getVoice().getSettings().getReadOnlyCopy();
        return voiceSettings;
    }       
    
    private void setVoiceSettings(VoiceSettings voiceSettings) {
        if (this.voiceSettings != null) this.voiceSettings.removeSubscriber(this);
        this.voiceSettings = voiceSettings;
        if (this.voiceSettings != null) this.voiceSettings.addSubscriber(this);
    }

    /**
     * Gets whether or not to make this VoiceSection one long rest.  This
     * overrides all other settings. 
     * 
     * @return whether or not to make this VoiceSection one long rest
     */
    public boolean getRest() {
        return rest;
    }
    
    /**
     * Sets whether or not to make this VoiceSection one long rest.  This
     * overrides all other settings. 
     * 
     * @param val whether or not to make this VoiceSection one long rest
     */
    public void setRest(boolean val) {
        if (val != this.rest) clearVoiceSectionResult();
        this.rest = val;
    }

    /**
     * Creates a hash map key using the Voice and Section for this VoiceSection.
     * @return the hash map key
     */
    protected VoiceSectionHashMapKey createHashMapKey() {
        return new VoiceSectionHashMapKey(this.getVoice(), this.getSection());
    }
    
    /**
     * Gets the result of applying this VoiceSection's settings to the germ.
     * 
     * @return a NoteList containing the result of applying the settings to the 
     *         germ
     */
    public NoteList getVoiceSectionResult() {
        if (voiceSectionResult == null) voiceSectionResult = this.generateVoiceSectionResult();
        voiceSectionResult.setInstrument(Instrument.getInstrument(this.getVoice().getInstrumentName()));
        return voiceSectionResult;
    }
    
    /**
     * Creates the output manager for this voice section.
     * 
     * @return the output manager
     * @throws com.myronmarston.music.GermIsEmptyException if the germ is empty
     */
    public OutputManager createOutputManager() throws GermIsEmptyException {                        
        Fraction sectionDuration = this.getSection().getDuration();
        NoteList result = this.getLengthenedVoiceSectionResult(sectionDuration);
                
        return new OutputManager(this.getVoice().getFractalPiece(), Arrays.asList(result));
    }
  
    /**
     * Returns the voice section result, set to a particular length by padding 
     * it with repeats and/or rests as appropriate.
     * 
     * @param length the length to set the voice section to
     * @return the voice section result, set to the given length
     */
    public NoteList getLengthenedVoiceSectionResult(Fraction length) {
        // get a clone of the result, so we can modify the clone rather than the original result.
        NoteList temp = this.getVoiceSectionResult().clone();
        Fraction originalVoiceSectionLength = temp.getDuration();
        if (originalVoiceSectionLength.compareTo(length) > 0) {
            throw new IllegalArgumentException(String.format("The voice section length (%f) is longer than the passed argument (%f).  The passed argument must be greater than or equal to the voice section length.", originalVoiceSectionLength.asDouble(), length.asDouble()));
        }
                        
        if (temp.getDuration().compareTo(0) > 0) { // only do this if we have something...
            // pad the length with additional copies of the entire voice section 
            // while there is space left...
            while (temp.getDuration().plus(originalVoiceSectionLength).compareTo(length) <= 0) {
                temp.addAll(this.getVoiceSectionResult());
            }
        }        
        
        // fill in the rest of the length with a rest...
        if (temp.getDuration().compareTo(length) < 0) {
            temp.add(Note.createRest(length.minus(temp.getDuration())));
        }
        
        assert temp.getDuration().equals(length) : temp;
        return temp;
    }
    
    /**
     * Sets the voiceSectionResult field to null.  Should be called anytime a field
     * that affects the voiceSectionResult changes. 
     */
    protected void clearVoiceSectionResult() {
        this.voiceSectionResult = null;
    }
    
    /**
     * Generates the NoteList containing the result of applying this 
     * VoiceSection's settings to the germ.
     * 
     * @return a NoteList containing the result of applying the settings to the 
     *         germ
     */
    private NoteList generateVoiceSectionResult() {
        NoteList clonedGerm = this.getSection().getGermForSection().clone(); 
        Scale sectionScale = this.getSection().getScale();
        Scale scaleToUse = (sectionScale == null ? this.getSection().getFractalPiece().getScale() : sectionScale);
        clonedGerm.updateScale(scaleToUse);
        NoteList temp = null;
        
        if (this.getRest()) {            
            // scale the duration according to the speed of this voice...
            Fraction duration = clonedGerm.getDuration();            
            duration = duration.dividedBy(this.getVoiceSettings().getSpeedScaleFactor());
            
            // create a note list of a single rest, the duration of the germ            
            temp = new NoteList();
            if (duration.compareTo(0L) > 0) temp.add(Note.createRest(duration));                                    
        } else {
            temp = this.getSectionSettings().applySettingsToNoteList(clonedGerm, scaleToUse);
            temp = this.getVoiceSettings().applySettingsToNoteList(temp, scaleToUse);               
        }
                
        temp.setSourceVoiceSectionOnAllNotes(this);
        return temp;
    }        

    public void publisherNotification(Publisher p, Object args) {   
        assert p == this.sectionSettings || p == this.voiceSettings : p;        
        this.clearVoiceSectionResult();
    }

    @Override
    public VoiceSection clone() throws CloneNotSupportedException {
        VoiceSection clone = (VoiceSection) super.clone();
        if (clone.getSectionSettings() != null) clone.setSectionSettings(clone.getSectionSettings().clone());
        if (clone.getVoiceSettings() != null) clone.setVoiceSettings(clone.getVoiceSettings().clone());
        return clone;
    }
   
}