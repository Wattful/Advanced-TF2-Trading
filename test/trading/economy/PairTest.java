package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import org.junit.Test;

public class PairTest {
	private static Pair<String, String> neitherNull = Pair.of("epic", "gamer moment");
	private static Pair<String, String> firstNull = Pair.of(null, "gamer moment");
	private static Pair<String, String> secondNull = Pair.of("epic", null);
	private static Pair<String, String> bothNull = Pair.of(null, null);

	@Test
	public void testValues() {
		assertEquals(neitherNull.first(), "epic");
		assertEquals(neitherNull.second(), "gamer moment");
		assertEquals(firstNull.first(), null);
		assertEquals(firstNull.second(), "gamer moment");
		assertEquals(secondNull.first(), "epic");
		assertEquals(secondNull.second(), null);
		assertEquals(bothNull.first(), null);
		assertEquals(bothNull.second(), null);
	}
	
	@Test
	public void testReverse() {
		assertEquals(neitherNull.reversePair(), new Pair<String, String>("gamer moment", "epic"));
		assertEquals(firstNull.reversePair(), new Pair<String, String>("gamer moment", null));
		assertEquals(secondNull.reversePair(), new Pair<String, String>(null, "epic"));
		assertEquals(bothNull.reversePair(), new Pair<String, String>(null, null));
	}
	
	@Test
	public void testToString() {
		assertEquals(neitherNull.toString(), "(epic, gamer moment)");
		assertEquals(firstNull.toString(), "(null, gamer moment)");
		assertEquals(secondNull.toString(), "(epic, null)");
		assertEquals(bothNull.toString(), "(null, null)");
	}
	
	@Test
	public void testEquality() {
		testNotEquals(neitherNull, firstNull, secondNull, bothNull, new Object());
		testEquals(
				pair(neitherNull, pair("epic", "gamer moment")),
				pair(firstNull, pair(null, "gamer moment")),
				pair(secondNull, pair("epic", null)),
				pair(bothNull, pair(null, null))
		);
	}
	
	@Test
	public void testHash() {
		testHashCode(neitherNull, firstNull, secondNull, bothNull, pair("epic", "gamer moment"), pair(null, "gamer moment"), pair("epic", null), pair(null, null));
	}

}
