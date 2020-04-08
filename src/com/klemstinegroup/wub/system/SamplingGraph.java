package com.klemstinegroup.wub.system;

import com.echonest.api.v4.Segment;

import javax.sound.sampled.AudioFormat;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Render a WaveForm.
 */
public class SamplingGraph {

	public SamplingGraph() {
	}

	public BufferedImage createWaveForm(List<Segment> segment, double duration, byte[] audioBytes, AudioFormat format, int w, int h) {
		if (w < 2)
			return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		int[] audioData = null;
		if (format.getSampleSizeInBits() == 16) {
			int nlengthInSamples = audioBytes.length / 2;
			audioData = new int[nlengthInSamples];
			if (format.isBigEndian()) {
				for (int i = 0; i < nlengthInSamples; i++) {
					/* First byte is MSB (high order) */
					int MSB = (int) audioBytes[2 * i];
					/* Second byte is LSB (low order) */
					int LSB = (int) audioBytes[2 * i + 1];
					audioData[i] = MSB << 8 | (255 & LSB);
				}
			} else {
				for (int i = 0; i < nlengthInSamples; i++) {
					/* First byte is LSB (low order) */
					int LSB = (int) audioBytes[2 * i];
					/* Second byte is MSB (high order) */
					int MSB = (int) audioBytes[2 * i + 1];
					audioData[i] = MSB << 8 | (255 & LSB);
				}
			}
		} else if (format.getSampleSizeInBits() == 8) {
			int nlengthInSamples = audioBytes.length;
			audioData = new int[nlengthInSamples];
			if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
				for (int i = 0; i < audioBytes.length; i++) {
					audioData[i] = audioBytes[i];
				}
			} else {
				for (int i = 0; i < audioBytes.length; i++) {
					audioData[i] = audioBytes[i] - 128;
				}
			}
		}
		Vector<Line2D.Double> lines = new Vector<Line2D.Double>();
		double frames_per_pixel = (double) audioBytes.length / (double) format.getFrameSize() / (double) w;
		byte my_byte = 0;
		int numChannels = format.getChannels();
		for (double x = 0; x < w && audioData != null; x++) {
			int idx = (int) (frames_per_pixel * numChannels * x);
			byte min = Byte.MAX_VALUE;
			byte max = Byte.MIN_VALUE;
			for (int i = 0; i < frames_per_pixel * numChannels; i += 1) {
				if (format.getSampleSizeInBits() == 8) {
					my_byte = (byte) audioData[idx + i];
				} else {
					my_byte = (byte) (128 * audioData[idx + i] / 32768);
				}
				min = (byte) Math.min(min, my_byte);
				max = (byte) Math.max(max, my_byte);
			}
			double y_new = (double) (h * (128 - min) / 256);
			double y_new1 = (double) (h * (128 - max) / 256);
			lines.add(new Line2D.Double(x, h - y_new1, x, y_new1));
		}
		BufferedImage bufferedImage = new BufferedImage(w + 1, h + 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bufferedImage.createGraphics();

//		transparent
//		if (whiteBackground){g2.setBackground(new Color(255, 255, 255, 255));}
//		else{
			g2.setBackground(new Color(0, 0, 0, 0));
//		}
		//glossy
//		g2.setBackground(new Color(50, 50, 150, 255));
		g2.setColor(Color.white);
		g2.clearRect(0, 0, w, h);


		HashMap<Integer, Color> hm = new HashMap<Integer, Color>();
		if (segment != null) {
			double[] min = new double[12];
			double[] max = new double[12];
			double[] range = new double[12];
			for (int i = 0; i < 12; i++) {
				min[i] = Double.MAX_VALUE;
				max[i] = Double.MIN_VALUE;
			}
			for (Segment s : segment) {
				double[] timbre = s.getTimbre();
				for (int i = 0; i < 12; i++) {
					min[i] = Math.min(min[i], timbre[i]);
					max[i] = Math.max(max[i], timbre[i]);
				}
			}
			for (int i = 0; i < 12; i++) {
				range[i] = max[i] - min[i];
			}

			for (Segment s : segment) {
				int x1 = (int) ((s.getStart() / duration) * (double) w + .5d);
				int x2 = (int) (((s.getStart() + s.getDuration()) / duration) * (double) w + .5d);
				float hc = (float) ((s.getTimbre()[1] - min[1]) / range[1]);
				float sc = 1.0f;
				// float lc = (float) ((s.getTimbre()[0] - min[0]) / range[0]);
				float lc = .7f;

				Color c = HSLColor.toRGB(hc * 360, sc * 100, lc * 100);
				for (int x3 = x1; x3 <= x2; x3++) {
					hm.put(x3, c);
				}
			}

		}
		g2.setColor(Color.black);
		for (int i = 1; i < lines.size(); i++) {
			Line2D line = lines.get(i);

			if (hm.get((int) line.getP1().getX()) != null) {
				g2.setColor(hm.get((int) line.getP1().getX()));

			}
			g2.draw(line);
		}

		g2.dispose();
		return bufferedImage;
	}
}
