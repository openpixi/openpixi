package org.openpixi.pixi.physics.collision.util;

public class Pair<A, B> {
	
	private A first;
	private B second;
	
	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}

	public int hashCode() {
		int hashFirst = first != null ? first.hashCode() : 0;
		int hashSecond = second != null ? second.hashCode() : 0;
		
		return (hashFirst + hashSecond) * hashFirst + hashSecond;
	}
	
	public boolean equals (Object another) {
		if(another != null && this.getClass() == another.getClass()) {
			Pair anotherPair = (Pair) another;
			return
					((this.first == anotherPair.first || (this.first != null && anotherPair.first != null && 
					this.first.equals(anotherPair.first))) &&
					(this.second == anotherPair.second || (this.second != null && anotherPair.second != null &&
					this.second.equals(anotherPair.second))));
		}
		
		return false;
	}
	
	public A getFirst() {
		return this.first;
	}
	
	public void setFirst(A newFirst) {
		this.first = newFirst; 
	}
	
	public B getSecond() {
		return this.second;
	}
	public void setSecond(B newSecond) {
		this.second = newSecond;
	}
	
	
}
