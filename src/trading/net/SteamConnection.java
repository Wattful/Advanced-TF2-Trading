package trading.net;

import org.json.*;
import java.io.*;

//TODO:

/**Interface representing a connection to the Steam API.
*/

public interface SteamConnection{
	/**Returns the result of a Steam user inventory API call for the given ID.
	@param steamID the ID to lookup.
	@throws NullPointerException if steamID is null.
	@throws IOException if an IO error occurs.
	@return the result of a Steam user inventory API call for the given ID.
	*/
	JSONObject getInventoryForUser(String steamID) throws IOException;
}