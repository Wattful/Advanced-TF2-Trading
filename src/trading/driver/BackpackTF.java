package trading.driver;

import trading.net.*;
import trading.economy.*;
import org.json.*;
import java.io.*;
import java.nio.file.*;
import javax.imageio.IIOException;

import static trading.driver.FileUtils.*;

//TODO:

/**Class representing a connection to backpack.tf, which can be used for API calls.<br>
This class tracks a "used" property, in which the function hasBeenUsed() will return true if an API function has been called since the last resetUsed() call.<br>
In addition, for debug and logging purposes, this class will track the last IOException which it threw.
*/

class BackpackTF implements LoggingBackpackTFConnection{
	//private static final BackpackTF singleInstance = new BackpackTF(); //Not a singleton!

	private final String apiKey;
	private final String apiToken;
	private final String fallbackPath;
	private boolean used;
	private IOException lastThrown;
	private JSONObject pricesObject;

	private static final int LISTINGS_LIMIT = 50;

	private BackpackTF(String apiKey, String apiToken, String fallback){
		if(apiKey == null || apiToken == null){
			throw new NullPointerException();
		}
		this.apiKey = apiKey;
		this.apiToken = apiToken;
		this.fallbackPath = fallback;
		this.used = false;
		this.lastThrown = null;
	}

	/**Returns a connection to backpack.tf, using the given API credentials.
	@param apiKey a valid backpack.tf API key.
	@param apiToken a valid backpack.tf API token.
	@param fallback optional path to fallback file to read from and write to if a Backpack.tf prices request fails.
	@throws NullPointerExcpetion if apiKey or apiToken is null.
	@return a connection to backpack.tf.
	*/
	static BackpackTF open(String apiKey, String apiToken, String fallback){
		return new BackpackTF(apiKey, apiToken, fallback);
	}

	/**Resets this BackpackTF's used value to false.
	*/
	public void resetUsed(){
		this.used = false;
	}

	/**Returns whether this BackpackTF has been used since the last call to resetUsed()
	@return whether this BackpackTF has been used.
	*/
	public boolean hasBeenUsed(){
		return this.used;
	}

	//Sets used to true
	private void used(){
		this.used = true;
	}

	/**Resets the last thrown IOException to null.
	*/
	public void resetIOException(){
		this.lastThrown = null;
	}

	/**Returns the last IOException that was thrown after the last call to resetIOException(), or null if no such IOException was thrown.
	@return the last thrown IOException.
	*/
	public IOException lastThrownIOException(){
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
		int numberOfRequests = (int)Math.ceil(((double)toSend.length())/LISTINGS_LIMIT);
		for(int i = 0; i < numberOfRequests; i++){
			JSONObject args = new JSONObject();
			args.put("token", this.apiToken);
			JSONArray listingsToSend = new JSONArray();
			for(int j = i * LISTINGS_LIMIT; j < toSend.length() && j < (i + 1) * LISTINGS_LIMIT; j++){
				listingsToSend.put(toSend.getJSONObject(j));
			}
			args.put("listings", listingsToSend);
			JSONObject response = NetUtils.request("https://backpack.tf/api/classifieds/list/v1", "POST", args);
		}
		
		//checkForError(response, "Send listings");
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
		JSONObject data = NetUtils.request("https://backpack.tf/api/classifieds/search/v1", "GET", args);
		return data;
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
		JSONObject response;
		try{
			response = NetUtils.request("https://backpack.tf/api/IGetPrices/v4", "GET", args);
		} catch(IOException e){
			if(this.fallbackPath != null){
				this.lastThrown = e;
				return new JSONObject(readFile(this.fallbackPath));
			} else {
				throw e;
			}
		}
		if(this.fallbackPath != null){
			try{
				write(response.toString(), this.fallbackPath);
			} catch(IOException e){
				this.lastThrown = new IIOException("Failed to save fallback prices.", e);
			}
		}
		return response;
	}
}