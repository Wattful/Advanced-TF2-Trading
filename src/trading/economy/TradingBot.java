package trading.economy;

import trading.net.*;
import org.json.*;
import java.io.*;
import java.util.*;
import java.time.*;
import java.util.function.*;

//TODO:

/**Class representing a trading bot. This class keeps track of all Hats that are in the bot's inventory, as well as all BuyListings that the bot wants to buy, 
and a Backpack.tf prices object.
*/

public class TradingBot{
	private final String myID;
	private final FunctionSuite functions;
	private volatile ListingCollection<Hat> myHats;
	private volatile ListingCollection<BuyListing> myListings;
	private volatile JSONObject pricesObject;
	private volatile int keyScrapRatio;

	private TradingBot(String botID, JSONObject pricesObject, FunctionSuite functions, ListingCollection<Hat> hats, ListingCollection<BuyListing> listings){
		if(botID == null || functions == null || pricesObject == null){
			throw new NullPointerException();
		}
		if(keyScrapRatio <= 0){
			throw new IllegalArgumentException("keyScrapRatio was non-positive.");
		}
		this.myID = botID;
		this.pricesObject = pricesObject;
		this.functions = functions;
		this.keyScrapRatio = keyScrapRatio;
		this.myHats = hats == null ? new ListingHashSet<Hat>() : hats.copy();
		this.myListings = hats == null ? new ListingHashSet<BuyListing>() : listings.copy();
		this.updateKeyScrapRatio();
	}

	/**Returns this TradingBot's Hats.
	@return this TradingBot's Hats.
	*/
	public synchronized ListingCollection<Hat> getHats(){
		return this.myHats.copy();
	}

	/**Returns this TradingBot's BuyListings.
	@return this TradingBot's BuyListings.
	*/
	public synchronized ListingCollection<BuyListing> getBuyListings(){
		return this.myListings.copy();
	}

	/**Returns the key-to-scrap ratio that this TradingBot is currently using.
	@return the key-to-scrap ratio that this TradingBot is currently using.
	*/
	public synchronized int getKeyScrapRatio(){
		return this.keyScrapRatio;
	}

	/**Uses the given BackpackTFConnection to send all of this bot's listings to Backpack.tf.
	@param connection the connection to Backpack.tf
	@throws NullPointerException if connection is null.
	*/
	public synchronized void sendListings(BackpackTFConnection connection) throws IOException {
		ListingHashSet<Listing> toSend = new ListingHashSet<>(this.myHats.copy());
		toSend.addAll(this.myListings.copy());
		connection.sendListings(toSend, this.functions.listingDescriptionFunction);
	}

	/**Calls recalculatePrice() on all of this bot's Hats and BuyListings using the given BackpackTFConnection.
	@param connection the connection to Backpack.tf
	@throws NullPointerException if connection is null.
	*/
	public synchronized void recalculatePrices(BackpackTFConnection connection){
		this.recalculatePrices(connection, null);
	}

	/**Calls recalculatePrice() on all of this bot's Hats and BuyListings using the given BackpackTFConnection.<br>
	In between each call of recalculatePrices(), calls the given callback function, passing in the BackpackTFConnection.<br>
	This could be used to, for example, sleep between Backpack.tf API calls. 
	@param connection the connection to Backpack.tf
	@param callback callback function to call inbetween calls to recalculatePrice(). Ignored if null.
	@throws NullPointerException if connection is null.
	*/
	public synchronized void recalculatePrices(BackpackTFConnection connection, Consumer<? super BackpackTFConnection> callback){
		recalculatePriceInternal(this.myHats, connection, this.keyScrapRatio, this.functions.hatPriceFunction, callback);
		recalculatePriceInternal(this.myListings, connection, this.keyScrapRatio, this.functions.buyListingPriceFunction, callback);
	}

	/**Constructs, evaluates, and returns a TradeOffer from the given data.<br>
	This function is equivalent to calling the more complicated evaluateTrade function with a forgiveness of 0, canHold = true, and ownerIDs = null.
	@param offer the JSONObject represenation of the offer.
	@throws JSONException if offer is malformed.
	@throws NullPointerException if any parameter is null.
	@return the constructed and evaluated TradeOffer.
	*/
	public synchronized TradeOffer evaluateTrade(JSONObject offer){
		return this.evaluateTrade(offer, 0.0, true, null);
	}

