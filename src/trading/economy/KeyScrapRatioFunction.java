package trading.economy;

import org.json.*;

//TODO:

/**Functional interface representing a method for providing a key-to-scrap ratio given a Backpack.tf prices object.<br>
The ratio can be based on the prices object or calculated in a different way.
*/

@FunctionalInterface
public interface KeyScrapRatioFunction {
	/**Provides a key-to-scrap ratio given a Backpack.tf prices object.
	@param pricesObject a Backpack.tf prices object
	@throws NullPointerException if pricesObject is null.
	@throws JSONException if pricesObject is malformed.
	@return a key-to-scrap ratio.
	*/
	int calculateRatio(JSONObject pricesObject);

	/**Returns a KeyScrapRatioFunction which always returns the given custom ratio, ignoring the prices object.
	@param ratio the key-to-scrap ratio to return in the function.
	@throws IllegalArgumentException if ratio is non-positive.
	@return the described KeyScrapRatioFunction.
	*/
	public static KeyScrapRatioFunction customRatio(int ratio){
		if(ratio <= 0){
			throw new IllegalArgumentException("Key-scrap ratio was non-positive.");
		}
		return (JSONObject pricesObject) -> {
			return ratio;
		};
	}

	/**Returns a KeyScrapRatioFunction which return the middle of Backpack.tf's community key-to-scrap ratio.
	@return the described KeyScrapRatioFunction.
	*/
	public static KeyScrapRatioFunction backpackTFRatio(){
		return (JSONObject pricesObject) -> {
			//Key-to-scrap ratio doesn't matter for these calculations, so 1 is used.
			PriceRange range = PriceRange.fromBackpackTFRepresentation(pricesObject.getJSONObject("response").getJSONObject("items").getJSONObject("Mann Co. Supply Crate Key").getJSONObject("prices").getJSONObject("6").getJSONObject("Tradable").getJSONObject("Craftable").getJSONObject("0"), 1);
			return range.middle().getScrapValue(1);
		};
	}
}