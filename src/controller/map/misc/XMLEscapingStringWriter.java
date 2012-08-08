package controller.map.misc;

import java.io.StringWriter;

/**
 * A StringWriter that escapes any XML-sensitive characters in its buffer
 * (double quotes and angle-brackets).
 * 
 * @author Jonathan Lovelace
 * 
 */
public class XMLEscapingStringWriter extends StringWriter {
	/**
	 * @return the escaped version of the text in the buffer
	 */
	public String asText() {
		return this.toString().replace("&", "&amp;").replace("\"", "&quot;")
				.replace("<", "&lt;").replace(">", "&gt;");
	}
}
