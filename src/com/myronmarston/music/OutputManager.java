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

import com.myronmarston.music.notation.*;
import com.myronmarston.music.scales.KeySignature;
import com.myronmarston.music.settings.*;
import com.myronmarston.util.Fraction;
import com.myronmarston.util.MathHelper;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that manages the outputs from FractalComposer, such as Midi files, WAV
 * files, Mp3 files, PDF files of scores, etc.
 * 
 * @author Myron
 */
public class OutputManager {    
    private final Piece pieceNotation;
    private Sequence sequence;
    private final FractalPiece fractalPiece;
    private final int tempo;
    private final Fraction timeSignatureFraction;
    private List<NoteList> noteLists;
    private SheetMusicCreator sheetMusicCreator;
    private AudioFileCreator audioFileCreator;
    private boolean testNotationError = false;
    private boolean generateKeySignaturesForSections;
    private String lastMidiFileName;
    private String lastGuidoFileName;
    private String lastWavFileName;
    private String lastMp3FileName;
    private String lastGifFileName;
    private String lastLilypondFileName;
    private String lastPdfFileName;
    private String lastPngFileName;
    
    private static final int MIDI_FILE_TYPE_FOR_MULTI_TRACK_SEQUENCE = 1;    
        
    /**
     * Gets the Midi sequence.
     * 
     * @return the midi sequence
     */
    public Sequence getSequence() {        
        return sequence;
    }
    
    /**
     * Gets the tempo of the music.
     * 
     * @return the tempo, in beats per minute
     */
    public int getTempo() {
        return tempo;
    }
    
    /**
     * Gets a flag that can be used to test that errors in lilypond properly 
     * raise java exceptions.
     * 
     * @return the testLilypondError flag
     */
    public boolean getTestNotationError() {
        return testNotationError;
    }

    /**
     * Sets a flag that can be used to test that errors in guido or lilypond 
     * properly raise java exceptions.
     * 
     * @param testNotationError the testNotationError flag
     */
    protected void setTestNotationError(boolean testNotationError) {
        this.testNotationError = testNotationError;
    }        

    /**
     * Gets the sheet music creator.
     * 
     * @return the sheet music creator
     */
    protected SheetMusicCreator getSheetMusicCreator() {
        if (sheetMusicCreator == null) sheetMusicCreator = new SheetMusicCreator(this);
        return sheetMusicCreator;
    }

    /**
     * Gets the audio file creator.
     * 
     * @return the audio file creator
     */
    public AudioFileCreator getAudioFileCreator() {
        if (audioFileCreator == null) audioFileCreator = new AudioFileCreator(this);
        return audioFileCreator;
    }        
    
    /**
     * Gets the collection of note lists that was used to generate the output.
     * 
     * @return the collection of note lists
     */
    public List<NoteList> getNoteLists() {
        return this.noteLists;
    }

    /**
     * Gets the fractal piece to generate the output.
     * 
     * @return the fractal piece
     */
    public FractalPiece getFractalPiece() {
        return fractalPiece;
    }

    /**
     * Gets the piece notation, which can be used to produce graphical notation
     * using GUIDO or Lilypond.
     * 
     * @return the piece notation
     */
    public Piece getPieceNotation() {
        return pieceNotation;
    }        
    
    /**
     * Constructor.  This automatically constructs the midi sequence and the
     * guido notation.  All aspects of the guido notation are included.
     * 
     * @param fractalPiece the fractal piece
     * @param noteLists collection of noteLists containing music
     * @throws com.myronmarston.music.GermIsEmptyException if the germ is empty
     */
    public OutputManager(FractalPiece fractalPiece, List<NoteList> noteLists) throws GermIsEmptyException {
        this(fractalPiece, noteLists, true, true, true);
    }

