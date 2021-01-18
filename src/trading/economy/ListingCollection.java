package trading.economy;

import org.json.*;
import java.util.Collection;

//TODO:

/**Collection which provides extra functionality for subclasses of Listing.
*/

public interface ListingCollection<T extends Listing> extends Collection<T>{
	/**Returns a JSONArray representing all listings in this ListingCollection, suitable for storage and reconstruction.
	@return a JSONArray representing all listings in this ListingCollection.
	*/
	JSONArray getJSONRepresentation();

	/**Returns a JSONArray representing all listings in this ListingCollection, suitable for sending to Backpack.tf.
	@param ldf a ListingDescriptionFunction to pass to all listings in this ListingCollection.
	@return a JSONArray representing all listings in this ListingCollection
	*/
	JSONArray getListingRepresentation(ListingDescriptionFunction ldf);

	/**Performs a deep copy of this ListingCollection.
	@return the copied collection.
	*/
	ListingCollection<T> copy();

	/**Returns a Listing in this Collection which represents the same item as the given Item, or null if no such Listing exists.
	@param item The item to search for.
	@return a Listing in this Collection which represents the same item as the given Item.
	*/
	T get(Item item);
}