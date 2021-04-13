package trading.driver;

import trading.net.*;
import trading.economy.*;
import org.json.*;
import java.io.*;

//TODO:

/**BackpackTFConnection which never sends listings to Backpack.tf. Used for testing purposes.
*/

class NoListingsBackpackTF implements LoggingBackpackTFConnection{
	private final BackpackTF connection;

	private NoListingsBackpackTF(BackpackTF conn){
		this.connection = conn;
	}

	/**Opens a NoListingsBackpackTF with the given API key and API token.
	@param apiKey a valid backpack.tf API key.
	@param apiToken a valid backpack.tf API token.
	@param fallback optional path to fallback file to read from and write to if a Backpack.tf prices request fails.
	@throws NullPointerExcpetion if apiKey or apiToken is null.
	@return a connection to backpack.tf.
	*/
	static NoListingsBackpackTF open(String apiKey, String apiToken, String fallback){
		return new NoListingsBackpackTF(BackpackTF.open(apiKey, apiToken, fallback));
	}

	/**Resets this BackpackTF's used value to false.
	*/
	public void resetUsed(){
		this.connection.resetUsed();
	}

	/**Returns whether this BackpackTF has been used since the last call to resetUsed()
	@return whether this BackpackTF has been used.
	*/
	public boolean hasBeenUsed(){
		return this.connection.hasBeenUsed();
	}

	/**Resets the last thrown IOException to null.
	*/
	public void resetIOException(){
		this.connection.resetIOException();
	}

	/**Returns the last IOException that was thrown after the last call to resetIOException(), or null if no such IOException was thrown.
	@return the last thrown IOException.
	*/
	public IOException lastThrownIOException(){
		return this.connection.lastThrownIOException();
	}

	/**Does nothing.
	*/
	public void sendListings(ListingCollection<? extends Listing> listings, ListingDescriptionFunction ldf){}

	/**Sends a heartbeat API call to Backpack.tf.
	@throws IOException if an IO error occurs.
	*/
	public void heartbeat() throws IOException {
		this.connection.heartbeat();
	}

	/**Searches the backpack.tf listings for the given item and returns the result.<br>
	More specifically, it returns the result of a backpack.tf classifieds search API call as detailed at https://backpack.tf/api/docs/classifieds_search.
	@param i The item to search for.
	@throws NullPointerException if i is null.
	@throws IOException if an IO error occurs.
	@return the result of a backpack.tf classifieds search API call for the given item.
	*/
	public JSONObject getListingsForItem(Item i) throws IOException {
		return this.connection.getListingsForItem(i);
	}

	/**Returns the result of a backpack.tf get prices API call, as detailed at https://backpack.tf/api/index.html#/webapi-economy/App\Controllers\API\WebAPI\IGetPrices::v4
	@throws IOException if an IO error occurs or the request fails.
	@return the result of a backpack.tf get prices API call
	*/
	public JSONObject getPricesObject() throws IOException {
		return this.connection.getPricesObject();
	}
}