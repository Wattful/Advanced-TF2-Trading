package trading.net;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

public class SampleSteamConnection implements SteamConnection {
	private static final String SAMPLE_INVENTORY_PATH = "./test/trading/net/sampleSteamInventory.json";
	
	@Override
	public JSONObject getInventoryForUser(String steamID) throws IOException {
		return new JSONObject(new String(Files.readAllBytes(Paths.get(SAMPLE_INVENTORY_PATH))));
	}

}
