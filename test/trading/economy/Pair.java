package trading.economy;

/**Immutable class representing a pair.
@author Owen Kulik
*/

public class Pair<F, S> {
	private final F first;
	private final S second;

	/**Creates a pair with f and s.
	@param f the first value in the pair.
	@param s the second value in the pair.
	*/
	public Pair(F f, S s){
		first = f;
		second = s;
	}

	/**Returns a pair consisting of the given values.
	@param f the first value in the pair.
	@param s the second value in the pair.
	@return the pair.
	*/
	public static <F, S> Pair<F, S> of(F f, S s){
		return new Pair<F, S>(f, s);
	}

	/**Returns the first value in the pair.
	@return the first value in the pair.
	*/
	public F first(){
		return first;
	}

	/**Returns the second value in the pair.
	@return the second value in the pair.
	*/
	public S second(){
		return second;
	}

	/**Returns a pair with this Pair's values reversed. <br>
	Note that changes to the returned pair's values will affect this pair's values.
	@return a pair with this Pair's values reversed.
	*/
	public Pair<S, F> reversePair(){
		return new Pair<S, F>(this.second(), this.first());
	}

	@Override
	/**Returns a String representing this pair.
	@return a String representing this pair.
	*/
	public String toString(){
		return "(" + (first != null ? first.toString() : "null") + ", " + (second != null ? second.toString() : "null") + ")";
	}

	@Override
	/**Returns a hash code for this Pair.
	@return a hash code for this Pair.
	*/
	public int hashCode(){
		return (first != null ? first.hashCode() : 0) + (second != null ? second.hashCode() : 0);
	}

	@Override
	/**Indicates if another object is equal to this Pair.
	They are considered equal if both the first and second elements of each pair are equal.
	@param o The object to compare to.
	@return a boolean indicating if another object is equal to this Pair.
	*/
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(this == o){
			return true;
		}
		if(!(o instanceof Pair)){
			return false;
		}
		Pair p = (Pair)o;
		boolean firstEqual = this.first != null ? this.first.equals(p.first) : p.first == null;
		boolean secondEqual = this.second != null ? this.second.equals(p.second) : p.second == null;
		return firstEqual && secondEqual;
	}
}
