package trading;

import org.json.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.net.*;
import java.util.Date;
import java.util.LinkedList;
import java.text.SimpleDateFormat;
import java.net.SocketTimeoutException;

public class Main{
	//JSON objects representing reference.json and inventory.json
	public static final JSONObject REFERENCE;
	public static JSONObject A;

	//The number of scrap per key. Must be updated manually.
	public static final int KEY_SCRAP_RATIO = 444;

	//Variables relating to how a Hat's price is calculated.
	//SELL_RATIO is the default sell value, when a hat's age is 0.
	//SPEED relates to how fast a hat's price drops (higher is faster)
	//MIN_PROFIT is the minimum percent profit the bot bases its prices on
	//See Hat.java for more information.
	public static final double SELL_RATIO = .9;
	public static final double SPEED = 1;
	public static final double MIN_PROFIT = .035;

	//Bot's "sayings" which will be part of the listing descriptions on backpack.tf.
	public static final String[] SAYINGS = {
		"\"And you told me you were gonna wear something nice!\"",
        "\"Folks need heroes, Chief.\"",
        "\"Oh, I know what the ladies like...\"",
        "\"Dear humanity - we regret being alien bastards.\"",
        "\"Hey, bastard! Knock, Knock!\"",
        "\"For a brick, he flew pretty good.\"",
        "\"Well, I just happen to have a key!\"",
        "\"Please... don't shake the lightbulb.\"",
        "\"Usually, the good lord works in mysterious ways. But not today!\"",
        "\"Then they must love the smell of badass.\"",
        "\"This is it, baby. hold me.\"",
        "\"Send me out... with a bang.\""
    };

    //File locations of json files storing hats and listings.
	private static final String HAT_PATH = "../json/allHats.json";
	private static final String LISTING_PATH = "../json/allListings.json";
	private static final String PRICES_OBJECT_PATH = "../json/prices.json";
	private static final String REFERENCE_PATH = "../json/reference.json";
	private static final String OFFER_SAVE_PATH = "../offers";

	//Command to start offerChecking.
	private static final String OFFER_CHECK_ARGUMENT = "node trading/offerChecking.js";

	private static final double OFFER_ERROR = .998;

	//Sleep time, in milliseconds, between performing periodic actions and looking at listings.
	private static final long PERIODIC_SLEEP = 21600000;
	private static final long LOOK_SLEEP = 30000;

	//OFFER_TEST_MODE, if true, disables the owner automatic accept feature, to allow for testing of offer logic.
	private static final boolean OFFER_TEST_MODE = true;

	//Date format used when saving offer records.
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh;mm;ss aa");

	//Counter determines when to start the periodic thread (see below)
	private static int counter = 1;

	//The trading bot object.
	private volatile static TradingBot elonMusk;

	//This program has three threads.
	//inputThread takes in user input and commands.
	//botThread performs periodic actions.
	//offerThread detects and responds to offers.
	private static Thread inputThread;
	private static Thread botThread;
	private static Thread offerThread;

	private static Thread.UncaughtExceptionHandler handler = (th, ex) -> {
		System.out.println("Thread " + th.getName() + " terminated due to an uncaught exception.");
		ex.printStackTrace();
		exit();
	};

	private static Runnable userInput = () -> {
		Scanner keyboard = new Scanner(System.in);
		while(keyboard.hasNextLine()){
			String input = keyboard.nextLine();
			synchronized(elonMusk){
				if(input.toLowerCase().equals("exit")){
					exit();
				}
				else if(input.toLowerCase().equals("autobuyandsell")){
					elonMusk.autoBuyAndSell();
					System.out.println("Auto bought and sold.");
				} else if(input.toLowerCase().equals("save")){
					try{
						elonMusk.save();
						System.out.println("Saved.");
					} catch(IOException e){
						System.out.println("Failed to save.");
					}
				} else if(input.toLowerCase().equals("getid")){
					elonMusk.getID();
					System.out.println("Got ID.");
				}
			}
		}
		throw new AssertionError("System.in was closed.");
	};

