package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import java.time.LocalDate;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class HatTest {
	private static Hat first = new Hat("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(10, 0), new Price(11, 0), 180), new Price(9, 0), LocalDate.of(2021, 2, 3));
	private static Hat second = new Hat("Anger", Effect.forName("Massed Flies"), new PriceRange(new Price(10, 0), new Price(12, 0), 180), new Price(8, 0), "KJ", LocalDate.of(2021, 1, 20));
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
	public void testGetDateBought() {
		assertEquals(first.getDateBought(), LocalDate.of(2021, 2, 3));
		assertEquals(second.getDateBought(), LocalDate.of(2021, 1, 20));
	}
	
	@Test
	public void testGetPurchasePrice() {
		assertEquals(first.getPurchasePrice(), new Price(9, 0));
		assertEquals(second.getPurchasePrice(), new Price(8, 0));
	}
	
	@Test
	public void testVisibility() {
		assertFalse(first.isVisible());
		assertTrue(second.isVisible());
		Hat vis = new Hat("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(10, 0), new Price(11, 0), 180), new Price(9, 0), LocalDate.of(2021, 2, 3));
		assertFalse(vis.isVisible());
		vis.setID("KJ");
		vis.setPrice(new Price(10, 0));
		assertTrue(vis.isVisible());
	}
	
	@Test
	public void testIDFunctions() {
		assertNull(first.getID());
		assertEquals(second.getID(), "KJ");
		Hat ident = new Hat("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(10, 0), new Price(11, 0), 180), new Price(9, 0), LocalDate.of(2021, 2, 3));
		assertNull(ident.getID());
		ident.setID("OKFS");
		assertEquals(ident.getID(), "OKFS");
	}
	
	@Test
	public void testPriceFunctions() throws NonVisibleListingException {
		testExpectedException(() -> {first.getPrice();}, NonVisibleListingException.class);
		assertEquals(second.getPrice(), new Price(15, 0));
		Hat pric = new Hat("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(10, 0), new Price(11, 0), 180), new Price(9, 0), "KJ", LocalDate.of(2021, 2, 3));
		testExpectedException(() -> {pric.getPrice();}, NonVisibleListingException.class);
		pric.setPrice(new Price(10, 0));
		assertEquals(pric.getPrice(), new Price(10, 0));
	}
	
	@Test
	public void testCopy() throws NonVisibleListingException {
		Hat secondCopy = second.copy();
		assertEquals(second, secondCopy);
		assertFalse(second == secondCopy);
		assertEquals(second.hashCode(), secondCopy.hashCode());
		assertEquals(second.getCommunityPrice(), secondCopy.getCommunityPrice());
		assertEquals(second.getDateBought(), secondCopy.getDateBought());
		assertEquals(second.getPrice(), secondCopy.getPrice());
		assertEquals(second.getPurchasePrice(), secondCopy.getPurchasePrice());
		assertEquals(second.getID(), secondCopy.getID());
		assertEquals(second.isVisible(), secondCopy.isVisible());
	}
	
	@Test
	public void testJSONFunctions() throws NonVisibleListingException {
		assertEquals(Hat.fromJSONRepresentation(first.getJSONRepresentation()), first);
		Hat secondJSON = Hat.fromJSONRepresentation(second.getJSONRepresentation());
		assertEquals(second, secondJSON);
		assertEquals(second.hashCode(), secondJSON.hashCode());
		assertEquals(second.getCommunityPrice(), secondJSON.getCommunityPrice());
		assertEquals(second.getDateBought(), secondJSON.getDateBought());
		assertEquals(second.getPrice(), secondJSON.getPrice());
		assertEquals(second.getPurchasePrice(), secondJSON.getPurchasePrice());
		assertEquals(second.getID(), secondJSON.getID());
		assertEquals(second.isVisible(), secondJSON.isVisible());
		testExpectedException(() -> {Hat.fromJSONRepresentation(null);}, NullPointerException.class);
		testExpectedException(() -> {Hat.fromJSONRepresentation(new JSONObject());}, JSONException.class);
	}
	
	@Test
	public void testGetListingRepresentation() throws NonVisibleListingException {
		JSONObject obj = second.getListingRepresentation();
		JSONObject comparison = new JSONObject("{\"intent\": 1, \"id\": \"KJ\", \"promoted\": 1, \"currencies\": {\"keys\": 15, \"metal\": 0}}");
		assertTrue(obj.similar(comparison));
	}
	
	@Test
	public void testFromListing() throws NonVisibleListingException {
		Hat secondFromListing = Hat.fromListing(second);
		assertEquals(second, secondFromListing);
		assertEquals(second.hashCode(), secondFromListing.hashCode());
		assertEquals(second.getCommunityPrice(), secondFromListing.getCommunityPrice());
		assertEquals(LocalDate.now(), secondFromListing.getDateBought());
		assertEquals(second.getPrice(), secondFromListing.getPurchasePrice());
		assertFalse(secondFromListing.isVisible());
		testExpectedException(() -> {Hat.fromListing(first);}, NonVisibleListingException.class);
		testExpectedException(() -> {Hat.fromListing(null);}, NullPointerException.class);
	}
	
	@Test
	public void testInvalidInput() {
		testExpectedException(() -> {new Hat(null, Effect.forName("Massed Flies"), new PriceRange(new Price(10, 0), new Price(12, 0), 180), new Price(8, 0), LocalDate.of(2021, 1, 20));}, NullPointerException.class);
		testExpectedException(() -> {new Hat("Anger", null, new PriceRange(new Price(10, 0), new Price(12, 0), 180), new Price(8, 0), "KJ", LocalDate.of(2021, 1, 20));}, NullPointerException.class);
		testExpectedException(() -> {new Hat("Anger", Effect.forName("Massed Flies"), null, new Price(8, 0), "KJ", LocalDate.of(2021, 1, 20));}, NullPointerException.class);
		testExpectedException(() -> {new Hat("Anger", Effect.forName("Massed Flies"), new PriceRange(new Price(10, 0), new Price(12, 0), 180), null, "KJ", LocalDate.of(2021, 1, 20));}, NullPointerException.class);
		testExpectedException(() -> {new Hat("Anger", Effect.forName("Massed Flies"), new PriceRange(new Price(10, 0), new Price(12, 0), 180), new Price(8, 0), "KJ", null);}, NullPointerException.class);
	}
	
	@Test
	public void testHash() {
		testHashCode(first, second, first.copy(), second.copy(), new Hat("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(10, 0), new Price(11, 0), 180), new Price(9, 0), LocalDate.of(2021, 2, 3)));
	}
	
	@Test
	public void testEquality() {
		testEquals(
				pair(first, first.copy()),
				pair(second, second.copy()),
				pair(first, new Hat("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(10, 0), new Price(11, 0), 180), new Price(9, 0), LocalDate.of(2021, 2, 3)))
		);
		testNotEquals(first, second, new Object());
	}
	
	@Test
	public void testToString() {
		assertNotNull(first.toString());
		assertNotNull(second.toString());
	}
}
