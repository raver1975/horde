package com.klemstinegroup.wub.system;

import com.echonest.api.v4.Segment;
import com.echonest.api.v4.TimedEvent;
import com.echonest.api.v4.TrackAnalysis;

import java.util.ArrayList;
import java.util.List;

public class FakeTrackAnalysis extends TrackAnalysis {

	public double duration;
	public ArrayList<Segment> segments = new ArrayList<Segment>();
	public ArrayList<TimedEvent> sections = new ArrayList<TimedEvent>();
	public ArrayList<TimedEvent> bars = new ArrayList<TimedEvent>();
	public ArrayList<TimedEvent> beats = new ArrayList<TimedEvent>();
	public ArrayList<TimedEvent> tatums = new ArrayList<TimedEvent>();

	@Override
	public Double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public FakeTrackAnalysis() {
		super(null);
	}

	@Override
	public List<Segment> getSegments() {
		return new ArrayList<Segment>(segments);
	}

	@Override
	public List<TimedEvent> getSections() {
		return new ArrayList<TimedEvent>(sections);
	}

	@Override
	public List<TimedEvent> getBars() {
		return new ArrayList<TimedEvent>(bars);
	}

	@Override
	public List<TimedEvent> getBeats() {
		return new ArrayList<TimedEvent>(beats);
	}

	@Override
	public List<TimedEvent> getTatums() {
		return new ArrayList<TimedEvent>(tatums);
	}
}
