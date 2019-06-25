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

import com.myronmarston.music.scales.KeySignature;
import com.myronmarston.music.settings.VoiceSection;

import java.util.List;

/**
 * Represents one section of a notation part.  This is necessary to allow 
 * different sections to have different key signatures.
 * 
 * @author Myron
 */
public class PartSection extends AbstractNotationElement {
    private NotationElementList notationElements = new NotationElementList();
    private final VoiceSection sourceVoiceSection;
    private final KeySignature sectionKeySignature;
    private final Part part;

    /**
     * Constructor. Adds this part section to the given part.  Gets the
     * key signature from the source voice section, or from the piece if
     * that is null.
     * 
     * @param part the notation part
     * @param sourceVoiceSection the source voice section; can be null
     */
    public PartSection(Part part, VoiceSection sourceVoiceSection) {
        assert part != null;        
        
        this.part = part;
        this.part.getPartSections().add(this);
        this.sourceVoiceSection = sourceVoiceSection; 
        if (sourceVoiceSection == null) {
            // the source voice section will be null if we just got the output manager
            // for the germ rather than the whole piece, or a section, voice or voice section
            this.sectionKeySignature = this.part.getPiece().getKeySignature();
        } else {
            this.sectionKeySignature = sourceVoiceSection.getSection().getSectionKeySignature();
        }        
    }

    /**
     * The notation part that owns this part section.
     * 
     * @return the part
     */
    public Part getPart() {
        return part;
    }
        
    /**
     * The source voice section for this part section.  Can be null when this
     * is a part section for the germ.  This is a mutable object, so it can 
     * change after being set here.
     * 
     * @return the source
     */
    public VoiceSection getSourceVoiceSection() {
        return sourceVoiceSection;
    }

    /**
     * Gets the key signature for this part section.
     * 
     * @return the section key signature
     */
    public KeySignature getSectionKeySignature() {
        return this.sectionKeySignature;
    }      
    
    /**
     * Gets a value indicating whether or not this is the first part section
     * of the part.
     * 
     * @return true if this is the first part section
     */
    private boolean isFirstPartSection() {
        return this == this.getPart().getPartSections().get(0);
    }
    
    /**
     * Gets the part section prior to this one.  If this is the first part 
     * section, null is returned.
     * 
     * @return the prior part section, or null
     */
    private PartSection getLastSection() {
        NotationElementList partSections = this.getPart().getPartSections();
        for (int i = partSections.size() - 1; i >= 0; i--) {
            PartSection partSection = (PartSection) partSections.get(i);
            if (this == partSection) {
                if (i == 0) return null;
                return (PartSection) partSections.get(i - 1);
            }
        }
        
        throw new AssertionError("No PartSection matching this one was found.  This should never be the case, so there must be a programming error somewhere.");
    }
    
    /**
     * Gets the key signature from the prior part section.
     * 
     * @return the last part section's key signature
     */
    private KeySignature getLastSectionKeySignature() {
        PartSection lastPartSection = this.getLastSection();
        if (lastPartSection == null) return null;
        return lastPartSection.getSectionKeySignature();        
    }
        
    /**
     * Gets the list of notation elements for this part section.
     * 
     * @return the list of notation elements
     */
    public NotationElementList getNotationElements() {
        return notationElements;
    }
    
    /**
     * Gets a copy of the list of notation elements, with notes that have tuplet 
     * durations properly grouped.
     * 
     * @return list of elements with tuplets properly grouped
     */
    private NotationElementList getNotationElementsWithGroupedTuplets() {
        NotationElementList list = this.getNotationElements().clone();
        list.setElementSeperator(" ");
        list.groupTuplets();
        return list;
    }
                
    /**
     * Gets the lilypond notation string for this part section.
     * 
     * @return the lilypond string
     */
    public String toLilypondString() {
        StringBuilder str = new StringBuilder();
        
        if (!this.isFirstPartSection() && this.getSectionKeySignature() != this.getLastSectionKeySignature()) str.append("        " + this.getSectionKeySignature().toLilypondString());        
        str.append(this.getNotationElementsWithGroupedTuplets().toLilypondString());
        
        return str.toString();
    }

    /**
     * Gets the Guido notation string for this part section.
     * 
     * @return the Guido notation string
     */
    public String toGuidoString() {
        this.getNotationElements().setElementSeperator(" ");
        StringBuilder str = new StringBuilder();
               
        if (!this.isFirstPartSection() && this.getSectionKeySignature() != this.getLastSectionKeySignature()) str.append("        " + this.getSectionKeySignature().toGuidoString());        
        str.append(this.getNotationElements().toGuidoString());
        
        return str.toString();
    }        

    /**
     * Gets a value indicating if this supports duration scaling.
     * 
     * @return true if duration scaling is supported
     */
    public boolean supportsDurationScaling() {
        return this.getNotationElements().supportsDurationScaling();
    }

    /**
     * Gets the largest duration denominator found in this part seciton.
     * 
     * @return the largest duration denominator
     * @throws UnsupportedOperationException if there are no notation
     *         elements that support duration scaling
     */
    @Override
    public long getLargestDurationDenominator() throws UnsupportedOperationException {
        // here we use the list with grouped tuplets because we want to get the
        // duration denominator that will actually be used for the notes, taking
        // into account tuplets.
        return this.getNotationElementsWithGroupedTuplets().getLargestDurationDenominator();
    }

    /**
     * Scales all durations by the given scale factor.
     *
     * @param scaleFactor the scale factor
     * @throws UnsupportedOperationException if there are no notation
     *         elements that support duration scaling
     */
    @Override
    public void scaleDurations(long scaleFactor) throws UnsupportedOperationException {
        this.getNotationElements().scaleDurations(scaleFactor);
    }

    @Override
    public List<NotationNote> getNotationNotes() {
        return this.getNotationElements().getNotationNotes();
    }
                
}
