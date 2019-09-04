package trading;

import org.json.JSONObject;

public class Price{
	private int keys;
	private int refined;

	private Price(){}

	public Price(int k, int r){
		keys = k;
		refined = r;
	}

	public int getKeys(){
		return keys;
	}

	public int getRefined(){
		return refined;
	}

	public double getDecimalPrice(){
		return keys + ((refined*9.0)/Main.KEY_SCRAP_RATIO);
	}

	public int getScrapValue(){
		return (keys * Main.KEY_SCRAP_RATIO) + (refined * 9);
	}

	public String toString(){
		return keys + " keys and " + refined + " refined";
	}

	public JSONObject getJSONRepresentation(){
		JSONObject answer = new JSONObject();
		answer.put("keys", keys);
		answer.put("metal", refined);
		return answer;
	}

	public Price scaleBy(double scale){
		return Price.calculate(getDecimalPrice(), scale);
	}

	public static Price calculate(double communityPrice, double multiplier){
		double decimalPrice = communityPrice * multiplier;
		int k = (int)decimalPrice;
		double decimalRef = decimalPrice - k;
		int r = (int)((decimalRef * Main.KEY_SCRAP_RATIO) / 9);
		return new Price(k, r);
	}

	public static Price calculate(double decimalPrice){
		return calculate(decimalPrice, 1);
	}

	public static Price average(Price[] prices){
		double total = 0;
		for(Price p : prices){
			total += p.getDecimalPrice();
		}
		double average = total/prices.length;
		return calculate(average);
	}
}