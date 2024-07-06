package legacy.xmlio.fluidxml;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import javax.xml.namespace.QName;

import common.xmlio.Warning;

import legacy.idreg.IDRegistrar;
import common.xmlio.SPFormatException;
import legacy.map.IMutableLegacyPlayerCollection;

@FunctionalInterface
public interface FluidXMLReader<Return> {
	// FIXME: What does this need to be declared as throwing?
	Return read(StartElement element, QName parent, Iterable<XMLEvent> stream,
	            IMutableLegacyPlayerCollection players, Warning warner, IDRegistrar factory)
			throws SPFormatException;
}