    /**
     * Constructor.  This automatically constructs the midi sequence and the
     * guido notation.
     * 
     * @param fractalPiece the fractal piece 
     * @param noteLists collection of noteLists containing music     
     * @param includeTempoOnSheetMusic whether or not to include a tempo marking
     *        on the produced sheet music
     * @param includeInstrumentOnSheetMusic whether or not to include 
     *        instrument markings on the produced sheet music
     * @param generateKeySignaturesForSections whether or not to generate seperate
     *        key signatures for each section
     * @throws com.myronmarston.music.GermIsEmptyException if the germ is empty
     */
    public OutputManager(FractalPiece fractalPiece, List<NoteList> noteLists, boolean includeTempoOnSheetMusic, boolean includeInstrumentOnSheetMusic, boolean generateKeySignaturesForSections) throws GermIsEmptyException {
        this.fractalPiece = fractalPiece;
        this.timeSignatureFraction = this.fractalPiece.getTimeSignature().toFraction();
        this.noteLists = noteLists;
        this.tempo = this.fractalPiece.getTempo();        
        this.pieceNotation = new Piece(this.fractalPiece.getScale().getKeySignature(), this.fractalPiece.getTimeSignature(), this.tempo, includeTempoOnSheetMusic, includeInstrumentOnSheetMusic);
        this.generateKeySignaturesForSections = generateKeySignaturesForSections;
        constructMidiSequence();
    }   
    
    /**
     * Creates the midi sequence and the guido notation
     *      
     * @throws com.myronmarston.music.GermIsEmptyException if the germ is empty
     */           
    private void constructMidiSequence() throws GermIsEmptyException {
        // this is only meant to be called once, to construct the sequence...
        assert this.sequence == null : sequence;
        
        // We can't create any midi sequence if we don't have a germ from which to "grow" our piece...
        if (this.fractalPiece.getGerm() == null || this.fractalPiece.getGerm().size() == 0) throw new GermIsEmptyException();                
                
        try {
            this.sequence = new Sequence(Sequence.PPQ, this.getMidiTickResolution());
        } catch (InvalidMidiDataException ex) {
            // our logic should prevent this exception from ever occurring, 
            // so we transform this to an unchecked exception instead of 
            // having to declare it on our method.
            throw new UndeclaredThrowableException(ex, "Error while creating sequence.  This indicates a programming error of some sort.");                
        }  

        // next, use the first track to set key signature, time signature and tempo...
        Track track1 = sequence.createTrack();        
        track1.add(this.fractalPiece.getScale().getKeySignature().getKeySignatureMidiEvent(0));                  
        addSectionKeySigEventsToTrack(track1, sequence.getResolution());
        track1.add(this.fractalPiece.getTimeSignature().getMidiTimeSignatureEvent());
        track1.add(Tempo.getMidiTempoEvent(this.getTempo()));

        // finally, create and fill our midi tracks...
        for (NoteList nl : noteLists) {                       
            this.constructMidiTrack(nl);             
        }        
    }
    
    /**
     * Adds key signature events to the given track for each section, as needed.
     * 
     * @param track the track to add the events to 
     * @param sequenceResolution the midi sequence resolution
     */
    private void addSectionKeySigEventsToTrack(Track track, int sequenceResolution) {
        if (!generateKeySignaturesForSections) return;
        
        Fraction durationSoFar = new Fraction(0, 1);
        KeySignature lastKeySignature = this.fractalPiece.getScale().getKeySignature();
        KeySignature sectionKeySignature;
        for (Section s : this.fractalPiece.getSections()) { 
            sectionKeySignature = s.getSectionKeySignature();
            if (!lastKeySignature.equals(sectionKeySignature)) {
                Fraction tickCount = durationSoFar.times(sequenceResolution);
        
                // our tick count should be an integral value...
                assert tickCount.denominator() == 1L : tickCount.denominator();
                long tickValue = convertMidiTickUnitFromQuarterNotesToWholeNotes((long) tickCount.asDouble());
                track.add(sectionKeySignature.getKeySignatureMidiEvent(tickValue));
            }
            lastKeySignature = sectionKeySignature;            
            durationSoFar = durationSoFar.plus(s.getDuration());
        }
    }    
    
