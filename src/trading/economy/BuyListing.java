package trading.economy;

import org.json.*;
import java.io.*;
import trading.net.*;

//TODO:

/**Class representing a listing for an unusual hat that the bot wants to buy.
The prices of BuyListings are calculated via a BuyListingPriceFunction, a function which takes in Backpack.tf listings for the unusual hat, and returns a Price.
Upon initial construction, if a Price value is not provided by default, the price will be uninitialized. 
While the price is uninitialized, the BuyListing cannot be set to visible. The price can be initialized by calling the look() function.
*/

public class BuyListing extends Listing{
	private Price myPrice;
	private boolean visible;

	//Constructs a BuyListing from the given values.
	private BuyListing(String name, Effect effect, PriceRange communityPrice, Price currentPrice){
		super(name, effect, communityPrice);
		this.myPrice = currentPrice;
		this.considerVisibility();
	}

	/**Constructs a BuyListing from the given values.<br>
	When this constructor is used, the BuyListing's price will be uninitialized at first.
	@param name The name of the hat.
	@param effect The hat's unusual effect.
	@param communityPrice The hat's community price.
	*/
	public BuyListing(String name, Effect effect, PriceRange communityPrice){
		this(name, effect, communityPrice, null);
	}

	/**Returns a JSON representation of this BuyListing, suitable for storage and for use with the fromJSONRepresentation() method.
	@return JSON representation of this BuyListing
	*/
	public JSONObject getJSONRepresentation(){
		JSONObject j = new JSONObject();
		j.put("price", this.myPrice == null ? JSONObject.NULL : this.myPrice.getJSONRepresentation());
		j.put("name", this.getName());
		j.put("effect", this.getEffect().getIntValue());
		j.put("communityPrice", this.getCommunityPrice().getJSONRepresentation());
		j.put("priority", this.getPriority());
		return j;
	}

	/**Returns a JSON representation of this BuyListing which can be sent to Backpack.tf as a Listing object.
	@throws NonVisibleListingException if this Listing is not visible.
	@return a JSON representation of this BuyListing which can be sent to Backpack.tf as a Listing object.
	*/
	public JSONObject getListingRepresentation() throws NonVisibleListingException{
		if(!this.visible){
			throw new NonVisibleListingException();
		}
		JSONObject j = new JSONObject();
		j.put("currencies", this.myPrice.getJSONRepresentation());
		j.put("intent", 0);
		JSONObject item = new JSONObject();
		item.put("quality", 5);
		item.put("item_name", this.getName());
		item.put("priceindex", this.getEffect().getIntValue());
		j.put("item", item);
		return j;
	}

	/**Returns whether this BuyListing is visible.
	@return whether this BuyListing is visible.
	*/
	public boolean isVisible(){
		return this.visible;
	}

	/**Sets the bot's price for this BuyListing. A null value can be used to manually set the BuyListing to non-visible.
	@param newPrice the new price to use.
	*/
	public void setPrice(Price newPrice){
		this.myPrice = newPrice;
		this.considerVisibility();
	}

	/**Returns this BuyListing's current Price.
	@throws NonVisibleListingException if this BuyListing is non-visible and it prevents the price from being determined.
	@return this BuyListing's current Price.
	*/
	public Price getPrice() throws NonVisibleListingException {
		if(!this.visible){
			throw new NonVisibleListingException();
		}
		return this.myPrice;
	}

	/**Returns a deep copy of this BuyListing.
	@return a deep copy of this BuyListing.
	*/
	public BuyListing copy(){
		BuyListing bl = new BuyListing(this.getName(), this.getEffect(), this.getCommunityPrice(), this.myPrice);
		bl.setPriority(this.getPriority());
		return bl;
	}

	/**Constructs and returns a BuyListing from the given JSONObject.<br>
	JSONObjects returned by the getJSONRepresentation() method are compatible with this method.
	@param input the JSONObject to parse from.
	@throws NullPointerException if any parameter is null.
	@throws JSONException if the input is malformed.
	@return the constructed BuyListing.
	*/
	public static BuyListing fromJSONRepresentation(JSONObject input){
		String name = input.getString("name");
		Effect effect = Effect.forInt(input.getInt("effect"));
		PriceRange communityPrice = PriceRange.fromJSONRepresentation(input.getJSONObject("communityPrice"));
		Price myPrice = input.isNull("price") ? null : Price.fromJSONRepresentation(input.getJSONObject("price"));
		BuyListing bl = new BuyListing(name, effect, communityPrice, myPrice);
		bl.setPriority(input.has("priority") ? (Integer)input.getInt("priority") : null);
		return bl;
	}

	//Redetermines this hat's visibility.
	private void considerVisibility(){
		this.visible = this.myPrice != null;
	}

	/**Returns a String representation of this BuyListing.
	@return a String representation of this BuyListing.
	*/
	@Override
	public String toString(){
		return "trading.economy.BuyListing: " + this.getEffect().getName() + " " + this.getName();
	}
}