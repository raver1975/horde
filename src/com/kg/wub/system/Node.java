package com.kg.wub.system;

import com.kg.wub.AudioObject;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;

public class Node implements Serializable{
	Rectangle2D.Double rect;
	transient BufferedImage image;
	AudioObject ao;
	boolean mute;
	long random;

	public Node(Rectangle2D.Double playFieldPosition, AudioObject ao) {
		this.rect = playFieldPosition;
		this.ao = ao;
		random=(long) (Math.random()*Long.MAX_VALUE);
	}

	public boolean isMute() {
		return mute;
	}

	public void setMute(boolean mute) {
		this.mute = mute;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Node))
			return false;
		Node n = (Node) o;
		return random==n.random&&(this.rect.equals(n.rect));
	}

	public void toggleMute() {
		mute=!mute;
	}
}
