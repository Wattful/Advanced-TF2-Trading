package trading.economy;

import org.json.*;
import java.util.*;

//TODO: Consider returning a map from inventory item to evaluation.

/**Class representing a trade offer.<br> 
This class, upon construction, will calculate whether the trade offer should be accepted, declined, or held according to provided prices.<br>
An instance of this class can be obtained by using the fromJSON method.
*/

public class TradeOffer{
	private final TradeOfferResponse response;
	private final Collection<InventoryItem> itemsToGive;
	private final Collection<InventoryItem> itemsToReceive;
	private final int ourValue;
	private final int theirValue;
	private final String partnerID;
	private final String data;

	private TradeOffer(TradeOfferResponse tor, Collection<InventoryItem> itemsToGive, Collection<InventoryItem> itemsToReceive, int ourValue, int theirValue, String partner, String data){
		this.response = tor;
		this.itemsToGive = itemsToGive;
		this.itemsToReceive = itemsToReceive;
		this.ourValue = ourValue;
		this.theirValue = theirValue;
		this.partnerID = partner;
		this.data = data;
	}

	/**Returns the calculated trade offer response.
	@return the calculated trade offer response.
	*/
	public TradeOfferResponse getResponse(){
		return this.response;
	} 

	/**Returns the items to give in the trade, as a set of InventoryItems.
	@return the items to give in the trade.
	*/
	public Set<InventoryItem> itemsToGive(){
		return Set.copyOf(this.itemsToGive);
	}

	/**Returns the items to receive in the trade, as a set of InventoryItems.
	@return the items to receive in the trade.
	*/
	public Set<InventoryItem> itemsToReceive(){
		return Set.copyOf(this.itemsToReceive);
	}

	/**Returns our value in the trade.
	@return our value in the trade.
	*/
	public int getOurValue(){
		return this.ourValue;
	}

	/**Returns their value in the trade.
	@return their value in the trade.
	*/
	public int getTheirValue(){
		return this.theirValue;
	}

	/**Returns the ID of the partner in this trade.
	@return the ID of the partner in this trade.
	*/
	public String getPartner(){
		return this.partnerID;
	}

	/**Returns a String containing a detailed representation of this TradeOffer. This detailed represenation will contain:<br>
	<ul>
		<li>The ID of the trade offer's partner.</li>
		<li>List of items to give and items to receive in the trade, along with each item's ID and its evaluation in total scrap.</li>
		<li>The total evaluations of both sides of the trade.</li>
		<li>The recommended response to the trade, and why this recommendation was made.</li>
	</ul>
	@return the detailed representation.
	*/
	public String getData(){
		return this.data;
	}

	/**Constructs, evaluates, and returns a TradeOffer from the given data.<br>
	This function is equivalent to calling the more complicated fromJSON function with a forgiveness of 0, canHold = true, and ownerIDs = null.
	@param offer the JSONObject represenation of the offer.
	@param hatPrices prices for the Bot's items.
	@param buyListingPrices prices for the items that the Bot wants to buy.
	@param keyScrapRatio the key-to-scrap ratio to use for this calculation.
	@throws NullPointerException if any parameter is null.
	@throws IllegalArgumentException if keyScrapRatio is non-positive.
	@throws JSONException if offer is malformed.
	@return the constructed and evaluated TradeOffer.
	*/
	static TradeOffer fromJSON(JSONObject offer, ListingCollection<Hat> hatPrices, ListingCollection<BuyListing> buyListingPrices, int keyScrapRatio){
		return fromJSON(offer, hatPrices, buyListingPrices, keyScrapRatio, 0.0, true, null);
	}

