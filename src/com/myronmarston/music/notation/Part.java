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

package com.myronmarston.music.notation;

import com.myronmarston.music.Dynamic;
import com.myronmarston.music.Instrument;
import com.myronmarston.music.Tempo;
import com.myronmarston.music.scales.KeySignature;
import com.myronmarston.util.FileHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents one instrumental part of the notation.  
 * 
 * @author Myron
 */
public class Part extends AbstractNotationElement {    
    private final NotationElementList partSections = new NotationElementList();
    private final Piece piece;
    private final Instrument instrument;
    private String pieceTitle;
    private String pieceComposer;        
    
    /**
     * Constructor.  The newly constructed part will automatically be added to 
     * the given piece.
     * 
     * @param piece the notation piece that owns this part
     * @param instrument the instrument for this part
     */
    public Part(Piece piece, Instrument instrument) {
        assert piece != null : "Piece should not be null.";
        assert instrument != null : "Instrument should not be null.";
        
        this.piece = piece;
        piece.getParts().add(this);
        this.instrument = instrument;
    }
        
    /**
     * Gets the notation piece that owns this part.
     * 
     * @return the piece
     */
    public Piece getPiece() {
        return piece;
    }
    
    /**
     * Checks to see if this part is the first part of the piece.
     * 
     * @return true if this is the first part of the piece
     */
    protected boolean isPartFirstPartOfPiece() {       
        return this == this.piece.getParts().get(0);
    }

    /**
     * Gets the composer of the piece.  It is available here because in 
     * Guido notation the composer is notated in the first part.
     * 
     * @return the piece composer
     */
    public String getPieceComposer() {
        return pieceComposer;
    }
    
    /**
     * Sets the composer of the piece.  It is available here because in 
     * Guido notation the composer is notated in the first part.
     * 
     * @param pieceComposer the composer of the piece
     */            
    public void setPieceComposer(String pieceComposer) {
        assert this.isPartFirstPartOfPiece() : "This should only be set on the first part of the piece.";
        this.pieceComposer = pieceComposer;
    }

    /**
     * Gets the title of the piece.  It is available here because in 
     * Guido notation the title is notated in the first part.
     * 
     * @return the title of the piece
     */
    public String getPieceTitle() {
        return pieceTitle;
    }

    /**
     * Sets the title of the piece.  It is available here because in 
     * Guido notation the title is notated in the first part.
     * 
     * @param pieceTitle the title of the piece
     */
    public void setPieceTitle(String pieceTitle) {
        assert this.isPartFirstPartOfPiece() : "This should only be set on the first part of the piece.";        
        this.pieceTitle = pieceTitle;
    }   
    
    /**
     * Gets the notation elements for this part.
     * 
     * @return the notation elements
     */
    public NotationElementList getPartSections() {
        return partSections;
    }   
    
    /**
     * Gets the first key signature used by this part.
     * 
     * @return the first key signature
     */
    private KeySignature getFirstKeySignature() {
        for (NotationElement element : partSections) {
            if (element instanceof PartSection) {
                return ((PartSection) element).getSectionKeySignature();
            }
        }
        
        return this.piece.getKeySignature();
    }

    /**
     * Indicates whether or not duration scaling is supported by this element.
     * 
     * @return true if any of the notation elements support duration scaling
     */
    public boolean supportsDurationScaling() {
        return this.getPartSections().supportsDurationScaling();
    }

    @Override
    public long getLargestDurationDenominator() {
        return this.getPartSections().getLargestDurationDenominator();        
    }

    @Override
    public void scaleDurations(long scaleFactor) {
        this.getPartSections().scaleDurations(scaleFactor);
    }

    @Override
    public List<NotationNote> getNotationNotes() {
        return this.getPartSections().getNotationNotes();
    }
    
