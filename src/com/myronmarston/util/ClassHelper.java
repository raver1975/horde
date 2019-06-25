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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author Myron
 */
public class ClassHelper {
    // 
    /**
     * Gets a list of all classes found in a particular package.  This method 
     * was taken from http://forum.java.sun.com/thread.jspa?threadID=341935&start=15.
     * 
     * @param packageName the name of the package to search for classes
     * @return a list of classes in the given package
     * @throws ClassNotFoundException if there is an error while
     *         searching for the classes
     */
    public static List<Class> getClassesInPackage(String packageName) throws ClassNotFoundException {
        // This will hold a list of directories matching the pckgname.
        //There may be more than one if a package is split over multiple jars/paths
        List<Class> classes = new ArrayList<Class>();
        ArrayList<File> directories = new ArrayList<File>();
        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) throw new ClassNotFoundException("Can't get class loader.");

            // Ask for all resources for the path
            Enumeration<URL> resources = cld.getResources(packageName.replace('.', '/'));
            while (resources.hasMoreElements()) {
                URL res = resources.nextElement();
                if (res.getProtocol().equalsIgnoreCase("jar")) {
                    JarURLConnection conn = (JarURLConnection) res.openConnection();
                    JarFile jar = conn.getJarFile();
                    for (JarEntry e:Collections.list(jar.entries())) {
                        if (e.getName().startsWith(packageName.replace('.', '/')) && e.getName().endsWith(".class") && !e.getName().contains("$")) {
                            String className = e.getName().replace("/",".").substring(0,e.getName().length() - 6);
                            classes.add(Class.forName(className));
                        }
                    }
                } else {
                    directories.add(new File(URLDecoder.decode(res.getPath(), "UTF-8")));
                }
            }
        } catch (NullPointerException x) {
            throw new ClassNotFoundException(packageName + " does not appear to be a valid package (Null pointer exception)");
        } catch (UnsupportedEncodingException encex) {
            throw new ClassNotFoundException(packageName + " does not appear to be a valid package (Unsupported encoding)");
        } catch (IOException ioex) {
            throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + packageName);
        }

        // For every directory identified capture all the .class files
        for (File directory : directories) {
            if (directory.exists()) {
                // Get the list of the files contained in the package
                String[] files = directory.list();
                for (String file : files) {
                    // we are only interested in .class files
                    if (file.endsWith(".class")) {
                        // removes the .class extension
                        classes.add(Class.forName(packageName + '.' + file.substring(0, file.length() - 6)));
                    }
                }
            } else {
                throw new ClassNotFoundException(packageName + " (" + directory.getPath() + ") does not appear to be a valid package");
            }
        }
        return classes;
    }

    /**
     * Gets a list of classes in a package that are subclasses of a given class.
     * This list will include both direct subclasses, and classes that are
     * subclassed through multiple levels of inheritance.
     *
     * @param packageName the name of the package to search
     * @param superClass all classes that are subclasses of this will be
     *        included in the list
     * @return list of subclasses of the given superClass
     * @throws ClassNotFoundException if there is a problem searching
     *         the package for classes
     */
    public static List<Class> getSubclassesInPackage(String packageName, Class superClass) throws ClassNotFoundException {
        List<Class> classList = new ArrayList<Class>();

        for (Class discovered : getClassesInPackage(packageName)) {            
            if (isClassSubclassOfClass(discovered, superClass)) {
                classList.add(discovered);
            }
        }
 
        return classList;
    }
    
    /**
     * Checks to see if a given class is a subclass of a given super class.
     * All levels of the super class hierarchy are checked.
     * 
     * @param classToTest the class to test
     * @param superClass the super class to test against
     * @return true if classToTest is a subclass of superClass; false otherwise
     */
    public static boolean isClassSubclassOfClass(Class classToTest, Class superClass) {
        if (classToTest == null) return false;
        
        // Return true if the super classes match; 
        // otherwise, recurse up the super class tree.
        // Eventually, getSuperclass() returns null, and the line above
        // will return false.
        return (classToTest.getSuperclass() == superClass) || isClassSubclassOfClass(classToTest.getSuperclass(), superClass);
    }

}
