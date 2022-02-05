package impl.xmlio.fluidxml;

import lovelace.util.MalformedXMLException;
import javax.xml.stream.XMLStreamWriter;

@FunctionalInterface
public interface FluidXMLWriter<Type> {
	// FIXME: What does this need to be declared as throwing?
	void write(XMLStreamWriter writer, Type obj, int indent) throws MalformedXMLException;

	default void writeCasting(final XMLStreamWriter writer, final Object obj, final int indent)
			throws MalformedXMLException {
		write(writer, (Type) obj, indent);
	}
}
