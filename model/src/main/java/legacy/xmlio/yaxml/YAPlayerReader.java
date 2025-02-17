package legacy.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.UnwantedChildException;
import legacy.idreg.IDRegistrar;
import legacy.map.Player;
import legacy.map.PlayerImpl;
import common.xmlio.Warning;

import lovelace.util.ThrowingConsumer;
import org.jetbrains.annotations.Nullable;

/**
 * A reader for {@link Player}s."
 * /* package
 */
final class YAPlayerReader extends YAAbstractReader<Player, Player> {
	public YAPlayerReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	@Override
	public Player read(final StartElement element, final @Nullable Path path, final QName parent,
	                   final Iterable<XMLEvent> stream)
			throws UnwantedChildException, MissingPropertyException {
		requireTag(element, path, parent, "player");
		expectAttributes(element, path, "number", "code_name", "portrait", "country");
		requireNonEmptyParameter(element, path, "number");
		requireNonEmptyParameter(element, path, "code_name");
		final String countryRaw = getParameter(element, "country", "");
		final String country = countryRaw.isEmpty() ? null : countryRaw;
		// We're thinking about storing "standing orders" in the XML under the <player>
		// tag; so as to not require players to upgrade to even read their maps once we
		// start doing so, we *now* only *warn* instead of *dying* if the XML contains
		// that idiom.
		spinUntilEnd(element.getName(), path, stream, "orders", "results", "science");
		final Player retval;
		if (Objects.isNull(country)) {
			retval = new PlayerImpl(getIntegerParameter(element, path, "number"),
					getParameter(element, path, "code_name"));
		} else {
			retval = new PlayerImpl(getIntegerParameter(element, path, "number"),
					getParameter(element, path, "code_name"), country);
		}
		retval.setPortrait(getParameter(element, "portrait", ""));
		return retval;
	}

	@Override
	public boolean isSupportedTag(final String tag) {
		return "player".equalsIgnoreCase(tag);
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final Player obj, final int indent)
			throws IOException {
		if (!obj.getName().isEmpty()) {
			writeTag(ostream, "player", indent);
			writeProperty(ostream, "number", obj.getPlayerId());
			writeProperty(ostream, "code_name", obj.getName());
			writeNonemptyProperty(ostream, "portrait", obj.getPortrait());
			final String country = obj.getCountry();
			if (Objects.nonNull(country)) {
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
