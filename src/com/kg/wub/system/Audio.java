package com.kg.wub.system;

import com.echonest.api.v4.Segment;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Node;

import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.*;

public class Audio {

    private Java2DFrameConverter converter;
    private Robot robot;
    public static FFmpegFrameRecorder recorder;
    public transient SourceDataLine line;
    public transient Queue<AudioInterval> queue;
    final AudioInterval[] lastPlayed = {null};
    public static HashSet<Integer> nodeset = new HashSet<>();
    public static LinkedList<EdgePair> edgemap = new LinkedList<>();
    final int[] idEdge = {0};
    transient int position = 0;
    transient AudioInterval currentlyPlaying;
    protected transient boolean breakPlay;
    public transient boolean pause = false;
    public transient boolean loop = false;

    public static final int resolution = 16;
    public static final int channels = 2;
    public static final int frameSize = channels * resolution / 8;
    public static final int sampleRate = 44100;
    public static final AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, resolution, channels, frameSize, sampleRate, false);
    public static final AudioFormat audioFormatMono = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, resolution, channels / 2, frameSize / 2, sampleRate, false);
    public static final int bufferSize = 8192;
    public static double maxDuration = 1;
    //    private Song cachedSong;
//    private int cachedSongIndex;
    public static ByteArrayOutputStream baos = new ByteArrayOutputStream();
    //    Node lastNode1 = null;
