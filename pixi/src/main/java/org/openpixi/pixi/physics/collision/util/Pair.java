package org.openpixi.pixi.physics.collision.util;

public class Pair<A, B> {
	
	private A first;
	private B second;
	
	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}

	@Override public int hashCode() {
		int hashFirst = first != null ? first.hashCode() : 0;
		int hashSecond = second != null ? second.hashCode() : 0;
		
		return (hashFirst + hashSecond) * hashFirst + hashSecond;
	}
	
	@Override public boolean equals (Object another) {
		if(another != null && this.getClass() == another.getClass()) {
			Pair anotherPair = (Pair) another;
			/*return
					((this.first == anotherPair.first || (this.first != null && anotherPair.first != null && 
					this.first.equals(anotherPair.first))) &&
					(this.second == anotherPair.second || (this.second != null && anotherPair.second != null &&
					this.second.equals(anotherPair.second))));
					*/
			return
					this.first == anotherPair.first && this.second == anotherPair.second;
					//this.hashCode() == anotherPair.hashCode();
		} else {		
		return false;
		}
	}
	
	public int compareTo(Pair<A, B> another) {
		return this.hashCode() < another.hashCode() ? -1 : 1;
	}
	
	public boolean containsFirst(A another) {
		return (this.first == another);
	}
	
	public boolean containsSecond(B another) {
		return (this.second == another);
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
