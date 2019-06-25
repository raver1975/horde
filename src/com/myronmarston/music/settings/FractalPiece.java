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
import com.myronmarston.music.Tempo;
import com.myronmarston.music.scales.Scale;
import com.myronmarston.util.Fraction;
import org.simpleframework.xml.*;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.CycleStrategy;

import java.io.StringWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * The GrandDaddy of them all.  This class controls the entire piece of music.
 * Before generating the piece, you will need to provide values for each
 * of the appropriate settings: the voices (via getVoices()), the sections 
 * (via getSections()), the germ, the scale, etc.
 * 
 * @author Myron
 */
@Root
public class FractalPiece {    
    private static interface InsertIndexProvider { int getInsertIndex(List l); }
       
    @Element
    private NoteList germ = new NoteList().getReadOnlyCopy();
    
    @Attribute
    private String germString = "";
    
    @Attribute
    private int tempo = Tempo.DEFAULT;
    
    @Element
    private Scale scale = Scale.DEFAULT;
    
    @Element
    private TimeSignature timeSignature = TimeSignature.DEFAULT;
    
    @Element
    private VoiceOrSectionList<Voice, Section> voices = new VoiceOrSectionList<Voice, Section>(this);
    
    @Element
    private VoiceOrSectionList<Section, Voice> sections = new VoiceOrSectionList<Section, Voice>(this);
    
    @Element
    private Map<VoiceSectionHashMapKey, VoiceSection> voiceSections = new VoiceSectionHashMap();
       
    @Attribute
    private boolean generateLayeredIntro = true;
    
    @Attribute
    private boolean generateLayeredOutro = true;        
        
    private List<Section> tempIntroOutroSections = new ArrayList<Section>();    
                                    
    /**
     * Returns the germ NoteList.  Guarenteed to never be null.  Is read-only.  
     * The germ is the short melody from which the entire piece is generated.
     * 
     * @return the germ NoteList
     */
    public NoteList getGerm() {   
        assert germ != null : germ; // germ should never be null!
        
        // the germ should be read-only because we only support changing it
        // through the setGermString() method.  Our section cached germs
        // must be updated when the germ changes, and it's easiest to only
        // do that the germ string changes
        assert germ.isReadOnly();
        return germ;
    }

    /**
     * Gets the germ string.
     * 
     * @return the germ string
     */
    public String getGermString() {
        assert germString != null : germString; // germString should never be null!
        return germString;
    }        
    
    /**
     * Sets the notes for the germ.
     * 
     * @param germString string containing a list of notes
     * @throws com.myronmarston.music.NoteStringParseException if the note list
     *         string cannot be parsed
     */
    public void setGermString(String germString) throws NoteStringParseException {        
        this.germ = NoteList.parseNoteListString(germString, this.getScale()).getReadOnlyCopy();
        this.germString = germString;
        
        // the germ string effects each section's germ for section, so clear them...
        for (Section section : this.getSections()) {
            section.clearCachedGermForSection();
        }
    }

    /**
     * Gets the tempo of the piece, in beats per minute.
     * 
     * @return the tempo of the piece
     */
    public int getTempo() {
        return tempo;
    }

    /**
     * Sets the tempo of the piece, in beats per minute.
     * 
     * @param tempo the tempo of the piece
     * @throws IllegalArgumentException if the tempo is outside of the 
     *         acceptable range
     */
    public void setTempo(int tempo) throws IllegalArgumentException {
        Tempo.checkTempoValidity(tempo);
        this.tempo = tempo;
    }
        
    /**
     * Returns the Scale.  The Scale is used to determine the tonality of the
     * piece, and will also be used to set the key signature of the Midi 
     * sequence.
     * 
     * @return the Scale used by this FractalPiece
     */
    public Scale getScale() {
        assert scale != null : scale; // scale should never be null!
        return scale;
    }

