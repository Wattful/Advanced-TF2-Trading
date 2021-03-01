package trading.driver;

import org.json.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.time.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.text.*;
import java.net.*;
import java.lang.reflect.*;
import java.nio.charset.*;
import java.util.stream.*;
import trading.net.*;
import trading.economy.*;
import javax.imageio.IIOException;

import static trading.driver.FileUtils.*;

//TODO:

//Possible refactorings: include options on whether to base on upper, lower, or middle, messaging feature, 
//have bot not updateandfilter on startup, fix behavior with unpriced hats

/**The program's main class.
*/

public class Main{
	private static final String OFFER_CHECK_ARGUMENT_1 = "node";
	private static final String OFFER_CHECK_ARGUMENT_2 = "nodejs/offerChecking.js";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh;mm;ss aa");

	private static final JSONObject botInfo;
	private static final JSONObject botSettings;
	private static final JSONObject functions;

	private static final String botPath;
	private static final String acceptedSavePath;
	private static final String declinedSavePath;
	private static final String heldSavePath;
	private static final String logFile;
	private static final String fallbackPath;
	private static final String configPath = resolveConfigPath();

	private static final LoggingBackpackTFConnection backpackTF;
	private static final SteamConnection steam;

	private static final String botID;
	private static final String username;
	private static final String password;
	private static final String sharedSecret;
	private static final String identitySecret;

	private static final double forgiveness;
	private static final boolean canHold;
	private static final List<String> ownerIDs;
	private static final double defaultRatio;
	private static final int offerCheckSleep;

	private static final long periodicSleep;
	private static final long priceUpdateSleep;

	private static TradingBot elonMusk;

	private static Thread inputThread;
	private static Thread botThread;
	private static Thread offerThread;

	private static int successes = 0;
	private static boolean recalculateOnStartup = false;

	static {
		botInfo = loadConfigFile("botInfo.json");
		botSettings = loadConfigFile("botSettings.json");
		functions = loadConfigFile("functions.json");

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
		boolean disconnectBPTF = botSettings.getBoolean("dontSendListings");
		offerCheckSleep = botSettings.getInt("offerCheckSleep");
		fallbackPath = botSettings.get("fallback") == JSONObject.NULL ? null : botSettings.getString("fallback");
		if(forgiveness < 0 || forgiveness > 1){
			throw new IllegalArgumentException("Expected forgiveness value between 0 and 1, got " + forgiveness);
		}
		if(defaultRatio < 0 || defaultRatio > 1){
			throw new IllegalArgumentException("Expected defaultRatio value between 0 and 1, got " + defaultRatio);
		}
		if(periodicSleep < 0){
			throw new IllegalArgumentException("Expected non-negative periodicSleep value, got " + periodicSleep);
		}
		if(priceUpdateSleep < 0){
			throw new IllegalArgumentException("Expected non-negative priceUpdateSleep value, got " + priceUpdateSleep);
		}
		if(offerCheckSleep < 0){
			throw new IllegalArgumentException("Expected non-negative offerCheckSleep value, got " + offerCheckSleep);
		}

		botID = botInfo.getString("botID");
		String apiKey = botInfo.getString("APIKey");
		String apiToken = botInfo.getString("APIToken");
		username = botInfo.getString("botUsername");
		password = botInfo.getString("botPassword");
		sharedSecret = botInfo.getString("sharedSecret");
		identitySecret = botInfo.getString("identitySecret");

		backpackTF = disconnectBPTF ? NoListingsBackpackTF.open(apiKey, apiToken, fallbackPath) : BackpackTF.open(apiKey, apiToken, fallbackPath);
		steam = Steam.open();

		AcceptabilityFunction acceptabilityFunction = getCustomFunction(AcceptabilityFunction.class, functions.getJSONObject("acceptabilityFunction"));
		BuyListingPriceFunction buyListingPriceFunction = getCustomFunction(BuyListingPriceFunction.class, functions.getJSONObject("buyListingPriceFunction"));
		HatPriceFunction hatPriceFunction = getCustomFunction(HatPriceFunction.class, functions.getJSONObject("sellListingPriceFunction"));
		ListingDescriptionFunction listingDescriptionFunction = getCustomFunction(ListingDescriptionFunction.class, functions.getJSONObject("listingDescriptionFunction"));
		KeyScrapRatioFunction keyScrapRatioFunction = autoKeyScrap ? KeyScrapRatioFunction.backpackTFRatio() : KeyScrapRatioFunction.customRatio(botSettings.getInt("keyScrapRatio"));
		FunctionSuite suite = new FunctionSuite(hatPriceFunction, buyListingPriceFunction, listingDescriptionFunction, acceptabilityFunction, keyScrapRatioFunction);

		try{
			elonMusk = TradingBot.fromJSONRepresentation(new JSONObject(readFile(botReadPath)), backpackTF, suite);
			System.out.println("Read bot data from " + botReadPath);
		} catch(NoSuchFileException e){
			System.out.println("No bot file found at " + botReadPath + ". Initializing automatically.");
			try{
				if(constructWithHats){
					elonMusk = TradingBot.autoCreate(botID, backpackTF, steam, defaultRatio, suite);
					System.out.println("Initialized bot automatically with hats in inventory.");
				} else {
					elonMusk = TradingBot.botWithoutHats(botID, backpackTF, suite);
					System.out.println("Initialized bot automatically.");
				}
				recalculateOnStartup = true;
			} catch(IOException f){
				throw new UncheckedIOException(f);
			}
		} catch(IOException e){
			throw new UncheckedIOException(e);
		}
		checkForUpdatePricesError();
		System.out.println(autoKeyScrap ? "Calculated key-to-scrap ratio to be " + elonMusk.getKeyScrapRatio() : "Using custom key-to-scrap ratio of " + botSettings.getInt("keyScrapRatio"));
		System.out.println("Bot is selling " + elonMusk.getHats().size() + " items and buying " + elonMusk.getBuyListings().size() + " items.");
		backpackTF.resetUsed();
	}

