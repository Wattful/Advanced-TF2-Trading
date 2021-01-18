package trading.economy;

import org.json.*;

//TODO: 

/**Class representing all that needs to be known about an item for making trades, including the item's name, quality, effect, and ID.
*/

public class InventoryItem extends Item{
	private final String id;

	/**Constructs a InventoryItem from the given values.
	@param fullName The item's name, with or without the effect prefix.
	@param quality The item's quality.
	@param effect The item's unusual effect. Ignored if item is not unusual.
	@param id The item's id.
	@throws NullPointerException if any paramter other than effect is null.
	@throws IllegalArgumentException if quality is Unusual and effect is null.
	*/
	public InventoryItem(String fullName, Quality quality, Effect effect, String id){
		super(fullName, quality, effect);
		if(id == null){
			throw new NullPointerException();
		}
		this.id = id;
	}

	/**Returns this InventoryItem's steam item ID.
	@return this InventoryItem's steam item ID.
	*/
	public String getID(){
		return this.id;
	}

	/**Parses and returns an item's unusual effect given its Steam description tags.
	@param tags the Steam description tags object.
	@throws NullPointerException if tags is null.
	@throws JSONException if tags is malformed.
	@return the item's effect, or null if the item is not unusual.
	*/
	public static Effect parseEffect(JSONArray tags){
		for(Object o : tags){
			JSONObject j = (JSONObject)o;
			if(j.getString("color").equals("ffd700")){
				String tag = j.getString("value");
				return Effect.forName(tag.substring(tag.indexOf(":") + 2));
			}
		}
		return null;
	}

	/**Returns a String representation of this InventoryItem.
	@return a String representation of this InventoryItem.
	*/
	@Override
	public String toString(){
		return super.toString().replace("trading.economy.Item", "trading.economy.InventoryItem") +  " (id: " + this.id + ")";
	}
}