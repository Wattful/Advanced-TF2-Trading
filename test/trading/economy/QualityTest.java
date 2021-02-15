package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

public class QualityTest {	
	@Test
	public void testRemovePrefix() {
		assertEquals(Quality.UNUSUAL.removePrefix("Unusual Hat"), "Hat");
		assertEquals(Quality.UNIQUE.removePrefix("The Hat"), "Hat");
		assertEquals(Quality.UNIQUE.removePrefix("Unique Hat"), "Unique Hat");
		testExpectedException(() -> {Quality.UNUSUAL.removePrefix(null);}, NullPointerException.class);
	}
	
	@Test
	public void testGetName() {
		assertEquals(Quality.UNUSUAL.getName(), "Unusual");
		assertEquals(Quality.UNIQUE.getName(), "Unique");
	}
	
	@Test
	public void testGetIntValue() {
		assertEquals(Quality.UNUSUAL.getIntValue(), 5);
		assertEquals(Quality.UNIQUE.getIntValue(), 6);
	}

	@Test
	public void testForName() {
		assertEquals(Quality.forName("unusual"), Quality.UNUSUAL);
		assertEquals(Quality.forName("UNIQUE"), Quality.UNIQUE);
		testExpectedException(() -> {Quality.forName(null);}, NullPointerException.class);
		testExpectedException(() -> {Quality.forName("a");}, NoSuchElementException.class);
	}
	
	@Test
	public void testForInt() {
		assertEquals(Quality.forInt(5), Quality.UNUSUAL);
		assertEquals(Quality.forInt(6), Quality.UNIQUE);
		testExpectedException(() -> {Quality.forInt(-1);}, NoSuchElementException.class);
	}
}
