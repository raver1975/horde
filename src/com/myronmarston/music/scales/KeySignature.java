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

package com.myronmarston.music.scales;

import com.myronmarston.music.MidiNote;
import com.myronmarston.music.NoteName;
import com.myronmarston.music.notation.AbstractNotationElement;
import com.myronmarston.music.notation.NotationNote;
import com.myronmarston.util.FileHelper;
import org.simpleframework.xml.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Locale;

/**
 * Represents a Midi key signature.  This is immutable.
 * 
 * @author Myron
 */
@Root
public class KeySignature extends AbstractNotationElement {  
    
    @Attribute
    private NoteName keyName;
        
    @Attribute
    private Tonality tonality;    
    private MetaMessage keySignatureMidiMessage;    
    private String lilypondString;    
    
    
    /**
     * Taken from http://www.sonicspot.com/guide/midifiles.html.
     */    
    protected final static int KEY_SIGNATURE_META_MESSAGE_TYPE = 89;  
    
    /**
     * Constructor.
     * 
     * @param tonality major or minor
     * @param keyName the tonal center of the key
     * @throws com.myronmarston.music.scales.InvalidKeySignatureException 
     *         if the the key is invalid-e.g., a key that would have double 
     *         flats or sharps, such as A# major.
     */
    public KeySignature(Tonality tonality, NoteName keyName) throws InvalidKeySignatureException {
        checkValidityOfKeySignature(keyName, tonality);         
        this.tonality = tonality;
        this.keyName = keyName;
    }
    
    /**
     * Provided for xml deserialization.
     */
    private KeySignature() {}
    
    /**
     * Gets the tonality of this key.
     * 
     * @return either major or minor
     */
    public Tonality getTonality() {
        return tonality;
    }

    /**
     * Gets the number of flats or sharps for this key.  A negative number 
     * indicates flats and a positive number indicates sharps.
     * 
     * @return the number of flats or sharps
     */
    public int getNumberOfFlatsOrSharps() {
        return this.getTonality().getSharpsOrFlatsForKeyName(this.getKeyName());
    }

    /**
     * Gets the note name of the tonal center.
     * 
     * @return the name of the key
     */
    public NoteName getKeyName() {
        return keyName;
    }     
            
    /**
     * Gets the key name for the given tonality that has the same number of 
     * accidentals as this key signature.
     * 
     * @param tonality the tonality
     * @return the key for the given tonality that has the same number of accidentals
     */
    public NoteName getKeyNameWithSameNumAccidentals(Tonality tonality) {
        int letterNumberOffset = tonality.getModeLetterNumberOffset() - this.getTonality().getModeLetterNumberOffset();
        int noteNumberOffset =  tonality.getModeNoteNumberOffset() - this.getTonality().getModeNoteNumberOffset();
        return this.keyName.getNoteNameFromInterval(letterNumberOffset, noteNumberOffset);
    }
    
    /**
     * Gets a string representing this key signature in GUIDO notation.
     * 
     * @return the Guido string
     */
    public String toGuidoString() {        
        return "\\key<" + this.getNumberOfFlatsOrSharps() + ">";
    }
    
    /**
     * Gets the representation of this key signature in Lilypond notation.
     * 
     * @return the lilypond string
     */
    public String toLilypondString() {                
        if (lilypondString == null) {
            String key = this.getKeyName().toString();
            key = key.replace('b', NotationNote.LILYPOND_FLAT_CHAR);
            key = key.replace("x", "##");
            key = key.replace('#', NotationNote.LILYPOND_SHARP_CHAR);
            key = key.toLowerCase(Locale.ENGLISH);

            String tonalityStr = this.getTonality().toString().toLowerCase(Locale.ENGLISH);
            
            lilypondString = "\\key " + key + " \\" + tonalityStr + FileHelper.NEW_LINE;
        }
                
        return lilypondString;
    }
    
    private static void checkValidityOfKeySignature(NoteName keyName, Tonality tonality) throws InvalidKeySignatureException {
        if (keyName == null || tonality == null) return;
        
        if (!tonality.isValidKeyName(keyName)) {
            throw new InvalidKeySignatureException(keyName.toString());
        }
    }
    
    /**
     * Gets a midi key signature event. 
     * 
     * @param tick the time-stamp for when this key signature should be used
     * @return the midi event
     */
    public MidiEvent getKeySignatureMidiEvent(long tick) {
        if (this.keySignatureMidiMessage == null) this.keySignatureMidiMessage = generateMidiKeySignatureMessage();                    
        return new MidiEvent(this.keySignatureMidiMessage, tick + MidiNote.MIDI_SEQUENCE_START_SILENCE_TICK_OFFSET);
    }
    
    /**
     * Generates the midi key signature event.
     * 
     * @return the midi event     
     */
    private MetaMessage generateMidiKeySignatureMessage() {
        // See http://www.sonicspot.com/guide/midifiles.html for a description of the contents of this message.
        MetaMessage ksMessage = new MetaMessage();
        
        byte[] ksMessageData = new byte[2];
        ksMessageData[0] = (byte) this.getNumberOfFlatsOrSharps();
        ksMessageData[1] = this.getTonality().getMidiValue();       
       
        try {
            ksMessage.setMessage(KEY_SIGNATURE_META_MESSAGE_TYPE,
                         ksMessageData,         // the key signature data
                         ksMessageData.length); // the size of the data array                
        } catch (InvalidMidiDataException ex) {
            // our logic should prevent this exception from ever occurring, 
            // so we transform this to an unchecked exception instead of 
            // having to declare it on our method.
            throw new UndeclaredThrowableException(ex, "The key signature midi event could not be created.  This indicates a programming error of some sort.");                
        }        
        
        return ksMessage;        
    }

    /**
     * Returns false to indicate that this element does not support duration
     * scaling, since a key signature has no durations.
     * 
     * @return false
     */
    public boolean supportsDurationScaling() {
        return false;
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
        final KeySignature other = (KeySignature) obj;
        if (this.keyName != other.keyName) {
            return false;
        }
        if (this.tonality != other.tonality) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.keyName != null ? this.keyName.hashCode() : 0);
        hash = 29 * hash + (this.tonality != null ? this.tonality.hashCode() : 0);
        return hash;
    }        
}