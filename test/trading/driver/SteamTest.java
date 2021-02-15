package trading.driver;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;
import org.junit.Test;

public class SteamTest {
	private static final Steam steam = Steam.open();
	private static final String id;
	static {
		try {
			JSONObject config = new JSONObject(new String(Files.readAllBytes(Paths.get("./test/trading/driver/testconfig.json"))));
			id = config.getString("steamid");
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	@Test
	public void test() throws IOException {
		JSONObject response = steam.getInventoryForUser(id);
		assertNotNull(response);
		assertEquals(response.getBoolean("success"), true);
		testExpectedException(() -> {steam.getInventoryForUser(null);}, NullPointerException.class);
	}

	
	public static void main(String[] args) throws IOException {
		JSONObject response = steam.getInventoryForUser(id);
		System.out.println(response.toString());
	}
}
