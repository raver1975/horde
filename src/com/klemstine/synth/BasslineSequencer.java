package com.klemstine.synth;

import java.util.ArrayList;

public class BasslineSequencer extends Sequencer {
    private BasslineSynthesizer synth;

    BasslineSequencer(BasslineSynthesizer synth) {
        this.synth = synth;
        randomizeRhythm();
        randomizeSequence();
    }

    public void randomizeRhythm() {
        this.setRhythm(createRhythm(this.patternLength));
    }

    public void randomizeSequence() {
        System.out.println("here!");
        synth.randomize();
        double[] basicCoeffs = {0.5D, 0.5D, 0.5D, 0.5D};
        double[] bassCoeffs = new double[16];
        boolean preferBassDrum = Math.random() > 0.5D;
        boolean preferSnareDrum = Math.random() > 0.5D;
        if ((!preferBassDrum) && (!preferSnareDrum)) {
            preferBassDrum = preferSnareDrum = true;
        }
        for (int i = 0; i < this.getRhythm()[0].length; i++) {
            bassCoeffs[i] = basicCoeffs[(i % 4)];
            if (((this.getRhythm()[0][i] > 0) && (preferBassDrum))
                    || ((preferSnareDrum) && ((this.getRhythm()[1][i] > 0) || (this.getRhythm()[4][i] > 0))))
                bassCoeffs[i] *= 4.0D;
            if (this.getRhythm()[3][i] > 0)
                bassCoeffs[i] *= 2.0D;
            if (this.getRhythm()[4][i] > 0) {
                bassCoeffs[i] *= 2.0D;
            }
        }

        Markov markov = new Markov(null, 0.0D);
        markov.addKid(new Markov(Harmony.SCALE_MELODIC_MINOR, 2.0D));
        markov.addKid(new Markov(Harmony.SCALE_MAJOR, 1.0D));
        markov.addKid(new Markov(Harmony.SCALE_HUNGARIAN_MINOR, 0.5D));
        this.setBassline(createBassline(this.patternLength, (int[]) (int[]) markov.getKid().getContent(), bassCoeffs));
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
                if ((!this.getBassline().pause[this.step])) {
                    this.synth
                            .noteOn(this.getBassline().note[this.step] + 36, this.getBassline().accent[this.step] ? 127 : 80);
                }
                if (this.shuffle)
                    setBpm(this.bpm);
            } else {
                if (!this.getBassline().slide[this.step]) {
                    this.synth.noteOff();
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

    public void setBpm(double value) {
        this.bpm = value;
        this.synth.setBpm(value);
//        this.synth.setBpm(value);
        this.samplesPerSequencerUpdate = (int) (Output.SAMPLE_RATE / (this.bpm / 60.0D) / 8.0D);

        if (this.shuffle)
            if (this.step % 2 == 0)
                this.samplesPerSequencerUpdate += (int) (this.samplesPerSequencerUpdate * 0.33D);
            else
                this.samplesPerSequencerUpdate -= (int) (this.samplesPerSequencerUpdate * 0.33D);
    }

    private BasslinePattern createBassline(int length, int[] scale, double[] weights) {
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
                for (int value : scale) {
                    int prob = 1;
                    if (value == 0)
                        prob *= 2;
                    if (value == prevNote)
                        prob *= 3;
                    if ((value == prevNote - 1)
                            || (value == prevNote + 2))
                        prob *= 2;
                    m.addKid(new Markov(value, prob));
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
//                if ((Math.random() < noteTranspProb) && (note + transpose < 12)) {
//                    pattern.transUp[i] = true;
//                } else if ((Math.random() < noteTranspProb)
//                        && (note + transpose > -12)) {
//                    pattern.transDown[i] = true;
//                }
                while ((Math.random() * sustainWeight > weights[((i + 1) % weights.length)])
                        && (i < length)) {
                    pattern.slide[i] = true;
//                    if ((i != 0) && (pattern.transUp[(i - 1)])) {
//                        pattern.transUp[i] = true;
//                    }
//                    if ((i != 0) && (pattern.transDown[(i - 1)])) {
//                        pattern.transDown[i] = true;
//                    }
                    i++;
                }
            } else {
                pattern.pause[i] = true;
            }
        }
        return pattern;
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
            Markov strategies = new Markov(null,
                    0.0D);
            strategies.addKid(new Markov(new AdditionStrategy(),
                    1.0D));
            strategies.addKid(new Markov(new AccentStrategy(),
                    1.0D));
            strategies
                    .addKid(new Markov(new MoveStrategy(), 1.0D));

            SyncopationStrategy ss = (SyncopationStrategy) strategies
                    .getKid().getContent();
            ss.execute(beat, weights);

            return beat;
        }

        private class AccentStrategy implements
                SyncopationStrategy {
            private AccentStrategy() {
            }

            public int[] execute(int[] source, double[] weights) {
                Markov m = new Markov(null, 0.0D);
                for (int i = 0; i < source.length; i++) {
                    if (source[i] == 1) {
                        m.addKid(new Markov(i,
                                weights[(i % weights.length)]));
                    }
                }
                if (m.hasKids()) {
                    Markov m2 = m.getKid();
                    source[(Integer) m2.getContent()] = 2;
                }
                return source;
            }
        }

        private class RemovalStrategy implements
                SyncopationStrategy {
            private RemovalStrategy() {
            }

            public int[] execute(int[] source, double[] weights) {
                Markov m = new Markov(null, 0.0D);
                for (int i = 0; i < source.length; i++) {
                    if (source[i] > 0) {
                        m.addKid(new Markov(i,
                                1.0D / weights[(i % weights.length)]));
                    }
                }
                if (m.hasKids()) {
                    Markov m2 = m.getKid();
                    source[(Integer) m2.getContent()] = 0;
                }
                return source;
            }
        }

        private class MoveStrategy implements SyncopationStrategy {
            private MoveStrategy() {
            }

            public int[] execute(int[] source, double[] weights) {
                RhythmEvolver.RemovalStrategy remove = new RhythmEvolver.RemovalStrategy();
                source = remove.execute(source, weights);
                RhythmEvolver.AdditionStrategy add = new RhythmEvolver.AdditionStrategy();
                source = add.execute(source, weights);
                return source;
            }
        }

        private class AdditionStrategy implements
                SyncopationStrategy {
            private AdditionStrategy() {
            }

            public int[] execute(int[] source, double[] weights) {
                Markov m = new Markov(null, 0.0D);
                for (int i = 0; i < source.length; i++) {
                    if (source[i] == 0) {
                        m.addKid(new Markov(i,
                                weights[(i % weights.length)]));
                    }
                }
                if (m.hasKids()) {
                    Markov m2 = m.getKid();
                    source[(Integer) m2.getContent()] = 1;
                }
                return source;
            }
        }
    }
}
