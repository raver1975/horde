package com.myronmarston.synth;

import com.myronmarston.music.AudioFileCreator;
import com.myronmarston.music.Instrument;
import com.myronmarston.music.MidiSoundbank;
import com.sun.media.sound.AudioSynthesizer;
import org.tritonus.share.sampled.AudioUtils;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.ArrayList;
import java.util.HashMap;

public class InstrumentSequencer extends Sequencer {
    private final String inst;
    ArrayList<Integer> noteOn = new ArrayList<Integer>();
    private AudioSynthesizer audioSynthesizer = null;
    //    public BasslineSynthesizer bass1;
    public BasslinePattern bassline1;
//    public BasslineSynthesizer bass2;
//    public BasslinePattern bassline2;

    //    public RhythmSynthesizer drums;
    public int[][] rhythm;
    private boolean shuffle;
    private int samplesPerSequencerUpdate;
    public int tick = 0;
    public int step = 0;
    private boolean sixteenth_note = true;
    int channel = 0;

    static int maxChannels = 16;
    public static String channels[] = new String[16];

    private int patternLength = 16;

    public InstrumentSequencer(SourceDataLine sourceDataLine, int channel) {
        this(sourceDataLine, Instrument.AVAILABLE_INSTRUMENTS.get((int) (Instrument.AVAILABLE_INSTRUMENTS.size() * Math.random())));
    }

