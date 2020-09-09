package com.kg.wub.system;

import com.echonest.api.v4.TimedEvent;
import com.kg.TheHorde;
import com.kg.synth.Output;
import com.kg.wub.AudioObject;

public class Interval {

	public TimedEvent te;
	int y;
	public int startBytes;
	public int lengthBytes;
	public int endBytes;
	public int newbytestart;

	public Interval(TimedEvent te, int y) {
		double start1 = te.getStart();
		double duration = te.getDuration();
		double sampleRate= Output.SAMPLE_RATE;
		int frameSize= TheHorde.output.mixingAudioInputStream.getFormat().getFrameSize();
		startBytes = (int) (start1 * sampleRate * frameSize) - (int) (start1 * sampleRate * frameSize) % frameSize;
		double lengthInFrames = duration * sampleRate;
		lengthBytes = (int) (lengthInFrames * frameSize) - (int) (lengthInFrames * frameSize) % frameSize;
		endBytes = startBytes + lengthBytes;
		// queue.add(new Interval(startInBytes, Math.min(startInBytes +
		// lengthInBytes, data.length),te,y));
		this.y = y;
		this.te = te;
	}

	public String toString() {
		return startBytes + ":" + lengthBytes + ":" + y;
	}

	@Override
	public int hashCode() {
		return startBytes * 10001 + lengthBytes * 993 + y;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Interval))
			return false;
		Interval i = (Interval) o;
		return startBytes == i.startBytes && endBytes == i.endBytes && y == i.y;
	}
}
