package trading.economy;

import java.util.*;

//TODO:

/**Enum representing an item quality. An enum constant is provided for every possible item quality in TF2.<br>
References to a quality's int value refer to its Steam API schema integer value. For more information, see https://wiki.teamfortress.com/wiki/WebAPI/GetSchema
*/

public enum Quality{
	NORMAL("Normal", 0, ""), GENUINE("Genuine", 1), VINTAGE("Vintage", 3), UNUSUAL("Unusual", 5), UNIQUE("Unique", 6, "The"), COMMUNITY("Community", 7), VALVE("Valve", 8), 
	SELF_MADE("Self-Made", 9), STRANGE("Strange", 11), HAUNTED("Haunted", 13), COLLECTORS("Collector's", 14), DECORATED("Decorated", 15, "");

	private final String name;
	private final int code;
	private final String prefix;

	private static final Map<Integer, Quality> intQualityLookup = new HashMap<>();
	private static final Map<String, Quality> stringQualityLookup = new HashMap<>();
	static{
		for(Quality q : Quality.values()){
			intQualityLookup.put(q.code, q);
			stringQualityLookup.put(q.name.toLowerCase(), q);
		}
	}

	private Quality(String name, int code){
		this(name, code, name);
	}

	private Quality(String name, int code, String prefix){
		this.name = name;
		this.code = code;
		this.prefix = prefix;
	}

	/**If the given String contains this Quality's prefix (eg "Unusual" for Unusual or "The" for Unique), 
	removes the prefix and any trailing space, and returns the result. Otherwise, returns the given String.
	@throws NullPointerException if fullName is null
	@return the described String.
	*/
	public String removePrefix(String fullName){
		if(fullName.length() > this.prefix.length() && fullName.toLowerCase().substring(0, this.prefix.length()).equals(this.prefix.toLowerCase())){
			return fullName.substring(this.prefix.length() + 1);
		} else {
			return fullName;
		}
	}

	/**Returns this Quality's name.
	@return this Quality's name.
	*/
	public String getName(){
		return this.name;
	}

	/**Returns this Quality's integer value.
	@return this Quality's integer value.
	*/
	public int getIntValue(){
		return this.code;
	}

	/**Returns a Quality corresponding to the given quality name, case insensitive.
	@throws NullPointerException if qualityName is null.
	@throws NoSuchElementException if no quality corresponds to the given String.
	@return a Quality corresponding to the given quality name.
	*/
	public static Quality forName(String qualityName){
		if(!stringQualityLookup.containsKey(qualityName.toLowerCase())){
			throw new NoSuchElementException("No Quality with name " + qualityName + " exists.");
		}
		return stringQualityLookup.get(qualityName.toLowerCase());
	}

	/**Returns a Quality corresponding to the given quality int value.
	@throws NoSuchElementException if no quality corresponds to the given int.
	@return a Quality corresponding to the given effect int value.
	*/
	public static Quality forInt(int qualityIndex){
		if(!intQualityLookup.containsKey(qualityIndex)){
			throw new NoSuchElementException("No Quality with code " + qualityIndex + " exists.");
		}
		return intQualityLookup.get(qualityIndex);
	}

	/**Returns a String representation of this Quality.
	@return a String representation of this Quality.
	*/
	@Override
	public String toString(){
		return "trading.economy.Quality: " + this.name;
	}
}