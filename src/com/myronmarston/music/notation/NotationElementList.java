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

import com.myronmarston.util.Fraction;
import com.myronmarston.util.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A list of notation elements.  This is intended to be used for a list of notes,
 * but can be used for any list of notation elements.
 *  
 * @author Myron
 */
public class NotationElementList extends ArrayList<NotationElement> implements NotationElement {
    // Used to serialize the class.  Change this if the class has a change significant enough to change the way the class is serialized.
    private static final long serialVersionUID = 1L;
    private String elementSeperator = "";    

    /**
     * Interface that specifies a method to use to convert a notation element to
     * a string.
     */    
    private interface ToNotationStringMethod {
        /**
         * Converts a notation element to a notation string.
         * 
         * @param element the element
         * @return the notation string
         */
        public String toNotationString(NotationElement element);
    }
    
    /**
     * Gets the string used to seperate the elements when getting the lilypond 
     * or guido string for this list.
     * 
     * @return the element seperator
     */
    public String getElementSeperator() {
        return elementSeperator;
    }

    /**
     * Sets the string used to seperate the elements when getting the lilypond
     * or guido string for this list.
     * 
     * @param elementSeperator the element seperator
     */
    public void setElementSeperator(String elementSeperator) {
        if (elementSeperator == null) throw new NullPointerException("elementSeperator cannot be null.");
        this.elementSeperator = elementSeperator;
    }
        
    /**
     * Constructor.
     */
    public NotationElementList() {
        super();
    }
        
    /**
     * Constructor.  Initializes the capacity of the list to the given size.
     * 
     * @param initialCapacity the initial capacity of the list
     */
    public NotationElementList(int initialCapacity) {
        super(initialCapacity);
    }
    
    /**
     * Constructor.  Initializes the list with the given elements.
     *      
     * @param elements collection of elements to put in the list
     */
    public NotationElementList(Collection<NotationElement> elements) {
        super(elements);
    }
    
    /**
     * Helper method for toLilypondString() and toGuidoString() containing
     * common logic.
     * 
     * @param toNotationStringMethod specifies whether toLilypondString() or
     *        toGuidoString() should be called on the elements.
     * @return the lilypond or guido string
     */
    private String toLilypondOrGuidoStringHelper(ToNotationStringMethod toNotationStringMethod) {
        StringBuilder strBuilder = new StringBuilder();
        boolean firstElementDone = false;
        
        for (NotationElement element : this) {
            if (firstElementDone) strBuilder.append(this.elementSeperator);
            strBuilder.append(toNotationStringMethod.toNotationString(element));
            firstElementDone = true;
        }
        
        return strBuilder.toString();
    }
    
    /**
     * Combines the lilypond strings of each element.
     * 
     * @return the lilypond string for this NotationElementList
     */
    public String toLilypondString() { 
        return toLilypondOrGuidoStringHelper(new NotationElementList.ToNotationStringMethod() {
            public String toNotationString(NotationElement element) {
                return element.toLilypondString();
            }
        });
    }

    /**
     * Combines the guido strings of each element.
     *
     * @return the guido string for this NotationElementList
     */
    public String toGuidoString() {
        return toLilypondOrGuidoStringHelper(new NotationElementList.ToNotationStringMethod() {
            public String toNotationString(NotationElement element) {
                return element.toGuidoString();
            }
        });
    }

    /**
     * Gets the largest duration denominator of this element.
     *
     * @return the largest duration denominator
     * @throws UnsupportedOperationException if this list has no
     *         elements that support duration scaling
     */
    public long getLargestDurationDenominator() throws UnsupportedOperationException {
        long largestDenom = Long.MIN_VALUE;
        boolean supported = false;

        for (NotationElement e : this) {
            if (e.supportsDurationScaling()) {
                supported = true;
                largestDenom = Math.max(largestDenom, e.getLargestDurationDenominator());
            }
        }

        if (!supported) throw new UnsupportedOperationException("getLargestDurationDenominator() is not supported on this element.");
        return largestDenom;
    }

