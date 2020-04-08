package com.klemstinegroup.wub.system;

import com.klemstinegroup.wub.AudioObject;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;

public class Node implements Serializable{
	Rectangle2D.Double rect;
	transient BufferedImage image;
	AudioObject ao;
	long random;

	public Node(Rectangle2D.Double playFieldPosition, AudioObject ao) {
		this.rect = playFieldPosition;
		this.ao = ao;
		random=(long) (Math.random()*Long.MAX_VALUE);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Node))
			return false;
		Node n = (Node) o;
		return random==n.random&&(this.rect.equals(n.rect));
	}
}
