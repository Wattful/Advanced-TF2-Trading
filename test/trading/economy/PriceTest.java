package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;


public class PriceTest {
	private static final Price first = new Price(25, 10);
	private static final Price second = new Price(10, 0);
	
	@Test
	public void testGetKeys() {
		assertEquals(first.getKeys(), 25);
		assertEquals(second.getKeys(), 10);
	}
	
	@Test
	public void testGetRefined() {
		assertEquals(first.getRefined(), 10);
		assertEquals(second.getRefined(), 0);
	}
	
	@Test
	public void testGetDecimalPrice() {
		assertEquals((Double)first.getDecimalPrice(9), (Double)35.0);
		assertEquals((Double)second.getDecimalPrice(1), (Double)10.0);
		testExpectedException(() -> {first.getDecimalPrice(0);}, IllegalArgumentException.class);
	}
	
	@Test
	public void testJSONFunctions() {
		assertEquals(Price.fromJSONRepresentation(first.getJSONRepresentation()), first);
		assertEquals(Price.fromJSONRepresentation(second.getJSONRepresentation()), second);
		assertTrue(first.getJSONRepresentation().similar(new JSONObject("{keys: 25, metal: 10}")));
		assertTrue(second.getJSONRepresentation().similar(new JSONObject("{keys: 10, metal: 0}")));
		testExpectedException(() -> {Price.fromJSONRepresentation(new JSONObject());}, JSONException.class);
		testExpectedException(() -> {Price.fromJSONRepresentation(null);}, NullPointerException.class);
	}
	
	@Test
	public void testScaleBy() {
		assertEquals(second.scaleBy(1.1, 1), new Price(11, 0));
		testExpectedException(() -> {second.scaleBy(0, 0);}, IllegalArgumentException.class);
	}
	
	@Test
	public void testCalculate() {
		assertEquals(Price.calculate(10.0, 35), new Price(10, 0));
		assertEquals(Price.calculate(10.5, 18), new Price(10, 1));
		testExpectedException(() -> {Price.calculate(1, 0);}, IllegalArgumentException.class);
	}
	
	@Test
	public void testAverage() {
		assertEquals(Price.average(180, first, second), new Price(17, 15));
		assertEquals(Price.average(1, second), second);
		testExpectedException(() -> {Price.average(1);}, IllegalArgumentException.class);
		testExpectedException(() -> {Price.average(-1, second);}, IllegalArgumentException.class);
		testExpectedException(() -> {Price.average(1, (Price[])null);}, NullPointerException.class);
		testExpectedException(() -> {Price.average(1, first, null);}, NullPointerException.class);
	}
	
	@Test
	public void testValueString() {
		assertNotNull(first.valueString());
		assertNotNull(second.valueString());
	}
	
	@Test
	public void testCompareTo() {
		assertEquals(first.compareTo(new Price(25, 10)), 0);
		assertTrue(first.compareTo(new Price(25, 9)) > 0);
		assertTrue(first.compareTo(new Price(25, 11)) < 0);
		assertTrue(first.compareTo(new Price(10, 11)) > 0);
		assertTrue(first.compareTo(new Price(29, 11)) < 0);
	}
	
	@Test
	public void testInvalidInput() {
		testExpectedException(() -> {new Price(-1, 1);}, IllegalArgumentException.class);
		testExpectedException(() -> {new Price(1, -1);}, IllegalArgumentException.class);
	}
	
	@Test
	public void testHash() {
		testHashCode(first, second, new Price(25, 10), new Price(10, 0));
	}
	
	@Test
	public void testEquality() {
		testEquals(
				pair(first, new Price(25, 10)),
				pair(second, new Price(10, 0))
		);
		testNotEquals(first, second, new Object());
	}
	
	@Test
	public void testToString() {
		assertNotNull(first.toString());
		assertNotNull(second.toString());
	}
}
