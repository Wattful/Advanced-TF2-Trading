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
	
	@Override
	/**Returns a String representation of this ListingHashSet.
	@return a String representation of this ListingHashSet.
	*/
	public String toString(){
		return this.getJSONRepresentation().toString();
	}
}