package trading.driver;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.json.*;

import trading.economy.AcceptabilityFunction;
import trading.economy.BuyListing;
import trading.economy.Effect;
import trading.economy.Hat;
import trading.economy.Item;
import trading.economy.ListingDescriptionFunction;
import trading.economy.NonVisibleListingException;
import trading.economy.Pair;
import trading.economy.Price;
import trading.economy.PriceRange;
import trading.economy.Quality;

public class FunctionSuiteTranslatorsTest {
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
	private static Hat hat = new Hat("Anger", Effect.forName("Massed Flies"), new PriceRange(new Price(10, 0), new Price(12, 0), 180), new Price(8, 0), "KJ", LocalDate.of(2021, 1, 20));
	private static BuyListing buyListing = new BuyListing("Anger", Effect.forName("Massed Flies"), new PriceRange(new Price(10, 0), new Price(12, 0), 180));
	static {
		hat.setPrice(new Price(15, 0));
		buyListing.setPrice(new Price(15, 10));
	}

	@Test
	public void testCheckType() {
		AcceptabilityFunction noRestrictions = FunctionSuiteTranslators.checkType(false, null, false, null);
		assertTrue(determineFromPair(noRestrictions, standardHat));
		assertTrue(determineFromPair(noRestrictions, lowPrice));
		assertTrue(determineFromPair(noRestrictions, highPrice));
		assertTrue(determineFromPair(noRestrictions, highRange));
		assertTrue(determineFromPair(noRestrictions, older));
		assertFalse(determineFromPair(noRestrictions, badCurrency));
		
		AcceptabilityFunction denyList = FunctionSuiteTranslators.checkType(false, new JSONArray("[\"Backwards Ballcap\"]"), false, new JSONArray("[\"Disco Beat Down\"]"));
		assertFalse(determineFromPair(denyList, standardHat));
		assertTrue(determineFromPair(denyList, lowPrice));
		assertTrue(determineFromPair(denyList, highPrice));
		assertFalse(determineFromPair(denyList, highRange));
		assertTrue(determineFromPair(denyList, older));
		assertFalse(determineFromPair(denyList, badCurrency));
		
		AcceptabilityFunction acceptList = FunctionSuiteTranslators.checkType(true, new JSONArray("[\"Backwards Ballcap\"]"), true, new JSONArray("[13]"));
		assertTrue(determineFromPair(acceptList, standardHat));
		assertFalse(determineFromPair(acceptList, lowPrice));
		assertFalse(determineFromPair(acceptList, highPrice));
		assertFalse(determineFromPair(acceptList, highRange));
		assertFalse(determineFromPair(acceptList, older));
		assertFalse(determineFromPair(acceptList, badCurrency));
		
		testExpectedException(() -> {FunctionSuiteTranslators.checkType(true, new JSONArray("[{}]"), true, new JSONArray("[62]"));}, JSONException.class);
		testExpectedException(() -> {FunctionSuiteTranslators.checkType(true, new JSONArray("[\"Backwards Ballcap\"]"), true, new JSONArray("[{}]"));}, JSONException.class);
		
		testExpectedException(() -> {noRestrictions.determineAcceptability(null, "", Effect.forInt(4), keyScrapRatio);}, NullPointerException.class);
		testExpectedException(() -> {noRestrictions.determineAcceptability(standardHat.second(), null, Effect.forInt(4), keyScrapRatio);}, NullPointerException.class);
		testExpectedException(() -> {noRestrictions.determineAcceptability(standardHat.second(), "", null, keyScrapRatio);}, NullPointerException.class);
		testExpectedException(() -> {noRestrictions.determineAcceptability(new JSONObject(), "", Effect.forInt(4), keyScrapRatio);}, JSONException.class);
	}
	
