package com.klemstinegroup.wub.ai.custom;

import com.echonest.api.v4.Segment;
import com.klemstinegroup.wub.ai.vectorrnn.RNNDemo;
import com.klemstinegroup.wub.system.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import weka.clusterers.SimpleKMeans;
import weka.core.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Paul on 4/4/2017.
 */
public class Custom implements KeyListener {

    static final int attLength = 28;
    private final Audio audio;
    public Attribute[] attlist;
    int width = 1200;
    int height = 400;
    int numClusters = 200;
    boolean generateGraph=false;

    public Custom() {
        this(SongManager.getRandom((int) (Math.random() * 1300)));

    }

    public Custom(Song song) {
        Canvas tf = new Canvas();
        tf.setBackground(new Color(255, 255, 255));
        tf.setSize(width, height);
//        JTextArea jta = new JTextArea(4, 20);
        JFrame jframe = new JFrame("Wub");
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setSize(width, height * 2);
        jframe.setResizable(true);

        jframe.setVisible(true);
        audio = new Audio(tf, numClusters);
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (generateGraph) {
                    AudioParams.graph = new SingleGraph("id");
                    AudioParams.graph.addAttribute("ui.quality");
                    AudioParams.graph.addAttribute("ui.antialias");
                    Viewer viewer = new Viewer(AudioParams.graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
                    SpringBox sb = new SpringBox();
                    sb.setForce(1f);
                    sb.setQuality(1f);
                    sb.setGravityFactor(.1f);
                    viewer.enableAutoLayout(sb);
                    ViewPanel vp = viewer.addDefaultView(false);
                    vp.setSize(width, height);
                    jframe.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent windowEvent) {
                            Audio.stop();

                        }
                    });

                    jframe.getContentPane().add("North", tf);
                    jframe.getContentPane().add("Center", vp);
//        tf.setMinimumSize(new Dimension(100, 100));
//        tf.setPreferredSize(new Dimension(100, 100));



                    vp.addKeyListener(Custom.this);
                }
                else{
                    jframe.getContentPane().add("Center", tf);
                }
                jframe.invalidate();
                tf.addKeyListener(Custom.this);
                jframe.addKeyListener(Custom.this);
            }
        }).start();


        List<Segment> segments = song.analysis.getSegments();


        HashMap<AudioInterval, AudioInterval> smallercluster = makeMap(numClusters, song);

        ArrayList<AudioInterval> reducedSong = new ArrayList<>();
        HashMap<AudioInterval, Character> language = new HashMap<>();


        char newChar = 'A';
        int i = 0;
        double lleng = segments.size();
        for (Segment s : segments) {
            if (i < lleng / 8d || i > (7d * lleng) / 8d) {
                i++;
                continue;
            }

            AudioInterval segOrig = song.getAudioIntervalForSegment(i);
            AudioInterval segMapped = smallercluster.get(segOrig);
            reducedSong.add(segMapped);
            if (!language.containsKey(segMapped)) {
                language.put(segMapped, newChar);
                newChar++;
            }
            i++;
        }
        System.out.println((newChar - 65) + " characters in language");
        String forRnn = "";
        for (AudioInterval s : reducedSong) {
            forRnn += language.get(s);
        }
        String out = "";
        for (int kk = 0; kk < 10; kk++) out += forRnn;
//        System.out.println(forRnn);
        String finalOut = out;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] samples = RNNDemo.process(smallercluster,song,language, audio, finalOut);
                    System.out.println(Arrays.toString(samples));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


