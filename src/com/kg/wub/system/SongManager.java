package com.kg.wub.system;

import com.echonest.api.v4.Segment;
import weka.core.Attribute;
import weka.core.Instance;

import java.io.File;
import java.util.ArrayList;


//933  good bassy dubstep
//2441 Crissy Criss & Youngman - Kick Snare
//713 kathy's song
//2088 bassnectar mesmerizing the ultra
//1670 covenant like tears in rain
//2344 saw wave bassicles
//2532 la roux, in for the the kill
//593 bassnectar lights


/**
 * Created by Paul on 7/30/2016.
 */
public class SongManager {

    public static String directory;
    static {
        for (int i=0;i<26;i++) {
            String s=(char)('a'+i)+":\\wubdata\\";
            File f=new File(s);
            if (f.isDirectory()){
                directory=s;
                break;
            }
        }
        System.out.println("directory = "+directory);
    }

    private static final File[] list;

    static final int attLength = 28;
    public static Attribute[] attlist;

    static int playback = 713;

    static final int numClusters = 255;
    static final int songsToScan = 1305;

    static float pitchFactor = 2f;
    static float timbreFactor = 2f;
    static float loudFactor = 20f;
    static float durationFactor = 40f;


    static {
        File[] list1 = new File(directory).listFiles();
        ArrayList<File> al = new ArrayList<>();
        for (File f : list1) {
            if (f.getAbsolutePath().endsWith(".au")) al.add(f);
        }
        list = al.toArray(new File[]{});

    }

    public static Song getRandom(int i) {
        if (i < 0) return getRandom();
        if (i > -1 && i < list.length) {
            Song ret= LoadFromFile.loadSong(list[i]);
            ret.number=i;
            System.out.println(ret);
        return ret;
        }
        else return null;
    }

    public static int getSize() {
        return list.length;
    }

    protected static Instance getInstance(Attribute[] attlist, Segment s) {

        int cnt = 0;
        Instance inst = new Instance(attLength);
        inst.setValue(attlist[cnt++], s.getDuration() * durationFactor);
        inst.setValue(attlist[cnt++], s.getLoudnessMax() * loudFactor);
        inst.setValue(attlist[cnt++], s.getLoudnessStart() * loudFactor);
        inst.setValue(attlist[cnt++], s.getLoudnessMaxTime() * loudFactor);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[0]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[1]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[2]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[3]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[4]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[5]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[6]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[7]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[8]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[9]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[10]);
        inst.setValue(attlist[cnt++], timbreFactor * s.getTimbre()[11]);
        inst.setValue(attlist[cnt++], s.getPitches()[0] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[1] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[2] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[3] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[4] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[5] * pitchFactor);
        inst.setValue(attlist[cnt++],
                s.getPitches()[6] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[7] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[8] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[9] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[10] * pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[11] * pitchFactor);
        return inst;
    }

    protected static double distance(Instance i1, Instance i2) {
        double tot = 0;
        for (int i = 0; i < attLength; i++) {
            double ta = i1.value(attlist[i]) - i2.value(attlist[i]);
            ta = Math.abs(ta);
            tot += ta;
        }
        return tot;
    }

    public static Song getRandom() {
        int sel = (int) (list.length * Math.random());
        return getRandom(sel);
    }
}
