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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * A class that redirects an input stream to the System.out stream.  This
 * runs on its own thread.
 * 
 * @author Myron
 */
public class ThreadedInputStreamRedirector extends Thread {
    private final InputStream inputStream;          
    private final OutputStream redirectStream;

    /**
     * Constructor.
     * 
     * @param inputStream the input stream to redirect
     * @param redirectStream 
     */    
    public ThreadedInputStreamRedirector(InputStream inputStream, OutputStream redirectStream) {
        this.inputStream = inputStream;
        this.redirectStream = redirectStream;
    }                

    @Override
    /**
     * Starts the process of redirecting the stream on its own thread.
     */
    public void run() {
        InputStreamReader iReader = null;
        OutputStreamWriter oWriter = null;        
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        try {        
            try {
                iReader = new InputStreamReader(this.inputStream);
                oWriter = new OutputStreamWriter(this.redirectStream);
                bufferedReader = new BufferedReader(iReader);            
                bufferedWriter = new BufferedWriter(oWriter);

                String line = null;
                boolean firstLineComplete = false;
                while (true) {            
                    try {
                        line = bufferedReader.readLine();                
                    } catch (IOException ex) {
                        // we can't simply declare this exception on our run method 
                        // since it's not declared on the superclass's run method,
                        // so we wrap it in an unchecked exception to pass it on 
                        // up the stack...
                        throw new UndeclaredThrowableException(ex, "I/O Error reading line.");                
                    }  

                    if (line == null) break;
                    
                    if (firstLineComplete) bufferedWriter.newLine();
                    bufferedWriter.write(line);                    
                    firstLineComplete = true;
                }         
            } finally {
                iReader.close();
                bufferedReader.close();
                oWriter.flush();
                bufferedWriter.flush();                
                oWriter.close();                
                bufferedWriter.close();                                
            }       
        } catch (IOException ex) {
            // to conform to the runnable interface, we can't throw any checked exceptions,
            // so we catch this and transform it to an unchecked exception
            throw new UndeclaredThrowableException(ex, "An error occured while redirecting the input stream: " + ex.getMessage());
        }            
    }                      
}