	/**Constructs, evaluates, and returns a TradeOffer from the given data.<br>
	In order to evaluate the trade offer, this method will do the following:<br>
	First, it will go through each item in the trade, and sum each side's total value, taking into consideration the given hatPrices and buyListingPrices objects.<br>
	More precisely, for each item in the trade it will do the following:
	<ol>
		<li>If the item is a currency (ie keys or metal), the item will be evaluated at the value of that currency.</li>
		<li>Otherwise, the method will check the given hatPrices and buyListingPrices for the item.
		items to give are evaluated using hatPrices, and items to receive are evaluated using buyListingPrices.</li>
		<li>If the item is not a currency and it does not appear in the prices objects, then the item's price cannot be determined. The offer now becomes an item offer (see below). 
		If the item is in items to receive, then the item is valued at 0. If the item is in items to give, the item is valued effectively infinitely. This is to prevent the bot from accidently trading away items.</li>
	</ol>
	Once this is finished, the result is determined as follows:
	<ul>
		<li>If the offerer's ID is in ownerIDs, evaluated as TradeOfferResponse.ACCEPT.</li>
		<li>Otherwise, if (theirValue * (1 - forgiveness)) <= ourValue, evaluated as TradeOfferResponse.ACCEPT.</li>
		<li>Otherwise, if the offer is an item offer, and canHold is true, evaluated as TradeOfferResponse.HOLD.</li>
		<li>Otherwise, evaluated as TradeOfferResponse.DECLINE.</li>
	</ul>
	@param offer the JSONObject represenation of the offer.
	@param hatPrices prices for the Bot's items.
	@param buyListingPrices prices for the items that the Bot wants to buy.
	@param keyScrapRatio the key-to-scrap ratio to use for this calculation.
	@param forgiveness a value to scale down the other person's price by. This is to prevent the declining of trades due to an insignificant price difference. Must be between 0 and 1.
	@param canHold whether the TradeOffer can be evaluated as TradeOfferResponse.HOLD.
	@param ownerIDs a list of Steam IDs to always accept offers from. This can be used to, for example, always accept offers from the bot's owner. null indicates no IDs to automatically accept.
	@throws NullPointerException if any parameter other than ownerIDs is null.
	@throws IllegalArgumentException if preconditions on forgiveness are violated, or if keyScrapRatio is non-positive.
	@throws JSONException if offer is malformed.
	@return the constructed and evaluated TradeOffer.
	*/
	static TradeOffer fromJSON(JSONObject offer, ListingCollection<Hat> hatPrices, ListingCollection<BuyListing> buyListingPrices, int keyScrapRatio, double forgiveness, boolean canHold, List<String> ownerIDs){
		return evaluate(offer, hatPrices, buyListingPrices, keyScrapRatio, forgiveness, canHold, ownerIDs);
	}

