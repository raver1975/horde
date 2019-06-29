package com.myronmarston;

import com.myronmarston.music.AudioFileCreator;
import com.myronmarston.music.GermIsEmptyException;
import com.myronmarston.music.Instrument;
import com.myronmarston.music.NoteStringParseException;
import com.myronmarston.music.OutputManager;
import com.myronmarston.music.scales.InvalidKeySignatureException;
import com.myronmarston.music.settings.FractalPiece;
import com.myronmarston.synth.Output;
import com.myronmarston.synth.Sequencer;

import javax.sound.midi.MidiUnavailableException;
import java.io.IOException;

public class Main {

    public Main() {
        System.out.println("acid audio system starting");
        Output output = new Output();

        for (Sequencer s:output.getSequencer()) {
            s.setBpm(120d);
            s.randomizeSequence();
        }
//                BasslineSynthesizer synth = (BasslineSynthesizer) output.getTrack(0);
//                RhythmSynthesizer drums = (RhythmSynthesizer) output.getTrack(1);
//                output.getSequencer()[2].drums.randomize();
//                output.getSequencer().bassline1.randomize();
//                output.getSequencer().bass2.randomize();
//        output.setVolume(1d);
        output.start();
        System.out.println("acid audio system started");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        output.dispose();
        System.exit(0);
/*
        FractalPiece fp = new FractalPiece();
        fp.createDefaultSettings();
//        fp.setGermString("G4,1/4,MF A4,1/8,F B4,1/8,F G4,1/4,MF");
        fp.setGermString("C4 E4 F4 G4 Gbb3,1/6 Bx4,1/4 D#5 Fb4");

        String inst1 = Instrument.AVAILABLE_INSTRUMENTS.get((int) (Instrument.AVAILABLE_INSTRUMENTS.size() * Math.random()));
        String inst2 = Instrument.AVAILABLE_INSTRUMENTS.get((int) (Instrument.AVAILABLE_INSTRUMENTS.size() * Math.random()));
        String inst3 = Instrument.AVAILABLE_INSTRUMENTS.get((int) (Instrument.AVAILABLE_INSTRUMENTS.size() * Math.random()));
        System.out.println(inst1 + "\t" + inst2 + "\t" + inst3);

        fp.getVoices().get(0).setInstrumentName(inst1);
        fp.getVoices().get(1).setInstrumentName(inst2);
        fp.getVoices().get(2).setInstrumentName(inst3);

        int cnt = 0;
//for (Section s:fp.getSections()) {
//    System.out.println("playing section: "+s.toString());
        // this will throw an exception if it fails...
//    OutputManager om=s.createOutputManager();
        OutputManager om = fp.createPieceResultOutputManager();
        om.saveWavFile("test" + (cnt++) + ".wav");
        AudioFileCreator afc = new AudioFileCreator(om);
        //afc.playAudio();
//}*/
    }

    public static void main(String[] args) {
        new Main();
    }
}
