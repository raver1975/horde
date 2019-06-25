/*
  File: Fraction.java
  Downloaded from: http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/misc/Fraction.java
  This has been moved to the com.myronmarston.util package because JRuby seems
  to have issues importing from an edu package.

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  7Jul1998  dl               Create public version
  11Oct1999 dl               add hashCode
*/

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

package com.myronmarston.util;

import com.myronmarston.music.notation.NotationNote;
import org.simpleframework.xml.*;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An immutable class representing fractions as pairs of longs.
 * Fractions are always maintained in reduced form.
 **/
@Root
public class Fraction implements Cloneable, Comparable, java.io.Serializable {
  @Attribute
  /** The numerator of this fraction. */
  protected final long numerator_;
  
  @Attribute
  /** The denominator of this fraction. */
  protected final long denominator_;
  
  private final static String FRACTION_CORE_REGEX_STRING_WITHOUT_EDGE_ASSERTIONS = "(%s(?:[1-9](?:\\d)*))(?:\\/([1-9](?:\\d)*))?";
  private final static String FRACTION_CORE_REGEX_STRING = String.format("^%s$", FRACTION_CORE_REGEX_STRING_WITHOUT_EDGE_ASSERTIONS);
  
  /** Regular expression string to parse a fraction string. */
  public final static String FRACTION_REGEX_STRING = String.format(FRACTION_CORE_REGEX_STRING, "0|-?");
  
  /** Regular expression string to parse a fraction string. */
  public final static String NON_NEGATIVE_FRACTION_REGEX_STRING = String.format(FRACTION_CORE_REGEX_STRING, "0|");
  
  /** Regular expression string to parse a fraction string that disallows zeros. */
  public final static String POSITIVE_FRACTION_REGEX_STRING = String.format(FRACTION_CORE_REGEX_STRING, "");
  
  /** Regular expression string to parse a fraction in the midst of other test. */
  public final static String POSITIVE_FRACTION_REGEX_STRING_WITHOUT_EDGE_ASSERTIONS = String.format(FRACTION_CORE_REGEX_STRING_WITHOUT_EDGE_ASSERTIONS, "");
  
  /** Regular expression pattern to parse a fraction string. */
  private final static Pattern FRACTION_REGEX_PATTERN = Pattern.compile(FRACTION_REGEX_STRING);  
  
  /**
   * The maximum duration denominator supported by lilypond.
   */
  public static final long MAX_ALLOWED_DURATION_DENOM = 64L;
  
  /**
   * The symbol that represents a tie in lilypond notation.
   */
  private static final String LILYPOND_TIE = " ~ ";
    
  /**
   * The character used to augment rhythmic durations in lilypond notation.
   */
  private static final char LILYPOND_AUGMENTATION_CHAR = '.';
        
  /** Return the numerator **/  
  public final long numerator() { return numerator_; }

  /** Return the denominator **/
  public final long denominator() { return denominator_; }

  /** Exists to allow xml deserialization using the simple framework **/   
  private Fraction() {
      this(0, 1);
  };
  
  /**
   * Constructs a fraction by parsing a fraction string.
   * 
   * @param fractionStr a string in fraction ("1/4") or integer ("23") form
   * @throws IllegalArgumentException if the string does not match
   *         the expected pattern or if the denominator is zero
   */
  public Fraction(String fractionStr) throws IllegalArgumentException {
    Matcher match = FRACTION_REGEX_PATTERN.matcher(fractionStr);
    if (!match.matches()) throw new IllegalArgumentException("The fraction string '" + fractionStr + "' is not in a recognized form.");
    String numStr = match.group(1);
    String denStr = match.group(2);
    Long num = Long.parseLong(numStr);
    Long den = (denStr == null || denStr.isEmpty() ? 1L : Long.parseLong(denStr));    
    
    // the code below is copied from the other constructor.
    // it would be better to make a helper method with this logic,
    // but numerator_ and denominator_ are final, so we can only set them
    // in a constructor.  Besides, this code has been well tested, so there is
    // no reason to expect this to ever change.
    
    // normalize while constructing    
    boolean numNonnegative = (num >= 0);
    boolean denNonnegative = (den >= 0);
    long a = numNonnegative? num : -num;
    long b = denNonnegative? den : -den;
    long g = gcd(a, b);
    numerator_ = (numNonnegative == denNonnegative)? (a / g) : (-a / g);
    denominator_ = b / g;
  }
  
