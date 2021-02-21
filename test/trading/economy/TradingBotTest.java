package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.lang.Integer;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import trading.net.BackpackTFConnection;
import trading.net.SampleBackpackTFConnection;
import trading.net.SampleSteamConnection;
import trading.net.SteamConnection;

public class TradingBotTest {
	private static final String steamID = "Halobot";
	private static final int keyScrapRatio = 450;
	private static final double defaultRatio = 0.7;
	private static final BackpackTFConnection tfConnection = new SampleBackpackTFConnection((Item i) -> {return new JSONObject(SampleBackpackTFConnection.getSampleListings());});
	private static final SteamConnection steamConnection = new SampleSteamConnection();
	private static final FunctionSuite functions;
	static {
		HatPriceFunction hpf = HatPriceFunction.fixedRatio(0.8);
		BuyListingPriceFunction blpf = BuyListingPriceFunction.fixedRatio(0.8);
		ListingDescriptionFunction ldf = ListingDescriptionFunction.simpleDescription();
		AcceptabilityFunction af = AcceptabilityFunction.checkType(true, List.of("War Pig", "Bunsen Brave"), true, List.of(Effect.forName("Massed Flies")));
		KeyScrapRatioFunction ksrf = KeyScrapRatioFunction.customRatio(keyScrapRatio);
		functions = new FunctionSuite(hpf, blpf, ldf, af, ksrf);
	}
	private static final TradingBot withoutHats;
	private static final TradingBot autoCreated;
	static {
		try {
			withoutHats = TradingBot.botWithoutHats(steamID, tfConnection, functions);
			autoCreated = TradingBot.autoCreate(steamID, tfConnection, steamConnection, defaultRatio, functions);
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Test
	public void testGetHats() {
		assertEquals(Set.copyOf(withoutHats.getHats()), Set.of());
		assertEquals(Set.copyOf(autoCreated.getHats()), Set.of(
				new Item("Bear Necessities", Quality.UNUSUAL, Effect.forName("Purple Confetti")), 
				new Item("Human Cannonball", Quality.UNUSUAL, Effect.forName("Circling TF Logo")),
				new Item("Flamboyant Flamenco", Quality.UNUSUAL, Effect.forName("Orbiting Planets")),
				new Item("Dragonborn Helmet", Quality.UNUSUAL, Effect.forName("Disco Beat Down"))
		));
	}
	
	@Test
	public void testGetBuyListings() {
		assertEquals(Set.copyOf(withoutHats.getBuyListings()), Set.of(
				new Item("War Pig", Quality.UNUSUAL, Effect.forName("Massed Flies")),
				new Item("Bunsen Brave", Quality.UNUSUAL, Effect.forName("Massed Flies"))
		));
		assertEquals(Set.copyOf(autoCreated.getBuyListings()), Set.of(
				new Item("War Pig", Quality.UNUSUAL, Effect.forName("Massed Flies")),
				new Item("Bunsen Brave", Quality.UNUSUAL, Effect.forName("Massed Flies"))
		));
	}
	
	@Test
	public void testGetKeyScrapRatio() {
		assertEquals(withoutHats.getKeyScrapRatio(), keyScrapRatio);
		assertEquals(autoCreated.getKeyScrapRatio(), keyScrapRatio);
	}
	
	@Test
	public void testSendListings() throws IOException {
		withoutHats.sendListings(tfConnection);
		autoCreated.sendListings(tfConnection);
	}
	
	@Test
	public void testRecalculatePrices() throws IOException, NonVisibleListingException {
		TradingBot autoCreatedCopy = TradingBot.fromJSONRepresentation(autoCreated.getJSONRepresentation(), tfConnection, functions);
		TradingBot autoCreatedCopy2 = TradingBot.fromJSONRepresentation(autoCreated.getJSONRepresentation(), tfConnection, functions);
		int[] callbackCalled = new int[] {0};
		autoCreatedCopy.recalculatePrices(tfConnection);
		autoCreatedCopy2.recalculatePrices(tfConnection, (BackpackTFConnection conn) -> {callbackCalled[0]++;});
		autoCreatedCopy.checkHatIDs(steamConnection);
		ListingCollection<Hat> hats = autoCreatedCopy.getHats();
		ListingCollection<BuyListing> listings = autoCreatedCopy2.getBuyListings();
		assertEquals(callbackCalled[0], 6);
		assertEquals(hats.get(new Item("Bear Necessities", Quality.UNUSUAL, Effect.forName("Purple Confetti"))).getPrice(), new Price(16, 0));
		assertEquals(listings.get(new Item("War Pig", Quality.UNUSUAL, Effect.forName("Massed Flies"))).getPrice(), new Price(56, 40));
		
	}
	
	@Test
	public void testTradeOfferFunctions() throws IOException {
		JSONObject sampleOffer = new JSONObject(new String(Files.readAllBytes(Paths.get("./test/trading/economy/sampleOffer.json"))));
		Item query = new Item("War Pig", Quality.UNUSUAL, Effect.forInt(12));
		TradingBot shouldDecline = TradingBot.fromJSONRepresentation(
				new JSONObject("{\"id\": \"\", \"hats\": [{\"name\": \"Backwards Ballcap\", \"effect\": 14, \"communityPrice\": {\"lower\": {\"keys\": 10, \"metal\": 0}, \"middle\": {\"keys\": 10, \"metal\": 0}, \"upper\": {\"keys\": 10, \"metal\": 0}}, \"id\": \"A\", \"dateBought\": \"1961-01-17\", \"price\": {\"keys\": 40, \"metal\": 10}, \"boughtAt\": {\"keys\": 10, \"metal\": 5}}], "
						+ "\"buyListings\": [{\"name\": \"War Pig\", \"effect\": 12, \"communityPrice\": {\"lower\": {\"keys\": 10, \"metal\": 0}, \"middle\": {\"keys\": 10, \"metal\": 0}, \"upper\": {\"keys\": 10, \"metal\": 0}}, \"price\": {\"keys\": 10, \"metal\": 5}}]}]}"), 
				tfConnection, functions
		);
		TradingBot shouldAccept = TradingBot.fromJSONRepresentation(
				new JSONObject("{\"id\": \"\", \"hats\": [{\"name\": \"Backwards Ballcap\", \"effect\": 14, \"communityPrice\": {\"lower\": {\"keys\": 10, \"metal\": 0}, \"middle\": {\"keys\": 10, \"metal\": 0}, \"upper\": {\"keys\": 10, \"metal\": 0}}, \"id\": \"A\", \"dateBought\": \"1961-01-17\", \"price\": {\"keys\": 10, \"metal\": 5}, \"boughtAt\": {\"keys\": 10, \"metal\": 5}}], "
						+ "\"buyListings\": [{\"name\": \"War Pig\", \"effect\": 12, \"communityPrice\": {\"lower\": {\"keys\": 10, \"metal\": 0}, \"middle\": {\"keys\": 10, \"metal\": 0}, \"upper\": {\"keys\": 10, \"metal\": 0}}, \"price\": {\"keys\": 40, \"metal\": 10}}]}]}"), 
				tfConnection, functions
		);
		TradeOffer shouldBeDeclined = shouldDecline.evaluateTrade(sampleOffer);
		TradeOffer shouldBeAccepted = shouldAccept.evaluateTrade(sampleOffer, 0.1, true, null);
		assertEquals((int)shouldBeDeclined.itemsToGive().get(new Item("Backwards Ballcap", Quality.UNUSUAL, Effect.forInt(14))), 40 * keyScrapRatio + 90);
		assertEquals((int)shouldBeAccepted.itemsToGive().get(new Item("Backwards Ballcap", Quality.UNUSUAL, Effect.forInt(14))), 10 * keyScrapRatio + 45);
		assertEquals((int)shouldBeDeclined.itemsToReceive().get(new Item("War Pig", Quality.UNUSUAL, Effect.forInt(12))), 10 * keyScrapRatio + 45);
		assertEquals((int)shouldBeAccepted.itemsToReceive().get(new Item("War Pig", Quality.UNUSUAL, Effect.forInt(12))), 40 * keyScrapRatio + 90);
		assertEquals(shouldBeDeclined.getResponse(), TradeOfferResponse.DECLINE);
		assertEquals(shouldBeAccepted.getResponse(), TradeOfferResponse.ACCEPT);
		
		shouldDecline.updateItemsAfterOffer(shouldBeDeclined, defaultRatio);
		assertEquals(shouldAccept, shouldDecline);
		shouldAccept.updateItemsAfterOffer(shouldBeAccepted, defaultRatio);
		Hat h = shouldAccept.getHats().get(query);
		assertNotNull(h);
		assertEquals(h.getDateBought(), LocalDate.now());
		assertEquals(h.getPurchasePrice(), new Price(40, 10));
		BuyListing bl = shouldAccept.getBuyListings().get(query);
		assertNull(bl);
		
		TradingBot noListing = TradingBot.fromJSONRepresentation(
				new JSONObject("{\"id\": \"\", \"hats\": [{\"name\": \"Backwards Ballcap\", \"effect\": 14, \"communityPrice\": {\"lower\": {\"keys\": 10, \"metal\": 0}, \"middle\": {\"keys\": 10, \"metal\": 0}, \"upper\": {\"keys\": 10, \"metal\": 0}}, \"id\": \"A\", \"dateBought\": \"1961-01-17\", \"price\": {\"keys\": 40, \"metal\": 10}, \"boughtAt\": {\"keys\": 10, \"metal\": 5}}], "
						+ "\"buyListings\": []}]}"), 
				tfConnection, functions
		);
		TradingBot nonVisibleListing = TradingBot.fromJSONRepresentation(
				new JSONObject("{\"id\": \"\", \"hats\": [{\"name\": \"Backwards Ballcap\", \"effect\": 14, \"communityPrice\": {\"lower\": {\"keys\": 10, \"metal\": 0}, \"middle\": {\"keys\": 10, \"metal\": 0}, \"upper\": {\"keys\": 10, \"metal\": 0}}, \"id\": \"A\", \"dateBought\": \"1961-01-17\", \"price\": {\"keys\": 40, \"metal\": 10}, \"boughtAt\": {\"keys\": 10, \"metal\": 5}}], "
						+ "\"buyListings\": [{\"name\": \"War Pig\", \"effect\": 12, \"communityPrice\": {\"lower\": {\"keys\": 10, \"metal\": 0}, \"middle\": {\"keys\": 10, \"metal\": 0}, \"upper\": {\"keys\": 10, \"metal\": 0}}, \"price\": null}]}]}"), 
				tfConnection, functions
		);
		
		noListing.updateItemsAfterOffer(shouldBeAccepted, defaultRatio);
		nonVisibleListing.updateItemsAfterOffer(shouldBeAccepted, defaultRatio);
		assertEquals(noListing.getHats().get(query).getPurchasePrice(), new Price(49, 35));
		assertEquals(nonVisibleListing.getHats().get(query).getPurchasePrice(), new Price(49, 35));
	
		testExpectedException(() -> {shouldDecline.evaluateTrade(null);}, NullPointerException.class);
		testExpectedException(() -> {shouldDecline.evaluateTrade(new JSONObject());}, JSONException.class);
		testExpectedException(() -> {shouldDecline.evaluateTrade(sampleOffer, -1, false, null);}, IllegalArgumentException.class);
	
		testExpectedException(() -> {shouldDecline.updateItemsAfterOffer(null, defaultRatio);}, NullPointerException.class);
		testExpectedException(() -> {shouldDecline.updateItemsAfterOffer(shouldBeAccepted, -1);}, IllegalArgumentException.class);
	}
	
	@Test
	public void testCheckHatIDs() throws IOException {
		TradingBot nullHatID = TradingBot.fromJSONRepresentation(
				new JSONObject("{\"id\": \"\", \"hats\": [{\"name\": \"Bear Necessities\", \"effect\": 7, \"communityPrice\": {\"lower\": {\"keys\": 10, \"metal\": 0}, \"middle\": {\"keys\": 10, \"metal\": 0}, \"upper\": {\"keys\": 10, \"metal\": 0}}, \"id\": null, \"dateBought\": \"1961-01-17\", \"price\": null, \"boughtAt\": {\"keys\": 10, \"metal\": 5}}], \"buyListings\": []}"), 
				tfConnection, functions
		);
		nullHatID.checkHatIDs(steamConnection);
		Hat result = nullHatID.getHats().get(new Item("Bear Necessities", Quality.UNUSUAL, Effect.forInt(7)));
		assertEquals(result.getID(), "7955412500");
	}
	
	@Test
	public void testReadHatsFromInventory() throws IOException, NonVisibleListingException {
		TradingBot withNonexistentHat = TradingBot.fromJSONRepresentation(
				new JSONObject("{\"id\": \"\", \"hats\": [{\"name\": \"Bear Necessities\", \"effect\": 12, \"communityPrice\": {\"lower\": {\"keys\": 10, \"metal\": 0}, \"middle\": {\"keys\": 10, \"metal\": 0}, \"upper\": {\"keys\": 10, \"metal\": 0}}, \"id\": null, \"dateBought\": \"1961-01-17\", \"price\": null, \"boughtAt\": {\"keys\": 10, \"metal\": 5}}], \"buyListings\": []}"), 
				tfConnection, functions
		);		
		withNonexistentHat.readHatsFromInventory(steamConnection, 0.8);
		ListingCollection<Hat> hats = withNonexistentHat.getHats();
		assertEquals(hats.size(), 4);
		assertEquals(hats.get(new Item("Bear Necessities", Quality.UNUSUAL, Effect.forName("Purple Confetti"))).getPurchasePrice(), new Price(16, 0));
		assertNull(hats.get(new Item("Bear Necessities", Quality.UNUSUAL, Effect.forInt(14))));
	}
	
	@Test
	public void testUpdateAndFilter() throws IOException {
		TradingBot badListings = TradingBot.fromJSONRepresentation(
				new JSONObject("{\"id\": \"\", \"hats\": [], \"buyListings\": [{\"name\": \"Liquidator's Lid\", \"effect\": 7, \"communityPrice\": {\"lower\": {\"keys\": 10, \"metal\": 0}, \"middle\": {\"keys\": 10, \"metal\": 0}, \"upper\": {\"keys\": 10, \"metal\": 0}}, \"price\": null}]}"), 
				tfConnection, functions
		);
		badListings.updateAndFilter(tfConnection);
		ListingCollection<BuyListing> listings = badListings.getBuyListings();
		assertEquals(listings.size(), 2);
		assertNull(listings.get(new Item("Liquidator's Lid", Quality.UNUSUAL, Effect.forInt(7))));
	}
	
	@Test
	public void testJSONFunctions() throws IOException {
		assertEquals(TradingBot.fromJSONRepresentation(withoutHats.getJSONRepresentation(), tfConnection, functions), withoutHats);
		assertEquals(TradingBot.fromJSONRepresentation(autoCreated.getJSONRepresentation(), tfConnection, functions), autoCreated);
		
		testExpectedException(() -> {TradingBot.fromJSONRepresentation(null, tfConnection, functions);}, NullPointerException.class);
		testExpectedException(() -> {TradingBot.fromJSONRepresentation(autoCreated.getJSONRepresentation(), null, functions);}, NullPointerException.class);
		testExpectedException(() -> {TradingBot.fromJSONRepresentation(autoCreated.getJSONRepresentation(), tfConnection, null);}, NullPointerException.class);
		testExpectedException(() -> {TradingBot.fromJSONRepresentation(new JSONObject(), tfConnection, functions);}, JSONException.class);
	}
	
	@Test
	public void testInvalidInput() {
		testExpectedException(() -> {TradingBot.botWithoutHats(null, tfConnection, functions);}, NullPointerException.class);
		testExpectedException(() -> {TradingBot.botWithoutHats(steamID, null, functions);}, NullPointerException.class);
		testExpectedException(() -> {TradingBot.botWithoutHats(steamID, tfConnection, null);}, NullPointerException.class);
		
		testExpectedException(() -> {TradingBot.autoCreate(null, tfConnection, steamConnection, 1.0, functions);}, NullPointerException.class);
		testExpectedException(() -> {TradingBot.autoCreate(steamID, null, steamConnection, 1.0, functions);}, NullPointerException.class);
		testExpectedException(() -> {TradingBot.autoCreate(steamID, tfConnection, null, 1.0, functions);}, NullPointerException.class);
		testExpectedException(() -> {TradingBot.autoCreate(steamID, tfConnection, steamConnection, 1.0, null);}, NullPointerException.class);
		testExpectedException(() -> {TradingBot.autoCreate(steamID, tfConnection, steamConnection, 1.1, functions);}, IllegalArgumentException.class);
		testExpectedException(() -> {TradingBot.autoCreate(steamID, tfConnection, steamConnection, Double.NaN, functions);}, IllegalArgumentException.class);
		
	}
	
	@Test
	public void testHash() throws IOException {
		testHashCode(withoutHats, autoCreated, 
				TradingBot.botWithoutHats(steamID, tfConnection, functions), TradingBot.autoCreate(steamID, tfConnection, steamConnection, defaultRatio, functions));
	}
	
	@Test
	public void testEquality() throws IOException {
		testEquals(
				pair(withoutHats, TradingBot.botWithoutHats(steamID, tfConnection, functions)),
				pair(autoCreated, TradingBot.autoCreate(steamID, tfConnection, steamConnection, defaultRatio, functions))
		);
		testNotEquals(withoutHats, autoCreated, new Object());
	}
	
	@Test
	public void testToString() {
		assertNotNull(withoutHats.toString());
		assertNotNull(autoCreated.toString());
	}

}
