package rnn.util;

import rnn.matrix.Matrix;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Util {
	
	public static int pickIndexFromRandomVector(Matrix probs, Random r) throws Exception {
		double mass = 1.0;
		for (int i = 0; i < probs.w.length; i++) {
			double prob = probs.w[i] / mass;
			if (r.nextDouble() < prob) {
				return i;
			}
			mass -= probs.w[i];
		}
		throw new Exception("no target index selected");
	}
	
	public static double median(List<Double> vals) {
		Collections.sort(vals);
		int mid = vals.size()/2;
		if (vals.size() % 2 == 1) {
			return vals.get(mid);
		}
		else {
			return (vals.get(mid-1) + vals.get(mid)) / 2;
		}
	}

	public static String timeString(double milliseconds) {
		String result = "";
		
		int m = (int) milliseconds;
		
		int hours = 0;
		while (m >= 1000*60*60) {
			m -= 1000*60*60;
			hours++;
		}
		int minutes = 0;
		while (m >= 1000*60) {
			m -= 1000*60;
			minutes++;
		}
		if (hours > 0) {
			result += hours + " hours, ";
		}
		int seconds = 0;
		while (m >= 1000) {
			m -= 1000;
			seconds ++;
		}
		result += minutes + " minutes and ";
		result += seconds + " seconds.";
		return result;
	}
}
