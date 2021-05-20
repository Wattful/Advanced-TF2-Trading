package trading.net;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import trading.economy.*;

import org.json.JSONObject;

public class SampleBackpackTFConnection implements BackpackTFConnection {
	private static final String SAMPLE_PRICES_PATH = "./test/trading/net/inventory.json";
	private static final String SAMPLE_LISTINGS_PATH = "./test/trading/net/sampleListingsResponse.json";
	
	private final ListingsReturnFunction lrf;
	
	public SampleBackpackTFConnection(ListingsReturnFunction lrf){
		this.lrf = lrf;
	}
	
	public void sendListings(ListingCollection<? extends Listing> listings, ListingDescriptionFunction ldf) {}
	
	public JSONObject getListingsForItem(Item i) throws IOException {
		return this.lrf.getListings(i);
	}
	
	public JSONObject getPricesObject() throws IOException {
		return new JSONObject(new String(Files.readAllBytes(Paths.get(SAMPLE_PRICES_PATH))));
	}
	
	public static String getSampleListings() {
		try {
			return new String(Files.readAllBytes(Paths.get(SAMPLE_LISTINGS_PATH)));
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void heartbeat() throws IOException {
		throw new IOException();
	}
	
	public static interface ListingsReturnFunction{
		JSONObject getListings(Item i) throws IOException;
	}
}
