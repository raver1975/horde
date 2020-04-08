package com.klemstinegroup.wub.ai.vectorrnn;

class Vector{
    char c;
	public Vector(char c){
		this.c=c;
	}
	public String toString(){
		return c+"";
	}

	@Override
	public boolean equals(Object v){
		if (!(v instanceof Vector))return false;
		Vector v1=(Vector)v;
		return this.c==v1.c;
	}

	@Override
	public int hashCode(){
		return new Character(c).hashCode();
	}
}