    package com.kg.wub;

import com.echonest.api.v4.Segment;
import com.kg.wub.system.*;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.json.simple.JSONObject;
import weka.clusterers.SimpleKMeans;
import weka.core.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.*;

    public class BeautifulKMGSRandReduce {


        //    static String directory = "e:\\wub\\";
        private static final File[] list;

        static final int attLength = 28;
        public static Attribute[] attlist;


        public static int[] playback = new int[]{(int) (Math.random() * 1300)};
    //    static int[] playback = new int[]{181};

    //    static int[] playback = new int[]{(int)(Math.random()*1300),(int)(Math.random()*1300),(int)(Math.random()*1300)};

        //    public static final float segmentsKept = .75f;
    //    public static int newSongLength = 2500;

    //    public static boolean makeVideo = false;
        //    public static boolean exitonSongexit = true;
    //    private static boolean addTrackInfo = true;
        //public static int numClusters = -1;


    //    static int playbackStart = playback;
    //    static int playbackEnd = playback + stretch;


    //    static float pitchFactor = 17f;
    //    static float timbreFactor = 17f;
    //    static float loudFactor = 70f;
    //    static float durationFactor = 90f;

        public static Canvas tf;
        private static AudioInterval firstSaved = null;
        private static int width = 1200;
        private static int height = 800;

    //    public static HashMap<String,Integer> hm;


        static {
    //        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            File[] list1 = new File(SongManager.directory).listFiles();
            ArrayList<File> al = new ArrayList<>();
            for (File f : list1) {
                if (f.getAbsolutePath().endsWith(".au")) al.add(f);
            }
            list = al.toArray(new File[]{});

        }


        public static void main(String[] args) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    int totsegm = 0;
                    JTextArea jta = new JTextArea(4, 20);
                    JFrame jframe = new JFrame("Wub");
                    jframe.setSize(width, height);
                    jframe.setResizable(false);
                    if (Settings.makeVideo) {
                        jframe.setAlwaysOnTop(true);
                    }

                    Song song1 = null;
                    for (int v : playback) {
                        song1 = SongManager.getRandom(v);
//                        JSONObject js = (JSONObject) song1.analysis.getMap().get("meta");
//                        String title = null;
//                        String artist = null;
//                        String album = null;
//                        String genre = null;
//                        Long seconds = null;
//
//                        try {
//                            title = (String) js.get("title");
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        try {
//                            artist = (String) js.get("artist");
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        try {
//                            album = (String) js.get("album");
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        try {
//                            genre = (String) js.get("genre");
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        try {
//                            seconds = (Long) js.get("seconds");
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        if (seconds == null || seconds == 0) seconds = new Long(-61);
//

                        int segm = song1.analysis.getSegments().size();
                        totsegm += segm;
                        // float scale = (int) (((float) numClusters / (float) song1.analysis.getSegments().size()) * 1000) / 10f;
                        System.out.println("------------------------------");
                        System.out.println("segment #" + v);
                        System.out.println("size = " + segm);
//                        System.out.println("title\t" + title);
//                        System.out.println("artist\t" + artist);
//                        System.out.println("album\t" + album);
//                        System.out.println("genre\t" + genre);
//                        String secs = seconds % 60 + "";
//                        while (secs.length() < 2) secs = "0" + secs;
//                        System.out.println("time\t" + seconds / 60 + ":" + secs);
//                        jta.append("song #\t" + v);
//                        jta.append("\n");
//                        jta.append("Title\t" + title);
//                        jta.append("\n");
//                        jta.append("Artist\t" + artist);
//                        jta.append("\n");
//                        jta.append("Album\t" + album);
//                        jta.append("\n");
//                        jta.append("Genre\t" + genre);
//                        jta.append("\n");
//                        jta.append("Time\t" + seconds / 60 + ": " + secs);
                        jta.append("\n");
                        jta.append("------------------------------");
                        jta.append("\n");
                    }
                    System.out.println("total segments=" + totsegm);
                    System.out.println(" path clusters=" + (totsegm - Settings.decreaseClustersBy));
                    System.out.println(" kept clusters=" + ((int) (totsegm * Settings.segmentsKept)));

    //        frame.setSize(400, 300);
                    tf = new Canvas();
                    tf.setFont(new Font("Arial", Font.BOLD, 300));
                    tf.setBackground(Color.BLACK);
    //        JScrollPane jscr = new JScrollPane(tf);

    //        DefaultCaret caret = (DefaultCaret) tf.getCaret();
    //        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    //        frame.add(jscr);
    //        frame.setVisible(true);


                    HashMap<AudioInterval, AudioInterval> map1 = makeMap(totsegm - Settings.decreaseClustersBy);
                    HashMap<AudioInterval, AudioInterval> map2 = makeMap((int) (totsegm * Settings.segmentsKept));

                    Audio audio = new Audio( tf, (totsegm - Settings.decreaseClustersBy));


                    AudioParams.graph = new SingleGraph("id");
                    AudioParams.graph.addAttribute("ui.quality");
                    AudioParams.graph.addAttribute("ui.antialias");
                    Viewer viewer = new Viewer(AudioParams.graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
                    SpringBox sb = new SpringBox();
                    sb.setForce(1.5f);
    //        sb.setQuality(0);
                    viewer.enableAutoLayout(sb);
                    View view = viewer.addDefaultView(false);
                    final AudioInterval[] startNode = {song1.getAudioIntervalForSegment(0)};

    //        for (int cnt=0;cnt<5000;cnt++){
    //            graph.addNode(cnt+"");
    //        }
                    HashMap<Integer, AudioInterval> nodes = new HashMap<>();
                    HashSet<Integer> nodeset = new HashSet<>();
    //                final Song[] tempSong = {null};
    //                final int[] lastSong = {-1};
    //        HashSet<String> edges = new HashSet<>();
                    int cnt2 = 0;
                    for (int songToPlay : playback) {
                        Song song = SongManager.getRandom(songToPlay);

                        for (int cnt = 0; cnt < song.analysis.getSegments().size(); cnt++) {
                            AudioInterval pp = song.getAudioIntervalForSegment(cnt);
                            AudioInterval play = map1.get(pp);
    //            }
                            if (play == null) {
                                System.out.println("null! " + pp);
                                continue;
                            }

                            if (!nodeset.contains(startNode[0].hashCode())) {
                                Node n = AudioParams.graph.addNode(startNode[0].hashCode() + "");
                                nodeset.add(startNode[0].hashCode());
                            }
                            if (!nodeset.contains(play.hashCode())) {
                                AudioParams.graph.addNode(play.hashCode() + "");
                                nodeset.add(play.hashCode());
                            }
                            AudioParams.graph.addEdge((cnt2) + "", startNode[0].hashCode() + "", play.hashCode() + "", true);
                            if (nodes.isEmpty()) firstSaved = play;
                            nodes.put(play.hashCode(), play);
                            startNode[0] = play;
                            cnt2++;
                        }
                        AudioParams.graph.addEdge((cnt2++) + "", startNode[0].hashCode() + "", firstSaved.hashCode() + "", true);
                    }


                    jframe.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent windowEvent) {
                            Audio.stop();

                        }
                    });

                    JPanel panel = new JPanel();
                    panel.setLayout(new BorderLayout());
                    jframe.add(panel);


                    panel.add("Center", viewer.getDefaultView());
                    tf.setMinimumSize(new Dimension(100, 100));
                    tf.setPreferredSize(new Dimension(100, 100));
                    panel.add("North", tf);
                    if (Settings.addTrackInfo) panel.add("West", jta);
                    jframe.setVisible(true);
                    for (int i = 0; i < AudioParams.graph.getNodeCount(); i++) {
                        AudioParams.graph.getNode(i).setAttribute("x", Math.random() * width);
                        AudioParams.graph.getNode(i).setAttribute("y", Math.random() * height);
                    }

                    HashMap<String, Integer> hm = new HashMap<>();

                    startNode[0] = song1.getAudioIntervalForSegment(0);

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(Settings.makeVideo ? (45 * 60 * 1000) : (5 * 60 * 1000));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Audio.stop();
                            jframe.dispose();
                        }
                    }).start();

                    int cnn = Settings.lengthOfBeaut;
                    while (cnn-- > 0) {
                        AudioInterval trans = map2.get(startNode[0]);
    //                    System.out.println(trans);
    //                    AudioInterval ai2 = tempSong[0].getAudioInterval(tempSong[0].analysis.getSegments().get(trans.segment), tempSong[0].number, trans.segment);
    //                    ai2.payloadPrintout = new AudioInterval(startNode[0].song, startNode[0].segment);
    //                    ai2.payloadPrintout = new AudioInterval(trans.song, trans.segment);

    //                    AudioInterval ai = tempSong[0].getAudioInterval(tempSong[0].analysis.getSegments().get(startNode[0].segment));
    //                    ai.payloadPrintout = new AudioInterval(trans.song, trans.segment);

    //                   AudioInterval ais2=map.get(ai.payloadPrintout);
    //                    AudioInterval ai2=  tempSong[0].getAudioInterval(tempSong[0].analysis.getSegments().get(ais2.segment));

                        if (trans != null) audio.play(trans);

                        Iterator<Edge> adj = AudioParams.graph.getNode(startNode[0].hashCode() + "").getEachLeavingEdge().iterator();
                        ArrayList<Edge> temp = new ArrayList<>();
                        int lowest = 0;
                        int lowestValue = Integer.MAX_VALUE;
    //            int cnt = 0;
                        while (adj.hasNext()) {
                            Edge bb = adj.next();
                            temp.add(bb);
                        }
                        int cnt1 = 0;
                        Collections.shuffle(temp);
                        for (Edge bb : temp) {
                            String key = bb.getNode1().getId();
                            if (!hm.containsKey(key)) {
                                hm.put(key, 0);
                            }
                            int val = hm.get(key);
                            if (val < lowestValue) {
                                lowestValue = val;
                                lowest = cnt1;

                            }
                            cnt1++;
                        }


    //            int next = (int) (Math.random() * temp.size());
                        int next = lowest;

    //            System.out.println("going down: " + next + " out of " + temp.size());
                        if (temp.size() == 0) startNode[0] = firstSaved;
                        else {
                            Edge selected = temp.get(next);
                            startNode[0] = nodes.get(Integer.parseInt(selected.getNode1().getId()));

                        }
                        String key = startNode[0].hashCode() + "";

                        if (!hm.containsKey(key)) {
                            hm.put(key, 0);
                        }
                        int val = hm.get(key);
                        hm.put(key, val + 1);
                        AudioParams.maxValue = Math.max(AudioParams.maxValue, val + 1);
    //            System.out.println(Arrays.toString(bb));

    //                }

    //                        byte[] output = Audio.baos.toByteArray();
    //                        try {
    //                            AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(output), Audio.audioFormat, output.length), AudioFileFormat.Type.WAVE, new File("out.wav"));
    //                        } catch (IOException e) {
    //                            e.printStackTrace();
    //                        }


                    }

    //        while (iter.hasNext()){
    //            Object bbb = iter.next();
    //
    //            Map.Entry<String, String> bbe = (Map.Entry<String, String>) bbb;
    //            System.out.println(bbb.toString());
    //
                }
            }).start();

        }

        private static HashMap<AudioInterval, AudioInterval> makeMap(int numClusters) {

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
            for (int songIter : playback) {


    //            datasets[songIter] = dataset;

                Song song = LoadFromFile.loadSong(list[songIter]);
                int cnt = 0;
                for (Segment s : song.analysis.getSegments()) {
                    Instance inst = getInstance(attlist, s);
                    coll.add(song.getAudioIntervalForSegment(cnt++));
                    inst.setDataset(dataset);
                    dataset.add(inst);
                }
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

        protected static double distance(Instance i1, Instance i2) {
            double tot = 0;
            for (int i = 0; i < attLength; i++) {
                double ta = i1.value(attlist[i]) - i2.value(attlist[i]);
                ta = Math.abs(ta);
                tot += ta;
            }
            return tot;
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


