package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import java.util.NoSuchElementException;

import org.junit.Test;

public class EffectTest {
	private static final Effect burningFlames = Effect.forName("Burning Flames");
	private static final Effect massedFlies = Effect.forName("Massed Flies");
	
	@Test
	public void testGetName() {
		assertEquals(burningFlames.getName(), "Burning Flames");
		assertEquals(massedFlies.getName(), "Massed Flies");
	}
	
	@Test
	public void testGetIntValue() {
		assertEquals(burningFlames.getIntValue(), 13);
		assertEquals(massedFlies.getIntValue(), 12);
	}

	@Test
	public void testForName() {
		assertEquals(Effect.forName("circling TF logo").getName(), "Circling TF Logo");
		assertEquals(Effect.forName("SCORCHING flames").getName(), "Scorching Flames");
		testExpectedException(() -> {Effect.forName(null);}, NullPointerException.class);
		testExpectedException(() -> {Effect.forName("a");}, NoSuchElementException.class);
	}
	
	@Test
	public void testForInt() {
		assertEquals(Effect.forInt(5).getName(), "Holy Glow");
		assertEquals(Effect.forInt(6).getName(), "Green Confetti");
		testExpectedException(() -> {Effect.forInt(-1);}, NoSuchElementException.class);
	}
}