	/**Constructs, evaluates, and returns a TradeOffer from the given data, using this bot's prices.<br>
	In order to evaluate the trade offer, this method will do the following:<br>
	First, it will go through each item in the trade, and sum each side's total value, taking into consideration this bot's prices.<br>
	More precisely, for each item in the trade it will do the following:
	<ol>
		<li>If the item is a currency (ie keys or metal), the item will be evaluated at the value of that currency.</li>
		<li>Otherwise, the method will check this bot's prices for the item.</li>
		<li>If the item is not a currency and it does not appear in the prices objects, then the item's price cannot be determined. The offer now becomes an item offer (see below). 
		If the item is in items to receive, then the item is valued at 0. If the item is in items to give, the item is valued effectively infinitely. This is to prevent the bot from accidently trading away items.</li>
	</ol>
	Once this is finished, the result is determined as follows:
	<ul>
		<li>If the offerer's ID is in ownerIDs, evaluated as TradeOfferResponse.ACCEPT.</li>
		<li>Otherwise, if (theirValue * (1 - forgiveness)) <= ourValue, evaluated as TradeOfferResponse.ACCEPT.</li>
		<li>Otherwise, if the offer is an item offer, and canHold is true, evaluated as TradeOfferResponse.HOLD.</li>
		<li>Otherwise, evaluated as TradeOfferResponse.DECLINE.</li>
	</ul>
	@param offer the JSONObject represenation of the offer.
	@param forgiveness a value to scale down the other person's price by. This is to prevent the declining of trades due to an insignificant price difference. Must be between 0 and 1.
	@param canHold whether the TradeOffer can be evaluated as TradeOfferResponse.HOLD.
	@param ownerIDs a list of Steam IDs to always accept offers from. This can be used to, for example, always accept offers from the bot's owner. null indicates no IDs to automatically accept.
	@throws JSONException if offer is malformed.
	@throws NullPointerException if offer is null.
	@throws IllegalArgumentException if preconditions on forgiveness are violated.
	@return the constructed and evaluated TradeOffer.
	*/
	public synchronized TradeOffer evaluateTrade(JSONObject offer, double forgiveness, boolean canHold, List<String> ownerIDs){
		return TradeOffer.fromJSON(offer, this.myHats.copy(), this.myListings.copy(), this.keyScrapRatio, forgiveness, canHold, ownerIDs);
	}

	/**Resolves the consequences of a trade offer.<br>
	If the given offer was accepted, the bot adds any hats that were received in the offer, and removes them from its BuyListings.<br>
	Additionally, any hats which were given away in the offer are removed from the bot.
	@param offer The trade offer to consider.
	@throws IllegalArgumentException if itemsToReceive contains an unusual hat which this bot does not have a BuyListing for, or its BuyListing is non-visible.
	@throws NullPointerException if offer is null.
	*/
	public synchronized void updateItemsAfterOffer(TradeOffer offer){
		if(offer.getResponse() != TradeOfferResponse.ACCEPT){
			return;
		}

		for(InventoryItem item : offer.itemsToReceive()){
			if(item.getQuality().equals(Quality.UNUSUAL)){
				BuyListing b = myListings.get(item);
				if(b == null){
					throw new IllegalArgumentException("Bot had no BuyListing for " + item.toString());
				}
				try{
					myHats.add(Hat.fromListing(b));
				} catch(NonVisibleListingException e){
					throw new IllegalArgumentException("Bot BuyListing for " + item.toString() + " was non-visible", e);
				}
				
				myListings.remove(item);
			}
		}

		for(InventoryItem item : offer.itemsToGive()){
			if(item.getQuality().equals(Quality.UNUSUAL)){
				myHats.remove(item);
			}
		}
	}

