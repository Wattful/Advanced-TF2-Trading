package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class KeyScrapRatioFunctionTest {
	private static final JSONObject pricesObject = new JSONObject("{\"response\": {\"items\": {\"Mann Co. Supply Crate Key\": {\"prices\": {\"6\": {\"Tradable\": {\"Craftable\": [{\"value\": 55, \"value_high\": 55.66, \"currency\": \"metal\"}]}}}}}}}}");
	private static final JSONObject badCurrency = new JSONObject("{\"response\": {\"items\": {\"Mann Co. Supply Crate Key\": {\"prices\": {\"6\": {\"Tradable\": {\"Craftable\": [{\"value\": 55, \"value_high\": 55.66, \"currency\": \"USD\"}]}}}}}}}}");
	
	@Test
	public void testCustomRatio() {
		KeyScrapRatioFunction ksrf = KeyScrapRatioFunction.customRatio(11);
		assertEquals(ksrf.calculateRatio(pricesObject), 11);
		testExpectedException(() -> {KeyScrapRatioFunction.customRatio(0);}, IllegalArgumentException.class);
	}
	
	@Test
	public void testBackpackTFRatio() {
		KeyScrapRatioFunction bptf = KeyScrapRatioFunction.backpackTFRatio();
		assertEquals(bptf.calculateRatio(pricesObject), 498);
		testExpectedException(() -> {bptf.calculateRatio(null);}, NullPointerException.class);
		testExpectedException(() -> {bptf.calculateRatio(new JSONObject());}, JSONException.class);
		testExpectedException(() -> {bptf.calculateRatio(badCurrency);}, JSONException.class);
	}
}
