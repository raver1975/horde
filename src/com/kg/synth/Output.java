package com.kg.synth;

import com.kg.TheHorde;
import com.kg.wub.AudioObject;
import com.kg.wub.system.CentralCommand;
import com.kg.wub.system.Tickable;
import com.myronmarston.music.AudioFileCreator;
import com.myronmarston.util.MixingAudioInputStream;
import edu.princeton.cs.algs4.In;
import org.encog.bot.browse.range.Input;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Output implements Runnable {
    public static final int PARTS = 16;
    public static Output instance;
    private static Thread thread = null;
    public static double SAMPLE_RATE = 44100;
    public static final int BUFFER_SIZE = 16384/2;
    private final TheHorde horde;

    public Synthesizer[] synthesizers;
    public MixingAudioInputStream mixingAudioInputStream;
    private Sequencer[] sequencer;
    public Reverb[] reverb;
    public Delay[] delay;
    public float[] pan;

    private final static int audioLines = 30;
    private byte[][] buffers = new byte[audioLines][BUFFER_SIZE];
    private InputStream[] pins = new InputStream[buffers.length];
    private boolean running = false;

    private boolean paused = false;
    private SourceDataLine sourceLine = null;
    private OutputStream audioWriter = null;

    double right1 = 0.0D;
    double left1 = 0.0D;
    double right2 = 0.0D;
    double left2 = 0.0D;
    double right3 = 0.0D;
    double left3 = 0.0D;
    double right4 = 0.0D;
    double left4 = 0.0D;
    double panl = 0;
    double panr = 0;
    private int lastStep = -1;
    private final List<Tickable> lines = new ArrayList<>();
    private final byte[] bufferOut = new byte[BUFFER_SIZE];

    public Output(TheHorde horde) {
        instance = this;
        this.horde = horde;
//        soundSystem();
        sourceLine = AudioFileCreator.getSourceDataLine();
        try {
            audioWriter = new BufferedOutputStream(new FileOutputStream("test.wav"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        synthesizers = new Synthesizer[4];
        delay = new Delay[PARTS];
        reverb = new Reverb[PARTS];
        pan = new float[PARTS];
        for (int i = 0; i < PARTS; i++) {
            delay[i] = new Delay();
            reverb[i] = new Reverb();
        }
        ArrayList<InputStream> streams = new ArrayList<InputStream>();
        this.sequencer = new Sequencer[PARTS];
        for (int it = 0; it < this.sequencer.length - 4; it++) {
            Sequencer its = null;
            if (it < 8) {
                its = new MidiSequencer(it, false);
            } else {
                its = new InstrumentSequencer(it, it == 9);
            }
            if (its instanceof MidiSequencer) {
                if (((MidiSequencer) its).midiDeviceReceiver == null) {
                    its = new InstrumentSequencer(it, it == 9);
                }
            }
//            its= (it<8)?new MidiSequencer(it, it == 9):new InstrumentSequencer(it, it == 9);
            this.sequencer[it] = its;
            if (its instanceof InstrumentSequencer) {
                streams.add(((InstrumentSequencer) its).audioInputStream);
            }
        }
        int cc = 0;
        for (byte[] buffer : buffers) {
            buffers[cc] = new byte[BUFFER_SIZE];
            InputStream pin = new ByteArrayInputStream(buffers[cc]);
            pins[cc++] = pin;
            streams.add(pin);
        }
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
        mixingAudioInputStream = new MixingAudioInputStream(audioFormat, streams);
        BasslineSynthesizer tb1 = new BasslineSynthesizer();
        BasslineSynthesizer tb2 = new BasslineSynthesizer();
        RhythmSynthesizer tr1 = new RhythmSynthesizer();
        RhythmSynthesizer tr2 = new RhythmSynthesizer();
        synthesizers[0] = tr2;
        synthesizers[1] = tr1;
        synthesizers[2] = tb2;
        synthesizers[3] = tb1;
//        synthesizers[1] = tr;
        this.sequencer[this.sequencer.length - 4] = new BasslineSequencer(tb1);
        this.sequencer[this.sequencer.length - 3] = new BasslineSequencer(tb2);
        this.sequencer[this.sequencer.length - 2] = new RhythmSequencer(tr1);
        this.sequencer[this.sequencer.length - 1] = new RhythmSequencer(tr2);
//        this.sequencer[this.sequencer.length - 1].setVolume(1f);
        tb1.controlChange(39, 127);
        tb2.controlChange(39, 127);
        tr1.controlChange(39, 127);
        tr2.controlChange(39, 127);
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


    public boolean isPaused() {
        return paused;
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
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


        while (running) {
            if (paused) {
                try {
                    Thread.sleep(25L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (sequencer[0].step != lastStep) {
                horde.drawSequencer();
            }
            lastStep = sequencer[0].step;
//            horde.drawVisualizer(buffer1);
            for (int i = 0; i < Output.BUFFER_SIZE; i += 4) {
                for (Sequencer sequencer : sequencer) {
                    sequencer.tick();
                }
                left1 = right1 = 0;
                left2 = right2 = 0;
                left3 = right3 = 0;
                left4 = right4 = 0;

                tmp = synthesizers[0].stereoOutput();
                int col = PARTS-1;
                delay[col].input(tmp[2]);
                reverb[col].input(tmp[3]);
                left1 += tmp[0];
                right1 += tmp[1];
                del = delay[col].output();
                left1 += del[0];
                right1 += del[1];
                rev = reverb[col].process();
                left1 += rev[0];
                right1 += rev[1];
                panl = 2f * Math.min(.5f, (127f - (pan[col] + 63.5f)) / 127f);
                panr = 2f * Math.min(.5f, (pan[col] + 63.5f) / 127f);
                left1 *= panl;
                right1 *= panr;
                sample_left_int1 = (int) (left1 * 32767.0D * sequencer[sequencer.length - 1].getVolume());
                sample_right_int1 = (int) (right1 * 32767.0D * sequencer[sequencer.length - 1].getVolume());
                buffers[0][i] = ((byte) (sample_left_int1 & 0xFF));
                buffers[0][(i + 1)] = ((byte) (sample_left_int1 >> 8 & 0xFF));
                buffers[0][(i + 2)] = ((byte) (sample_right_int1 & 0xFF));
                buffers[0][(i + 3)] = ((byte) (sample_right_int1 >> 8 & 0xFF));

                tmp = synthesizers[1].stereoOutput();
                col = PARTS-2;
                delay[col].input(tmp[2]);
                reverb[col].input(tmp[3]);
                left2 += tmp[0];
                right2 += tmp[1];
                del = delay[col].output();
                left2 += del[0];
                right2 += del[1];
                rev = reverb[col].process();
                left2 += rev[0];
                right2 += rev[1];
                panl = 2f * Math.min(.5f, (127f - (pan[col] + 63.5f)) / 127f);
                panr = 2f * Math.min(.5f, (pan[col] + 63.5f) / 127f);
                left2 *= panl;
                right2 *= panr;
                sample_left_int2 = (int) (left2 * 32767.0D * sequencer[sequencer.length - 2].getVolume());
                sample_right_int2 = (int) (right2 * 32767.0D * sequencer[sequencer.length - 2].getVolume());
                buffers[1][i] = ((byte) (sample_left_int2 & 0xFF));
                buffers[1][(i + 1)] = ((byte) (sample_left_int2 >> 8 & 0xFF));
                buffers[1][(i + 2)] = ((byte) (sample_right_int2 & 0xFF));
                buffers[1][(i + 3)] = ((byte) (sample_right_int2 >> 8 & 0xFF));


                tmp = synthesizers[2].stereoOutput();
                col = PARTS-3;
                delay[col].input(tmp[2]);
                reverb[col].input(tmp[3]);
                left3 += tmp[0];
                right3 += tmp[1];
                del = delay[col].output();
                left3 += del[0];
                right3 += del[1];
                rev = reverb[col].process();
                left3 += rev[0];
                right3 += rev[1];
                panl = 2f * Math.min(.5f, (127f - (pan[col] + 63.5f)) / 127f);
                panr = 2f * Math.min(.5f, (pan[col] + 63.5f) / 127f);
                left3 *= panl;
                right3 *= panr;
                sample_left_int3 = (int) (left3 * 32767.0D * sequencer[sequencer.length - 3].getVolume());
                sample_right_int3 = (int) (right3 * 32767.0D * sequencer[sequencer.length - 3].getVolume());
                buffers[2][i] = ((byte) (sample_left_int3 & 0xFF));
                buffers[2][(i + 1)] = ((byte) (sample_left_int3 >> 8 & 0xFF));
                buffers[2][(i + 2)] = ((byte) (sample_right_int3 & 0xFF));
                buffers[2][(i + 3)] = ((byte) (sample_right_int3 >> 8 & 0xFF));


                tmp = synthesizers[3].stereoOutput();
                col = PARTS-4;
                delay[col].input(tmp[2]);
                reverb[col].input(tmp[3]);
                left4 += tmp[0];
                right4 += tmp[1];
                del = delay[col].output();
                left4 += del[0];
                right4 += del[1];
                rev = reverb[col].process();
                left4 += rev[0];
                right4 += rev[1];
                panl = 2f * Math.min(.5f, (127f - (pan[col] + 63.5f)) / 127f);
                panr = 2f * Math.min(.5f, (pan[col] + 63.5f) / 127f);
                left4 *= panl;
                right4 *= panr;
                sample_left_int4 = (int) (left4 * 32767.0D * sequencer[sequencer.length - 4].getVolume());
                sample_right_int4 = (int) (right4 * 32767.0D * sequencer[sequencer.length - 4].getVolume());
                buffers[3][i] = ((byte) (sample_left_int4 & 0xFF));
                buffers[3][(i + 1)] = ((byte) (sample_left_int4 >> 8 & 0xFF));
                buffers[3][(i + 2)] = ((byte) (sample_right_int4 & 0xFF));
                buffers[3][(i + 3)] = ((byte) (sample_right_int4 >> 8 & 0xFF));
            }
            int bb = 0;
            Iterator<Tickable> it = lines.iterator();
            while (it.hasNext()) {
                Tickable au = it.next();
                if (bb < buffers.length - 4) {
                    if (au.tick(buffers[bb + 4])) {
                        bb++;
                    }
                }
            }


            try {
                for (InputStream pin : pins) {
                    pin.reset();
                }
                mixingAudioInputStream.activeInputStreams = bb + 15;
                mixingAudioInputStream.read(bufferOut);
                audioWriter.write(bufferOut);
                horde.drawVisualizer(bufferOut);
                sourceLine.write(bufferOut, 0, BUFFER_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dispose();
    }

    public void dispose() {
        running = false;
        try {
            rawToWave(new File("test.wav"), new File("testconv.wav"));
            audioWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Sequencer[] getSequencers() {
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
            writeShort(output, (short) 1); // audioInputStream format (1 = PCM)
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

    public void addLine(Tickable audioObject) {
        lines.add(audioObject);
    }

}
