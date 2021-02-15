package trading.economy;

import org.json.*;
import java.util.*;

//TODO: Figure out how unpriced hats work, consider feature of accepting/declining groups of hats

/**Functional interface which takes in information about an unusual hat and determines whether this hat is 
"acceptable", or whether a BuyListing should be made for it.<br>
All default functions provided, including acceptAll, reject hats which are priced in a non-keys-or-metal currency, such as buds or USD.
Accepting these hats will cause errors, as they are unsupported by the Price and PriceRange objects.<br>
A method for checking currency types, acceptableCurrency, is provided in PriceRange.
*/

@FunctionalInterface
public interface AcceptabilityFunction{
	/**Returns whether the given hat is acceptable.
	@param pricesObject the backpack.tf prices object for the unusual hat (see https://backpack.tf/api/index.html, v4PricesEntry)
	@param name the name of the hat.
	@param effect the hat's unusual effect.
	@param keyScrapRatio the key-to-scrap ratio, which may be useful for calculations.
	@throws NullPointerException if any parameter is null.
	@throws IllegalArgumentException if keyScrapRatio is non-positive.
	@throws JSONException if pricesObject is malformed.
	*/
	boolean determineAcceptability(JSONObject pricesObject, String name, Effect effect, int keyScrapRatio);

	/**Returns a trivial AcceptabilityFunction which accepts all hats, with one exception: 
	those that are priced in a non-keys-or-metal currency such as buds or USD.
	@return the described AcceptabilityFunction.
	*/
	public static AcceptabilityFunction acceptAll(){
		return (JSONObject pricesObject, String name, Effect effect, int keyScrapRatio) -> {
			return PriceRange.acceptableCurrency(pricesObject);
		};
	}

	/**Returns an AcceptabilityFunction which returns true only if the hat satisfies several conditions, one for each parameter.<br>
	The parameter restrictions are:
	@param minKeys Checks that the hat's average value is above minKeys keys.
	@param maxKeys Checks that the hat's average value is below maxKeys keys. Negative or 0 value indicates no restriction.
	@param maxRange Checks that the range between the hat's high and low values is below maxRange keys. Negative value indicates no restriction.
	@param lastUpdate Checks that the hat's price was updated within lastUpdate seconds. Negative or 0 value indicates no restriction.
	@throws IllegalArgumentException if any double value is NaN.
	@return the described AcceptabilityFunction.
	*/
	public static AcceptabilityFunction checkData(double minKeys, double maxKeys, double maxRange, long lastUpdate){
		if(Double.isNaN(minKeys) || Double.isNaN(maxKeys) || Double.isNaN(maxRange)){
			throw new IllegalArgumentException("A value was NaN.");
		}
		return (JSONObject pricesObject, String name, Effect effect, int keyScrapRatio) -> {
			if(!PriceRange.acceptableCurrency(pricesObject)) {
				return false;
			}
			return checkDataOnHat(pricesObject, minKeys, maxKeys, maxRange, lastUpdate, keyScrapRatio);
		};
	}

	/**Returns an AcceptabilityFunction which determines acceptability based on the hat itself and the hat's effect.<br>
	The hat's name and effect will be checked against collections provided by the caller. 
	The caller can choose, through the mode parameters, whether these collections contain the only names or effects which will be accepted, 
	or the only names or effects which will not be accepted.<br>
	If the mode variable is true, then the collection contains the names or effects which will be accepted, if it is false, 
	it contains those which will not be accepted.<br>
	In addition, the collections can be null, which indicates no restriction for names or effects, and in which case that mode variable is ignored.
	@param nameMode the mode variable for hat names.
	@param acceptableHats The names of the hats (should not have "the" at the beginning). null value indicates no restriction.
	@param effectMode the mode variable for hat effects.
	@param acceptableEffects The effects. null value indicates no restriction.
	@return the described AcceptabilityFunction.
	*/
	public static AcceptabilityFunction checkType(boolean nameMode, Collection<? extends String> names, boolean effectMode, Collection<? extends Effect> effects){
		return (JSONObject pricesObject, String name, Effect effect, int keyScrapRatio) -> {
			if(!PriceRange.acceptableCurrency(pricesObject)) {
				return false;
			}
			return checkTypeOfHat(name, effect, nameMode, names, effectMode, effects);
		};
	}

	/**Returns an acceptability function which returns true if and only if the hat would satisfy the functions returned by both checkData and checkType.
	@return the described AcceptabilityFunction.
	*/
	public static AcceptabilityFunction checkDataAndType(double minKeys, double maxKeys, double maxRange, long lastUpdate, 
				boolean nameMode, Collection<? extends String> names, boolean effectMode, Collection<? extends Effect> effects){
		if(Double.isNaN(minKeys) || Double.isNaN(maxKeys) || Double.isNaN(maxRange)){
			throw new IllegalArgumentException("A value was NaN.");
		}
		return (JSONObject pricesObject, String name, Effect effect, int keyScrapRatio) -> {
			if(!PriceRange.acceptableCurrency(pricesObject)) {
				return false;
			}
			return checkDataOnHat(pricesObject, minKeys, maxKeys, maxRange, lastUpdate, keyScrapRatio) && checkTypeOfHat(name, effect, nameMode, names, effectMode, effects);
		};
	}
	
	//Performs the checks described in checkData
	private static boolean checkDataOnHat(JSONObject pricesObject, double minKeys, double maxKeys, double maxRange, long lastUpdate, int keyScrapRatio){
		if(pricesObject == null){
			throw new NullPointerException();
		}
		if(Double.isNaN(minKeys) || Double.isNaN(maxKeys) || Double.isNaN(maxRange)){
			throw new IllegalArgumentException("A value was NaN.");
		}

		PriceRange communityPrice = PriceRange.fromBackpackTFRepresentation(pricesObject, keyScrapRatio);
		Price lowPrice = communityPrice.lower();
		Price middlePrice = communityPrice.middle();
		Price highPrice = communityPrice.upper();
		java.util.Date d = new java.util.Date();

		if(middlePrice.getDecimalPrice(keyScrapRatio) < minKeys){
			return false;
		}
		if(maxKeys > 0 && middlePrice.getDecimalPrice(keyScrapRatio) > maxKeys){
			return false;
		}
		if(maxRange >= 0 && highPrice.getDecimalPrice(keyScrapRatio) - lowPrice.getDecimalPrice(keyScrapRatio) > maxRange){
			return false;
		}
		if(lastUpdate > 0 && (d.getTime()/1000) - pricesObject.getInt("last_update") > lastUpdate){
			return false;
		}
		return true;
	}

	//Performs the checks described in checkType
	private static boolean checkTypeOfHat(String name, Effect effect, boolean nameMode, Collection<? extends String> names, boolean effectMode, Collection<? extends Effect> effects){
		if(name == null || effect == null){
			throw new NullPointerException();
		}
		if(names != null && nameMode != names.contains(name)){
			return false;
		}
		if(effects != null && effectMode != effects.contains(effect)){
			return false;
		}
		return true;
	}
}