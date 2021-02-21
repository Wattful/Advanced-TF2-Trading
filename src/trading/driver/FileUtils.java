package trading.driver;

import java.io.*;
import java.nio.file.*;

/**Class containing various utility methods related to reading and writing files.
*/

public class FileUtils{
	/**Reads the file located at the given path and returns its contents.
	@param path path to the file
	@throws NullPointerException if path is null
	@throws IOException if an IO error occurs
	@return the contents of the file
	*/
	public static String readFile(String path) throws IOException {
  		return new String(Files.readAllBytes(Paths.get(path)));
	}

	/**Writes the given content to the given path, creating the file if it does not exist.
	@param content content to write to the file
	@param path path to the file
	@throws NullPointerException if path is null
	@throws IOException if an IO error occurs
	*/
	public static void write(String content, String path) throws IOException {
		ensurePathExists(path);
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(path))){
	    	writer.write(content);	
		}
	}

	/**Ensures that the given path exists, creating it if necessary.
	@param path the path.
	@throws NullPointerException if path is null
	@throws IOException if an IO error occurs
	*/
	public static void ensurePathExists(String path) throws IOException {
		File f = new File(path).getParentFile();
		if(f != null){
			f.mkdirs();
		}
	}

}