  /** Create a Fraction equal in value to num / den **/
  public Fraction(long num, long den) throws IllegalArgumentException {
    if (den == 0L) throw new IllegalArgumentException("The fraction denominator cannot be zero."); 
      
    // normalize while constructing
    boolean numNonnegative = (num >= 0);
    boolean denNonnegative = (den >= 0);
    long a = numNonnegative? num : -num;
    long b = denNonnegative? den : -den;
    long g = gcd(a, b);
    numerator_ = (numNonnegative == denNonnegative)? (a / g) : (-a / g);
    denominator_ = b / g;
  }

  /** Create a fraction with the same value as Fraction f **/
  public Fraction(Fraction f) {
    numerator_ = f.numerator();
    denominator_ = f.denominator();
  }  

  /** 
   * Gets a string representation of this fraction. 
   * 
   * @return a string
   */
  public String toString() { 
    if (denominator() == 1) 
      return "" + numerator();
    else
      return numerator() + "/" + denominator(); 
  }
  
  /**
   * Gets a string representation of this fraction in GUIDO notation for a
   * note duration.
   * 
   * @return the guido string
   */
  public String toGuidoString() {      
      if (this.numerator() == 1L) return "/" + this.denominator();
      return "*" + this.toString();
  }

  /**
   * Gets a string representing this duration fraction in lilypond notation.  
   * The string will have format placeholders for where the rest of the lilypond
   * notation note should go.
   *       
   * @param timeLeftInBar the amount of time that is left in the current bar, 
   *        in absolute terms (i.e. not scaled by the tupletMultiplier)
   * @param barLength the length of a bar in absolute terms (i.e. not scaled by
   *        the tuplet multiplier)
   * @param tupletMultiplier the tuplet multiplier that has been applied
   *        to this notation note duration
   * @return the lilypond duration string for the given duration
   * @throws IllegalArgumentException if the dnominator of the duration is
   *         greater than the miximum supported by lilypond
   * @throws UnsupportedOperationException if the duration is a tuplet 
   *         duration
   */
  public String toLilypondString(Fraction timeLeftInBar, Fraction barLength, Fraction tupletMultiplier) throws IllegalArgumentException, UnsupportedOperationException {
      String lilypondString = toLilypondString(timeLeftInBar, barLength, tupletMultiplier, true);

      // Lilypond blows up when a note with a tuplet multiplier is tied to another note,
      // such as '/times 2/3 { c4 } ~ d4'.  We'll remove the tie here, which isn't strictly
      // the correct notation, but it's better to have lilypond produce something than nothing...
      if (lilypondString.contains("times")) lilypondString = lilypondString.replaceAll(LILYPOND_TIE, " ");

      // we should only have the dynamic placeholder once in this string...
      assert lilypondString.contains(NotationNote.NOTE_PLACEHOLDER_2) : lilypondString;
      assert lilypondString.lastIndexOf(NotationNote.NOTE_PLACEHOLDER_2) == lilypondString.indexOf(NotationNote.NOTE_PLACEHOLDER_2) : lilypondString;

      // the dynamic place holder should only be on the first of all the tied
      // notes, so check that it is before the tie string...
      assert !lilypondString.contains(LILYPOND_TIE) || lilypondString.indexOf(NotationNote.NOTE_PLACEHOLDER_2) < lilypondString.indexOf(LILYPOND_TIE);
      
      return lilypondString;      
  }
  
