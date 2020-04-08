package com.klemstinegroup.wub.system;

import com.klemstinegroup.wub.AudioObject;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CentralCommand {

	static ArrayList<AudioObject> aolist = new ArrayList<AudioObject>();
	static PlayingField pf = new PlayingField();
	static public CentralCommandNode ccn = new CentralCommandNode();
	static int yOffset = 40;
	public static File lastDirectory = new File(System.getProperty("user.dir"));

	public static void add(AudioObject ao) {

		aolist.add(ao);
		addRectangle(new Node(new Rectangle2D.Double(0, 0, 1, yOffset), ao));

	}

	public static void remove(AudioObject ao) {
		aolist.remove(ao);
	}

	public static void key(String s) {
		for (AudioObject au : aolist) {
			if (au.midiMap.containsKey(s)) {
				au.queue.add(au.midiMap.get(s));
			}
		}
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
		ccn.nodes.remove(mover);
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
			if (cn != null)
				ccn = cn;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pf.makeData();
		pf.playByte = 0;
		ArrayList<String> loaded=new ArrayList<String>();
		for (Node n : ccn.nodes) {
			if (!loaded.contains(n.ao.file.getAbsolutePath())){
				n.ao.init(false);
				loaded.add(n.ao.file.getAbsolutePath());
			}
		}
	}
}
