package com.echonest.api.v4;

import com.echonest.api.v4.util.MQuery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrackAnalysis implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8164758136686318758l;
	@SuppressWarnings("unchecked")
	private Map map;
	private MQuery mq;
	private ArrayList<TimedEvent> sections;
	private ArrayList<TimedEvent> bars;
	private ArrayList<TimedEvent> beats;
	private ArrayList<TimedEvent> tatums;
	private ArrayList<Segment> segments;

	@SuppressWarnings("unchecked")
    public TrackAnalysis(Map map) {
		this.map = map;
		this.mq = new MQuery(map);
	}

	@SuppressWarnings("unchecked")
	public Map getMap() {
		return map;
	}

	@Override
	public String toString() {
		return map.toString();
	}

	public Integer getNumSamples() {
		return mq.getInteger("track.num_samples");
	}

	public Double getDuration() {
		return mq.getDouble("track.duration");
	}

	public String getMD5() {
		return mq.getString("track.sample_md5");
	}

	public Double getSampleRate() {
		return mq.getDouble("track.analysis_sample_rate");
	}

	public Integer getNumChannels() {
		return mq.getInteger("track.analysis_channels");
	}

	public Double getEndOfFadeIn() {
		return mq.getDouble("track.end_of_fade_in");
	}

	public Double getStartOfFadeOut() {
		return mq.getDouble("track.start_of_fade_out");
	}

	public Double getLoudness() {
		return mq.getDouble("track.loudness");
	}

	public Double getTempo() {
		return mq.getDouble("track.tempo");
	}

	public Double getTempoConfidence() {
		return mq.getDouble("track.tempo_confidence");
	}

	public Integer getTimeSignature() {
		return mq.getInteger("track.time_signature");
	}

	public Double getTimeSignatureConfidence() {
		return mq.getDouble("track.time_signature_confidence");
	}

	public Integer getKey() {
		return mq.getInteger("track.key");
	}

	public Double getKeyConfidence() {
		return mq.getDouble("track.key_confidence");
	}

	public Integer getMode() {
		return mq.getInteger("track.mode");
	}

	public Double getModeConfidence() {
		return mq.getDouble("track.mode_confidence");
	}

	@SuppressWarnings("unchecked")
	public List<TimedEvent> getSections() {
		if (sections == null) {
			sections = new ArrayList<TimedEvent>();
			List event = (List) mq.getObject("sections");
			for (int i = 0; i < event.size(); i++) {
				sections.add(new TimedEvent((Map) event.get(i)));
			}
		}
		return sections;
	}

	@SuppressWarnings("unchecked")
	public List<TimedEvent> getBars() {
		if (bars == null) {
			bars = new ArrayList<TimedEvent>();
			List event = (List) mq.getObject("bars");
			for (int i = 0; i < event.size(); i++) {
				bars.add(new TimedEvent((Map) event.get(i)));
			}
		}
		return bars;
	}

	@SuppressWarnings("unchecked")
	public List<TimedEvent> getBeats() {
		if (beats == null) {
			beats = new ArrayList<TimedEvent>();
			List event = (List) mq.getObject("beats");
			for (int i = 0; i < event.size(); i++) {
				beats.add(new TimedEvent((Map) event.get(i)));
			}
		}
		return beats;
	}

	@SuppressWarnings("unchecked")
	public List<TimedEvent> getTatums() {
		if (tatums == null) {
			tatums = new ArrayList<TimedEvent>();
			List event = (List) mq.getObject("tatums");
			for (int i = 0; i < event.size(); i++) {
				tatums.add(new TimedEvent((Map) event.get(i)));
			}
		}
		return tatums;
	}

	@SuppressWarnings("unchecked")
	public List<Segment> getSegments() {
		if (segments == null) {
			segments = new ArrayList<Segment>();
			List event = (List) mq.getObject("segments");
			for (int i = 0; i < event.size(); i++) {
				segments.add(new Segment((Map) event.get(i)));
			}
		}
		return segments;
	}

	public void dump() {
		System.out.println("num samples : " + getNumSamples());
		System.out.println("sample md5  : " + getMD5());
		System.out.println("num channels: " + getNumChannels());
		System.out.println("duration    : " + getDuration());

		System.out.println(" Sections ");
		List<TimedEvent> sections = getSections();
		for (TimedEvent e : sections) {
			System.out.println(e);
		}

		System.out.println(" Bars ");
		List<TimedEvent> bars = getBars();
		for (TimedEvent e : bars) {
			System.out.println(e);
		}

		System.out.println(" Beats ");
		List<TimedEvent> beats = getBeats();
		for (TimedEvent e : beats) {
			System.out.println(e);
		}

		System.out.println(" Tatums ");
		List<TimedEvent> tatums = getTatums();
		for (TimedEvent e : tatums) {
			System.out.println(e);
		}

		System.out.println(" Segments ");
		List<Segment> segments = getSegments();
		for (Segment e : segments) {
			System.out.println(e);
		}
	}
}
