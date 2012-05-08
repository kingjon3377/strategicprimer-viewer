package controller.map.simplexml.node;

import java.io.FileNotFoundException;

import javax.xml.stream.XMLStreamException;

import model.map.PlayerCollection;
import model.map.XMLWritable;
import util.Warning;
import controller.map.BadIncludeException;
import controller.map.MissingIncludeException;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.FileOpener;
import controller.map.simplexml.SimpleXMLReader;
/**
 * A Node for reading "include" tags---tags that include data from another file by reference.
 * @author Jonathan Lovelace
 *
 */
@Deprecated
public class IncludeNode extends AbstractChildNode<XMLWritable> {
	/**
	 * The property saying what file to include.
	 */
	private static final String FILE_PROPERTY = "file";
	/**
	 * Constructor.
	 */
	public IncludeNode() {
		super(XMLWritable.class);
	}
	/**
	 * 
	 * @param players ignored
	 * @param warner the Warning instance to use
	 * @return the data produced from the included file
	 * @throws SPFormatException on SP format error in its data.
	 */
	@Override
	public XMLWritable produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		try {
			return new SimpleXMLReader().readXML(
					new FileOpener().createReader(getProperty(FILE_PROPERTY)),
					XMLWritable.class, warner);
		} catch (FileNotFoundException e) {
			throw new MissingIncludeException(getProperty(FILE_PROPERTY), e, getLine());
		} catch (XMLStreamException e) {
			throw new BadIncludeException(getProperty(FILE_PROPERTY), e, getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether we can use it.
	 */
	@Override
	public boolean canUse(final String property) {
		return FILE_PROPERTY.equals(property);
	}
	/**
	 * Check the node for errors. Note that we don't check the included file here! (Because we can't.)
	 * @param warner the Warning instance to use
	 * @throws SPFormatException on SP format error in this tag.
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException("include", iterator().next().toString(), getLine());
		} else if (!hasProperty(FILE_PROPERTY)) {
			throw new MissingParameterException("include", FILE_PROPERTY, getLine());
		}
	}

}
