package legacy.xmlio.yaxml;

import common.xmlio.SPFormatException;
import common.xmlio.Warning;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.UnwantedChildException;
import legacy.idreg.IDRegistrar;
import legacy.map.ILegacyPlayerCollection;
import legacy.map.fixtures.UnitMember;
import legacy.map.fixtures.mobile.IMutableUnit;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.Unit;
import lovelace.util.ThrowingConsumer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A reader for units.
 */
/* package */ class YAUnitReader extends YAAbstractReader<IUnit, IUnit> {
	public YAUnitReader(final Warning warner, final IDRegistrar idRegistrar, final ILegacyPlayerCollection players) {
		super(warner, idRegistrar);
		this.players = players;
		this.warner = warner;
		readers = List.of(new YAMobileReader(warner, idRegistrar), new YAWorkerReader(warner, idRegistrar, players),
				new YAResourcePileReader(warner, idRegistrar), new YAImplementReader(warner, idRegistrar));
	}

	private final ILegacyPlayerCollection players;
	private final Warning warner;

	private final List<YAReader<?, ?>> readers;

	/**
	 * Parse the kind of unit, from the "kind" or deprecated "type"
	 * parameter, but merely warn if neither is present.
	 */
	private String parseKind(final StartElement element) throws SPFormatException {
		try {
			final String retval = getParamWithDeprecatedForm(element, "kind", "type");
			if (retval.isEmpty()) {
				warner.handle(new MissingPropertyException(element, "kind"));
			}
			return retval;
		} catch (final MissingPropertyException except) {
			warner.handle(except);
			return "";
		}
	}

	/**
	 * Parse orders for a unit for a specified turn.
	 */
	private void parseOrders(final StartElement element, final IMutableUnit unit, final Iterable<XMLEvent> stream)
			throws SPFormatException {
		expectAttributes(element, "turn");
		final int turn = getIntegerParameter(element, "turn", -1);
		final StringBuilder builder = new StringBuilder();
		for (final XMLEvent event : stream) {
			switch (event) {
				case final Characters c -> builder.append(c.getData());
				case final StartElement se -> throw new UnwantedChildException(element.getName(), se);
				case EndElement end when isMatchingEnd(element.getName(), end) -> {
					unit.setOrders(turn, builder.toString().strip());
					return;
				}
				default -> {
				}
			}
		}
		unit.setOrders(turn, builder.toString().strip());
	}

	/**
	 * Parse results for a unit for a specified turn.
	 */
	private void parseResults(final StartElement element, final IMutableUnit unit, final Iterable<XMLEvent> stream)
			throws SPFormatException {
		expectAttributes(element, "turn");
		final int turn = getIntegerParameter(element, "turn", -1);
		final StringBuilder builder = new StringBuilder();
		for (final XMLEvent event : stream) {
			switch (event) {
				case final Characters c -> builder.append(c.getData());
				case final StartElement se -> throw new UnwantedChildException(element.getName(), se);
				case EndElement end when isMatchingEnd(element.getName(), end) -> {
					unit.setResults(turn, builder.toString().strip());
					return;
				}
				default -> {
				}
			}
		}
		unit.setResults(turn, builder.toString().strip());
	}

	private UnitMember parseChild(final StartElement element, final QName parent, final Iterable<XMLEvent> stream)
			throws SPFormatException, XMLStreamException {
		final String name = element.getName().getLocalPart().toLowerCase();
		for (final YAReader<?, ?> reader : readers) {
			if (reader.isSupportedTag(name)) {
				final Object retval;
				retval = reader.read(element, parent, stream);
				if (retval instanceof final UnitMember um) {
					return um;
				} else {
					throw new UnwantedChildException(parent, element);
				}
			}
		}
		throw new UnwantedChildException(parent, element);
	}

	@Override
	public IUnit read(final StartElement element, final QName parent, final Iterable<XMLEvent> stream)
			throws SPFormatException, XMLStreamException {
		requireTag(element, parent, "unit");
		expectAttributes(element, "name", "owner", "image", "portrait", "kind", "id", "type");
		requireNonEmptyParameter(element, "name", false);
		requireNonEmptyParameter(element, "owner", false);
		final Unit retval = new Unit(
				players.getPlayer(getIntegerParameter(element, "owner", -1)),
				parseKind(element), getParameter(element, "name", ""),
				getOrGenerateID(element));
		retval.setImage(getParameter(element, "image", ""));
		retval.setPortrait(getParameter(element, "portrait", ""));
		final StringBuilder orders = new StringBuilder();
		for (final XMLEvent event : stream) {
			switch (event) {
				case final StartElement se when isSupportedNamespace(se.getName()) -> {
					// TODO: merge with case condition?
					if ("orders".equalsIgnoreCase(se.getName().getLocalPart())) {
						parseOrders(se, retval, stream);
					} else if ("results".equalsIgnoreCase(se.getName().getLocalPart())) {
						parseResults(se, retval, stream);
					} else {
						retval.addMember(parseChild(se, element.getName(), stream));
					}
				}
				case final Characters c -> orders.append(c.getData());
				case EndElement end when isMatchingEnd(element.getName(), end) -> {
					return retval;
				}
				default -> {
				}
			}
			final String tempOrders = orders.toString().strip();
			if (!tempOrders.isEmpty()) {
				retval.setOrders(-1, tempOrders);
			}
		}
		return retval;
	}

	@Override
	public boolean isSupportedTag(final String tag) {
		return "unit".equalsIgnoreCase(tag);
	}

	public static void writeOrders(final ThrowingConsumer<String, IOException> ostream, final String tag,
	                               final int turn, final String orders, final int indent) throws IOException {
		if (orders.isEmpty()) {
			return;
		}
		writeTag(ostream, tag, indent);
		if (turn >= 0) {
			writeProperty(ostream, "turn", turn);
		}
		ostream.accept(">");
		ostream.accept(simpleQuote(orders, '<'));
		closeTag(ostream, 0, tag);
	}

	private void writeChild(final ThrowingConsumer<String, IOException> ostream, final UnitMember child,
	                        final Integer indent) throws IOException {
		for (final YAReader<?, ?> reader : readers) {
			if (reader.canWrite(child)) {
				reader.writeRaw(ostream, child, indent);
				return;
			}
		}
		throw new IllegalStateException("After checking %d readers, don't know how to write a %s".formatted(
				readers.size(), child.getClass().getName()));
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final IUnit obj, final int indent)
			throws IOException {
		writeTag(ostream, "unit", indent);
		writeProperty(ostream, "owner", obj.owner().getPlayerId());
		writeNonemptyProperty(ostream, "kind", obj.getKind());
		writeNonemptyProperty(ostream, "name", obj.getName());
		writeProperty(ostream, "id", obj.getId());
		writeImageXML(ostream, obj);
		writeNonemptyProperty(ostream, "portrait", obj.getPortrait());
		if (!obj.iterator().hasNext() && obj.getAllOrders().isEmpty() &&
				obj.getAllResults().isEmpty()) {
			closeLeafTag(ostream);
		} else {
			finishParentTag(ostream);
			for (final Map.Entry<Integer, String> entry : obj.getAllOrders().entrySet()) {
				writeOrders(ostream, "orders", entry.getKey(), entry.getValue(), indent + 1);
			}
			for (final Map.Entry<Integer, String> entry : obj.getAllResults().entrySet()) {
				writeOrders(ostream, "results", entry.getKey(), entry.getValue(), indent + 1);
			}
			for (final UnitMember member : obj) {
				writeChild(ostream, member, indent + 1);
			}
			closeTag(ostream, indent, "unit");
		}
	}

	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof IUnit;
	}
}
