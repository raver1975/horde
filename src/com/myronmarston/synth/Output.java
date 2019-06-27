package com.myronmarston.synth;

import com.myronmarston.music.AudioFileCreator;
import com.myronmarston.music.Instrument;

import javax.sound.midi.Sequence;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Output implements Runnable {
    private static Thread thread = null;
    private static Synthesizer[] tracks;
    private Sequencer sequencer[];

    public static double volume = 1D;
    public static double SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 16384;
    private byte[] buffer = new byte[BUFFER_SIZE];
    public static boolean running = false;
    private static Reverb reverb;
    private static Delay delay;
    private static boolean paused = false;
    private static SourceDataLine line = null;

    double left = 0.0D;
    double right = 0.0D;

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
//        line = AudioFileCreator.getDataLine();
        createOutput();
        line.start();
        tracks = new Synthesizer[Statics.drumsOn ? 2 : 1];
        BasslineSynthesizer tb1 = new BasslineSynthesizer();
        tb1.controlChange(39,64);

        tracks[0] = tb1;
        RhythmSynthesizer tr = new RhythmSynthesizer();
        tr.controlChange(36,64);
        if (Statics.drumsOn)
            tracks[1] = tr;

        delay = new Delay();
        reverb = new Reverb();
        this.sequencer = new Sequencer[InstrumentSequencer.maxChannels];
//        this.sequencer[0] = new AcidSequencer(tb1,  tr);
        for (String inst: Instrument.AVAILABLE_INSTRUMENTS){
            System.out.println(inst);
        }
        for (int it=0;it<this.sequencer.length-1;it++) {
            this.sequencer[it] = new InstrumentSequencer(AudioFileCreator.getDataLine(),it);
        }
        this.sequencer[this.sequencer.length-1] = new InstrumentSequencer(AudioFileCreator.getDataLine(),"Cello");
//        this.sequencer[2] = new InstrumentSequencer(AudioFileCreator.getDataLine(), 1);
//        this.sequencer[2] = new InstrumentSequencer(AudioFileCreator.getDataLine(), 2);
//        this.sequencer[3] = new InstrumentSequencer(AudioFileCreator.getDataLine(), 3);
//        this.sequencer[4] = new InstrumentSequencer(AudioFileCreator.getDataLine(), 4);

        thread = new Thread(this);
        thread.setPriority(10);
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
                    left += tmp[0];
                    right += tmp[1];
                }
                if (Statics.synthOn) {
                    double[] tmp = null;
                    tmp = tracks[0].stereoOutput();
                    delay.input(tmp[2]);
                    reverb.input(tmp[3]);
                    left += tmp[0];
                    right += tmp[1];
//                    tmp = tracks[1].stereoOutput();
//                    delay.input(tmp[2]);
//                    reverb.input(tmp[3]);
//                    left += tmp[0];
//                    right += tmp[1];
                }

                double[] del = delay.output();
                left += del[0];
                right += del[1];

                double[] rev = reverb.process();
                left += rev[0];
                right += rev[1];


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

//            if (Statics.export) {
//                if (Statics.exportFile!=null)Statics.exportFile.writeBytes(FloatArray2ByteArray(buffer), true);
//            } else {
            if (line == null) {
                line = AudioFileCreator.getDataLine();
            }
//            byte[] b=FloatArray2ByteArray(buffer);
//            line.write(b, 0, b.length);
            line.write(buffer, 0, BUFFER_SIZE);
            //ad.writeSamples(buffer, 0, BUFFER_SIZE);
//            }
        }
        dispose();
    }

    private void createOutput() {
        line = AudioFileCreator.getDataLine();
    }

    public void dispose() {
        running = false;
        line.drain();
        line.stop();
        line.close();
    }

    public Sequencer[] getSequencer() {
        return this.sequencer;
    }

}
