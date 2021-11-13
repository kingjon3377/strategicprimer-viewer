package impl.xmlio.fluidxml;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import javax.xml.namespace.QName;

import common.map.IMutablePlayerCollection;

import common.xmlio.Warning;

import common.idreg.IDRegistrar;
import common.xmlio.SPFormatException;

@FunctionalInterface
public interface FluidXMLReader<Return> {
	// FIXME: What does this need to be declared as throwing?
	Return read(StartElement element, QName parent, Iterable<XMLEvent> stream,
		IMutablePlayerCollection players, Warning warner, IDRegistrar factory)
		throws SPFormatException;
}
