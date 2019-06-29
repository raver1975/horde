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

package com.myronmarston.music.transformers;

import com.myronmarston.music.MidiNote;
import com.myronmarston.music.Note;
import com.myronmarston.music.NoteList;
import com.myronmarston.music.settings.SelfSimilaritySettings;
import com.myronmarston.util.Fraction;

/**
 * Transformer that applies the self-similarity algorithm to the given NoteList.
 * This can apply to the pitch, drums and/or volume, depending on the
 * SelfSimilaritySettings.  
 * Example: G5 A5 B5 G5 -> G5 A5 B5 G5, A5 B5 C6 A5, B5 C6 D6 B5, G5 A5 B5 G5
 * 
 * @author Myron
 */
public class SelfSimilarityTransformer implements Transformer {   
    private SelfSimilaritySettings settings;        

    /**
     * Gets the self-similarity settings to be used by this transformer.  
     * Guarenteed to never be null.  The settings determine whether or not 
     * self-similarity is applied to the pitch, drums and/or volume.
     * 
     * @return the self-similarity settings
     */
    public SelfSimilaritySettings getSettings() {
        if (settings == null) settings = new SelfSimilaritySettings(true, true, true, 1);
        return this.settings;
    }
    
    /**
     * Constructor.
     * 
     * @param settings the self-similarity settings to use
     */
    public SelfSimilarityTransformer(SelfSimilaritySettings settings) {
        this.settings = settings;
    }         
    
    /**
     * Constructor.  Use this constructor to provide values for the 
     * self-similarity settings.
     * 
     * @param applyToPitch true to apply self-similarity to the pitch
     * @param applyToRhythm true to apply self-similarity to the drums
     * @param applyToVolume true to apply self-similarity to the volume
     * @param selfSimilarityIterations number of times to apply self-similarity
     *        to the germ
     */
    public SelfSimilarityTransformer(boolean applyToPitch, boolean applyToRhythm, boolean applyToVolume, int selfSimilarityIterations) {
        this(new SelfSimilaritySettings(applyToPitch, applyToRhythm, applyToVolume, selfSimilarityIterations));
    }
    
    /**
     * Default Constructor.
     */
    public SelfSimilarityTransformer() {};
    
    public NoteList transform(NoteList input) {                
        if (!this.getSettings().selfSimilarityShouldBeAppliedToSomething()) {
            // there is no self-similarity, so just return a copy of the input
            return input.clone();            
        }            
             
        NoteList tempList = input;
        
        for (int i = 0; i < this.getSettings().getSelfSimilarityIterations(); i++) {
            tempList = transformOneLevel(input, tempList);
        }
        
        return tempList;
    }
    
    private NoteList transformOneLevel(NoteList germ, NoteList input) {           
        Note firstGermNote = germ.getFirstAudibleNote(); // the note we will compare against for the self-similarity
        NoteList transformedList; // used to store the temporary results of the transformations                
        NoteList output = new NoteList(germ.size() * input.size()); // the final output
                
        for (Note germNote : germ) {                  
            if (germNote.isRest()) {                
                transformedList = new NoteList();
                
                // the rest one will be one complete copy of the germ, all enclosed in a rest,
                // so we need to set the first note flag on it
                Note restNote = Note.createRest(input.getDuration());
                restNote.setIsFirstNoteOfGermCopy(true);
                
                transformedList.add(restNote);                
                transformedList = transform_rhythm(transformedList, firstGermNote, germNote);
            } else {                
                transformedList = transform_pitch(input, firstGermNote, germNote);
                transformedList = transform_rhythm(transformedList, firstGermNote, germNote);
                transformedList = transform_volume(transformedList, firstGermNote, germNote);
            }
            output.addAll(transformedList);            
        }
        
        return output;
    }

    private NoteList transform_pitch(NoteList input, Note firstNote, Note inputNote) {
        if (!this.getSettings().getApplyToPitch()) return input;
                                                   
        // transpose the input to the correct octave...
        OctaveTransformer octaveTransformer = new OctaveTransformer(inputNote.getOctave() - firstNote.getOctave());
        NoteList octaveTransformedList = octaveTransformer.transform(input);

        // transpose the input to the correct pitch level...        
        TransposeTransformer transposer = new TransposeTransformer(inputNote.getScaleStep() - firstNote.getScaleStep(), inputNote.getLetterNumber() - firstNote.getLetterNumber());
        NoteList transposedList = transposer.transform(octaveTransformedList);

        // set the segment chromatic adjustment on this note as necessary...
        int segmentChromaticAdjustment = inputNote.getChromaticAdjustment() - firstNote.getChromaticAdjustment();
        for (Note n : transposedList) n.setSegmentChromaticAdjustment(segmentChromaticAdjustment);
        
        return transposedList;
    }

    private NoteList transform_rhythm(NoteList input, Note firstNote, Note inputNote) {
        if (!this.getSettings().getApplyToRhythm()) return input;                
        
        // scale the drums...
        assert (inputNote.getDuration().compareTo(0) > 0) : inputNote.getDuration(); // we would get div-by-zero below if the duration is zero, and less than zero is nonsensical
        RhythmicDurationTransformer rhythmScaler = new RhythmicDurationTransformer(firstNote.getDuration().dividedBy(inputNote.getDuration()));
        return rhythmScaler.transform(input);
    }
    
    private NoteList transform_volume(NoteList input, Note firstNote, Note inputNote) {
        if (!this.getSettings().getApplyToVolume()) return input;
                        
        int remainingVolumeRange = // get the above or below volume range based on the volume of the current note relative to the first note
            (inputNote.getVolume() > firstNote.getVolume()) ? 
            MidiNote.MAX_VELOCITY - firstNote.getVolume() :
            firstNote.getVolume() - MidiNote.MIN_VELOCITY;
        
        // if there's no volume range left to use, we have no way to scale it...
        if (remainingVolumeRange == 0) return input;
        
        // Our input note volume should not be 0, or we will get a scale factor of -1,
        // which our volume transformer does not allow.  But the code that calls this
        // method should check for this, so this should never occur...
        assert inputNote.getVolume() != 0 : inputNote.getVolume();
        
        Fraction scaleFactor = new Fraction(inputNote.getVolume() - firstNote.getVolume(), remainingVolumeRange);
        VolumeTransformer volumeScaler = new VolumeTransformer(scaleFactor);
        return volumeScaler.transform(input);        
    }
}
