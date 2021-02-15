package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import trading.net.SampleBackpackTFConnection;

public class PriceFunctionUtilsTest {
	private static final String sampleListingsString = SampleBackpackTFConnection.getSampleListings();
	
	@Test
	public void testRemoveListingsFromUser() {
		JSONObject sample = new JSONObject(sampleListingsString);
		PriceFunctionUtils.removeListingsFromUser(sample, "A");
		JSONArray buyListings = sample.getJSONObject("buy").getJSONArray("listings");
		JSONArray sellListings = sample.getJSONObject("sell").getJSONArray("listings");
		assertEquals(buyListings.length(), 2);
		assertEquals(sellListings.length(), 3);
		for(Object o : buyListings) {
			JSONObject jo = (JSONObject)o;
			assertNotEquals(jo.getString("steamid"), "A");
		}
		for(Object o : sellListings) {
			JSONObject jo = (JSONObject)o;
			assertNotEquals(jo.getString("steamid"), "A");
		}
		testExpectedException(() -> {PriceFunctionUtils.removeListingsFromUser(null, "A");}, NullPointerException.class);
		testExpectedException(() -> {PriceFunctionUtils.removeListingsFromUser(sample, null);}, NullPointerException.class);
		testExpectedException(() -> {PriceFunctionUtils.removeListingsFromUser(new JSONObject(), "A");}, JSONException.class);
	}
	
	@Test
	public void testRemoveListingsWithoutUnusualEffect() {
		JSONObject sample = new JSONObject(sampleListingsString);
		PriceFunctionUtils.removeListingsWithoutUnusualEffect(sample);
		JSONArray buyListings = sample.getJSONObject("buy").getJSONArray("listings");
		JSONArray sellListings = sample.getJSONObject("sell").getJSONArray("listings");
		assertEquals(buyListings.length(), 2);
		assertEquals(sellListings.length(), 2);
		for(Object o : buyListings) {
			JSONObject jo = (JSONObject)o;
			assertFalse(jo.has("attributes"));
		}
		for(Object o : sellListings) {
			JSONObject jo = (JSONObject)o;
			assertFalse(jo.has("attributes"));
		}
		testExpectedException(() -> {PriceFunctionUtils.removeListingsWithoutUnusualEffect(null);}, NullPointerException.class);
		testExpectedException(() -> {PriceFunctionUtils.removeListingsWithoutUnusualEffect(new JSONObject());}, JSONException.class);
	}
}
