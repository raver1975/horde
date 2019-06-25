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

import com.myronmarston.music.OutputManager;
import com.myronmarston.util.FileHelper;
import com.myronmarston.util.OSHelper;
import com.myronmarston.util.ProcessRunner;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that manages the creation of sheet music files using Lilypond and GUIDO.
 * 
 * @author Myron
 */
public class SheetMusicCreator {        
    private final OutputManager outputManager;
    private static final String GUIDO_2_GIF_EXE_FILE = "guido2gif";
    private static final String GUIDO_SUB_DIRECTORY = "guido";
    private static final String CURRENT_DIR = System.getProperty("user.dir");    
    private static String guidoParentDirectory = CURRENT_DIR;
    private static final String LILYPOND_EXE_FILE;
    private static final Pattern LILYPOND_OR_GUIDO_OUTPUT_ERROR = Pattern.compile(".*?error.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);       
    private static final Pattern LILYPOND_OR_GUIDO_OUTPUT_WARNING = Pattern.compile(".*?warning.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);       

    static {
        if (OSHelper.isMacOSX()) {
            // The path environment variable works a bit differently on OS X, and I haven't
            // found a way to get changes to the path to be picked up by netbeans when it
            // runs the test suite...so we'll just manually set it here.
            LILYPOND_EXE_FILE = "/Applications/LilyPond.app/Contents/Resources/bin/lilypond";
        } else {
            LILYPOND_EXE_FILE = "lilypond";
        }
    }

    /**
     * Constructor.
     * 
     * @param outputManager the output manager containing the piece of music to
     *        turn into sheet music
     */
    public SheetMusicCreator(OutputManager outputManager) {
        if (outputManager == null) throw new NullPointerException();
        this.outputManager = outputManager;
    }       
    
    /**
     * Gets the directory under which Guido is located.  Guido should be in a
     * directory called "Guido" under the specified directory.
     * 
     * @return the guido parent directory
     */
    synchronized public static String getGuidoParentDirectory() {
        return guidoParentDirectory;
    }

    /**
     * Sets the directory under which Guido is located.  Guido should be in a
     * directory called "Guido" under the specified directory.
     * 
     * @param guidoParentDirectory the guido parent directory
     */
    synchronized public static void setGuidoParentDirectory(String guidoParentDirectory) {
        SheetMusicCreator.guidoParentDirectory = guidoParentDirectory;
    }        
    
    /**
     * Saves the sheet music as a gif image.
     * 
     * @param gifFileName the name of the gif file to save the image to
     * @param title the title of the piece
     * @param composer the composer of the piece
     * @return the output of the guido run, if there was a warning
     * @throws Exception if an error occurs
     */
    public String saveAsGifImage(final String gifFileName, final String title, final String composer) throws Exception {
        final StringBuilder returnStr = new StringBuilder();

        // create a temp file...
        FileHelper.createAndUseTempFile("TempGuido", ".gmn", new FileHelper.TempFileUser() {
            public void useTempFile(String tempFileName) throws Exception {
                saveGuidoFile(tempFileName, title, composer);

                ProcessBuilder pb = new ProcessBuilder();
                pb.directory(new File(SheetMusicCreator.getGuidoParentDirectory()));

                pb.command(
                     GUIDO_SUB_DIRECTORY + File.separator + GUIDO_2_GIF_EXE_FILE,
                    "-i", tempFileName,
                    "-o", gifFileName);

                String output = ProcessRunner.runProcess(pb);

                // if lilypond had a problem, throw an exception...
                if (!FileHelper.fileExists(gifFileName) || lilypondOrGuidoOutputIndicatesError(output)) {
                    throw new GuidoRunException(output);
                } else if (lilypondOrGuidoOutputIndicatesWarning(output)) {
                    returnStr.append(output);
                } else {
                    System.out.println(output);
                }
            }
        });

        return returnStr.toString();
    }

    /**
     * Saves the guido notation to a file.
     *
     * @param fileName the name of the file to save the guido notation to
     * @param title the title of the piece
     * @param composer the composer of the piece
     * @throws IOException if an I/O error occurs
     */
    public void saveGuidoFile(String fileName, String title, String composer) throws IOException {
        String guidoContent = this.outputManager.getPieceNotation().toGuidoString(title, composer);
        if (this.outputManager.getTestNotationError()) guidoContent += "}";
        FileHelper.createTextFile(fileName, guidoContent);
    }

    /**
     * Saves the lilypond notation to a file.
     *
     * @param fileName the name of the file to save the lilypond notation to
     * @param title the title of the piece
     * @param composer the composer of the piece
     * @param imageWidth the width of the image, or use 0 to use the default paper size
     * @throws IOException if an I/O error occurs
     */
    public void saveLilypondFile(String fileName, String title, String composer, int imageWidth) throws IOException {
        String lilypondContent = this.outputManager.getPieceNotation().toLilypondString(title, composer, imageWidth);
        if (this.outputManager.getTestNotationError()) lilypondContent += "}";
        FileHelper.createTextFile(fileName, lilypondContent);
    }

    /**
     * Saves the notation to a PDF file using lilypond.
     *
     * @param fileName the file name to save the results to.
     * @param title the title of the piece
     * @param composer the composer of the piece
     * @return the lilypond output if there was a warning
     * @throws Exception if there is an error
     */
    public String saveAsPdf(final String fileName, final String title, final String composer) throws Exception {
        final String rawFileName = FileHelper.getRawFileName(fileName, ".pdf");
        return this.runLilypond(
            fileName,
            title,
            composer,
            0,
            ".pdf",
            new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(rawFileName + ".ps");
                }
            },
            "--pdf"
        );
    }

