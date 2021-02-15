package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import java.io.IOException;
import org.json.*;

import org.junit.Test;

import trading.net.SampleBackpackTFConnection;

public class BuyListingPriceFunctionTest {
	@Test
	public void testOvercutByRatio() throws IOException {
		BuyListingPriceFunction sampleFunction = BuyListingPriceFunction.overcutByRatio(2, 0.05, 0.9, 0.6, "B");
		BuyListing sampleListing = new BuyListing("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(19, 0), new Price(21, 0), 180));
		SampleBackpackTFConnection connection = new SampleBackpackTFConnection((Item i) -> {
			if(!i.equals(new Item("Backwards Ballcap", Quality.UNUSUAL, Effect.forName("Scorching Flames")))) {
				throw new IllegalArgumentException();
			}
			return new JSONObject(SampleBackpackTFConnection.getSampleListings());
		});
		assertEquals(sampleFunction.calculatePrice(sampleListing, connection, 900), new Price(15, 41));
		assertEquals(sampleFunction.calculatePrice(sampleListing, new SampleBackpackTFConnection((Item i) -> {return new JSONObject("{\"buy\": {\"listings\": []}, \"sell\": {\"listings\": []}}");}), 900), new Price(12, 0));
		
		BuyListingPriceFunction lowMaxRatio = BuyListingPriceFunction.overcutByRatio(2, 0.05, 0.7, 0.5, "B");
		assertEquals(lowMaxRatio.calculatePrice(sampleListing, connection, 900), new Price(14, 0));
		
		testExpectedException(() -> {sampleFunction.calculatePrice(new BuyListing("Backwards Ballcap", Effect.forName("Burning Flames"), new PriceRange(new Price(19, 0), new Price(21, 0), 180)), connection, 1);}, IllegalArgumentException.class);
		
		testExpectedException(() -> {BuyListingPriceFunction.overcutByRatio(1, Double.NaN, 1.0, 1.0, "A");}, IllegalArgumentException.class);
		testExpectedException(() -> {BuyListingPriceFunction.overcutByRatio(1, 1.0, Double.NEGATIVE_INFINITY, 1.0, "A");}, IllegalArgumentException.class);
		testExpectedException(() -> {BuyListingPriceFunction.overcutByRatio(0, 1.0, 1.0, 1.0, "A");}, IllegalArgumentException.class);
		testExpectedException(() -> {BuyListingPriceFunction.overcutByRatio(1, 1.0, 0.0, 1.0, "A");}, IllegalArgumentException.class);
		testExpectedException(() -> {BuyListingPriceFunction.overcutByRatio(1, 1.0, 1.0, 0.0, "A");}, IllegalArgumentException.class);
		testExpectedException(() -> {BuyListingPriceFunction.overcutByRatio(1, 1.0, 1.0, 1.0, null);}, NullPointerException.class);
		testExpectedException(() -> {sampleFunction.calculatePrice(null, connection, 1);}, NullPointerException.class);
		testExpectedException(() -> {sampleFunction.calculatePrice(sampleListing, null, 1);}, NullPointerException.class);
		testExpectedException(() -> {sampleFunction.calculatePrice(sampleListing, connection, 0);}, IllegalArgumentException.class);
		testExpectedException(() -> {sampleFunction.calculatePrice(sampleListing, new SampleBackpackTFConnection((Item i) -> {throw new IOException();}), 1);}, IOException.class);
	}

	@Test
	public void testFixedRatio() throws IOException {
		BuyListingPriceFunction sampleFunction = BuyListingPriceFunction.fixedRatio(0.9);
		BuyListing sampleListing = new BuyListing("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(19, 0), new Price(21, 0), 180));
		SampleBackpackTFConnection connection = new SampleBackpackTFConnection((Item i) -> {
			if(!i.equals(new Item("Backwards Ballcap", Quality.UNUSUAL, Effect.forName("Scorching Flames")))) {
				throw new IllegalArgumentException();
			}
			return new JSONObject(SampleBackpackTFConnection.getSampleListings());
		});
		
		assertEquals(sampleFunction.calculatePrice(sampleListing, connection, 180), new Price(18, 0));
		
		testExpectedException(() -> {BuyListingPriceFunction.fixedRatio(0.0);}, IllegalArgumentException.class);
		SampleBackpackTFConnection sampleConnection = new SampleBackpackTFConnection((Item i) -> {throw new IOException();});
		testExpectedException(() -> {sampleFunction.calculatePrice(null, sampleConnection, 1);}, NullPointerException.class);
		testExpectedException(() -> {sampleFunction.calculatePrice(sampleListing, sampleConnection, 0);}, IllegalArgumentException.class);
	}
}
