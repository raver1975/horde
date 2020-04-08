package com.klemstinegroup.wub.system;

//import com.jsyn.JSyn;
//import com.jsyn.Synthesizer;
//import com.jsyn.data.ShortSample;
//import com.jsyn.unitgen.LineOut;
//import com.jsyn.unitgen.VariableRateStereoReader;
//import com.softsynth.jsyn.Synth;

public class JsynSamplePlayer { 

	public JsynSamplePlayer(byte[] data) {
//		Synth.startEngine(0);
//		LineOut lo = new LineOut();
//		Synthesizer synth = JSyn.createSynthesizer();
//		VariableRateStereoReader samplePlayer = new VariableRateStereoReader();
//		synth.add(samplePlayer);
//		synth.add(lo);
//		samplePlayer.output.connect(0, lo.input, 0);
//		samplePlayer.output.connect(0, lo.input, 1);
//		samplePlayer.start();
//		lo.start();
//		synth.start();
//
//		System.out.println("dumping sample");
//		ShortSample myStereoSample = new ShortSample(data.length / AudioObject.frameSize, 2);
//		byte LSB, MSB;
//		short left, right; 
//		for (int i = 0; i < data.length / AudioObject.frameSize; i += 1) {
//			LSB = data[4 * i];
//			MSB = data[4 * i + 1];
//			left = (short) (((MSB & 0xFF) << 8) | (LSB & 0xFF));
//
//			LSB = data[4 * i + 2];
//			MSB = data[4 * i + 3];
//			right = (short) (((MSB & 0xFF) << 8) | (LSB & 0xFF));
//			myStereoSample.write(i, new short[] { left, right }, 0, 1);
//		}
//		samplePlayer.rate.set(44100);
//		samplePlayer.dataQueue.queue(myStereoSample, 0, myStereoSample.getNumFrames());
//
//		try {
//			double time = synth.getCurrentTime();
//			synth.sleepUntil(time + 400.0);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		synth.stop();

	}

}