	private static final Thread.UncaughtExceptionHandler handler = (th, ex) -> {
		System.err.println("Thread " + th.getName() + " terminated due to an uncaught exception.");
		ex.printStackTrace();
		exit();
	};

	private static final Consumer<BackpackTFConnection> callback = (BackpackTFConnection conn) -> {
		LoggingBackpackTFConnection connection = (LoggingBackpackTFConnection)conn;
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

	private static final Runnable userInput = () -> {
		Scanner keyboard = new Scanner(System.in);
		while(keyboard.hasNextLine()){
			String normalizedInput = keyboard.nextLine();
			String input = normalizedInput.toLowerCase();
			if(input.equals("exit")){
				exit();
			} else if(input.equals("sendlistings")){
				sendListings();
			} else if(input.equals("save")){
				save();
			} else if(input.equals("getid")){
				checkHatIDs();
				save();
			} else if(input.startsWith("readitems")){
				int before = elonMusk.getHats().size();
				if(input.equals("readitems")){
					try{
						elonMusk.readHatsFromInventory(steam, defaultRatio);
						int difference = elonMusk.getHats().size() - before;
						if(difference >= 0){
							System.out.println("Read " + difference + " items from inventory.");
						} else {
							System.out.println("Removed " + (-difference) + " items no longer in inventory.");
						}

					} catch(IOException e){
						log(e);
						System.out.println("Failed to read items from inventory. See " + logFile + " for more details.");
					}
				} else {
					String toParse = input.substring("readitems".length() + 1);
					try{
						elonMusk.readHatsFromInventory(steam, Double.parseDouble(toParse));
						int after = elonMusk.getHats().size();
						System.out.println("Read " + (after - before) + " items from inventory.");
					} catch(NumberFormatException e){
						System.err.println("Error: could not parse " + toParse);
					} catch(IOException e){
						log(e);
						System.out.println("Failed to read items from inventory. See " + logFile + " for more details.");
					}
				}
				save();
			} else if(input.equals("updateandfilter")){
				updatePrices();
				save();
			} else if(input.equals("recalculateprices")){
				new Thread(() -> {
					recalculate();
					save();
				}).start();
			} else if(input.equals("keyscrapratio")){
				System.out.println("Current key-to-scrap ratio is " + elonMusk.getKeyScrapRatio());
			} else if(input.equals("numberitems")){
				System.out.println("Bot is selling " + elonMusk.getHats().size() + " items and buying " + elonMusk.getBuyListings().size() + " items.");
			} else if(input.startsWith("iteminfo")){
				String toParse = normalizedInput.substring("iteminfo".length());
				Item query;
				try{
					query = parseItem(toParse);
				} catch(IllegalArgumentException | NoSuchElementException e){
					System.out.println("Error: " + e.getMessage());
					continue;
				}
				Hat h = elonMusk.getHats().get(query);
				BuyListing bl = elonMusk.getBuyListings().get(query);
				if(h == null && bl == null){
					System.out.println("Bot is neither buying nor selling " + query.getEffect().getName() + " " + query.getName());
				} 
				if(h != null){
					System.out.println("Bot is selling this hat. Info:\n" + h.getJSONRepresentation().toString());
				}
				if(bl != null){
					System.out.println("Bot is buying this hat. Info:\n" + bl.getJSONRepresentation().toString());
				}
			} else if(input.startsWith("itemprice")){
				String toParse = normalizedInput.substring("itemprice".length());
				Item query;
				try{
					query = parseItem(toParse);
				} catch(IllegalArgumentException | NoSuchElementException e){
					System.out.println("Error: " + e.getMessage());
					continue;
				}
				Hat h = elonMusk.getHats().get(query);
				BuyListing bl = elonMusk.getBuyListings().get(query);
				if(h == null && bl == null){
					System.out.println("Bot is neither buying nor selling " + query.getEffect().getName() + " " + query.getName());
				} 
				if(h != null){
					try{
						System.out.println("Bot is selling this hat for " + h.getPrice().valueString());
					} catch(NonVisibleListingException e) {
						System.out.println("Bot is selling this hat, but has not yet set a price.");
					}
				}
				if(bl != null){
					try{
						System.out.println("Bot is buying this hat for " + bl.getPrice().valueString());
					} catch(NonVisibleListingException e) {
						System.out.println("Bot is buying this hat, but has not yet set a price.");
					}
				}
			} else if(input.equals("sellprices")) {
				if(elonMusk.getHats().size() == 0){
					System.out.println("Bot is not selling any items.");
				}
				for(Hat h : elonMusk.getHats()){
					try{
						System.out.println(h.getEffect().getName() + " " + h.getName() + ": " + h.getPrice().valueString());
					} catch(NonVisibleListingException e){
						System.out.println(h.getEffect().getName() + " " + h.getName() + ": Price not set yet");
					}
				}
			} else if(input.equals("buyprices")) {
				if(elonMusk.getBuyListings().size() == 0){
					System.out.println("Bot is not buying any items.");
				}
				for(BuyListing bl : elonMusk.getBuyListings()){
					try{
						System.out.println(bl.getEffect().getName() + " " + bl.getName() + ": " + bl.getPrice().valueString());
					} catch(NonVisibleListingException e){
						System.out.println(bl.getEffect().getName() + " " + bl.getName() + ": Price not set yet");
					}
				}
			} else if(input.equals("botinfo")) {
				System.out.println(botInfo.toString());
			} else if(input.equals("botsettings")) {
				System.out.println(botSettings.toString());
			} else if(input.equals("functions")) {
				System.out.println(functions.toString());
			} else {
				System.out.println("Unrecognized command: " + normalizedInput);
			}
		}
		throw new IllegalStateException("System.in was closed.");
	};

	private static final Runnable periodic = () -> {
		while(true){
			if(recalculateOnStartup){
				recalculate();
				recalculateOnStartup = false;
			}
			checkHatIDs();
			sendListings();
			try{
				Thread.sleep(periodicSleep);
			} catch(InterruptedException e){
				throw new RuntimeException("Periodic sleep was interrupted.", e);
			}
			System.out.println("Periodic function has started.");
			updatePrices();
			save();
			recalculate();
			save();
		}
	};

	private static final Runnable nativeOfferChecking = () -> {
		Process offerManagement;
		try{
			offerManagement = Runtime.getRuntime().exec(new String[]{OFFER_CHECK_ARGUMENT_1, OFFER_CHECK_ARGUMENT_2, username, password, sharedSecret, identitySecret, ((Integer)offerCheckSleep).toString()});
		} catch(IOException e){
			throw new UncheckedIOException(OFFER_CHECK_ARGUMENT_2 + " execution failed.", e);
		}
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			offerManagement.destroy();
			System.out.println("Offer checking thread exited safely.");
		}));
		Scanner input = new Scanner(offerManagement.getInputStream());
		Scanner errInput = new Scanner(offerManagement.getErrorStream());
		PrintStream output = new PrintStream(offerManagement.getOutputStream(), true);
		String next;
		while(input.hasNextLine()){
			next = input.nextLine();
			//System.out.println(offer);
			JSONObject parsedOffer;
			try {
				parsedOffer = new JSONObject(next);
			} catch(JSONException e){
				String message = next;
				while(input.hasNextLine()){
					message += "\n" + input.nextLine();
				}
				//System.err.println("Native offer checking encountered an error:\n" + message);
				throw new RuntimeException("Native offer checking encountered an error:\n" + message);
			}
			System.out.println("\nWe received an offer.");
			TradeOffer offer = elonMusk.evaluateTrade(parsedOffer, forgiveness, canHold, ownerIDs);
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
				System.out.println("The offer was " + passive.toLowerCase() + ".");
			} else {
				System.out.println("There was an error, the offer may or may not have been responded to.");
				System.err.println(result);
			}
			elonMusk.updateItemsAfterOffer(offer, defaultRatio);
			String data = offer.getData();
			Date time = new Date();
			try{
				write(data.toString(), savePath + File.separator + passive + " " + DATE_FORMAT.format(time) + ".txt");
			} catch(IOException e){
				System.out.println("The offer could not be documented.");
				e.printStackTrace();
			}
			new Thread(() -> {
				try{
					Thread.sleep(60000);
				} catch(InterruptedException e){
					throw new RuntimeException("Sleep before checking item IDs was interrupted.");
				}
				checkHatIDs();
			}).start();
		}
		if(errInput.hasNextLine()){
			throw new IllegalStateException("Native offer checking encountered an error:\n" + errInput.useDelimiter("\\Z").next());
		}
		throw new IllegalStateException("Native offer checking stopped unexpectedly.");
	};

	private static void recalculate(){
		successes = 0;
		System.out.println("Recalculating prices for " + elonMusk.getHats().size() + " sell listings and " + elonMusk.getBuyListings().size() + " buy listings.");
		System.out.println("(. indicates success, ' indicates failure for an inidividual listing)");
		backpackTF.resetIOException();
		backpackTF.resetUsed();
		elonMusk.recalculatePrices(backpackTF, callback);
		int failures = (elonMusk.getHats().size() + elonMusk.getBuyListings().size() - successes);
		System.out.println("\nFinished recalculating prices. " + successes + " successes and " + failures + " failures.");
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
		Method[] methods = Stream.concat(Arrays.stream(FunctionSuiteTranslators.class.getDeclaredMethods()), Arrays.stream(cl.getDeclaredMethods())).toArray(Method[]::new);
		Object[] methodInputs = new Object[parameters.length()];
		for(int i = 0; i < parameters.length(); i++){
			methodInputs[i] = parameters.isNull(i) ? null : parameters.get(i); 
		}
		for(Method m : methods){
			//An assumption is made that no two default implementation methods will have the same name. This is OK as I have control over it.
			if(m.getName().equalsIgnoreCase(name)){
				if(!cl.isAssignableFrom(m.getReturnType())){
					continue;
				}
				AnnotatedType[] types = m.getAnnotatedParameterTypes();
				if(types.length == parameters.length() + 1){
					Object[] temp = methodInputs;
					methodInputs = new Object[methodInputs.length + 1];
					System.arraycopy(temp, 0, methodInputs, 0, temp.length);
					methodInputs[methodInputs.length - 1] = botID;
				} else if(types.length != parameters.length()){
					throw new JSONException("Specified method for " + cl.toString() + " takes " + types.length + " parameters, but " + parameters.length() + " were provided.");
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
			throw new JSONException("No default implementation method or class for " + cl.toString() + " found with name " + name);
		}
		for(Constructor<?> cons : customClass.getConstructors()){
			Constructor<? extends T> constructor = (Constructor<? extends T>)cons;
			AnnotatedType[] types = constructor.getAnnotatedParameterTypes();
			if(types.length == methodInputs.length){
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
			log(e);
			System.out.println("Failed to save bot data. See " + logFile + " for more details.");
		}
	}

	private static String resolveConfigPath(){
		List<String> pathsToInspect = List.of(".", "..", "./src", "../src", "../config", "./config");
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
			System.out.println(backpackTF instanceof BackpackTF ? "Sent listings to Backpack.tf." : "Bot is in no-send mode; listings were not sent to Backpack.tf.");
		} catch(IOException e){
			log(e);
			System.out.println("Failed to send listings. See " + logFile + " for more details.");
		}
	}

	private static void checkHatIDs(){
		try{
			elonMusk.checkHatIDs(steam);
			System.out.println("Verified hat IDs.");
		} catch(IOException e){
			log(e);
			System.out.println("Failed to verify hat IDs. See " + logFile + " for more details.");
		}
	}

	private static void updatePrices(){
		try{
			elonMusk.updateAndFilter(backpackTF);
			checkForUpdatePricesError();
			System.out.println("Updated community prices and filtered buy listings. Bot is now buying " + elonMusk.getBuyListings().size() + " items.");
		} catch(IOException e){
			log(e);
			System.out.println("Failed to update prices. See " + logFile + " for more details.");
		}
	}

	private static void checkForUpdatePricesError(){
		if(backpackTF.lastThrownIOException() != null){
			IOException ioe = backpackTF.lastThrownIOException();
			backpackTF.resetIOException();
			if(ioe instanceof IIOException){
				System.out.println("Failed to save fallback prices. See " + logFile + " for more details.");
				log(ioe.getCause());
			} else {
				System.out.println("Failed to get community prices from Backpack.tf. Using fallback located at " + fallbackPath  + 
					". See " + logFile + " for more details.");
				log(ioe);
			}
		}
	}

	private static Item parseItem(String toParse){
		Matcher m = Pattern.compile("\\s*\"(.*?)\"\\s*\"(.*?)\"\\s*").matcher(toParse);
		if(!m.matches()){
			throw new IllegalArgumentException("This command requires two quoted arguments.");
		}
		String rawEffect = m.group(1);
		String name = m.group(2);
		Effect effect;
		try{
			effect = Effect.forInt(Integer.parseInt(rawEffect));
		} catch(NumberFormatException e){
			effect = Effect.forName(rawEffect);
		}
		return new Item(name, Quality.UNUSUAL, effect);
	}

	private static void log(Throwable t){
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		log(LocalDateTime.now().toString() + ": " + sw.toString());
	}

	private static void log(String information){
		try {
			ensurePathExists(logFile);
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
		checkNodeJS();
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

	private static void checkNodeJS(){
		try{
			Runtime.getRuntime().exec("node");
		} catch(IOException e){
			throw new IllegalStateException("Node.js is not installed or has not been added to the PATH.", e);
		}
	}
}