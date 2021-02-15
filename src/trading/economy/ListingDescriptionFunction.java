package trading.economy;

import java.util.*;

//TODO:

/**Function which generates a description for a listing.
*/

@FunctionalInterface
public interface ListingDescriptionFunction{
	/**Generates a description for the given listing.<br>
	The output of this function does not need to be consistent for different calls with the same listing.
	@param listing The listing to generate a decription for.
	@throws NonVisibleListingException if the given listing is non-visible.
	@throws NullPointerExcpetion if listing is null.
	*/
	String generateDescription(Listing listing) throws NonVisibleListingException;

	/**Returns a ListingDescriptionFunction which returns a simple description 
	which states the price of the item and states that the offer will be accepted instantly.<br>
	More precisely, the description will take the following form:<br>
	"[Buying/Selling] this hat for X keys and X refined. Send me an offer, I will accept instantly! Item offers held for manual review."<br>
	Note that "and X refined" will be omitted if the listing's Price has 0 refined.
	@return the described ListingDescriptionFunction
	*/
	public static ListingDescriptionFunction simpleDescription(){
		return (Listing l) -> {
			return generateSimpleDescription(l);
		};
	}

	/**Returns a ListingDescriptionFunction which returns the exact same simple description described in the simpleDescription function, 
	with a saying placed before or after the simple description.<br>
	The saying will be randomly selected each time from the provided list of Strings.<br> 
	It will be placed before the simple description if placeBefore is true, otherwise it will be placed after the simple description.
	@param placeBefore whether to place the saying before or after the simple description.
	@param sayings the sayings to use.
	@throws NullPointerException if sayings is null or any saying is null.
	@throws IllegalArgumentException if sayings is empty
	@return the described ListingDescriptionFunction.
	*/
	public static ListingDescriptionFunction descriptionWithSayings(boolean placeBefore, String... sayings){
		if(sayings == null){
			throw new NullPointerException();
		}
		if(sayings.length == 0){
			throw new IllegalArgumentException("Sayings was empty");
		}
		for(String s : sayings){
			if(s == null){
				throw new NullPointerException();
			}
		}
		Random rand = new Random();
		return (Listing l) -> {
			String saying = sayings[rand.nextInt(sayings.length)];
			String simpleDescription = generateSimpleDescription(l);
			if(placeBefore){
				return saying + " " + simpleDescription;
			} else {
				return simpleDescription + " " + saying;
			}
		};
	}

	//Generates the simple description
	private static String generateSimpleDescription(Listing l) throws NonVisibleListingException {
		StringBuilder description = new StringBuilder();
		if(l instanceof Hat){
			description.append("Selling this hat for ");
		} else if(l instanceof BuyListing){
			description.append("Buying this hat for ");
		}
		/*switch(l){ //This block only works with preview features on
			case Hat h -> description.append("Selling this hat for ");
			case BuyListing bl -> description.append("Buying this hat for ");
		}*/
		Price myPrice = l.getPrice();
		if(myPrice.getRefined() == 0){
			description.append(myPrice.getKeys() + " keys.");
		} else {
			description.append(myPrice.getKeys() + " keys and " + myPrice.getRefined() + " refined.");
		}
		description.append(" Send me an offer, I will accept instantly! Item offers held for manual review.");
		return description.toString();
	}
}