    /**
     * Saves the notation as a png image using lilypond.
     *
     * @param fileName the name of the png file
     * @param title the title of the piece
     * @param composer the composer of the piece
     * @param imageWidth the desired image width
     * @return the lilypond output if there was a warning
     * @throws Exception if there is an error
     */
    public String saveAsPng(final String fileName, final String title, final String composer, final int imageWidth) throws Exception {
        final Pattern transientFilePattern = Pattern.compile(".*?" + FileHelper.getRawFileName(fileName, ".png") + "(.*?eps|-system.*?)", Pattern.DOTALL);
        return this.runLilypond(
            fileName,
            title,
            composer,
            imageWidth,
            ".png",
            new FileFilter() {
                public boolean accept(File pathname) {
                    return transientFilePattern.matcher(pathname.getName()).matches();
                }
            },
            // http://lilypond.org/doc/v2.11/Documentation/user/lilypond-program/Inserting-LilyPond-output-into-other-programs#Inserting-LilyPond-output-into-other-programs
            "-dbackend=eps",
            "-dno-gs-load-fonts",
            "-dinclude-eps-fonts",
            "--png"
        );
    }

    /**
     * Runs lilypond, using the given options.
     *
     * @param fileName the name of file to save the results to
     * @param title the title of the piece
     * @param composer the composer of the piece
     * @param imageWidth the desired image width in pixels, or 0 to use the
     *        default
     * @param fileExtension the file extension used
     * @param transientFileFilter filter that specifies which transient files
     *        to delete
     * @param lilypondCommandLineOptions list of command line options to pass
     *        to lilypond
     * @return the lilypond output, if there was a warning
     * @throws Exception if an error occurs
     */
    private String runLilypond(final String fileName, final String title, final String composer, final int imageWidth, final String fileExtension, final FileFilter transientFileFilter, final String ... lilypondCommandLineOptions) throws Exception {               
        final StringBuilder returnStr = new StringBuilder();
        
        File givenFile = new File(fileName);        
        final String fileNameWithoutExtension = FileHelper.stripFileExtension(givenFile.getName(), fileExtension);        
        File parentFile = givenFile.getParentFile();
        final File directory = (parentFile != null ? parentFile : new File(SheetMusicCreator.CURRENT_DIR));
        assert directory.isDirectory();
                        
        FileHelper.deleteNewTransientFiles(directory, transientFileFilter, new FileHelper.TransientFileUser() {
            public void doWork() throws Exception {
                FileHelper.createAndUseTempFile("Lilypond", ".ly", new FileHelper.TempFileUser() {
                    public void useTempFile(String tempFileName) throws Exception {                    
                       SheetMusicCreator.this.saveLilypondFile(tempFileName, title, composer, imageWidth);

                        List<String> commandLineOptions = new ArrayList<String>();
                        commandLineOptions.add(LILYPOND_EXE_FILE);
                        commandLineOptions.addAll(Arrays.asList(lilypondCommandLineOptions));
                        commandLineOptions.add("--output=" + fileNameWithoutExtension);
                        commandLineOptions.add(tempFileName);                        
                        
                        ProcessBuilder pb = new ProcessBuilder();                   
                        pb.directory(directory);                        
                        pb.command(commandLineOptions);

                        String output = ProcessRunner.runProcess(pb);                    

                        // if lilypond had a problem, throw an exception...
                        if (!FileHelper.fileExists(fileName) || lilypondOrGuidoOutputIndicatesError(output)) {
                            throw new LilypondRunException(output);
                        } else if (lilypondOrGuidoOutputIndicatesWarning(output)) {
                            returnStr.append(output);
                        } else {
                            System.out.println(output);   
                        }
                    }
                });        
            }
        });    
        
        return returnStr.toString();
    }
    
    /**
     * Checks the logging messages produced by lilypond or guido to see if it 
     * there was an error.  
     * 
     * @param ouptut the logging messages produced by lilypond or guido
     * @return true if there was an error
     */    
    private static boolean lilypondOrGuidoOutputIndicatesError(String output) {        
        Matcher errorMatches = SheetMusicCreator.LILYPOND_OR_GUIDO_OUTPUT_ERROR.matcher(output);
        return (errorMatches.matches());
    }   
    
    /**
     * Checks the logging messages produced by lilypond or guido to see if it 
     * there was a warning.  
     * 
     * @param output the logging messages produced by lilypond or guido
     * @return true if there was a warning
     */    
    private static boolean lilypondOrGuidoOutputIndicatesWarning(String output) {        
        Matcher warningMatches = SheetMusicCreator.LILYPOND_OR_GUIDO_OUTPUT_WARNING.matcher(output);
        return (warningMatches.matches());
    }     
}
