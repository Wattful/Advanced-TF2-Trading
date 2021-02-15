package trading.economy;

import trading.net.*;
import org.json.*;
import java.io.*;

//TODO:

/**Functional interface representing a function to calculate the price of a BuyListing given the BuyListing and a BackpackTFConnection, which can be used to make Backpack.tf API calls.<br>
This interface contains static methods which return "default" implementations of BuyListingPriceFunction.
*/

@FunctionalInterface
public interface BuyListingPriceFunction extends PriceFunction<BuyListing>{
	/**Calculates the price of the BuyListing given the listing and the Backpack.tf listings for the unusual hat.
	@param listing the listing to calculate for.
	@param connection a connection to Backpack.tf.
	@param keyScrapRatio the key-to-scrap ratio to use for this calculation.
	@throws NullPointerException if any parameter is null.
	@throws IllegalArgumentException if keyScrapRatio is non-positive.
	@throws IOException the function may throw IOException to accommodate API call failures.
	@return the price for the BuyListing.
	*/
	Price calculatePrice(BuyListing listing, BackpackTFConnection connection, int keyScrapRatio) throws IOException;

	/**Returns a BuyListingPriceFunction which takes the average of the first listingsToConsider buy listings for a hat on Backpack.tf, 
	then overcuts this average by overcutRatio of the hat's community price.<br>
	In considering listings, it will ignore listings made by the bot, as well as listings which do not list a specific unusual effect.<br>
	Additionally, it will never set a price higher than maxRatio of the hat's community price, 
	and if the hat has no buy listings, it will set the price to defaultRatio of the hat's community price.
	@param listingsToConsider The number of listings to average. Must be positive.
	@param overcutRatio Ratio to overcut price by. Notice that undercutting can be done by setting this value to be negative.
	@param maxRatio The maximum ratio to set a price to. Must be positive
	@param defaultRatio The ratio to set a price to when no listings are found. Must be positive.
	@param botID The bot's ID. Used to ignore listings made by the bot.
	@throws NullPointerException if id is null.
	@throws IllegalArgumentException if any preconditions are violated, or any double arguments are NaN or infinite.
	@return the described BuyListingPriceFunction.
	*/
	public static BuyListingPriceFunction overcutByRatio(int listingsToConsider, double overcutRatio, double maxRatio, double defaultRatio, String botID){
		if(Double.isNaN(overcutRatio) || Double.isNaN(maxRatio) || Double.isNaN(defaultRatio)){
			throw new IllegalArgumentException("A value was NaN.");
		}
		if(Double.isInfinite(overcutRatio) || Double.isInfinite(maxRatio) || Double.isInfinite(defaultRatio)){
			throw new IllegalArgumentException("A value was infinite.");
		}
		if(listingsToConsider <= 0){
			throw new IllegalArgumentException("listingsToConsider was non-positive.");
		}
		if(maxRatio <= 0){
			throw new IllegalArgumentException("maxRatio was non-positive.");
		}
		if(defaultRatio <= 0){
			throw new IllegalArgumentException("defaultRatio was non-positive.");
		}
		if(botID == null) {
			throw new NullPointerException();
		}
		return (BuyListing bl, BackpackTFConnection connection, int keyScrapRatio) -> {
			JSONObject listings = connection.getListingsForItem(bl);
			PriceFunctionUtils.removeListingsFromUser(listings, botID);
			PriceFunctionUtils.removeListingsWithoutUnusualEffect(listings);

			Price communityPrice = bl.getCommunityPrice().middle();
			JSONArray buyListings = listings.getJSONObject("buy").getJSONArray("listings");
			int numListings = Math.min(listingsToConsider, buyListings.length());

			if(numListings == 0){
				return communityPrice.scaleBy(defaultRatio, keyScrapRatio);
			}

			Price[] prices = new Price[numListings];
			for(int i = 0; i < numListings; i++){
				JSONObject currenciesObject = buyListings.getJSONObject(i).getJSONObject("currencies");
				int keys = currenciesObject.has("keys") ? currenciesObject.getInt("keys") : 0;
				int refined = currenciesObject.has("metal") ? currenciesObject.getInt("metal") : 0;
				prices[i] = new Price(keys, refined);
			}

			Price tentativePrice = Price.average(keyScrapRatio, prices).scaleBy(overcutRatio + 1.0, keyScrapRatio);
			if(tentativePrice.getDecimalPrice(keyScrapRatio)/communityPrice.getDecimalPrice(keyScrapRatio) > maxRatio){
				return Price.calculate(communityPrice.getDecimalPrice(keyScrapRatio) * maxRatio, keyScrapRatio);
			}
			return tentativePrice;
		};
	}

	/**Returns a BuyListingPriceFunction which returns a fixedratio of the hat's community price.<br>
	The returned function does not use the BackpackTFConnection.
	@param ratioOfPrice ratio of hat's community price to set price to.
	@throws IllegalArgumentException if ratioOfPrice is non-positive, infinite, or NaN.
	@return the described BuyListingPriceFunction.
	*/
	public static BuyListingPriceFunction fixedRatio(double ratioOfPrice){
		if(Double.isNaN(ratioOfPrice)){
			throw new IllegalArgumentException("A value was NaN.");
		}
		if(Double.isInfinite(ratioOfPrice)){
			throw new IllegalArgumentException("A value was infinite.");
		}
		if(ratioOfPrice <= 0){
			throw new IllegalArgumentException("ratioOfPrice was non-positive.");
		}
		return (BuyListing bl, BackpackTFConnection listings, int keyScrapRatio) -> {
			return bl.getCommunityPrice().middle().scaleBy(ratioOfPrice, keyScrapRatio);
		};
	}
}