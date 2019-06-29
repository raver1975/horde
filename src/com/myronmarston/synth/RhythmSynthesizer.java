package com.myronmarston.synth;


public class RhythmSynthesizer
        implements Synthesizer {
    private static double[] bda = Resources.loadSample("909bd_atk.raw", 0.5D);
    private static double[] bds = Resources.loadSampleCyclic("909bd_base.raw", 0.75D);
    private static double[] bd808s = Resources.loadSampleCyclic("808bd.raw", 1.1D);

    private static double[] sds = Resources.loadSample("909sd_base.raw");
    private static double[] sdsn = Resources.loadSample("909sd_snap.raw");
    private static double[] sd808s = Resources.loadSample("808sd_base.raw", 1.25D);
    private static double[] sd808sn = Resources.loadSample("808sd_snap.raw", 1.25D);
    private static double[] sd808snot = Resources.loadSample("808sd_overtones.raw", 1.25D);
    private static double[] chs = Resources.loadSample("909ch.raw", 0.5D);
    private static double[] ohs = Resources.loadSample("909oh.raw", 0.5D);
    private static double[] ch808s = Resources.loadSample("808ch.raw", 0.66D);
    private static double[] oh808s = Resources.loadSample("808oh.raw", 0.66D);
    private static double[] claps;
    private static double[] clap808s;
    private static double[] lts;
    private static double[] mts = Resources.loadSample("909mt.raw");
    private static double[] hts = Resources.loadSample("909ht.raw");
    private static double[] hc808s;
    private static double[] mc808s = Resources.loadSample("808mc.raw");
    private static double[] cb808s;
    private static double[] crs;
    private static double[] crs808;
    private CubicOscillator bd;
    private BasicSampler bd808;
    private BasicSampler bdatk;
    private boolean isDistorted = false;
    private Distortion bddist;
    private ADREnvelope bdaeg;
    private ADREnvelope bdpeg;
    private RhythmSampler sd;
    private BasicSampler sd808;
    private BasicSampler sd808ot;
    private double sd808mix1;
    private double sd808mix2;
    private BasicSampler sdNoise;
    private ADREnvelope sdtone;
    private BasicSampler ch;
    private ADREnvelope chaeg;
    private BasicSampler oh;
    private ADREnvelope ohaeg;
    private BasicSampler clap;
    private RhythmSampler crash;
    private RhythmSampler lt;
    private ADREnvelope ltaeg;
    private double lt_volume;
    private RhythmSampler mt;
    private ADREnvelope mtaeg;
    private double mt_volume;
    private RhythmSampler ht;
    private ADREnvelope htaeg;
    private double ht_volume;
    private double bd_volume;
    private double bd_attack;
    private double sd_snappy;
    private double sd_volume;
    private double cp_volume;
    public static final int MSG_CC_BD_TUNE = 33;
    public static final int MSG_CC_BD_ATTACK = 34;
    public static final int MSG_CC_BD_DECAY = 35;
    public static final int MSG_CC_BD_VOLUME = 36;
    public static final int MSG_CC_SD_TUNE = 37;
    public static final int MSG_CC_SD_TONE = 38;
    public static final int MSG_CC_SD_SNAPPY = 39;
    public static final int MSG_CC_SD_VOLUME = 40;
    public static final int MSG_CC_LT_TUNE = 41;
    public static final int MSG_CC_LT_DECAY = 42;
    public static final int MSG_CC_LT_VOLUME = 43;
    public static final int MSG_CC_MT_TUNE = 44;
    public static final int MSG_CC_MT_DECAY = 45;
    public static final int MSG_CC_MT_VOLUME = 46;
    public static final int MSG_CC_HT_TUNE = 47;
    public static final int MSG_CC_HT_DECAY = 48;
    public static final int MSG_CC_HT_VOLUME = 49;
    private double aux;
    private double aux1Amt;
    private double aux2Amt;
    private boolean is808BassDrum;
    private boolean is808SnareDrum;
    private double vol_i = 1f;

    RhythmSynthesizer() {
        this.bd = new CubicOscillator();
        this.bd.setSample(bds);
        this.bd.setSamplingFrequency(Output.SAMPLE_RATE);
        this.bd808 = new BasicSampler();
        this.bd808.setSample(bd808s);
        this.bdatk = new BasicSampler();
        this.bdatk.setSample(bda);
        this.bddist = new Distortion();
        this.bddist.setGain(0.01D);
        this.bd_attack = 0.5D;
        this.bd_volume = 2.0D;

        this.bdaeg = new ADREnvelope();
        this.bdaeg.setSamplingFrequency(Output.SAMPLE_RATE);
        this.bdaeg.setAttack(1000.0D);
        this.bdaeg.setDecay(3.0D);
        this.bdaeg.setRelease(1.0D);
        this.bdaeg.setLevel(1.0D);
        this.bdpeg = new ADREnvelope();
        this.bdpeg.setSamplingFrequency(Output.SAMPLE_RATE);
        this.bdpeg.setAttack(2000.0D);
        this.bdpeg.setDecay(5.0D);
        this.bdpeg.setRelease(0.5D);
        this.bdpeg.setLevel(170.0D);

        this.sd = new RhythmSampler();
        this.sd.setSample(sds);
        this.sd.setRate(1.0D);
        this.bd.setSamplingFrequency(Output.SAMPLE_RATE);
        this.sd808 = new BasicSampler();
        this.sd808.setSample(sd808s);
        this.sd808ot = new BasicSampler();
        this.sd808ot.setSample(sd808snot);
        this.sdNoise = new BasicSampler();
        this.sdNoise.setSample(sdsn);
        this.sdtone = new ADREnvelope();
        this.sdtone.setSamplingFrequency(Output.SAMPLE_RATE);
        this.sdtone.setAttack(1000.0D);
        this.sdtone.setDecay(3.0D);
        this.sdtone.setRelease(1.0D);
        this.sdtone.setLevel(1.0D);
        this.sd_snappy = 1.0D;
        this.sd_volume = 1.0D;
        this.sd808mix1 = 1.0D;
        this.sd808mix2 = 0.0D;

        this.clap = new BasicSampler();
        this.clap.setSample(claps);
        this.cp_volume = 1.0D;
        this.crash = new RhythmSampler();
        this.crash.setSample(crs);

        this.ch = new BasicSampler();
        this.ch.setSample(chs);
        this.chaeg = new ADREnvelope();
        this.chaeg.setSamplingFrequency(Output.SAMPLE_RATE);
        this.chaeg.setAttack(2000.0D);
        this.chaeg.setDecay(5.0D);
        this.chaeg.setRelease(0.5D);
        this.chaeg.setLevel(0.5D);
        this.oh = new BasicSampler();
        this.oh.setSample(ohs);
        this.ohaeg = new ADREnvelope();
        this.ohaeg.setSamplingFrequency(Output.SAMPLE_RATE);
        this.ohaeg.setAttack(2000.0D);
        this.ohaeg.setDecay(2.0D);
        this.ohaeg.setRelease(0.5D);
        this.ohaeg.setLevel(0.1D);

        this.ltaeg = new ADREnvelope();
        this.ltaeg.setAttack(1000.0D);
        this.ltaeg.setDecay(3.0D);
        this.ltaeg.setRelease(1.0D);
        this.ltaeg.setLevel(1.0D);

        this.mt = new RhythmSampler();
        this.mt.setSample(mts);
        this.mt.setRate(1.0D);

        this.mtaeg = new ADREnvelope();
        this.mtaeg.setAttack(1000.0D);
        this.mtaeg.setDecay(1.0D);
        this.mtaeg.setRelease(1.0D);
        this.mtaeg.setLevel(1.0D);

        this.ht = new RhythmSampler();
        this.ht.setRate(1.0D);
        this.ht.setSample(hts);
        this.ht_volume = 0.75D;
        this.htaeg = new ADREnvelope();
        this.htaeg.setAttack(1000.0D);
        this.htaeg.setDecay(1.0D);
        this.htaeg.setRelease(1.0D);
        this.htaeg.setLevel(1.0D);

        randomize();
    }

    private void randomize() {
        controlChange(33, (int) (Math.random() * 127.0D));
        controlChange(34, (int) (Math.random() * 127.0D));
        controlChange(35, (int) (Math.random() * 127.0D));
        controlChange(39, (int) (Math.random() * 127.0D));
        controlChange(38, (int) (Math.random() * 127.0D));
        controlChange(37, (int) (Math.random() * 127.0D));
        this.chaeg.setDecay(5.0D + Math.random() * 5.0D);
        double max = 0.5D;

        this.aux1Amt = (Math.random() * 0.2D);
        this.aux2Amt = (Math.random() * 0.5D);
        this.isDistorted = (Math.random() > 0.66D);
        double tmp = Math.random() / 10.0D;
        tmp *= tmp;
        this.bddist.setGain(tmp + 0.05D);

        this.is808BassDrum = Math.random() > 0.8D;
        if (Math.random() > 0.66D) {
            this.is808SnareDrum = true;
            this.sd808mix1 = Math.random();
            this.sd808mix2 = (1.0D - this.sd808mix1);
            this.sdNoise.setSample(sd808sn);
        } else {
            this.is808SnareDrum = false;
            this.sdNoise.setSample(sdsn);
        }

        if (Math.random() > 0.5D)
            this.clap.setSample(claps);
        else {
            this.clap.setSample(clap808s);
        }
        if (Math.random() < 0.33D) {
            this.ch.setSample(ch808s);
            this.oh.setSample(oh808s);
        } else {
            this.ch.setSample(chs);
            this.oh.setSample(ohs);
        }

        if (Math.random() > 0.5D) {
            this.ht.setSample(hc808s);
            this.mt.setSample(mc808s);
        } else {
            this.ht.setSample(hts);
            this.mt.setSample(mts);
        }

        if (Math.random() < 0.2D) {
            this.ht.setSample(cb808s);
        }

        double rate = 1.0D - Math.random() * 0.2D;
        this.ht.setRate(rate);
        this.mt.setRate(rate);

        if (Math.random() < 0.33D) {
            this.crash.setSample(crs808);
            this.crash.setRate(1.0D);
        } else {
            this.crash.setSample(crs);
            double vari = Math.random() * max - max * 0.25D;
            this.crash.setRate(1.0D + vari);
        }
    }

    void setBpm(double value) {
    }

    public double monoOutput() {
        this.aux = 0.0D;
        this.bd.setFrequency(52.0D + this.bdpeg.tick());
        double bds = this.bd.tick() * this.bdaeg.tick() + this.bdatk.tick() * this.bd_attack;
        bds = this.bddist.distort(bds);
        double sds = (this.sd.tick() + this.sdNoise.tick() * this.sd_snappy) * this.sdtone.tick() * this.sd_volume;
        double chs = this.ch.tick() * this.chaeg.tick();
        double ohs = this.oh.tick() * this.ohaeg.tick();
        this.aux += sds + chs + ohs + this.clap.tick() + this.crash.tick();
        return bds * this.bd_volume + this.aux;
    }

    public double[] stereoOutput() {
        double left;
        double right;
        this.aux = (left = right = 0.0D);
        this.bd.setFrequency(52.0D + this.bdpeg.tick());
        double bds;
        if (this.is808BassDrum)
            bds = this.bd808.tick() * this.bdaeg.tick();
        else {
            bds = this.bd.tick() * this.bdaeg.tick() + this.bdatk.tick() * this.bd_attack;
        }
        if (this.isDistorted)
            bds = this.bddist.distort(bds) * 1.5D;
        double sds;
        if (this.is808SnareDrum)
            sds = (this.sd808.tick() * this.sd808mix1 + this.sd808ot.tick() * this.sd808mix2 + this.sdNoise.tick() * this.sd_snappy) * this.sd_volume;
        else {
            sds = (this.sd.tick() + this.sdNoise.tick() * this.sd_snappy) * this.sdtone.tick() * this.sd_volume;
        }
        double chs = this.ch.tick() * this.chaeg.tick();
        double ohs = this.oh.tick() * this.ohaeg.tick();
        double cps = this.clap.tick() * this.cp_volume;
        double crs = this.crash.tick();

        double perc1 = this.ht.tick() * this.ht_volume;
        double perc2 = this.mt.tick() * this.mt_volume;

        this.aux += sds + (chs + ohs) * 0.66D + cps + crs + (perc1 + perc2) * 2.0D;

        left += bds + (sds + perc2) * 0.66D + (chs + perc1 + ohs) * 1.33D + cps + crs * 0.5D;
        right += bds + (sds + perc2) * 1.33D + (chs + perc1 + ohs) * 0.66D + cps + crs * 1.5D;
        return new double[]{left * vol_i, right * vol_i, this.aux * this.aux1Amt * vol_i, this.aux * this.aux2Amt * vol_i};
    }


    public double getAux1() {
        return this.aux * this.aux1Amt;
    }

    public double getAux2() {
        return this.aux * this.aux2Amt;
    }

    public void resetAllControllers() {
    }

    public int getPitchBend() {
        return 0;
    }

    public void setPitchBend(int value) {
    }

    public int getProgram() {
        return 0;
    }

    public void programChange(int program) {
    }

    public void programChange(int bank, int program) {
    }

    public int getController(int controller) {
        return -1;
    }

    public double calcDecayCoefficient(double time) {
        return 0.0D;
    }

    void controlChange(int controller, int value) {
        switch (controller) {
            case 39:
                this.vol_i = value / 127d;
                break;
            case 36:
                bd_volume = (value / 127.0D * 2.0D);
                if (bd_volume > 1.0D) {
                    bddist.setGain(1.0D / ((bd_volume - 1.0D) * 20.0D + 1.0D));
                    bd_volume = 1.0D;
                }
                break;
            case 33:
                this.bdpeg.setDecay(10.0D - value / 127.0D * 5.0D);
                break;
            case 34:
                this.bd_attack = (value / 127.0D);
                break;
            case 35:
                this.bdaeg.setDecay(3.0D - value / 127.0D * 2.0D);
                break;
//            case 39:
//                this.sd_snappy = (value / 127.0D);
//                break;
            case 38:
                double sd808mix1 = value / 127.0D;
                this.sd808mix2 = (1.0D - sd808mix1);
                this.sdtone.setDecay(5.0D - value / 127.0D * 5.0D);
                break;
            case 37:
                this.sd.setRate(value / 256.0D + 0.5D);
                break;
            case 40:
                this.sd_volume = (value / 127.0D);
                break;
            case 41:
                this.lt.setRate(value / 256.0D + 0.5D);
                break;
            case 42:
                this.ltaeg.setDecay(3.0D - value / 127.0D * 3.0D);
                break;
            case 43:
                this.lt_volume = (value / 127.0D);
                break;
            case 44:
                this.mt.setRate(value / 256.0D + 0.5D);
                break;
            case 45:
                this.mtaeg.setDecay(3.0D - value / 127.0D * 3.0D);
                break;
            case 46:
                this.mt_volume = (value / 127.0D);
                break;
            case 47:
                this.ht.setRate(value / 256.0D + 0.5D);
                break;
            case 48:
                this.htaeg.setDecay(3.0D - value / 127.0D * 3.0D);
                break;
            case 49:
                this.ht_volume = (value / 127.0D);
        }
    }

    void noteOn(int note, int velocity) {
        double vel = velocity / 255.0D;
        switch (note) {
            case 32:
                this.bdatk.trigger();
                this.bd808.trigger();
                this.bdaeg.attack();
                this.bdaeg.setLevel(vel);
                this.bdpeg.attack();
                this.bd.trigger();
                break;
            case 33:
                this.sd.trigger();
                this.sd808.trigger();
                this.sd808ot.trigger();
                this.sdtone.attack();
                this.sd_volume = vel;
                this.sdNoise.trigger();
                break;
            case 34:
                this.ch.trigger();
                this.chaeg.setLevel(vel);
                this.chaeg.attack();
                this.ohaeg.release();
                break;
            case 35:
                this.oh.trigger();
                this.ohaeg.setLevel(vel);
                this.ohaeg.attack();
                break;
            case 36:
                this.clap.trigger();
                this.cp_volume = vel;
                break;
            case 37:
                this.ht.trigger();
                this.ht_volume = vel;
                break;
            case 38:
                this.mt.trigger();
                this.mt_volume = vel;
                break;
            case 0:
                this.crash.trigger();
        }
    }

    private void noteOff(int note) {
    }

    public void noteOff(int note, int velocity) {
        noteOff(note);
    }

    static {
        hc808s = Resources.loadSample("808hc.raw");
        cb808s = Resources.loadSample("808cb.raw");
        claps = Resources.loadSample("909clap.raw", 0.75D);
        clap808s = Resources.loadSample("808cp.raw");
        crs = Resources.loadSample("909crash.raw", 0.5D);
        crs808 = Resources.loadSample("808crash.raw", 0.66D);
    }
}