    /**
     * Scales the durations by the given scale factor.  Each duration is
     * multiplied by it.
     *
     * @param scaleFactor the scale factor; should be a power of 2
     * @throws UnsupportedOperationException if this list has no
     *         elements that support duration scaling
     */
    public void scaleDurations(long scaleFactor) throws UnsupportedOperationException {        
        assert MathHelper.numIsPowerOf2(scaleFactor) : scaleFactor;
        boolean supported = false;
        for (NotationElement e : this) {
            if (e.supportsDurationScaling()) {
                supported = true;
                e.scaleDurations(scaleFactor);
            }
        }        
        
        if (!supported) throw new UnsupportedOperationException("scaleDurations() is not supported on this element.");
    }

    /**
     * Indicates whether or not this list supports duration scaling.
     * 
     * @return true if this list supports duration scaling
     */
    public boolean supportsDurationScaling() {
        for (NotationElement element : this) {
            if (element.supportsDurationScaling()) return true;
        }
        
        return false;
    }  
    
    /**
     * Gets a list of notation notes from owned by this list.  This combines 
     * and collapses all nested lists into a single list.
     * 
     * @return list of notation notes owned by this list
     */
    public List<NotationNote> getNotationNotes() {
        List<NotationNote> notes = new ArrayList<NotationNote>();
        for (NotationElement element : this) {
            if (element instanceof NotationNote) {
                notes.add((NotationNote) element);
            } else {
                notes.addAll(element.getNotationNotes());
            }            
        }
        return notes;
    }       
    
    /**
     * Checks to see if the sum total of the denominators of all the durations 
     * is a power of 2.
     * 
     * @param elements the list of elements to check
     * @return true if the sum total of all the duration denominators is a 
     *         power of 2
     */
    public static boolean totalDurationDenomAddsToPowerOf2(Collection<NotationElement> elements) {
        Fraction duration = new Fraction(0, 1);
        NotationNote note;
        for (NotationElement element : elements) {
            if (element instanceof NotationNote) {
                note = (NotationNote) element;
                duration = duration.plus(note.getDuration());
            }
        }
        
        return duration.denomIsPowerOf2();
    }
    
    /**
     * Removes the notes with duration denominators that are powers of two.
     */
    private void removeNotesWhoseDenomsArePowerOf2() {
        NotationElement element;
        NotationNote note;
                
        for (int i = this.size() - 1; i >= 0; i--) {
            // count down, so that we can remove the notes, and it won't change
            // the indices of the ones we haven't checked yet
            element = this.get(i);
            if (element instanceof NotationNote) {
                note = (NotationNote) element;
                if (note.getDuration().denomIsPowerOf2()) {
                    this.remove(i);
                }
            }
        }        
    }
    
    /**
     * Removes sublists whose sum total of all duration denominators is a 
     * power of 2.
     */
    private void removeSublistsWhoseDenomsAddToPowerOf2() {
        boolean sublistRemoved = false;
        
        for (int firstIndex = 0; !sublistRemoved && firstIndex < this.size() - 1; firstIndex++) {
            for (int lastIndex = firstIndex + 1; !sublistRemoved && lastIndex < this.size(); lastIndex++) {
                // we don't want to remove the entire list, just sub lists, so
                // skip the case where we have the first and last index of the entire list...
                if (firstIndex == 0 && lastIndex == this.size() - 1) continue;
                
                List<NotationElement> sublist = this.subList(firstIndex, lastIndex + 1);
                if (NotationElementList.totalDurationDenomAddsToPowerOf2(sublist)) {
                    sublist.clear();                    
                    
                    // now that we've removed a sublist, the indices will have 
                    // changed, so set our flag that will exit the for loops...
                    sublistRemoved = true;
                }
            }                
        }
        
        // run this algorithm again until we don't find any more sublists to remove...
        if (sublistRemoved) removeSublistsWhoseDenomsAddToPowerOf2();
    }
    
