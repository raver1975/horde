package com.klemstinegroup.wub.system;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.resample.RateTransposer;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Paul on 2/21/2016.
 */
public class AudioUtils implements Handler {

    private static float[] buffer;
    private static AudioDispatcher d;
    AudioInterval ad;
    CountDownLatch cdl;

    public AudioUtils(AudioInterval ad, CountDownLatch cdl) {
        this.ad = ad;
        this.cdl = cdl;
    }

    public static void timeStretch(AudioInterval ad,double stretch){
        AudioInterval[] ai=ad.getMono();
        timeStretch1(ai[0],stretch);
        timeStretch1(ai[1],stretch);
        ad.makeStereo(ai);
    }

    private static void timeStretch1(AudioInterval ad, double stretch){
        AudioDispatcher adp= null;
        WaveformSimilarityBasedOverlapAdd wsola = new WaveformSimilarityBasedOverlapAdd(WaveformSimilarityBasedOverlapAdd.Parameters.automaticDefaults(stretch, Audio.audioFormatMono.getSampleRate()));
        try {
            adp = AudioDispatcherFactory.fromByteArray(ad.data, Audio.audioFormatMono,wsola.getInputBufferSize(),wsola.getOverlap());
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        TarsosDSPAudioFormat format = adp.getFormat();
        //   System.out.println(format);
//        AudioPlayer audioPlayer = null;
//        try {
//            audioPlayer = new AudioPlayer(format);
//        } catch (LineUnavailableException e) {
//            e.printStackTrace();
//        }
        // GainProcessor gain = new GainProcessor(1.0);
        CountDownLatch cdl=new CountDownLatch(1);
        ByteProcessor bp = new ByteProcessor(new AudioUtils(ad,cdl),format);

        wsola.setDispatcher(adp);
        adp.addAudioProcessor(wsola);
        // adp.addAudioProcessor(gain);
        adp.addAudioProcessor(bp);

//        Thread t = new Thread(adp);
//        t.start();
        adp.run();
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public static void pitchShift(AudioInterval ad,double shift){
        AudioInterval[] ai=ad.getMono();
        pitchShift1(ai[0],shift);
        pitchShift1(ai[1],shift);
        ad.makeStereo(ai);
    }

    private static void pitchShift1(AudioInterval ad, double shift){
        shift=1d/shift;
        AudioDispatcher adp= null;
        RateTransposer rateTransposer = new RateTransposer(shift);
        WaveformSimilarityBasedOverlapAdd wsola = new WaveformSimilarityBasedOverlapAdd(WaveformSimilarityBasedOverlapAdd.Parameters.automaticDefaults(shift, Audio.audioFormatMono.getSampleRate()));
        try {
            adp = AudioDispatcherFactory.fromByteArray(ad.data, Audio.audioFormatMono,wsola.getInputBufferSize(),wsola.getOverlap());
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        TarsosDSPAudioFormat format = adp.getFormat();
        //   System.out.println(format);
//        AudioPlayer audioPlayer = null;
//        try {
//            audioPlayer = new AudioPlayer(format);
//        } catch (LineUnavailableException e) {
//            e.printStackTrace();
//        }
        // GainProcessor gain = new GainProcessor(1.0);
        CountDownLatch cdl=new CountDownLatch(1);
        ByteProcessor bp = new ByteProcessor(new AudioUtils(ad,cdl),format);

        wsola.setDispatcher(adp);
        adp.addAudioProcessor(wsola);
        adp.addAudioProcessor(rateTransposer);
        // adp.addAudioProcessor(gain);
        adp.addAudioProcessor(bp);

//        Thread t = new Thread(adp);
//        t.start();
        adp.run();
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


//    public static void pitchShift(AudioInterval ad, double shift) {
//        AudioDispatcher adp = null;
//        RateTransposer rateTransposer = new RateTransposer(shift);
//        //WaveformSimilarityBasedOverlapAdd wsola = new WaveformSimilarityBasedOverlapAdd(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(shift, Audio.audioFormat.getSampleRate()));
//        try {
//            adp = AudioDispatcherFactory.fromByteArray(ad.data, Audio.audioFormat, 2048, 1024);
//        } catch (UnsupportedAudioFileException e) {
//            e.printStackTrace();
//        }
//        TarsosDSPAudioFormat format = adp.getFormat();
//        System.out.println(format);
////        AudioPlayer audioPlayer = null;
////        try {
////            audioPlayer = new AudioPlayer(format);
////        } catch (LineUnavailableException e) {
////            e.printStackTrace();
////        }
//        // GainProcessor gain = new GainProcessor(1.0);
//        CountDownLatch cdl = new CountDownLatch(1);
//        ByteProcessor bp = new ByteProcessor(new AudioUtils(ad, cdl), format);
//
////        wsola.setDispatcher(adp);
//
////        adp.addAudioProcessor(wsola);
//        adp.addAudioProcessor(rateTransposer);
//        // adp.addAudioProcessor(gain);
//        adp.addAudioProcessor(bp);
//
////        Thread t = new Thread(adp);
////        t.start();
//        adp.run();
//        try {
//            cdl.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//
//    }


    @Override
    public void handle(byte[] data) {
        for (int i = 495000; i < 500000; i++) System.out.print(ad.data[i]);
        System.out.println();
        ad.data = data;
        for (int i = 495000; i < 500000; i++) System.out.print(data[i]);
        System.out.println();

        cdl.countDown();
    }
}