    /**
     * Constructs a midi track based on the given note list.
     * 
     * @param noteList the note list     
     */
    protected void constructMidiTrack(NoteList noteList) {
        MidiNote thisMidiNote, lastMidiNote = null;
        Note lastNote = null;
        Fraction startTime = new Fraction(0, 1);        
        
        // get a default instrument if we we're not passed one...
        Instrument instrument = (noteList.getInstrument() == null ? Instrument.DEFAULT : noteList.getInstrument());
                                
        // make each track be on a different channel, but make sure we don't go over our total number of channels...
        int numTracks = sequence.getTracks().length;
        // The 1st track should be the tempo/key sig/time sig track        
        assert numTracks > 0 : numTracks; 
        int midiChannel = numTracks - 1;
        assert midiChannel < MidiNote.MAX_CHANNEL;
        
        Part part = new Part(this.pieceNotation, instrument);
        
        PartSection partSection = null;
        Track track = sequence.createTrack();
        track.add(instrument.getProgramChangeMidiEvent(midiChannel));        
        Fraction timeLeftInBar = this.timeSignatureFraction;
        
        // in Midi, the tick resolution is based on quarter notes, but we use whole notes...
        int midiTicksPerWholeNote = convertMidiTickUnitFromQuarterNotesToWholeNotesInt(sequence.getResolution());
        
        for (Note thisNote : noteList.getListWithNormalizedRests()) {                        
            // update our part section if necessary...
            if (partSection == null) {
                partSection = new PartSection(part, thisNote.getSourceVoiceSection());
            } else if (lastNote != null && lastNote.getSourceVoiceSection() != partSection.getSourceVoiceSection()) {
                partSection = new PartSection(part, lastNote.getSourceVoiceSection());
            }
            
            thisMidiNote = thisNote.convertToMidiNote(startTime, midiTicksPerWholeNote, midiChannel, true);                        
            
            if (lastMidiNote != null) {
                assert lastNote != null;
                                
                if (thisMidiNote.getPitch() == lastMidiNote.getPitch() && lastNote.getNormalizedNote().getScaleStep() != thisNote.getNormalizedNote().getScaleStep()) {               
                    // the notes are different scale steps and should have different pitches.
                    // This can happen with notes like B# and C in the key of C.

                    if (lastNote.getChromaticAdjustment() != 0) {
                        lastMidiNote = lastNote.convertToMidiNote(startTime.minus(thisNote.getDuration()), midiTicksPerWholeNote, midiChannel, false);
                    } else if (thisNote.getChromaticAdjustment() != 0) {
                        thisMidiNote = thisNote.convertToMidiNote(startTime, midiTicksPerWholeNote, midiChannel, false);
                    } else {
                        // one of these notes should always have a chromatic 
                        // adjustment--otherwise, how do they have the same pitches
                        // but different scale steps?
                        assert false : "Neither last note '" + lastNote.toString() + "' nor this note '" + thisNote.toString() + "' have a chromatic adjustment.";
                    }
                    
                    assert thisMidiNote.getPitch() != lastMidiNote.getPitch() : "The midi notes have the same pitch and should not: " + thisMidiNote.getPitch();
                }              
                timeLeftInBar = addMidiNoteEventsToTrack(track, partSection, lastMidiNote, lastNote, timeLeftInBar);                
            }                                      
            
            //The next note start time will be the end of this note...
            startTime = startTime.plus(thisNote.getDuration());
            
            lastMidiNote = thisMidiNote;
            lastNote = thisNote;
        }           
        timeLeftInBar = addMidiNoteEventsToTrack(track, partSection, lastMidiNote, lastNote, timeLeftInBar);                
    }        
  
    /**
     * Adds the midi note on and note off events to a track.  Also adds the 
     * Notation note to the part.
     * 
     * @param track the midi track
     * @param partSection the notation part
     * @param midiNote the midi note
     * @param note the note
     * @param timeLeftInBar the time left in the bar so far
     * @return the new timeLeftInBar
     */
    private Fraction addMidiNoteEventsToTrack(Track track, PartSection partSection, MidiNote midiNote, Note note, Fraction timeLeftInBar) {
        try {
            track.add(midiNote.getNoteOnEvent());
            track.add(midiNote.getNoteOffEvent());
        } catch (InvalidMidiDataException ex) {
            // our logic should prevent this exception from ever occurring, 
            // so we transform this to an unchecked exception instead of 
            // having to declare it on our method.
            throw new UndeclaredThrowableException(ex, "MidiNote's note on and note off events could not be created.  This indicates a programming error of some sort.");                
        }        
          
        // add the NotationNote to our partSection...        
        partSection.getNotationElements().add(note.toNotationNote(partSection, midiNote, timeLeftInBar));
        
        // calculate and return the new timeLeftInBar
        timeLeftInBar = timeLeftInBar.minus(note.getDuration());
        while (timeLeftInBar.compareTo(0L) <= 0) timeLeftInBar = timeLeftInBar.plus(this.timeSignatureFraction);
        return timeLeftInBar;
    }

