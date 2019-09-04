# Advanced-TF2-Trading
Java and node.js based Team Fortress 2 trading bot with backpack.tf API integration.

This bot trades Team Fortress 2 unusual hats. It does not work for any other steam games, and it will not automatically trade items that are not unusual hats or TF2 currency.

# Features
* Automatically accepts and declines offers
	* Evaluates whether a trade offer meets the bot's prices and accepts or declines accordingly
	* If the offer is an item offer, holds for manual review
	* If the offer was sent by the onwer, bot automatically accepts
* Keeps records of all accepted and declined trades
* Automatically sends listings to Backpack.tf
* Tracks unusual hats in bot's inventory
* Automatically prices buy and sell listings
	* Keeps editable records of all buy and sell listings in JSON files.
	* Creates buy listings for all unusual hats that meet certain criteria
	* Bases price for buy listings on other buy listings on the same item
	* Sell listings start at a high price, and lower over time, approaching the original buy price

# Setup
Clone the repository. To use this bot, you must have a Steam account (obviously) and a Backpack.tf API key.
### Node.js dependencies
You must have [Node.js](https://nodejs.org/en/) installed on your computer and added to the PATH. Otherwise, the bot will crash and give an ambiguous error message.
The Node.js portion of the bot has several dependencies.  
To install these, first you must have [Node Package Manager](https://www.npmjs.com/) installed on your computer.  
For each package, navigate to the src/trading folder and type `npm install <package>`, replacing `<package>` with the name of the package.
Here are the dependencies:
* steam-tradeoffer-manager
* steam-user
* steamcommunity
* steam-totp
* readline

### Java Config File
The bot's config is located in Configuration.java.
The config contains several constant values. By default, the values are empty.
You **MUST** fill out the config for the bot to work.
These are the values of the config:
```
value name         value description
USERNAME           The bot's Steam username
PASSWORD           The bot's Steam password
SHARED_SECRET      The bot's Steam authenticator shared secret (see [Steam Desktop Authenticator](https://github.com/Jessecar96/SteamDesktopAuthenticator))
IDENTITY_SECRET    The bot's Steam authenticator identity secret (see [Steam Desktop Authenticator](https://github.com/Jessecar96/SteamDesktopAuthenticator))
API_KEY            The bot's Backpack.tf API key
API_TOKEN          The bot's Backpack.tf API token
BOT_ID             The bot's steamID64
BOT_ID3            The bot's steamID3
OWNER_ID           Optional value. The bot "owner's" steamID64. Any offers from the owner will be automatically accepted.
```
### Offer Checking Config object
In the offerChecking.js file, there is an object called config.
You **MUST** fill out this object for the bot to work.
The values in this config are the same as those in the java config.

# Offer logic
Upon receiving an offer, the bot calculates the total value of both sides of the proposed trade.
If the trade partner's ID matches the OWNER_ID in the config, the trade offer is automatically accepted.
If the values are equal, or if the bot's value is lesser, the offer will be accepted. (Note that the offer will still be accepted if the bot's value is slightly less than the partner's value - there's no point in declining an unusual trade because of a difference of a couple of scrap).
If the offer is not accepted, and the trading partner has an item which is not on the bot's pricelist, the offer will be held for manual review. The user must log into the bot's steam account and decide whether to accept or decline the offer.
If the offer is neither accepted or held, it is declined.

# Offer output and records
The bot will output information as it goes through the offer evaluation process.
Here's an example of the bot's output:
```
We received an offer.
Our value: 8280 Their value: 8280
The offer was accepted.
```
After the offer is evaluated, the bot will save a record of the trade in the offers folder.
This record documents all items on both sides of the trade.
An example of the record can be found in the repo's offers folder.

# JSON listing files
The bot maintains editable JSON files which represent all of the buy and sell listings it tracks.
Sell listings represent hats that the bot is trying to sell, while buy listings respresent hats that the bot wants to buy.
These file are found in the json directory, and are by default named allHats.json for sell listings and allListings.json for buy listings.
The user can edit the files if they want to manually change listing prices.
The allHats.json file consists of an array of Hat objects.
Hat object documentation:
```
value name         value description
name               Name of the hat.
effect             The backpack.tf priceindex assigned to the hat's effect (priceindex table can be found in reference.json).
boughtAt           The percent of the hat's backpack.tf value the hat was bought for. Used to calculate sell prices.
communityPrice     The backpack.tf community price of the hat.
id                 The hat item id in the bot's inventory. May be null if the bot has not yet collected the hat's id.
age                The approximate amount of time that the hat has been in the bot's inventory in days. Used to calculate sell prices.
```
If the user wishes to manually raise or lower the price of a hat, they should edit the age value. Higher ages result in lower prices, without going under the price that the hat was bought for.

The allListings.json file consists of an array of Listing objects.
Listing object documentation:
```
value name         value description
name               Name of the hat.
effect             The backpack.tf priceindex assigned to the hat's effect (priceindex table can be found in reference.json).
communityPrice     The backpack.tf community price of the hat.
multiplier         Percent of the hat's backpack.tf value that the bot is buying the hat for.
```
Editing listings is currently unsupported, any changes made will be reverted when the bot updates its listings.

# Automatic creation of buy and sell listings
The bot automatically creates sell listings for all hats that it automatically trades for.

The bot automatically creates buy listings for all hats that meet certain criteria:
* The hat has a community price in keys.
* The hat's community price is under MAX_KEYS, 30 by default.
* The difference between the high and low community prices of the hat is less than MAX_RANGE, 0 by default.
* The hat's community price was last updated within LAST_UPDATE seconds, 182 days by default.
* The hat's name is accepted in reference.json (see below)
* The hat's effect is not unaccepted in reference.json (see below)
All constants can be found in BuyListing.java
With these rules, the bot will create approximately 450 buy listings.

# Backpack.tf API Integration
The bot posts buy and sell listings on backpack.tf for every item in the allHats and allListings files. (Note that the bot edits these files during operation).
The bot collects pricing data from backpack.tf, and will recognize any changes in hat prices.

# Periodic function
The bot performs four "housekeeping" operations periodically, sleeping for PERIODIC_SLEEP milliseconds between each operation.
PERIODIC_SLEEP is six hours by default, meaning that each operation is performed once every day.
The operations are as follows.
### Grow hats
Increases the age of every hat by 1, decreasing the hats' prices. Updates IDs of any hats that are missing them.
### Update prices
Gets prices of all hats from Backpack.tf.
### Update listings
Removes all buy listings that no longer fit the bot's criteria, and add buy listings that meet the criteria but were not previously tracked.
### Look
Update prices of all buy listing by "looking" at them (see automatic pricing, below). Between each look, the bot sleeps for LOOK_SLEEP milliseconds, 30 seconds by default.

# Automatic Pricing
The bot prices buy and sell listings according to different algorithms.
### Sell listings
Sell listings are priced according to a negative exponential function, with age as the exponent. This ensures that while prices will decrease over time, the price will never go below what the hat was bought for.
The initial value is SELL_RATIO, set to .9 by default.
The function approches the ratio the hat was bought for plus MIN_PROFIT, set to .035 by default.
### Buy Listings
Buy listings are priced according to other listings on Backpack.tf.
Currently this algorithm is rigid, but I am planning on making it more customizable in the future.
The bot "looks" at the other buy listings for the same hat on Backpack.tf.
If it finds no other listings for the hat, the bot sets the listing price at MIN_BUY, which is .5 times the community price by default.
If it finds one other listing for the hat, it sets the price as the average of MIN_BUY and the price of the other listing.
If it finds two or more listings for the hat, it sets the price as slightly more than the average of the other listing's prices.

# User input
The bot takes a limited amount of command line imput at runtime.
These are all the inputs that the bot can take
```
command            effect
exit               save allHats and allListings files and exit safely.
save               save allHats and allListings without exiting.
autobuyandsell     sends listings to Backpack.tf.
getid              gets id for all hats that are missing it.
```

# Reference.json File
Reference.json contains important information for the bot in json format.
There are three objects in reference:
```
value name         value description
effect             Object that matches the name of an effect to its Backpack.tf priceindex. This should NOT be modified under any circumstances.
accepted           All hats that the bot will create buy listings for. Most hats are included be default. Customization is encouraged.
unaccepted         All effects that the bot will NOT create buy listings for. Very low tier effects are included by default. Customization is encouraged.
```

# Build and Run
In order to build and run the bot, navigate to the src directory.  
To build:   
`javac -cp ../lib/*;. trading/*.java`  
To run:  
`java -Djdk.tls.client.protocols="TLSv1,TLSv1.1,TLSv1.2" -cp ../lib/*;. trading.Main`

# Troubleshooting
Here are some errors you might experience.  
`Native offer checking stopped due to an error of type Not logged in`  
This error can occur when trying to respond to an offer. The best option is to simply restart the bot and try again.  
`javax.net.ssl.SSLHandshakeException: Received fatal alert: record_overflow`  
If this error (or a similar one) occurs, it means that you are running the programs without the recommended Djdk command line option seen above.  
`java.lang.IllegalStateException: native offer checking has stopped working` immediately after starting the bot.
If you get this error, this means that the Node.js portion of the program is not working. This could happen for several reasons. Firstly, you may not be running the program from the src directory. This can additionally happen if you have not installed all required NPM packages, or if you do not have Node.js installed and added to the Path. To get a more specific error message, try running node offerChecking.js yourself.
```
Exception in thread "main" java.lang.ExceptionInInitializerError
Caused by: org.json.JSONException: A JSONArray text must start with '[' at 0 [character 1 line 1]
```
This error (and similar ones) occur if the allHats or allListings json files are empty or are improperly formatted.  
Instead of an empty file, have the file solely consist of an empty json array. (ie [] ).

# Known issues
The bot has no way of removing listings from Backpack.tf. Additionally, the Backpack.tf API *somtimes* does not allow the bot to immediately change a listing's price.
This can rarely lead to situations where a the bot has a listing on Backpack.tf which it does not know about or a listing has an outdated price.

# Other notes
Right now the bot is relatively uncustomizable, I plan on adding more options in the future.