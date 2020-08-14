package com.echonest.api.v4;

import com.echonest.api.v4.util.MQuery;
import com.kg.synth.Sequencer;

import java.io.Serializable;
import java.util.*;


public class TrackAnalysis implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8164758136686318758l;
    private ArrayList<TimedEvent> sections=new ArrayList<>();;
    private ArrayList<TimedEvent> bars=new ArrayList<>();;
    private ArrayList<TimedEvent> beats=new ArrayList<>();;
    private ArrayList<TimedEvent> tatums=new ArrayList<>();;
    private ArrayList<Segment> segments=new ArrayList<>();;
    private double tempo= Sequencer.bpm;
    private double duration=1;


    public TrackAnalysis(Map map) {
        if (map==null){
            return;
        }
        MQuery mq = new MQuery(map);
        sections = new ArrayList<TimedEvent>();
        List event = (List) mq.getObject("sections");
        if (event != null) {
            for (int i = 0; i < event.size(); i++) {
                TimedEvent te = new TimedEvent((Map) event.get(i));
                sections.add(te);
            }
        }

        bars = new ArrayList<TimedEvent>();
        event = (List) mq.getObject("bars");
        if (event != null) {
            for (int i = 0; i < event.size(); i++) {
                TimedEvent te = new TimedEvent((Map) event.get(i));
                bars.add(te);
            }
        }

        beats = new ArrayList<TimedEvent>();
        event = (List) mq.getObject("beats");
        if (event != null) {
            for (int i = 0; i < event.size(); i++) {
                TimedEvent te = new TimedEvent((Map) event.get(i));
                beats.add(te);
            }
        }

        tatums = new ArrayList<TimedEvent>();
        event = (List) mq.getObject("tatums");
        if (event != null) {
            for (int i = 0; i < event.size(); i++) {
                TimedEvent te = new TimedEvent((Map) event.get(i));
                tatums.add(te);
            }
        }

        segments = new ArrayList<Segment>();
        if (event != null) {
            event = (List) mq.getObject("segments");
            for (int i = 0; i < event.size(); i++) {
                Segment te = new Segment((Map) event.get(i));
                segments.add(te);
            }
        }

        this.tempo=mq.getDouble("track.tempo");
        this.duration=mq.getDouble("track.duration");
    }

    public void timeStretch(double bpmFactor){
        this.tempo*=bpmFactor;
        this.duration/=bpmFactor;

        for (TimedEvent te:sections){
            te.duration/=bpmFactor;
            te.start/=bpmFactor;
        }
        for (TimedEvent te:segments){
            te.duration/=bpmFactor;
            te.start/=bpmFactor;
        }
        for (TimedEvent te:tatums){
            te.duration/=bpmFactor;
            te.start/=bpmFactor;
        }
        for (TimedEvent te:beats){
            te.duration/=bpmFactor;
            te.start/=bpmFactor;
        }
        for (TimedEvent te:bars){
            te.duration/=bpmFactor;
            te.start/=bpmFactor;
        }

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

    public double getTempo() {
        return tempo;
    }

    public void setTempo(double tempo) {
        this.tempo = tempo;
    }

    public double getDuration() {

        return duration;
    }


    public void setDuration(double duration) {
        this.duration = duration;
    }


}
