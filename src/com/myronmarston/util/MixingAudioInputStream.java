package com.myronmarston.util;/*
 *	MixingAudioInputStream.java
 *
 *	This file is part of jsresources.org
 *
 *	This code follows an idea of Paul Sorenson.
 */

/*
 * Copyright (c) 1999 - 2001 by Matthias Pfisterer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 |<---            this code is formatted to fit into 80 columns             --->|
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/*
 * This is a class of Tritonus. It's not one of the best ideas to use it here.
 * However, we really don't want to reimplement its functionality here.
 * You need to have tritonus_share.jar in the classpath.
 * Get it from http://www.tritonus.org .
 */
import com.kg.TheHorde;
import com.kg.synth.Output;
import edu.uci.ics.jung.algorithms.scoring.util.ScoringUtils;
import org.tritonus.share.sampled.TConversionTool;

/**
 * Mixing of multiple AudioInputStreams to one AudioInputStream. This class
 * takes a collection of AudioInputStreams and mixes them together. Being a
 * subclass of AudioInputStream itself, reading from instances of this class
 * behaves as if the mixdown result of the input streams is read.
 *
 * @author Matthias Pfisterer
 */
public class MixingAudioInputStream
        extends AudioInputStream {

    private static final boolean DEBUG = false;

    private List m_audioInputStreamList;
    public int activeInputStreams;

    public MixingAudioInputStream(AudioFormat audioFormat, Collection audioInputStreams) {
        super(new ByteArrayInputStream(new byte[0]),
                audioFormat,
                AudioSystem.NOT_SPECIFIED);
        if (DEBUG) {
            out("MixingAudioInputStream.<init>(): begin");
        }
        m_audioInputStreamList = new ArrayList(audioInputStreams);
        if (DEBUG) {
            out("MixingAudioInputStream.<init>(): stream list:");
            for (int i = 0; i < m_audioInputStreamList.size(); i++) {
                out("  " + m_audioInputStreamList.get(i));
            }
        }
        if (DEBUG) {
            out("MixingAudioInputStream.<init>(): end");
        }
    }


//    public int read()
//            throws IOException {
//        if (DEBUG) {
//            out("MixingAudioInputStream.read(): begin");
//        }
//        int nSample = 0;
//        Iterator streamIterator = m_audioInputStreamList.iterator();
//        while (streamIterator.hasNext()) {
//            AudioInputStream stream = (AudioInputStream) streamIterator.next();
//            int nByte = stream.read();
//            if (nByte == -1) {
//                /*
//                 The end of this stream has been signaled.
//                 We remove the stream from our list.
//                 */
//                streamIterator.remove();
//                continue;
//            } else {
//                /*
//                 what about signed/unsigned?
//                 */
//                nSample += nByte;
//            }
//        }
//        if (DEBUG) {
//            out("MixingAudioInputStream.read(): end");
//        }
//        return (byte) ((nSample) & 0xFF);
//    }

    //    public void read(byte[] buffer1, byte[] buffer2) {
//        try {
//            read(buffer1);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        for (int i = 0; i < buffer1.length; i++) {
//            buffer1[i] = (byte) (((buffer1[i] + buffer2[i])/2) & 0xFF);
//        }
//    }
    @Override
    public int read(byte[] abData, int nOffset, int nLength)
            throws IOException {
        if (DEBUG) {
            out("MixingAudioInputStream.read(byte[], int, int): begin");
            out("MixingAudioInputStream.read(byte[], int, int): requested length: " + nLength);
        }
        int nChannels = getFormat().getChannels();
        int nFrameSize = getFormat().getFrameSize();
        /*
         This value is in bytes. Note that it is the storage size.
         It may be four bytes for 24 bit samples.
         */
        int nSampleSize = nFrameSize / nChannels;
        boolean bBigEndian = getFormat().isBigEndian();
        AudioFormat.Encoding encoding = getFormat().getEncoding();
        if (DEBUG) {
            out("MixingAudioInputStream.read(byte[], int, int): channels: " + nChannels);
            out("MixingAudioInputStream.read(byte[], int, int): frame size: " + nFrameSize);
            out("MixingAudioInputStream.read(byte[], int, int): sample size (bytes, storage size): " + nSampleSize);
            out("MixingAudioInputStream.read(byte[], int, int): big endian: " + bBigEndian);
            out("MixingAudioInputStream.read(byte[], int, int): encoding: " + encoding);
        }
        byte[] abBuffer = new byte[nFrameSize];
        long[] anMixedSamples = new long[nChannels];
        for (int nFrameBoundry = 0; nFrameBoundry < nLength; nFrameBoundry += nFrameSize) {
            if (DEBUG) {
                out("MixingAudioInputStream.read(byte[], int, int): frame boundry: " + nFrameBoundry);
            }
            for (int i = 0; i < nChannels; i++) {
                anMixedSamples[i] = 0;
            }
            int cnt = 0;
            Iterator streamIterator = m_audioInputStreamList.iterator();
            while (cnt++ <= activeInputStreams && streamIterator.hasNext()) {
                InputStream stream = (InputStream) streamIterator.next();
                if ((cnt - 1) < Output.PARTS - 4) {
                    if (TheHorde.output.getSequencers()[cnt - 1].getVolume() == 0) {
                        continue;
                    }
                }
//                for (int ii=0;ii<Output.PARTS;ii++){
//                    System.out.println(ii+"\t"+TheHorde.output.getSequencers()[ii].getClass());
//                }
                if (DEBUG) {
                    out("MixingAudioInputStream.read(byte[], int, int): AudioInputStream: " + stream);
                }
                int nBytesRead = stream.read(abBuffer, 0, nFrameSize);
                if (DEBUG) {
                    out("MixingAudioInputStream.read(byte[], int, int): bytes read: " + nBytesRead);
                }
                /*
                 TODO: we have to handle incomplete reads.
                 */
                if (nBytesRead == -1) {
                    /*
                     The end of the current stream has been signaled.
                     We remove it from the list of streams.
                     */
                    streamIterator.remove();
                    continue;
                }
                for (int nChannel = 0; nChannel < nChannels; nChannel++) {
                    int nBufferOffset = nChannel * nSampleSize;
                    int nSampleToAdd = 0;

                    if (encoding.equals(AudioFormat.Encoding.PCM_SIGNED)) {
                        switch (nSampleSize) {
                            case 1:
                                nSampleToAdd = abBuffer[nBufferOffset];
                                break;
                            case 2:
                                nSampleToAdd = TConversionTool.bytesToInt16(abBuffer, nBufferOffset, bBigEndian);
                                break;
                            case 3:
                                nSampleToAdd = TConversionTool.bytesToInt24(abBuffer, nBufferOffset, bBigEndian);
                                break;
                            case 4:
                                nSampleToAdd = TConversionTool.bytesToInt32(abBuffer, nBufferOffset, bBigEndian);
                                break;
                        }
                    } // TODO: pcm unsigned
                    else if (encoding.equals(AudioFormat.Encoding.ALAW)) {
                        nSampleToAdd = TConversionTool.alaw2linear(abBuffer[nBufferOffset]);
                    } else if (encoding.equals(AudioFormat.Encoding.ULAW)) {
                        nSampleToAdd = TConversionTool.ulaw2linear(abBuffer[nBufferOffset]);
                    }
                    anMixedSamples[nChannel] += nSampleToAdd;


                } // loop over channels
            } // loop over streams
            if (DEBUG) {
                out("MixingAudioInputStream.read(byte[], int, int): starting to write to buffer passed by caller");
            }
//            for (int i = 0; i < buffer2.length; i += frameSize) {
//                for (int nChannel = 0; nChannel < nChannels; nChannel++) {
//                    int nBufferOffset = nChannel * nSampleSize;
//                    int nSampleToAdd = 0;
//                    if (encoding.equals(AudioFormat.Encoding.PCM_SIGNED)) {
//                        switch (nSampleSize) {
//                            case 1:
//                                nSampleToAdd = buffer2[nBufferOffset+i];
//                                break;
//                            case 2:
//                                nSampleToAdd = TConversionTool.bytesToInt16(buffer2, nBufferOffset+i, bBigEndian);
//                                break;
//                            case 3:
//                                nSampleToAdd = TConversionTool.bytesToInt24(buffer2, nBufferOffset+i, bBigEndian);
//                                break;
//                            case 4:
//                                nSampleToAdd = TConversionTool.bytesToInt32(buffer2, nBufferOffset+i, bBigEndian);
//                                break;
//                        }
//                    } // TODO: pcm unsigned
//                    else if (encoding.equals(AudioFormat.Encoding.ALAW)) {
//                        nSampleToAdd = TConversionTool.alaw2linear(buffer2[nBufferOffset+i]);
//                    } else if (encoding.equals(AudioFormat.Encoding.ULAW)) {
//                        nSampleToAdd = TConversionTool.ulaw2linear(buffer2[nBufferOffset+i]);
//                    }
//                    anMixedSamples[nChannel] += nSampleToAdd;
//                } // loop over channels
//            }

            for (int nChannel = 0; nChannel < nChannels; nChannel++) {
                if (DEBUG) {
                    out("MixingAudioInputStream.read(byte[], int, int): channel: " + nChannel);
                }
                int nBufferOffset = nOffset + nFrameBoundry /* * nFrameSize*/ + nChannel * nSampleSize;
                if (DEBUG) {
                    out("MixingAudioInputStream.read(byte[], int, int): buffer offset: " + nBufferOffset);
                }
//                System.out.println("encode:"+encoding+"\t"+nSampleSize);
                if (encoding.equals(AudioFormat.Encoding.PCM_SIGNED)) {
                    switch (nSampleSize) {
                        case 1:
                            abData[nBufferOffset] = (byte) clamp(anMixedSamples[nChannel],Integer.MIN_VALUE,Integer.MAX_VALUE);
                            break;
                        case 2:
                            TConversionTool.intToBytes16((int) clamp(anMixedSamples[nChannel],Short.MIN_VALUE,Short.MAX_VALUE), abData, nBufferOffset, bBigEndian);
                            break;
                        case 3:
                            TConversionTool.intToBytes24((int) clamp(anMixedSamples[nChannel],Integer.MIN_VALUE,Integer.MAX_VALUE), abData, nBufferOffset, bBigEndian);
                            break;
                        case 4:
                            TConversionTool.intToBytes32((int) clamp(anMixedSamples[nChannel],Long.MIN_VALUE,Long.MAX_VALUE), abData, nBufferOffset, bBigEndian);
                            break;
                    }
                } // TODO: pcm unsigned
                else if (encoding.equals(AudioFormat.Encoding.ALAW)) {
                    abData[nBufferOffset] = TConversionTool.linear2alaw((short) clamp(anMixedSamples[nChannel],Integer.MIN_VALUE,Integer.MAX_VALUE));
                } else if (encoding.equals(AudioFormat.Encoding.ULAW)) {
                    abData[nBufferOffset] = TConversionTool.linear2ulaw((int) clamp(anMixedSamples[nChannel],Integer.MIN_VALUE,Integer.MAX_VALUE));
                }

            } // (final) loop over channels
        } // loop over frames
        if (DEBUG) {
            out("MixingAudioInputStream.read(byte[], int, int): end");
        }
        // TODO: return a useful value
        return nLength;
    }
    public static long clamp(long val, long min, long max) {
        return Math.max(min, Math.min(max, val));
    }
    /**
     * calls skip() on all input streams. There is no way to assure that the
     * number of bytes really skipped is the same for all input streams. Due to
     * that, this method always returns the passed value. In other words: the
     * return value is useless (better ideas appreciated).
     */
    public long skip(long lLength)
            throws IOException {
        Iterator streamIterator = m_audioInputStreamList.iterator();
        while (streamIterator.hasNext()) {
            InputStream stream = (InputStream) streamIterator.next();
            stream.skip(lLength);
        }
        return lLength;
    }

    /**
     * The minimum of available() of all input stream is calculated and
     * returned.
     */
    public int available()
            throws IOException {
        int nAvailable = 0;
        Iterator streamIterator = m_audioInputStreamList.iterator();
        while (streamIterator.hasNext()) {
            InputStream stream = (InputStream) streamIterator.next();
            nAvailable = Math.min(nAvailable, stream.available());
        }
        return nAvailable;
    }

    public void close()
            throws IOException {
        // TODO: should we close all streams in the list?
    }

    /**
     * Calls mark() on all input streams.
     */
    public void mark(int nReadLimit) {
        Iterator streamIterator = m_audioInputStreamList.iterator();
        while (streamIterator.hasNext()) {
            InputStream stream = (InputStream) streamIterator.next();
            stream.mark(nReadLimit);
        }
    }

    /**
     * Calls reset() on all input streams.
     */
    public void reset()
            throws IOException {
        Iterator streamIterator = m_audioInputStreamList.iterator();
        while (streamIterator.hasNext()) {
            InputStream stream = (InputStream) streamIterator.next();
            stream.reset();
        }
    }

    /**
     * returns true if all input stream return true for markSupported().
     */
    public boolean markSupported() {
        Iterator streamIterator = m_audioInputStreamList.iterator();
        while (streamIterator.hasNext()) {
            InputStream stream = (InputStream) streamIterator.next();
            if (!stream.markSupported()) {
                return false;
            }
        }
        return true;
    }

    private static void out(String strMessage) {
        System.out.println(strMessage);
    }
}


/*** MixingAudioInputStream.java ***/