    /**
     * Calculates the optimal midi tick resolution for the given collection of 
     * noteLists, based on the duration of the notes.
     *      
     * @return the midi tick resolution
     */
    protected int getMidiTickResolution() {        
        // next, figure out the resolution of our Midi sequence...
        ArrayList<Long> uniqueDurationDenominators = new ArrayList<Long>();
        for (NoteList nl : noteLists) {
            for (Note n : nl) {
                if (!uniqueDurationDenominators.contains(n.getDuration().denominator())) {
                    uniqueDurationDenominators.add(n.getDuration().denominator());
                }                
            }
        }        
        
        long resolution = MathHelper.leastCommonMultiple(uniqueDurationDenominators);
        assert resolution < Integer.MAX_VALUE;
        return (int) resolution;
    }
    
    /**
     * Converts the midi tick unit from quarter notes to whole notes, using 
     * longs.
     * 
     * @param ticksInWholeNotes ticks in whole notes
     * @return ticks in quarter notes
     */
    private static long convertMidiTickUnitFromQuarterNotesToWholeNotes(long ticksInWholeNotes) {
        return ticksInWholeNotes * 4;
    }
    
    /**
     * Converts the midi tick unit from quarter notes to whole notes, using 
     * ints.
     * 
     * @param ticksInWholeNotes ticks in whole notes
     * @return ticks in quarter notes
     */
    private static int convertMidiTickUnitFromQuarterNotesToWholeNotesInt(int ticksInWholeNotes) {
        return ticksInWholeNotes * 4;
    }
    
    /**
     * Saves the midi sequence to a file.
     * 
     * @param fileName the name of the file
     * @throws IOException if an I/O error occurs
     */
    public void saveMidiFile(String fileName) throws IOException {
        File outputFile = new File(fileName);
        MidiSystem.write(this.getSequence(), MIDI_FILE_TYPE_FOR_MULTI_TRACK_SEQUENCE, outputFile);
        this.lastMidiFileName = fileName;
    }

    /**
     * Saves the guido notation to file.
     *
     * @param fileName the name of the file to save to
     * @throws IOException if an I/O error occurs
     */
    public void saveGuidoFile(String fileName) throws IOException {
        this.saveGuidoFile(fileName, null, null);
    }

    /**
     * Saves the guido notation to file.
     *
     * @param fileName the name of the file to save to
     * @param title the title of the piece
     * @param composer the composer of the piece
     * @throws IOException if an I/O error occurs
     */
    public void saveGuidoFile(String fileName, String title, String composer) throws IOException {
        this.getSheetMusicCreator().saveGuidoFile(fileName, title, composer);
        this.lastGuidoFileName = fileName;
    }

    /**
     * Saves the Lilypond notation to file.
     *
     * @param fileName the name of the file to save to
     * @throws IOException if an I/O error occurs
     */
    public void saveLilypondFile(String fileName) throws IOException {
        this.saveLilypondFile(fileName, null, null, 0);
    }

    /**
     * Saves the Lilypond notation to file.
     *
     * @param fileName the name of the file to save to
     * @param title the title to include in the Lilypond file
     * @param composer the composer to include in the Lilypond file
     * @param imageWidth the width of the paper in pixels
     * @throws IOException if an I/O error occurs
     */
    public void saveLilypondFile(String fileName, String title, String composer, int imageWidth) throws IOException {
        this.getSheetMusicCreator().saveLilypondFile(fileName, title, composer, imageWidth);
        this.lastLilypondFileName = fileName;
    }

    /**
     * Uses Lilypond to save sheet music notation to a PDF document.
     *
     * @param fileName the file name to save the pdf file to
     * @return the lilypond logging output if there was a warning
     * @throws Exception if there is an error
     */
    public String savePdfFile(String fileName) throws Exception {
        return this.savePdfFile(fileName, null, null);
    }

    /**
     * Uses Lilypond to save sheet music notation to a PDF document.
     *
     * @param fileName the file name to save the pdf file to
     * @param title the title of the piece
     * @param composer the composer of the piece
     * @return the lilypond logging output if there was a warning
     * @throws Exception if there is an error
     */
    public String savePdfFile(String fileName, String title, String composer) throws Exception {
        String returnVal = this.getSheetMusicCreator().saveAsPdf(fileName, title, composer);
        this.lastPdfFileName = fileName;
        return returnVal;
    }

