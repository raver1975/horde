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
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that provides static methods to manage temporary file resources.
 * 
 * @author Myron
 */
public class FileHelper {

    /**
     * The new line string for this environment.
     */
    public static final String NEW_LINE = System.getProperty("line.separator");
    
    /**
     * The temporary directory for this system.
     */
    private static File tempDirectory;
    
    /**
     * Interface for using a temporary file.
     */
    public interface TempFileUser {
        /**
         * Uses a temporary file.
         * 
         * @param tempFileName the name of the temp file to use
         * @throws Exception if there is an error
         */
        public void useTempFile(String tempFileName) throws Exception;
    }

    /**
     * Interface for using a buffered writer.
     */
    public interface BufferedWriterUser {
        /**
         * Uses a buffered writer.
         *
         * @param bufferedWriter the bufferd writer to use
         * @throws IOException if there is an I/O error
         */
        public void useBufferedWriter(BufferedWriter bufferedWriter) throws IOException ;
    }

    /**
     * Interface used by method deleteNewTransientFiles.
     */
    public interface TransientFileUser {
        /**
         * Do something that might create some transient files.
         *
         * @throws Exception if an error occurs
         */
        public void doWork() throws Exception;
    }

    /**
     * Creates a temporary file, and allows a TempFileUser to use it.  When it
     * is complete, the file is deleted.
     *
     * @param tempFilePrefix the temp file prefix
     * @param tempFileSuffix the temp file suffix
     * @param tempFileUser the object that will use the temp file
     * @throws Exception if an error occurs
     */
    public static void createAndUseTempFile(String tempFilePrefix, String tempFileSuffix, TempFileUser tempFileUser) throws Exception {
        File tempFile = null;
        try {
            tempFile = File.createTempFile(tempFilePrefix, tempFileSuffix);
            String tempFileName = tempFile.getCanonicalPath();
            tempFileUser.useTempFile(tempFileName);
        } finally {
            attemptTempFileDelete(tempFile);
        }
    }

