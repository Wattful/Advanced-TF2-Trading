package trading;

import org.json.JSONObject;
import org.json.JSONArray;

public class SimpleItem{
	private String name;
	private String quality;
	private int priceIndex;
	private String id;

	public SimpleItem(JSONObject item){
		this(item.getString("market_name"), item.getJSONObject("app_data").getInt("quality"), item);
		id = item.getString("id");
	}

	public SimpleItem(JSONObject shortItem, JSONObject inventory){
		this(inventory.getJSONObject(shortItem.getString("classid") + "_" + shortItem.getString("instanceid")).getString("market_name"), Integer.parseInt(inventory.getJSONObject(shortItem.getString("classid") + "_" + shortItem.getString("instanceid")).getJSONObject("app_data").getString("quality")), inventory.getJSONObject(shortItem.getString("classid") + "_" + shortItem.getString("instanceid")));
		id = shortItem.getString("id");
	}

	private SimpleItem(String fullName, int qual, JSONObject item){
		if(qual == 6){
			quality = "Unique";
			if(fullName.substring(0, 3).equals("The")){
				name = fullName.substring(4);
			} else {
				name = fullName;
			}
			priceIndex = 0;
		} else if(qual == 5){
			quality = "Unusual";
			name = fullName.substring(8);
			priceIndex = parseEffect(item.getJSONArray("descriptions"));
		} else {
			quality = "Not Unusual";
			name = fullName;
			priceIndex = 0;
		}
	}

	private int parseEffect(JSONArray tags){
		JSONObject effects = Main.REFERENCE.getJSONObject("effects");
		for(Object o : tags){
			JSONObject j = (JSONObject)o;
			if(j.getString("color").equals("ffd700")){
				String tag = j.getString("value");
				return effects.getInt(tag.substring(tag.indexOf(":") + 2));
			}
		}
		return 0;
	}

	public boolean represents(Hat h){
		return name.equals(h.getName()) && priceIndex == h.getEffect();
	}

	public String getName(){
		return name;
	}

	public String getQuality(){
		return quality;
	}

	public int getPriceIndex(){
		return priceIndex;
	}

	public String getID(){
		return id;
	}

	public String getKey(){
		return name + "|" + priceIndex;
	}

	public String toString(){
		return quality + "," + name + "," + priceIndex + "," + id;
	}
}