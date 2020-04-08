package com.klemstinegroup.wub.system;

import java.io.*;

/*
 * Copyright (c) 2000 David Flanagan.  All rights reserved.
 * This code is from the book Java Examples in a Nutshell, 2nd Edition.
 * It is provided AS-IS, WITHOUT ANY WARRANTY either expressed or implied.
 * You may study, use, and modify it for any non-commercial purpose.
 * You may distribute it non-commercially as long as you retain this notice.
 * For a commercial use license, or to purchase the book (recommended),
 * visit http://www.davidflanagan.com/javaexamples2.
 */

/**
 * This class defines utility routines that use Java serialization.
 */
public class Serializer {
	/**
	 * Serialize the object o (and any Serializable objects it refers to) and
	 * store its serialized state in File f.
	 */
	public static void store(Serializable o, File f) throws IOException {
		System.out.println("file:"+f);
		ObjectOutputStream out = // The class for serialization
		new ObjectOutputStream(new FileOutputStream(f));
		out.writeObject(o); // This method serializes an object graph
		out.close();
	}

	public static byte[] toByteArray(Serializable o) throws IOException {
		ByteArrayOutputStream ba=new ByteArrayOutputStream();
		ObjectOutputStream out = // The class for serialization
		new ObjectOutputStream(ba);
		out.writeObject(o); // This method serializes an object graph
		out.close();
		return ba.toByteArray();
	}
	
	/**
	 * Deserialize the contents of File f and return the resulting object
	 */
	public static Object load(File f) throws IOException, ClassNotFoundException {
		ObjectInputStream in = // The class for de-serialization
		new ObjectInputStream(new FileInputStream(f));
		return in.readObject(); // This method deserializes an object graph
	}

	/**
	 * Use object serialization to make a "deep clone" of the object o. This
	 * method serializes o and all objects it refers to, and then deserializes
	 * that graph of objects, which means that everything is copied. This
	 * differs from the clone() method of an object which is usually implemented
	 * to produce a "shallow" clone that copies references to other objects,
	 * instead of copying all referenced objects.
	 */
	public static Object deepclone(final Serializable o) throws IOException, ClassNotFoundException {
		// Create a connected pair of "piped" streams.
		// We'll write bytes to one, and them from the other one.
		final PipedOutputStream pipeout = new PipedOutputStream();
		PipedInputStream pipein = new PipedInputStream(pipeout);

		// Now define an independent thread to serialize the object and write
		// its bytes to the PipedOutputStream
		Thread writer = new Thread() {
			public void run() {
				ObjectOutputStream out = null;
				try {
					out = new ObjectOutputStream(pipeout);
					out.writeObject(o);
				} catch (IOException e) {
				} finally {
					try {
						out.close();
					} catch (Exception e) {
					}
				}
			}
		};
		writer.start(); // Make the thread start serializing and writing

		// Meanwhile, in this thread, read and deserialize from the piped
		// input stream. The resulting object is a deep clone of the original.
		ObjectInputStream in = new ObjectInputStream(pipein);
		return in.readObject();
	}

}