//    Node lastNode2 = null;
//    Node lastNode3 = null;
    private int start;
    //    private int lastSeg;
    private Queue<AudioInterval> lastPlayedQueue = new LinkedList<>();
    private int graphSize = 15;

    public Audio() {
        this(null, 1);
    }

    public Audio(Canvas ip, int numClusters) {

        queue = new LinkedList<AudioInterval>();
        startPlaying(ip, numClusters);
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        if (Settings.makeVideo) {
            recorder = new FFmpegFrameRecorder(new File("out" + Settings.spotifyId + ".mp4"), ip.getWidth(), ip.getHeight(), 2);
            recorder.setSampleRate((int) audioFormat.getSampleRate());
            recorder.setAudioChannels(2);
            recorder.setInterleaved(true);
            recorder.setVideoQuality(0);
//            recorder.setVideoBitrate(10000000);
//            recorder.setAudioBitrate(10000000);
            recorder.setImageWidth(ip.getWidth());
            recorder.setImageHeight(ip.getHeight());
            try {
                recorder.start();
                Thread.sleep(2000);
                System.out.println("****recorder started");
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            } catch (InterruptedException e) {

            }
            converter = new Java2DFrameConverter();
        }
    }

    ArrayList<Integer> tem = new ArrayList<>();


    public static HashMap<Integer, Color> randomColor = new HashMap<>();

    static {
        ArrayList<Integer> gill = new ArrayList<>();
        for (int i = 0; i < AudioParams.numClusters; i++) {
            gill.add(i);
        }

        for (int i = 0; i < AudioParams.numClusters; i++) {
            int pos = (int) (Math.random() * gill.size());
            double normd = ((double) pos / (double) AudioParams.numClusters) * 100d;
//            Test3.tf.setBackground(ColorHelper.numberToColor(normd));
            randomColor.put(i, ColorHelper.numberToColor(normd));
            gill.remove(new Integer(pos));
        }

    }

    private void startPlaying(Canvas tf, int numClusters) {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
//        HashMap<String, Integer> hm = new HashMap<>();
        line = getLine();

        new Thread(new Runnable() {
            public void run() {
                int cnt = 25000000;
                top:
                while (cnt-- > 0) {
                    if (!queue.isEmpty()) {
                        cnt = 25000000;
                        AudioInterval audioInterval = queue.poll();
                        currentlyPlaying = audioInterval;

                        if (tf != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {


                                    ArrayList<Segment> list = new ArrayList<>();
                                    Segment bbbb1 = audioInterval.te;
                                    list.add(bbbb1);
//

//                                        double seconds = 1;
//                                        for (Segment s : cachedSong.analysis.getSegments()) {
//                                            seconds = Math.max(seconds, s.getStart() + s.getDuration());
//                                        }
                                    double duration = 1;
                                    duration = audioInterval.te.duration;

                                    if (bbbb1.getStart() + bbbb1.getDuration() > maxDuration) {
                                        maxDuration = bbbb1.getDuration() + bbbb1.getStart();
                                    }

                                    BufferedImage bi = new SamplingGraph().createWaveForm(list, duration, audioInterval.data, audioFormat, tf.getWidth(), tf.getHeight());
                                    Graphics g = bi.getGraphics();


//                                        JSONObject js = (JSONObject) cachedSong.analysis.getMap().get("meta");
//                                        String title = null;
//                                        String artist = null;
//                                        String album = null;
//                                        String genre = null;
//
//
//                                        try {
//                                            title = (String) js.get("title");
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        try {
//                                            artist = (String) js.get("artist");
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        try {
//                                            album = (String) js.get("album");
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        try {
//                                            genre = (String) js.get("genre");
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                            try {
//                                                seconds = (Long) js.get("seconds");
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }

//                                            String sonTit = "Title: " + artist;
//                                            String sonArt = "Artist: " + title;
//                                        String sonSeq = "#" + i.payloadString.segment;

//                                        Segment seg = cachedSong.analysis.getSegments().get(i.payloadString.segment);
                                    lastPlayedQueue.add(audioInterval);
                                    LinkedList<AudioInterval> lastPlayedQueue1 = new LinkedList();
                                    lastPlayedQueue1.addAll(lastPlayedQueue);
                                    Iterator<AudioInterval> quit = lastPlayedQueue1.iterator();
                                    int cnt = 0;
                                    int qsize = 15;
                                    g.setFont(new Font("Arial", Font.BOLD, 20));
                                    while (quit != null && quit.hasNext()) {
                                        AudioInterval ff = null;
                                        ff = quit.next();
                                        Segment seg1 = null;
                                        if (ff != null) seg1 = ff.te;
                                        int zz = lastPlayedQueue.size();
                                        if (zz == 0) zz = 1;
                                        g.setColor(ColorHelper.numberToColor((cnt * 100) / zz));
                                        //System.out.println(seg1+"\t"+seg1.getDuration());
                                        if (seg1 != null) {
                                            int x = (int) ((bi.getWidth() * seg1.getStart()) / maxDuration) - cnt;
                                            int y = bi.getHeight() / 2 - (bi.getHeight() / 2) * cnt / qsize;
                                            int w = (int) ((bi.getWidth() * seg1.getDuration() * cnt) / maxDuration);
                                            int h = (bi.getHeight()) * cnt / qsize;
                                            g.fillRect(x, y, w, h);
                                        }
                                        cnt++;
                                    }
                                    while (lastPlayedQueue1.size() > qsize) {
                                        lastPlayedQueue1.removeFirst();
                                    }
                                    lastPlayedQueue = new LinkedList<AudioInterval>();
                                    lastPlayedQueue.addAll(lastPlayedQueue1);

//                                    g.setColor(Color.YELLOW);
//                                    for (int xi = -1; xi < 2; xi++) {
//                                        for (int yi = -1; yi < 2; yi++) {
//                                            g.drawString("#" + audioInterval.segment, 10 - xi, 25 + yi);
//                                        }
//                                    }
//                                    g.setColor(Color.RED);
//                                    g.drawString("#" + audioInterval.segment, 10, 25);

                                    Graphics gra = tf.getGraphics();
                                    if (gra != null) {
                                        gra.setColor(new Color(0, 0, 0));
                                        gra.clearRect(0, 0, tf.getWidth(), tf.getHeight());
                                        gra.drawImage(bi, 0, 0, null);
                                    }
                                    if (AudioParams.graph != null) {
                                        if (!BeautifulKMGSRandReducefromAudioObject.running) {
                                            if (audioInterval != null && AudioParams.graph != null) {
                                                try {
                                                    if (!nodeset.contains(audioInterval.hashCode())) {
                                                        Node n = AudioParams.graph.addNode(audioInterval.hashCode() + "");
                                                        n.setAttribute("label", audioInterval.segment);
                                                        n.addAttribute("ui.style", "fill-color: rgba(0,255,0,255);");
                                                        n.addAttribute("ui.style", "size: 25;");
                                                        nodeset.add(audioInterval.hashCode());
                                                    } else {
                                                        Node n = AudioParams.graph.getNode(audioInterval.hashCode() + "");
                                                        if (n != null) n.removeAttribute("ui.hide");
                                                    }

                                                } catch (Exception e) {
                                                }
                                            }

                                            if (lastPlayed[0] != null) {
                                                if (!nodeset.contains(lastPlayed[0].hashCode())) {
                                                    try {
                                                        Node n = AudioParams.graph.addNode(lastPlayed[0].hashCode() + "");
                                                        n.setAttribute("label", audioInterval.segment);
                                                        n.addAttribute("ui.style", "fill-color: rgba(0,255,0,255);");
                                                        n.addAttribute("ui.style", "size: 25;");
//
//                                        }
                                                        nodeset.add(lastPlayed[0].hashCode());
                                                    } catch (Exception e) {
                                                    }
                                                } else {
                                                    Node n = AudioParams.graph.getNode(lastPlayed[0].hashCode() + "");
                                                    if (n != null) n.removeAttribute("ui.hide");
                                                }
                                            }
                                            if (lastPlayed[0] != null && audioInterval != null) {
                                                EdgePair ep2 = new EdgePair(lastPlayed[0].hashCode(), audioInterval.hashCode());
                                                if (!edgemap.contains(ep2)) {
                                                    edgemap.add(ep2);
                                                    AudioInterval lp = lastPlayed[0];
                                                    AudioInterval ai = audioInterval;
//                                            new Thread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    try {
//                                                        Thread.sleep(200);
//                                                    } catch (InterruptedException e) {
//                                                        e.printStackTrace();
//                                                    }
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                Thread.sleep(100);
                                                                if (AudioParams.graph.getNode(lp.hashCode() + "") != null && AudioParams.graph.getNode(ai.hashCode() + "") != null) {
                                                                    Edge edge = AudioParams.graph.addEdge("" + (idEdge[0]++), lp.hashCode() + "", ai.hashCode() + "", true);
                                                                    if (edge != null) {
                                                                        if (edge.getNode0() != null) {
                                                                            Color hhcolor = ColorHelper.numberToColorPercentage((double) (edge.getNode0().getInDegree() + edge.getNode0().getOutDegree()) / 10d);
                                                                            edge.getNode0().addAttribute("ui.style", "fill-color: rgba(" + hhcolor.getRed() + "," + hhcolor.getGreen() + "," + hhcolor.getBlue() + ",127);");
                                                                            edge.getNode0().addAttribute("ui.style", "size: 25;");
                                                                        }
                                                                        if (edge.getNode1() != null) {
                                                                            Color hhcolor = ColorHelper.numberToColorPercentage((double) (edge.getNode1().getInDegree() + edge.getNode1().getOutDegree()) / 10d);
                                                                            edge.getNode1().addAttribute("ui.style", "fill-color: rgba(" + hhcolor.getRed() + "," + hhcolor.getGreen() + "," + hhcolor.getBlue() + ",127);");
                                                                            edge.getNode1().addAttribute("ui.style", "size: 25;");
                                                                        }
                                                                    }
                                                                }
                                                                Node n = AudioParams.graph.getNode(audioInterval.hashCode() + "");
                                                                if (n != null) {
                                                                    n.addAttribute("ui.style", "fill-color: rgba(127,127,127,127);");
                                                                    n.addAttribute("ui.style", "size: 25;");
                                                                }
                                                                if (lastPlayed[0] != null) {
                                                                    n = AudioParams.graph.getNode(lastPlayed[0].hashCode() + "");
                                                                    if (n != null) {
                                                                        Color hhcolor = ColorHelper.numberToColorPercentage((double) (n.getInDegree() + n.getOutDegree()) / 10d);
                                                                        n.addAttribute("ui.style", "fill-color: rgba(" + hhcolor.getRed() + "," + hhcolor.getGreen() + "," + hhcolor.getBlue() + ",127);");
                                                                        n.addAttribute("ui.style", "fill-color: rgba(255,0,0,255);");
                                                                        n.addAttribute("ui.style", "size: 35;");
                                                                    }
                                                                }

                                                            } catch (org.graphstream.graph.EdgeRejectedException e) {
                                                            } catch (ElementNotFoundException e) {
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }).start();

//                                                }
//                                            }).start();

                                                }
                                            }
                                        } else {
                                            if (audioInterval != null) {
                                                Node n = AudioParams.graph.getNode(audioInterval.hashCode() + "");
                                                if (n!=null) {
                                                    n.addAttribute("ui.style", "fill-color: rgba(255,0,0,255);");
                                                    n.addAttribute("ui.style", "size: 25;");

                                                    if (lastPlayed[0] != null) {
                                                        n = AudioParams.graph.getNode(lastPlayed[0].hashCode() + "");
                                                        n.addAttribute("ui.style", "fill-color: rgba(0,0,255,255);");
                                                        n.addAttribute("ui.style", "size: 10;");

                                                    }
                                                }
                                            }
                                        }
//                                    if (Aud
// ioParams.graph != null) {
//                                        Node node1 = AudioParams.graph.getNode(audioInterval.hashCode() + "");
//                                        if (node1 != null) {
//                                            node1.addAttribute("ui.style", "fill-color: rgba(127,127,127,128);");
//                                            node1.addAttribute("ui.style", "size:35;");
//                                        }
////                                        lastNode1 = node1;
//                                    }
                                        try {
                                            while (edgemap.size() > graphSize) {

                                                EdgePair ep = edgemap.removeFirst();
                                                Edge edge = AudioParams.graph.removeEdge(ep.s + "", ep.e + "");
                                                if (edge.getNode0() != null) {
                                                    Color hhcolor = ColorHelper.numberToColorPercentage((double) (edge.getNode0().getInDegree() + edge.getNode0().getOutDegree()) / 10d);
                                                    edge.getNode0().addAttribute("ui.style", "fill-color: rgba(" + hhcolor.getRed() + "," + hhcolor.getGreen() + "," + hhcolor.getBlue() + ",127);");
                                                    edge.getNode0().addAttribute("ui.style", "size: 25;");
                                                }
                                                if (edge.getNode1() != null) {
                                                    Color hhcolor = ColorHelper.numberToColorPercentage((double) (edge.getNode1().getInDegree() + edge.getNode1().getOutDegree()) / 10d);
                                                    edge.getNode1().addAttribute("ui.style", "fill-color: rgba(" + hhcolor.getRed() + "," + hhcolor.getGreen() + "," + hhcolor.getBlue() + ",127);");
                                                    edge.getNode1().addAttribute("ui.style", "size: 25;");
                                                }
//                                        if (lastNode1 != null) {
                                                try {
                                                    if (edge.getNode0().getInDegree() + edge.getNode0().getOutDegree() == 0) {
                                                        Node n = AudioParams.graph.getNode(edge.getNode0().getId());
//                                                    nodeset.remove((Integer) Integer.parseInt(n.getId()));
                                                        n.addAttribute("ui.hide");
                                                    }
                                                } catch (Exception e) {
                                                }
                                                try {
                                                    if (edge.getNode1().getInDegree() + edge.getNode1().getOutDegree() == 0) {
                                                        Node n = AudioParams.graph.getNode(edge.getNode1().getId());
//                                                    nodeset.remove((Integer) Integer.parseInt(n.getId()));
                                                        n.addAttribute("ui.hide");
                                                    }
                                                } catch (Exception e) {
                                                }
                                            }
//                                        for (int gg : nodeset) {
//                                            Node node1 = AudioParams.graph.getNode(gg + "");
//                                            if ((node1.getInDegree() == 0 && node1.getOutDegree() == 0) || (node1.hasEdgeFrom(node1) || node1.hasEdgeToward(node1))) {
//                                                new Thread(new Runnable() {
//                                                    @Override
//                                                    public void run() {
//                                                        try {
//                                                            Thread.sleep(5000);
//                                                        } catch (InterruptedException e) {
//                                                            e.printStackTrace();
//                                                        }
//                                                        try {
//                                                            nodeset.remove(Integer.parseInt(node1.getId()));
//                                                            AudioParams.graph.removeNode(node1);
//
//                                                        } catch (Exception e) {
//                                                        }
//                                                    }
//                                                }).start();
//                                            }
//                                        }


                                        } catch (Exception e) {
                                        }
                                        lastPlayed[0] = audioInterval;

                                    }
//                                System.out.println("hetre2");

                                }
                            }).

                                    start();
                        }


                        int j = 0;
                        for (j = 0; j <= audioInterval.data.length - bufferSize; j += bufferSize) {
                            while (pause || breakPlay) {
                                if (breakPlay) {
                                    breakPlay = false;
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    continue top;
                                }
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            position = j;
                            line.write(audioInterval.data, j, bufferSize);

                        }

                        if (j < audioInterval.data.length) {
                            position = j;
                            line.write(audioInterval.data, j, audioInterval.data.length - j);
                            // line.drain();
                        }
                        if (loop)
                            queue.add(audioInterval);
                    } else {
                        currentlyPlaying = null;
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                stop();
            }
        }).

                start();

    }

    public SourceDataLine getLine() {
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, Audio.audioFormat);
        try {
            res = (SourceDataLine) AudioSystem.getLine(info);
            res.open(Audio.audioFormat);
            res.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        return res;
    }

    public void play(AudioInterval i) {
        try {
            baos.write(i.data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        queue.add(i);
    }

    public static void stop() {
        if (Settings.makeVideo) {
            try {
                Audio.recorder.stop();
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    class EdgePair {
        int s;
        int e;

        EdgePair(int s, int e) {
            this.s = s;
            this.e = e;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof EdgePair)) return false;
            EdgePair ep = (EdgePair) o;
            return ep.s == s && ep.e == e;
        }
    }

}