    private String toLilypondString(Fraction timeLeftInBar, Fraction barLength, Fraction tupletMultiplier, boolean firstNoteInListOfTiedNotes) throws IllegalArgumentException, UnsupportedOperationException {
      if (this.denominator_ > MAX_ALLOWED_DURATION_DENOM) throw new IllegalArgumentException("The given duration (" + this.toString() + ") has a denominator that is outside of the allowed range.  The denominator cannot be greater than " + MAX_ALLOWED_DURATION_DENOM + ".");
      assert timeLeftInBar.compareTo(barLength) <= 0 : timeLeftInBar;

      if (!denomIsPowerOf2()) {
          // We shouldn't actually get here for tuplet notes--instead, we'll
          // get here when a non-tuplet note is split across a bar, splitting
          // at an irregular point, i.e. splitting 1/4 into 1/6 | 1/12.

          // We'll wrap the note in an appropriate tuplet multiplier in this case.
          Fraction desiredFraction = new Fraction(1, MathHelper.getLargestPowerOf2LessThanGivenNumber(this.denominator_));
          Fraction tupletMultiplierForThisFraction = this.dividedBy(desiredFraction);
          tupletMultiplier = tupletMultiplier.times(tupletMultiplierForThisFraction);

          return "\\times " +
                  tupletMultiplierForThisFraction.toString() +
                  " { " +
                  desiredFraction.toLilypondString(timeLeftInBar, barLength, tupletMultiplier, firstNoteInListOfTiedNotes) +
                  " }";
      }

      // if there is no time left in the bar, start a whole new bar...
      if (timeLeftInBar.numerator_ == 0) timeLeftInBar = barLength;

      Fraction tupletScaledTimeLeftInBar = timeLeftInBar.dividedBy(tupletMultiplier);          

      // this notationDuration should already be tuplet scaled, so compare it to the tupletScaledTimeLeftInBar...
      if (this.compareTo(tupletScaledTimeLeftInBar) > 0) {
          // this duration lasts longer than the time we have left in the bar,
          // so we need to split it and use a tie to connect
          
          Fraction tupletScaledRemaining = this.minus(tupletScaledTimeLeftInBar);
          return tupletScaledTimeLeftInBar.toLilypondString(timeLeftInBar, barLength, tupletMultiplier, true && firstNoteInListOfTiedNotes) 
                 + LILYPOND_TIE + 
                 tupletScaledRemaining.toLilypondString(barLength, barLength, tupletMultiplier, false);      
      } else {
          // we only want to put a dynamic or articulation marking on the first
          // note of the tied notes, so we have a special place holder for that
          String afterFirstNoteString = (firstNoteInListOfTiedNotes ? NotationNote.NOTE_PLACEHOLDER_2 : "");

          // take care of the easy cases: notes such as 1/4, 1/8, and
          // notes using augmentation dots (3/8, 7/16, etc).
          switch ((int) this.numerator_) {
              case 1: return NotationNote.NOTE_PLACEHOLDER +
                             Long.toString(this.denominator_) +
                             afterFirstNoteString;
              case 3: if (this.denominator_ < 2) break;
                      return NotationNote.NOTE_PLACEHOLDER +
                             Long.toString(this.denominator_ / 2) +
                             LILYPOND_AUGMENTATION_CHAR +
                             afterFirstNoteString;
              case 7: if (this.denominator_ < 4) break;
                      return NotationNote.NOTE_PLACEHOLDER +
                             Long.toString(this.denominator_ / 4) +
                             LILYPOND_AUGMENTATION_CHAR +
                             LILYPOND_AUGMENTATION_CHAR +
                             afterFirstNoteString;
          }

          // split the duration into two seperate durations that can be tied together
          Fraction d1 = this.getLargestPowerOf2FractionThatIsLessThanThis();
          
          // if splitting it this way gives us a duration longer than our time left in the bar,
          // just use the time left in the bar instead...
          if (d1.compareTo(timeLeftInBar) > 0) d1 = timeLeftInBar;
          Fraction d2 = this.minus(d1);        
          Fraction timeLeftInBar2 = timeLeftInBar.minus(d1);
          assert timeLeftInBar2.compareTo(0L) >= 0 : timeLeftInBar2;

          return d1.toLilypondString(timeLeftInBar, barLength, tupletMultiplier, true && firstNoteInListOfTiedNotes)
                 + LILYPOND_TIE + 
                 d2.toLilypondString(timeLeftInBar2, barLength, tupletMultiplier, false);      
      }      
  }
   
  /**
   * Clones this fraction.
   * 
   * @return the clone
   */
  @Override
  public Fraction clone() { 
    try {
      return (Fraction) super.clone();
    } catch (CloneNotSupportedException ex) {
      // We have implemented the Cloneable interface, so we should never
      // get this exception.  If we do, there's something very, very wrong...
      throw new UndeclaredThrowableException(ex, "Unexpected error while cloning.  This indicates a programming or JVM error.");
    }      
  }

  /** Return the value of the Fraction as a double **/
  public double asDouble() { 
    return ((double)(numerator())) / ((double)(denominator()));
  }

  /** 
   * Compute the nonnegative greatest common divisor of a and b.
   * (This is needed for normalizing Fractions, but can be
   * useful on its own.)
   **/
  public static long gcd(long a, long b) { 
    long x;
    long y;

    if (a < 0) a = -a;
    if (b < 0) b = -b;

    if (a >= b) { x = a; y = b; }
    else        { x = b; y = a; }

    while (y != 0) {
      long t = x % y;
      x = y;
      y = t;
    }
    return x;
  }

  /** return a Fraction representing the negated value of this Fraction **/
  public Fraction negative() {
    long an = numerator();
    long ad = denominator();
    return new Fraction(-an, ad);
  }

  /** return a Fraction representing 1 / this Fraction **/
  public Fraction inverse() {
    long an = numerator();
    long ad = denominator();
    return new Fraction(ad, an);
  }

  /** return a Fraction representing this Fraction plus b **/
  public Fraction plus(Fraction b) {
    long an = numerator();
    long ad = denominator();
    long bn = b.numerator();
    long bd = b.denominator();
    return new Fraction(an*bd+bn*ad, ad*bd);
  }