	@Test
	public void testCheckDataAndType() {
		AcceptabilityFunction noRestrictions = FunctionSuiteTranslators.checkDataAndType(0, 0, -1, 0, false, null, false, null);
		assertTrue(determineFromPair(noRestrictions, standardHat));
		assertTrue(determineFromPair(noRestrictions, lowPrice));
		assertTrue(determineFromPair(noRestrictions, highPrice));
		assertTrue(determineFromPair(noRestrictions, highRange));
		assertTrue(determineFromPair(noRestrictions, older));
		assertFalse(determineFromPair(noRestrictions, badCurrency));
		
		AcceptabilityFunction someRestrictions = FunctionSuiteTranslators.checkDataAndType(8, 100, 10, 100000, false, new JSONArray("[\"Your Worst Nightmare\", \"Vintage Tyrolean\"]"), false, new JSONArray("[\"Stormy Storm\", 62]"));
		assertTrue(determineFromPair(someRestrictions, standardHat));
		assertFalse(determineFromPair(someRestrictions, lowPrice));
		assertFalse(determineFromPair(someRestrictions, highPrice));
		assertFalse(determineFromPair(someRestrictions, highRange));
		assertFalse(determineFromPair(someRestrictions, older));
		assertFalse(determineFromPair(someRestrictions, badCurrency));
		
		testExpectedException(() -> {FunctionSuiteTranslators.checkDataAndType(0, 50, -1, 0, true, new JSONArray("[{}]"), true, new JSONArray("[62]"));}, JSONException.class);
		testExpectedException(() -> {FunctionSuiteTranslators.checkDataAndType(0, 50, -1, 0, true, new JSONArray("[\"Backwards Ballcap\"]"), true, new JSONArray("[{}]"));}, JSONException.class);
		testExpectedException(() -> {FunctionSuiteTranslators.checkDataAndType(0, Double.NaN, -1, 0, false, null, false, null);}, IllegalArgumentException.class);
		
		testExpectedException(() -> {noRestrictions.determineAcceptability(null, "", Effect.forInt(4), keyScrapRatio);}, NullPointerException.class);
		testExpectedException(() -> {noRestrictions.determineAcceptability(standardHat.second(), null, Effect.forInt(4), keyScrapRatio);}, NullPointerException.class);
		testExpectedException(() -> {noRestrictions.determineAcceptability(standardHat.second(), "", null, keyScrapRatio);}, NullPointerException.class);
		testExpectedException(() -> {noRestrictions.determineAcceptability(standardHat.second(), "", Effect.forInt(4), 0);}, IllegalArgumentException.class);
		testExpectedException(() -> {noRestrictions.determineAcceptability(new JSONObject(), "", Effect.forInt(4), keyScrapRatio);}, JSONException.class);
	}

	@Test
	public void testDescriptionWithSayings() throws NonVisibleListingException {
		ListingDescriptionFunction before = FunctionSuiteTranslators.descriptionWithSayings(true, new JSONArray("[\"Saying 1\"]"));
		ListingDescriptionFunction after = FunctionSuiteTranslators.descriptionWithSayings(false, new JSONArray("[\"Saying 2\"]"));
		assertEquals(before.generateDescription(hat), "Saying 1 Selling this hat for 15 keys. Send me an offer, I will accept instantly! Item offers held for manual review.");
		assertEquals(after.generateDescription(hat), "Selling this hat for 15 keys. Send me an offer, I will accept instantly! Item offers held for manual review. Saying 2");
		assertEquals(before.generateDescription(buyListing), "Saying 1 Buying this hat for 15 keys and 10 refined. Send me an offer, I will accept instantly! Item offers held for manual review.");
		assertEquals(after.generateDescription(buyListing), "Buying this hat for 15 keys and 10 refined. Send me an offer, I will accept instantly! Item offers held for manual review. Saying 2");
		
		testExpectedException(() -> {FunctionSuiteTranslators.descriptionWithSayings(true, null);}, NullPointerException.class);
		testExpectedException(() -> {FunctionSuiteTranslators.descriptionWithSayings(true, new JSONArray());}, IllegalArgumentException.class);
		testExpectedException(() -> {FunctionSuiteTranslators.descriptionWithSayings(true, new JSONArray("[{}]"));}, IllegalArgumentException.class);
		
		testExpectedException(() -> {before.generateDescription(null);}, NullPointerException.class);
		testExpectedException(() -> {before.generateDescription(new Hat("Anger", Effect.forName("Massed Flies"), new PriceRange(new Price(10, 0), new Price(12, 0), 180), new Price(8, 0), "KJ", LocalDate.of(2021, 1, 20)));}, NonVisibleListingException.class);
	}

	private static boolean determineFromPair(AcceptabilityFunction af, Pair<Item, JSONObject> p) {
		Item i = p.first();
		return af.determineAcceptability(p.second(), i.getName(), i.getEffect(), keyScrapRatio);
	}
}
