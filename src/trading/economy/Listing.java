package trading.economy;

import org.json.JSONObject;
import org.json.JSONException;
import trading.net.*;

//TODO: Consider renaming this class and its subclasses.

/**Abstract class representing a Listing for an unusual hat.<br>
Subclasses must override methods which provide a JSON representation for storage and a JSON representation for sending the listing to Backpack.tf.<br>
Subclasses should include a constructor or method which takes in a JSONObject and corresponds to its implementation of getJSONRepresentation().<br>
This class stores the community price of a hat as a PriceRange object. Subclasses must provide a method to return the bot's price for the listing.<br>
Listings can be marked as "non-visible", indicating that it is not ready to be sent as a listing, for reasons such as an uninitialized price or missing ID.<br>
Calling getListingRepresentation() or getPrice() on a non-visible listing will throw NonVisibleListingException.
Subclasses are responsible for tracking and setting visibility.
*/

public abstract /*sealed*/ class Listing extends Item /*permits BuyListing, Hat*/{
	private PriceRange range;

	/**Constructs a Listing with the given values.
	@param name the hat's name.
	@param effect the hat's effect.
	@param communityPrice the hat's community price.
	@throws NullPointerException if any parameter is null.
	*/
	public Listing(String name, Effect effect, PriceRange communityPrice){
		super(name, Quality.UNUSUAL, effect);
		this.range = communityPrice;
	}

	/**Returns this item's community price, as a PriceRange object.
	@return this item's community price.
	*/
	public PriceRange getCommunityPrice(){
		return this.range;
	}

	/**Updates this Listing's community price to the given value.
	@param price the community price.
	@throws NullPointerException if price is null.
	*/
	public final void changeCommunityPrice(PriceRange price){
		if(price == null){
			throw new NullPointerException();
		}
		this.range = price;
	}

	/**Returns a JSON representation containing all relevant values in the class, to be used for storage.<br>
	Subclasses are recommended to have a constructor or factory method which takes in a JSONObject corresponding to this method.
	@return the JSON representation.
	*/
	public abstract JSONObject getJSONRepresentation();

	/**Returns a JSONObject containing values relevant for a backpack.tf listing.
	@throws NonVisibleListingException if this Listing is not visible.
	@return the listing representation.
	*/
	public abstract JSONObject getListingRepresentation() throws NonVisibleListingException;

	/**Returns whether this Listing is visible.
	@return whether this Listing is visible.
	*/
	public abstract boolean isVisible();

	/**Sets the bot's price for this Listing. A null value can be used to manually set the listing to non-visible.
	@param newPrice the new price to use.
	*/
	public abstract void setPrice(Price newPrice);

	/**Returns a Price object representing the bot's price for this listing.
	@throws NonVisibleListingException if this lsiting is non-visible and that prevents a price from being determined. 
	A NVLE is not always thrown if the Listing is non-visible, only if it is non-visible for a reason that prevents its price from being calculated.
	@return a Price object representing the bot's price for this listing.
	*/
	public abstract Price getPrice() throws NonVisibleListingException;

	/**Returns a deep copy of this Listing.
	@return a deep copy of this Listing.
	*/
	public abstract Listing copy();

	/**Returns a String representation of this Listing.
	@return a String representation of this Listing.
	*/
	@Override
	public abstract String toString();
}