    /**
     * Creates a buffered writer to write to a file using UTF-8 encoding and
     * allows a BufferedWriterUser to use it.  When it is complete, the buffered
     * writer is flushed and all the consumed resources are released.
     *
     * @param fileName the name of the file
     * @param bufferedWriterUser the object that will use the buffered writer
     * @throws IOException if an I/O error occurs
     */
    public static void createAndUseFileBufferedWriter(String fileName, BufferedWriterUser bufferedWriterUser) throws IOException {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            fos = new FileOutputStream(fileName);
            osw = new OutputStreamWriter(fos, "UTF-8");
            bw = new BufferedWriter(osw);
            bufferedWriterUser.useBufferedWriter(bw);
        } finally {
            // first, flush the buffered writer...
            if (bw != null) bw.flush();

            // close our streams, in the order we opened them
            if (fos != null) fos.close();
            if (osw != null) osw.close();
            if (bw != null) bw.close();
        }
    }

    /**
     * Creates a text file, using UTF-8 encoding.
     *
     * @param fileName the name of the text file
     * @param fileContents the text contents to put in the text file
     * @throws IOException if there is an I/O error
     */
    public static void createTextFile(final String fileName, final String fileContents) throws IOException {
        FileHelper.createAndUseFileBufferedWriter(fileName, new FileHelper.BufferedWriterUser() {
            public void useBufferedWriter(BufferedWriter bufferedWriter) throws IOException {
                boolean firstLineWritten = false;
                for (String line : fileContents.split(FileHelper.NEW_LINE)) {
                    if (firstLineWritten) bufferedWriter.newLine();
                    bufferedWriter.write(line);
                    firstLineWritten = true;
                }
            }
        });
    }

    /**
     * Reads the contents of a file into a string using UTF-8 encoding.
     *
     * @param fileName the name of the file
     * @return the string contents of the file
     * @throws IOException if an I/O error occurs
     */
    public static String readFileIntoString(String fileName) throws IOException {
        StringBuilder strBuilder = new StringBuilder();
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        String line;
        boolean firstLineRead = false;
        try {
            fis = new FileInputStream(fileName);
            isr = new InputStreamReader(fis, "UTF-8");
            br = new BufferedReader(isr);

            while (true) {
                line = br.readLine();
                if (line == null) break;

                if (firstLineRead) strBuilder.append(NEW_LINE);
                strBuilder.append(line);
                firstLineRead = true;
            }

            return strBuilder.toString();
        } finally {
            if (fis != null) fis.close();
            if (isr != null) isr.close();
            if (br != null) br.close();
        }
    }

    /**
     * Manages the deletion of transient files--i.e. files created by some
     * external process that are no longer needed and can be deleted.  The
     * transientFileFilter determines what qualifies as a transient file.
     * Any existing files the match this filter will be kept; any files that
     * are created by the transientFileUser.doWork() method that match the
     * filter will be deleted.
     *
     * @param directory the directory in which the transient files will be
     *        created
     * @param transientFileFilter filter that determines what is a transient
     *        file
     * @param transientFileUser object containing a doWork() method that might
     *        create some transient files
     * @throws Exception if an error occurs
     */
    public static void deleteNewTransientFiles(File directory, FileFilter transientFileFilter, TransientFileUser transientFileUser) throws Exception {
        List<File> originalFilesMatchingTransientFilter = Arrays.asList(directory.listFiles(transientFileFilter));

        try {
            transientFileUser.doWork();
        } finally {
            List<File> filesMatchingTransientFilter = new ArrayList<File>(Arrays.asList(directory.listFiles(transientFileFilter)));
            filesMatchingTransientFilter.removeAll(originalFilesMatchingTransientFilter);
            for (File file : filesMatchingTransientFilter) {
                attemptTempFileDelete(file);
            }
        }
    }

    /**
     * Attempts to delete a temporary file.  If an error occurs, it is written
     * to the log rather than propigated up the call stack since it is usually
     * OK to leave temp files (but preferable to delete them, of course).
     *
     * @param tempFile the file to delete
     */
    public static void attemptTempFileDelete(File tempFile) {
        if (tempFile == null || !tempFile.exists()) return;
        try {
            tempFile.delete();
        } catch (Exception ex) {
            // It's a temp file, so if we can't delete it, it's not really a big deal.
            // We catch the exception and just log the error instead of passing
            // the exception up the call stack.
            Logger.getLogger(FileHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gets the temporary directory used on this system.
     *
     * @return the temp directory
     * @throws IOException if an error occurred while getting the temp
     *         directory
     */
    public synchronized static File getTempDirectory() throws IOException {
        if (tempDirectory == null) {
            File tempFile = File.createTempFile("Test", ".txt");
            tempDirectory = tempFile.getParentFile();
            assert tempDirectory.isDirectory();
            assert tempDirectory.exists();
            attemptTempFileDelete(tempFile);
        }

        return tempDirectory;
    }

    /**
     * Returns the file name without the extension or folder path.
     *
     * @param fileName the full name of the file
     * @param extension the file extension
     * @return the raw file name
     */
    public static String getRawFileName(String fileName, String extension) {
        String rawFileName = FileHelper.stripFileExtension(new File(fileName).getName(), extension);
        assert !rawFileName.contains(File.separator);
        return rawFileName;
    }

    /**
     * Returns the file name without the extension.  If the file does not have
     * the expected extension, throws an IllegalArgumentException.
     *
     * @param fileName the file name
     * @param expectedFileExtension the expected extension
     * @return the file name without the extension
     * @throws IllegalArgumentException if the file name does not
     *         have the extension
     */
    public static String stripFileExtension(String fileName, String expectedFileExtension) throws IllegalArgumentException {
        if (!fileName.endsWith(expectedFileExtension)) throw new IllegalArgumentException("The fileName must have a " + expectedFileExtension + " extension.");
        return fileName.substring(0, fileName.length() - expectedFileExtension.length());        
    }
    
    /**
     * Checks to see if the given file exists.
     * 
     * @param fileName the file name to check
     * @return true if it exists; false if it does not or is a directory rather 
     *         than a file
     */
    public static boolean fileExists(String fileName) {
        File testFile = new File(fileName);
        return testFile.exists() && testFile.isFile();
    }
}
