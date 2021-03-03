# Bot Behavior

# Initialization
Upon program start, the bot will look for a record of its buy and sell listings at a location defined in botSettings.json. If this record is found, it will initialize itself from the record. 

If not, the bot will create buy listings according to its acceptabilityFunction (see "custom functions"). If the constructWithHats user option is enabled, it will automatically create sell listings by reading the bot's inventory (see "sell listings"). If not, the bot will start without any sell listings.

During initialization, the bot will attempt to get a prices object from Backpack.tf, reading from a fallback if that operation fails. If both of these operations fail, the bot will be unable to start, and will exit cleanly.

# Bot records
The bot saves a record of all its buy and sell listings as a JSON file at a user-defined location. This is used to store the bot's data between runs.

# Custom Functions
The user can specify four custom functions which determine the bot's behavior. They are:
1. sellListingPriceFunction: Calculates prices for sell listings
2. buyListingPriceFunction: Calculate prices for buy listings
3. acceptabilityFunction: Determines which items to place buy listings for (ie which ones are "acceptable")
4. listingDescriptionFunction: Determines the description to use for a listing on Backpack.tf.

Multiple default implementations for each of these functions are provided. 
Additionally, the user can write custom versions of these functions. See [config/README.md](config/README.md) for details.

# Listings
The bot automatically creates and manages its buy and sell listings, calculating the prices using the buyListingPriceFunction and sellListingPriceFunction.

Upon creating a listing, the listing's price will be initially unset, and won't be calculated until all listing prices are recalculated.

When recalculating listings, some price functions call the Backpack.tf API to see other listings on the same item. When recalculating prices, if a price function uses the Backpack.tf API, the bot will sleep priceUpdateSleep milliseconds (default 2500) between function calls. This is to prevent Backpack.tf API rate limiting.

All prices are rounded to the nearest refined.

### Buy listings
The bot creates buy listings for all unusual items which pass its acceptabilityFunction. The prices for these buy listings are calculated using buyListingPriceFunction.

### Sell listings
The bot automatically creates a sell listing for any unusual item which it acquires in a trade. The prices for these sell listings are calculated using sellListingPriceFunction.

Sell listings are prioritized when sending listings to Backpack.tf, ensuring that they don't get left out due to exceeding the limit on total listings.

If the user wants to create sell listings for items that the bot did not automatically trade for, such as items from a manually reviewed trade, the bot can read its inventory to create listings for any unusual items that it was not previously tracking. This can be achieved using the `readitems` command.

When creating sell listings using this method, the bot does not know what price it purchased that hat for. The item's purchase price is essential to some sellListingPriceFunctions, which ensure that the item is not sold at a loss. To fill in a purchase price for these sell listings, the bot will use the defaultRatio value, specified in botSettings.json. The purchase price will be set to defaultRatio times the item's community price.

# Offer checking
The bot checks offers via its Node.js component. It will check for a new offer every offerCheckSleep milliseconds (default 15000). Values lower than this may lead to mysterious Steam errors. If multiple offers are received in short succession, it will process each one in the order they were received, sleeping for offerCheckSleep milliseconds between each offer.

# Offer logic
Upon receiving an offer, the bot calculates the total value of both sides of the proposed trade.

If the trade partner is an owner as specified in betSettings.json, the trade will be accepted.

If the bot's value is less than or equal to the partner's value, within the forgiveness threshold (see "forgiveness"), the offer will be accepted.

Otherwise, if either side has an item which is not on the bot's pricelist, and holding is enabled, the offer will be held for manual review. The user must log into the bot's Steam account and manually decide whether to accept or decline the offer.

If the offer is neither accepted or held, it is declined.

The list of the bot's owners, its forgiveness threshold, and whether it can hold are all specified in botSettings.json. See [config/README.md](config/README.md) for details.

