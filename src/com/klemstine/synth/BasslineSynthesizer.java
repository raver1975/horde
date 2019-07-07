package com.klemstine.synth;

public class BasslineSynthesizer
        implements Synthesizer {
    private static final double[][] WAVETABLE_SAW;
    private static final double[][] WAVETABLE_SQUARE;
    private double SAMPLING_FREQUENCY = Output.SAMPLE_RATE * 2.0D;
    private Decimator decimator;
    public double frequency;
    private double bpm;
    private double samplesPer32thNote;
    private int slideCount;
    private double slideCoefficient;
    private final WavetableOscillator osc;
    private final BasslineFilter2 filter;
    public double tune;
    public final Control cutoff;
    public final Control resonance;
    private static final double CUTOFF_MAX = 5000.0D;
    private static final double CUTOFF_MIN = 200.0D;
    public double envMod;
    public double decay;
    public double accent;
    private double vol_i;
    private double gain_i;
    private static final double DECAY_MAX = 0.125D;
    private static final double DECAY_MIN = 20.0D;
    private static final int coeffOptimization = 16;
    private static final int coeffOptimizationIndex = 0;
    private final ADREnvelope aeg;
    private final double aegLevel = 0.6D;
    private final ADREnvelope feg;
    private final Distortion distortion;
    private int tmp;
    private static final double[] MIDI_NOTES = new double[127];
    public static final int MSG_NOTE_ON = 30;
    public static final int MSG_NOTE_OFF = 31;
    public static final int MSG_CONTROL_CHANGE = 32;
    public static final int MSG_CC_TUNE = 33;
    public static final int MSG_CC_CUTOFF = 34;
    public static final int MSG_CC_RESONANCE = 35;
    public static final int MSG_CC_ENVMOD = 36;
    public static final int MSG_CC_DECAY = 37;
    public static final int MSG_CC_ACCENT = 38;
    public static final int MSG_CC_VOLUME = 39;
    private double out;
    private double aux1;
    private double aux1Amt;
    private double aux2;
    private double aux2Amt;

    public BasslineSynthesizer() {
        this.decimator = new Decimator();

        this.distortion = new Distortion();
        this.distortion.setGain(1.0D);

        this.osc = new WavetableOscillator();
        this.osc.setSamplingFrequency(this.SAMPLING_FREQUENCY);

        this.tune = 1.0D;

        this.cutoff = new Control(Math.random() * 500.0D + 100.0D);
        this.cutoff.setSamplingFrequency(this.SAMPLING_FREQUENCY / 16.0D);
        this.resonance = new Control(Math.random());
        this.resonance.setSamplingFrequency(this.SAMPLING_FREQUENCY / 16.0D);

        this.filter = new BasslineFilter2(this.cutoff.getValue(), this.resonance.getValue());
        this.filter.setSamplingFrequency(this.SAMPLING_FREQUENCY);
        this.aeg = new ADREnvelope();
        this.aeg.setSamplingFrequency(this.SAMPLING_FREQUENCY);

        this.feg = new ADREnvelope();
        this.feg.setSamplingFrequency(this.SAMPLING_FREQUENCY / 16.0D);
        randomize();
    }

    public void randomize() {
        if (Math.random() > 0.5D) {
            this.osc.setWavetable(WAVETABLE_SQUARE);
        } else {
            this.osc.setWavetable(WAVETABLE_SAW);
        }
        cutoff.setValue(Math.random() * 3866.0D + 100.0D);
        resonance.setValue(Math.random());
     if (Math.random() > 0.66D)
       controlChange(39, (int)(64.0D + Math.random() * 63.0D));
     else {
       controlChange(39, 63);
     }
        this.envMod = Math.random();
        this.decay = (Math.random() * 19.875D + 0.125D);
        this.accent = (Math.random() * 0.5D + Math.random() * 0.2D);

        this.aeg.setAttack(80.0D);
        this.aeg.setDecay(0.25D);
        this.aeg.setRelease(100.0D);
        this.aeg.setLevel(0.6D);

        this.feg.setAttack(40.0D);
        this.feg.setDecay(this.decay);
        this.feg.setRelease(40.0D);

        this.aux1Amt = Math.random();
        this.aux1Amt = (1.0D - this.aux1Amt * this.aux1Amt);
        this.aux2Amt = (Math.random() * 0.66D);
    }

    public final double monoOutput() {
        return this.decimator.calc(monoOutputI(), monoOutputI()) * vol_i;
    }

    public final double monoOutputI() {
        if (this.aeg.getStage() != 3) {
            if (this.tmp++ == 16) {
                double mod = this.feg.tick() * this.envMod * 5.0D - this.envMod + 1.75D;
                this.filter.setCoefficients(this.cutoff.getInstancedValue() * mod, this.resonance.getInstancedValue());
                this.tmp = 0;
            }

            if (this.slideCount-- > 0) {
                this.frequency *= this.slideCoefficient;
                this.osc.setFrequency(this.frequency * this.tune);
            }

            this.out = (this.distortion.distort(this.filter.filter(this.osc.tick() * this.aeg.tick())) * 1.66D);
            return this.out;
        }

        return 0.0D;
    }

    public double getAux1() {
        return this.out * this.aux1Amt * vol_i;
    }

    public double getAux2() {
        return this.out * this.aux2Amt * vol_i;
    }

    public void setBpm(double bpm) {
        this.bpm = bpm;
        this.samplesPer32thNote = (Output.SAMPLE_RATE / (bpm / 60.0D) / 8.0D);
    }

    public double[] stereoOutput() {
        double tmp = monoOutput();
        return new double[]{tmp, tmp, this.out * this.aux1Amt, this.out * this.aux2Amt};
    }

    public void setWaveform(boolean waveSqure) {
        if (waveSqure) {
            this.osc.setWavetable(WAVETABLE_SQUARE);
        } else {
            this.osc.setWavetable(WAVETABLE_SAW);
        }
    }

    public void close() {
    }


    public void controlChange(int controller, int value) {

        switch (controller) {
            case MSG_CC_CUTOFF: //cutoff
                double newValue = value / 127.0D * 4800.0D + 200.0D;
                if (newValue > 0d && newValue < 4000d) {
                    this.cutoff.setValue(newValue);
                }
                break;
            case MSG_CC_RESONANCE: //resonance
                newValue = value / 127.0D;
                if (newValue > -.2d && newValue <= 1.0d) {
                    this.resonance.setValue(newValue);
                }
                break;
            case MSG_CC_ENVMOD: //envelop
                newValue = value / 127.0D;
                if (newValue > -.2d && newValue <= 1.3d) {
                    this.envMod = (newValue);
                }
                break;
            case MSG_CC_TUNE:  //tune
                newValue = value / 127.0F * 2.0F - 1.0F;
                if (newValue > -0.9d && newValue < 4d) {
                    this.tune = Math.pow(2.0D, newValue);
                    this.osc.setFrequency(this.frequency * this.tune);
                }
                break;
            case MSG_CC_DECAY:  //decay
                newValue = 1.0D - value / 127.0D;
                if (newValue > 0d && newValue < 1d) {
                    this.decay = (newValue * 19.875D + 0.125D);
                    this.feg.setDecay(this.decay);
                }
                break;
            case MSG_CC_VOLUME:  // volume
                newValue = value / 127.0D;
                if (newValue >= 0d && newValue <= 2d) {
//                    Output.setVolume((value / 127.0D));
                    this.vol_i = newValue;
                    System.out.println("bassline volume set to:" + vol_i);
                    if (this.vol_i > 1.0D) {
                        this.distortion.setGain(1.0D / ((this.vol_i - 1.0D) * 50.0D + 1.0D));
                        this.vol_i = 1.0D;
                    } else {
                        this.distortion.setGain(1.0D);
                    }
                }
                break;
            case MSG_CC_ACCENT:
                newValue = value / 127.0D;
                if (newValue >= 0d && newValue <= 1d) {
                    this.accent = (newValue);
                }
        }
    }

    public void noteOn(int note, int velocity) {
        if (this.aeg.getStage() > 1) {
            this.frequency = MIDI_NOTES[note];
            this.osc.setFrequency(this.frequency * this.tune);
            if (velocity > 80) {
                this.aeg.setLevel(0.6D + this.accent);
                this.feg.setLevel(1.0D + this.accent);
            } else {
                this.aeg.setLevel(0.6D);
                this.feg.setLevel(1.0D);
            }
            this.aeg.attack();
            this.feg.attack();
        } else {
            this.slideCount = (int) this.samplesPer32thNote;
            this.slideCoefficient = Math.pow(MIDI_NOTES[note] * this.tune / this.osc.getFrequency(), 1.0D / this.slideCount);
        }
    }

    public void noteOff() {
        this.aeg.release();
        this.feg.release();
    }

    static {
        WAVETABLE_SAW = WavetableOscillator.loadWavetable(Resources.load("saw.raw.tab"));

        WAVETABLE_SQUARE = WavetableOscillator.loadWavetable(Resources.load("sq.raw.tab"));

        for (double i = 0.0D; i < 127.0D; i += 1.0D)
            MIDI_NOTES[(int) i] = (8.1757989156D * Math.pow(2.0D, i / 12.0D));
    }
}

