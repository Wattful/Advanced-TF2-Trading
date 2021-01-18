package trading.net;

import java.io.*;
import org.json.*;
import trading.economy.*;

//TODO: Consider a heartbeat method

/**Interface representing a connection to Backpack.tf. Implementing classes must implement several methods which return Backpack.tf data.
*/

public interface BackpackTFConnection{
	/**Sends the given listings to backpack.tf.
	@param listings The listings to send.
	@param ldf ListingDescriptionFunction to generate descriptions for each listing.
	@throws NullPointerException if listings is null.
	@throws IOException if an IO error occurs.
	*/
	void sendListings(ListingCollection<? extends Listing> listings, ListingDescriptionFunction ldf) throws IOException;

	/**Searches the backpack.tf listings for the given item and returns the result.<br>
	More specifically, it returns the result of a backpack.tf classifieds search API call as detailed at https://backpack.tf/api/docs/classifieds_search.
	@param i The item to search for.
	@throws NullPointerException if i is null.
	@throws IOException if an IO error occurs.
	@return the result of a backpack.tf classifieds search API call for the given item.
	*/
	JSONObject getListingsForItem(Item i) throws IOException;

	/**Returns the result of a backpack.tf get prices API call, as detailed at https://backpack.tf/api/index.html#/webapi-economy/App\Controllers\API\WebAPI\IGetPrices::v4
	@throws IOException if an IO error occurs.
	@return the result of a backpack.tf get prices API call
	*/
	JSONObject getPricesObject() throws IOException;
}