//        HashMap<AudioInterval, AudioInterval> mapr = makeMap(numClusters, song, true);
//        HashMap<AudioInterval, Integer> count = new HashMap<>();
//        for (Segment s : segments) {
//            AudioInterval segOrig = new AudioInterval(song.number, i);
//            AudioInterval segMapped = map1.get(segOrig);
//            Segment sem=segments.get(segMapped.segment);
//            audio.play(song.getAudioInterval(sem,segMapped));
//
////            System.out.println(segOrig + " maps to " + segMapped);
//            if (count.containsKey(segMapped)) count.put(segMapped, count.get(segMapped) + 1);
//            else count.put(segMapped, 1);
//            i++;
//        }
//        i = 0;
//        for (Segment s : segments) {
//            System.out.println(i + "\t" + count.get(map1.get(new AudioInterval(song.number, i))));
//            i++;
//        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                jframe.setSize(jframe.getWidth() - 1, jframe.getHeight() - 1);
            }
        }).start();
    }

    private HashMap<AudioInterval, AudioInterval> makeMap(int numClusters, Song song) {
        List<Segment> segs = song.analysis.getSegments();
        List<AudioInterval> aulist = new ArrayList<>();
        int cnt = 0;
        for (Segment s : segs) {
            aulist.add(song.getAudioIntervalForSegment(cnt++));
        }
        return makeMap(numClusters, song, aulist);
    }

    private HashMap<AudioInterval, AudioInterval> makeMap(int numClusters, Song song, List<AudioInterval> segments) {

        //one time attribute setup
        FastVector attrs = new FastVector();
        attlist = new Attribute[attLength];
        for (int io = 0; io < attLength; io++) {
            attlist[io] = new Attribute("at" + io);
            attrs.addElement(attlist[io]);
        }

        //kmeans setup
        SimpleKMeans kmeans = new SimpleKMeans();

        try {
            String[] options = Utils.splitOptions("-I 100");
            kmeans.setNumClusters(numClusters);
            kmeans.setDistanceFunction(new ManhattanDistance());
            kmeans.setOptions(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<AudioInterval> coll = new ArrayList<>();
        Instances dataset = new Instances("my_dataset", attrs, 0);
        AudioInterval[] lastSeen = new AudioInterval[numClusters];

        int cnt = 0;
        for (AudioInterval s : segments) {
            Instance inst = getInstance(attlist, s);
            coll.add(s);
            inst.setDataset(dataset);
            dataset.add(inst);
        }


        long time = System.currentTimeMillis();
        System.out.println("building cluster " + numClusters);
        try {
            kmeans.buildClusterer(dataset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("cluster found in " + ((System.currentTimeMillis() - time)) + " ms");

        // print out the cluster centroids
        Instances centroids = kmeans.getClusterCentroids();
        for (int io = 0; io < centroids.numInstances(); io++) {
            double dist = Float.MAX_VALUE;
            int best = -1;
            for (int j = 0; j < dataset.numInstances(); j++) {
                double dd = distance(centroids.instance(io), dataset.instance(j));
//                System.out.println("dist="+dd);
                if (dd < dist) {
                    dist = dd;
                    best = j;
                }
            }
            AudioInterval gg = coll.get(best);
            lastSeen[io] = gg;
//            System.out.println("centroid io " + io + "\t" + gg);


        }
        // get cluster membership for each instance
        HashMap<AudioInterval, AudioInterval> map = new HashMap<>();
        for (int io = 0; io < dataset.numInstances(); io++) {
            try {
                int cluster = kmeans.clusterInstance(dataset.instance(io));
                AudioInterval tempAudioInterval = lastSeen[cluster];
                map.put(coll.get(io), tempAudioInterval);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        ObjectManager.write(map, "map-universal.ser");
        return map;
    }

    protected double distance(Instance i1, Instance i2) {
        double tot = 0;
        for (int i = 0; i < attLength; i++) {
            double ta = i1.value(attlist[i]) - i2.value(attlist[i]);
            ta = Math.abs(ta);
            tot += ta;
        }
        return tot;
    }

    protected static Instance getInstance(Attribute[] attlist, AudioInterval ai) {

        Segment s = ai.te;
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


    public static void main(String[] args) {
        new Custom();
    }

    public static void performMagic(Song song) {
        new Custom(song);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
//        if (e.getKeyChar() == 'c') {
//            AudioParams.graph.clear();
//            Audio.nodeset.clear();
//            Audio.edgemap.clear();
//        }
        if (e.getKeyChar() == ' ') {
            audio.pause = !audio.pause;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
