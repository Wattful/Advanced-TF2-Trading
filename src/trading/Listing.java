package trading;

import org.json.JSONObject;
import org.json.JSONException;

public abstract class Listing{
	private String name;
	private int effect;
	private Price myPrice;

	private double communityPrice;
	protected double multiplier;

	private Listing(){}

	protected Listing(String n, int e, double c){
		name = n;
		effect = e;
		communityPrice = c;
	}

	public Price getPrice(){
		return myPrice;
	}

	public String getName(){
		return name;
	}

	public int getEffect(){
		return effect;
	}

	public String getKey(){
		return name + "|" + effect;
	}

	protected double getCommunityPrice(){
		return communityPrice;
	}

	public void setCommunityPrice(double price){
		communityPrice = price;
	}

	public boolean equals(Object o){
		if(o instanceof Listing){
			return name == ((Listing)o).getName() && effect == ((Listing)o).getEffect();
		}
		return false;
	}

	public String toString(){
		return effect + " " + name + " valued at " + myPrice;
	}

	public static Class<? extends Listing> getType(JSONObject j){
		try{
			j.get("age");
			return Hat.class;
		} catch (JSONException e){
			return BuyListing.class;
		}
	}

	protected final void calculatePrice(){
		myPrice = Price.calculate(communityPrice, multiplier);
	}

	protected JSONObject getJSONRepresentation(){
		JSONObject answer = new JSONObject();
		answer.put("name", name);
		answer.put("effect", effect);
		answer.put("communityPrice", communityPrice);
		return answer;
	}

	protected JSONObject getListingRepresentation(){
		JSONObject answer = new JSONObject();
		answer.put("currencies", myPrice.getJSONRepresentation());
		//String description = Main.SAYINGS[new Random().nextInt(Main.SAYINGS.length)];
		String description = " = this hat for ";
		if(myPrice.getRefined() == 0){
			description += myPrice.getKeys() + " keys. Send me an offer, I will accept instantly! Item offers held for manual review. -- ";
		} else {
			description += myPrice.getKeys() + " keys and " + myPrice.getRefined() + " refined.  Offers accepted instantly. Item offers held for manual review. -- ";
		}
		answer.put("details", description);
		return answer;
	}

	private void setPrice(Price p){
		myPrice = p;
	}
}