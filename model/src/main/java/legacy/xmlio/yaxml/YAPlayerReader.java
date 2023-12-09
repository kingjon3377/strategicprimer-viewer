package legacy.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;

import common.xmlio.SPFormatException;
import legacy.idreg.IDRegistrar;
import common.map.Player;
import common.map.PlayerImpl;
import common.xmlio.Warning;

import lovelace.util.ThrowingConsumer;

/**
 * A reader for {@link Player}s."
 /* package */
class YAPlayerReader extends YAAbstractReader<Player, Player> {
    public YAPlayerReader(final Warning warning, final IDRegistrar idRegistrar) {
        super(warning, idRegistrar);
    }

    @Override
    public Player read(final StartElement element, final QName parent, final Iterable<XMLEvent> stream)
            throws SPFormatException, XMLStreamException {
        requireTag(element, parent, "player");
        expectAttributes(element, "number", "code_name", "portrait", "country");
        requireNonEmptyParameter(element, "number", true);
        requireNonEmptyParameter(element, "code_name", true);
        final String countryRaw = getParameter(element, "country", "");
        final String country = countryRaw.isEmpty() ? null : countryRaw;
        // We're thinking about storing "standing orders" in the XML under the <player>
        // tag; so as to not require players to upgrade to even read their maps once we
        // start doing so, we *now* only *warn* instead of *dying* if the XML contains
        // that idiom.
        spinUntilEnd(element.getName(), stream, "orders", "results", "science");
        final Player retval;
        if (country == null) {
            retval = new PlayerImpl(getIntegerParameter(element, "number"),
                    getParameter(element, "code_name"));
        } else {
            retval = new PlayerImpl(getIntegerParameter(element, "number"),
                    getParameter(element, "code_name"), country);
        }
        retval.setPortrait(getParameter(element, "portrait", ""));
        return retval;
    }

    @Override
    public boolean isSupportedTag(final String tag) {
        return "player".equalsIgnoreCase(tag);
    }

    @Override
    public void write(final ThrowingConsumer<String, IOException> ostream, final Player obj, final int indent) throws IOException {
        if (!obj.getName().isEmpty()) {
            writeTag(ostream, "player", indent);
            writeProperty(ostream, "number", obj.getPlayerId());
            writeProperty(ostream, "code_name", obj.getName());
            writeNonemptyProperty(ostream, "portrait", obj.getPortrait());
            final String country = obj.getCountry();
            if (country != null) {
                writeNonemptyProperty(ostream, "country", country);
            }
            closeLeafTag(ostream);
        }
    }

    @Override
    public boolean canWrite(final Object obj) {
        return obj instanceof Player;
    }
}
