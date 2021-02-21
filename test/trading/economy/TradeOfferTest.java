package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class TradeOfferTest {
	private static final int keyScrapRatio = 450;
	private static final JSONObject sampleOffer;
	private static final TradeOffer offer1;
	private static final TradeOffer offer2;
	private static final Hat ballcap = new Hat("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(50, 0)), new Price(35, 0), "C", LocalDate.now());
	private static final BuyListing lid = new BuyListing("War Pig", Effect.forName("Massed Flies"), new PriceRange(new Price(11, 0)));
	static {
		try {
			sampleOffer = new JSONObject(new String(Files.readAllBytes(Paths.get("./test/trading/economy/sampleOffer.json"))));
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
		Hat ballcapCopy = ballcap.copy();
		ballcapCopy.setPrice(new Price(21, 35));
		BuyListing lidCopy = lid.copy();
		lidCopy.setPrice(new Price(10, 0));
		offer1 = TradeOffer.fromJSON(sampleOffer, new ListingHashSet<Hat>(List.of(ballcapCopy)), new ListingHashSet<BuyListing>(List.of(lidCopy)), keyScrapRatio);
		offer2 = TradeOffer.fromJSON(sampleOffer, new ListingHashSet<Hat>(List.of(ballcapCopy)), new ListingHashSet<BuyListing>(List.of(lidCopy)), keyScrapRatio, 0, true, null);
	}
	
	@Test
	public void testResponses() {
		assertEquals(offer1.getResponse(), TradeOfferResponse.DECLINE);
		assertEquals(offer2.getResponse(), TradeOfferResponse.DECLINE);
		
		Hat ballcapCopy = ballcap.copy();
		BuyListing lidCopy = lid.copy();
		
		ballcapCopy.setPrice(new Price(10, 0));
		lidCopy.setPrice(new Price(20, 0));
		assertEquals(
				TradeOffer.fromJSON(sampleOffer, new ListingHashSet<Hat>(List.of(ballcapCopy)), new ListingHashSet<BuyListing>(List.of(lidCopy)), keyScrapRatio).getResponse(), 
				TradeOfferResponse.ACCEPT
		);
		assertEquals(
				TradeOffer.fromJSON(sampleOffer, new ListingHashSet<Hat>(List.of()), new ListingHashSet<BuyListing>(List.of(lidCopy)), keyScrapRatio).getResponse(), 
				TradeOfferResponse.HOLD
		);
		assertEquals(
				TradeOffer.fromJSON(sampleOffer, new ListingHashSet<Hat>(List.of(ballcapCopy)), new ListingHashSet<BuyListing>(List.of()), keyScrapRatio).getResponse(), 
				TradeOfferResponse.HOLD
		);
		assertEquals(
				TradeOffer.fromJSON(sampleOffer, new ListingHashSet<Hat>(List.of()), new ListingHashSet<BuyListing>(List.of()), keyScrapRatio).getResponse(), 
				TradeOfferResponse.HOLD
		);
		assertEquals(
				TradeOffer.fromJSON(sampleOffer, new ListingHashSet<Hat>(List.of(ballcapCopy)), new ListingHashSet<BuyListing>(List.of()), keyScrapRatio, 0.0, false, List.of("not an id")).getResponse(), 
				TradeOfferResponse.DECLINE
		);
		assertEquals(
				TradeOffer.fromJSON(sampleOffer, new ListingHashSet<Hat>(List.of(ballcapCopy)), new ListingHashSet<BuyListing>(List.of()), keyScrapRatio, 0.0, false, List.of("Halibuttcheeks")).getResponse(), 
				TradeOfferResponse.ACCEPT
		);
		assertEquals(
				TradeOffer.fromJSON(sampleOffer, new ListingHashSet<Hat>(List.of(ballcapCopy)), new ListingHashSet<BuyListing>(List.of()), keyScrapRatio, 0.999, false, null).getResponse(), 
				TradeOfferResponse.ACCEPT
		);
		
		
	}
	
	@Test
	public void testItemsToGive() {
		assertEquals(offer1.itemsToGive(), Map.of(
				new Item("Mann Co. Supply Crate Key", Quality.UNIQUE), 450, 
				new Item("Scrap Metal", Quality.UNIQUE), 1, 
				new Item("Backwards Ballcap", Quality.UNUSUAL, Effect.forName("Scorching Flames")), 9765)
		);
		assertEquals(offer2.itemsToGive(), Map.of(
				new Item("Mann Co. Supply Crate Key", Quality.UNIQUE), 450, 
				new Item("Scrap Metal", Quality.UNIQUE), 1, 
				new Item("Backwards Ballcap", Quality.UNUSUAL, Effect.forName("Scorching Flames")), 9765)
		);
	}
	
	@Test
	public void testItemsToReceive() {
		assertEquals(offer1.itemsToReceive(), Map.of(
				new Item("Refined Metal", Quality.UNIQUE), 9, 
				new Item("Reclaimed Metal", Quality.UNIQUE), 3, 
				new Item("War Pig", Quality.UNUSUAL, Effect.forName("Massed Flies")), 4500)
		);
		assertEquals(offer2.itemsToReceive(), Map.of(
				new Item("Refined Metal", Quality.UNIQUE), 9, 
				new Item("Reclaimed Metal", Quality.UNIQUE), 3, 
				new Item("War Pig", Quality.UNUSUAL, Effect.forName("Massed Flies")), 4500)
		);
	}
	
	@Test
	public void testGetOurValue() {
		assertEquals(offer1.getOurValue(), 10216);
		assertEquals(offer2.getOurValue(), 10216);
	}
	
	@Test
	public void testGetTheirValue() {
		assertEquals(offer1.getTheirValue(), 4512);
		assertEquals(offer2.getTheirValue(), 4512);
	}
	
	@Test
	public void testGetPartner() {
		assertEquals(offer1.getPartner(), "Halibuttcheeks");
		assertEquals(offer2.getPartner(), "Halibuttcheeks");
	}
	
	@Test
	public void testGetData() {
		assertNotNull(offer1.getData());
		assertNotNull(offer2.getData());
	}
	
	@Test
	public void testInvalidInput() {
		testExpectedException(() -> {TradeOffer.fromJSON(null, new ListingHashSet<Hat>(List.of()), new ListingHashSet<BuyListing>(List.of()), keyScrapRatio);}, NullPointerException.class);
		testExpectedException(() -> {TradeOffer.fromJSON(sampleOffer, null, new ListingHashSet<BuyListing>(List.of()), keyScrapRatio);}, NullPointerException.class);
		testExpectedException(() -> {TradeOffer.fromJSON(sampleOffer, new ListingHashSet<Hat>(List.of()), null, keyScrapRatio);}, NullPointerException.class);
		testExpectedException(() -> {TradeOffer.fromJSON(sampleOffer, new ListingHashSet<Hat>(List.of()), new ListingHashSet<BuyListing>(List.of()), 0);}, IllegalArgumentException.class);
		testExpectedException(() -> {TradeOffer.fromJSON(new JSONObject(), new ListingHashSet<Hat>(List.of()), new ListingHashSet<BuyListing>(List.of()), keyScrapRatio);}, JSONException.class);
		testExpectedException(() -> {TradeOffer.fromJSON(sampleOffer, new ListingHashSet<Hat>(List.of()), new ListingHashSet<BuyListing>(List.of()), keyScrapRatio, -1, true, null);}, IllegalArgumentException.class);
	}
	
	@Test
	public void testHash() {
		testHashCode(offer1, offer2);
	}
	
	@Test
	public void testEquality() {
		testEquals(pair(offer1, offer2));
		testNotEquals(offer1, new Object());
	}
	
	@Test
	public void testToString() {
		assertNotNull(offer1.toString());
		assertNotNull(offer2.toString());	
	}
}