    /**
     * Sets the Scale used by this FractalPiece.  The Scale is used to 
     * determine the tonality of the piece, and will also be used to set the 
     * key signature of the Midi sequence.
     * 
     * @param scale the Scale to be used by this FractalPiece
     * @throws IllegalArgumentException if the passed scale is null
     */
    public void setScale(Scale scale) throws IllegalArgumentException {
        if (scale == null) throw new IllegalArgumentException("Scale cannot be set to null.");        
        
        if (this.getGermString() != null && !this.getGermString().isEmpty()) {
            try {
                this.germ = NoteList.parseNoteListString(this.getGermString(), scale).getReadOnlyCopy();
            } catch (NoteStringParseException ex) {                
                // All scales should be able to handle a valid note list string.
                // if we have a germString, it was valid with the existing scale,
                // so it should also be valid with this scale.  We should only
                // get a NoteStringParseException in the case of a programming
                // error.
                throw new UndeclaredThrowableException(ex, "An error occured while parsing the note list string '" + this.getGermString() + "' using the scale " + scale.toString() + ".  This indicates a programming error.");        
            }
        }                   
        
        this.scale = scale;                
        
        // the scale effects each section's germ for section, so clear them...
        for (Section section : this.getSections()) {
            section.clearCachedGermForSection();
        }
    }

    /**
     * Gets whether or not a layered intro should be included in the generated
     * fractal piece.
     * 
     * @return whether or not to generate the layered intro
     */
    public boolean getGenerateLayeredIntro() {
        return generateLayeredIntro;
    }

    /**
     * Sets whether or not a layered intro should be included in the generated
     * fractal piece.
     * 
     * @param generateLayeredIntro whether or not to generate the layered intro
     */
    public void setGenerateLayeredIntro(boolean generateLayeredIntro) {
        this.generateLayeredIntro = generateLayeredIntro;
    }

    /**
     * Gets whether or not a layered outro should be included in the generated
     * fractal piece.
     * 
     * @return whether or not to generate the layered outro
     */
    public boolean getGenerateLayeredOutro() {
        return generateLayeredOutro;
    }

    /**
     * Sets whether or not a layered outro should be included in the generated
     * fractal piece.
     * 
     * @param generateLayeredOutro whether or not to generate the layered outro
     */
    public void setGenerateLayeredOutro(boolean generateLayeredOutro) {
        this.generateLayeredOutro = generateLayeredOutro;
    }
        
    /**
     * Gets the time signature for this piece.  If none has been set, a default
     * signature of 4/4 will be created.
     * 
     * @return the time signature
     */
    public TimeSignature getTimeSignature() {  
        assert timeSignature != null : timeSignature; // timeSignature should never be null...
        return timeSignature;
    }

    /**
     * Sets the time signature for this piece.
     * 
     * @param timeSignature the time signature
     */
    public void setTimeSignature(TimeSignature timeSignature) {
        if (timeSignature == null) throw new IllegalArgumentException("TimeSignature cannot be set to null.");
        this.timeSignature = timeSignature;
    }
    
    /**
     * Gets the hash table containing all the VoiceSections for the entire
     * piece.  
     * 
     * @return the hash table containing all VoiceSections
     */
    protected Map<VoiceSectionHashMapKey, VoiceSection> getVoiceSections() {
        return voiceSections;
    }
    
    /**
     * Gets a list of Voices for the FractalPiece.  To add a Voice, use the 
     * provided createVoice() method, rather than attempting to add it to the
     * list on your own.
     * 
     * @return the list of Voices
     */
    public VoiceOrSectionList<Voice, Section> getVoices() {        
        assert voices != null : voices; // voices should never be null!
        return voices;
    }
    
    /**
     * Gets an unmodifiable list of voices, in order from the fastest to the
     * slowest.
     * 
     * @return an unmodifiable list
     */
    private List<Voice> getVoices_FastToSlow() {
        List<Voice> sortableVoiceList = new ArrayList<Voice>(this.getVoices());  
        
        // sort the list using a fast-to-slow comparator...
        Collections.sort(sortableVoiceList, new Comparator<Voice>() {
            public int compare(Voice v1, Voice v2) {        
                return v2.getSettings().getSpeedScaleFactor().compareTo(v1.getSettings().getSpeedScaleFactor());
            }
        });
        
        return Collections.unmodifiableList(sortableVoiceList);
    }
        
    /**
     * Gets a list of Sections for the FractalPiece.  To add a Section, use the
     * provided createSection() method, rather than attempting to add it to the
     * list on your own.
     * 
     * @return the list of Sections
     */
    public VoiceOrSectionList<Section, Voice> getSections() {  
        assert sections != null : sections; // sections should never be null!
        return sections;
    }
    
    /**
     * Normalizes the unique indices so as to label the items in natural order.
     */
    public void normalizeUniqueIndices() {
        this.voices.normalizeUniqueIndices();
        this.sections.normalizeUniqueIndices();
    }
    
    /**
     * Creates a Voice for the FractalPiece, and adds it to the Voice list.
     * 
     * @return the created Voice
     */
    public Voice createVoice() {
        return this.createVoice(this.getVoices().size());
    }
    