	private static TradeOffer evaluate(JSONObject offer, ListingCollection<Hat> hatPrices, ListingCollection<BuyListing> buyListingPrices, int keyScrapRatio, double forgiveness, boolean canHold, List<String> ownerIDs){
		if(hatPrices == null || buyListingPrices == null){
			throw new NullPointerException();
		}
		if(Double.isNaN(forgiveness) || forgiveness < 0 || forgiveness > 1){
			throw new IllegalArgumentException("Invalid forgiveness value: " + forgiveness);
		}
		if(keyScrapRatio <= 0){
			throw new IllegalArgumentException("keyScrapRatio was non-positive");
		}

		JSONArray ourItems = offer.getJSONArray("itemsToGive");
		JSONArray theirItems = offer.getJSONArray("itemsToReceive");
		int ourValue = 0;
		int theirValue = 0;
		boolean isItem = false;
		StringBuffer data = new StringBuffer();
		List<InventoryItem> ourInventoryItems = new ArrayList<>();
		List<InventoryItem> theirInventoryItems = new ArrayList<>();

		data.append("Our items include:");
		for(Object o : ourItems){
			data.append("\n");
			JSONObject j = (JSONObject)o;
			InventoryItem item = fromTradeOfferItem(j);
			ourInventoryItems.add(item);
			int value = evaluateItem(item, hatPrices, keyScrapRatio);

			if(value != 0){
				data.append(item.toString() + ", valued at " + value);
				ourValue += value;
			} else {
				data.append(item.toString() + ", not in our pricelist");
				ourValue = Integer.MAX_VALUE/2;
			}
		}

		data.append("\n\nTheir items include:");
		for(Object o : theirItems){
			data.append("\n");
			JSONObject j = (JSONObject)o;
			InventoryItem item = fromTradeOfferItem(j);
			theirInventoryItems.add(item);
			int value = evaluateItem(item, buyListingPrices, keyScrapRatio);

			if(value != 0){
				data.append(item.toString() + ", valued at " + value);
				theirValue += value;
			} else {
				data.append(item.toString() + ", not in our pricelist");
				isItem = true;
			}
		}

		String totals = "Our value: " + ourValue + " Their value: " + theirValue;
		data.append("\n\n" + totals);
		String partner = offer.getString("partner");

		if(ownerIDs != null && ownerIDs.contains(offer.getString("partner"))){
			data.append("The offer was accepted because " + offer.getString("partner") + " is an owner");
			return new TradeOffer(TradeOfferResponse.ACCEPT, ourInventoryItems, theirInventoryItems, ourValue, theirValue, partner, data.toString());
		} else if(ourValue * (1 - forgiveness) <= theirValue){
			data.append("\n\nThe offer with " + partner + " was accepted because our value was less than or equal to their value.");
			return new TradeOffer(TradeOfferResponse.ACCEPT, ourInventoryItems, theirInventoryItems, ourValue, theirValue, partner, data.toString());
		} else if(isItem && canHold){
			data.append("\n\nThe offer with " + partner + " was held because it was an item offer.");
			return new TradeOffer(TradeOfferResponse.HOLD, ourInventoryItems, theirInventoryItems, ourValue, theirValue, partner, data.toString());
		} else {
			data.append("\n\nThe offer with " + partner + " was declined because our value was greater than their value.");
			return new TradeOffer(TradeOfferResponse.DECLINE, ourInventoryItems, theirInventoryItems, ourValue, theirValue, partner, data.toString());
		}
	}

	private static int evaluateItem(InventoryItem item, ListingCollection<? extends Listing> set, int keyScrapRatio){
		try{
			return set.get(item).getPrice().getScrapValue(keyScrapRatio);
		} catch(NullPointerException e){
			String n = item.getName();
			if(n.equals("Mann Co. Supply Crate Key")){return keyScrapRatio;}
			else if(n.equals("Refined Metal")){return 9;}
			else if(n.equals("Reclaimed Metal")){return 3;}
			else if(n.equals("Scrap Metal")){return 1;}
		} catch(NonVisibleListingException e){
			return 0;
		}
		return 0;
	}

	/**Constructs and returns a InventoryItem from the given JSONObject, where the JSONObject is from a Steam trade offer API call.
	@param item an item JSONObject from a Steam trade offer API call.
	@throws NullPointerException if item is null
	@throws JSONException if item is not an item JSONObject from a Steam trade offer API call.
	@return the InventoryItem.
	*/
	private static InventoryItem fromTradeOfferItem(JSONObject item){
		String name = item.getString("market_name");
		Quality quality = Quality.forInt(item.getJSONObject("app_data").getInt("quality"));
		Effect effect = InventoryItem.parseEffect(item.getJSONArray("descriptions"));
		String id = item.getString("id");
		return new InventoryItem(name, quality, effect, id);
	}

	/**Returns a hash code for this TradeOffer.
	@return a hash code for this TradeOffer.
	*/
	@Override
	public int hashCode(){
		return this.data.hashCode();
	}

	/**Returns a boolean indicating whether this TradeOffer is equal to the given Object.<br>
	This will return true if and only if the given Object is a TradeOffer with the same items and evaluation.
	@param o the Object to compare to
	@return a boolean indicating whether this TradeOffer is equal to the given Object.
	*/
	@Override
	public boolean equals(Object o){
		if(this == o){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof TradeOffer)){
			return false;
		}
		TradeOffer to = (TradeOffer)o;
		return this.data.equals(to.data);
	}

	/**Returns a non-detailed String representation of this TradeOffer.
	@return a non-detailed String representation of this TradeOffer.
	*/
	@Override
	public String toString(){
		return "trading.economy.TradeOffer: Offer from " + this.partnerID + ", response: " + this.response.toString(); 
	}
}