    public InstrumentSequencer(SourceDataLine sourceDataLine, String inst) {
        this.inst = inst;

        randomizeRhythm();
        randomizeSequence();


        System.out.println("picking instrument:");

        channel = (int) (Math.random() * 16);
        System.out.println("inst:" + inst);
        Instrument instrument = Instrument.getInstrument(inst);
        try {
            audioSynthesizer = AudioFileCreator.getAudioSynthesizer();
            audioSynthesizer.open(sourceDataLine, new HashMap<String, Object>());
//            audioSynthesizer.getReceiver().send(programChangeMidiEvent.getMessage(), -1);
//            synth.controlChange(39, cc)
//            setChannel();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
//        try {
//            sourceDataLine.open(format, 16384);
//        } catch (LineUnavailableException e) {
//            e.printStackTrace();
//        }


//        this.bass1 = bass1;
//        this.bass2 = bass2;
//        this.drums = drums;

    }

    public void randomizeRhythm() {
        this.rhythm = createRhythm(this.patternLength);
    }

    public void randomizeSequence() {
        double[] basicCoeffs = {0.5D, 0.5D, 0.5D, 0.5D};
        double[] bassCoeffs = new double[16];
        boolean preferBassDrum = Math.random() > 0.5D;
        boolean preferSnareDrum = Math.random() > 0.5D;
        if ((!preferBassDrum) && (!preferSnareDrum)) {
            preferBassDrum = preferSnareDrum = true;
        }
        for (int i = 0; i < this.rhythm[0].length; i++) {
            bassCoeffs[i] = basicCoeffs[(i % 4)];

            if (((this.rhythm[0][i] > 0) && (preferBassDrum))
                    || ((preferSnareDrum) && ((this.rhythm[1][i] > 0) || (this.rhythm[4][i] > 0))))
                bassCoeffs[i] *= 4.0D;
            if (this.rhythm[3][i] > 0)
                bassCoeffs[i] *= 2.0D;
            if (this.rhythm[4][i] > 0) {
                bassCoeffs[i] *= 2.0D;
            }
        }

        Markov markov = new Markov(null, 0.0D);
        markov.addKid(new Markov(Harmony.SCALE_MELODIC_MINOR, 2.0D));
        markov.addKid(new Markov(Harmony.SCALE_MAJOR, 1.0D));
        markov.addKid(new Markov(Harmony.SCALE_HUNGARIAN_MINOR, 0.5D));
        this.bassline1 = createBassline(this.patternLength, (int[]) (int[]) markov.getKid().getContent(), bassCoeffs);


//			this.bass.randomize();
//        } else {

//        }

//		double newBpm = 100.0D + Math.random() * 60.0D;
//		this.shuffle = ((newBpm < 120.0D) && (Math.random() > 0.33D));
//		setBpm(newBpm);

        Markov delayTimes = new Markov(null, 0.0D);
        delayTimes.addKid(new Markov(this.samplesPerSequencerUpdate * 2 * 2, 0.5D));
        delayTimes.addKid(new Markov(this.samplesPerSequencerUpdate * 2 * 3, 2.0D));
        delayTimes.addKid(new Markov(this.samplesPerSequencerUpdate * 2 * 4, 1.0D));
        delayTimes.addKid(new Markov(this.samplesPerSequencerUpdate * 2 * 8, 1.0D));
        Output.getDelay().setFeedback(Math.random() * 0.75D + 0.2D);
        Output.getDelay().setTime(
                (Integer) delayTimes.getKid().getContent());
    }


    public void tick() {
        if (this.tick == 0) {
            if (this.sixteenth_note) {
                if ((!this.bassline1.pause[this.step])
                        && (this.bassline1.note[this.step] != -1)) {
//                    this.bass1
//                            .noteOn(this.bassline1.note[this.step]
//                                            + 36
//                                            + (this.bassline1.isTransUp(this.step) ? 12
//                                            : 0)
//                                            - (this.bassline1.isTransDown(this.step) ? 12
//                                            : 0),
//                                    this.bassline1.accent[this.step] ? 127
//                                            : 80);
//                    System.out.println("playing note on:" + (this.bassline1.note[this.step] + 36 + (this.bassline1.isTransUp(this.step) ? 12 : 0) - (this.bassline1.isTransDown(this.step) ? 12 : 0)));
                    try {
                        int pitch = this.bassline1.note[this.step] + 36 + (this.bassline1.isTransUp(this.step) ? 12 : 0) - (this.bassline1.isTransDown(this.step) ? 12 : 0);
                        int vel = (int) ((this.bassline1.accent[this.step] ? 127 : 80) * Output.getVolume());
                        setChannel(channel);
                        noteOn.add(pitch);

                        audioSynthesizer.getReceiver().send(new ShortMessage(ShortMessage.NOTE_ON, channel, pitch, vel), -1);
                    } catch (MidiUnavailableException e) {
                        e.printStackTrace();
                    } catch (InvalidMidiDataException e) {
                        e.printStackTrace();
                    }

                }


//                for (int ch = 0; ch < this.rhythm.length; ch++) {
//                    if (this.rhythm[ch][this.step] != 0) {
//                        int vol = 255;
//                        if ((this.step > 1) && (this.step < 15)
//                                && (this.rhythm[ch][(this.step - 1)] != 0)) {
//                            vol = (int) (vol * 0.66D);
//                        }
//                        if (this.step % 4 != 0)
//                            vol = (int) (vol * 0.66D);
//                        if (this.step % 2 != 0) {
//                            vol = (int) (vol * 0.66D);
//                        }
////                        this.drums.noteOn(ch + 32, vol);
//                    }
//                }
//				if (this.step == 0) {
//					if (this.evenPattern)
//						this.drums.noteOn(0, 127);
//					this.evenPattern = (!this.evenPattern);
//				}
                if (this.shuffle)
                    setBpm(this.bpm);
            } else {
                if (!this.bassline1.slide[this.step]) {
//                    this.bass1.noteOff();

                    for (int n : noteOn) {
                        try {
                            setChannel(channel);
                            audioSynthesizer.getReceiver().send(new ShortMessage(ShortMessage.NOTE_OFF, channel, n, 0), -1);
                        } catch (MidiUnavailableException e) {
                            e.printStackTrace();
                        } catch (InvalidMidiDataException e) {
                            e.printStackTrace();
                        }
                    }
                    noteOn.clear();
                }

                this.step += 1;
            }
            this.sixteenth_note = (!this.sixteenth_note);
        }

        this.tick += 1;

        if (this.tick >= this.samplesPerSequencerUpdate) {
            this.tick = 0;
            if (this.step == 16)
                this.step = 0;
        }
    }

    private void setChannel(int channel) {
        if (!inst.equals(channels[channel])) {
            this.channel = channel;
            channels[channel] = inst;
            Instrument instrument = Instrument.getInstrument(inst);
            MidiEvent programChangeMidiEvent = instrument.getProgramChangeMidiEvent(channel);
            try {
                audioSynthesizer.getReceiver().send(programChangeMidiEvent.getMessage(), -1);
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    public void setBpm(double value) {
        this.bpm = value;
        this.samplesPerSequencerUpdate = (int) (Output.SAMPLE_RATE
                / (this.bpm / 60.0D) / 8.0D);

        if (this.shuffle)
            if (this.step % 2 == 0)
                this.samplesPerSequencerUpdate += (int) (this.samplesPerSequencerUpdate * 0.33D);
            else
                this.samplesPerSequencerUpdate -= (int) (this.samplesPerSequencerUpdate * 0.33D);
    }

    public BasslinePattern createBassline(int length, int[] scale,
                                          double[] weights) {
        BasslinePattern pattern = new BasslinePattern(length);
        pattern.clear();
        int prevNote = 0;
        int transpose = (int) (Math.random() * 12.0D - 12.0D);

        double sustainWeight = Math.random() * 3.0D + 0.5D;
        double noteProb = Math.random() * 3.0D + 0.5D;

        for (int i = 0; i < length; i++) {
            double probability = weights[(i % weights.length)];

            if (Math.random() * noteProb < probability) {
                Markov m = new Markov(null, 0.0D);

                for (int j = 0; j < scale.length; j++) {
                    int prob = 1;
                    if (scale[j] == 0)
                        prob *= 2;
                    if (scale[j] == prevNote)
                        prob *= 3;
                    if ((scale[j] == prevNote - 1)
                            || (scale[j] == prevNote + 2))
                        prob *= 2;
                    m.addKid(new Markov(scale[j], prob));
                }

                int note = (Integer) m.getKid().getContent();

                if ((Math.abs(note - prevNote) > 7) && (Math.random() > 0.5D)) {
                    if (prevNote > note)
                        note += 12;
                    if (prevNote < note) {
                        note -= 12;
                    }
                }
                prevNote = note;
                note += transpose;
                pattern.note[i] = (byte) note;

                pattern.pause[i] = false;

                if ((!pattern.pause[i])
                        && ((i == 0) || (!pattern.slide[(i - 1)]))) {
                    if (Math.random() * 6.0D < probability) {
                        pattern.accent[i] = true;
                    }

                }

                double noteTranspProb = 0.12D;

                if ((Math.random() < noteTranspProb) && (note + transpose < 12)) {
                    pattern.transUp[i] = true;
                } else if ((Math.random() < noteTranspProb)
                        && (note + transpose > -12)) {
                    pattern.transDown[i] = true;
                }

                while ((Math.random() * sustainWeight > weights[((i + 1) % weights.length)])
                        && (i < length)) {
                    pattern.slide[i] = true;

                    if ((i != 0) && (pattern.transUp[(i - 1)])) {
                        pattern.transUp[i] = true;
                    }
                    if ((i != 0) && (pattern.transDown[(i - 1)])) {
                        pattern.transDown[i] = true;
                    }
                    i++;
                }

            } else {
                pattern.pause[i] = true;
            }
        }

        return pattern;
    }

    public int[][] createRhythm(int patternLength) {
        int[] nothing = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        int[][] bdTemplates = {
                {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0}};

        int[][] sdTemplates = {
                {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0}};

        int[][] hhTemplates = {
                {0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0},
                {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}};

        int[][] ohTemplates = {
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0},
                {0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};

        int[][] loopTemplates = {
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}};

        int[] perc1Template = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] perc2Template = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        int[] bdTemplate = bdTemplates[(int) (Math.random() * bdTemplates.length)];
        int[] sdTemplate = sdTemplates[(int) (Math.random() * sdTemplates.length)];
        int hat = (int) (Math.random() * hhTemplates.length);
        int[] hhTemplate = hhTemplates[hat];
        int[] ohTemplate = ohTemplates[hat];
        int[] loopTemplate = loopTemplates[(int) (Math.random() * loopTemplates.length)];

        RhythmEvolver rc = new RhythmEvolver();

        for (int i = 1; i < Math.random() * 3.0D; i++) {
            rc.evolve(bdTemplate, new double[]{1.0D, 0.25D, 0.25D, 0.25D,
                    0.125D, 0.25D, 0.5D, 0.5D, 0.125D, 0.5D, 0.5D, 0.25D,
                    0.125D, 0.25D, 0.25D, 0.25D});
        }
        for (int i = 1; i < Math.random() * 3.0D; i++) {
            rc.evolve(sdTemplate, new double[]{0.125D, 0.25D, 0.25D, 0.25D,
                    1.0D, 0.25D, 0.25D, 0.33D});
        }
        for (int i = 1; i < Math.random() * 3.0D; i++) {
            rc.evolve(hhTemplate, new double[]{1.0D, 0.25D, 0.5D, 0.25D});
        }
        for (int i = 1; i < Math.random() * 3.0D; i++) {
            rc.evolve(ohTemplate, new double[]{1.0D, 0.25D, 0.5D, 0.25D});
        }
        if (Math.random() > 0.66D) {
            for (int i = 1; i < Math.random() * 3.0D; i++) {
                rc.evolve(perc1Template, new double[]{0.0D, 0.25D, 0.5D,
                        0.25D});
            }
            for (int i = 1; i < Math.random() * 3.0D; i++) {
                rc.evolve(perc2Template, new double[]{0.0D, 0.25D, 0.5D,
                        0.25D});
            }
        }
        for (int i = 0; i < ohTemplate.length; i++) {
            if (ohTemplate[i] > 0) {
                hhTemplate[i] = 0;
            }
        }

        double rnd = Math.random();
        int[] clap;
        int[] sd;
        if (rnd < 0.33D) {
            sd = sdTemplate;
            clap = nothing;
        } else {
            if (rnd < 0.66D) {
                sd = nothing;
                clap = sdTemplate;
            } else {
                sd = sdTemplate;
                clap = sdTemplate;
            }
        }

        int[][] r = {bdTemplate, sd, hhTemplate, ohTemplate, clap,
                perc1Template, perc2Template};

        return r;
    }

    private static abstract interface SyncopationStrategy {
        public abstract int[] execute(int[] paramArrayOfInt,
                                      double[] paramArrayOfDouble);
    }

    private class Markov {
        private double weight;
        private Object content;
        private ArrayList<Markov> kids;

        public Markov(Object content, double weight) {
            this.weight = weight;
            this.content = content;
            this.kids = new ArrayList();
        }

        public double getWeight() {
            return this.weight;
        }

        public boolean hasKids() {
            return !this.kids.isEmpty();
        }

        public void addKid(Markov m) {
            this.kids.add(m);
        }

        public Object getContent() {
            return this.content;
        }

        public Markov getKid() {
            double totalWeight = 0.0D;
            for (Markov m : this.kids) {
                totalWeight += m.getWeight();
            }
            double random = Math.random() * totalWeight;
            double curWeight = 0.0D;
            for (Markov m : this.kids) {
                curWeight += m.getWeight();
                if (curWeight > random) {
                    return m;
                }
            }
            return null;
        }
    }

    private class RhythmEvolver {
        private RhythmEvolver() {
        }

        public int[] evolve(int[] beat, double[] weights) {
            InstrumentSequencer.Markov strategies = new InstrumentSequencer.Markov(null,
                    0.0D);
            strategies.addKid(new InstrumentSequencer.Markov(new AdditionStrategy(),
                    1.0D));
            strategies.addKid(new InstrumentSequencer.Markov(new AccentStrategy(),
                    1.0D));
            strategies
                    .addKid(new InstrumentSequencer.Markov(new MoveStrategy(), 1.0D));

            InstrumentSequencer.SyncopationStrategy ss = (InstrumentSequencer.SyncopationStrategy) strategies
                    .getKid().getContent();
            ss.execute(beat, weights);

            return beat;
        }

        private class AccentStrategy implements
                InstrumentSequencer.SyncopationStrategy {
            private AccentStrategy() {
            }

            public int[] execute(int[] source, double[] weights) {
                InstrumentSequencer.Markov m = new InstrumentSequencer.Markov(null, 0.0D);
                for (int i = 0; i < source.length; i++) {
                    if (source[i] == 1) {
                        m.addKid(new InstrumentSequencer.Markov(i,
                                weights[(i % weights.length)]));
                    }
                }
                if (m.hasKids()) {
                    InstrumentSequencer.Markov m2 = m.getKid();
                    source[(Integer) m2.getContent()] = 2;
                }
                return source;
            }
        }

        private class RemovalStrategy implements
                InstrumentSequencer.SyncopationStrategy {
            private RemovalStrategy() {
            }

            public int[] execute(int[] source, double[] weights) {
                InstrumentSequencer.Markov m = new InstrumentSequencer.Markov(null, 0.0D);
                for (int i = 0; i < source.length; i++) {
                    if (source[i] > 0) {
                        m.addKid(new InstrumentSequencer.Markov(i,
                                1.0D / weights[(i % weights.length)]));
                    }
                }
                if (m.hasKids()) {
                    InstrumentSequencer.Markov m2 = m.getKid();
                    source[(Integer) m2.getContent()] = 0;
                }
                return source;
            }
        }

        private class MoveStrategy implements InstrumentSequencer.SyncopationStrategy {
            private MoveStrategy() {
            }

            public int[] execute(int[] source, double[] weights) {
                InstrumentSequencer.RhythmEvolver.RemovalStrategy remove = new InstrumentSequencer.RhythmEvolver.RemovalStrategy();
                source = remove.execute(source, weights);
                InstrumentSequencer.RhythmEvolver.AdditionStrategy add = new InstrumentSequencer.RhythmEvolver.AdditionStrategy();
                source = add.execute(source, weights);
                return source;
            }
        }

        private class AdditionStrategy implements
                InstrumentSequencer.SyncopationStrategy {
            private AdditionStrategy() {
            }

            public int[] execute(int[] source, double[] weights) {
                InstrumentSequencer.Markov m = new InstrumentSequencer.Markov(null, 0.0D);
                for (int i = 0; i < source.length; i++) {
                    if (source[i] == 0) {
                        m.addKid(new InstrumentSequencer.Markov(i,
                                weights[(i % weights.length)]));
                    }
                }
                if (m.hasKids()) {
                    InstrumentSequencer.Markov m2 = m.getKid();
                    source[(Integer) m2.getContent()] = 1;
                }
                return source;
            }
        }
    }
}
