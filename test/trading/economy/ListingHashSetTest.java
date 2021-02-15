package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import java.time.LocalDate;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class ListingHashSetTest {
	private static final ListingHashSet<BuyListing> empty = new ListingHashSet<>();
	private static final ListingHashSet<Hat> one;
	static {
		Hat h = new Hat("Anger", Effect.forName("Massed Flies"), new PriceRange(new Price(10, 0), new Price(12, 0), 180), new Price(8, 0), "KJ", LocalDate.of(2021, 1, 20));
		h.setPrice(new Price(15, 0));
		one = new ListingHashSet<>(List.of(h));
	}

	@Test
	public void testJSONFunctions() {
		assertEquals(ListingHashSet.buyListingSetFromJSON(empty.getJSONRepresentation()), empty);
		assertEquals(ListingHashSet.hatSetFromJSON(one.getJSONRepresentation()), one);
		testExpectedException(() -> {ListingHashSet.buyListingSetFromJSON(null);}, NullPointerException.class);
		testExpectedException(() -> {ListingHashSet.hatSetFromJSON(null);}, NullPointerException.class);
		testExpectedException(() -> {ListingHashSet.buyListingSetFromJSON(new JSONArray("[0, 1]"));}, JSONException.class);
		testExpectedException(() -> {ListingHashSet.hatSetFromJSON(new JSONArray("[0, 1]"));}, JSONException.class);
	}
	
	@Test
	public void testGetListingRepresentation() throws NonVisibleListingException {
		ListingDescriptionFunction simpleDescription = ListingDescriptionFunction.simpleDescription();
		Hat h = new Hat("Anger", Effect.forName("Massed Flies"), new PriceRange(new Price(10, 0), new Price(12, 0), 180), new Price(8, 0), "KJ", LocalDate.of(2021, 1, 20));
		h.setPrice(new Price(15, 0));
		JSONObject hListing = h.getListingRepresentation();
		hListing.put("details", simpleDescription.generateDescription(h));
		JSONObject hListing2 = h.getListingRepresentation();
		Hat h2 = new Hat("Anger", Effect.forName("Massed Flies"), new PriceRange(new Price(10, 0), new Price(12, 0), 180), new Price(8, 0), "KJ", LocalDate.of(2021, 1, 20));
		ListingHashSet<Hat> hats = new ListingHashSet<>(List.of(h, h2));
		assertTrue(hats.getListingRepresentation(simpleDescription).similar(new JSONArray("[" + hListing.toString() + "]")));
		assertTrue(hats.getListingRepresentation(null).similar(new JSONArray("[" + hListing2.toString() + "]")));
	}
	
	@Test
	public void testCopy() {
		ListingHashSet<BuyListing> emptyCopy = empty.copy();
		assertEquals(emptyCopy, empty);
		assertFalse(emptyCopy == empty);
		ListingHashSet<Hat> oneCopy = one.copy();
		assertEquals(oneCopy, one);
		assertFalse(oneCopy == one);
	}
	
	@Test
	public void testToString() {
		assertNotNull(empty.toString());
		assertNotNull(one.toString());
	}

}
