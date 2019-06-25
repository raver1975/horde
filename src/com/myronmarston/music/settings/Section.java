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
import com.myronmarston.music.NoteList;
import com.myronmarston.music.NoteStringParseException;
import com.myronmarston.music.OutputManager;
import com.myronmarston.music.scales.InvalidKeySignatureException;
import com.myronmarston.music.scales.KeySignature;
import com.myronmarston.music.scales.Scale;
import com.myronmarston.util.Fraction;
import com.myronmarston.util.Publisher;
import org.simpleframework.xml.*;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The entire fractal piece is composed of a series of sections.  Each section
 * represents a group of bars of the piece of music, e.g., bars 8-15. 
 * 
 * @author Myron
 */
@Root
public class Section extends AbstractVoiceOrSection<Section, Voice> {    
    
    @Element
    private final SectionSettings settings;
    
    @Element(required=false)
    private Scale scale;
    
    @Attribute
    private boolean overridePieceScale = false;   
        
    private NoteList germForSection;
    
    /**
     * Constructor.
     * 
     * @param fractalPiece the FractalPiece this section is a part of
     * @param uniqueIndex the unique index for this section
     */
    protected Section(FractalPiece fractalPiece, int uniqueIndex) {
        super(fractalPiece, uniqueIndex);
        settings = new SectionSettings();
        settings.addSubscriber(this);
    }     
    
    /**
     * Provided for xml deserialization.
     */
    private Section() {
        this(null, 0);
    }

    /**
     * Gets the settings for this section.
     * 
     * @return the settings for this section
     */
    public SectionSettings getSettings() {
        return settings;
    } 
    
    /**
     * Gets a value indicating whether or not this section will override the 
     * fractal piece's scale.
     * 
     * @return false if the default scale will be used, true if the scale will 
     *         be overriden
     */
    public boolean getOverridePieceScale() {
        return overridePieceScale;
    }

    /**
     * Sets a value indicating whether or not this section will override the 
     * fractal piece's scale.
     * 
     * @param overridePieceScale false if the default scale will be used, true 
     *        if the scale will be overriden
     */
    public void setOverridePieceScale(boolean overridePieceScale) {
        boolean valueChanged = (this.overridePieceScale != overridePieceScale);
        
        this.overridePieceScale = overridePieceScale;
        
        // if the value changed, update our scale appropriately...
        if (valueChanged) {
            if (overridePieceScale) {
                this.setScale(this.getFractalPiece().getScale().clone());   
            } else {
                this.setScale(null);
            }            
        }        
    }
    
    /**
     * Gets the scale to be used by this section.
     * 
     * @return the scale to be used by this section
     */
    public Scale getScale() {
        return scale;
    }

    /**
     * Sets the scale to be used by this section.
     * 
     * @param scale the scale to be used by this section
     * @throws UnsupportedOperationException if the scale is changing in a way 
     *         that would violate the overridePieceScale setting      
     */
    public void setScale(Scale scale) throws UnsupportedOperationException {
        if (this.getOverridePieceScale()) {
            if (scale == null) throw new UnsupportedOperationException("The scale cannot be set to null since overridePieceScale is true.");
        } else {
            if (scale != null) throw new UnsupportedOperationException("The scale cannot be changed since overridePieceScale is false.");
        }        
        
        this.scale = scale;
        this.clearVoiceSectionResults();
        this.clearCachedGermForSection();
    }

    /**
     * Gets the germ to use for this section.  Usually this will be the Fractal
     * Piece's germ, but if the scale is overriden with a scale containing 
     * different number of scale steps from the FractalPiece scale, the germ
     * will be reparsed.
     * 
     * @return the germ to use for this section
     */
    public NoteList getGermForSection() {
        if (!this.getOverridePieceScale() || 
            this.getScale().getScaleStepArray().length == this.getFractalPiece().getScale().getScaleStepArray().length) {
            // There is no reason to use a different scale here, as we are either
            // not overriding the scale, or using a scale with the same number of
            // scale steps.  It'll work fine to just use the germ set on the whole piece.
            return this.getFractalPiece().getGerm();
        }

        // We are using a different scale, and we have a different number of 
        // scale steps as the whole piece's scale.  The FractalPiece's germ
        // won't work well for this section, so we should reparse the germ
        // using our Section scale.
        if (germForSection == null) germForSection = this.parseGermForSection();
        return germForSection;
    }        
    
