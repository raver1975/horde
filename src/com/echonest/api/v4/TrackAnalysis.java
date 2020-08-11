package com.echonest.api.v4;

import com.echonest.api.v4.util.MQuery;

import java.io.Serializable;
import java.util.*;

import static com.kg.wub.ai.vectorrnn1.RNNDemo.song;

public class TrackAnalysis implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8164758136686318758l;
    @SuppressWarnings("unchecked")
    private Map map;
    public MQuery mq;
    public ArrayList<TimedEvent> sections;
    public ArrayList<TimedEvent> bars;
    public ArrayList<TimedEvent> beats;
    public ArrayList<TimedEvent> tatums;
    private ArrayList<Segment> segments;
    double tempo;
    double bpmFactor = 1d;
    protected double duration;

    @SuppressWarnings("unchecked")
    public TrackAnalysis(Map map, double bpmFactor, double tempo, double duration) {
        this.tempo = tempo;
        this.duration = duration / bpmFactor;
        this.bpmFactor = bpmFactor;
        finishInit(map, bpmFactor);
    }

    public void finishInit(Map map, double bpmFactor) {
        this.mq = new MQuery(map);
        this.map = map;
        sections = new ArrayList<TimedEvent>();
        List event = (List) mq.getObject("sections");
        if (event != null) {
            for (int i = 0; i < event.size(); i++) {
                TimedEvent te = new TimedEvent((Map) event.get(i), bpmFactor);
                sections.add(te);
            }
        }

        bars = new ArrayList<TimedEvent>();
        event = (List) mq.getObject("bars");
        if (event != null) {
            for (int i = 0; i < event.size(); i++) {
                TimedEvent te = new TimedEvent((Map) event.get(i), bpmFactor);
                bars.add(te);
            }
        }

        beats = new ArrayList<TimedEvent>();
        event = (List) mq.getObject("beats");
        if (event != null) {
            for (int i = 0; i < event.size(); i++) {
                TimedEvent te = new TimedEvent((Map) event.get(i), bpmFactor);
                beats.add(te);
            }
        }

        tatums = new ArrayList<TimedEvent>();
        event = (List) mq.getObject("tatums");
        if (event != null) {
            for (int i = 0; i < event.size(); i++) {
                TimedEvent te = new TimedEvent((Map) event.get(i), bpmFactor);
                tatums.add(te);
            }
        }

        segments = new ArrayList<Segment>();
        if (event != null) {
            event = (List) mq.getObject("segments");
            for (int i = 0; i < event.size(); i++) {
                Segment te = new Segment((Map) event.get(i), bpmFactor);
                segments.add(te);
            }
        }
    }

    public TrackAnalysis(Map map) {
        bpmFactor = 1d;
        finishInit(map, 1d);
        if (mq.getDouble("track.duration") != null) {
            duration = (double) mq.getDouble("track.duration");
        }
        if (mq.getDouble("track.tempo") != null) {
            tempo = (double) mq.getDouble("track.tempo");
        }
        System.out.println("tttempo=" + tempo);
//        bpmFactor=(double)map.get("track.bpmFactor");
//        song.analysis.setDuration(song.analysis.getDuration()*bpmFactor);
    }

    @SuppressWarnings("unchecked")
    public Map getMap() {
        return map;
    }

    @Override
    public String toString() {
        if (map != null) {
            return map.toString();
        }
        return "map null";
    }

    public Integer getNumSamples() {
        return mq.getInteger("track.num_samples");
    }

    public Double getDuration() {
        return duration;
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
        return tempo;
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

    public List<TimedEvent> getSections() {
        return sections;
    }

    public List<TimedEvent> getBars() {
        return bars;
    }

    public List<TimedEvent> getBeats() {
        return beats;
    }

    public List<TimedEvent> getTatums() {
        return tatums;
    }

    public List<Segment> getSegments() {

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

    public double getTempo1() {
        return mq.getDouble("track.tempo");
    }

    public double getDuration1() {
        return mq.getDouble("track.duration");
    }


    public void setDuration(double duration) {
        this.duration = duration;
    }
}