	private static Runnable periodic = () -> {
		while(true){
			if(counter % 4 == 0){
				synchronized(elonMusk){
					System.out.println("Hats have been grown.");
					elonMusk.growHats();
					elonMusk.autoBuyAndSell();
					try {
						elonMusk.save();
					} catch(IOException e){
						System.out.println("Failed to save.");
					}
				}
			}
			if(counter % 4 == 1){
				synchronized(elonMusk){
					System.out.println("The inventory object has been updated.");
					getPrices();
					elonMusk.updatePrices();
				}
			}
			if(counter % 4 == 2){
				synchronized(elonMusk){	
					System.out.println("Listings have been updated.");
					elonMusk.updateListings();
					elonMusk.autoBuyAndSell();
					try {
						elonMusk.save();
					} catch(IOException e){
						System.out.println("Failed to save.");
					}
				}
			}
			if(counter % 4 == 3){	
				System.out.println("Look has started.");
				//This is ugly and should be encapsulated 😩😩😩
				for(BuyListing b : elonMusk.myListings){
					synchronized(elonMusk){
						b.look();
						elonMusk.autoBuyAndSell();
					}
					try {
						Thread.sleep(LOOK_SLEEP);
					} catch(InterruptedException e){
						throw new AssertionError("Look sleep was interrupted.", e);
					}
				}
				System.out.println("Look has finished.");
				try {
					elonMusk.save();
				} catch(IOException e){
					System.out.println("Failed to save.");
				}
			}
			counter++;
			try{
				Thread.sleep(PERIODIC_SLEEP);
			} catch(InterruptedException e){
				throw new AssertionError("Periodic sleep was interrupted.", e);
			}
		}
	};

	private static Runnable nativeOfferChecking = () -> {
		Process offerManagement;
		try{
			offerManagement = Runtime.getRuntime().exec(OFFER_CHECK_ARGUMENT);
		} catch(IOException e){
			throw new UncheckedIOException(OFFER_CHECK_ARGUMENT + " execution failed.", e);
		}
		Scanner input = new Scanner(offerManagement.getInputStream());
		PrintStream output = new PrintStream(offerManagement.getOutputStream(), true);
		String offer;
		while(input.hasNextLine()){
			synchronized(elonMusk){
				offer = input.nextLine();
				//System.out.println(offer);
				JSONObject parsedOffer;
				try {
					parsedOffer = new JSONObject(offer);
				} catch(JSONException e){
					System.out.println("Native offer checking stopped due to an error of type " + offer);
					offerManagement.destroy();
					break;
				}
				System.out.println("\nWe received an offer.");
				StringBuilder data = new StringBuilder();
				TradeOfferResponse response = elonMusk.evaluateOffer(parsedOffer, data);
				output.println(response);
				String result = input.nextLine();
				//String result = "success";
				if(result.equals("success")){
					String passive = "";
					if(response == TradeOfferResponse.ACCEPT){
						System.out.println("The offer was accepted.");
						elonMusk.updateHats(parsedOffer);
						passive = "Accepted";
					} else if(response == TradeOfferResponse.HOLD){
						System.out.println("The offer was put on hold.");
						passive = "Declined";
					} else if(response == TradeOfferResponse.DECLINE){
						System.out.println("The offer was declined.");
						passive = "Declined";
					}
					Date time = new Date();
					try{
						write(data.toString(), OFFER_SAVE_PATH + File.separator + passive + " " + DATE_FORMAT.format(time) + ".txt");
					} catch(IOException e){
						e.printStackTrace();
						System.out.println("The offer could not be documented.");
					}
				} else {
					System.out.println("There was an error, the offer may or may not have been responded to.");
				}
			}
		}
		throw new IllegalStateException("Native offer checking has stopped working.");
	};

