package com.myronmarston.synth;

import com.myronmarston.music.AudioFileCreator;
import com.myronmarston.util.MixingAudioInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class Output implements Runnable {
    private static Thread thread = null;
    private static Synthesizer[] tracks;
    private MixingAudioInputStream mixingAudioInputStream;
    private Sequencer[] sequencer;

    public static double SAMPLE_RATE = 44100;
    public static final int BUFFER_SIZE = 16384;
    private byte[] buffer1 = new byte[BUFFER_SIZE];
    private byte[] buffer2 = new byte[BUFFER_SIZE];
    private byte[] buffer3 = new byte[BUFFER_SIZE];
    private byte[] buffer4 = new byte[BUFFER_SIZE];
    private InputStream pin1 = new ByteArrayInputStream(buffer1);
    private InputStream pin2 = new ByteArrayInputStream(buffer2);
    private InputStream pin3 = new ByteArrayInputStream(buffer3);
    private InputStream pin4 = new ByteArrayInputStream(buffer4);
    private static boolean running = false;
    private static Reverb reverb;
    private static Delay delay;
    private static boolean paused = false;
    private SourceDataLine sourceLine = null;
    private OutputStream audioWriter = null;
    public static Delay getDelay() {
        return delay;
    }

    public static Reverb getReverb() {
        return reverb;
    }

    public Output() {
//        soundSystem();
        sourceLine = AudioFileCreator.getSourceDataLine();
        try {
            audioWriter = new BufferedOutputStream(new FileOutputStream("test.wav"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        tracks = new Synthesizer[4];
        delay = new Delay();
        reverb = new Reverb();
        ArrayList<InputStream> streams = new ArrayList<InputStream>();
        this.sequencer = new Sequencer[16];
        for (int it = 0; it < this.sequencer.length - 4; it++) {
            InstrumentSequencer its = new InstrumentSequencer(it);
            this.sequencer[it] = its;
            streams.add(its.ais1);
        }
        streams.add(pin1);
        streams.add(pin2);
        streams.add(pin3);
        streams.add(pin4);
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
        mixingAudioInputStream = new MixingAudioInputStream(audioFormat, streams);
        BasslineSynthesizer tb1 = new BasslineSynthesizer();
        BasslineSynthesizer tb2 = new BasslineSynthesizer();
        RhythmSynthesizer tr1 = new RhythmSynthesizer();
        RhythmSynthesizer tr2 = new RhythmSynthesizer();
        tracks[0] = tr2;
        tracks[1] = tr1;
        tracks[2] = tb2;
        tracks[3] = tb1;
//        tracks[1] = tr;
        this.sequencer[this.sequencer.length - 4] = new RhythmSequencer(tb1);
        this.sequencer[this.sequencer.length - 3] = new RhythmSequencer( tb2);
        this.sequencer[this.sequencer.length - 2] = new DrumSequencer(tr1);
        this.sequencer[this.sequencer.length - 1] = new DrumSequencer(tr2);
//        this.sequencer[this.sequencer.length - 1].setVolume(1f);
        tb1.controlChange(39, 127);
        tb2.controlChange(39, 127);
        tr1.controlChange(39, 127);
        tr2.controlChange(39, 127);
        thread = new Thread(this);
        thread.setPriority(10);
    }

    /*private void soundSystem() {
        Mixer mixer = AudioSystem.getMixer(null); // default mixer
        try {
            mixer.open();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        System.out.printf("Supported SourceDataLines of default mixer (%s):\n\n", mixer.getMixerInfo().getName());
        for (Line.Info info : mixer.getSourceLineInfo()) {
            if (SourceDataLine.class.isAssignableFrom(info.getLineClass())) {
                SourceDataLine.Info info2 = (SourceDataLine.Info) info;
                System.out.println(info2);
                System.out.printf("  max buffer size: \t%d\n", info2.getMaxBufferSize());
                System.out.printf("  min buffer size: \t%d\n", info2.getMinBufferSize());
                AudioFormat[] formats = info2.getFormats();
                System.out.println("  Supported Audio formats: ");
                for (AudioFormat format : formats) {
                    System.out.println("    " + format);
                }
                System.out.println();
            } else {
                System.out.println(info.toString());
            }
            System.out.println();
        }

        System.out.printf("Supported TargetDataLines of default mixer (%s):\n\n", mixer.getMixerInfo().getName());
        for (Line.Info info : mixer.getTargetLineInfo()) {
            if (TargetDataLine.class.isAssignableFrom(info.getLineClass())) {
                TargetDataLine.Info info2 = (TargetDataLine.Info) info;
                System.out.println(info2);
                System.out.printf("  max buffer size: \t%d\n", info2.getMaxBufferSize());
                System.out.printf("  min buffer size: \t%d\n", info2.getMinBufferSize());
                AudioFormat[] formats = info2.getFormats();
                System.out.println("  Supported Audio formats: ");
                for (AudioFormat format : formats) {
                    System.out.println("    " + format);
                }
                System.out.println();
            } else {
                System.out.println(info.toString());
            }
            System.out.println();
        }
        mixer.close();
        int resolution = 16;
        int channels = 2;
        int frameSize = channels * resolution / 8;
        int sampleRate = 44100;
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, resolution, channels, frameSize, sampleRate, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        Line.Info[] targetInfo = AudioSystem.getTargetLineInfo(info);
        for (Line.Info l : targetInfo) {
            System.out.println("target:\t" + l.toString());
        }
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info m : mixers) {
            System.out.println("----------------------------\t" + m.toString());
            mixer = AudioSystem.getMixer(m);
            Line.Info[] sourceInfo = mixer.getSourceLineInfo();
            targetInfo = mixer.getTargetLineInfo();
            for (Line.Info l : sourceInfo) {
                System.out.println("\tsource:\t" + l.toString());
            }
            for (Line.Info l : targetInfo) {
                System.out.println("\ttarget:\t" + l.toString());
            }
        }
    }*/

    public void start() {
        running = true;
        thread.start();
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }


    public static boolean isPaused() {
        return paused;
    }

    public static void pause() {
        paused = true;
    }

    public static void resume() {
        paused = false;
    }

    public void run() {
        double[] tmp = null;
        double[] del = null;
        double[] rev = null;
        int sample_left_int1 = 0;
        int sample_right_int1 = 0;
        int sample_left_int2 = 0;
        int sample_right_int2 = 0;
        int sample_left_int3 = 0;
        int sample_right_int3 = 0;
        int sample_left_int4 = 0;
        int sample_right_int4 = 0;

        byte[] buffer5 = new byte[BUFFER_SIZE];
        while (running) {
            if (paused) {
                try {
                    Thread.sleep(25L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            for (int i = 0; i < buffer1.length; i += 4) {
                for (Sequencer sequencer : sequencer) {
                    sequencer.tick();
                }
                double right1 = 0.0D;
                double left1 = right1 = 0;
                double right2 = 0.0D;
                double left2 = right2 = 0;
                double right3 = 0.0D;
                double left3 = right3 = 0;
                double right4 = 0.0D;
                double left4 = right4 = 0;

                tmp = tracks[0].stereoOutput();
                left1 += tmp[0];
                right1 += tmp[1];

                tmp = tracks[1].stereoOutput();
//                delay.input(tmp[2]);
//                reverb.input(tmp[3]);
                left2 += tmp[0];
                right2 += tmp[1];

                tmp = tracks[2].stereoOutput();
//                delay.input(tmp[2]);
//                reverb.input(tmp[3]);
                left3 += tmp[0];
                right3 += tmp[1];

                tmp = tracks[3].stereoOutput();
//                delay.input(tmp[2]);
//                reverb.input(tmp[3]);
                left4 += tmp[0];
                right4 += tmp[1];


//                del = delay.output();
//                left2 += del[0];
//                right2 += del[1];
//
//                rev = reverb.process();
//                left2 += rev[0];
//                right2 += rev[1];

                sample_left_int1 = (int) (left1 * 32767.0D * sequencer[sequencer.length - 1].getVolume());
                sample_right_int1 = (int) (right1 * 32767.0D * sequencer[sequencer.length - 1].getVolume());
                sample_left_int2 = (int) (left2 * 32767.0D * sequencer[sequencer.length - 2].getVolume());
                sample_right_int2 = (int) (right2 * 32767.0D * sequencer[sequencer.length - 2].getVolume());
                sample_left_int3 = (int) (left3 * 32767.0D * sequencer[sequencer.length - 3].getVolume());
                sample_right_int3 = (int) (right3 * 32767.0D * sequencer[sequencer.length - 3].getVolume());
                sample_left_int4 = (int) (left4 * 32767.0D * sequencer[sequencer.length - 4].getVolume());
                sample_right_int4 = (int) (right4 * 32767.0D * sequencer[sequencer.length - 4].getVolume());

                buffer1[i] = ((byte) (sample_left_int1 & 0xFF));
                buffer1[(i + 1)] = ((byte) (sample_left_int1 >> 8 & 0xFF));
                buffer1[(i + 2)] = ((byte) (sample_right_int1 & 0xFF));
                buffer1[(i + 3)] = ((byte) (sample_right_int1 >> 8 & 0xFF));

                buffer2[i] = ((byte) (sample_left_int2 & 0xFF));
                buffer2[(i + 1)] = ((byte) (sample_left_int2 >> 8 & 0xFF));
                buffer2[(i + 2)] = ((byte) (sample_right_int2 & 0xFF));
                buffer2[(i + 3)] = ((byte) (sample_right_int2 >> 8 & 0xFF));

                buffer3[i] = ((byte) (sample_left_int3 & 0xFF));
                buffer3[(i + 1)] = ((byte) (sample_left_int3 >> 8 & 0xFF));
                buffer3[(i + 2)] = ((byte) (sample_right_int3 & 0xFF));
                buffer3[(i + 3)] = ((byte) (sample_right_int3 >> 8 & 0xFF));

                buffer4[i] = ((byte) (sample_left_int4 & 0xFF));
                buffer4[(i + 1)] = ((byte) (sample_left_int4 >> 8 & 0xFF));
                buffer4[(i + 2)] = ((byte) (sample_right_int4 & 0xFF));
                buffer4[(i + 3)] = ((byte) (sample_right_int4 >> 8 & 0xFF));
            }
            try {
                pin1.reset();
                pin2.reset();
                pin3.reset();
                pin4.reset();
                mixingAudioInputStream.read(buffer5);
                audioWriter.write(buffer5);
                sourceLine.write(buffer5, 0, BUFFER_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dispose();
    }

    public void dispose() {
        running = false;
        try {
            audioWriter.close();
            rawToWave(new File("test.wav"), new File("testconv.wav"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Sequencer[] getSequencer() {
        return this.sequencer;
    }


    private void rawToWave(final File rawFile, final File waveFile) throws IOException {

        byte[] rawData = new byte[(int) rawFile.length()];
        InputStream input = null;
        try {
            input = new FileInputStream(rawFile);
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }

        FileOutputStream output = null;
        try {
            output = new FileOutputStream((waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            int samplerate = 44100;
            int channels = 2;
            int bitspersample = 16;
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) channels); // number of channels
            writeInt(output, samplerate); // sample rate
            writeInt(output, samplerate * channels * bitspersample / 8); // byte rate   == SampleRate * NumChannels * BitsPerSample/8
            writeShort(output, (short) (channels * bitspersample / 8)); // block align == NumChannels * BitsPerSample/8
            writeShort(output, (short) bitspersample); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            output.write(rawData);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    private void writeInt(final OutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final OutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final OutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }
}