    /**
     * Creates a Voice for the FractalPiece, and adds it to a particular point 
     * in the voice list.
     * 
     * @param index the point to insert the voice
     * @return the created voice
     * @throws UnsupportedOperationException if there are more than 15 voices
     */
    public Voice createVoice(int index) throws UnsupportedOperationException {   
        if (this.voices.size() > 15) throw new UnsupportedOperationException("You cannot create more than 16 voices, since Midi only supports 16 channels.");
        Voice v = new Voice(this, this.voices.getNextUniqueIndex());
        this.voices.add(index, v);
        return v;
    }
    
    /**
     * Creates a Section for the FractalPiece, and adds it to the Section list.
     * 
     * @return the created Section
     */
    public Section createSection() {
        return this.createSection(this.getSections().size());
    }   
    
    /**
     * Creates a Section for the FractalPiece, and inserts it at a particular
     * point in the Section list.
     * 
     * @param index the point in the list to insert the section
     * @return the created Section
     */
    public Section createSection(int index) {
        Section s = new Section(this, this.sections.getNextUniqueIndex());
        this.sections.add(index, s);        
        return s;
    }
    
    /**
     * Sets up the default settings.  If there are already voices, they will be 
     * left alone and no new voices will be created.  Any section settings will
     * be overriden with new ones.
     */
    public void createDefaultSettings() {
        createDefaultVoices();
        createDefaultSections();
    }
    
    /**
     * Creates the default voice settings.  This generates three voices.  The
     * highest and fastest voice will have self-similarity applied to the
     * pitch and volume.
     */
    public void createDefaultVoices() {        
        this.voices.clear();
        
        createDefaultVoice(1, new Fraction(2, 1), true, false, true, 1);
        createDefaultVoice(0, new Fraction(1, 1), false, false, false, 1);
        createDefaultVoice(-1, new Fraction(1, 2), false, false, false, 1);          
    }
        
    private void createDefaultVoice(int octaveAdjustment, Fraction speedScaleFactor, boolean applySelfSimilarityToPitch, boolean applySelfSimilarityToRhythm, boolean applySelfSimilarityToVolume, int selfSimilarityIterations) {
        Voice v = this.createVoice();
        v.getSettings().setOctaveAdjustment(octaveAdjustment);
        v.getSettings().setSpeedScaleFactor(speedScaleFactor);        
                
        SelfSimilaritySettings sss = v.getSettings().getSelfSimilaritySettings();
        sss.setApplyToPitch(applySelfSimilarityToPitch);                
        sss.setApplyToRhythm(applySelfSimilarityToRhythm);
        sss.setApplyToVolume(applySelfSimilarityToVolume);  
        sss.setSelfSimilarityIterations(selfSimilarityIterations);
    }
    
    /**
     * Sets up default section settings.  Any existing sections will be 
     * overriden.  This generates one normal section, one inversion section,
     * one retrograde inversion section, and one retrograde section.  For each
     * section, the self-similarity is applied to only the fastest voice.
     */
    public void createDefaultSections() {
        this.sections.clear();
        
        createDefaultSection(false, false); // normal
        createDefaultSection(true, false);  // inversion
        createDefaultSection(true, true);   // retrograde inversion     
        createDefaultSection(false, true);  // retrograde       
    }
    
    /**
     * Creates a default section. 
     *
     * @param applyInversion whether or not to apply inversion to this section
     * @param applyRetrograde whether or not to apply retrograde to this section
     */
    private void createDefaultSection(boolean applyInversion, boolean applyRetrograde) {       
        Section section = this.createSection();
        
        // apply inversion and retrograde based on the passed settings...
        section.getSettings().setApplyInversion(applyInversion);
        section.getSettings().setApplyRetrograde(applyRetrograde);    
    }
    
    /**
     * Creates the layered intro sections.
     */
    protected void createIntroSections() {
        if (!this.getGenerateLayeredIntro()) return;
        
        createLayeredSections(
            new InsertIndexProvider() { 
                public int getInsertIndex(List l) { return 0; }                
            }
        );
    }
    
    /**
     * Creates the layered outro sections.
     */
    protected void createOutroSections() {
        if (!this.getGenerateLayeredOutro()) return;
        
        createLayeredSections(
            new InsertIndexProvider() {                 
                public int getInsertIndex(List l) { return l.size(); }                
            }
        );
    }
        
