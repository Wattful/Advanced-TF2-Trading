# Config Files
The bot reads settings from three config files - botInfo.json, botSettings.json, functions.json. 

Default, non-filled-out versions of these configs are located in config/default. Before running the program for the first time, the user must fill out these config files and paste them into this folder.

# botInfo.json
Specifies essential account information related to the bot.
The keys for botInfo.json are:
* `botID`: string. The bot's steam ID.
* `botUsername`: string. The bot's steam username.
* `botPassword`: string. The bot's steam password.
* `sharedSecret` and `identitySecret`: string. The bot's shared secret and identity secret, two values needed for the bot to bypass two-factor authentication. See [here](https://github.com/Jessecar96/SteamDesktopAuthenticator) for more info.
* `APIKey`: string. The bot's Backpack.tf API key.
* `APIToken`: string. The bot's Backpack.tf API token.

# botSettings.json
Specifies several user-defined settings which do have valid default values.
The keys for betSettings.json are:
* `ownerIDs`: JSONArray of string. List of Steam IDs to be considered owners. Default value: `[]`.
* `canHold`: boolean. Whether the bot can hold trades for manual review. Default value: `true`.
* `forgiveness`: number. Forgivenss threshold (see "forgiveness" in [behavior.md](../behavior.md)). Must be between 0 and 1, inclusive. Default value: `0.005`, or about 1 refined for every 10 keys.
* `keyScrapRatio`: string or number. Custom key-to-scrap ratio for the bot to use, or "auto". If "auto" is used, the ratio is calculated using the Backpack.tf community prices, and is recalculated as part of the bot's periodic function. If custom ratio used, must be an integer. Default value: `"auto"`.
* `botReadPath`: string. Path to read bot records from on initialization. Default value: `"../records/tradingBot.json"`.
* `botWritePath`: string. Path to save bot records to. Default value: `"../records/tradingBot.json"`.
* `constructWithHats`: boolean. Whether to read hats from the bot's inventory on initialization (see "initialization" in [behavior.md](../behavior.md)). Default value: `true`.
* `defaultRatio`: number. Ratio of community price to initialize sell listings' purchase price value to when purchase price is unknown (see "sell listings" in [behavior.md](../behavior.md)). Must be between 0 and 1, inclusive. Default value: `0.75`.
* `acceptPath`: string. Path to save records of accepted trades to. Default value: `"../records/acceptedTrades"`.
* `declinePath`: string. Path to save records of declined trades to. Default value: `"../records/declinedTrades"`.
* `holdPath`: string. Path to save records of held trades to. Default value: `"../records/heldTrades"`.
* `logFile`: string. File to save exception logs to. Default value: `"../records/log.txt"`.
* `periodicSleep`: number. Milliseconds to sleep between periodic function calls (see "periodic activities" in [behavior.md](../behavior.md)). Must be a non-negative integer. Default value: `86400000`, or one day.
* `priceUpdateSleep`: number. Milliseconds to sleep between price function calls which use the Backpack.tf API (see "listings" in [behavior.md](../behavior.md)). Must be a non-negative integer. Default value: `2500`.
* `dontSendListings`: boolean. If true, bot will never send listings to Backpack.tf. This is useful for testing purposes. Default value: `false`.
* `offerCheckSleep`: number. Milliseconds to sleep between checking for offers. Values lower than the default may lead to mysterious Steam API errors. Must be a non-negative integer. Default value: `15000`.
* `fallback`: string or null. Optional path to store a fallback version of Backpack.tf community prices. Default value: `"../records/fallback.json"`.

# functions.json
Specifies the user-defined functions used by the bot.

Each specified function has two keys: 
* `name`: string. If a default implementation is being used, the name of the default implementation. If a custom implementation is being used, the fully-qualified name of the class implementing the function's interface.
* `arguments`: JSONArray. Arguments to be passed to the function. If a custom implementation is being used, these arguments will be passed into its constructor.

