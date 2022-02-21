package impl.xmlio;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import common.xmlio.Warning;

import common.xmlio.SPFormatException;
import lovelace.util.MalformedXMLException;


/**
 * An interface for readers of any SP model type.
 */
public interface ISPReader {
	/**
	 * Tags we expect to use in the future; they are skipped for now and
	 * we'll warn if they're used.
	 */
	List<String> FUTURE_TAGS =
			List.of("future", "explorer", "building", "resource", "changeset", "change", "move", "work", "discover", "submap", "futuretag", "futureTag", "science");

	/**
	 * The namespace that SP XML will use.
	 */
	String SP_NAMESPACE =
		"https://github.com/kingjon3377/strategicprimer-viewer";

	/**
	 * Read an object from XML.
	 *
	 * @param file The name of the file being read from
	 * @param istream The reader from which to read the XML
	 * @param warner "The Warning instance to use for warnings
	 */
	<Element> Element readXML(Path file, Reader istream, Warning warner)
		throws SPFormatException, MalformedXMLException, IOException;
}
