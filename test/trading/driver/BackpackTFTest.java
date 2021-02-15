package trading.driver;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

import trading.economy.*;

public class BackpackTFTest {
	private static final BackpackTF backpackTF;
	static {
		try {
			JSONObject config = new JSONObject(new String(Files.readAllBytes(Paths.get("./test/trading/driver/testconfig.json"))));
			backpackTF = BackpackTF.open(config.getString("apiKey"), config.getString("apiToken"));
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Test
	public void testSendListings() throws IOException {
		backpackTF.sendListings(new ListingHashSet<Hat>(List.of()), ListingDescriptionFunction.simpleDescription());
	
		testExpectedException(() -> {backpackTF.sendListings(null, ListingDescriptionFunction.simpleDescription());}, NullPointerException.class);
	}
	
	@Test
	public void testGetPricesObject() throws IOException {
		JSONObject response = backpackTF.getPricesObject();
		assertNotNull(response);
	}
	
	@Test
	public void testGetListingsForItem() throws IOException {
		JSONObject response = backpackTF.getListingsForItem(new Item("Anger", Quality.UNUSUAL, Effect.forInt(14)));
		assertNotNull(response);
		
		testExpectedException(() -> {backpackTF.getListingsForItem(null);}, NullPointerException.class);
	}
	
	@Test
	public void testUsed() throws IOException {
		backpackTF.sendListings(new ListingHashSet<Hat>(List.of()), ListingDescriptionFunction.simpleDescription());
		assertTrue(backpackTF.hasBeenUsed());
		backpackTF.resetUsed();
		assertFalse(backpackTF.hasBeenUsed());
	}
	
	@Test
	public void testInvalidInput() {
		testExpectedException(() -> {BackpackTF.open(null, "");}, NullPointerException.class);
		testExpectedException(() -> {BackpackTF.open("", null);}, NullPointerException.class);
	}
	
	public static void main(String[] args) throws IOException {
		BuyListing bl = new BuyListing("Anger", Effect.forName("Massed Flies"), new PriceRange(new Price(10, 0), new Price(12, 0), 180));
		bl.setPrice(new Price(15, 0));
		backpackTF.sendListings(new ListingHashSet<BuyListing>(List.of(bl)), ListingDescriptionFunction.simpleDescription());
		JSONObject response = backpackTF.getListingsForItem(new Item("Anger", Quality.UNUSUAL, Effect.forInt(14)));
		System.out.println(response);
	}

}