All default implementations use the average of the Backpack.tf community price for their calculations.

## sellListingPriceFunction
Calculates the price of a sell listing.

This function has four default implementations:

### fixedRatio
Parameters:
* ratioOfPrice: number. Must be between 0 and 1, inclusive.

Sets the hat's price to a fixed ratio of its community price. 

For example, if a hat's community price is 20 keys and ratioOfPrice is 0.75, it will be sold at 15 keys. 

This implementation does not use the Backpack.tf API.

### profitByRatio
Parameters:
* ratioOfPrice: number. Must be between 0 and 1, inclusive.

Sets the hat's price to a fixed ratio of its community price above the hat's purchase price. 

For example, if a hat's community price is 20 keys, it was purchased for 14 keys, and ratioOfPrice is 0.1, it will be sold at 16 keys.

This implementation does not use the Backpack.tf API.

### negativeExponentialFunction
Parameters:
* maxRatio: number. Must be between 0 and 1, inclusive.
* profitRatio: number. Must be between 0 and 1, inclusive.
* speed: number. Must be greater than 0.

Calculates a hat's price using a negative exponential function, with (age + 1) as the base and speed as the negative exponent,
where age is the number of days since this hat was bought.
Using this function, the price of the hat will start at maxRatio percent of the community price, and will lower over time, 
while always ensuring a minimum profit of minimumProfitRatio percent of the community price.
Higher values of speed cause the price to drop faster.
Formally, if CMP is the hat's community price, and BAP is the price the hat was bought at, then
the function's value at age = 0 will be sellRatio * CMP, and the function will approach BAP + (minimumProfitRatio * CMP) as age approaches infinity.

This implementation does not use the Backpack.tf API.

### undercutByRatio
Parameters:
* listingsToConsider: number. Must be a positive integer.
* undercutRatio: number.
* defaultRatio: number. Must be between 0 and 1, inclusive.
* mustProfit: boolean.

Calculates a hat's price based on other sell listings for the same hat on Backpack.tf.
This function calculates the average of the best listingsToConsider sell listings for this hat on Backpack.tf, then sets the hat's price to (1 - undercutRatio) times the average.
A negative undercutRatio value is allowed, which will result in overcutting.
In considering listings, it will ignore listings made by the bot itself.
If mustProfit is true, it will never set its price lower than the price that the hat was bought for.
If there are no other sell listings for the hat, it will set the price to defaultRatio times the hat's community price.

For example, if the calculated average if 20 keys, and undercutRatio is 0.05, then the hat's price will be set to 19 keys.

## buyListingPriceFunction
Calculates the price of a buy listing.
This function has four default implementations:

### fixedRatio
Parameters:
* ratioOfPrice: number. Must be between 0 and 1, inclusive.

Sets the buy listing's price to a fixed ratio of its community price. 

For example, if an items's community price is 20 keys and ratioOfPrice is 0.75, it will be bought at 15 keys.

This implementation does not use the Backpack.tf API.

### overcutByRatio
Parameters:
* listingsToConsider: number. Must be a positive integer.
* overcutRatio: number.
* maxRatio: number. Must be between 0 and 1, inclusive.
* defaultRatio: number. Must be between 0 and 1, inclusive.

Calculates a hat's price based on other sell listings for the same hat on Backpack.tf.
This function calculates the average of the best listingsToConsider buy listings for this hat on Backpack.tf, then sets the hat's price to (1 + overcutRatio) times the average.
A negative overcutRatio value is allowed, which will result in undercutting.
In considering listings, it will ignore listings made by the bot itself.
It will never make a listing for greater than maxRatio times the item's community price.
If there are no other sell listings for the item, it will set the price to defaultRatio times the its community price.

For example, if the calculated average if 20 keys, and overcutRatio is 0.05, then the item will be bought at 21 keys.

