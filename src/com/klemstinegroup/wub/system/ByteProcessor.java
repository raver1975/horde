package com.klemstinegroup.wub.system;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class writes the ongoing sound to an output specified by the programmer
 */
public class ByteProcessor implements AudioProcessor {
    private final Handler handler;
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    AudioFormat format;

    /**
     * @param audioFormat which this processor is attached to
     */
    public ByteProcessor(Handler handler, TarsosDSPAudioFormat audioFormat) {
        this.format = JVMAudioInputStream.toAudioFormat(audioFormat);
        this.handler = handler;
        System.out.println(format.getFrameSize());
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        int byteOverlap = audioEvent.getOverlap() * format.getFrameSize();
        int byteStepSize = audioEvent.getBufferSize() * format.getFrameSize() - byteOverlap;
       // System.out.println(byteOverlap+"\t"+byteStepSize);
        output.write(audioEvent.getByteBuffer(), byteOverlap, byteStepSize);
        return true;
    }

    @Override
    public void processingFinished() {
        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        handler.handle(output.toByteArray());
    }
}