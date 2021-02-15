package trading.economy;

import org.json.*;

//TODO:

/**Class representing a range of prices, with upper, lower, and middle price values.
*/

public class PriceRange{
	private final Price lower;
	private final Price upper;
	private final Price middle;

	/**Constructs a PriceRange using the given price as the upper and lower bounds.
	@param price the price to use
	@throws NullPointerExcpetion if price is null
	*/
	public PriceRange(Price price){
		if(price == null){
			throw new NullPointerException();
		}
		this.lower = price;
		this.upper = price;
		this.middle = price;
	}

	/**Constructs a PriceRange using the given pricse as the upper and lower bounds.
	@param lower the lower bound
	@param upper the upper bound
	@param keyScrapRatio the key-to-scrap ratio to use for this calculation.
	@throws NullPointerExcpetion if any paramter is null.
	@throws IllegalArgumentException if keyScrapRatio is non-positive.
	*/
	public PriceRange(Price lower, Price upper, int keyScrapRatio){
		if(lower == null || upper == null){
			throw new NullPointerException();
		}
		Price middle = Price.average(keyScrapRatio, lower, upper);
		boolean switched = lower.getDecimalPrice(keyScrapRatio) > upper.getDecimalPrice(keyScrapRatio);
		this.lower = switched ? upper : lower;
		this.upper = switched ? lower : upper;
		this.middle = middle;
	}
	
	private PriceRange(Price lower, Price upper, Price middle) {
		this.lower = lower;
		this.upper = upper;
		this.middle = middle;
	}

	/**Returns a Price representing this PriceRange's upper bound.
	@return a Price representing this PriceRange's upper bound.
	*/
	public Price upper(){
		return this.upper;
	}

	/**Returns a Price representing this PriceRange's lower bound.
	@return a Price representing this PriceRange's lower bound.
	*/
	public Price lower(){
		return this.lower;
	}

	/**Returns a Price representing the middle of this PriceRange
	@return a Price representing this PriceRange's lower bound.
	*/
	public Price middle(){
		return this.middle;
	}

	/**Returns a JSON representation of this PriceRange, suitable for use with the fromJSON method.
	@return a JSON representation of this PriceRange
	*/
	public JSONObject getJSONRepresentation(){
		JSONObject answer = new JSONObject();
		answer.put("lower", this.lower.getJSONRepresentation());
		answer.put("upper", this.upper.getJSONRepresentation());
		answer.put("middle", this.middle.getJSONRepresentation());
		return answer;
	}

	/**Constructs and returns a PriceRange from the given JSON representation.<br>
	JSON objects from the getJSONRepresentation() method are suitable for use with this method.
	@param input the JSONObject to parse
	@throws NullPointerException if input is null
	@throws JSONException if the JSON object is improperly formatted
	@return the constructed PriceRange.
	*/
	public static PriceRange fromJSONRepresentation(JSONObject input){
		Price lower = Price.fromJSONRepresentation(input.getJSONObject("lower"));
		Price upper = Price.fromJSONRepresentation(input.getJSONObject("upper"));
		Price middle = Price.fromJSONRepresentation(input.getJSONObject("middle"));
		return new PriceRange(lower, upper, middle);
	}
	
	/**Determines whether the given backpack.tf price range object is priced in the two acceptable currencies: keys and metal.
	Using the fromBackpackTFRepresentation method to parse currencies other than keys or metal will result in an exception.
	@param pricesObject the item's prices object.
	@throws NullPointerException if pricesObject is null.
	@throws JSONException if pricesObject is malformed.
	@return a boolean indicating whether the given item price object is priced in keys.
	*/
	public static boolean acceptableCurrency(JSONObject pricesObject) {
		return pricesObject.getString("currency").equals("keys") || pricesObject.getString("currency").equals("metal");
	}

	/**Constructs and returns a PriceRange from the Backpack.tf representation of an item's price.<br>
	This representation can be found in the Backpack.tf prices object.
	@param input the JSONObject.
	@param keyScrapRatio the key-to-scrap ratio to use for this calculation.
	@throws NullPointerException if input is null.
	@throws IllegalArgumentException if the given price object's currency is neither keys nor metal, or if keyScrapRatio is non-positive.
	@throws JSONException if the input JSONObject is malformed.
	@return the constructed PriceRange.
	*/
	public static PriceRange fromBackpackTFRepresentation(JSONObject input, int keyScrapRatio){
		int low = input.getInt("value");
		int high = input.has("value_high") ? input.getInt("value_high") : low;
		if(input.getString("currency").equals("keys")){
			return new PriceRange(new Price(low, 0), new Price(high, 0), keyScrapRatio);
		} else if(input.getString("currency").equals("metal")){
			return new PriceRange(new Price(0, low), new Price(0, high), keyScrapRatio);
		} else {
			throw new IllegalArgumentException("Unsupported currency: " + input.getString("currency"));
		}
	}

	/**Returns a hash code for this PriceRange.
	@return a hash code for this PriceRange.
	*/
	@Override
	public int hashCode(){
		return this.lower.hashCode() + this.upper.hashCode() + this.middle.hashCode();
	}

	/**Returns a boolean indicating whether the given object is equal to this PriceRange.<br>
	This will return true if the given object is a PriceRange with the same upper and lower bounds.
	@param o the object to compare to.
	@return a boolean indicating whether the given object is equal to this PriceRange.
	*/
	@Override
	public boolean equals(Object o){
		if(this == o){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof PriceRange)){
			return false;
		}
		PriceRange pr = (PriceRange)o;
		return this.lower.equals(pr.lower) && this.upper.equals(pr.upper) && this.middle.equals(pr.middle);
	}

	@Override
	/**Returns a String representation of this PriceRange.
	@return a String representation of this PriceRange.
	*/
	public String toString(){
		if(this.lower.equals(this.upper)) {
			return "trading.economy.PriceRange: " + this.middle.valueString();
		} else {
			return "trading.economy.PriceRange: " + this.lower.valueString() + " to " + this.upper.valueString();
		}
	}
}