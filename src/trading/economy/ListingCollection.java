package trading.economy;

import org.json.*;
import java.util.Collection;

//TODO:

/**Collection which provides extra functionality for subclasses of Listing.
*/

public interface ListingCollection<E extends Listing> extends Collection<E>{
	/**Returns a JSONArray representing all listings in this ListingCollection, suitable for storage and reconstruction.
	@return a JSONArray representing all listings in this ListingCollection.
	*/
	default JSONArray getJSONRepresentation(){
		JSONArray answer = new JSONArray();
		for(E entry : this){
			answer.put(entry.getJSONRepresentation());
		}
		return answer;
	}

	/**Returns a JSONArray representing all listings in this ListingCollection, suitable for sending to Backpack.tf.
	@param ldf a ListingDescriptionFunction to pass to all listings in this ListingCollection.
	@return a JSONArray representing all listings in this ListingCollection
	*/
	default JSONArray getListingRepresentation(ListingDescriptionFunction ldf){
		JSONArray answer = new JSONArray();
		for(E entry : this){
			try{
				JSONObject obj = entry.getListingRepresentation();
				if(ldf != null){
					obj.put("details", ldf.generateDescription(entry));
				}
				answer.put(obj);
			} catch(NonVisibleListingException e){}
		}
		return answer;
	}

	/**Performs a deep copy of this ListingCollection.
	@return the copied collection.
	*/
	@SuppressWarnings("unchecked")
	default ListingHashSet<E> copy(){
		ListingHashSet<E> answer = new ListingHashSet<>();
		for(E item : this){
			answer.add((E)item.copy());
		}
		return answer;
	}

	/**Returns a Listing in this Collection which represents the same item as the given Item, or null if no such Listing exists.
	@param item The item to search for.
	@return a Listing in this Collection which represents the same item as the given Item.
	*/
	default E get(Item item){
		if(item == null){
			return null;
		}
		//O(n) ugly
		for(E t : this){
			if(item.equals(t)){
				return t;
			}
		}
		return null;
	}
}