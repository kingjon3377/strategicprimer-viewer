package controller.map;

import java.io.IOException;
import java.io.Serializable;

import model.viewer.SPMap;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A class to read a map stored in XML.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class XMLReader implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1008526252261106761L;
	/**
	 * The XML parser that hands events to the SAXReader.
	 */
	private final transient org.xml.sax.XMLReader parser;
	/**
	 * The event-handler that constructs the objects for us.
	 */
	private final SAXReader reader = new SAXReader();

	/**
	 * 
	 * Private to prevent this class from being instantiated.
	 * 
	 * @throws SAXException
	 *             when XMLReaderFactory throws it
	 */
	public XMLReader() throws SAXException {
		parser = XMLReaderFactory.createXMLReader();
		parser.setContentHandler(reader);
	}

	/**
	 * @param filename
	 *            the filename of the XML document
	 * @return a map constructed from the XML document.
	 * @throws IOException
	 *             on I/O error, including file not found
	 * @throws SAXException
	 *             on XML parsing error
	 */
	public SPMap getMap(final String filename) throws SAXException, IOException {
		parser.parse(filename);
		return reader.getMap();
	}
}
