import ceylon.collection {
    LinkedList,
    Queue,
    MutableMap,
    HashMap
}
import java.lang {
	JBoolean=Boolean
}
import ceylon.logging {
    Logger,
    logger
}
import ceylon.test {
    assertEquals,
    test
}

import java.io {
    StringWriter,
    StringReader,
    FileNotFoundException,
    IOException,
    JFileReader=FileReader
}
import java.nio.file {
    NoSuchFileException
}

import javax.xml {
    XMLConstants
}
import javax.xml.namespace {
    QName
}
import javax.xml.stream {
    XMLInputFactory,
    XMLStreamException
}
import javax.xml.stream.events {
    Attribute,
    StartElement,
    EndElement,
    StartDocument,
    EndDocument,
    Namespace,
    Characters,
    XMLEvent
}

import lovelace.util.common {
    todo,
    IteratorWrapper
}
import lovelace.util.jvm {
    ConvertingIterable,
    TypesafeXMLEventReader
}

import strategicprimer.model.map {
    Player,
    TileType,
    IMutableMapNG,
    pointFactory,
    TileFixture,
    Point,
    SPMapNG,
    MapDimensionsImpl,
    PlayerCollection,
    PlayerImpl
}
import strategicprimer.model.map.fixtures {
    TextFixture
}
import strategicprimer.model.map.fixtures.resources {
    MineralVein
}
import strategicprimer.model.map.fixtures.towns {
    Town,
    TownSize,
    TownStatus,
    Fortress
}
import strategicprimer.model.xmlio {
    spNamespace,
    SPWriter,
    readMap,
    warningLevels,
    testReaderFactory
}
Logger log = logger(`module strategicprimer.model`);
object zeroToOneConverter {
	MutableMap<Integer, String> equivalents = HashMap<Integer, String>();
	void addXML(String xml, Integer* numbers) {
		for (number in numbers) {
			equivalents[number] = xml;
		}
	}
	addXML("""<mineral kind="iron" exposed="true" dc="0" />""", 200, 206);
	addXML("""<mineral kind="iron" exposed="false" dc="0" />""", 201, 202, 207, 208);
	addXML("""<mineral kind="copper" exposed="true" dc="0" />""", 203, 209);
	addXML("""<mineral kind="copper" exposed="false" dc="0" />""", 204, 205, 210, 211);
	addXML("""<mineral kind="gold" exposed="true" dc="0" />""", 212);
	addXML("""<mineral kind="gold" exposed="true" dc="0" />""", 213);
	addXML("""<mineral kind="silver" exposed="false" dc="0" />""", 214);
	addXML("""<mineral kind="silver" exposed="false" dc="0" />""", 215);
	addXML("""<mineral kind="coal" exposed="true" dc="0" />""", 216, 219);
	addXML("""<mineral kind="coal" exposed="false" dc="0" />""", 217, 218, 220, 221);
	addXML("""<town status="active" size="small" dc="0" />""", 222);
	addXML("""<town status="abandoned" size="small" dc="0" />""", 223, 227, 231);
	addXML("""<fortification status="abandoned" size="small" dc="0" />""", 224, 228, 232);
	addXML("""<town status="burned" size="small" dc="0" />""", 225, 229, 233);
	addXML("""<fortification status="burned" size="small" dc="0" />""", 226, 230, 234);
	addXML("""<battlefield dc="0" />""", *(235..240));
	addXML("""<city status="ruined" size="medium" dc="0" />""", 241, 243);
	addXML("""<fortification status="ruined" size="medium" dc="0" />""", 242, 244);
	addXML("""<city status="ruined" size="large" dc="0" />""", 245);
	addXML("""<fortification status="ruined" size="large" dc="0" />""", 246);
	addXML("""<stone kind="limestone" dc="0" />""", 247, 248, 249);
	addXML("""<stone kind="marble" dc="0" />""", 250, 251, 252);
	addXML("""<cave dc="0" />""", 253, 254, 255);
	Boolean isSpecifiedTag(QName tag, String desired) {
		return tag == QName(spNamespace, desired) || tag == QName(desired);
	}
	void printAttribute(Anything(String) ostream, Attribute attribute, QName parentTag) {
		QName name = attribute.name;
		// Based on my reading of the W3C documentation, it should only be
		// possible to have a namespace URI but no prefix when that's been
		// defined to be the default namespace in a parent tag. So we apparently
		// don't need to handle that case specially here.
		String namespace = name.prefix;
		if (!namespace.empty && namespace != parentTag.prefix) {
			ostream(" ``namespace``:");
		} else {
			ostream(" ");
		}
		ostream("``attribute.name.localPart``=\"``attribute.\ivalue``\"");
	}
	"Convert the version attribute of the map"
	void convertMap(Anything(String) ostream, StartElement element,
			{Attribute*} attributes) {
		ostream("<");
		if (XMLConstants.defaultNsPrefix != element.name.namespaceURI) {
			ostream("``element.name.prefix``:");
		}
		ostream(element.name.localPart);
		for (namespace in ConvertingIterable<Namespace>(element.namespaces)) {
			ostream(" ``namespace``");
		}
		for (attribute in attributes) {
			if ("version" == attribute.name.localPart.lowercased) {
				ostream(" version=\"1\"");
			} else {
				printAttribute(ostream, attribute, element.name);
			}
		}
		ostream(">");
	}
	void printEvent(Anything(String) ostream, Integer number) {
		if (exists val = equivalents[number]) {
			ostream(val);
		}
	}
	void printEndElement(Anything(String) ostream, EndElement element) {
		if (XMLConstants.defaultNsPrefix == element.name.namespaceURI) {
			ostream("</``element.name.localPart``>");
		} else {
			ostream("</``element.name.prefix``:``element.name.localPart``>");
		}
	}
	void printStartElement(Anything(String) ostream, StartElement element) {
		ostream("<");
		if (XMLConstants.defaultNsPrefix != element.name.namespaceURI) {
			ostream("``element.name.prefix``:");
		}
		ostream(element.name.localPart);
		for (attribute in ConvertingIterable<Attribute>(element.attributes)) {
			printAttribute(ostream, attribute, element.name);
		}
		ostream(">");
	}
	void convertTile(Anything(String) ostream, StartElement element,
			{Attribute*} attributes) {
		ostream("<");
		if (XMLConstants.defaultNsPrefix != element.name.namespaceURI) {
			ostream("``element.name.prefix``:");
		}
		ostream(element.name.localPart);
		Queue<Integer> events = LinkedList<Integer>();
		for (attribute in attributes) {
			if ("event" == attribute.name.localPart.lowercased) {
				value number = Integer.parse(attribute.\ivalue);
				if (is Integer number) {
					events.offer(number);
				} else {
					log.error("Non-numeric 'event' in line ``element.location
						.lineNumber``",
						number);
				}
			} else {
				printAttribute(ostream, attribute, element.name);
			}
		}
		ostream(">");
		while (exists event = events.accept()) {
			ostream(operatingSystem.newline);
			printEvent(ostream, event);
		}
	}
	"Read version-0 XML from the input stream and write version-1 equivalent XML to the
	 output stream."
	todo("Convert input to ceylon.io, ceylon.buffer, and/or ceylon.file")
	shared void convert({XMLEvent*} stream, Anything(String) ostream) {
		for (event in stream) {
			if (is StartElement event) {
				if (isSpecifiedTag(event.name, "tile")) {
					convertTile(ostream, event, ConvertingIterable<Attribute>(event.attributes));
				} else if (isSpecifiedTag(event.name, "map")) {
					convertMap(ostream, event, ConvertingIterable<Attribute>(event.attributes));
				} else {
					printStartElement(ostream, event);
				}
			} else if (is Characters event) {
				ostream(event.data.trimmed);
			} else if (is EndElement event) {
				printEndElement(ostream, event);
			} else if (is StartDocument event) {
				ostream("""<?xml version="1.0"?>""");
				ostream(operatingSystem.newline);
			} else if (is EndDocument event) {
				break;
			} else {
				log.warn("Unhandled element type ``event.eventType``");
			}
		}
		ostream(operatingSystem.newline);
	}
}
void initialize(IMutableMapNG map, Point point, TileType? terrain,
		TileFixture* fixtures) {
	if (exists terrain, terrain != TileType.notVisible) {
		map.baseTerrain[point] = terrain;
	}
	for (fixture in fixtures) {
		map.addFixture(point, fixture);
	}
}
test
suppressWarnings("deprecation")
void testZeroToOneConversion() {
	// FIXME: Include tile fixtures beyond those implicit in events
	String orig =
			"""<map xmlns:sp="spNamespaceXYZZY" version="0" rows="2" columns="2">
			   		<player number="0" code_name="Test Player" />
			   		<row index="0">
			   			<tile row="0" column="0" type="tundra" event="0">Random event here
			   			</tile>
			   			<tile row="0" column="1" type="boreal_forest" event="183"></tile>
			   		</row>
			   		<row index="1">
			   			<sp:tile row="1" column="0" type="mountain" event="229">
			   				<sp:fortress name="HQ" owner="0" id="15" />
			   			</sp:tile>
			   			<tile row="1" column="1" type="temperate_forest" event="219">
			   			</tile>
			   		</row>
			   	</map>""".replace("spNamespaceXYZZY", spNamespace);
	StringBuilder ostream = StringBuilder();
	XMLInputFactory xif = XMLInputFactory.newInstance();
	xif.setProperty(XMLInputFactory.supportDtd, JBoolean(false));
	zeroToOneConverter.convert(IteratorWrapper(TypesafeXMLEventReader(
		XMLInputFactory.newInstance().createXMLEventReader(StringReader(orig)))),
		ostream.append);
	StringBuilder actualXML = StringBuilder();
	SPWriter writer = testReaderFactory.oldWriter;
	writer.writeSPObject(actualXML.append,
		readMap(StringReader(ostream.string), warningLevels.ignore));
	IMutableMapNG expected = SPMapNG(MapDimensionsImpl(2, 2, 1), PlayerCollection(), 0);
	Player player = PlayerImpl(0, "Test Player");
	expected.addPlayer(player);
	initialize(expected, pointFactory(0, 0), TileType.tundra,
		TextFixture("Random event here", -1));
	initialize(expected, pointFactory(0, 1), TileType.borealForest);
	initialize(expected, pointFactory(1, 0), TileType.mountain,
		Town(TownStatus.burned, TownSize.small, 0, "", 0, PlayerImpl(-1, "Independent")),
		Fortress(player, "HQ", 15, TownSize.small));
	initialize(expected, pointFactory(1, 1), TileType.temperateForest,
		MineralVein("coal", true, 0, 1));
	StringWriter expectedXML = StringWriter();
	writer.writeSPObject((String str) => expectedXML.append(str), expected);
	assertEquals(actualXML.string, expectedXML.string,
		"Converted map's serialized form was as expected");
	assertEquals(readMap(StringReader(ostream.string),
		warningLevels.ignore), expected, "Converted map was as expected");
}
"Convert files provided on command line; prints results to standard output."
todo("Write results to file")
shared void convertZeroToOne() {
	for (argument in process.arguments) {
		try (reader = JFileReader(argument)) {
			zeroToOneConverter.convert(ConvertingIterable<XMLEvent>(
				XMLInputFactory.newInstance().createXMLEventReader(reader)),
				process.write);
		} catch (FileNotFoundException|NoSuchFileException except) {
			log.error("File ``argument`` not found", except);
		} catch (XMLStreamException except) {
			log.error("Malformed XML in ``argument``", except);
		} catch (IOException except) {
			log.error("I/O error dealing with file ``argument``", except);
		}
	}
}
