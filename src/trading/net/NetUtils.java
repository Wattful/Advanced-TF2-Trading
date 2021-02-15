package trading.net;

import org.json.*;
import java.io.*;
import java.net.*;
import java.util.*;

//TODO:

/**Static class containing utility methods for use in connection requests.
*/

public class NetUtils{
	/**Performs a HTTP request for a JSONObject and returns the result.
	@param uri The uri to request
	@param method The HTTP request method (ie "get", "post"), etc
	@param args The JSON arguments to include.
	@throws IOException if an IO error occurs.
	@throws NullPointerException if any parameter is null.
	@return the JSON result of the request.
	*/
	public static JSONObject request(String uri, String method, JSONObject args) throws IOException {
		if(uri == null || method == null || args == null){
			throw new NullPointerException();
		}
		HttpURLConnection connection;
		boolean get = method.equals("GET");
		if(get){
			uri += JSONToURL(args);
		}
		URL url = new URL(uri);
		connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod(method);
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		if(!get){
			PrintStream wr = new PrintStream(connection.getOutputStream(), true, "UTF-8");
			wr.print(args.toString());
			wr.flush();
			wr.close();
		}
		JSONObject data;
		Scanner input = new Scanner(connection.getInputStream());
		input.useDelimiter("\\Z");
		String result = input.next();
		data = new JSONObject(result);
		input.close();
		return data;
	}

	//Converts a JSONObject to a String usable in a URL.
	private static String JSONToURL(JSONObject args){
		String answer = "?";
		String[] keys = JSONObject.getNames(args);
		if(keys == null){
			return "";
		}
		for(String s : keys){
			answer += s + "=" + args.get(s).toString().replaceAll(" ", "%20").replaceAll("'", "%27") + "&";
		}
		return answer.substring(0, answer.length() - 1);
	}
}