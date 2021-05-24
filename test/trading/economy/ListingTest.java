package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import org.json.JSONObject;
import org.junit.Test;

public class ListingTest {
	private static Listing sample = new SampleListing("Cool Cat Cardigan", Effect.forName("Burning Flames"), new PriceRange(new Price(10, 0), new Price(15, 0), 180));
	
	@Test
	public void testGetName() {
		assertEquals(sample.getName(), "Cool Cat Cardigan");
	}
	
	@Test
	public void testGetQuality() {
		assertEquals(sample.getQuality(), Quality.UNUSUAL);
	}
	
	@Test
	public void testGetEffect() {
		assertEquals(sample.getEffect(), Effect.forName("Burning Flames"));
	}
	
	@Test
	public void testGetPriority() {
		sample.setPriority(11);
		assertEquals(sample.getPriority(), (Integer)11);
	}
	
	@Test
	public void testCommunityPriceFunctions() {
		assertEquals(sample.getCommunityPrice(), new PriceRange(new Price(10, 0), new Price(15, 0), 180));
		Listing changeling = new SampleListing("Cool Cat Cardigan", Effect.forName("Burning Flames"), new PriceRange(new Price(10, 0), new Price(15, 0), 180));
		changeling.changeCommunityPrice(new PriceRange(new Price(10, 0), new Price(20, 0), 180));
		assertEquals(changeling.getCommunityPrice(), new PriceRange(new Price(10, 0), new Price(20, 0), 180));
	}
	
	@Test
	public void testInvalidInput() {
		testExpectedException(() -> {new SampleListing(null, Effect.forName("Burning Flames"), new PriceRange(new Price(10, 0), new Price(15, 0), 180));}, NullPointerException.class);
		testExpectedException(() -> {new SampleListing("Liquidator's lid", null, new PriceRange(new Price(10, 0), new Price(15, 0), 180));}, NullPointerException.class);
		testExpectedException(() -> {new SampleListing("Liquidator's lid", Effect.forName("Burning Flames"), null);}, NullPointerException.class);
	}
	
	@Test
	public void testHash() {
		testHashCode(sample, new SampleListing("Cool Cat Cardigan", Effect.forName("Burning Flames"), new PriceRange(new Price(10, 0), new Price(15, 0), 180)));
	}
	
	@Test
	public void testEquality() {
		testEquals(
				pair(sample, new SampleListing("Cool Cat Cardigan", Effect.forName("Burning Flames"), new PriceRange(new Price(10, 0), new Price(15, 0), 180)))
		);
		testNotEquals(sample, new Object());
	}
	
	@Test
	public void testToString() {
		assertNotNull(sample.toString());
	}

	private static class SampleListing extends Listing{
		public SampleListing(String name, Effect effect, PriceRange communityPrice) {
			super(name, effect, communityPrice);
		}

		public JSONObject getJSONRepresentation() {
			return null;
		}

		public JSONObject getListingRepresentation() throws NonVisibleListingException{
			return null;
		}

		public boolean isVisible() {
			return false;
		}

		public void setPrice(Price newPrice) {}

		public Price getPrice() throws NonVisibleListingException{
			return null;
		}

		public Listing copy() {
			return null;
		}

		@Override
		public String toString() {
			return "SampleListing";
		}
	}
}
