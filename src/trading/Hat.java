package trading;

import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import java.util.Random;

public class Hat extends Listing{
	private String id;

	private int age;
	private double boughtAt;

	private Hat(String n, int e, double b, double c){
		super(n, e, c);
		boughtAt = b;
		age = 0;
		calculateMultiplier();
	}

	public Hat(BuyListing b){
		this(b.getName(), b.getEffect(), b.getMultiplier(), b.getCommunityPrice());
	}

	public Hat(JSONObject j){
		super(j.getString("name"), j.getInt("effect"), j.getDouble("communityPrice"));
		id = j.isNull("id") ? null : j.getString("id");
		age = j.getInt("age");
		boughtAt = j.getDouble("boughtAt");
		calculateMultiplier();
	}

	public void grow(){
		age++;
		calculateMultiplier();
	}

	private void calculateMultiplier(){
		double s_r = Main.SELL_RATIO;
		double m_p = Main.MIN_PROFIT;
		double s = Main.SPEED;
		multiplier = ((s_r - (boughtAt + m_p)) * Math.pow(age + 1, -s)) + (boughtAt + m_p);
		super.calculatePrice();
	}

	public JSONObject getJSONRepresentation(){
		JSONObject j = super.getJSONRepresentation();
		j.put("age", age);
		j.put("id", id == null ? JSONObject.NULL : id);
		j.put("boughtAt", boughtAt);
		return j;
	}

	public JSONObject getListingRepresentation(){
		JSONObject j = super.getListingRepresentation();
		j.put("intent", 1);
		j.put("id", id);
		j.put("promoted", 1);
		String description = j.getString("details");
		j.remove("details");
		description = description.replace("=", "Selling");
		description += Main.SAYINGS[new Random().nextInt(Main.SAYINGS.length)];
		j.put("details", description);
		return j;
	}

	public boolean hasID(){
		return id != null;
	}

	public void setID(String identification){
		id = identification;
	}

	public boolean equals(Object o){
		return o instanceof Hat && super.equals(o);
	}
}