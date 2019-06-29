 package com.myronmarston.synth;

 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;

 import static jdk.xml.internal.SecuritySupport.getClassLoader;

 public class Resources
 {
   private static Resources loader;
 
   public static double[] loadSample(String name)
   {
     return loadSample(name, 1.0D);
   }
 
   public static double[] loadSample(String name, double velocity) {
     byte[] bytes = load(name);
     double[] sample = new double[(int)Math.floor(bytes.length * 0.5D)];
     for (int i = 0; i < bytes.length; i += 2) {
       int s = bytes[(i + 1)] << 8 | bytes[(i + 0)] & 0xFF;
       sample[(int)(i * 0.5D)] = (s / 32768.0D * velocity);
     }
     return sample;
   }
   public static double[] loadSampleCyclic(String name) {
     return loadSampleCyclic(name, 1.0D);
   }
   public static double[] loadSampleCyclic(String name, double velocity) {
     byte[] bytes = load(name);
     double[] sample = new double[(int)Math.floor(bytes.length * 0.5D) + 3];
     for (int i = 0; i < bytes.length; i += 2) {
       int s = bytes[(i + 1)] << 8 | bytes[(i + 0)] & 0xFF;
       sample[((int)(i * 0.5D) + 1)] = (s / 32768.0D * velocity);
     }
     sample[(sample.length - 1)] = sample[2];
     sample[(sample.length - 2)] = sample[1];
     sample[0] = sample[(sample.length - 3)];
     return sample;
   }
 
   public static byte[] load(String name)
   {
     try
     {
       //InputStream in = loader.getClass().getResourceAsStream(name);
//    	 FileHandle file1 = Gdx.files.internal("data/"+name);
    	 File file1=new File("data/"+name);
       InputStream in = null;
       try {
           in= new FileInputStream(file1);
         }
    	 catch (FileNotFoundException e){
           ClassLoader cl = Resources.class.getClassLoader();
           in = cl.getResourceAsStream(name);
         }

       ByteArrayOutputStream bytes = new ByteArrayOutputStream();
       int array_size = 1024;
       byte[] array = new byte[array_size];
       int rb;
       while ((rb = in.read(array, 0, array_size)) > -1) {
         bytes.write(array, 0, rb);
       }
 
       bytes.close();
 
       array = bytes.toByteArray();
 
       in.close();
 
       return array;
     } catch (IOException e) {
       e.printStackTrace();
     }return null;
   }
 
   static
   {
     loader = new Resources();
   }
 }