## acceptabilityFunction
Determines which unusual items the bot will make a buy listing for.
All of these imeplementations, including acceptAll, reject items which have a price in a currency other than metal or keys, such as USD.
In addition, the bot will never make a buy listing for a unpriced hat, as the acceptabilityFunction will not even be called for these hats.

This function has four default implementations:

### acceptAll
Parameters: none

Accepts every hat, except those priced in a currency other than metal or keys.

### checkData
An acceptabilityFunction which accepts a hat only if several conditions are satisfied, one for each paramter.

Parameters:
* minKeys: number. Checks that the hat's average value is above minKeys keys.
* maxKeys: number. Checks that the hat's average value is below maxKeys keys. Negative or 0 value indicates no restriction.
* maxRange: number. Checks that the range between the hat's high and low values is below maxRange keys. Negative value indicates no restriction.
* lastUpdate: number. Checks that the hat's community price was updated within lastUpdate seconds. Negative or 0 value indicates no restriction.

### checkType
Parameters:
* nameMode: boolean.
* names: null or JSONArray of string.
* effectMode: boolean.
* effects: null or JSONArray of string or number.

Determines acceptability based on the hat itself and the hat's effect.
The hat's name and effect will be checked against arrays provided by the user. 

The user can choose, through the mode parameters, whether these arrays act as an acceptlist or denylist of names and effects.
If the mode variable is true, then the array contains the names or effects which will be accepted, if it is false, 
it contains those which will not be accepted.
In addition, the arrays can be null, which indicates no restriction for names or effects, and in which case that mode variable is ignored.
Effects can be specified as either an effect name or the effect's integer code (see [here](https://backpack.tf/developer/particles)).

Note that regular expressions are accepted for hat's names (but NOT effects). For example, "Taunt:.\*" matches all unusual taunts.

### checkDataAndType
Parameters:
* minKeys: number.
* maxKeys: number.
* maxRange: number.
* lastUpdate: number.
* nameMode: boolean.
* names: null or JSONArray of string.
* effectMode: boolean.
* effects: null or JSONArray of string or number.

Accepts a hat if it would be accepted by both the checkData and the checkType functions.

## ListingDescriptionFunction
Determines the Backpack.tf description to use for each listing.

This function has two default implementations:

### simpleDescription
Parameters: none

Returns a simple description 
which states the price of the item and states that the offer will be accepted instantly.

More precisely, the description will take the following form:

"[Buying/Selling] this hat for X keys and X refined. Send me an offer, I will accept instantly! Item offers held for manual review."

Note that "and X refined" will be omitted if the listing's Price has 0 refined.

### descriptionWithSayings
Parameters:
* placeBefore: boolean
* sayings: JSONArray of string

Returns the same simple description described in the simpleDescription function, 
with a saying placed before or after the simple description.
The saying will be randomly selected each time from the provided array of strings.
It will be placed before the simple description if placeBefore is true, otherwise it will be placed after the simple description.

## How to specify custom functions
The user can specify custom implementations of each function. These implementations must be written in Java.

To write a custom function, you must write a class which implements an interface corresponding to the function:
* sellListingPriceFunction: [trading.economy.HatPriceFunction](../src/trading/economy/HatPriceFunction.java)
* buyListingPriceFunction: [trading.economy.BuyListingPriceFunction](../src/trading/economy/BuyListingPriceFunction.java)
* acceptabilityFunction: [trading.economy.AcceptabilityFunction](../src/trading/economy/AcceptabilityFunction.java)
* listingDescriptionFunction: [trading.economy.ListingDescriptionFunction](../src/trading/economy/ListingDescriptionFunction.java)

See each interface for more specific documentation on how to write an implementation of that function.

The class that you write must have a public constructor. This constructor can take any number of arguments, but all arguments must be expressable as JSON literals (int, double, boolean, String, JSONObject, JSONArray).
Once you have written the class, set `name` to the fully qualified name of that class, and `arguments` to the arguments that you would like to provide to the class' constructor, if any.
