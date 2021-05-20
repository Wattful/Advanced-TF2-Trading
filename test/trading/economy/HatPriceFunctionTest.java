package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import java.io.IOException;
import java.time.LocalDate;

import org.json.JSONObject;
import org.junit.Test;

import trading.net.SampleBackpackTFConnection;

public class HatPriceFunctionTest {

	@Test
	public void testNegativeExponentialFunction() throws IOException {
		HatPriceFunction sampleFunction = HatPriceFunction.negativeExponentialFunction(0.9, 0.1, 4.0);
		Hat boughtToday = new Hat("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(19, 0), new Price(21, 0), 180), new Price(9, 0), LocalDate.now());
		Hat mid = new Hat("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(19, 0), new Price(21, 0), 180), new Price(9, 0), LocalDate.now().minusDays(5));
		Hat older = new Hat("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(19, 0), new Price(21, 0), 180), new Price(9, 0), LocalDate.of(-44, 3, 15));
		SampleBackpackTFConnection connection = new SampleBackpackTFConnection((Item i) -> {
			if(!i.equals(new Item("Backwards Ballcap", Quality.UNUSUAL, Effect.forName("Scorching Flames")))) {
				throw new IllegalArgumentException();
			}
			return new JSONObject(SampleBackpackTFConnection.getSampleListings());
		});
		assertEquals(sampleFunction.calculatePrice(boughtToday, connection, 900).first(), new Price(18, 0));
		assertTrue(sampleFunction.calculatePrice(older, connection, 900).first().compareTo(new Price(12, 0)) < 0);
		assertTrue(sampleFunction.calculatePrice(mid, connection, 900).first().compareTo(HatPriceFunction.negativeExponentialFunction(0.9, 0.1, 5.0).calculatePrice(mid, connection, 900).first()) > 0);
	}
	
	@Test
	public void testProfitByRatio() throws IOException {
		HatPriceFunction sampleFunction = HatPriceFunction.profitByRatio(0.1);
		Hat sampleHat = new Hat("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(19, 0), new Price(21, 0), 180), new Price(9, 0), LocalDate.of(2021, 2, 3));
		SampleBackpackTFConnection connection = new SampleBackpackTFConnection((Item i) -> {
			if(!i.equals(new Item("Backwards Ballcap", Quality.UNUSUAL, Effect.forName("Scorching Flames")))) {
				throw new IllegalArgumentException();
			}
			return new JSONObject(SampleBackpackTFConnection.getSampleListings());
		});
		
		assertEquals(sampleFunction.calculatePrice(sampleHat, connection, 900).first(), new Price(11, 0));
		
		testExpectedException(() -> {HatPriceFunction.profitByRatio(Double.NaN);}, IllegalArgumentException.class);
		testExpectedException(() -> {HatPriceFunction.profitByRatio(Double.POSITIVE_INFINITY);}, IllegalArgumentException.class);
		testExpectedException(() -> {HatPriceFunction.profitByRatio(-0.1);}, IllegalArgumentException.class);
	}
	
	@Test
	public void testFixedRatio() throws IOException {
		HatPriceFunction sampleFunction = HatPriceFunction.fixedRatio(0.9);
		Hat sampleHat = new Hat("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(19, 0), new Price(21, 0), 180), new Price(9, 0), LocalDate.of(2021, 2, 3));
		SampleBackpackTFConnection connection = new SampleBackpackTFConnection((Item i) -> {
			if(!i.equals(new Item("Backwards Ballcap", Quality.UNUSUAL, Effect.forName("Scorching Flames")))) {
				throw new IllegalArgumentException();
			}
			return new JSONObject(SampleBackpackTFConnection.getSampleListings());
		});
		
		assertEquals(sampleFunction.calculatePrice(sampleHat, connection, 180).first(), new Price(18, 0));
		
		testExpectedException(() -> {HatPriceFunction.fixedRatio(0.0);}, IllegalArgumentException.class);
		SampleBackpackTFConnection sampleConnection = new SampleBackpackTFConnection((Item i) -> {throw new IOException();});
		testExpectedException(() -> {sampleFunction.calculatePrice(null, sampleConnection, 1);}, NullPointerException.class);
		testExpectedException(() -> {sampleFunction.calculatePrice(sampleHat, sampleConnection, 0);}, IllegalArgumentException.class);
	}
	
	@Test
	public void testUndercutByRatio() throws IOException {
		HatPriceFunction sampleFunction = HatPriceFunction.undercutByRatio(2, 0.05, 0.9, false, "A");
		HatPriceFunction mustProfit = HatPriceFunction.undercutByRatio(2, 0.05, 0.9, true, "A");
		Hat sampleHat = new Hat("Backwards Ballcap", Effect.forName("Scorching Flames"), new PriceRange(new Price(19, 0), new Price(21, 0), 180), new Price(16, 0), LocalDate.of(2021, 2, 3));
		SampleBackpackTFConnection connection = new SampleBackpackTFConnection((Item i) -> {
			if(!i.equals(new Item("Backwards Ballcap", Quality.UNUSUAL, Effect.forName("Scorching Flames")))) {
				throw new IllegalArgumentException();
			}
			return new JSONObject(SampleBackpackTFConnection.getSampleListings());
		});
		assertEquals(sampleFunction.calculatePrice(sampleHat, connection, 900).first(), new Price(15, 46));
		assertEquals(sampleFunction.calculatePrice(sampleHat, new SampleBackpackTFConnection((Item i) -> {return new JSONObject("{\"buy\": {\"listings\": []}, \"sell\": {\"listings\": []}}");}), 900).first(), new Price(18, 0));
		assertEquals(mustProfit.calculatePrice(sampleHat, connection, 900).first(), new Price(16, 0));
		
		testExpectedException(() -> {sampleFunction.calculatePrice(new Hat("Backwards Ballcap", Effect.forName("Burning Flames"), new PriceRange(new Price(19, 0), new Price(21, 0), 180), new Price(16, 0), LocalDate.of(2021, 2, 3)), connection, 1);}, IllegalArgumentException.class);
		
		testExpectedException(() -> {HatPriceFunction.undercutByRatio(0, 0.05, 0.9, false, "A");}, IllegalArgumentException.class);
		testExpectedException(() -> {HatPriceFunction.undercutByRatio(2, 0.05, 0, false, "A");}, IllegalArgumentException.class);
		testExpectedException(() -> {HatPriceFunction.undercutByRatio(2, 0.05, 0.9, false, null);}, NullPointerException.class);
		testExpectedException(() -> {sampleFunction.calculatePrice(null, connection, 1);}, NullPointerException.class);
		testExpectedException(() -> {sampleFunction.calculatePrice(sampleHat, null, 1);}, NullPointerException.class);
		testExpectedException(() -> {sampleFunction.calculatePrice(sampleHat, connection, 0);}, IllegalArgumentException.class);
		testExpectedException(() -> {sampleFunction.calculatePrice(sampleHat, new SampleBackpackTFConnection((Item i) -> {throw new IOException();}), 1);}, IOException.class);
	}

}
