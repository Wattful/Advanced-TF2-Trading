package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class PriceRangeTest {
	private static final PriceRange range = new PriceRange(new Price(10, 0), new Price(9, 0), 90);
	private static final PriceRange one = new PriceRange(new Price(20, 0));
	
	@Test
	public void testUpper() {
		assertEquals(range.upper(), new Price(10, 0));
		assertEquals(one.upper(), new Price(20, 0));
	}
	
	@Test
	public void testLower() {
		assertEquals(range.lower(), new Price(9, 0));
		assertEquals(one.lower(), new Price(20, 0));
	}
	
	@Test
	public void testMiddle() {
		assertEquals(range.middle(), new Price(9, 5));
		assertEquals(one.middle(), new Price(20, 0));
	}
	
	@Test
	public void testJSONFunctions() {
		assertEquals(PriceRange.fromJSONRepresentation(range.getJSONRepresentation()), range);
		assertEquals(PriceRange.fromJSONRepresentation(one.getJSONRepresentation()), one);
		testExpectedException(() -> {PriceRange.fromJSONRepresentation(new JSONObject());}, JSONException.class);
		testExpectedException(() -> {PriceRange.fromJSONRepresentation(null);}, NullPointerException.class);
	}
	
	@Test
	public void testAcceptableCurrency() {
		assertTrue(PriceRange.acceptableCurrency(new JSONObject("{\"value\": 10, \"currency\": \"keys\"}")));
		assertTrue(PriceRange.acceptableCurrency(new JSONObject("{\"value\": 10, \"currency\": \"metal\"}")));
		assertFalse(PriceRange.acceptableCurrency(new JSONObject("{\"value\": 10, \"currency\": \"USD\"}")));
		
		testExpectedException(() -> {PriceRange.acceptableCurrency(null);}, NullPointerException.class);
		testExpectedException(() -> {PriceRange.acceptableCurrency(new JSONObject());}, JSONException.class);
	}
	
	@Test
	public void testFromBackpackTFRepresentation() {
		JSONObject keysOne = new JSONObject("{currency: \"keys\", value: 10}");
		JSONObject keysRange = new JSONObject("{currency: \"keys\", value: 10, value_high: 19}");
		JSONObject metalOne = new JSONObject("{currency: \"metal\", value: 10}");
		assertEquals(PriceRange.fromBackpackTFRepresentation(keysOne, 1), new PriceRange(new Price(10, 0)));
		assertEquals(PriceRange.fromBackpackTFRepresentation(keysRange, 90), new PriceRange(new Price(10, 0), new Price(19, 0), 90));
		assertEquals(PriceRange.fromBackpackTFRepresentation(metalOne, 180), new PriceRange(new Price(0, 10)));
		testExpectedException(() -> {PriceRange.fromBackpackTFRepresentation(null, 1);}, NullPointerException.class);
		testExpectedException(() -> {PriceRange.fromBackpackTFRepresentation(keysOne, 0);}, IllegalArgumentException.class);
		testExpectedException(() -> {PriceRange.fromBackpackTFRepresentation(new JSONObject("{currency: \"usd\", value: 10}"), 1);}, IllegalArgumentException.class);
		testExpectedException(() -> {PriceRange.fromBackpackTFRepresentation(new JSONObject(), 1);}, JSONException.class);
	}
	
	@Test
	public void testInvalidInput() {
		testExpectedException(() -> {new PriceRange(null);}, NullPointerException.class);
		testExpectedException(() -> {new PriceRange(null, new Price(10, 0), 1);}, NullPointerException.class);
		testExpectedException(() -> {new PriceRange(new Price(10, 0), null, 1);}, NullPointerException.class);
		testExpectedException(() -> {new PriceRange(new Price(10, 0), new Price(10, 0), 0);}, IllegalArgumentException.class);
	}
	
	@Test
	public void testHash() {
		testHashCode(range, one, new PriceRange(new Price(10, 0), new Price(9, 0), 90), new PriceRange(new Price(20, 0)));
	}
	
	@Test
	public void testEquality() {
		testEquals(
				pair(range, new PriceRange(new Price(10, 0), new Price(9, 0), 90)),
				pair(one, new PriceRange(new Price(20, 0))),
				pair(new PriceRange(new Price(10, 0), new Price(9, 0), 90), new PriceRange(new Price(9, 0), new Price(10, 0), 90))
		);
		testNotEquals(range, one, new Object());
	}
	
	@Test
	public void testToString() {
		assertNotNull(range.toString());
		assertNotNull(one.toString());
	}
}
