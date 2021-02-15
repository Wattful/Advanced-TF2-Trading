package trading.economy;

import org.json.*;

//TODO: 

/**Static class containing utility methods for use with price functions.
*/

public class PriceFunctionUtils{
	/**Filters a set of listings by removing listings made by the given user.<br>
	It is suggested that all BuyListingPriceFunctions call this function to remove listings created by the bot, to avoid the bot's own listings from influencing its price.<br>
	This method returns nothing, the removal is done in-place.
	@param listings this listings to consider. Must be the result of a Backpack.tf listings search API call.
	@param userid the user id to remove listings of.
	@throws JSONExcpetion if listings is malformed.
	@throws NullPointerException if any parameter is null.
	*/
	public static void removeListingsFromUser(JSONObject listings, String userid){
		if(userid == null) {
			throw new NullPointerException();
		}
		JSONArray buyListings = listings.getJSONObject("buy").getJSONArray("listings");
		JSONArray sellListings = listings.getJSONObject("sell").getJSONArray("listings");
		for(int i = 0; i < buyListings.length(); i++){
			if(buyListings.getJSONObject(i).getString("steamid").equals(userid)){
				buyListings.remove(i);
				i--;
			}
		}
		for(int i = 0; i < sellListings.length(); i++){
			if(sellListings.getJSONObject(i).getString("steamid").equals(userid)){
				sellListings.remove(i);
				i--;
			}
		}
	}

	/**Filters a set of listings by removing any listings which do not specifically list an unusual effect.<br>
	This function is useful because Backpack.tf will return buy listings made for a "generic" unusual hat without effect, which this function will remove.
	@param listings this listings to consider. Must be the result of a Backpack.tf listings search API call.
	@throws JSONExcpetion if listings is malformed.
	@throws NullPointerException if any parameter is null.
	*/
	public static void removeListingsWithoutUnusualEffect(JSONObject listings){
		JSONArray buyListings = listings.getJSONObject("buy").getJSONArray("listings");
		JSONArray sellListings = listings.getJSONObject("sell").getJSONArray("listings");
		for(int i = 0; i < buyListings.length(); i++){
			if(!buyListings.getJSONObject(i).getJSONObject("item").has("attributes")){
				buyListings.remove(i);
				i--;
			}
		}
		for(int i = 0; i < sellListings.length(); i++){
			if(!sellListings.getJSONObject(i).getJSONObject("item").has("attributes")){
				sellListings.remove(i);
				i--;
			}
		}
	}
}