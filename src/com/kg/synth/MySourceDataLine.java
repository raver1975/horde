package com.kg.synth;

import javax.sound.sampled.SourceDataLine;

public class MySourceDataLine {

    private final SourceDataLine sdl;
    static final int writeBuffers=2;
    private byte[][] buffers = new byte[writeBuffers][Output.BUFFER_SIZE];
    private long cnt = 0;

    public MySourceDataLine(SourceDataLine sdl) {
        this.sdl=sdl;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    sdl.write(buffers[(int) (cnt % writeBuffers)], 0, Output.BUFFER_SIZE);
                    cnt++;
                }
            }
        }).start();
    }

    public void waitFor(byte[] b) {
        long c=cnt;
        System.arraycopy(b, 0, buffers[(int) ((cnt + 1) % writeBuffers)], 0, Output.BUFFER_SIZE);
        while (c==cnt) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