    /**
     * Saves notation to a png file using lilypond.
     *
     * @param fileName the name of the file
     * @param imageWidth the desired width of the image
     * @return the logging output if there was a warning
     * @throws Exception if an error occurs
     */
    public String savePngFile(String fileName, int imageWidth) throws Exception {
        return this.savePngFile(fileName, null, null, imageWidth);
    }

    /**
     * Saves notation to a png file using lilypond.
     *
     * @param fileName the name of the file
     * @param title the title of the piece
     * @param composer the composer of the piece
     * @param imageWidth the desired width of the image
     * @return the logging output from lilypond if there was a warning
     * @throws Exception if an error occurs
     */
    public String savePngFile(String fileName, String title, String composer, int imageWidth) throws Exception {
        String returnVal = this.getSheetMusicCreator().saveAsPng(fileName, title, composer, imageWidth);
        this.lastPngFileName = fileName;
        return returnVal;
    }

    /**
     * Saves the music as a sheet music image in gif format.
     *
     * @param fileName the file name to save to
     * @return the guido logging output if there was a warning
     * @throws Exception if an error occurs
     */
    public String saveGifImage(String fileName) throws Exception {
        return this.saveGifImage(fileName, null, null);
    }

    /**
     * Saves the music as a sheet music image in gif format.
     *
     * @param fileName the file name to save to
     * @param title the title of the piece
     * @param composer the composer of the piece
     * @return the guido logging output if there was a warning
     * @throws Exception if an error occurs
     */
    public String saveGifImage(String fileName, String title, String composer) throws Exception {
        String returnVal = this.getSheetMusicCreator().saveAsGifImage(fileName, title, composer);
        this.lastGifFileName = fileName;
        return returnVal;
    }

    /**
     * Saves the music as a wav file.
     *
     * @param fileName the file name to save to
     * @throws MidiUnavailableException if there is a midi
     *         problem
     * @throws IOException if there is an i/o error
     */
    public void saveWavFile(String fileName) throws MidiUnavailableException, IOException {
        this.getAudioFileCreator().saveWavFile(fileName);
        this.lastWavFileName = fileName;
    }

    /**
     * Saves the music to an mp3 file.
     *
     * @param fileName the file name to save to
     * @throws Exception if there is an error
     */
    public void saveMp3File(String fileName) throws Exception {
        // TODO: sometimes our germ mp3 file is way long (over a minute) when it should be only a few seconds
        this.getAudioFileCreator().saveMp3File(fileName);  
        this.lastMp3FileName = fileName;
    }

    /**
     * Gets the file name of the last gif file saved using this output manager.
     * 
     * @return the last gif file
     */
    public String getLastGifFileName() {
        return lastGifFileName;
    }

    /**
     * Gets the file name of the last guido file saved using this output manager.
     * 
     * @return the last guido file
     */
    public String getLastGuidoFileName() {
        return lastGuidoFileName;
    }

    /**
     * Gets the file name of the last midi file saved using this output manager.
     * 
     * @return the last midi file
     */
    public String getLastMidiFileName() {
        return lastMidiFileName;
    }
    
    /**
     * Gets the file name of the last mp3 file saved using this output manager.
     * 
     * @return the last mp3 file
     */
    public String getLastMp3FileName() {
        return lastMp3FileName;
    }
    
    /**
     * Gets the file name of the last wav file saved using this output manager.
     * 
     * @return the last wav file
     */
    public String getLastWavFileName() {
        return lastWavFileName;
    }

    /**
     * Gets the file name of the last lilypond file saved using this output
     * manager.
     * 
     * @return the last lilypond file
     */
    public String getLastLilypondFileName() {
        return lastLilypondFileName;
    }

    /**
     * Gets the file name last passed to lilypond to produce PDF sheet music
     * output.
     * 
     * @return the pdf file name last used by lilypond
     */
    public String getLastPdfFileName() {
        return lastPdfFileName;
    }        
    
    /**
     * Gets the file name last passed to lilypond to produce png sheet music
     * output.
     * 
     * @return the png file name last used by lilypond
     */
    public String getLastPngFileName() {
        return lastPngFileName;
    }        
}
