package com.myronmarston.synth;

import com.myronmarston.music.AudioFileCreator;
import com.myronmarston.util.MixingAudioInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.BufferedOutputStream;
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

    public static double volume = 1D;
    public static double SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 16384;
    private byte[] buffer = new byte[BUFFER_SIZE];
    private byte[] buffer2 = new byte[BUFFER_SIZE];
    private static boolean running = false;
    private static Reverb reverb;
    private static Delay delay;
    private static boolean paused = false;
    private SourceDataLine sourceLine = null;
    private OutputStream audioWriter = null;

    private double left = 0.0D;
    private double right = 0.0D;

    public static double getVolume() {
        return volume;
    }

    public static void setVolume(double value) {
        volume = value;
    }

    public static Delay getDelay() {
        return delay;
    }

    public static Reverb getReverb() {
        return reverb;
    }

    public Output() {
        soundSystem();
        sourceLine = AudioFileCreator.getSourceDataLine();
        try {
            audioWriter = new BufferedOutputStream(new FileOutputStream("test.wav"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        tracks = new Synthesizer[Statics.drumsOn ? 2 : 1];
        delay = new Delay();
        reverb = new Reverb();
        ArrayList<AudioInputStream> streams = new ArrayList<AudioInputStream>();
        this.sequencer = new Sequencer[3];
        for (int it = 0; it < this.sequencer.length - 1; it++) {
            InstrumentSequencer its = new InstrumentSequencer(it);
            this.sequencer[it] = its;
            streams.add(its.ais1);
        }
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
        mixingAudioInputStream = new MixingAudioInputStream(audioFormat, streams);
        BasslineSynthesizer tb1 = new BasslineSynthesizer();
        tracks[0] = tb1;
        RhythmSynthesizer tr = new RhythmSynthesizer();
        if (Statics.drumsOn)
            tracks[1] = tr;
        this.sequencer[this.sequencer.length - 1] = new AcidSequencer(tb1, tr);
        tb1.controlChange(39, 20);
        tr.controlChange(39, 127);
        thread = new Thread(this);
        thread.setPriority(10);
    }

    private void soundSystem() {
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
    }

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
        while (running) {
            if (paused) {
                try {
                    Thread.sleep(25L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            for (int i = 0; i < buffer.length; i += 4) {

                for (Sequencer sequencer : sequencer) {
                    sequencer.tick();
                }

                if (Statics.drumsOn) {
                    double[] tmp = null;
                    tmp = tracks[1].stereoOutput();
                    delay.input(tmp[2]);
                    reverb.input(tmp[3]);
                    left = (left + tmp[0]) / 2;
                    right = (right + tmp[1]) / 2;
                }
                if (Statics.synthOn) {
                    double[] tmp = null;
                    tmp = tracks[0].stereoOutput();
                    delay.input(tmp[2]);
                    reverb.input(tmp[3]);
                    left = (left + tmp[0]) / 2;
                    right = (right + tmp[1]) / 2;
//                    tmp = tracks[1].stereoOutput();
//                    delay.input(tmp[2]);
//                    reverb.input(tmp[3]);
//                    left += tmp[0];
//                    right += tmp[1];
                }

                double[] del = delay.output();
                left = (left+del[0])/2;
                right = (right+del[1])/2;

                double[] rev = reverb.process();
                left += (left+rev[0])/2;
                right += (right+rev[1])/2;


                if (left > 1.0D) {
                    left = 1.0D;
                } else if (left < -1.0D)
                    left = -1.0D;
                if (right > 1.0D) {
                    right = 1.0D;
                } else if (right < -1.0D) {
                    right = -1.0D;
                }
                int sample_left_int = (int) (left * 32767.0D * volume);
                int sample_right_int = (int) (right * 32767.0D * volume);


                buffer[i] = ((byte) (sample_left_int & 0xFF));
                buffer[(i + 1)] = ((byte) (sample_left_int >> 8 & 0xFF));

                buffer[(i + 2)] = ((byte) (sample_right_int & 0xFF));
                buffer[(i + 3)] = ((byte) (sample_right_int >> 8 & 0xFF));
            }
            mixingAudioInputStream.read(buffer2, buffer);

            sourceLine.write(buffer, 0, BUFFER_SIZE);
            try {
                audioWriter.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        dispose();

    }

    public void dispose() {
        running = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    audioWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        try {
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