    /**
     * Sets the dynamic on each NotationNote.  Should be called before getting
     * the lilypond or guido string.
     */
    protected void setNotationNoteDynamics() {        
        List<NotationNote> notes = this.getNotationNotes();
        List<NotationNote> germCopy;
        Dynamic lastGermCopyDynamic = null;
        int firstNoteOfGermCopy_curIndex = getNextIndexOfFirstNoteOfAGermCopy(notes, 0);
        int firstNoteOfGermCopy_nextIndex;
        
        // Seperate the list into copies of the germ, and figure out the dynamics
        // for each copy.  We do this because each copy of the germ could be
        // volume-scaled, producing a different dynamic level for that part of
        // the piece.  Dynamics on individual notes can be treated as articulations
        while(firstNoteOfGermCopy_curIndex < notes.size()) {            
            firstNoteOfGermCopy_nextIndex = getNextIndexOfFirstNoteOfAGermCopy(notes, firstNoteOfGermCopy_curIndex + 1);            
            if (firstNoteOfGermCopy_nextIndex == -1) firstNoteOfGermCopy_nextIndex = notes.size();        
                                    
            // make sure the start index note isFirstNoteOfGermCopy
            assert notes.get(firstNoteOfGermCopy_curIndex).isFirstNoteInGermCopy();
            
            // make sure the end index is the index before the next firstNote, or the last index of the list
            assert firstNoteOfGermCopy_nextIndex == notes.size() || notes.get(firstNoteOfGermCopy_nextIndex).isFirstNoteInGermCopy();
            
            germCopy = notes.subList(firstNoteOfGermCopy_curIndex, firstNoteOfGermCopy_nextIndex);            
            firstNoteOfGermCopy_curIndex = firstNoteOfGermCopy_nextIndex;
            
            lastGermCopyDynamic = setDynamicsOnGermCopy(germCopy, lastGermCopyDynamic);
        }         
    }
        
    /**
     * Gets the next index of a notation note which is the first not of a germ
     * copy.
     * 
     * @param notes the list of notation notes
     * @param currentIndex the index to begin the search
     * @return the next index of a first note of a germ copy, or -1 if none is 
     *         found
     */
    protected static int getNextIndexOfFirstNoteOfAGermCopy(List<NotationNote> notes, int currentIndex) {
        for (int i = currentIndex; i < notes.size(); i++) {
            if (notes.get(i).isFirstNoteInGermCopy()) return i;
        }
        
        return -1;
    }
    
    /**
     * Sets the notation dynamics on the notation notes in the germ copy.
     * 
     * @param germCopy a copy of the germ
     * @param lastGermCopyDynamic the dynamic used for the last germ copy
     * @return the dynamic used for this germ copy, which should be passed to
     *         this method as a parameter in the next iteration
     */
    protected static Dynamic setDynamicsOnGermCopy(List<NotationNote> germCopy, Dynamic lastGermCopyDynamic) {                
        assert germCopy.get(0).isFirstNoteInGermCopy() : germCopy.get(0);
        
        List<Dynamic> dynamics = new ArrayList<Dynamic>();
        Dynamic thisDynamic;
        
        for (NotationNote note : germCopy) {            
            if (!note.isRest()) {
                thisDynamic = Dynamic.getDynamicForMidiVolume(note.getVolume());
                if (!dynamics.contains(thisDynamic)) dynamics.add(thisDynamic);
            }            
        }
               
        Collections.sort(dynamics);
        Map<Dynamic, NotationDynamic.Articulation> articulations = new HashMap<Dynamic, NotationDynamic.Articulation>();        
        NotationDynamic.Articulation articulation;
        int numDynamicLevels = dynamics.size();

        // assign articulations to our different dynamic levels...
        for (int i = 0; i < numDynamicLevels; i++) {
            if (i == 0) {
                // the lowest level.  This is simply the dynamic (i.e. mf) for our germ copy
                articulation = NotationDynamic.Articulation.NONE;
            } else if (numDynamicLevels > 2 && i == numDynamicLevels - 1) {
                // this is the loudest dynamic among 3 or more dynamic levels; 
                // use a marcato mark since this is a heavy accent
                articulation = NotationDynamic.Articulation.MARCATO;
            } else {
                // accent marks are more common in notation than marcatos,
                // so we use accents for everything else. This is louder than our 
                // dynamic marking, but softer than the loudest level
                articulation = NotationDynamic.Articulation.ACCENT;
            }
            
            articulations.put(dynamics.get(i), articulation);
        }
        
        // Get the dynamic level for this germ copy--the lowest dynamic level.
        // But, we only care to print it if it is different from the dynamic 
        // level of the last germ copy.
        Dynamic lowestDynamic = dynamics.get(0);
        Dynamic dynamicForThisGermCopy = (lowestDynamic == lastGermCopyDynamic ? null : lowestDynamic);        
        
        for (NotationNote note : germCopy) {
            // get the articulation based on the dynamic level; rests are always none
            articulation = 
                (note.isRest() ? 
                 NotationDynamic.Articulation.NONE : 
                 articulations.get(Dynamic.getDynamicForMidiVolume(note.getVolume())));
            
            // the dynamic should only be printed once, on the first note of the germ copy
            thisDynamic = (note.isFirstNoteInGermCopy() ? dynamicForThisGermCopy : null);
            note.setDynamic(new NotationDynamic(thisDynamic, articulation));
        }
        
        return lowestDynamic;
    }    
                            
