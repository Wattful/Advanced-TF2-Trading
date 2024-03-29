package trading.economy;

import trading.net.*;
import java.io.*;

//TODO: Update documentation

/**Functional interface representing a function which calculates a new price for a Listing.<br>
In addition to the listing itself, the function takes in a BackpackTFConnection which it can use to make calls, and a key-to-scrap ratio.
*/

@FunctionalInterface
public interface PriceFunction<T extends Listing>{
	/**Calulates and returns a Price and priority for the given Listing. Both the price and priority can be null.
	@param listing The Listing to consider.
	@param connection a connection to Backpack.tf.
	@param keyScrapRatio the key-to-scrap ratio to use for this calculation.
	@throws NullPointerException if listing is null.
	@throws IllegalArgumentException if keyScrapRatio is non-positive.
	@throws IOException the function may throw IOException to accommodate API call failures.
	@return the calculated price and priority.
	*/
	Pair<Price, Integer> calculatePrice(T listing, BackpackTFConnection connection, int keyScrapRatio) throws IOException;
}