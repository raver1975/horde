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

import com.sun.media.sound.DLSSoundbank;
import com.sun.media.sound.SF2Soundbank;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around the java midi soundbank interface.  Lets you set the midi 
 * soundbank that will be used to generate the audio files.
 * 
 * @author Myron
 */
public class MidiSoundbank {
    
    private File soundbankFile;
    private Soundbank soundbank;
    private static MidiSoundbank current;
    
    static {
        // load the default soundbank...
        MidiSoundbank.load(null);
    }
    
    private MidiSoundbank(File soundbankFile) {                                    
        this.soundbankFile = soundbankFile;
        
        if (soundbankFile == null) {
            // get the default soundbank...
            Synthesizer synth = null;
            try {
                synth = AudioFileCreator.getAudioSynthesizer();
                this.soundbank = synth.getDefaultSoundbank();
            } catch (MidiUnavailableException ex) {
                throw new UndeclaredThrowableException(ex, "An error occured while getting the Midi synthesizer.");
            } finally {
              if (synth != null) synth.close();
            }
        } else {
            // This isn't well unit-tested because we would need to check a
            // midi soundbank into source control to include it along with the
            // tests, and I'm not sure of the legality of that...
            
            // try the different soundbank types...
            List<Exception> exceptions = new ArrayList<Exception>();            
            try {
                soundbank = new SF2Soundbank(soundbankFile);                 
            } catch (Exception ex) {
                exceptions.add(ex);
            }                

            if (soundbank == null) {
                try {
                    soundbank = new DLSSoundbank(soundbankFile);                   
                } catch (Exception ex) {
                    exceptions.add(ex);
                }
            }            

            if (soundbank == null) {
                StringBuilder exceptionMessage = new StringBuilder();
                for (Exception ex : exceptions) {
                    if (exceptionMessage.length() > 0) exceptionMessage.append("; ");
                    exceptionMessage.append(ex.getMessage());
                }

                throw new UndeclaredThrowableException(exceptions.get(0), "An exception occurred while creating the soundbank from file " + soundbankFile.getAbsolutePath() + ": " + exceptionMessage.toString());
            }            
        }                    
    }
    
    /**
     * Lots a midi sound bank from the given soundbank file.
     * 
     * @param soundbankFile the sound bank file
     */
    public synchronized static void load(File soundbankFile) {
        current = new MidiSoundbank(soundbankFile);
        System.out.println("Loaded Soundbank: " + current.toString());
    }
    
    /**
     * Gets the current midi soundbank.
     * 
     * @return the current soundbank.
     */
    public synchronized static MidiSoundbank getCurrent() {
        return current;
    }        
    
    /**
     * Gets the java soundbank instance for this soundbank.
     * 
     * @return the java soundbank
     */
    public Soundbank getSoundbank() {
        return soundbank;
    }                    
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Soundbank Description: ");
        str.append(this.getSoundbank().getDescription());
        str.append("; Vendor: ");
        str.append(this.getSoundbank().getVendor());
        str.append("; Name: ");
        str.append(this.getSoundbank().getName());
        str.append("; Version: ");
        str.append(this.getSoundbank().getVersion());
        str.append("; Loaded from: ");                
        str.append(this.soundbankFile == null ? "(no file)" : this.soundbankFile.getAbsolutePath());
        
        return str.toString();
    }
}
