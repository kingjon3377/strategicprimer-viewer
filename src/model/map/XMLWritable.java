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
	// Marker interface
}
