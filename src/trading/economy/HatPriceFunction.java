package trading.economy;

import java.time.*;
import trading.net.*;
import org.json.*;
import java.io.*;

//TODO:

/**Functional interface representing a function to calculate the price of a Hat given the Hat and a BackpackTFConnection, which can be used to make Backpack.tf API calls.<br>
This class contains static methods which return "default" implementations of HatPriceFunction.
*/

@FunctionalInterface
public interface HatPriceFunction extends PriceFunction<Hat>{
	/**Calulates and returns a Price and priority for the given hat.<br>
	Both price and priority can be nulled. A null price indicates that price should be unset, and a null priority indicates a priority lower than any other.
	@param h The hat to consider.
	@param connection a connection to Backpack.tf.
	@param keyScrapRatio the key-to-scrap ratio to use for this calculation.
	@throws NullPointerException if h is null.
	@throws IllegalArgumentException if keyScrapRatio is non-positive.
	@throws IOException the function may throw IOException to accommodate API call failures.
	@return the hat's price and priority.
	*/
	Pair<Price, Integer> calculatePrice(Hat h, BackpackTFConnection connection, int keyScrapRatio) throws IOException;

	/**Returns a HatPriceFunction which uses a negative exponential function, with (age + 1) as the base and speed as the negative exponent, 
	where age is the number of days since this hat was bought.<br>
	Using this function, the price of the hat will start at sellRatio percent of the community price, and will lower over time, 
	while always ensuring a minimum profit of minimumProfitRatio percent of the community price.<br>
	Higher values of speed cause the price to drop faster.<br>
	Formally, if CMP is the hat's community price, and BAP is the price the hat was bought at, then
	the function's value at age = 0 will be sellRatio * CMP, and the function will approach BAP + (minimumProfitRatio * CMP) as age approaches infinity.<br>
	The returned function does not use the BackpackTFConnection, and will not return a priority.
	@param sellRatio The maximum ratio of the base price that the hat can be sold at.
	@param minimumProfitRatio The minimum profit that the hat will be sold at, as a ratio of the hat's base price.
	@param speed The speed at which the price decreases.
	@throws IllegalArgumentException if any value is NaN, infinite or non-positive.
	@return a HatPriceFunction which uses a negative exponential function to calculate the hat's price. 
	*/
	public static HatPriceFunction negativeExponentialFunction(final double sellRatio, final double minimumProfitRatio, final double speed){
		if(Double.isNaN(sellRatio) || Double.isNaN(minimumProfitRatio) || Double.isNaN(speed)){
			throw new IllegalArgumentException("A value was NaN.");
		}
		if(Double.isInfinite(sellRatio) || Double.isInfinite(minimumProfitRatio) || Double.isInfinite(speed)){
			throw new IllegalArgumentException("A value was infinite.");
		}
		if(sellRatio <= 0){
			throw new IllegalArgumentException("sellRatio was non-positive.");
		}
		if(minimumProfitRatio <= 0){
			throw new IllegalArgumentException("minimumProfitRatio was non-positive.");
		}
		if(speed <= 0){
			throw new IllegalArgumentException("speed was non-positive.");
		}
		return (Hat hat, BackpackTFConnection listings, int keyScrapRatio) -> {
			int age = Period.between(hat.getDateBought(), LocalDate.now()).getDays();
			Price communityPrice = hat.getCommunityPrice().middle();
			double max = communityPrice.getDecimalPrice(keyScrapRatio) * sellRatio;
			double base = hat.getPurchasePrice().getDecimalPrice(keyScrapRatio) + (minimumProfitRatio * communityPrice.getDecimalPrice(keyScrapRatio));
			double value = ((max - base) * (Math.pow(age + 1, -speed))) + base;
			return new Pair<>(Price.calculate(value, keyScrapRatio), null);
		};
	}

	/**Returns a HatPriceFunction which returns the price the hat was bought at, plus profitRatio times the hat's community price.<br>
	The returned function does not use the BackpackTFConnection, and will not return a priority.
	@param profitRatio the ratio above the bought at price to sell the hat.
	@throws IllegalArgumentException if profitRatio is NaN or negative.
	@return a HatPriceFunction which returns the price the hat was bought at, plus profitRatio times the hat's community price.
	*/
	public static HatPriceFunction profitByRatio(final double profitRatio){
		if(Double.isNaN(profitRatio)){
			throw new IllegalArgumentException("profitRatio was NaN.");
		}
		if(Double.isInfinite(profitRatio)){
			throw new IllegalArgumentException("profitRatio was infinite.");
		}
		if(profitRatio <= 0){
			throw new IllegalArgumentException("profitRatio was non-positive.");
		}
		return (Hat hat, BackpackTFConnection listings, int keyScrapRatio) -> {
			Price communityPrice = hat.getCommunityPrice().middle();
			double value = hat.getPurchasePrice().getDecimalPrice(keyScrapRatio) + (profitRatio * communityPrice.getDecimalPrice(keyScrapRatio));
			return new Pair<>(Price.calculate(value, keyScrapRatio), null);
		};
	}

