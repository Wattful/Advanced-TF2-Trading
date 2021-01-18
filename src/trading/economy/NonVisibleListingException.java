package trading.economy;

//TODO:

/**Checked exception thrown when attempting to perform certain operations on non-visible exceptions.
*/

public class NonVisibleListingException extends Exception{
	/**Constructs a NonVisibleListingException without a message or cause.
	*/
	public NonVisibleListingException(){
		super();
	}

	/**Constructs a NonVisibleListingExcpetion with the given message.
	@param message The message to use.
	*/
	public NonVisibleListingException(String message){
		super(message);
	}

	/**Constructs a NonVisibleListingExcpetion with the given message and cause
	@param message The message to use.
	@param cause The cause to use.
	*/
	public NonVisibleListingException(String message, Throwable cause){
		super(message, cause);
	}
}