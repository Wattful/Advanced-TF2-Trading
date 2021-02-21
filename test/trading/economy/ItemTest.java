package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import org.junit.Test;

public class ItemTest {
	private static final Item eureka = new Item("Strange Eureka Effect", Quality.STRANGE); //If only :(
	private static final Item cap = new Item("Team Captain", Quality.UNUSUAL, Effect.forInt(11));
	private static final Item fakeUnusual = new Item("War Pig", Quality.UNIQUE, Effect.forInt(34));
	
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
	public void testInvalidInput() {
		testExpectedException(() -> {new Item(null, Quality.STRANGE);}, NullPointerException.class);
		testExpectedException(() -> {new Item("Liquidator's lid", null);}, NullPointerException.class);
		testExpectedException(() -> {new Item("Liquidator's lid", Quality.UNUSUAL, null);}, NullPointerException.class);
	}
	
	@Test
	public void testHash() {
		testHashCode(eureka, cap, fakeUnusual, new Item("Strange EUREKA effect", Quality.STRANGE), new Item("Team Captain", Quality.UNUSUAL, Effect.forInt(11)), new Item("War Pig", Quality.UNIQUE, Effect.forInt(34)));
	}
	
	@Test
	public void testEquality() {
		testEquals(
				pair(eureka, new Item("Strange EUREKA effect", Quality.STRANGE)),
				pair(cap, new Item("Team Captain", Quality.UNUSUAL, Effect.forInt(11))),
				pair(fakeUnusual, new Item("War Pig", Quality.UNIQUE, Effect.forInt(99)))
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