	/**Returns a HatPriceFunction which returns a fixed ratio of the hat's community price.<br>
	The returned function does not use the BackpackTFConnection, and will not return a priority.
	@param ratioOfPrice the ratio to use.
	@throws IllegalArgumentException if ratioOfPrice is NaN or non-positive.
	@return a HatPriceFunction which returns a fixed ratio of the hat's community price.
	*/
	public static HatPriceFunction fixedRatio(final double ratioOfPrice){
		if(Double.isNaN(ratioOfPrice)){
			throw new IllegalArgumentException("ratioOfPrice was NaN.");
		}
		if(Double.isInfinite(ratioOfPrice)){
			throw new IllegalArgumentException("ratioOfPrice was infinite.");
		}
		if(ratioOfPrice <= 0){
			throw new IllegalArgumentException("ratioOfPrice was non-positive.");
		}
		return (Hat hat, BackpackTFConnection listings, int keyScrapRatio) -> {
			Price communityPrice = hat.getCommunityPrice().middle();
			double value = ratioOfPrice * communityPrice.getDecimalPrice(keyScrapRatio);
			return new Pair<>(Price.calculate(value, keyScrapRatio), null);
		};
	}

	/**Returns a HatPriceFunction which takes the average of the first listingsToConsider sell listings for a hat on Backpack.tf, 
	then undercuts this average by undercutRatio of the hat's community price.<br>
	In considering listings, it will ignore listings made by the bot.<br>
	If mustProfit is true, it will never set its price lower than the price that the hat was bought for.<br>
	If the hat has no sell listings, it will set the price to defaultRatio of the hat's community price.<br>
	The returned function will not return a priority.
	@param listingsToConsider The number of listings to average. Must be positive.
	@param overcutRatio Ratio to undercut price by. Notice that overcutting can be done by setting this value to be negative.
	@param defaultRatio The ratio to set a price to when no listings are found. Must be positive.
	@param mustProfit Whether to disable the function from setting a price lower than that the Hat was bought for.
	@param botID The bot's ID. Used to ignore listings made by the bot.
	@throws NullPointerException if id is null.
	@throws IllegalArgumentException if any preconditions are violated, any double arguments are NaN or infinite, or any argument is null.
	@return the described HatPriceFunction.
	*/
	public static HatPriceFunction undercutByRatio(int listingsToConsider, double undercutRatio, double defaultRatio, boolean mustProfit, String botID){
		if(Double.isNaN(undercutRatio) || Double.isNaN(defaultRatio)){
			throw new IllegalArgumentException("A value was NaN.");
		}
		if(Double.isInfinite(undercutRatio) || Double.isInfinite(defaultRatio)){
			throw new IllegalArgumentException("A value was infinite.");
		}
		if(listingsToConsider <= 0){
			throw new IllegalArgumentException("listingsToConsider was non-positive.");
		}
		if(defaultRatio <= 0){
			throw new IllegalArgumentException("defaultRatio was non-positive.");
		}
		if(botID == null) {
			throw new NullPointerException();
		}
		return (Hat h, BackpackTFConnection connection, int keyScrapRatio) -> {
			JSONObject listings = connection.getListingsForItem(h);
			PriceFunctionUtils.removeListingsFromUser(listings, botID);
			PriceFunctionUtils.removeListingsWithoutUnusualEffect(listings);

			Price communityPrice = h.getCommunityPrice().middle();
			JSONArray sellListings = listings.getJSONObject("sell").getJSONArray("listings");
			int numListings = Math.min(listingsToConsider, sellListings.length());

			if(numListings == 0){
				return new Pair<>(communityPrice.scaleBy(defaultRatio, keyScrapRatio), null);
			}

			Price[] prices = new Price[numListings];
			for(int i = 0; i < numListings; i++){
				JSONObject currenciesObject = sellListings.getJSONObject(i).getJSONObject("currencies");
				int keys = currenciesObject.getInt("keys");
				int refined = currenciesObject.has("metal") ? currenciesObject.getInt("metal") : 0;
				prices[i] = new Price(keys, refined);
			}

			Price tentativePrice = Price.average(keyScrapRatio, prices).scaleBy(1 - undercutRatio, keyScrapRatio);
			if(tentativePrice.getDecimalPrice(keyScrapRatio) < h.getPurchasePrice().getDecimalPrice(keyScrapRatio) && mustProfit){
				return new Pair<>(h.getPurchasePrice(), null);
			}
			return new Pair<>(tentativePrice, null);
		};
	}
}