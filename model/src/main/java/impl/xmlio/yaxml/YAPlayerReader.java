package impl.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;

import common.xmlio.SPFormatException;
import common.idreg.IDRegistrar;
import common.map.Player;
import common.map.PlayerImpl;
import common.xmlio.Warning;

import lovelace.util.IOConsumer;

/**
 * A reader for {@link Player}s."
/* package */ class YAPlayerReader extends YAAbstractReader<Player, Player> {
	public YAPlayerReader(Warning warning, IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	@Override
	public Player read(StartElement element, QName parent, Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, parent, "player");
		expectAttributes(element, "number", "code_name", "portrait", "country");
		requireNonEmptyParameter(element, "number", true);
		requireNonEmptyParameter(element, "code_name", true);
		String countryRaw = getParameter(element, "country", "");
		String country = countryRaw.isEmpty() ? null : countryRaw;
		// We're thinking about storing "standing orders" in the XML under the <player>
		// tag; so as to not require players to upgrade to even read their maps once we
		// start doing so, we *now* only *warn* instead of *dying* if the XML contains
		// that idiom.
		spinUntilEnd(element.getName(), stream, "orders", "results", "science");
		Player retval = new PlayerImpl(getIntegerParameter(element, "number"),
			getParameter(element, "code_name"), country);
		retval.setPortrait(getParameter(element, "portrait", ""));
		return retval;
	}

	@Override
	public boolean isSupportedTag(String tag) {
		return "player".equalsIgnoreCase(tag);
	}

	@Override
	public void write(IOConsumer<String> ostream, Player obj, int indent) throws IOException {
		if (!obj.getName().isEmpty()) {
			writeTag(ostream, "player", indent);
			writeProperty(ostream, "number", obj.getPlayerId());
			writeProperty(ostream, "code_name", obj.getName());
			writeNonemptyProperty(ostream, "portrait", obj.getPortrait());
			String country = obj.getCountry();
			if (country != null) {
				writeNonemptyProperty(ostream, "country", country);
			}
			closeLeafTag(ostream);
		}
	}

	@Override
	public boolean canWrite(Object obj) {
		return obj instanceof Player;
	}
}
