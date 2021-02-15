package trading.driver;

import org.json.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.net.*;
import java.lang.reflect.*;
import java.nio.charset.*;
import java.util.stream.*;
import trading.net.*;
import trading.economy.*;

//TODO: Update catches to output to log.

//Possible refactorings: include options on whether to base on upper, lower, or middle, consider a messaging feature

/*
User input:
1. Each function and its parameters.
2. Key/Scrap ratio determination method (hardcoded or automatic update).
3. Paths to read and write documentation files.                             <-- Need to be statically saved
4. Bot parameters: Bot username/password, id, secrets, etc
5. Trade offer parameters: Forgiveness, canHold, userID.                    <-- Need to be statically saved
*/

/*
Config file specs:
1. botInfo.json:
	botID: bot's steam ID
	botUsername: bot's steam username
	botPassword: bot's steam password
	sharedSecret: bot's shared secret
	identitySecret: bot's identity secret
	APIKey: bot's Backpack.tf API key
	APIToken: bot's Backpack.tf API token
	
2. botSettings.json:
	ownerIDs: (JSONArray of String) owner IDs to accept automatically from.
	canHold: whether the bot can hold trades.
	forgiveness: forgiveness value.
	keyScrapRatio: int indicating hardcoded value or "auto" to automatically update.
	readBotFile: location to read trading bot data from.
	writeBotFile: location to write trading bot data to.
	constructWithHats: whether to automatically include hats in bot inventory when automatically constructing bot.
	defaultRatio: default ratio for hats found in bot's inventory.
	acceptPath: path to write accepted log files to.
	declinePath: path to write declined log files to.
	holdPath: path to write held log files to.
	logFile: path for exception log file, if any.
	periodicSleep: time slept in between periodic actions
	priceUpdateSleep: time slept after calling Backpack.tf listings API.

3. functions.json:
	acceptabilityFunction
	buyListingPriceFunction
	hatPriceFunction
	listingDescriptionFunction

	Each of these point to a JSONObject. This object should have two keys: a "name" key, which points to
	 either the fully-qualified name of a custom function or the name of one of the provided functions,
	and an "arguments" key, which provides arguments for either the provided function or a constructor for the custom function class.
	"arguments" can be omitted if the function or constructor has no arguments.
*/

public class Main{
    //File locations of json files storing hats and listings.
	private static final String botPath;
	private static final String acceptedSavePath;
	private static final String declinedSavePath;
	private static final String heldSavePath;
	private static final String logFile;
	private static final String configPath = resolveConfigPath();

	//private static final Map<Class<?>, Map<String, DefaultMethodConstructor<?>>> defaultMethodLookup;

	private static final BackpackTF backpackTF;
	private static final Steam steam;

	//Command to start offerChecking.
	private static final String OFFER_CHECK_ARGUMENT = "node trading/offerChecking.js";

	private static final double forgiveness;
	private static final boolean canHold;
	private static final List<String> ownerIDs;
	private static final double defaultRatio;

	//Sleep time, in milliseconds, between performing periodic actions and looking at listings.
	private static final long periodicSleep;
	private static final long priceUpdateSleep;

	//Date format used when saving offer records.
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh;mm;ss aa");

	//The trading bot object.
	private static TradingBot elonMusk;

	//This program has three threads.
	//inputThread takes in user input and commands.
	//botThread performs periodic actions.
	//offerThread detects and responds to offers.
	private static Thread inputThread;
	private static Thread botThread;
	private static Thread offerThread;

	private static int successes = 0;