    /**
     * Removes individual notes, or groups of consecutive notes, with duration
     * denominators that are a power of 2.  This will leave notes with abnormal
     * durations.
     */
    public void removeConsecutiveNotesWhoseDenomsAddToPowerOf2() {
        this.removeNotesWhoseDenomsArePowerOf2();
        this.removeSublistsWhoseDenomsAddToPowerOf2();
    }
    
    /**
     * Finds the end index that should be used to create a tuplet collection.  
     * Beginning with the given beginIndex, the durations are summed until the
     * denominator is a power of two.  The notes between these two indices
     * should be part of the same tuplet group.
     * 
     * @param beginIndex the index the tuplet group to begins from
     * @return the index the tuplet group ends at
     */
    public int getEndIndexForTupletGroup(int beginIndex) {
        NotationElement element;
        NotationNote note;
        Fraction durationSoFar = new Fraction(0, 1);
        for (int i = beginIndex; i < this.size(); i++) {
            element = this.get(i);
            if (element instanceof NotationNote) {                
                note = (NotationNote) element;
                durationSoFar = durationSoFar.plus(note.getDuration());
                
                if (durationSoFar.denomIsPowerOf2()) return i;                
            }
        }
        
        return -1;
    }
    
    /**
     * Gets the smallest note duration denominator in this list.
     * 
     * @return the smallest note duration denominator
     * @throws UnsupportedOperationException if the list does not have any notes
     */
    public long getSmallestNoteDurationDenominator() throws UnsupportedOperationException {
        long smallestDenom = Long.MAX_VALUE;
        boolean noteFound = false;
        
        NotationNote note;
        for (NotationElement element : this) {
            if (element instanceof NotationNote) {
                noteFound = true;
                note = (NotationNote) element;
                smallestDenom = Math.min(smallestDenom, note.getDuration().denominator());
            }
        }
        
        if (!noteFound) throw new UnsupportedOperationException("This list does not have any notes, so this operation is not supported.");
        
        return smallestDenom;
    }
    
    /**
     * Searches the list for any notes that should be grouped as a tuplet and
     * wraps them in a Tuplet.
     */
    public void groupTuplets() {
        NotationNote note;        
        NotationElement element;
        Tuplet tuplet = null;
        int firstTupletIndex, lastTupletIndex = 0;
        
        // look through our list for the first note duration that does not have a denominator power of 2
        for (firstTupletIndex = 0; firstTupletIndex < this.size(); firstTupletIndex++) {
            element = this.get(firstTupletIndex);
            if (element instanceof NotationNote) {                
                note = (NotationNote) element;                
                if (!note.getDuration().denomIsPowerOf2()) {
                    // make a tuplet sublist...                    
                    lastTupletIndex = this.getEndIndexForTupletGroup(firstTupletIndex);
                    
                    if (lastTupletIndex != -1) {
                        tuplet = new Tuplet(this.subList(firstTupletIndex, lastTupletIndex + 1));
                    }              
                    
                    break;
                }
            } 
        }        
                
        if (tuplet != null) {
            // we created a tuplet sublist; replace the original range with
            // this new sublist...
            this.removeRange(firstTupletIndex, lastTupletIndex + 1);
            this.add(firstTupletIndex, tuplet);
            
            // we might have nested tuplets, so wrap those as well...
            tuplet.getNotes().groupTuplets();
            
            // We cannot deal with multiple tuplets in the same list in the same
            // pass through the list, since we change the indices of the elements
            // when we replace a range with a Tuplet.
            // Instead, we broke from our loop above, and now we just re-run
            // this method.  The tuplet we just dealt with will be skipped, and
            // it will continue looking through the list for later tuplets.
            this.groupTuplets();
        }
    }

    @Override
    public NotationElementList clone() {
        return (NotationElementList) super.clone();
    }
}
