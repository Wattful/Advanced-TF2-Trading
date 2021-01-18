package trading.driver;

import trading.net.*;
import java.io.*;
import org.json.*;

//TODO:

/**An implementation of the SteamConnection interface.
*/

public class Steam implements SteamConnection{
	private Steam(){}

	/**Returns an instance of Steam.
	@return an instance of Steam.
	*/
	static Steam open(){
		return new Steam();
	}

	/**Returns the result of a Steam user inventory API call for the given ID.
	@param steamID the ID to lookup.
	@throws NullPointerException if steamID is null.
	@throws IOException if an IO error occurs.
	@return the result of a Steam user inventory API call for the given ID.
	*/
	public JSONObject getInventoryForUser(String steamID) throws IOException {
		return NetUtils.request("https://steamcommunity.com/profiles/" + steamID + "/inventory/json/440/2", "GET", new JSONObject());
	}
}