		static {
		JSONObject botInfo = loadConfigFile("botInfo.json");
		JSONObject botSettings = loadConfigFile("botSettings.json");
		JSONObject functions = loadConfigFile("function.json");

		String botID = botInfo.getString("botID");
		String apiKey = botInfo.getString("apiKey");
		String apiToken = botInfo.getString("apiToken");

		backpackTF = BackpackTF.open(apiKey, apiToken);
		steam = Steam.open();

		canHold = botSettings.getBoolean("canHold");
		forgiveness = botSettings.getDouble("forgiveness");
		List<Object> ids = botSettings.getJSONArray("ownerIDs").toList();
		ownerIDs = new ArrayList<>();
		for(Object o : ids){
			ownerIDs.add((String)o);
		}

		String botReadPath = botSettings.getString("botReadPath");
		botPath = botSettings.getString("botWritePath");
		acceptedSavePath = botSettings.getString("acceptPath");
		declinedSavePath = botSettings.getString("declinePath");
		heldSavePath = botSettings.getString("holdPath");
		logFile = botSettings.getString("logFile");
		periodicSleep = botSettings.getInt("periodicSleep");
		priceUpdateSleep = botSettings.getInt("priceUpdateSleep");
		defaultRatio = botSettings.getDouble("defaultRatio");
		boolean constructWithHats = botSettings.getBoolean("constructWithHats");
		boolean autoKeyScrap = botSettings.get("keyScrapRatio").equals("auto");

		AcceptabilityFunction acceptabilityFunction = getCustomFunction(AcceptabilityFunction.class, functions.getJSONObject("acceptabilityFunction"));
		BuyListingPriceFunction buyListingPriceFunction = getCustomFunction(BuyListingPriceFunction.class, functions.getJSONObject("buyListingPriceFunction"));
		HatPriceFunction hatPriceFunction = getCustomFunction(HatPriceFunction.class, functions.getJSONObject("hatPriceFunction"));
		ListingDescriptionFunction listingDescriptionFunction = getCustomFunction(ListingDescriptionFunction.class, functions.getJSONObject("listingDescriptionFunction"));
		KeyScrapRatioFunction keyScrapRatioFunction = autoKeyScrap ? KeyScrapRatioFunction.backpackTFRatio() : KeyScrapRatioFunction.customRatio(botSettings.getInt("keyScrapRatio"));
		FunctionSuite suite = new FunctionSuite(hatPriceFunction, buyListingPriceFunction, listingDescriptionFunction, acceptabilityFunction, keyScrapRatioFunction);

		try{
			elonMusk = TradingBot.fromJSONRepresentation(new JSONObject(readFile(botReadPath)), backpackTF, suite);
			System.out.println("Read bot data from " + botReadPath);
		} catch(FileNotFoundException e){
			try{
				if(constructWithHats){
					elonMusk = TradingBot.autoCreate(botID, backpackTF, steam, defaultRatio, suite);
					System.out.println("Initialized bot automatically with hats in inventory.");
				} else {
					elonMusk = TradingBot.botWithoutHats(botID, backpackTF, suite);
					System.out.println("Initialized bot automatically.");
				}
			} catch(IOException f){
				throw new UncheckedIOException(f);
			}
		} catch(IOException e){
			throw new UncheckedIOException(e);
		}
		System.out.println(autoKeyScrap ? "Calculated key-to-scrap ratio to be " + elonMusk.getKeyScrapRatio() : "Using custom key-to-scrap ratio of " + botSettings.getInt("keyScrapRatio"));
		System.out.println("Bot has " + elonMusk.getHats().size() + " hats and " + elonMusk.getBuyListings().size() + " buy listings.");
		backpackTF.resetUsed();
	}

	private static final Thread.UncaughtExceptionHandler handler = (th, ex) -> {
		System.err.println("Thread " + th.getName() + " terminated due to an uncaught exception.");
		ex.printStackTrace();
		exit();
	};

	private static final Consumer<BackpackTFConnection> callback = (BackpackTFConnection conn) -> {
		BackpackTF connection = (BackpackTF)conn;
		IOException ioe = connection.lastThrownIOException();
		if(ioe != null){
			log(ioe);
			connection.resetIOException();
			System.out.print("'");
		} else {
			System.out.print(".");
			successes++;
		}
		if(connection.hasBeenUsed()){
			connection.resetUsed();
			try{
				Thread.sleep(priceUpdateSleep);
			} catch(InterruptedException e){
				throw new RuntimeException("Look sleep was interrupted.", e);
			}
		}
	};

	//Possible user input options: exit, save, sendlistings, getid, readhats, updateprices, recalculateprices
	private static final Runnable userInput = () -> {
		Scanner keyboard = new Scanner(System.in);
		while(keyboard.hasNextLine()){
			String input = keyboard.nextLine().toLowerCase();
			if(input.equals("exit")){
				exit();
			} else if(input.equals("sendlistings")){
				sendListings();
			} else if(input.equals("save")){
				save();
			} else if(input.equals("getid")){
				checkHatIDs();
				save();
			} else if(input.startsWith("readhats")){
				if(input.equals("readhats")){
					try{
						elonMusk.readHatsFromInventory(steam, defaultRatio);
						System.out.println("Read hats from inventory.");
					} catch(IOException e){
						log(e);
						System.out.println("Failed to read hats from inventory.");
					}
				} else {
					String toParse = input.substring("readhats".length() + 1);
					try{
						elonMusk.readHatsFromInventory(steam, Double.parseDouble(toParse));
						System.out.println("Read hats from inventory.");
					} catch(NumberFormatException e){
						System.err.println("Error: could not parse " + toParse);
					} catch(IOException e){
						log(e);
						System.out.println("Failed to read hats from inventory.");
					}
				}
				save();
			} else if(input.equals("updateprices")){
				updatePrices();
				save();
			} else if(input.equals("recalculateprices")){
				recalculate();
				save();
			} else if(input.equals("keyscrapratio")){
				System.out.println("Current key-to-scrap ratio is " + elonMusk.getKeyScrapRatio());
			} else if(input.equals("numberhats")){
				System.out.println("Bot has " + elonMusk.getHats().size() + " hats and " + elonMusk.getBuyListings().size() + " buy listings.");
			}
		}
		throw new RuntimeException("System.in was closed.");
	};

