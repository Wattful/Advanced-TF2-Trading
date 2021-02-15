package trading.net;

import static org.junit.Assert.*;
import static trading.economy.StaticTests.*;

import java.io.IOException;

import org.json.JSONObject;
import org.junit.Test;

public class NetUtilsTest {
	@Test
	public void testRequest() throws IOException {
		JSONObject response = NetUtils.request("https://httpbin.org/anything", "GET", new JSONObject());
		assertTrue(response.getJSONObject("args").similar(new JSONObject()));
		
		JSONObject arguments = new JSONObject();
		arguments.put("answer", "42");
		response = NetUtils.request("https://httpbin.org/anything", "GET", arguments);
		assertTrue(response.getJSONObject("args").similar(arguments));
		
		response = NetUtils.request("https://httpbin.org/anything", "POST", arguments);
		assertEquals(response.getString("data"), arguments.toString());
	}

}