    /**
     * Parses the FractalPiece's germ using this section's scale.  
     * 
     * @return the re-parsed germ
     */
    private NoteList parseGermForSection() {
        assert this.getOverridePieceScale() : "parseGermForSection should only be called when the piece scale is being overriden.";
        assert this.getScale().getScaleStepArray().length != this.getFractalPiece().getScale().getScaleStepArray().length : "parseGermForSection should only be called when the piece scale and section scale have a different number of scale steps.";
        
        // It's a little ambiguous what it means to have a different scale for a section.
        // I could spell this out in the instructions of the webpage, but that would
        // only add complexity and confusion.
        // Does the user mean to reparse the germ using the selected scale, but the 
        // original tonic?  Or reparse the scale using the selected tonic, too?
        // We try to intelligently guess here based on the number of accidentals
        // that are produced.
        
        String germString = this.getFractalPiece().getGermString();
        if (germString.isEmpty()) return new NoteList();
        try {
            // first try parsing the germ using the selected scale and tonic...            
            NoteList testGerm1 = NoteList.parseNoteListString(germString, this.getScale());                                
            int testGerm1AccidentalCount = testGerm1.getNumberOfAccidentals();
            if (testGerm1AccidentalCount == 0) return testGerm1;
            
            // We got accidentals; try parsing the germ using the selected scale 
            // type, but the original tonic
            Scale scale2 = null;            
            NoteList testGerm2 = null;
            int testGerm2AccidentalCount = 0;
            try {
                scale2 = this.getScale().getCopyWithDifferentKey(this.getFractalPiece().getScale().getKeyName());
                testGerm2 = NoteList.parseNoteListString(germString, scale2);                                
                testGerm2AccidentalCount = testGerm2.getNumberOfAccidentals();
                if (testGerm2AccidentalCount == 0) return testGerm2;
            } catch (InvalidKeySignatureException ivksex) {
                // there's no need to raise an exception here; we'll just keep trying other possibilities
                
                // set the accidental count to a value that will ensure testGerm2
                // will never have the fewest accidentals
                testGerm2AccidentalCount = Integer.MAX_VALUE;                                
            }                                                               
            
            // We still have accidentals; try parsing the germ using the 
            // selected  scale type, but the relative of the original tonic
            Scale scale3 = null;            
            NoteList testGerm3 = null;
            int testGerm3AccidentalCount = Integer.MAX_VALUE;
            try {
                scale3 = this.getScale().getCopyWithDifferentKey(this.getFractalPiece().getScale().getKeySignature().getKeyNameWithSameNumAccidentals(this.getScale().getKeySignature().getTonality()));                
                testGerm3 = NoteList.parseNoteListString(germString, scale3);                                
                testGerm3AccidentalCount = testGerm3.getNumberOfAccidentals();
                if (testGerm3AccidentalCount == 0) return testGerm3;
            } catch (InvalidKeySignatureException iksex) {
                // there's no need to raise an exception here; we'll just keep trying other possibilities
                
                // set the accidental count to a value that will ensure testGerm3
                // will never have the fewest accidentals
                testGerm3AccidentalCount = Integer.MAX_VALUE;                                
            }
            
            // all three possibilities have accidentals; just choose the one
            // with the fewest accidentals
            if (testGerm1AccidentalCount <= testGerm2AccidentalCount && testGerm1AccidentalCount <= testGerm3AccidentalCount) {
                return testGerm1;
            } else if (testGerm2AccidentalCount <= testGerm3AccidentalCount) {
                assert testGerm2AccidentalCount <= testGerm1AccidentalCount : "testGerm2 had more accidentals than testGerm1.";
                return testGerm2;
            } else {
                assert testGerm3AccidentalCount <= testGerm1AccidentalCount : "testGerm3 had more accidentals than testGerm1.";
                assert testGerm3AccidentalCount <= testGerm2AccidentalCount : "testGerm3 had more accidentals than testGerm2.";
                return testGerm3;
            }
        } catch (NoteStringParseException ex) {                
            // All scales should be able to handle a valid note list string.
            // if we have a germString, it was valid with the existing scale,
            // so it should also be valid with this scale.  We should only
            // get a NoteStringParseException in the case of a programming
            // error.
            throw new UndeclaredThrowableException(ex, "An error occured while parsing the note list string '" + germString + ".  This indicates a programming error.");        
        }        
    }