	static {
		try{
			elonMusk = new TradingBot();
		} catch(IOException e){
			throw new UncheckedIOException(e);
		}
		readPrices();
    	try{
			REFERENCE = new JSONObject(Main.readFile(REFERENCE_PATH));
    	} catch (IOException e){
    		throw new AssertionError("Reference not parsed correctly", e);
    	}
		inputThread = new Thread(userInput, "User input");
		inputThread.setDefaultUncaughtExceptionHandler(handler);
		botThread = new Thread(periodic, "Periodic beginning on " + counter);
		botThread.setDefaultUncaughtExceptionHandler(handler);
		offerThread = new Thread(nativeOfferChecking, "Offer checking and processing");
		offerThread.setDefaultUncaughtExceptionHandler(handler);
	}

	private enum TradeOfferResponse{
		ACCEPT, DECLINE, HOLD;
	}

	private static class TradingBot{
		private volatile ListingHashSet<Hat> myHats;
		private volatile ListingHashSet<BuyListing> myListings;

		public TradingBot() throws IOException {
			myHats = new ListingHashSet<Hat>(new JSONArray(readFile(HAT_PATH)), Hat.class);
			myListings = new ListingHashSet<BuyListing>(new JSONArray(readFile(LISTING_PATH)), BuyListing.class);
		}

		private void autoBuyAndSell(){
			JSONArray toSend = myHats.getListingRepresentation();
			JSONArray temp = myListings.getListingRepresentation();
			for(Object o : temp){
				JSONObject j = (JSONObject)o;
				if(j.getJSONObject("currencies").getInt("keys") == 0){
					continue;
				}
				toSend.put(j);
			}
			JSONObject args = new JSONObject();
			args.put("token", Configuration.API_TOKEN);
			args.put("listings", toSend);
			JSONObject response = request("https://backpack.tf/api/classifieds/list/v1", "POST", args);
			try{
				if(response != null){
					write(response.toString(), "./report.json");
				}
			} catch(IOException e){
				System.out.println("Could not document response.");
			}
			JSONObject secondArgs = new JSONObject();
			secondArgs.put("token", Configuration.API_TOKEN);
			//request("https://backpack.tf/api/aux/heartbeat/v1", "POST", secondArgs);
		}

		private TradeOfferResponse evaluateOffer(JSONObject offer, StringBuilder data){
			if(offer.getString("partner").equals(Configuration.OWNER_ID) && !OFFER_TEST_MODE){
				System.out.println("The offer is from one of the owners.");
				data.append("The offer was accepted because " + offer.getString("partner") + " is an owner");
				return TradeOfferResponse.ACCEPT;
			}
			JSONArray ourItems = offer.getJSONArray("itemsToGive");
			JSONArray theirItems = offer.getJSONArray("itemsToReceive");
			int ourValue = 0;
			int theirValue = 0;
			boolean isItem = false;

			data.append("Our items include:");
			for(Object o : ourItems){
				data.append("\n");
				JSONObject j = (JSONObject)o;
				SimpleItem item = new SimpleItem(j);
				int value = evaluateItem(item, myHats);

				if(value != 0){
					data.append(item.toString() + ", valued at " + value);
					ourValue += value;
				} else {
					data.append(item.toString() + ", not in our pricelist");
					ourValue = Integer.MAX_VALUE/2;
				}
			}

			data.append("\n\nTheir items include:");
			for(Object o : theirItems){
				data.append("\n");
				JSONObject j = (JSONObject)o;
				SimpleItem item = new SimpleItem(j);
				int value = evaluateItem(item, myListings);

				if(value != 0){
					data.append(item.toString() + ", valued at " + value);
					theirValue += value;
				} else {
					data.append(item.toString() + ", not in our pricelist");
					isItem = true;
				}
			}

			String totals = "Our value: " + ourValue + " Their value: " + theirValue;
			data.append("\n\n" + totals);
			System.out.println(totals);
			String partner = offer.getString("partner");

			if(ourValue * OFFER_ERROR <= theirValue){
				data.append("\n\nThe offer with " + partner + " was accepted because our value was less than or equal to their value.");
				//System.out.println("The offer was accepted.");
				return TradeOfferResponse.ACCEPT;
			} else if(isItem){
				data.append("\n\nThe offer with " + partner + " was accepted after manual review.");
				//System.out.println("The offer was put on hold.");
				return TradeOfferResponse.HOLD;
			} else {
				data.append("\n\nThe offer with " + partner + " was declined because our value was greater than their value.");
				//System.out.println("The offer was declined.");
				return TradeOfferResponse.DECLINE;
			}
		}

