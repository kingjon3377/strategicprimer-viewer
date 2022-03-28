package impl.xmlio.fluidxml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

@FunctionalInterface
public interface FluidXMLWriter<Type> {
	// FIXME: What does this need to be declared as throwing?
	void write(XMLStreamWriter writer, Type obj, int indent) throws XMLStreamException;

	default void writeCasting(final XMLStreamWriter writer, final Object obj, final int indent)
			throws XMLStreamException {
		write(writer, (Type) obj, indent);
	}
}
