package com.klemstine.synth;

import java.util.ArrayList;

public class RhythmSequencer extends Sequencer {
    public RhythmSynthesizer synth;
    private int[][] drums;
    private double bpm;
    private boolean shuffle;
    private int samplesPerSequencerUpdate;
    public int tick = 0;
    public int step = 0;
    private boolean sixteenth_note = true;

    RhythmSequencer(RhythmSynthesizer drums) {
        this.synth = drums;
        randomizeRhythm();
        randomizeSequence();
    }

    public void randomizeRhythm() {
        int patternLength = 16;
        this.drums = createRhythm(patternLength);
    }

    public void randomizeSequence() {
        Markov delayTimes = new Markov(null, 0.0D);
        delayTimes.addKid(new Markov(this.samplesPerSequencerUpdate * 2 * 2, 0.5D));
        delayTimes.addKid(new Markov(this.samplesPerSequencerUpdate * 2 * 3, 2.0D));
        delayTimes.addKid(new Markov(this.samplesPerSequencerUpdate * 2 * 4, 1.0D));
        delayTimes.addKid(new Markov(this.samplesPerSequencerUpdate * 2 * 8, 1.0D));
//        Output.getDelay().setFeedback(Math.random() * 0.75D + 0.2D);
//        Output.getDelay().setTime(
//                (Integer) delayTimes.getKid().getContent());
    }

    public void tick() {
        if (this.tick == 0) {
            if (this.sixteenth_note) {
                for (int ch = 0; ch < this.drums.length; ch++) {
                    if (this.drums[ch][this.step] != 0) {
                        int vol = 255;
                        if ((this.step > 1) && (this.step < 15)
                                && (this.drums[ch][(this.step - 1)] != 0)) {
                            vol = (int) (vol * 0.66D);
                        }
                        if (this.step % 4 != 0)
                            vol = (int) (vol * 0.66D);
                        if (this.step % 2 != 0) {
                            vol = (int) (vol * 0.66D);
                        }
                        this.synth.noteOn(ch + 32, vol);
                    }
                }
                if (this.shuffle)
                    setBpm(this.bpm);
            } else {
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

    public void setBpm(double value) {
        this.bpm = value;
        this.synth.setBpm(value);
        this.samplesPerSequencerUpdate = (int) (Output.SAMPLE_RATE / (this.bpm / 60.0D) / 8.0D);
        if (this.shuffle)
            if (this.step % 2 == 0)
                this.samplesPerSequencerUpdate += (int) (this.samplesPerSequencerUpdate * 0.33D);
            else
                this.samplesPerSequencerUpdate -= (int) (this.samplesPerSequencerUpdate * 0.33D);
    }
    private int[][] createRhythm(int patternLength) {
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

        return new int[][]{bdTemplate, sd, hhTemplate, ohTemplate, clap,
                perc1Template, perc2Template};
    }

    private static abstract interface SyncopationStrategy {
        public abstract int[] execute(int[] paramArrayOfInt,
                                      double[] paramArrayOfDouble);
    }

    private class Markov {
        private double weight;
        private Object content;
        private ArrayList<Markov> kids;

        Markov(Object content, double weight) {
            this.weight = weight;
            this.content = content;
            this.kids = new ArrayList<Markov>();
        }

        double getWeight() {
            return this.weight;
        }

        boolean hasKids() {
            return !this.kids.isEmpty();
        }

        void addKid(Markov m) {
            this.kids.add(m);
        }

        Object getContent() {
            return this.content;
        }

        Markov getKid() {
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

        int[] evolve(int[] beat, double[] weights) {
            RhythmSequencer.Markov strategies = new RhythmSequencer.Markov(null,
                    0.0D);
            strategies.addKid(new RhythmSequencer.Markov(new AdditionStrategy(),
                    1.0D));
            strategies.addKid(new RhythmSequencer.Markov(new AccentStrategy(),
                    1.0D));
            strategies
                    .addKid(new RhythmSequencer.Markov(new MoveStrategy(), 1.0D));

            RhythmSequencer.SyncopationStrategy ss = (RhythmSequencer.SyncopationStrategy) strategies
                    .getKid().getContent();
            ss.execute(beat, weights);

            return beat;
        }

        private class AccentStrategy implements
                RhythmSequencer.SyncopationStrategy {
            private AccentStrategy() {
            }

            public int[] execute(int[] source, double[] weights) {
                RhythmSequencer.Markov m = new RhythmSequencer.Markov(null, 0.0D);
                for (int i = 0; i < source.length; i++) {
                    if (source[i] == 1) {
                        m.addKid(new RhythmSequencer.Markov(i,
                                weights[(i % weights.length)]));
                    }
                }
                if (m.hasKids()) {
                    RhythmSequencer.Markov m2 = m.getKid();
                    source[(Integer) m2.getContent()] = 2;
                }
                return source;
            }
        }

        private class RemovalStrategy implements
                RhythmSequencer.SyncopationStrategy {
            private RemovalStrategy() {
            }

            public int[] execute(int[] source, double[] weights) {
                RhythmSequencer.Markov m = new RhythmSequencer.Markov(null, 0.0D);
                for (int i = 0; i < source.length; i++) {
                    if (source[i] > 0) {
                        m.addKid(new RhythmSequencer.Markov(i,
                                1.0D / weights[(i % weights.length)]));
                    }
                }
                if (m.hasKids()) {
                    RhythmSequencer.Markov m2 = m.getKid();
                    source[(Integer) m2.getContent()] = 0;
                }
                return source;
            }
        }

        private class MoveStrategy implements RhythmSequencer.SyncopationStrategy {
            private MoveStrategy() {
            }

            public int[] execute(int[] source, double[] weights) {
                RhythmSequencer.RhythmEvolver.RemovalStrategy remove = new RhythmSequencer.RhythmEvolver.RemovalStrategy();
                source = remove.execute(source, weights);
                RhythmSequencer.RhythmEvolver.AdditionStrategy add = new RhythmSequencer.RhythmEvolver.AdditionStrategy();
                source = add.execute(source, weights);
                return source;
            }
        }

        private class AdditionStrategy implements
                RhythmSequencer.SyncopationStrategy {
            private AdditionStrategy() {
            }

            public int[] execute(int[] source, double[] weights) {
                RhythmSequencer.Markov m = new RhythmSequencer.Markov(null, 0.0D);
                for (int i = 0; i < source.length; i++) {
                    if (source[i] == 0) {
                        m.addKid(new RhythmSequencer.Markov(i,
                                weights[(i % weights.length)]));
                    }
                }
                if (m.hasKids()) {
                    RhythmSequencer.Markov m2 = m.getKid();
                    source[(Integer) m2.getContent()] = 1;
                }
                return source;
            }
        }
    }
}