		private int evaluateItem(SimpleItem item, ListingHashSet<? extends Listing> set){
			int value = 0;
			try{
				value = set.get(item.getKey()).getPrice().getScrapValue();
			} catch(NullPointerException e){
				String n = item.getName();
				if(n.equals("Mann Co. Supply Crate Key")){value = KEY_SCRAP_RATIO;}
				else if(n.equals("Refined Metal")){value = 9;}
				else if(n.equals("Reclaimed Metal")){value = 3;}
				else if(n.equals("Scrap Metal")){value = 1;}
			}
			return value;
		}

		private void updateHats(JSONObject offer){
			JSONArray ourItems = offer.getJSONArray("itemsToGive");
			JSONArray theirItems = offer.getJSONArray("itemsToReceive");

			for(Object o : theirItems){
				SimpleItem item = new SimpleItem((JSONObject)o);
				if(item.getQuality().equals("Unusual")){
					BuyListing b = myListings.get(item.getKey());
					myHats.add(new Hat(b));
					myListings.remove(item.getKey());
				}
			}

			for(Object o : ourItems){
				SimpleItem item = new SimpleItem((JSONObject)o);
				if(item.getQuality().equals("Unusual")){
					myHats.remove(item.getKey());
				}
			}
			getID();
		}

		private void updateListings(){
			forEachUnusual((j, n, e) -> {
				if(BuyListing.isAcceptable(j, n, e)){
					myListings.add(new BuyListing(j, n, e));
				}
			});

			LinkedList<BuyListing> toRemove = new LinkedList<BuyListing>();
			for(BuyListing b : myListings){
				JSONObject j;
				try{
					j = getHatObject(b);
				} catch(JSONException e){
					toRemove.add(b);
					continue;
				}
				if(myHats.get(b) != null){
					toRemove.add(b);
					continue;
				}
				if(!BuyListing.isAcceptable(j, b.getName(), b.getEffect())){
					toRemove.add(b);
				}
			}
			for(BuyListing b : toRemove){
				myListings.remove(b);
			}
		}

		private void updatePrices(){
			for(BuyListing b : myListings){
				JSONObject j = getHatObject(b);
				b.setCommunityPrice(average(j));
			}
			for(Hat h : myHats){
				JSONObject j = getHatObject(h);
				h.setCommunityPrice(average(j));
			}
		}

		private void growHats(){
			boolean hasID = true;
			for(Hat h : myHats){
				h.grow();
				if(!h.hasID()){
					hasID = false;
				}
			}
			if(!hasID){
				getID();
			}
		}

		private void getID(){
			JSONObject inventory = request("https://steamcommunity.com/id/halobot/inventory/json/440/2/", "GET", new JSONObject());
			JSONObject items = inventory.getJSONObject("rgInventory");
			for(String s : JSONObject.getNames(items)){
				SimpleItem item = new SimpleItem(items.getJSONObject(s), inventory.getJSONObject("rgDescriptions"));
				if(!item.getQuality().equals("Unusual")){
					continue;
				}
				for(Hat h : myHats){
					if(item.represents(h)){
						h.setID(item.getID());
					}
				}
			}
		}

		private void save() throws IOException {
			write(myHats.toString(), HAT_PATH);
			write(myListings.toString(), LISTING_PATH);
		}
	}

