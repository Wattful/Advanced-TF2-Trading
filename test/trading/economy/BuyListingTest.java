package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import java.time.LocalDate;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class BuyListingTest {
	private static BuyListing first = new BuyListing("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(10, 0), new Price(11, 0), 180));
	private static BuyListing second = new BuyListing("Anger", Effect.forName("Massed Flies"), new PriceRange(new Price(10, 0), new Price(12, 0), 180));
	static {
		second.setPrice(new Price(15, 0));
	}
	
	@Test
	public void testGetName() {
		assertEquals(first.getName(), "Backwards Ballcap");
		assertEquals(second.getName(), "Anger");
	}
	
	@Test
	public void testGetQuality() {
		assertEquals(first.getQuality(), Quality.UNUSUAL);
		assertEquals(second.getQuality(), Quality.UNUSUAL);
	}
	
	@Test
	public void testGetEffect() {
		assertEquals(first.getEffect(), Effect.forName("Scorching Flames"));
		assertEquals(second.getEffect(), Effect.forName("Massed Flies"));
	}
	
	@Test
	public void testGetCommunityPrice() {
		assertEquals(first.getCommunityPrice(), new PriceRange(new Price(10, 0), new Price(11, 0), 180));
		assertEquals(second.getCommunityPrice(), new PriceRange(new Price(10, 0), new Price(12, 0), 180));
	}
	
	@Test
	public void testVisibility() {
		assertFalse(first.isVisible());
		assertTrue(second.isVisible());
		BuyListing vis = new BuyListing("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(10, 0), new Price(11, 0), 180));
		assertFalse(vis.isVisible());
		vis.setPrice(new Price(10, 0));
		assertTrue(vis.isVisible());
	}
	
	@Test
	public void testPriceFunctions() throws NonVisibleListingException {
		testExpectedException(() -> {first.getPrice();}, NonVisibleListingException.class);
		assertEquals(second.getPrice(), new Price(15, 0));
		BuyListing pric = new BuyListing("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(10, 0), new Price(11, 0), 180));
		testExpectedException(() -> {pric.getPrice();}, NonVisibleListingException.class);
		pric.setPrice(new Price(10, 0));
		assertEquals(pric.getPrice(), new Price(10, 0));
	}
	
	@Test
	public void testCopy() throws NonVisibleListingException {
		BuyListing secondCopy = second.copy();
		assertEquals(second, secondCopy);
		assertFalse(second == secondCopy);
		assertEquals(second.hashCode(), secondCopy.hashCode());
		assertEquals(second.getCommunityPrice(), secondCopy.getCommunityPrice());
		assertEquals(second.getPrice(), secondCopy.getPrice());
		assertEquals(second.isVisible(), secondCopy.isVisible());
	}
	
	@Test
	public void testJSONFunctions() throws NonVisibleListingException {
		assertEquals(BuyListing.fromJSONRepresentation(first.getJSONRepresentation()), first);
		BuyListing secondJSON = BuyListing.fromJSONRepresentation(second.getJSONRepresentation());
		assertEquals(secondJSON, second);
		assertEquals(second.hashCode(), secondJSON.hashCode());
		assertEquals(second.getCommunityPrice(), secondJSON.getCommunityPrice());
		assertEquals(second.getPrice(), secondJSON.getPrice());
		assertEquals(second.isVisible(), secondJSON.isVisible());
		testExpectedException(() -> {BuyListing.fromJSONRepresentation(null);}, NullPointerException.class);
		testExpectedException(() -> {BuyListing.fromJSONRepresentation(new JSONObject());}, JSONException.class);
	}
	
	@Test
	public void testGetListingRepresentation() throws NonVisibleListingException {
		JSONObject obj = second.getListingRepresentation();
		JSONObject comparison = new JSONObject("{\"intent\": 0, \"currencies\": {\"keys\": 15, \"metal\": 0}, \"item\": {\"quality\": 5, \"item_name\": \"Anger\", \"priceindex\": " + Effect.forName("Massed Flies").getIntValue() + "}}");
		assertTrue(obj.similar(comparison));
	}
	
	@Test
	public void testInvalidInput() {
		testExpectedException(() -> {new BuyListing(null, Effect.forName("Scorching Flames"), new PriceRange(new Price(10, 0), new Price(11, 0), 180));}, NullPointerException.class);
		testExpectedException(() -> {new BuyListing("Backwards Ballcap", null, new PriceRange(new Price(10, 0), new Price(11, 0), 180));}, NullPointerException.class);
		testExpectedException(() -> {new BuyListing("Backwards Ballcap", Effect.forName("Scorching Flames"), null);}, NullPointerException.class);
	}
	
	@Test
	public void testHash() {
		testHashCode(first, second, first.copy(), second.copy(), new BuyListing("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(10, 0), new Price(11, 0), 180)));
	}
	
	@Test
	public void testEquality() {
		testEquals(
				pair(first, first.copy()),
				pair(second, second.copy()),
				pair(first, new BuyListing("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(10, 0), new Price(11, 0), 180)))
		);
		testNotEquals(first, second, new Object());
	}
	
	@Test
	public void testToString() {
		assertNotNull(first.toString());
		assertNotNull(second.toString());
	}
}
