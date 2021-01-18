package trading.economy;

//TODO:

/**Container class which stores a copy of every custom function which is needed for a TradingBot.
*/

public class FunctionSuite{
	public final HatPriceFunction hatPriceFunction;
	public final BuyListingPriceFunction buyListingPriceFunction;
	public final ListingDescriptionFunction listingDescriptionFunction;
	public final AcceptabilityFunction acceptabilityFunction;
	public final KeyScrapRatioFunction keyScrapRatioFunction;

	/**Constructs a FunctionSuite from the given functions.
	@throws NullPointerException if any parameter is null.
	*/
	public FunctionSuite(HatPriceFunction hpf, BuyListingPriceFunction blpf, ListingDescriptionFunction ldf, AcceptabilityFunction af, KeyScrapRatioFunction ksrf){
		if(hpf == null || blpf == null || ldf == null || af == null || ksrf == null){
			throw new NullPointerException();
		}
		this.hatPriceFunction = hpf;
		this.buyListingPriceFunction = blpf;
		this.listingDescriptionFunction = ldf;
		this.acceptabilityFunction = af;
		this.keyScrapRatioFunction = ksrf;
	}
}