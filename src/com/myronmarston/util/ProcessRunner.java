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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A class that manages the running of a process.
 * 
 * @author Myron
 */
public class ProcessRunner {          
    
    /**
     * Runs the given process, taking care of the necessary streams from the 
     * process.
     * 
     * @param processBuilder ProcessBuilder containing the process to be run
     * @return string containing the std and error output from the process
     * @throws IOException if an I/O exception occurs
     * @throws InterruptedException if the process is interrupted
     */
    public static String runProcess(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();                                                            
            
            // start the process...
            Process p = processBuilder.start();             
            
            // redirect the streams so that our process doesn't block...
            ThreadedInputStreamRedirector errorStr = new ThreadedInputStreamRedirector(p.getErrorStream(), baos);
            ThreadedInputStreamRedirector outputStr = new ThreadedInputStreamRedirector(p.getInputStream(), baos);
            errorStr.start();
            outputStr.start();

            // wait for the process to finish...
            p.waitFor();
            
            baos.flush();
            return baos.toString();                    
        } finally {
            if (baos != null) baos.close();
        }        
    }
}
