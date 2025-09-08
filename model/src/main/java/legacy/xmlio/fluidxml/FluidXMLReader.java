package legacy.xmlio.fluidxml;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import javax.xml.namespace.QName;

import common.xmlio.Warning;

import legacy.idreg.IDRegistrar;
import common.xmlio.SPFormatException;
import legacy.map.IMutableLegacyPlayerCollection;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

@FunctionalInterface
public interface FluidXMLReader<Return> {
	// FIXME: What does this need to be declared as throwing?
	Return read(StartElement element, @Nullable Path path, QName parent, Iterable<XMLEvent> stream,
	            IMutableLegacyPlayerCollection players, Warning warner, IDRegistrar factory)
			throws SPFormatException;
}
