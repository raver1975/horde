package com.klemstinegroup.wub;


public class Wub {

	public static void main(String[] args) {
//		AudioObject.factory("songs/test.play");
		if (args.length==0) AudioObject.factory();
		else {
			for (int i = 1; i < args.length; i++) {
				AudioObject.factory(args[i],null);
			}
		}
	}
}