	/**Checks that all of the bot's hats have their ID defined.<br>
	If not, uses the given SteamConnection to resolve the IDs of all hats.
	@param conection the SteamConnection to use.
	@throws NullPointerException if connection is null.
	@throws IOException if the given SteamConnection throws IOException.
	*/
	public synchronized void checkHatIDs(SteamConnection connection) throws IOException {
		JSONObject inventory = connection.getInventoryForUser(this.myID);
		JSONObject items = inventory.getJSONObject("rgInventory");
		for(String s : JSONObject.getNames(items)){
			InventoryItem item = fromInventory(items.getJSONObject(s), inventory.getJSONObject("rgDescriptions"));
			if(!item.getQuality().equals(Quality.UNUSUAL)){
				continue;
			}
			for(Hat h : this.myHats){
				if(item.equals(h)){
					h.setID(item.getID());
				}
			}
		}
	}

	/**This function replaces the trading bot's hats with all unusual hats read from the bot's inventory, using the given SteamConnection.<br>
	Since the date and price bought at are unknown, the current date will be used for date bought and the hat's community price 
	times the given defaultRatio will be used for price bought at.<br>
	Sometimes, hard-to-fix errors may cause the bot to track hats which it does not have or fail to track hats which it does have. This function will solve those problems.
	@param connection the connection to use.
	@param defaultRatio the ratio of a hat's community price to set its boughtAt value to. Must be between 0 and 1, inclusive.
	@throws NullPointerConnection if connection is null.
	@throws IOException if the given SteamConnection throws IOException.
	@throws IllegalArgumentException if precondiditons on defaultRatio are violated.
	*/
	public synchronized void readHatsFromInventory(SteamConnection connection, double defaultRatio) throws IOException {
		if(Double.isNaN(defaultRatio) || defaultRatio < 0 || defaultRatio > 1){
			throw new IllegalArgumentException("Invalid default ratio value: " + defaultRatio);
		}
		JSONObject inventory = connection.getInventoryForUser(this.myID);
		JSONObject items = inventory.getJSONObject("rgInventory");
		for(String s : JSONObject.getNames(items)){
			InventoryItem item = fromInventory(items.getJSONObject(s), inventory.getJSONObject("rgDescriptions"));
			if(!item.getQuality().equals(Quality.UNUSUAL)){
				continue;
			}
			if(item.getName().equals("Haunted Metal Scrap") || item.getName().equals("Horseless Headless Horsemann's Headtaker")){
				continue;
			}
			if(this.myHats.contains(item)){
				Hat h = this.myHats.get(item);
				h.setID(item.getID());
				continue;
			}
			PriceRange communityPrice = PriceRange.fromBackpackTFRepresentation(getHatObject(this.pricesObject, item), this.keyScrapRatio);
			Hat h = new Hat(item.getName(), item.getEffect(), communityPrice, communityPrice.middle().scaleBy(defaultRatio, this.keyScrapRatio), LocalDate.now());
			this.myHats.add(h);
			this.myListings.remove(h);
		}
	}

	/**Updates this TradingBot's prices object, recalcuates the key-to-scrap ratio, and updates the community prices on all Hats and BuyListings.<br>
	Additionally, filters this TradingBot's BuyListings to be exactly those which meet the bot's AcceptabilityFunction.
	@param connection a connection to Backpack.tf, used to update the prices object.
	@throws NullPointerException if pricesObject is null.
	@throws IOException if the given BackpackTFConnection throws IOException.
	*/
	public synchronized void updateAndFilter(BackpackTFConnection connection) throws IOException {
		this.pricesObject = connection.getPricesObject();
		this.updateKeyScrapRatio();
		for(Hat h : this.myHats){
			JSONObject j = getHatObject(this.pricesObject, h);
			h.changeCommunityPrice(PriceRange.fromBackpackTFRepresentation(j, this.keyScrapRatio));
		}
		for(BuyListing b : this.myListings){
			JSONObject j = getHatObject(this.pricesObject, b);
			b.changeCommunityPrice(PriceRange.fromBackpackTFRepresentation(j, this.keyScrapRatio));
		}

		forEachUnusual(this.pricesObject, (j, n, e) -> {
			if(this.functions.acceptabilityFunction.determineAcceptability(j, n, Effect.forInt(e), this.keyScrapRatio)){
				PriceRange communityPrice = PriceRange.fromBackpackTFRepresentation(pricesObject, this.keyScrapRatio);
				this.myListings.add(new BuyListing(n, Effect.forInt(e), communityPrice));
			}
		});

		List<BuyListing> toRemove = new ArrayList<BuyListing>();
		for(BuyListing b : myListings){
			JSONObject j;
			try{
				j = getHatObject(this.pricesObject, b);
			} catch(JSONException e){
				//Hat is unpriced
				toRemove.add(b);
				continue;
			}
			if(myHats.get(b) != null){
				toRemove.add(b);
				continue;
			}
			if(!this.functions.acceptabilityFunction.determineAcceptability(j, b.getName(), b.getEffect(), this.keyScrapRatio)){
				toRemove.add(b);
			}
		}
		for(BuyListing b : toRemove){
			myListings.remove(b);
		}
	}