    /**
     * Creates the necessary layered intro or outro sections.
     * 
     * @param insertIndexProvider object that provides index into the start of
     *        the list (for the intro) or the end of the list (for the outro)
     */
    private void createLayeredSections(InsertIndexProvider insertIndexProvider) {
        List<Voice> fastToSlowVoices = this.getVoices_FastToSlow();
        
        for (int sectionIndex = 0; sectionIndex < fastToSlowVoices.size(); sectionIndex++) {
            // create the section at the appropriate index...
            Section s = this.createSection(insertIndexProvider.getInsertIndex(this.getSections()));
            
            // add our section to our temp list, since the layered sections are
            // only created during fractal piece generation and should never be
            // available for editing
            this.tempIntroOutroSections.add(s);    
            
            // set defaults...
            s.getSettings().setApplyInversion(false);
            s.getSettings().setApplyRetrograde(false);
            s.setSelfSimilaritySettingsOnAllVoiceSections(false, false, false, 1);
            s.setRestOnAllVoiceSections(false);
            
            // set some of the voice sections to rests, to create our layered effect...
            for (int voiceIndex = 0; voiceIndex < sectionIndex; voiceIndex++) {  
                VoiceSection vs = this.getVoiceSections().get(new VoiceSectionHashMapKey(fastToSlowVoices.get(voiceIndex), s));
                vs.setRest(true);
            }                        
        }
    }            
    
    /**
     * Clears out any temporary intro or outro sections.  These are created 
     * during fractal piece generation and should not be available the rest of 
     * the time.
     * 
     * @param sectionLastUniqueIndex the lastUniqueIndex to set on the sections
     */
    protected void clearTempIntroOutroSections(int sectionLastUniqueIndex) {
        // clear out any sections that were temporarily created...
        for (Section s : this.tempIntroOutroSections) {
            this.getSections().remove(s);
        }
        this.tempIntroOutroSections.clear();
        this.getSections().setLastUniqueIndex(sectionLastUniqueIndex);
    }                      
    
    /**
     * Creates the output manager for the whole piece.
     * 
     * @return the output manager
     * @throws com.myronmarston.music.GermIsEmptyException if the germ is empty
     * @throws UnsupportedOperationException if there are no voices or sections
     */
    public OutputManager createPieceResultOutputManager() throws GermIsEmptyException, UnsupportedOperationException {
        if (this.voices.isEmpty() || this.sections.isEmpty()) throw new UnsupportedOperationException("You must have at least one voice and one section to generate a fractal piece.");
        int originalSectionUniqueIndex = this.sections.getLastUniqueIndex();
        try {
            // create our intro and outro...
            this.createIntroSections();
            this.createOutroSections();
                        
            ArrayList<NoteList> voiceResults = new ArrayList<NoteList>();
            for (Voice v : this.getVoices()) voiceResults.add(v.getEntireVoice());
            
            return new OutputManager(this, voiceResults);
        } finally {
            this.clearTempIntroOutroSections(originalSectionUniqueIndex);
        }         
    }
    
    /**
     * Creates the output manager for the germ.
     * 
     * @return the output manager
     * @throws com.myronmarston.music.GermIsEmptyException if the germ is empty
     */
    public OutputManager createGermOutputManager() throws GermIsEmptyException {        
        return new OutputManager(this, Arrays.asList(this.getGerm()), false, false, false);
    }
   
    /**
     * Creates and loads a fractal piece from a serialized xml string.
     * 
     * @param xml string containing an xml representation of the fractal piece
     * @return the new fractal piece
     * @throws Exception if there is a deserialization error
     */
    public static FractalPiece loadFromXml(String xml) throws Exception {        
        Serializer serializer = new Persister(new CycleStrategy());
        return serializer.read(FractalPiece.class, xml);        
    }
       
    /**
     * Serializes the fractal piece to xml.
     * 
     * @return the xml representation of the fractal piece
     */
    public String getXmlRepresentation() {
        Serializer serializer = new Persister(new CycleStrategy());
        StringWriter xml = new StringWriter();
        
        try {
          serializer.write(this, xml);    
        } catch (Exception ex) {
            // Our serialization annotations and accompanying code should prevent
            // this from ever occuring; if it does it is a programming error.
            // We do this so that we don't have to declare it as a checked exception.
            throw new UndeclaredThrowableException(ex, "An error occurred while serializing the fractal piece to xml.");
        }        
        
        return xml.toString();
    }
}
