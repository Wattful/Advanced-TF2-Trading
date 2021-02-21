package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class AcceptabilityFunctionTest {
	private static int keyScrapRatio = 900;
	private static long nowTime = (new Date().getTime()/1000);
	private static Pair<Item, JSONObject> standardHat = Pair.of(
			new Item("Backwards Ballcap", Quality.UNUSUAL, Effect.forName("Burning Flames")), 
			new JSONObject("{\"value\": 10, \"currency\": \"keys\", \"last_update\": " + (nowTime - 1000) + "}")
	);
	private static Pair<Item, JSONObject> lowPrice = Pair.of(
			new Item("Old Guadalajara", Quality.UNUSUAL, Effect.forName("Terror-Watt")), 
			new JSONObject("{\"value\": 4, \"value_high\": 5, \"currency\": \"keys\", \"last_update\": " + (nowTime - 10000) + "}")
	);
	private static Pair<Item, JSONObject> highPrice = Pair.of(
			new Item("Vintage Tyrolean", Quality.UNUSUAL, Effect.forName("Stormy Storm")), 
			new JSONObject("{\"value\": 50, \"value_high\": 54, \"currency\": \"keys\", \"last_update\": " + (nowTime - 10000) + "}")
	);
	private static Pair<Item, JSONObject> highRange = Pair.of(
			new Item("Your Worst Nightmare", Quality.UNUSUAL, Effect.forName("Disco Beat Down")), 
			new JSONObject("{\"value\": 10, \"value_high\": 20, \"currency\": \"keys\", \"last_update\": " + (nowTime - 10000) + "}")
	);
	private static Pair<Item, JSONObject> older = Pair.of(
			new Item("Charmer's Chapeau", Quality.UNUSUAL, Effect.forName("Ancient Codex")), 
			new JSONObject("{\"value\": 10, \"value_high\": 12, \"currency\": \"keys\", \"last_update\": " + (nowTime - 1000000000) + "}")
	);
	private static Pair<Item, JSONObject> badCurrency = Pair.of(
			new Item("Ghostly Gibus", Quality.UNUSUAL, Effect.forName("Massed Flies")), 
			new JSONObject("{\"value\": 10, \"currency\": \"USD\", \"last_update\": " + (nowTime - 100) + "}")
	);
	
	@Test
	public void testAcceptAll() {
		AcceptabilityFunction acceptAll = AcceptabilityFunction.acceptAll();
		assertTrue(determineFromPair(acceptAll, standardHat));
		assertTrue(determineFromPair(acceptAll, lowPrice));
		assertTrue(determineFromPair(acceptAll, highPrice));
		assertTrue(determineFromPair(acceptAll, highRange));
		assertTrue(determineFromPair(acceptAll, older));
		assertFalse(determineFromPair(acceptAll, badCurrency));
	}
	
	@Test
	public void testCheckData() {
		AcceptabilityFunction standardCheckData = AcceptabilityFunction.checkData(8, 51, 4, 100000);
		assertTrue(determineFromPair(standardCheckData, standardHat));
		assertFalse(determineFromPair(standardCheckData, lowPrice));
		assertFalse(determineFromPair(standardCheckData, highPrice));
		assertFalse(determineFromPair(standardCheckData, highRange));
		assertFalse(determineFromPair(standardCheckData, older));
		assertFalse(determineFromPair(standardCheckData, badCurrency));

		AcceptabilityFunction lessRestrictions = AcceptabilityFunction.checkData(0, 0, -1, 0);
		assertTrue(determineFromPair(lessRestrictions, standardHat));
		assertTrue(determineFromPair(lessRestrictions, lowPrice));
		assertTrue(determineFromPair(lessRestrictions, highPrice));
		assertTrue(determineFromPair(lessRestrictions, highRange));
		assertTrue(determineFromPair(lessRestrictions, older));
		assertFalse(determineFromPair(lessRestrictions, badCurrency));
		
		testExpectedException(() -> {AcceptabilityFunction.checkData(0, Double.NaN, -1, 0);}, IllegalArgumentException.class);
		testExpectedException(() -> {standardCheckData.determineAcceptability(null, "", Effect.forInt(4), keyScrapRatio);}, NullPointerException.class);
		testExpectedException(() -> {standardCheckData.determineAcceptability(standardHat.second(), "", Effect.forInt(4), 0);}, IllegalArgumentException.class);
		testExpectedException(() -> {standardCheckData.determineAcceptability(new JSONObject(), "", Effect.forInt(4), keyScrapRatio);}, JSONException.class);
	}
	
	@Test
	public void testCheckType() {
		AcceptabilityFunction noRestrictions = AcceptabilityFunction.checkType(false, null, false, null);
		assertTrue(determineFromPair(noRestrictions, standardHat));
		assertTrue(determineFromPair(noRestrictions, lowPrice));
		assertTrue(determineFromPair(noRestrictions, highPrice));
		assertTrue(determineFromPair(noRestrictions, highRange));
		assertTrue(determineFromPair(noRestrictions, older));
		assertFalse(determineFromPair(noRestrictions, badCurrency));
		
		AcceptabilityFunction denyList = AcceptabilityFunction.checkType(false, List.of("Backwards Ballcap"), false, List.of(Effect.forName("Disco Beat Down")));
		assertFalse(determineFromPair(denyList, standardHat));
		assertTrue(determineFromPair(denyList, lowPrice));
		assertTrue(determineFromPair(denyList, highPrice));
		assertFalse(determineFromPair(denyList, highRange));
		assertTrue(determineFromPair(denyList, older));
		assertFalse(determineFromPair(denyList, badCurrency));
		
		AcceptabilityFunction acceptList = AcceptabilityFunction.checkType(true, List.of("Backwards Ballcap"), true, List.of(Effect.forName("Burning Flames")));
		assertTrue(determineFromPair(acceptList, standardHat));
		assertFalse(determineFromPair(acceptList, lowPrice));
		assertFalse(determineFromPair(acceptList, highPrice));
		assertFalse(determineFromPair(acceptList, highRange));
		assertFalse(determineFromPair(acceptList, older));
		assertFalse(determineFromPair(acceptList, badCurrency));
		
		AcceptabilityFunction regex = AcceptabilityFunction.checkType(true, List.of(".*p.*"), false, List.of());
		assertTrue(determineFromPair(regex, standardHat));
		assertFalse(determineFromPair(regex, lowPrice));
		assertFalse(determineFromPair(regex, highPrice));
		assertFalse(determineFromPair(regex, highRange));
		assertTrue(determineFromPair(regex, older));
		assertFalse(determineFromPair(regex, badCurrency));
		
		testExpectedException(() -> {noRestrictions.determineAcceptability(null, "", Effect.forInt(4), keyScrapRatio);}, NullPointerException.class);
		testExpectedException(() -> {noRestrictions.determineAcceptability(standardHat.second(), null, Effect.forInt(4), keyScrapRatio);}, NullPointerException.class);
		testExpectedException(() -> {noRestrictions.determineAcceptability(standardHat.second(), "", null, keyScrapRatio);}, NullPointerException.class);
		testExpectedException(() -> {noRestrictions.determineAcceptability(new JSONObject(), "", Effect.forInt(4), keyScrapRatio);}, JSONException.class);
	}
	
	@Test
	public void testCheckDataAndType() {
		AcceptabilityFunction noRestrictions = AcceptabilityFunction.checkDataAndType(0, 0, -1, 0, false, null, false, null);
		assertTrue(determineFromPair(noRestrictions, standardHat));
		assertTrue(determineFromPair(noRestrictions, lowPrice));
		assertTrue(determineFromPair(noRestrictions, highPrice));
		assertTrue(determineFromPair(noRestrictions, highRange));
		assertTrue(determineFromPair(noRestrictions, older));
		assertFalse(determineFromPair(noRestrictions, badCurrency));
		
		AcceptabilityFunction someRestrictions = AcceptabilityFunction.checkDataAndType(8, 100, 10, 100000, false, List.of("Your Worst Nightmare", "Vintage Tyrolean"), false, List.of(Effect.forName("Stormy Storm"), Effect.forName("Disco Beat Down")));
		assertTrue(determineFromPair(someRestrictions, standardHat));
		assertFalse(determineFromPair(someRestrictions, lowPrice));
		assertFalse(determineFromPair(someRestrictions, highPrice));
		assertFalse(determineFromPair(someRestrictions, highRange));
		assertFalse(determineFromPair(someRestrictions, older));
		assertFalse(determineFromPair(someRestrictions, badCurrency));
		
		testExpectedException(() -> {AcceptabilityFunction.checkDataAndType(0, Double.NaN, -1, 0, false, null, false, null);}, IllegalArgumentException.class);
		testExpectedException(() -> {noRestrictions.determineAcceptability(null, "", Effect.forInt(4), keyScrapRatio);}, NullPointerException.class);
		testExpectedException(() -> {noRestrictions.determineAcceptability(standardHat.second(), null, Effect.forInt(4), keyScrapRatio);}, NullPointerException.class);
		testExpectedException(() -> {noRestrictions.determineAcceptability(standardHat.second(), "", null, keyScrapRatio);}, NullPointerException.class);
		testExpectedException(() -> {noRestrictions.determineAcceptability(standardHat.second(), "", Effect.forInt(4), 0);}, IllegalArgumentException.class);
		testExpectedException(() -> {noRestrictions.determineAcceptability(new JSONObject(), "", Effect.forInt(4), keyScrapRatio);}, JSONException.class);
	}
	
	private static boolean determineFromPair(AcceptabilityFunction af, Pair<Item, JSONObject> p) {
		Item i = p.first();
		return af.determineAcceptability(p.second(), i.getName(), i.getEffect(), keyScrapRatio);
	}
}