	/**Returns a JSON representation of this TradingBot which is compatible with the fromJSONRepresentation method.
	@return a JSON representation of this TradingBot.
	*/
	public synchronized JSONObject getJSONRepresentation(){
		JSONObject answer = new JSONObject();
		answer.put("id", this.myID);
		answer.put("hats", this.myHats.getJSONRepresentation());
		answer.put("buyListings", this.myListings.getJSONRepresentation());
		return answer;
	}

	/**Constructs and returns a TradingBot constructed from the given JSON input and the given function suite.
	@param input The JSON input to construct from, a JSONObject returned from the getJSONRepresentation() method.
	@param tfConnection a BackpackTFConnection used to retrieve backpack.tf prices.
	@param pricesObject a complete Backpack.tf prices object.
	@param functions The functions for this TradingBot to use.
	@throws NullPointerException if any parameter is null.
	@throws JSONException if input is malformed.
	@throws IOException if the given BackpackTFConnection throws IOException.
	@return a TradingBot constructed from the given JSON input.
	*/
	public static TradingBot fromJSONRepresentation(JSONObject input, BackpackTFConnection tfConnection, FunctionSuite functions) throws IOException {
		String id = input.getString("id");
		ListingCollection<Hat> lch = ListingHashSet.hatSetFromJSON(input.getJSONArray("hats"));
		ListingCollection<BuyListing> lcbl = ListingHashSet.buyListingSetFromJSON(input.getJSONArray("buyListings"));
		return new TradingBot(id, tfConnection.getPricesObject(), functions, lch, lcbl);
	}

	/**Constructs and returns a TradingBot without any hats and with BuyListings automatically filtered.<br>
	BuyListings will be initialized via a call to the updatePrices method, using the AcceptabilityFunction in the given FunctionSuite.
	@param steamID the bot's Steam ID.
	@param tfConnection a BackpackTFConnection used to retrieve backpack.tf prices.
	@param functions The functions for this TradingBot to use.
	@throws NullPointerException if any parameter is null.
	@throws IOException if the given BackpackTFConnection throws IOException.
	@return the described TradingBot.
	*/
	public static TradingBot botWithoutHats(String steamID, BackpackTFConnection tfConnection, FunctionSuite functions) throws IOException {
		TradingBot answer = new TradingBot(steamID, tfConnection.getPricesObject(), functions, null, null);
		answer.updateAndFilter(tfConnection);
		return answer;
	}

	/**Constructs and returns a TradingBot with Hats read from the bot's steam inventory and BuyListings automatically filtered.<br>
	Hats will be initialized via a call to the readHatsFromInventory function, passing through the given defaultRatio value.<br>
	BuyListings will be initialized via a call to the updatePrices method, using the AcceptabilityFunction in the given FunctionSuite.
	@param steamID the bot's Steam ID.
	@param tfConnection a BackpackTFConnection used to retrieve backpack.tf prices.
	@param steamConnection SteamConnection to use with the readHatsFromInventory method.
	@param defaultRatio the ratio of a hat's community price to set its boughtAt value to. Must be between 0 and 1, inclusive.
	@param functions The functions for this TradingBot to use.
	@throws NullPointerException if any parameter is null.
	@throws IllegalArgumentException if precondiditons on defaultRatio are violated.
	@throws IOException if either of the given connections throws IOException.
	@return the described TradingBot.
	*/
	public static TradingBot autoCreate(String steamID, BackpackTFConnection tfConnection, SteamConnection steamConnection, double defaultRatio, FunctionSuite functions) throws IOException {
		TradingBot answer = botWithoutHats(steamID, tfConnection, functions);
		answer.readHatsFromInventory(steamConnection, defaultRatio);
		return answer;
	}

