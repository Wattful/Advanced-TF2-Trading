package trading.economy;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import java.time.LocalDate;

import org.junit.Test;

public class ListingDescriptionFunctionTest {
	private static Hat hat = new Hat("Anger", Effect.forName("Massed Flies"), new PriceRange(new Price(10, 0), new Price(12, 0), 180), new Price(8, 0), "KJ", LocalDate.of(2021, 1, 20));
	private static BuyListing buyListing = new BuyListing("Anger", Effect.forName("Massed Flies"), new PriceRange(new Price(10, 0), new Price(12, 0), 180));
	static {
		hat.setPrice(new Price(15, 0));
		buyListing.setPrice(new Price(15, 10));
	}
	
	@Test
	public void testSimpleDescription() throws NonVisibleListingException {
		ListingDescriptionFunction simpleDescription = ListingDescriptionFunction.simpleDescription();
		assertEquals(simpleDescription.generateDescription(hat), "Selling this hat for 15 keys. Send me an offer, I will accept instantly! Item offers held for manual review.");
		assertEquals(simpleDescription.generateDescription(buyListing), "Buying this hat for 15 keys and 10 refined. Send me an offer, I will accept instantly! Item offers held for manual review.");
		
		testExpectedException(() -> {simpleDescription.generateDescription(null);}, NullPointerException.class);
		testExpectedException(() -> {simpleDescription.generateDescription(new Hat("Anger", Effect.forName("Massed Flies"), new PriceRange(new Price(10, 0), new Price(12, 0), 180), new Price(8, 0), "KJ", LocalDate.of(2021, 1, 20)));}, NonVisibleListingException.class);
	}
	
	@Test
	public void testDescriptionWithSayings() throws NonVisibleListingException {
		ListingDescriptionFunction before = ListingDescriptionFunction.descriptionWithSayings(true, "Saying 1");
		ListingDescriptionFunction after = ListingDescriptionFunction.descriptionWithSayings(false, "Saying 2");
		assertEquals(before.generateDescription(hat), "Saying 1 Selling this hat for 15 keys. Send me an offer, I will accept instantly! Item offers held for manual review.");
		assertEquals(after.generateDescription(hat), "Selling this hat for 15 keys. Send me an offer, I will accept instantly! Item offers held for manual review. Saying 2");
		assertEquals(before.generateDescription(buyListing), "Saying 1 Buying this hat for 15 keys and 10 refined. Send me an offer, I will accept instantly! Item offers held for manual review.");
		assertEquals(after.generateDescription(buyListing), "Buying this hat for 15 keys and 10 refined. Send me an offer, I will accept instantly! Item offers held for manual review. Saying 2");
		
		testExpectedException(() -> {ListingDescriptionFunction.descriptionWithSayings(true, (String[])null);}, NullPointerException.class);
		testExpectedException(() -> {ListingDescriptionFunction.descriptionWithSayings(true, "Saying 1", null);}, NullPointerException.class);
		testExpectedException(() -> {ListingDescriptionFunction.descriptionWithSayings(true);}, IllegalArgumentException.class);
		testExpectedException(() -> {before.generateDescription(null);}, NullPointerException.class);
		testExpectedException(() -> {before.generateDescription(new Hat("Anger", Effect.forName("Massed Flies"), new PriceRange(new Price(10, 0), new Price(12, 0), 180), new Price(8, 0), "KJ", LocalDate.of(2021, 1, 20)));}, NonVisibleListingException.class);
	}

}
