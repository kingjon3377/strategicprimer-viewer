package model.map;

/**
 * An interface for model objects that can be written to XML. I know this is
 * model-controller mixing, but I *really* don't want to have to keep adding new
 * values to enums and new methods to the XMLWriter every time I want to extend
 * the model. So instead we let every model object take care of writing itself
 * to XML.
 * 
 * @author Jonathan Lovelace
 * 
 */
public interface XMLWritable {
	/**
	 * Write the object to XML.
	 * 
	 * @return the XML representation of the object.
	 */
	@Deprecated
	String toXML();

	/**
	 * @return The name of the file this is to be written to.
	 */
	String getFile();

	/**
	 * @param file the name of the file this, and its children unless later
	 *        specified otherwise, should be written to.
	 */
	void setFile(final String file);
}