	/**Returns a hash code for this TradingBot.
	@return a hash code for this TradingBot.
	*/
	@Override
	public int hashCode(){
		return this.myID.hashCode() + this.myHats.hashCode() + this.myListings.hashCode();
	}

	/**Returns a boolean indicating whether this TradingBot is equal to the given Object.<br>
	This will return true if and only if the given Object is a TradingBot with the same ID, Hats and BuyListings.
	@param o the Object to compare to
	@return a boolean indicating whether this TradingBot is equal to the given Object.
	*/
	@Override
	public boolean equals(Object o){
		if(this == o){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof TradingBot)){
			return false;
		}
		TradingBot tb = (TradingBot)o;
		return this.myID.equals(tb.myID) && this.myHats.equals(tb.myHats) && this.myListings.equals(tb.myListings);
	}

	/**Returns a String representation of this TradingBot.
	@return a String representation of this TradingBot.
	*/
	@Override
	public String toString(){
		return "trading.economy.TradingBot: ID: " + this.myID;
	}

	private static <T extends Listing> void recalculatePriceInternal(ListingCollection<T> l, BackpackTFConnection connection, int keyScrapRatio, PriceFunction<T> priceFunction, Consumer<? super BackpackTFConnection> callback){
		for(T list : l){
			try{
				Price newPrice = priceFunction.calculatePrice(list, connection, keyScrapRatio);
				list.setPrice(newPrice);
			} catch(IOException e){} //This is not great, but acceptable because callback function should log any errors.
			if(callback != null){
				callback.accept(connection);
			}
		}
	}

	private static JSONObject getHatObject(JSONObject pricesObject, Item l){
		return pricesObject.getJSONObject("response").getJSONObject("items").getJSONObject(l.getName()).getJSONObject("prices").getJSONObject("5").getJSONObject("Tradable").getJSONObject("Craftable").getJSONObject(Integer.toString(l.getEffect().getIntValue()));
	}

	private static interface PricesObjectFunction{
    	void execute(JSONObject j, String name, int effect);
    }

	private static void forEachUnusual(JSONObject a, PricesObjectFunction jof){
		for(String s : JSONObject.getNames(a.getJSONObject("response").getJSONObject("items"))){
			if(s.equals("Haunted Metal Scrap") || s.equals("Horseless Headless Horsemann's Headtaker")){
				continue;
			}
			if(a.getJSONObject("response").getJSONObject("items").getJSONObject(s).getJSONObject("prices").has("5")){
				for(String t : JSONObject.getNames(a.getJSONObject("response").getJSONObject("items").getJSONObject(s).getJSONObject("prices").getJSONObject("5").getJSONObject("Tradable").getJSONObject("Craftable"))){
					jof.execute(a.getJSONObject("response").getJSONObject("items").getJSONObject(s).getJSONObject("prices").getJSONObject("5").getJSONObject("Tradable").getJSONObject("Craftable").getJSONObject(t), s, Integer.parseInt(t));
				}
			}
		}
	}

	private static InventoryItem fromInventory(JSONObject inventoryItem, JSONObject inventoryDescriptions){
		JSONObject itemDescription = inventoryDescriptions.getJSONObject(inventoryItem.getString("classid") + "_" + inventoryItem.getString("instanceid"));
		return new InventoryItem(itemDescription.getString("market_name"), Quality.forInt(Integer.parseInt(itemDescription.getJSONObject("app_data").getString("quality"))), InventoryItem.parseEffect(itemDescription.getJSONArray("descriptions")), inventoryItem.getString("id"));
	}

	private void updateKeyScrapRatio(){
		this.keyScrapRatio = this.functions.keyScrapRatioFunction.calculateRatio(this.pricesObject);
	}
}