### Forgiveness
The user can specify a forgiveness value, which is a real number between 0 and 1, inclusive. If the bot's value in a trade is higher than the partner's value, but its value is greater than (partnerValue * (1 - forgiveness)), then it will accept the trade anyway. This feature prevents high-value trades from being declined due to a trivial difference in value. A forgiveness value of 0 specifies no forgivenss, whereas a value of 1 would cause every trade to be accepted, regardless of each sides' value.

# Offer output and records
The bot will output information as it goes through the offer evaluation process.
Here's an example of the bot's output:
```
We received an offer.
Our value: 8280 Their value: 8280
The offer was accepted.
```
After the offer has been accepted or declined, it will save a record of that offer in a location specified in botSettings.json.
This record documents all items on both sides of the trade, as well as the bot's evaluations of each item. The evaluations are all recorded in scrap.
Examples of trade records can be found in records/acceptedOffers and records/declinedOffers.

# Periodic activities
Periodically, the bot will perform several maintenence actions. How often these are performed is specified in botSettings.json, and is once per day by default. All of these actions can be manually performed using command-line input (see "command line input").

The periodic actions are:
1. Retrieves community prices from Backpack.tf and saves it to a fallback location. If this operation fails, uses a fallback specified in botSettings.json.
2. Recalculates the key-to-refined ratio (unless the user specified a custom ratio in botSettings.json).
3. Uses the bot's acceptabilityFunction to refilter its buy listings.
4. Recalculates prices for all buy and sell listings. (This will not be done in the first cycle after startup if the bot was initialized from JSON)
5. Verifies that the bot has correct Steam item IDs for sell listings (as these are required to post sell listings on Backpack.tf).
6. Sends listings to Backpack.tf.

# Command-line input
During runtime, the bot accepts several command-line inputs. These inputs can be used to force the bot to perform certain actions or output information about the bot and its items.

Here are the commands:
* `exit`:  Saves the bot's records and exits safely.
* `save`:  Saves the bot's records without exiting.
* `sendlistings`: Sends the bot's listings to Backpack.tf.
* `getid`:  Verifies that the bot has correct Steam item IDs for all sell listings.
* `readitems`:  Creates sell listings for untracked unusual items in the bot's inventory. See "sell listings" section for more details.
* `updateandfilter`: Retrieves community prices from Backpack.tf, recalculates key-to-refined ratio, and uses the bot's acceptabilityFunction to refilter its buy listings.
* `recalculateprices`: Recalculates prices for all buy and sell listings.
* `keyscrapratio`: Outputs the current key-to-scrap ratio being used by the bot.
* `numberitems`: Outputs the number of buy and sell listings that the bot has.
* `itemprice "EFFECT" "NAME"`: Outputs the buy or sell price that the bot has for the given item, if any. Note that both the effect and name must be double quoted for this command to work.
* `iteminfo "EFFECT" "NAME"`: Outputs all information that the bot has for the given item, as a JSONObject. Note that both the effect and name must be double quoted for this command to work.
* `sellprices`: Outputs prices for all of the bot's sell listings.
* `buyprices`: Outputs prices for all of the bot's buy listings.
* `botinfo`: Outputs the bot's botInfo.json config file.
* `botsettings`: Outputs the bot's botSettings.json config file.
* `functions`: Outputs the bot's functions.json config file.

# Known issues
* The bot has no way of removing listings from Backpack.tf. Additionally, the Backpack.tf API *sometimes* does not allow the bot to immediately change a listing's price.
This can lead to situations where a the bot has a listing on Backpack.tf which it does not know about or a listing has an outdated price. To combat this, I would recommend deleting all listings on Backpack.tf before manually resending the listings from time to time.

* The bot cannot make a listing for an unpriced hat or a hat with a price in a currency other than keys or metal, such as USD. If it tries to, errors may occur. This doesn't cause any problems with the offer acceptance logic, and this will only come up if an acceptabilityFunction accepts a hat with a bad currency or if one manually tries to make a sell listing for such a hat with `readitems`.

# A Word of Warning
Unless the acceptabilityFunction says otherwise, this bot will make buy listings for unusual taunts! The checkType default implementation of acceptabilityFunction makes it easy to avoid making listings for taunts.