	private static final Runnable periodic = () -> {
		while(true){
			System.out.println("Periodic function has started.");
			updatePrices();
			checkHatIDs();
			save();
			recalculate();
			save();
			sendListings();
			try{
				Thread.sleep(periodicSleep);
			} catch(InterruptedException e){
				throw new RuntimeException("Periodic sleep was interrupted.", e);
			}
		}
	};

	private static final Runnable nativeOfferChecking = () -> {
		Process offerManagement;
		try{
			offerManagement = Runtime.getRuntime().exec(OFFER_CHECK_ARGUMENT);
		} catch(IOException e){
			throw new UncheckedIOException(OFFER_CHECK_ARGUMENT + " execution failed.", e);
		}
		Scanner input = new Scanner(offerManagement.getInputStream());
		PrintStream output = new PrintStream(offerManagement.getOutputStream(), true);
		String next;
		while(input.hasNextLine()){
			next = input.nextLine();
			//System.out.println(offer);
			JSONObject parsedOffer;
			try {
				parsedOffer = new JSONObject(next);
			} catch(JSONException e){
				System.err.println("Native offer checking encountered an error:\n" + next);
				offerManagement.destroy();
				break;
			}
			System.out.println("\nWe received an offer.");
			TradeOffer offer = elonMusk.evaluateTrade(parsedOffer);
			System.out.println("Our value: " + offer.getOurValue() + ", Their value: " + offer.getTheirValue());
			TradeOfferResponse response = offer.getResponse();
			String passive = "";
			String savePath = "";
			switch(response){
				case ACCEPT -> {
					passive = "Accepted";
					savePath = acceptedSavePath;
				}
				case DECLINE -> {
					passive = "Declined";
					savePath = declinedSavePath;
				}
				case HOLD -> {
					passive = "Held";
					savePath = heldSavePath;
				}
			}
			output.println(response);
			String result = input.nextLine();
			//String result = "success";
			if(result.equals("success")){
				System.out.println("The offer was " + passive.toLowerCase());
			} else {
				System.out.println("There was an error, the offer may or may not have been responded to.");
				System.err.println(result);
			}
			elonMusk.updateItemsAfterOffer(offer);
			String data = offer.getData();
			Date time = new Date();
			try{
				write(data.toString(), savePath + File.separator + passive + " " + DATE_FORMAT.format(time) + ".txt");
			} catch(IOException e){
				System.out.println("The offer could not be documented.");
				e.printStackTrace();
			}
		}
		throw new IllegalStateException("Native offer checking has stopped working.");
	};