    /**
     * Clears the cached germForSection.  Should be called anytime a value that
     * effects germForSection is changed.
     */
    protected void clearCachedGermForSection() {
        this.germForSection = null;
        
        // the germForSection effects the voice section resutls, so clear that, too...
        this.clearVoiceSectionResults();
    }
    
    /**
     * Gets the key signature for this section.
     * 
     * @return the key signature for this section
     */
    public KeySignature getSectionKeySignature() {
        return (this.getScale() == null ? 
            this.getFractalPiece().getScale().getKeySignature() : 
            this.getScale().getKeySignature());
    }
    
    /**
     * Updates the self-similarity settings of all voice sections.  This also
     * updates the overrideVoiceSettings property to true.
     * 
     * @param applyToPitch new value
     * @param applyToRhythm new value
     * @param applyToVolume new value
     * @param selfSimilarityIterations new value
     */
    public void setSelfSimilaritySettingsOnAllVoiceSections(boolean applyToPitch, boolean applyToRhythm, boolean applyToVolume, int selfSimilarityIterations) {
        for (VoiceSection vs : this.getVoiceSections()) {
            vs.setOverrideVoiceSettings(true);
            SelfSimilaritySettings sss = vs.getVoiceSettings().getSelfSimilaritySettings();
            
            sss.setApplyToPitch(applyToPitch);
            sss.setApplyToRhythm(applyToRhythm);
            sss.setApplyToVolume(applyToVolume);
            sss.setSelfSimilarityIterations(selfSimilarityIterations);
        }        
    } 
    
    /**
     * Gets the duration for this entire section.  This will be the duration of
     * the longest voice section.
     * 
     * @return the duration of this entire section
     */
    @SuppressWarnings("unchecked")
    public Fraction getDuration() {
        ArrayList<Fraction> voiceSectionDurations = new ArrayList<Fraction>(this.getListOfOtherType().size());
        
        for (VoiceSection vs : this.getVoiceSections()) {
            voiceSectionDurations.add(vs.getVoiceSectionResult().getDuration());
        }
        
        return Collections.max(voiceSectionDurations);
    }    
    
    public OutputManager createOutputManager() throws GermIsEmptyException {                
        Fraction sectionDuration = this.getDuration();
                    
        List<NoteList> voiceSectionResults = new ArrayList<NoteList>(this.getVoiceSections().size());
        for (VoiceSection vs : this.getVoiceSections()) {
            voiceSectionResults.add(vs.getLengthenedVoiceSectionResult(sectionDuration));
        }
        
        return new OutputManager(this.getFractalPiece(), voiceSectionResults);
    }

    public void publisherNotification(Publisher p, Object args) {
        assert p == this.getSettings() : p;
        this.clearVoiceSectionResults();
    }          
    
    @Override
    protected VoiceOrSectionList<Section, Voice> getListOfMainType() {
        return this.getFractalPiece().getSections();
    }
    
    @Override
    protected VoiceOrSectionList<Voice, Section> getListOfOtherType() {
        return this.getFractalPiece().getVoices();
    }    

    @Override
    protected VoiceSectionHashMapKey getHashMapKeyForOtherTypeIndex(int index) {
        Voice indexedVoice = this.getFractalPiece().getVoices().get(index);
        return new VoiceSectionHashMapKey(indexedVoice, this);
    }

    @Override
    protected VoiceSectionHashMapKey getHashMapKeyForOtherTypeUniqueIndex(int uniqueIndex) {
        Voice indexedVoice = this.getFractalPiece().getVoices().getByUniqueIndex(uniqueIndex);
        return new VoiceSectionHashMapKey(indexedVoice, this);
    }        

    @Override
    protected VoiceSection instantiateVoiceSection(Voice vOrS) {
        return new VoiceSection(vOrS, this);
    }
       
}
