package trading.economy;

import org.json.*;
import java.io.*;
import java.time.*;
import trading.net.*;

//TODO:

/**Class representing an unusual hat in the bot's inventory, as well as a sell listing for that hat.<br>
This class stores the date that the hat was bought on, which can be accessed by the getDateBought() method.<br>
Users can provide a HatPriceFunction at construction-time. This function calculates the hat's price given the hat.<br>
This class provides a default HatPriceFunction, which can be retrieved using the defaultPriceFunction() method.<br>
Initally, the program will not know the hat's ID. During this time, the Hat will be non-visible. This can be remedied using the SetID method.
*/

public class Hat extends Listing{
	private String id;
	//private int age;
	private final LocalDate dateBought;
	private final Price boughtAt;
	private Price myPrice;
	private boolean visible;

	/**Constructs a Hat from the given values.
	@param name The hat's name
	@param effect The hat's effect
	@param communityPrice The hat's community price
	@param purchasePrice The price the hat was bought at
	@param id the hat's ID. Can be null, in which case the hat will be non-visible.
	@param dateBought The date the hat was bought
	@throws NullPointerException if any parameter except id is null.
	*/
	public Hat(String name, Effect effect, PriceRange communityPrice, Price purchasePrice, String id, LocalDate dateBought){
		super(name, effect, communityPrice);
		if(purchasePrice == null || dateBought == null){
			throw new NullPointerException();
		}
		this.boughtAt = purchasePrice;
		this.id = id;
		this.dateBought = dateBought;
		this.considerVisibility();
	}

	/**Constructs a Hat from the given values.
	@param name The hat's name
	@param effect The hat's effect
	@param communityPrice The hat's community price
	@param purchasePrice The price the hat was bought at
	@param dateBought The date the hat was bought
	@throws NullPointerException if any parameter is null.
	*/
	public Hat(String name, Effect effect, PriceRange communityPrice, Price purchasePrice, LocalDate dateBought){
		this(name, effect, communityPrice, purchasePrice, null, dateBought);
	}

	/**Returns a JSON Object representation of this Hat, suitable for storage and use with the fromJSONRepresentation() method.
	@return JSON Object representation of this Hat
	*/
	public JSONObject getJSONRepresentation(){
		JSONObject j = new JSONObject();
		//j.put("age", this.age);
		j.put("dateBought", this.dateBought.toString());
		j.put("id", this.id == null ? JSONObject.NULL : this.id);
		j.put("price", this.myPrice == null ? JSONObject.NULL : this.myPrice.getJSONRepresentation());
		j.put("boughtAt", this.boughtAt.getJSONRepresentation());
		j.put("name", this.getName());
		j.put("effect", this.getEffect().getIntValue());
		j.put("communityPrice", this.getCommunityPrice().getJSONRepresentation());
		j.put("priority", this.getPriority());
		return j;
	}

	/**Returns a JSON Object representation of this Hat, suitable for sending to Backpack.tf as a listing.
	@throws NonVisibleListingException if this Listing is not visible.
	@return the Backpack.tf listing representation of this hat.
	*/
	public JSONObject getListingRepresentation() throws NonVisibleListingException {
		if(!this.visible){
			throw new NonVisibleListingException();
		}
		JSONObject j = new JSONObject();
		j.put("currencies", this.myPrice.getJSONRepresentation());
		j.put("intent", 1);
		j.put("id", id);
		j.put("promoted", 1);
		return j;
	}

	/**Returns whether this Hat is visible.
	@return whether this Hat is visible.
	*/
	public boolean isVisible(){
		return this.visible;
	}

	/**Sets this Hat's id to the given value.
	@param identification the id to set to
	@throws NullPointerException if identification is null.
	*/
	public void setID(String identification){
		this.id = identification;
		this.considerVisibility();
	}

	/**Returns this hat's ID, or null if the ID has not been set.
	@return this hat's ID, or null if the ID has not been set.
	*/
	public String getID(){
		return this.id;
	}

	/**Returns the date that this Hat was bought at.
	@return the date that this Hat was bought at.
	*/
	public LocalDate getDateBought(){
		return this.dateBought;
	}

	/**Sets the bot's price for this Hat. A null value can be used to manually set the Hat to non-visible.
	@param newPrice the new price to use.
	*/
	public void setPrice(Price newPrice){
		this.myPrice = newPrice;
		this.considerVisibility();
	}

	/**Returns this hat's price.
	@return this hat's price.
	*/
	public Price getPrice() throws NonVisibleListingException {
		if(!this.visible){
			throw new NonVisibleListingException();
		}
		return this.myPrice;
	}

	/**Returns a deep copy of this Hat.
	@return a deep copy of this Hat.
	*/
	public Hat copy(){
		Hat h = new Hat(this.getName(), this.getEffect(), this.getCommunityPrice(), this.boughtAt, this.id, this.dateBought);
		h.setPrice(this.myPrice);
		return h;
	}

	/**Returns the price that this Hat was purchased at.
	@return the price that this Hat was purchased at.
	*/
	public Price getPurchasePrice(){
		return this.boughtAt;
	}

	/**Constructs and returns a Hat from a Listing, with the ID and price uninitialized, and the purchase price set to the Listing's bot price.
	@param listing the Listing to construct from.
	@throws NullPointerException if any parameter is null.
	@throws NonVisibleListingException if the given listing is non-visible.
	@return the constructed Hat.
	*/
	public static Hat fromListing(Listing listing) throws NonVisibleListingException {
		return new Hat(listing.getName(), listing.getEffect(), listing.getCommunityPrice(), listing.getPrice(), null, LocalDate.now());
	}

	/**Constructs and returns a Hat from a JSONObject, with the ID being initialized.<br>
	JSON Objects returned from the getJSONRepresentation() method are compatible with this method.
	@param obj the JSON Object to construct from.
	@throws NullPointerException if any parameter is null.
	@throws JSONException if the JSON Object is malformed.
	@return the constructed Hat.
	*/
	public static Hat fromJSONRepresentation(JSONObject obj){
		String name = obj.getString("name");
		Effect effect = Effect.forInt(obj.getInt("effect"));
		PriceRange communityPrice = PriceRange.fromJSONRepresentation(obj.getJSONObject("communityPrice"));
		String id = obj.isNull("id") ? null : obj.getString("id");
		//int age = obj.getInt("age");
		LocalDate dateBought = LocalDate.parse(obj.getString("dateBought"));
		Price boughtAt = Price.fromJSONRepresentation(obj.getJSONObject("boughtAt"));
		Hat h = new Hat(name, effect, communityPrice, boughtAt, id, dateBought);
		if(!obj.isNull("price")) {
			h.setPrice(Price.fromJSONRepresentation(obj.getJSONObject("price")));
		}
		h.setPriority(obj.has("priority") ? (Integer)obj.getInt("priority") : null);
		return h;
	}

	//Redetermines this hat's visibility.
	private void considerVisibility(){
		this.visible = this.myPrice != null && this.id != null;
	}

	@Override
	/**Returns a String representation of this Hat.
	@return a String representation of this Hat.
	*/
	public String toString(){
		return "trading.economy.Hat: " + this.getEffect().getName() + " " + this.getName();
	}
}