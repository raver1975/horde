package com.kg.wub.system;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.resample.RateTransposer;
import com.echonest.api.v4.Segment;
import com.echonest.api.v4.TimedEvent;
import com.echonest.api.v4.TrackAnalysis;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static Song timeStretch(Song song, double newBpm) {
        double bpmFactor = newBpm / song.analysis.getTempo();
        System.out.println("BpmFactor=" + bpmFactor + "\tnewpm=" + newBpm + "\told=" + song.analysis.getTempo());
//        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        AudioInterval ad = new AudioInterval(song.data);
        AudioInterval[] ai = ad.getMono();
        timeStretch1(ai[0], bpmFactor);
        timeStretch1(ai[1], bpmFactor);

        ad.makeStereo(ai);
        System.out.println("-------------------------------");
        System.out.println("old song data length:" + song.data.length);
        System.out.println("new song data length:" + ad.data.length);
        System.out.println("bpmFactor:" + bpmFactor);
        System.out.println("-------------------------------");
//        bpmFactor =   song.analysis.getTempo()/newBpm;

        song.data = ad.data;
//            System.exit(0);
//        double time = 0;
//        song.analysis.getSegments();
//        song.analysis.getBars();
//        song.analysis.getBeats();
//        song.analysis.getTatums();
//        song.analysis.getSections();
//        double maxTime=0;
//        song.analysis.getMap().computeIfAbsent("track", k -> new HashMap<String, Object>());
//        ((Map) song.analysis.getMap().get("track")).put("tempo", newBpm);
//        ((Map) song.analysis.getMap().get("track")).put("duration", song.analysis.getDuration1()/bpmFactor);
//        song.analysis.getMap().put("track.tempo", newBpm);
//        song.analysis.getMap().put("track.bpmFactor", bpmFactor);
//        song.analysis.setDuration(song.analysis.getDuration()*bpmFactor);
//        song.analysis.getMap().put("track.duration", song.analysis.getDuration() * bpmFactor);
//        System.out.println(song.analysis.getMap());
        song.analysis = new TrackAnalysis(song.analysis, bpmFactor, newBpm, song.analysis.getDuration());
//        bpmFactor = 1.0d / bpmFactor;
//        for (Segment seg : song.analysis.getSegments()) {
//            seg.duration *= bpmFactor;
//            seg.start *= bpmFactor;
//        }

//        for (TimedEvent seg : song.analysis.getSections()) {
//            seg.duration *= bpmFactor;
//            seg.start *= bpmFactor;
//        }
//        for (TimedEvent seg : song.analysis.getBeats()) {
//            seg.duration *= bpmFactor;
//            seg.start *= bpmFactor;
//        }
//        for (TimedEvent seg : song.analysis.getBars()) {
//            seg.duration *= bpmFactor;
//            seg.start *= bpmFactor;
//        }
//        for (TimedEvent seg : song.analysis.getTatums()) {
//            seg.duration *= bpmFactor;
//            seg.start *= bpmFactor;
//        }

//        return new Song(song.data,song.analysis);
        return song;
    }

    public static AudioInterval timeStretch(AudioInterval ad, double stretch) {
        AudioInterval[] ai = ad.getMono();
        timeStretch1(ai[0], stretch);
        timeStretch1(ai[1], stretch);
        ad.makeStereo(ai);
        return ad;
    }

    public static void timeStretch1(AudioInterval ad, double stretch) {
        AudioDispatcher adp = null;
        WaveformSimilarityBasedOverlapAdd wsola = new WaveformSimilarityBasedOverlapAdd(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(stretch, Audio.audioFormatMono.getSampleRate()));
        try {
            adp = AudioDispatcherFactory.fromByteArray(ad.data, Audio.audioFormatMono, wsola.getInputBufferSize(), wsola.getOverlap());
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
        CountDownLatch cdl = new CountDownLatch(1);
        ByteProcessor bp = new ByteProcessor(new AudioUtils(ad, cdl), format);

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

    public static void pitchShift(AudioInterval ad, double shift) {
        AudioInterval[] ai = ad.getMono();
        pitchShift1(ai[0], shift);
        pitchShift1(ai[1], shift);
        ad.makeStereo(ai);
    }

    private static void pitchShift1(AudioInterval ad, double shift) {
        shift = 1d / shift;
        AudioDispatcher adp = null;
        RateTransposer rateTransposer = new RateTransposer(shift);
        WaveformSimilarityBasedOverlapAdd wsola = new WaveformSimilarityBasedOverlapAdd(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(shift, Audio.audioFormatMono.getSampleRate()));
        try {
            adp = AudioDispatcherFactory.fromByteArray(ad.data, Audio.audioFormatMono, wsola.getInputBufferSize(), wsola.getOverlap());
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
        CountDownLatch cdl = new CountDownLatch(1);
        ByteProcessor bp = new ByteProcessor(new AudioUtils(ad, cdl), format);

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
//        for (int i = 495000; i < 500000; i++) System.out.print(ad.data[i]);
//        System.out.println();
        ad.data = data;
//        for (int i = 495000; i < 500000; i++) System.out.print(data[i]);
//        System.out.println();

        cdl.countDown();
    }
}
