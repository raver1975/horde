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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import java.lang.reflect.UndeclaredThrowableException;

/**
 *
 * @author Myron
 */
public class Tempo {        
    /**
     * These constants were taken from http://www.sonicspot.com/guide/midifiles.html
     */
    private final static int MICROSECONDS_PER_MINUTE = 60000000;
    private final static int MAX_MICROSECONDS_PER_QTR_NOTE = 8355711;
    private final static int MIN_MICROSECONDS_PER_QTR_NOTE = 0;        
    private final static int TEMPO_META_MESSAGE_TYPE = 81;
    
    /**
     * The minimum tempo allowed, in beats per minute.
     */    
    public final static int MIN_TEMPO_BPM = (int) Math.ceil((double) MICROSECONDS_PER_MINUTE / (double) MAX_MICROSECONDS_PER_QTR_NOTE);
        
    /** 
     * The maximum tempo allowed, in beats per minute.
     */
    public final static int MAX_TEMPO_BPM = 499;
    
    /**
     * The default tempo to use.  This is different from the default tempo of 
     * the midi standard but will be the default for our pieces.
     */
    public final static int DEFAULT = 90;
    
    /**
     * Gets a string representation of this tempo in GUIDO notation.
     * 
     * @param tempo the tempo in BPM
     * @return the guido string
     */
    public static String toGuidoString(int tempo) {        
        //TODO: for some reason, the only thing on the guido output is
        // the tempo description string, not the 1/4=120 string.  Find a way
        // to include the BPM string.
        return "\\tempo<\"" + getTempoName(tempo) + "\",\"1/4=" + tempo + "\">";
    }
        
    /**
     * Gets a string representation of this tempo in Lilypond notation.
     * 
     * @param tempo the tempo in BPM
     * @return the lilypond string
     */
    public static String toLilypondString(int tempo) {
        return "\\tempo 4=" + tempo;
    }    
    
    /**
     * Gets the name of the given tempo.
     * 
     * @param tempo the tempo in BPM
     * @return the  name of the tempo
     */
    private static String getTempoName(int tempo) {                
        // Here's a list from the Wikipedia Tempo article 
        // (http://en.wikipedia.org/wiki/Tempo).  I modify these slightly and
        // only use some of them.
        
        // From fastest to slowest, the common tempo markings are:
        // 
        // * Prestissimo — extremely fast (200 - 208 bpm)
        // * Vivacissimamente — adverb of vivacissimo, "very quickly and lively"
        // * Vivacissimo — very fast and lively
        // * Presto — very fast (168 - 200 bpm)
        // * Allegrissimo — very fast
        // * Vivo — lively and fast
        // * Vivace — lively and fast (~140 bpm)
        // * Allegro — fast and bright or "march tempo" (120 - 168 bpm)
        // * Allegro moderato — moderately quick (112 - 124 bpm)
        // * Allegretto — moderately fast (but less so than allegro)
        // * Allegretto grazioso — moderately fast and with grace
        // * Moderato — moderately (108 - 120 bpm)
        // * Moderato con espressivo — moderately with expression
        // * Andantino — alternatively faster or slower than andante
        // * Andante — at a walking pace (76 - 108 bpm)
        // * Tranquillamente — adverb of tranquillo, "tranquilly"
        // * Tranquillo — tranquil
        // * Adagietto — rather slow (70 - 80 bpm)
        // * Adagio — slow and stately (literally, "at ease") (66 - 76 bpm)
        // * Grave — slow and solemn
        // * Larghetto — rather broadly (60 - 66 bpm)
        // * Largo — Very slow (40 - 60 bpm), like lento
        // * Lento — very slow (40 - 60 bpm)
        // * Largamente/Largo — "broadly", very slow (40 bpm and below)
        // * Larghissimo — very slow (20 bpm and below)
        
        if (tempo <= 20) return "Larghissimo";
        else if (tempo <= 40) return "Largamente";
        else if (tempo <= 60) return "Largo";
        else if (tempo <= 70)  return "Adagio";
        else if (tempo <= 80) return "Adagietto";
        else if (tempo < 108) return "Andante";
        else if (tempo < 120) return "Moderato";
        else if (tempo < 140) return "Vivace";
        else if (tempo < 168) return "Allegro";
        else if (tempo < 200) return "Presto";
        else return "Prestissimo";        
    }
    