	/*public static void main(String[] args) {
		elonMusk.getID();
		elonMusk.autoBuyAndSell();
		exit();
	}*/

	public static void main(String[] args) {
		inputThread.start();
		offerThread.start();
		botThread.start();
	}

	public static String readFile(String path) throws IOException {
  		return new String(Files.readAllBytes(Paths.get(path)));
	}

	public static void write(String content, String path) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(path));
    	writer.write(content);
    	writer.close();
	}

	public static JSONObject request(String uri, String method, JSONObject args){
		HttpURLConnection connection;
		boolean get = method.equals("GET");
		if(get){
			uri += JSONToURL(args);
		}
		try{
			URL url = new URL(uri);
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod(method);
			connection.setDoOutput(true);
		} catch(MalformedURLException | ProtocolException e){
			throw new AssertionError(e);
		} catch(IOException e){
			return null;
		}
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		if(!get){
			PrintStream wr;
			try{
				wr = new PrintStream(connection.getOutputStream(), true, "UTF-8");
			} catch(IOException e){
				System.out.println("bruh");
				e.printStackTrace();
				return null;
			}
			wr.print(args.toString());
			wr.flush();
		}
		JSONObject data;
		try{
			Scanner input = new Scanner(connection.getInputStream());
			String result = input.nextLine();
			//System.out.println(result);
			data = new JSONObject(result);
			input.close();
		} catch (SocketTimeoutException e){
			System.out.println("Timed out.");
			return null;
		} catch (IOException | JSONException e){
			e.printStackTrace();
			return null;
		}
		//System.out.println(data);
		return data;
	}

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

	public static interface PricesObjectFunction{
    	void execute(JSONObject j, String name, int effect);
    }

	public static void getPrices(){
		JSONObject args = new JSONObject();
		args.put("key", Configuration.API_KEY);
		A = Main.request("https://backpack.tf/api/IGetPrices/v4", "GET", args);
		try{
			Main.write(A.toString(), PRICES_OBJECT_PATH);
		} catch(IOException e){
			System.out.println("Inventory file failed to save.");
		}
	}

	private static void readPrices(){
		try{
			A = new JSONObject(Main.readFile(PRICES_OBJECT_PATH));
		} catch(JSONException | IOException e){
			getPrices();
		}
	}

	public static double average(JSONObject j){
		int low = j.getInt("value");
		int high = j.has("value_high") ? j.getInt("value_high") : low;
		return (high + low)/2.0;
	}

	public static JSONObject getHatObject(Listing l){
		return A.getJSONObject("response").getJSONObject("items").getJSONObject(l.getName()).getJSONObject("prices").getJSONObject("5").getJSONObject("Tradable").getJSONObject("Craftable").getJSONObject(Integer.toString(l.getEffect()));
	}

	public static void forEachUnusual(PricesObjectFunction jof){
		for(String s : JSONObject.getNames(A.getJSONObject("response").getJSONObject("items"))){
			if(s.equals("Haunted Metal Scrap") || s.equals("Horseless Headless Horsemann's Headtaker")){
				continue;
			}
			if(A.getJSONObject("response").getJSONObject("items").getJSONObject(s).getJSONObject("prices").has("5")){
				for(String t : JSONObject.getNames(A.getJSONObject("response").getJSONObject("items").getJSONObject(s).getJSONObject("prices").getJSONObject("5").getJSONObject("Tradable").getJSONObject("Craftable"))){
					jof.execute(A.getJSONObject("response").getJSONObject("items").getJSONObject(s).getJSONObject("prices").getJSONObject("5").getJSONObject("Tradable").getJSONObject("Craftable").getJSONObject(t), s, Integer.parseInt(t));
				}
			}
		}
	}

	private static void exit(){
		try{
			elonMusk.save();
			System.out.println("Exited correctly.");
			System.exit(0);
		} catch (IOException e){
			System.out.println("Failed to exit correctly.");
			throw new UncheckedIOException(e);
		}
	}
}