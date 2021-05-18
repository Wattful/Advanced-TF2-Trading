package trading.economy;

import java.util.*;
import java.io.IOException;
import org.json.*;

/**ListingCollection which sorts its listings by priority. While not technically a queue, it is helpful to think of this class as a queue.
*/

public class ListingPriorityQueue<E extends Listing> extends TreeSet<E> implements ListingCollection<E> {
	private static final Comparator<Listing> comparator = (Listing listing1, Listing listing2) -> {
		Integer priority1 = listing1.getPriority();
		Integer priority2 = listing2.getPriority();
		if(priority1 == null && priority2 == null){
			if(listing1 instanceof Hat != listing2 instanceof Hat){
				return listing1 instanceof Hat ? -1 : 1;
			} else {
				return 0;
			}
		} else if(priority1 == null || priority2 == null){
			return priority1 == null ? 1 : -1;
		}
		return priority1 - priority2;
	};

	/**Constructs a ListingPriorityQueue with no members.
	*/
	public ListingPriorityQueue(){
		super(comparator);
	}

	/**Constructs a ListingPriorityQueue with members from the given collection.
	@param coll the collection to use.
	@throws NullPointerException if coll is null.
	*/
	public ListingPriorityQueue(Collection<? extends E> coll){
		super(comparator);
		this.addAll(coll);
	}

	/**Returns a JSONArray representing all listings in this ListingPriorityQueue, suitable for storage and reconstruction.
	@return a JSONArray representing all listings in this ListingPriorityQueue.
	*/
	public JSONArray getJSONRepresentation(){
		JSONArray answer = new JSONArray();
		for(E entry : this){
			answer.put(entry.getJSONRepresentation());
		}
		return answer;
	}

	/**Returns a JSONArray representing all listings in this ListingPriorityQueue, suitable for sending to Backpack.tf.<br>
	Non-visible listings will not be included in the array.
	@param ldf Function to generate a descriptions for the listings. If ldf is null, listings will not have descriptions.
	@return a JSONArray representing all listings in this ListingPriorityQueue
	*/
	public JSONArray getListingRepresentation(ListingDescriptionFunction ldf){
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

	/**Returns a ListingPriorityQueue of Hat constructed from the given JSONArray of Hat JSON representations.
	@param input the Hat JSON array
	@throws NullPointerException if any parameter is null
	@throws JSONException if input is not a properly-formatted JSON array of Hat JSON representations.
	@return a ListingPriorityQueue of Hat constructed from the given JSONArray
	*/
	public static ListingPriorityQueue<Hat> hatQueueFromJSON(JSONArray input){
		ListingPriorityQueue<Hat> answer = new ListingPriorityQueue<Hat>();
		for(Object h : input){
			JSONObject j;
			try{
				j = (JSONObject)h;
				answer.add(Hat.fromJSONRepresentation(j));
			} catch (ClassCastException e){
				throw new JSONException("Not all entries in JSON array were JSON objects.", e);
			}
		}
		return answer;
	}

	/**Returns a ListingPriorityQueue of BuyListing constructed from the given JSONArray of Hat JSON representations.
	@param input the BuyListing JSON array
	@throws NullPointerException if input is null
	@throws JSONException if input is not a properly-formatted JSON array of BuyListing JSON representations.
	@return a ListingPriorityQueue of BuyListing constructed from the given JSONArray
	*/
	public static ListingPriorityQueue<BuyListing> buyListingQueueFromJSON(JSONArray input){
		ListingPriorityQueue<BuyListing> answer = new ListingPriorityQueue<BuyListing>();
		for(Object h : input){
			JSONObject j;
			try{
				j = (JSONObject)h;
				answer.add(BuyListing.fromJSONRepresentation(j));
			} catch (ClassCastException e){
				throw new JSONException("Not all entries in JSON array were JSON objects.", e);
			}
		}
		return answer;
	}

	/**Returns a deep copy of this ListingPriorityQueue.
	@return a deep copy of this ListingPriorityQueue.
	*/
	@SuppressWarnings("unchecked")
	public ListingPriorityQueue<E> copy(){
		ListingPriorityQueue<E> answer = new ListingPriorityQueue<>();
		for(E item : this){
			answer.add((E)item.copy());
		}
		return answer;
	}

	/**Returns a Listing in this ListingPriorityQueue which represents the same item as the given Item, or null if no such Listing exists.
	@param item The item to search for.
	@return a Listing in this Collection which represents the same item as the given Item.
	*/
	public E get(Item item){
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
	
	@Override
	/**Returns a String representation of this ListingPriorityQueue.
	@return a String representation of this ListingPriorityQueue.
	*/
	public String toString(){
		return this.getJSONRepresentation().toString();
	}
}