    /**
     * Gets the lilypond notation for this part.
     * 
     * @return the lilypond string
     */
    public String toLilypondString() {  
        this.setNotationNoteDynamics();
        this.getPartSections().setElementSeperator(" ");
        StringBuilder str = new StringBuilder();

        str.append("\\new Voice " + FileHelper.NEW_LINE);
        str.append("{" + FileHelper.NEW_LINE); 
        if (this.isPartFirstPartOfPiece() && this.piece.getIncludeTempo()) str.append("       " + Tempo.toLilypondString(this.piece.getTempo()) + FileHelper.NEW_LINE);
        str.append("       " + this.piece.getTimeSignature().toLilypondString());
        str.append("       " + this.getFirstKeySignature().toLilypondString());
        if (this.piece.getIncludeInstruments()) str.append("       " + this.instrument.toLilypondString()); 
        str.append("       " + Clef.getBestMatchForNoteList(this.getNotationNotes()).toLilypondString() + FileHelper.NEW_LINE);
        str.append("       " + this.getPartSections().toLilypondString());
        str.append("       \\bar \"|.\"" + FileHelper.NEW_LINE); 
        str.append("}" + FileHelper.NEW_LINE);            

        return str.toString();        
    }

    /**
     * Gets the GUIDO notation for this part.
     * 
     * @return the GUIDO notation for this part
     */
    public String toGuidoString() {
        assert this.partSections.size() > 0; 
        
        this.setNotationNoteDynamics();
        this.getPartSections().setElementSeperator(" ");
        StringBuilder str = new StringBuilder();        
        str.append("[");
        //If a pageFormat should be specified, put it here, such as \pageFormat<"A4",10pt,10pt,10pt,10pt>
                
        if (this.piece.getIncludeInstruments()) {
            str.append(instrument.toGuidoString() + " ");
        }
                
        str.append(this.getFirstKeySignature().toGuidoString() + " ");
        str.append(this.piece.getTimeSignature().toGuidoString() + " ");                
        str.append(Clef.getBestMatchForNoteList(this.getNotationNotes()).toGuidoString() + " ");
        
        if (this.isPartFirstPartOfPiece()) {
            if (this.piece.getIncludeTempo()) {
                str.append(Tempo.toGuidoString(this.piece.getTempo()) + " ");
            }
            if (this.getPieceTitle() != null && !this.getPieceTitle().isEmpty()) {
                str.append("\\title<\"" + this.getPieceTitle() + "\">");
            }
            if (this.getPieceComposer() != null && !this.getPieceComposer().isEmpty()) {
                str.append("\\composer<\"" + this.getPieceComposer() + "\">");
            }
        }        

        str.append(this.getPartSections().toGuidoString());
        str.append("]");           
        return str.toString();
    }
   
}
