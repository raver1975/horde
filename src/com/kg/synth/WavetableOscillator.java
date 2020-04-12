package com.kg.synth;

public class WavetableOscillator extends Oscillator {
	protected int waveIndex = 33;
	protected double waveLength;
	protected double[][] wavetable;
	private double[] wave;
	private static final double[] MIDI_NOTES = new double[127];
	private double wavePhase;
	private int x;
	private double mu;
	private double mu2;
	private double y0;
	private double y1;
	private double y2;
	private double y3;
	private double a0;
	private double a1;
	private double a2;
	private double a3;

	public final double tick() {
		try{
		this.phase += this.phaseDelta;
		if (this.phase >= 1.0D) {
			this.phase -= 1.0D;
		}
		this.wavePhase = (this.phase * this.waveLength + 1.0D);

		this.x = (int) this.wavePhase;
		this.mu = (this.wavePhase - this.x);
		this.mu2 = (this.mu * this.mu);
		this.y0 = this.wave[(this.x - 1)];
		this.y1 = this.wave[this.x];
		this.y2 = this.wave[(this.x + 1)];
		this.y3 = this.wave[(this.x + 2)];

		this.a0 = (this.y3 - this.y2 - this.y0 + this.y1);
		this.a1 = (this.y0 - this.y1 - this.a0);
		this.a2 = (this.y2 - this.y0);
		this.a3 = this.y1;
	}
catch(Exception e){e.printStackTrace();}

		return this.a0 * this.mu * this.mu2 + this.a1 * this.mu2 + this.a2
				* this.mu + this.a3;
	}

	public final void setFrequency(double frequency) {
		if (this != null) {
			this.phaseDelta = (frequency * this.oneHzPhaseDelta);
			if ((frequency > MIDI_NOTES[this.waveIndex])
					|| (frequency < MIDI_NOTES[this.waveIndex])) {
				this.waveIndex = seekIndexByFrequency(frequency);
				this.wave = this.wavetable[this.waveIndex];
				if (this.wave!=null)this.waveLength = (this.wave.length - 3);
			}
		}
	}

	public double getFrequency() {
		return this.phaseDelta / this.oneHzPhaseDelta;
	}

	public double[][] getWavetable() {
		return this.wavetable;
	}

	public void setWavetable(double[][] wavetable) {
		this.wavetable = wavetable;
		this.wave = wavetable[this.waveIndex];
	}

	private static int getIndexByFrequency(double frequency) {
		return (int) (12.0D * Math.log(frequency / 440.0D)) + 57;
	}

	private static int seekIndexByFrequency(double frequency) {
		int i = 64;
		int delta = 32;
		double i1;
		double i2;
		do {
			i1 = MIDI_NOTES[i];

			if (i1 > frequency) {
				i -= delta;
				delta = (int) (delta * 0.5D);
			}

			i2 = MIDI_NOTES[(i + 1)];

			if (i2 < frequency) {
				i += delta;
				delta = (int) (delta * 0.5D);
			}
		} while ((i1 > frequency) || (i2 < frequency));

		return i;
	}

	public static double[][] loadWavetable(byte[] bytes) {
		double[][] wavetable = new double[127][];

		int fileIndex = 0;

		for (int i = 12; i < 127; i++) {
			double[] wave = new double[(int) Math.round(Output.SAMPLE_RATE
					/ MIDI_NOTES[i]) + 3];
			for (int j = 1; j < wave.length - 2; j++) {
				int s = bytes[(fileIndex + 1)] << 8 | bytes[(fileIndex + 0)]
						& 0xFF;
				wave[j] = (s / 32768.0D);
				fileIndex += 2;
			}
			wave[(wave.length - 1)] = wave[2];
			wave[(wave.length - 2)] = wave[1];
			wave[0] = wave[(wave.length - 3)];
			wavetable[i] = wave;
		}

		return wavetable;
	}

	static {
		for (double i = 0.0D; i < 127.0D; i += 1.0D)
			MIDI_NOTES[(int) i] = (8.1757989156D * Math.pow(2.0D, i / 12.0D));
	}
}
