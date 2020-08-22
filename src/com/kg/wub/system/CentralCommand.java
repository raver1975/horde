package com.kg.wub.system;

import com.google.common.cache.CacheBuilder;
import com.kg.wub.AudioObject;
import com.twelvemonkeys.util.ExpiringMap;
import com.twelvemonkeys.util.TimeoutMap;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class CentralCommand<lastSeenMidi> {

    public static ArrayList<AudioObject> aolist = new ArrayList<AudioObject>();
    public static PlayingField pf = new PlayingField();
    static public CentralCommandNode ccn = new CentralCommandNode();
    static int yOffset = 40;
    public static File lastDirectory = new File(System.getProperty("user.dir"));

    public static void add(AudioObject ao) {
        pf.frame.setVisible(true);
        aolist.add(ao);
        addRectangle(new Node(new Rectangle2D.Double(0, 0, 1, yOffset), ao));

    }

    public static void remove(AudioObject ao) {
        aolist.remove(ao);
    }


    private static ExpiringMap<String, String> lastSeenMidi = new TimeoutMap<>(1000);

    public static List<String> getLastSeenMidi() {
        String[] a = lastSeenMidi.keySet().toArray(new String[0]);
        Arrays.sort(a);
//        System.out.println(Arrays.toString(a));
        return Arrays.asList(a);
    }

    public static void midi(String s) {
//		System.out.println("key pressed:"+s);
//        getLastSeenMidi();
        for (AudioObject au : aolist) {
            if (au != null && au.midiMap != null && au.midiMap.containsKey(s) && !lastSeenMidi.containsKey(s)) {
                System.out.println("found mapping");
                au.queue.add(au.midiMap.get(s));
            }
        }
        lastSeenMidi.put(s, "");
    }

    public static void addRectangleNoMoveY(Node n) {
        ccn.nodes.add(n);
        // pf.makeImageResize();
    }

    public static void addRectangle(Node n) {
        ccn.nodes.add(n);
        while (CentralCommand.intersects(n)) {
            n.rect.y += yOffset;
        }
        pf.makeData();
    }

    public static void removeRectangle(Node mover) {
        if (mover != null) {
            ccn.nodes.remove(mover);
            try {
                mover.ao.mc.frame.dispose();
            } catch (Exception e) {
            }
        }
    }

    public static boolean intersects(Rectangle2D.Double r) {

        for (Node n : ccn.nodes) {
            if (r.intersects(n.rect))
                return true;
        }
        return false;
    }

    public static boolean intersects(Node mover) {
        for (Node n : ccn.nodes) {
            if (mover != n && mover.rect.intersects(n.rect))
                return true;
        }
        return false;
    }

    public static Node whichIntersects(Node mover, ArrayList<Node> copy) {
        for (Node n : ccn.nodes) {
            if (mover != n && mover.rect.intersects(n.rect) && !copy.contains(n))
                return n;
        }
        return null;
    }

    public static void loadPlay(File selectedFile) {
        try {
            CentralCommandNode cn = (CentralCommandNode) Serializer.load(selectedFile);
            if (cn != null) {

                for (Node n : cn.nodes) {
                    addRectangle(n);
                    aolist.add(n.ao);
                }
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        pf.makeData();
        pf.playByte = 0;
        ArrayList<String> loaded = new ArrayList<String>();
        for (Node n : ccn.nodes) {
            if (!loaded.contains(n.ao.file.getAbsolutePath())) {
                n.ao.init(false);
                loaded.add(n.ao.file.getAbsolutePath());
            }
        }
    }


}
