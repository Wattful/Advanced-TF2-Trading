package trading.economy;

import org.json.*;

//TODO:

/**Immutable class representing an item price, in keys and refined.
*/

public class Price implements Comparable<Price>{
	private final int keys;
	private final int refined;

	/**Constructs a Price with the given keys and refined values.
	@param keys the number of keys.
	@param refined the number of refined.
	@throws IllegalArgumentException if either argument is negative.
	*/
	public Price(int keys, int refined){
		if(keys < 0 || refined < 0){
			throw new IllegalArgumentException("A value was negative.");
		}
		this.keys = keys;
		this.refined = refined;
	}

	/**Returns the number of keys in this Price.
	@return the number of keys in this Price.
	*/
	public int getKeys(){
		return this.keys;
	}

	/**Returns the number of refined in this Price.
	@return the number of refined in this Price.
	*/
	public int getRefined(){
		return this.refined;
	}

	/**Returns this Price's key price as a decimal value.
	@param keyScrapRatio the key-to-scrap ratio to use for this calculation.
	@throws IllegalArgumentException if keyScrapRatio is non-positive.
	@return this Price as a decimal value.
	*/
	public double getDecimalPrice(int keyScrapRatio){
		if(keyScrapRatio <= 0){
			throw new IllegalArgumentException("keyScrapRatio was non-positive");
		}
		return this.keys + ((this.refined*9.0)/keyScrapRatio);
	}

	/**Returns this Price as a total number of scrap.
	@param keyScrapRatio the key-to-scrap ratio to use for this calculation.
	@throws IllegalArgumentException if keyScrapRatio is non-positive.
	@return this Price as a total number of scrap.
	*/
	public int getScrapValue(int keyScrapRatio){
		if(keyScrapRatio <= 0){
			throw new IllegalArgumentException("keyScrapRatio was non-positive");
		}
		return (this.keys * keyScrapRatio) + (this.refined * 9);
	}

	/**Returns a JSON representation of this Price suitable for use in Backpack.tf API calls and Price's fromJSON method.
	@return JSON representation of this Price
	*/
	public JSONObject getJSONRepresentation(){
		JSONObject answer = new JSONObject();
		answer.put("keys", this.keys);
		answer.put("metal", this.refined);
		return answer;
	}

	/**Returns a Price object parsed from the given input. The JSON objects returned by getJSONRepresentation() are compatible with this method.
	@param input the JSONOjbect to parse.
	@throws NullPointerException if input is null.
	@throws JSONException if the input is improperly formatted.
	@return the parsed Price.
	*/
	public static Price fromJSONRepresentation(JSONObject input){
		if(input == null){
			throw new NullPointerException();
		}
		return new Price(input.getInt("keys"), input.getInt("metal"));
	}

	/**Returns this Price scaled by the given double.
	@param scale the ratio to scale by.
	@param keyScrapRatio the key-to-scrap ratio to use for this calculation.
	@throws IllegalArgumentException if keyScrapRatio is non-positive.
	@return this Price scaled by the given double.
	*/
	public Price scaleBy(double scale, int keyScrapRatio){
		return Price.calculate(this.getDecimalPrice(keyScrapRatio) * scale, keyScrapRatio);
	}

	/**Returns a Price consisting of the given double interpreted as the key price.
	@param decimalPrice the key price.
	@param keyScrapRatio the key-to-scrap ratio to use for this calculation.
	@throws IllegalArgumentException if keyScrapRatio is non-positive.
	@return the Price.
	*/
	public static Price calculate(double decimalPrice, int keyScrapRatio){
		if(keyScrapRatio <= 0){
			throw new IllegalArgumentException("keyScrapRatio was non-positive");
		}
		int k = (int)decimalPrice;
		double decimalRef = decimalPrice - k;
		int r = (int)Math.round((decimalRef * keyScrapRatio) / 9);
		return new Price(k, r);
	}

	/**Averages all prices and returns the result.
	@param keyScrapRatio the key-to-scrap ratio to use for this calculation.
	@param prices the prices to average.
	@throws NullPointerException if prices is null or any value in prices is null.
	@throws IllegalArgumentException if keyScrapRatio is non-positive, or no prices are provided.
	@return the average price.
	*/
	public static Price average(int keyScrapRatio, Price... prices){
		if(prices.length == 0) {
			throw new IllegalArgumentException("No prices were provided.");
		}
		double total = 0;
		for(Price p : prices){
			total += p.getDecimalPrice(keyScrapRatio);
		}
		double average = total/prices.length;
		return calculate(average, keyScrapRatio);
	}
	
	/**Returns a String describing this Price.
	@return a String describing this Price.
	*/
	String valueString() {
		return this.keys + " keys, " + this.refined + " refined";
	}
	
	@Override
	/**Compares this Price to the specified Price, returning a negative integer, zero, or a positive integer if this Price is
	less than, equal to, or greater than the given Price.
	@param p the Price to compare to
	@throws NullPointerException if p is null
	@return an integer indicating the comparison's result.
	*/
	public int compareTo(Price p) {
		if(this.equals(p)) {
			return 0;
		}
		if(this.keys != p.keys) {
			return Integer.compare(this.keys, p.keys);
		}
		return Integer.compare(this.refined, p.refined);
	}

	/**Returns a hash code for this Price.
	@return a hash code for this Price.
	*/
	@Override
	public int hashCode(){
		return this.keys + this.refined;
	}

	/**Returns a boolean indicating whether the given object is equal to this Price.
	@param o the object to compare to.
	@return a boolean indicating whether the given object is equal to this Price.
	*/
	@Override
	public boolean equals(Object o){
		if(this == o){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof Price)){
			return false;
		}
		Price p = (Price)o;
		return p.keys == this.keys && p.refined == this.refined;
	}
	
	/**Returns a String representation of this Price.
	@return a String representation of this Price.
	*/
	@Override
	public String toString(){
		return "trading.economy.Price: " + this.valueString();
	}
}