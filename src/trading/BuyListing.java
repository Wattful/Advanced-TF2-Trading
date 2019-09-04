package trading;

import org.json.JSONObject;
import org.json.JSONException;
import java.util.Random;

public class BuyListing extends Listing{
	private static final int MAX_KEYS = 30;
	private static final double MAX_RANGE = 0;
	private static final long LAST_UPDATE = 31557600/2;
	private static final double MAX_BUY = .65;
	private static final double MIN_BUY = .5;
	private static final double DIFFERENCE_UP = 1.005;
	private static final int NUM_LISTINGS_LOOKED = 2;

	public BuyListing(String n, int e, double c){
		super(n, e, c);
	}

	public BuyListing(JSONObject j){
		super(j.getString("name"), j.getInt("effect"), j.getDouble("communityPrice"));
		multiplier = j.getDouble("multiplier");
		calculatePrice();
	}

	public BuyListing(JSONObject j, String n, int e){
		super(n, e, Main.average(j));
		multiplier = 0;
		calculatePrice();
	}

	public boolean equals(Object o){
		return o instanceof Listing && super.equals(o);
	}

	public JSONObject getJSONRepresentation(){
		JSONObject j = super.getJSONRepresentation();
		j.put("multiplier", multiplier);
		return j;
	}

	public JSONObject getListingRepresentation(){
		JSONObject j = super.getListingRepresentation();
		j.put("intent", 0);
		JSONObject item = new JSONObject();
		item.put("quality", 5);
		item.put("item_name", super.getName());
		item.put("priceindex", super.getEffect());
		j.put("item", item);
		String description = j.getString("details");
		j.remove("details");
		description = description.replace("=", "Buying");
		description += Main.SAYINGS[new Random().nextInt(Main.SAYINGS.length)];
		j.put("details", description);
		return j;
	}

	public double getMultiplier(){
		return multiplier;
	}

	//Goals: Set a price based on the average of the two highest listings that INCLUDE EFFECT. Do not include musk's listings.
	//If there is only one listing including effect, set the price to zero.
	//If there are no listings including effect, set multiplier to MIN_BUY.
	public void look(){
		JSONObject args = new JSONObject();
		args.put("key", Configuration.API_KEY);
		args.put("intent", "buy");
		args.put("item", super.getName());
		args.put("particle", super.getEffect());
		args.put("quality", 5);
		JSONObject data = Main.request("https://backpack.tf/api/classifieds/search/v1", "GET", args);
		if(data == null){
			multiplier = 0;
			super.calculatePrice();
			return;
		}
		//data.getJSONObject("buy").getJSONArray("listings").getJSONObject(2).put("steamid", Configuration.BOT_ID);

		int numListings = 0;
		for(int i = 0; true; i++){
			try{
				data.getJSONObject("buy").getJSONArray("listings").getJSONObject(i);
			} catch(JSONException e){
				break;
			}
			if(data.getJSONObject("buy").getJSONArray("listings").getJSONObject(i).getString("steamid") == Configuration.BOT_ID){
				continue;
			}
			if(!data.getJSONObject("buy").getJSONArray("listings").getJSONObject(i).getJSONObject("item").has("attributes")){
				break;
			}
			numListings++;
		}

		if(numListings == 0){
			multiplier = MIN_BUY;
			super.calculatePrice();
			return;
		} else if(numListings == 1){
			int offset = 0;
			if(data.getJSONObject("buy").getJSONArray("listings").getJSONObject(0).getString("steamid") == Configuration.BOT_ID){
				offset = 1;
			}
			int keys = data.getJSONObject("buy").getJSONArray("listings").getJSONObject(offset).getJSONObject("currencies").getInt("keys");
			int refined = data.getJSONObject("buy").getJSONArray("listings").getJSONObject(offset).getJSONObject("currencies").has("metal") ? data.getJSONObject("buy").getJSONArray("listings").getJSONObject(offset).getJSONObject("currencies").getInt("metal") : 0;
			Price otherListing = new Price(keys, refined);
			double otherMultiplier = otherListing.getDecimalPrice()/getCommunityPrice();
			multiplier = (otherMultiplier + MIN_BUY)/2.0;
			super.calculatePrice();
			return;
		}
		

		JSONObject[] listings = new JSONObject[NUM_LISTINGS_LOOKED];

		//Note: currently cannot compare more than two listings.
		int offset = 0;
		for(int i = 0; i < listings.length; i++){
			if(data.getJSONObject("buy").getJSONArray("listings").getJSONObject(i + offset).getString("steamid") == Configuration.BOT_ID){
				offset = 1;
				i--;
				continue;
			}
			listings[i] = data.getJSONObject("buy").getJSONArray("listings").getJSONObject(offset + i);
		}

		Price[] prices = new Price[listings.length];

		for(int i = 0; i < listings.length; i++){
			int keys = listings[i].getJSONObject("currencies").getInt("keys");
			int refined = listings[i].getJSONObject("currencies").has("metal") ? listings[i].getJSONObject("currencies").getInt("metal") : 0;
			prices[i] = new Price(keys, refined);
		}

		Price answer = Price.average(prices).scaleBy(DIFFERENCE_UP);
		double tentativeMultiplier = answer.getDecimalPrice()/getCommunityPrice();
		multiplier = tentativeMultiplier < MAX_BUY ? tentativeMultiplier : MAX_BUY;
		super.calculatePrice();
	}

	public static boolean isAcceptable(JSONObject j, String name, int effect){
		double average = Main.average(j);
		java.util.Date d = new java.util.Date();
		int low = j.getInt("value");
		int high = j.has("value_high") ? j.getInt("value_high") : low;
		return average <= MAX_KEYS && j.getString("currency").equals("keys") && (d.getTime()/1000) - j.getInt("last_update") <= LAST_UPDATE && (high - low)/average <= MAX_RANGE && !Main.REFERENCE.getJSONObject("unaccepted").has(Integer.toString(effect)) && Main.REFERENCE.getJSONObject("accepted").has(name);
	}
}