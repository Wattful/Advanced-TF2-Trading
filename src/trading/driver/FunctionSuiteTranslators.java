package trading.driver;

import trading.economy.*;
import org.json.*;
import java.util.*;

//TODO:

/**Class with static methods used to translate JSON input to default versions of FunctionSuite functions.
*/

class FunctionSuiteTranslators{
	/**Returns an AcceptabilityFunction using the checkType method, with each JSONArray being translated into a Collection of String and Effect, respectively.
	@param nameMode the name mode.
	@param names the hat names. Each value in the JSONArray must be a String. null value indicates no restriction.
	@param effectMode the effect mode.
	@param effects the effects. Each value in the JSONArray must be a String or int, with String parsed using Effect.forName() and int parsed using Effect.forInt(). 
	null value indicates no restriction.
	@throws JSONException if either JSONArray is malformed.
	@return the described AcceptabilityFunction.
	*/
	static AcceptabilityFunction checkType(boolean nameMode, JSONArray names, boolean effectMode, JSONArray effects){
		return AcceptabilityFunction.checkType(nameMode, parseNames(names), effectMode, parseEffects(effects));
	}

	/**Returns an AcceptabilityFunction using the checkDataAndType method, similarly to the checkType method in this class.
	*/
	static AcceptabilityFunction checkDataAndType(double minKeys, double maxKeys, double maxRange, long lastUpdate, 
				boolean nameMode, JSONArray names, boolean effectMode, JSONArray effects){
		return AcceptabilityFunction.checkDataAndType(minKeys, maxKeys, maxRange, lastUpdate, nameMode, parseNames(names), effectMode, parseEffects(effects));
	}

	/**Returns a ListingDescrptionFunction using the descrptionWithSayings method.
	@param placeBefore whether to place the saying before or after the simple description.
	@param sayings the sayings to use. Each element must be a String.
	@throws NullPointerException if sayings is null or any saying is null.
	@throws IllegalArgumentException if sayings is empty
	@return the described ListingDescriptionFunction
	*/
	static ListingDescriptionFunction descriptionWithSayings(boolean placeBefore, JSONArray sayings){
		return ListingDescriptionFunction.descriptionWithSayings(placeBefore, (String[])parseNames(sayings).toArray());
	} 

	private static Collection<String> parseNames(JSONArray names){
		List<String> parsedNames = null; 
		if(names != null){
			parsedNames = new ArrayList<>();
			for(int i = 0; i < names.length(); i++){
				parsedNames.add(names.getString(i));
			}
		}
		return parsedNames;
	}

	private static Collection<Effect> parseEffects(JSONArray effects){
		List<Effect> parsedEffects = null; 
		if(effects != null){
			parsedEffects = new ArrayList<>();
			for(int i = 0; i < effects.length(); i++){
				Object o = effects.get(i);
				if(o instanceof String){
					parsedEffects.add(Effect.forName((String)o));
				} else if(o instanceof Integer){
					parsedEffects.add(Effect.forInt((Integer)o));
				} else {
					throw new JSONException(o.toString() + " could not be parsed as an effect.");
				}
			}
		}
		return parsedEffects;
	}
}