    /**
     * Checks the validity of the tempo.
     * 
     * @param tempo the tempo to check
     * @throws IllegalArgumentException if the tempo is not in the acceptable
     *         range
     */    
    public static void checkTempoValidity(int tempo) throws IllegalArgumentException {             
        if (tempo < MIN_TEMPO_BPM || tempo > MAX_TEMPO_BPM) {
            throw new IllegalArgumentException("The tempo must be between " + MIN_TEMPO_BPM + " and " + MAX_TEMPO_BPM + ".");
        }        
    }
    
    /**
     * Converts the given tempo, in beats per minute, to microseconds per 
     * quarter note.
     * 
     * @param tempoInBPM the tempo, in beats per minute
     * @return the number of microseconds per quarter note for this tempo
     */
    public static int convertToMicrosecondsPerQuarterNote(int tempoInBPM) {
        return MICROSECONDS_PER_MINUTE / tempoInBPM;
    }
    
    /**
     * Converts the tempo to a byte array for the midi event.
     * 
     * @param tempoInBPM the tempo, in beats per minute
     * @return the byte array
     */
    private static byte[] convertTempoToMidiByteArray(int tempoInBPM) {        
        // convert our tempo to the unit midi expects
        int microsecondsPerQuarterNote = MICROSECONDS_PER_MINUTE / tempoInBPM;        
        assert microsecondsPerQuarterNote > MIN_MICROSECONDS_PER_QTR_NOTE && microsecondsPerQuarterNote <= MAX_MICROSECONDS_PER_QTR_NOTE : microsecondsPerQuarterNote;        
        
        // convert our value to a 6-digit hex string, with leading zeroes if necessary
        String hexValue = Integer.toHexString(microsecondsPerQuarterNote);
        assert hexValue.length() <= 6;
        while (hexValue.length() < 6) hexValue = "0" + hexValue;
                        
        // convert it to 3 bytes...
        byte[] byteArray = new byte[3];        
        for (int i = 0; i < byteArray.length; i++) {
            // I would like to use Byte.parseByte(), but it fails for values outside
            // the range -128..127 because java uses signed bytes.
            // Instead, we use Integer.parseInt() because it can handle values
            // 128-255, and then we cast it to a byte.  The cast takes care of
            // wrapping the values around to the negatives if necessary.            
            byteArray[i] = (byte) Integer.parseInt(hexValue.substring(i * 2, (i * 2) + 2), 16);
        }
        
        return byteArray;
    }
    
    /**
     * Gets a midi event for a given tempo.
     * 
     * @param tempoInBPM the tempo, in beats per minute
     * @return the midi tempo event
     */
    public static MidiEvent getMidiTempoEvent(int tempoInBPM) {
        checkTempoValidity(tempoInBPM); // make sure out tempo is valid
        byte[] tMessageData = convertTempoToMidiByteArray(tempoInBPM);
        
        // See http://www.sonicspot.com/guide/midifiles.html for a description of the contents of this message.
        MetaMessage tMessage = new MetaMessage();
                   
        try {
            tMessage.setMessage(TEMPO_META_MESSAGE_TYPE, tMessageData, tMessageData.length); // the size of the data array
        } catch (InvalidMidiDataException ex) {
            // wrap it in an undeclared exception rather than declaring it on our method since
            // our code should prevent an InvalidMidiDataException from ever occuring...
            throw new UndeclaredThrowableException(ex, "An unexpected exception occurred while greating the midi tempo event.  This indicates a programming error.");
        }
        
        return new MidiEvent(tMessage, 0);
    }
}
