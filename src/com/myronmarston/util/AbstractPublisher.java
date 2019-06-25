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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * My own implementation of the Observer pattern, that overcomes some of the 
 * deficiencies of the Java library implementation.  This 
 * implementation addresses the following issues:
 *      1. removeSubscriber() finds the appropriate subscriber using 
 *         reference equality rather than the equals() method.
 *      2. clone() clears the subscriber list as we don't usually want to 
 *         have our subscribers subscribe to both the original and the clone
 *         
 * 
 * @author Myron
 */
public abstract class AbstractPublisher implements Publisher, Cloneable {
    private ArrayList<Subscriber> subscribers = new ArrayList<Subscriber>();
    
    /**
     * Gets a read-only list of subscribers.
     * 
     * @return the list of subscribers
     */
    public List<Subscriber> getSubscribers() {
        return Collections.unmodifiableList(subscribers);
    }
    
    public void addSubscriber(Subscriber s) {
        subscribers.add(s);
    }

    public void notifySubscribers(Object args) {
        for (Subscriber s : this.subscribers) {
            s.publisherNotification(this, args);
        }
    }

    public void removeAllSubscribers() {
        subscribers.clear();
    }

    public void removeSubscriber(Subscriber s) {        
        // ArrayList.remove(Object o) uses the equals() method to find a match.
        // We want to remove the exact object passed in, not just one that 
        // evaluates to equal, so we manually test for references equality.
        for (int i = 0; i < this.subscribers.size(); i++) {
            if (subscribers.get(i) == s) subscribers.remove(i);
        }
    }    

    @Override
    @SuppressWarnings("unchecked")
    /**
     * Clones this object.  The subscriber list is not cloned--instead a new
     * list is created.
     */
    public AbstractPublisher clone() {
        AbstractPublisher clonedAP;
        
        try {
            clonedAP = (AbstractPublisher) super.clone();
        } catch (CloneNotSupportedException ex) {
            // We have implemented the Cloneable interface, so we should never
            // get this exception.  If we do, there's something very, very wrong...
            throw new UndeclaredThrowableException(ex, "Unexpected error while cloning.  This indicates a programming or JVM error.");
        }  
        
        // make our subscribers be a new list as we don't want to 
        // have our subscribers subscribed to both the original and this 
        // clone
        clonedAP.subscribers = new ArrayList<Subscriber>();
        return clonedAP;                 
    }        
}
