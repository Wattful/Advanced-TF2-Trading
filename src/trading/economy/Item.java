package trading.economy;

//TODO:

/**Class representing an item, storing the item's name, quality, and effect if it has one.<br>
Item classes are immutable with respect to all values stored in Item, but subclasses may have their own mutable fields.
*/

public class Item{
	private final String name;
	private final Quality quality;
	private final Effect priceIndex;

	/**Constructs an Item from the given values, with no effect.
	@param fullName The item's name, with or without the quality prefix.
	@param quality The item's quality.
	@throws NullPointerException if any paramter is null.
	@throws IllegalArgumentException if quality is unusual (as an effect is not specified).
	*/
	public Item(String fullName, Quality quality){
		this(fullName, quality, null);
	}

	/**Constructs an Item from the given values.
	@param fullName The item's name, with or without the effect prefix.
	@param quality The item's quality.
	@param effect If the item is Unusual, its unusual effect. Cannot be null if item is unusual. Ignored if not unusual.
	@throws NullPointerException if fullName or quality is null.
	*/
	public Item(String fullName, Quality quality, Effect effect){
		if(fullName == null || quality == null || (quality == Quality.UNUSUAL && effect == null)){
			throw new NullPointerException();
		}

		this.quality = quality;
		this.priceIndex = quality == Quality.UNUSUAL ? effect : null;
		this.name = this.quality.removePrefix(fullName);
	}

	/**Returns the name of the item, without the quality or "The" before it.
	@return the name of the item.
	*/
	public final String getName(){
		return this.name;
	}

	/**Returns this item's effect, if it has one, otherwise returns null.
	@return the item's effect.
	*/
	public final Effect getEffect(){
		return this.priceIndex;
	}

	/**Returns this item's quality.
	@return this item's quality.
	*/
	public final Quality getQuality(){
		return this.quality;
	}

	/**Returns a hash code for this Item.
	@return a hash code for this Item.
	*/
	@Override
	public final int hashCode(){
		return this.getName().hashCode() + (this.getEffect() == null ? 0 : this.getEffect().hashCode()) + this.getQuality().hashCode();
	}

	/**Returns a boolean indicating whether the given Object is equal to this Item.<br>
	This implementation will return true if the given Object is also an Item and has the same name, effect and quality.
	@param o The object to compare to
	@return a boolean comparing the objects.
	*/
	@Override
	public final boolean equals(Object o){
		if(this == o){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof Item)){
			return false;
		}
		Item uh = (Item)o;
		return this.getName().equals(uh.getName()) && this.getEffect() == uh.getEffect() && this.getQuality().equals(uh.getQuality());
	}

	/**Returns a String representation of this Item.
	@return a String representation of this Item.
	*/
	@Override
	public String toString(){
		String nameString = "trading.economy.Item: " + this.quality.getName() + " " + this.name;
		if(this.priceIndex == null){
			return nameString;
		} else {
			return nameString + " (effect: " + this.priceIndex.getName() + ")";
		}
	}
}