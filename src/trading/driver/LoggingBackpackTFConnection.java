package trading.driver;

import trading.net.*;
import trading.economy.*;
import org.json.*;
import java.io.*;

/**Subinterface of BackpackTFConnection which specifies extra functionality, including tracking thrown exceptions and whether the connection has been used.
*/

public interface LoggingBackpackTFConnection extends BackpackTFConnection{
	/**Resets this connection's used value to false.
	*/
	void resetUsed();

	/**Returns whether this connection has been used since the last call to resetUsed()
	@return whether this connection has been used.
	*/
	boolean hasBeenUsed();

	/**Resets the last thrown IOException to null.
	*/
	void resetIOException();

	/**Returns the last IOException that was thrown after the last call to resetIOException(), or null if no such IOException was thrown.
	@return the last thrown IOException.
	*/
	IOException lastThrownIOException();
}