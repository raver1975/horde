package com.klemstinegroup.wub.system;

import java.io.Serializable;
import java.util.ArrayList;

public class CentralCommandNode implements Serializable{

	public ArrayList<Node> nodes = new ArrayList<Node>();

	public CentralCommandNode() {
	}
	
	public CentralCommandNode(ArrayList<Node> c){
		nodes.addAll(c);
	}

}
