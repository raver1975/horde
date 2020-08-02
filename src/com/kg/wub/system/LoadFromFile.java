package com.kg.wub.system;

import com.echonest.api.v4.TrackAnalysis;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class LoadFromFile {

	public synchronized  static Song loadSong(String file){
		return loadSong(new File(file));
	}

	public synchronized static Song loadSong(File file1){
		String file=file1.getAbsolutePath();
		byte[] data;
		TrackAnalysis ta;
		System.out.println("file:"+file);
		if (file.endsWith(".au")){
			data=loadData(file);
			ta=loadAnalysis(file.replace(".au", ".an"));
			return new Song(data,ta);
		}
		if (file.endsWith(".an")){
			data=loadData(file.replace(".an", ".au"));
			ta=loadAnalysis(file);
			return new Song(data,ta);
		}
		return null;
	}

	public synchronized static byte[] loadData(String file) {
		Path path = new File(file).toPath();
		try {
			byte[] data = Files.readAllBytes(path);
			return data;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public synchronized static TrackAnalysis loadAnalysis(String file) {
		Path path = new File(file).toPath();
		System.out.println("path="+path);
		try {
			byte[] data = Files.readAllBytes(path);
			ByteArrayInputStream bas = new ByteArrayInputStream(data);
			ObjectInputStream in = new ObjectInputStream(bas);
			Object pp=in.readObject();
			System.out.println("pp="+pp.getClass());
			return (TrackAnalysis)pp ;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

}
