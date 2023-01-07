package com.notlord.utils;

public class Tuple<V,K> {
	private V v;
	private K k;

	public Tuple(V v, K k) {
		this.v = v;
		this.k = k;
	}

	public V getV() {
		return v;
	}

	public void setV(V v) {
		this.v = v;
	}

	public K getK() {
		return k;
	}

	public void setK(K k) {
		this.k = k;
	}
}
