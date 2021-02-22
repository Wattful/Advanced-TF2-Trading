# Advanced-TF2-Trading

Java and node.js based Team Fortress 2 trading bot with backpack.tf API integration.

This bot trades Team Fortress 2 unusual hats. It does not work for any other steam games, and it will not automatically trade items that are not unusual hats or TF2 currency.

# Features
* Automatically accepts and declines offers
	* Evaluates whether a trade offer meets the bot's prices and accepts or declines accordingly
	* Holds item offers for manual review
	* Accepts all offers from the bot's owners
	* User can specify "forgiveness" value which prevents high-value trades from being declined due to a trivial difference
	* Keeps records of all trades
* Automatically prices buy and sell listings
	* Creates buy listings for all unusual hats that meet user-defined criteria
	* Listing prices calculated using user-defined function
		* Multiple default price calculation functions provided, with the ability to specify custom functions
	* Built-in under and overcutting scripts
* Backpack.tf integration
	* Automatically sends listings to Backpack.tf
	* Keeps track of community prices and key-to-refined ratio
	* Use other listings in under and overcutting scripts

# Setup
To use this trading bot, you must have the following:
1. A Steam account (obviously).
2. A Backpack.tf API key with elevated access (see [Backpack.tf's developer page](https://backpack.tf/developer)). This API key must be for the Steam account you are trading with.
3. Java 14 or higher (download and install [here](https://www.oracle.com/java/technologies/javase/jdk14-archive-downloads.html)).
4. Node.js installed and added to the PATH (download and install [here](https://nodejs.org/en/download/)).

Once you have all of those, follow these steps to setup:
1. Clone this repository.
2. Navigate to the `src/nodejs` folder in the command line. Run `npm install`. This will install all Node.js dependencies.
3. Install Java Dependencies (see "Java Dependencies" section)
4. Specify settings in the bot's config files (see "Config Files" section and config/README.md)

Once you are finished, you are ready to build and run the bot.

# Build and Run
This project is written in Java, and as such it is compiled and run using the [`javac`](https://docs.oracle.com/en/java/javase/14/docs/specs/man/javac.html) and [`java`](https://docs.oracle.com/en/java/javase/14/docs/specs/man/java.html) commands, respectively (links contain more information about the commands).

To build, navigate to the source directory, then run:
```
javac -cp CLASSPATH trading/driver/*.java trading/economy/*.java trading/net/*.java
```

Replace CLASSPATH with a list of paths to all required JAR files (More info about the java classpath can be found [here](https://stackoverflow.com/questions/2396493/what-is-a-classpath-and-how-do-i-set-it?lq=1)).

To run, stay in the source directory, and run:
```
java -cp CLASSPATH trading.driver.Main
```

This program takes no command-line arguments.

Make sure that the classpath is the same when compiling and running. Discrepancies between the compile-time and runtime classpaths can lead to mysterious runtime errors.

# Java Dependencies
This program has one dependency:

[JSON-java](https://github.com/stleary/JSON-java)

The following maven pom.xml file can be used to get the required JAR file:

```
<dependency>
    <groupId>org.json</groupId>
    <artifactId>json</artifactId>
    <version>20190722</version>
</dependency>
```
Use maven or paste the XML into [JAR-download.com](https://jar-download.com/online-maven-download-tool.php) to get the files.

# Config Files
This bot has several user options, which are read from config files. Default, non-filled-out versions of these configs are located in config/default. Before running the program for the first time, the user must fill out these config files and paste them into the config folder.

For config file specs, see config/README.md.

# Custom Functions
The user can specify four functions which determine the bot's behavior. They are:
1. sellListingPriceFunction: Calculates prices for sell listings
2. buyListingPriceFunction: Calculate prices for buy listings
3. acceptabilityFunction: Determines which items to place buy listings for (ie which ones are "acceptable")
4. listingDescriptionFunction: Determines the description to use for a listing on Backpack.tf.

Multiple default implementations for each of these functions are provided. Additionally, the user can write custom versions of these functions. See config/README.md for details.

# Bot Behavior
For an exhaustive description of the bot's behavior, see behavior.md.

# Unit Tests
This repo has JUnit 4 unit tests located in the tests folder.

# License
This software uses the [MIT license](license.txt). TL;DR You can use the software as much as you want, but I'm not liable for any value lost while trading.

In addition, I would appreciate it if any bot which uses this software links to this repository in their Steam bio.