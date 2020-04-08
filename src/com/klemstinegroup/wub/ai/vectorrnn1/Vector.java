package com.klemstinegroup.wub.ai.vectorrnn1;

import com.echonest.api.v4.Segment;
import com.klemstinegroup.wub.system.Settings;
import weka.core.Attribute;
import weka.core.Instance;

class Vector {
    static final int attLength = 28;

//    char c;
    Segment s;

//    public Vector(char c) {
//        this.c = c;
//    }

    public Vector(Segment s){
        this.s=s;
    }

    public String toString() {
        return s + "\n";
    }

    @Override
    public boolean equals(Object v) {
        if (!(v instanceof Vector)) return false;
        Vector v1 = (Vector) v;
        return this.s == v1.s;
    }

    @Override
    public int hashCode() {
        return s.hashCode();
    }


    protected static Instance getInstance(Attribute[] attlist, Segment s) {

        int cnt = 0;
        Instance inst = new Instance(attLength);
        inst.setValue(attlist[cnt++], s.getDuration() * Settings.durationFactor);
        inst.setValue(attlist[cnt++], s.getLoudnessMax() * Settings.loudFactor);
        inst.setValue(attlist[cnt++], s.getLoudnessStart() * Settings.loudFactor);
        inst.setValue(attlist[cnt++], s.getLoudnessMaxTime() * Settings.loudFactor);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[0]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[1]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[2]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[3]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[4]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[5]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[6]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[7]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[8]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[9]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[10]);
        inst.setValue(attlist[cnt++], Settings.timbreFactor * s.getTimbre()[11]);
        inst.setValue(attlist[cnt++], s.getPitches()[0] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[1] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[2] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[3] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[4] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[5] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[6] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[7] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[8] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[9] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[10] * Settings.pitchFactor);
        inst.setValue(attlist[cnt++], s.getPitches()[11] * Settings.pitchFactor);
        return inst;
    }
}