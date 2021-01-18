package trading.economy;

import java.util.*;
import java.io.IOException;
import org.json.*;

//TODO:

/**Sublass of HashSet which implements the ListingCollection interface.
*/

public class ListingHashSet<E extends Listing> extends HashSet<E> implements ListingCollection<E>{
	/**Constructs a ListingHashSet with no members.
	*/
	public ListingHashSet(){
		super();
	}

	/**Constructs a ListingHashSet with members from the given collection.
	@param coll the collection to use.
	@throws NullPointerException if coll is null.
	*/
	public ListingHashSet(Collection<? extends E> coll){
		super(coll);
	}

	/**Returns a JSONArray representing all listings in this ListingHashSet, suitable for storage and reconstruction.
	@return a JSONArray representing all listings in this ListingHashSet.
	*/
	public JSONArray getJSONRepresentation(){
		JSONArray answer = new JSONArray();
		for(E entry : this){
			answer.put(entry.getJSONRepresentation());
		}
		return answer;
	}

	/**Returns a JSONArray representing all listings in this ListingHashSet, suitable for sending to Backpack.tf.
	@param ldf Function to generate a descriptions for the listings. If ldf is null, listings will not have descriptions.
	@return a JSONArray representing all listings in this ListingHashSet
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

	@Override
	/**Returns a String representation of this ListingHashSet.
	@return a String representation of this ListingHashSet.
	*/
	public String toString(){
		return this.getJSONRepresentation().toString();
	}

	/**Returns a ListingHashSet of Hat constructed from the given JSONArray of Hat JSON representations.
	@param input the Hat JSON array
	@throws NullPointerException if any parameter is null
	@throws JSONException if input is not a properly-formatted JSON array of Hat JSON representations.
	@return a ListingHashSet of Hat constructed from the given JSONArray
	*/
	public static ListingHashSet<Hat> hatSetFromJSON(JSONArray input){
		ListingHashSet<Hat> answer = new ListingHashSet<Hat>();
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

	/**Returns a ListingHashSet of BuyListing constructed from the given JSONArray of Hat JSON representations.
	@param input the BuyListing JSON array
	@throws NullPointerException if input is null
	@throws JSONException if input is not a properly-formatted JSON array of BuyListing JSON representations.
	@return a ListingHashSet of BuyListing constructed from the given JSONArray
	*/
	public static ListingHashSet<BuyListing> buyListingSetFromJSON(JSONArray input){
		ListingHashSet<BuyListing> answer = new ListingHashSet<BuyListing>();
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

	/**Returns a deep copy of this ListingHashSet.
	@return a deep copy of this ListingHashSet.
	*/
	@SuppressWarnings("unchecked")
	public ListingHashSet<E> copy(){
		ListingHashSet<E> answer = new ListingHashSet<>();
		for(E item : this){
			answer.add((E)item.copy());
		}
		return answer;
	}

	/**Returns a Listing in this ListingHashSet which represents the same item as the given Item, or null if no such Listing exists.
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
}