package org.openpixi.pixi.physics.collision.util;

public class Pair<A, B> {
	
	private A first;
	private B second;
	
	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public int hashCode() {
		
		return first.hashCode() * second.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		 if (this == obj) return true;
		    if (obj == null ) return false;
		    return this.hashCode() == obj.hashCode();
	}
	
	/*
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}
	*/
	/*
	 * @Override public int hashCode() {
		int hashFirst = first != null ? first.hashCode() : 0;
		int hashSecond = second != null ? second.hashCode() : 0;
		
		int hash = 0;
		
		hash = 37 * hash + hashFirst;
		hash = 57 * hash + hashSecond;
		return hash;
		//return (hashFirst + hashSecond) * hashFirst + hashSecond;
	}
	
	@Override public boolean equals (Object another) {
		if(another != null && this.getClass() == another.getClass()) {
			Pair anotherPair = (Pair) another;
			/*return
					((this.first == anotherPair.first || (this.first != null && anotherPair.first != null && 
					this.first.equals(anotherPair.first))) &&
					(this.second == anotherPair.second || (this.second != null && anotherPair.second != null &&
					this.second.equals(anotherPair.second))));
					
			return
					this.first == anotherPair.first && this.second == anotherPair.second;
					//this.hashCode() == anotherPair.hashCode();
		} else {		
		return false;
		}
	}
	 */
	
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