	private static void recalculate(){
		successes = 0;
		System.out.println("Recalculating prices for " + elonMusk.getHats().size() + " hats and " + elonMusk.getBuyListings().size() + " buy listings.");
		System.out.print("(. indicates success, ' indicates failure for an inidividual listing)");
		elonMusk.recalculatePrices(backpackTF, callback);
		int failures = (elonMusk.getHats().size() + elonMusk.getBuyListings().size() - successes);
		System.out.println("Finished recalculating prices. " + successes + " successes and " + failures + " failures.");
		if(failures > 0){
			System.out.println("Check " + logFile + " for details on failures.");
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T getCustomFunction(Class<T> cl, JSONObject input){
		String name = input.getString("name");
		JSONArray parameters = input.optJSONArray("parameters");
		if(parameters == null){
			parameters = new JSONArray();
		}
		Method[] methods = Stream.concat(Arrays.stream(cl.getDeclaredMethods()), Arrays.stream(FunctionSuiteTranslators.class.getDeclaredMethods())).toArray(Method[]::new);
		List<Object> tmp = parameters.toList();
		tmp.replaceAll((Object in) -> {return JSONObject.NULL.equals(in) ? null : in;});
		Object[] methodInputs = tmp.toArray();
		for(Method m : methods){
			//An assumption is made that no two default implementation methods will have the same name. This is OK as I have control over it.
			if(m.getName().equalsIgnoreCase(name)){
				if(!cl.isAssignableFrom(m.getReturnType())){
					continue;
				}
				AnnotatedType[] types = m.getAnnotatedParameterTypes();
				if(types.length != parameters.length()){
					throw new JSONException("Specified method for " + cl.toString() + " takes " + types.length + " parameters, but " + parameters.length() + " was provided.");
				}
				try{
					return (T)m.invoke(null, methodInputs);
				} catch(ReflectiveOperationException e){
					throw new RuntimeException(e);
				}
			}
		}
		Class<? extends T> customClass;
		try{
			Class<?> qm = Class.forName(name);
			if(!cl.isAssignableFrom(qm)){
				throw new JSONException(qm.toString() + " is not an implementing class of " + cl.toString());
			}
			customClass = (Class<? extends T>)qm;
		} catch(ClassNotFoundException e){
			throw new JSONException("No default implementation method for " + cl.toString() + " or class found with name " + name);
		}
		for(Constructor<?> cons : customClass.getConstructors()){
			Constructor<? extends T> constructor = (Constructor<? extends T>)cons;
			AnnotatedType[] types = constructor.getAnnotatedParameterTypes();
			if(types.length == parameters.length()){
				try{
					return constructor.newInstance(methodInputs);
				} catch(ReflectiveOperationException e){
					throw new RuntimeException(e);
				}
			}
		}
		throw new JSONException("No constructor found in " + customClass.toString() + " that takes " + parameters.length() + " arguments.");
	}

	private static void save() {
		JSONObject botObject = elonMusk.getJSONRepresentation();
		try{
			write(botObject.toString(), botPath);
			System.out.println("Saved bot's data.");
		} catch(IOException e){
			System.out.println("Failed to save bot data.");
			log(e);
		}
	}

	private static String readFile(String path) throws IOException {
  		return new String(Files.readAllBytes(Paths.get(path)));
	}

	private static void write(String content, String path) throws IOException {
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(path))){
	    	writer.write(content);	
		}
	}

	private static String resolveConfigPath(){
		List<String> pathsToInspect = List.of(".", "..", "./src", "../src");
		for(String s : pathsToInspect){
			String dir = s + File.separator + "config";
			if(Files.exists(Path.of(dir))){
				return dir;
			}
		}
		//This is the worst piece of code that I have ever written
		try{
			throw new IOException("Could not find config folder.");
		} catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	private static void sendListings(){
		try{
			elonMusk.sendListings(backpackTF);
			System.out.println("Sent listings to Backpack.tf.");
		} catch(IOException e){
			log(e);
			System.out.println("Failed to send listings.");
		}
	}

	private static void checkHatIDs(){
		try{
			elonMusk.checkHatIDs(steam);
			System.out.println("Verified hat IDs.");
		} catch(IOException e){
			log(e);
			System.out.println("Failed to verify hat IDs.");
		}
	}

	private static void updatePrices(){
		try{
			elonMusk.updateAndFilter(backpackTF);
			System.out.println("Updated prices.");
		} catch(IOException e){
			log(e);
			System.out.println("Failed to update prices.");
		}
	}

	private static void log(Throwable t){
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		log(sw.toString());
	}

	private static void log(String information){
		try {
		    Path path = Paths.get(logFile);
		    Files.write(path, Arrays.asList(information), StandardCharsets.UTF_8,
		        Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
		} catch (IOException ioe) {
		    System.err.println("Failed to log info: " + information);
		    System.err.println("Caused by:");
		    ioe.printStackTrace();
		}
	}

	private static JSONObject loadConfigFile(String filename){
		try{
			return new JSONObject(readFile(configPath + File.separator + filename));
		} catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	private static void exit(){
		save();
		System.exit(0);
	}

	public static void main(String[] args) throws IOException {
		inputThread = new Thread(userInput, "User input");
		inputThread.setUncaughtExceptionHandler(handler);
		botThread = new Thread(periodic, "Periodic function thread");
		botThread.setUncaughtExceptionHandler(handler);
		offerThread = new Thread(nativeOfferChecking, "Offer checking and processing");
		offerThread.setUncaughtExceptionHandler(handler);
		inputThread.start();
		offerThread.start();
		botThread.start();
	}
}