  /** return a Fraction representing this Fraction plus n **/
  public Fraction plus(long n) {
    long an = numerator();
    long ad = denominator();
    long bn = n;
    long bd = 1;
    return new Fraction(an*bd+bn*ad, ad*bd);
  }

  /** return a Fraction representing this Fraction minus b **/
  public Fraction minus(Fraction b) {
    long an = numerator();
    long ad = denominator();
    long bn = b.numerator();
    long bd = b.denominator();
    return new Fraction(an*bd-bn*ad, ad*bd);
  }

  /** return a Fraction representing this Fraction minus n **/
  public Fraction minus(long n) {
    long an = numerator();
    long ad = denominator();
    long bn = n;
    long bd = 1;
    return new Fraction(an*bd-bn*ad, ad*bd);
  }

  /** return a Fraction representing this Fraction times b **/
  public Fraction times(Fraction b) {
    long an = numerator();
    long ad = denominator();
    long bn = b.numerator();
    long bd = b.denominator();
    return new Fraction(an*bn, ad*bd);
  }

  /** return a Fraction representing this Fraction times n **/
  public Fraction times(long n) {
    long an = numerator();
    long ad = denominator();
    long bn = n;
    long bd = 1;
    return new Fraction(an*bn, ad*bd);
  }

  /** return a Fraction representing this Fraction divided by b **/
  public Fraction dividedBy(Fraction b) {
    long an = numerator();
    long ad = denominator();
    long bn = b.numerator();
    long bd = b.denominator();
    return new Fraction(an*bd, ad*bn);
  }

  /** return a Fraction representing this Fraction divided by n **/
  public Fraction dividedBy(long n) {
    long an = numerator();
    long ad = denominator();
    long bn = n;
    long bd = 1;
    return new Fraction(an*bd, ad*bn);
  }    

  /** return a number less, equal, or greater than zero
   * reflecting whether this Fraction is less, equal or greater than 
   * the value of Fraction other.
   **/
  public int compareTo(Object other) {
    Fraction b = (Fraction)(other);
    long an = numerator();
    long ad = denominator();
    long bn = b.numerator();
    long bd = b.denominator();
    long l = an*bd;
    long r = bn*ad;
    return (l < r)? -1 : ((l == r)? 0: 1);
  }

  /**
   * Returns a number less, equal, or greater than zero
   * reflecting whether this Fraction is less, equal or greater than n.
   * 
   * @param n the number to compare to
   * @return value indicating whether this fraction is less, equal to or greater
   *         than n
   */
  public int compareTo(long n) {
    long an = numerator();
    long ad = denominator();
    long bn = n;
    long bd = 1;
    long l = an*bd;
    long r = bn*ad;
    return (l < r)? -1 : ((l == r)? 0: 1);
  }

  /**
   * Tests if this fraction equals the passed value.
   * 
   * @param other the value to check for equality 
   * @return true if the objects are equal
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Fraction)) return false;
    return compareTo((Fraction)other) == 0;
  }

  /**
   * Tests if this fraction equals the passed value.
   * 
   * @param n the value to check for equality 
   * @return true if the objects are equal
   */
  public boolean equals(long n) {
    return compareTo(n) == 0;
  }

  /**
   * Gets a hash code for this fraction.
   * 
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return (int) (numerator_ ^ denominator_);
  }
  
  /**
   * Checks to see if the denominator is a pwer of 2.
   * 
   * @return true if the denominator is a power of 2
   */
  public boolean denomIsPowerOf2() {
      return MathHelper.numIsPowerOf2(this.denominator_);
  }
  
  /**
   * Gets the largest fraction that is less than this fraction and has a 
   * denominator that is a power of 2.  This function assumes the denominator
   * already is a power of 2 and the numerator is greater than 1.
   * 
   * @return the largest fraction less than this fraction with a denominator
   *         power of 2
   * @throws UnsupportedOperationException if the fraction's denominator is not
   *         a power of 2 or the numerator is less than or equal to 1.
   */
  public Fraction getLargestPowerOf2FractionThatIsLessThanThis() throws UnsupportedOperationException {
      if (!this.denomIsPowerOf2() || this.numerator_ <= 1L) {
          throw new UnsupportedOperationException("This method is not supported on this fraction (" + this.toString() + ").  It is only supported on fractions that have a denominator power of 2 and a numerator greater than 1.");
      }
      
      Fraction subtractionAmount = new Fraction(1, this.denominator_);
      Fraction test = this.minus(subtractionAmount);
      while (!MathHelper.numIsPowerOf2(test.denominator_)) {
          test = test.minus(subtractionAmount);
          
          // We don't intend this to ever return a 0 or negative fraction...
          assert test.numerator_ > 0 : test.numerator_;
      }
      
      return test;
  }
}
