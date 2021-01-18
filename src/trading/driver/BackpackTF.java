package trading.driver;

import trading.net.*;
import trading.economy.*;
import org.json.*;
import java.io.*;

//TODO:

/**Class representing a connection to backpack.tf, which can be used for API calls.<br>
This class tracks a "used" property, in which the function hasBeenUsed() will return true if an API function has been called since the last resetUsed() call.<br>
In addition, for debug and logging purposes, this class will track the last IOException which it threw.
*/

class BackpackTF implements BackpackTFConnection{
	//private static final BackpackTF singleInstance = new BackpackTF(); //Not a singleton!

	private final String apiKey;
	private final String apiToken;
	private boolean used;
	private IOException lastThrown;
	private JSONObject pricesObject;

	private BackpackTF(String apiKey, String apiToken){
		if(apiKey == null || apiToken == null){
			throw new NullPointerException();
		}
		this.apiKey = apiKey;
		this.apiToken = apiToken;
		this.used = false;
		this.lastThrown = null;
	}

	/**Returns a connection to backpack.tf, using the given API credentials.
	@param apiKey a valid backpack.tf API key.
	@param apiToken a valid backpack.tf API token.
	@throws NullPointerExcpetion if any parameter is null.
	@return a connection to backpack.tf.
	*/
	static BackpackTF open(String apiKey, String apiToken){
		return new BackpackTF(apiKey, apiToken);
	}

	/**Resets this BackpackTF's used value to false.
	*/
	void resetUsed(){
		this.used = false;
	}

	/**Returns whether this BackpackTF has been used since the last call to resetUsed()
	@return whether this BackpackTF has been used.
	*/
	boolean hasBeenUsed(){
		return this.used;
	}

	//Sets used to true
	private void used(){
		this.used = true;
	}

	/**Resets the last thrown IOException to null.
	*/
	void resetIOException(){
		this.lastThrown = null;
	}

	/**Returns the last IOException that was thrown after the last call to resetIOException(), or null if no such IOException was thrown.
	@return the last thrown IOException.
	*/
	IOException lastThrownIOException(){
		return this.lastThrown;
	}

	/**Sends the given listings to backpack.tf.
	@param listings The listings to send.
	@param ldf ListingDescriptionFunction to generate descriptions for each listing.
	@throws NullPointerException if listings is null.
	@throws IOException if an IO error occurs or the request fails.
	*/
	public void sendListings(ListingCollection<? extends Listing> listings, ListingDescriptionFunction ldf) throws IOException {
		try{
			this.sendListingsInternal(listings, ldf);
		} catch(IOException e){
			this.lastThrown = e;
			throw e;
		}
	}

	private void sendListingsInternal(ListingCollection<? extends Listing> listings, ListingDescriptionFunction ldf) throws IOException {
		this.used();
		JSONArray toSend = listings.getListingRepresentation(ldf);
		JSONObject args = new JSONObject();
		args.put("token", this.apiToken);
		args.put("listings", toSend);
		JSONObject response = NetUtils.request("https://backpack.tf/api/classifieds/list/v1", "POST", args).getJSONObject("response");
		checkForError(response, "Send listings");
		//return response;
		/*try{
			if(response != null){
				write(response.toString(), "./report.json");
			}
		} catch(IOException e){
			System.out.println("Could not document response.");
		}*/
		/*JSONObject secondArgs = new JSONObject();
		secondArgs.put("token", this.apiToken);
		//request("https://backpack.tf/api/aux/heartbeat/v1", "POST", secondArgs);*/
	}

	/**Searches the backpack.tf listings for the given item and returns the result.<br>
	More specifically, it returns the result of a backpack.tf classifieds search API call as detailed at https://backpack.tf/api/docs/classifieds_search.
	@param i The item to search for.
	@throws NullPointerException if i is null.
	@throws IOException if an IO error occurs.
	@return the result of a backpack.tf classifieds search API call for the given item.
	*/
	public JSONObject getListingsForItem(Item i) throws IOException {
		try{
			return this.getListingsForItemInternal(i);
		} catch(IOException e){
			this.lastThrown = e;
			throw e;
		}
	}

	private JSONObject getListingsForItemInternal(Item i) throws IOException {
		this.used();
		JSONObject args = new JSONObject();
		args.put("key", this.apiKey);
		args.put("item", i.getName());
		if(i.getEffect() != null){
			args.put("particle", i.getEffect().getIntValue());
		}
		args.put("quality", i.getQuality().getIntValue());
		JSONObject response = NetUtils.request("https://backpack.tf/api/classifieds/search/v1", "GET", args).getJSONObject("response");
		checkForError(response, "Search listings");
		return response;
	}

	/**Returns the result of a backpack.tf get prices API call, as detailed at https://backpack.tf/api/index.html#/webapi-economy/App\Controllers\API\WebAPI\IGetPrices::v4
	@throws IOException if an IO error occurs or the request fails.
	@return the result of a backpack.tf get prices API call
	*/
	public JSONObject getPricesObject() throws IOException {
		try{
			return this.getPricesObjectInternal();
		} catch(IOException e){
			this.lastThrown = e;
			throw e;
		}
	}

	private JSONObject getPricesObjectInternal() throws IOException {
		this.used();
		JSONObject args = new JSONObject();
		args.put("key", this.apiKey);
		JSONObject response = NetUtils.request("https://backpack.tf/api/IGetPrices/v4", "GET", args);
		checkForError(response, "Get prices");
		this.pricesObject = response;
		return this.pricesObject;
	}

	private static void checkForError(JSONObject response, String name) throws IOException {
		if(response.getInt("success") == 0){
			throw new IOException(name + " API call failed with error message \"" + response.getString("message") + "\"");
		}
	}
}