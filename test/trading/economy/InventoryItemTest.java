package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class InventoryItemTest {
	private static final InventoryItem eureka = new InventoryItem("Strange Eureka Effect", Quality.STRANGE, null, "a"); //If only :(
	private static final InventoryItem cap = new InventoryItem("Team Captain", Quality.UNUSUAL, Effect.forInt(11), "b");
	private static final InventoryItem fakeUnusual = new InventoryItem("War Pig", Quality.UNIQUE, Effect.forInt(34), "c");
	
	@Test
	public void testGetName() {
		assertEquals(eureka.getName(), "Eureka Effect");
		assertEquals(cap.getName(), "Team Captain");
		assertEquals(fakeUnusual.getName(), "War Pig");
	}
	
	@Test
	public void testGetQuality() {
		assertEquals(eureka.getQuality(), Quality.STRANGE);
		assertEquals(cap.getQuality(), Quality.UNUSUAL);
		assertEquals(fakeUnusual.getQuality(), Quality.UNIQUE);
	}
	
	@Test
	public void testGetEffect() {
		assertEquals(eureka.getEffect(), null);
		assertEquals(cap.getEffect(), Effect.forInt(11));
		assertEquals(fakeUnusual.getEffect(), null);
	}
	
	@Test
	public void testGetID() {
		assertEquals(eureka.getID(), "a");
		assertEquals(cap.getID(), "b");
		assertEquals(fakeUnusual.getID(), "c");
	}
	
	@Test
	public void testParseEffect() {
		JSONArray burning = new JSONArray("[{color: \"00ffbb\", value: \"dummy\"}, {color: \"ffd700\", value: \"Unusual Effect: Burning Flames\"}]"); 
		JSONArray nonUnusual = new JSONArray("[{color: \"00ffbb\", value: \"dummy\"}]");
		assertEquals(InventoryItem.parseEffect(burning), Effect.forName("Burning Flames"));
		assertNull(InventoryItem.parseEffect(nonUnusual));
		testExpectedException(() -> {InventoryItem.parseEffect(null);}, NullPointerException.class);
	}
	
	@Test
	public void testInvalidInput() {
		testExpectedException(() -> {new InventoryItem(null, Quality.STRANGE, null, "a");}, NullPointerException.class);
		testExpectedException(() -> {new InventoryItem("Liquidator's lid", null, null, "b");}, NullPointerException.class);
		testExpectedException(() -> {new InventoryItem("Liquidator's lid", Quality.STRANGE, null, null);}, NullPointerException.class);
		testExpectedException(() -> {new InventoryItem("Liquidator's lid", Quality.UNUSUAL, null, "c");}, NullPointerException.class);
	}
	
	@Test
	public void testHash() {
		testHashCode(eureka, cap, fakeUnusual, new InventoryItem("Strange Eureka Effect", Quality.STRANGE, null, "t"), new InventoryItem("Team Captain", Quality.UNUSUAL, Effect.forInt(11), "e"), new InventoryItem("War Pig", Quality.UNIQUE, Effect.forInt(34), "l"));
	}
	
	@Test
	public void testEquality() {
		testEquals(
				pair(eureka, new InventoryItem("Strange Eureka Effect", Quality.STRANGE, null, "f")),
				pair(cap, new InventoryItem("Team Captain", Quality.UNUSUAL, Effect.forInt(11), "e")),
				pair(fakeUnusual, new InventoryItem("War Pig", Quality.UNIQUE, Effect.forInt(99), "d"))
		);
		testNotEquals(eureka, cap, fakeUnusual, new Object());
	}
	
	@Test
	public void testToString() {
		assertNotNull(eureka.toString());
		assertNotNull(cap.toString());
		assertNotNull(fakeUnusual.toString());
	}
}
