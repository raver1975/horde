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

/**
 * Interface for my own version of the Observer pattern, that overcomes some of 
 * the deficiencies of the Java librar implementation.  This is an interface to 
 * allow implementation when the publisher class must inherit from an existing 
 * super class.  AbstractPublisher provides a good default implementation and 
 * should be used in all other cases.
 * 
 * @author Myron
 */
public interface Publisher {
    
    /**
     * Adds a subscriber to the publisher.
     * 
     * @param s the subscriber to add
     */
    public void addSubscriber(Subscriber s);
    
    /**
     * Removes the given subscriber from the publisher.
     * 
     * @param s the subscriber to remove
     */
    public void removeSubscriber(Subscriber s);
    
    /**
     * Removes all subscribers.
     */
    public void removeAllSubscribers();
    
    /**
     * Notifies all subscribers, passing along the given arguments.
     * 
     * @param args an arguments object that can hold arbitrary data
     */
    public void notifySubscribers(Object args);
}
