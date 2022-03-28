package impl.xmlio.yaxml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Characters;
import java.io.IOException;

import lovelace.util.ThrowingConsumer;
import common.xmlio.SPFormatException;
import common.idreg.IDRegistrar;
import common.map.IPlayerCollection;
import common.map.fixtures.UnitMember;
import common.map.fixtures.mobile.IMutableUnit;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Unit;
import common.xmlio.Warning;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.UnwantedChildException;
import java.util.Map;
import java.util.List;

/**
 * A reader for units.
 */
/* package */ class YAUnitReader extends YAAbstractReader<IUnit, IUnit> {
	public YAUnitReader(final Warning warner, final IDRegistrar idRegistrar, final IPlayerCollection players) {
		super(warner, idRegistrar);
		this.players = players;
		this.warner = warner;
		readers = List.of(new YAMobileReader(warner, idRegistrar), new YAWorkerReader(warner, idRegistrar, players), new YAResourcePileReader(warner, idRegistrar), new YAImplementReader(warner, idRegistrar));
	}

	private final IPlayerCollection players;
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
			if (event instanceof Characters) {
				builder.append(((Characters) event).getData());
			} else if (event instanceof StartElement) {
				throw new UnwantedChildException(element.getName(), (StartElement) event);
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			}
		}
		unit.setOrders(turn, builder.toString().trim());
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
			if (event instanceof Characters) {
				builder.append(((Characters) event).getData());
			} else if (event instanceof StartElement) {
				throw new UnwantedChildException(element.getName(), (StartElement) event);
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			}
		}
		unit.setResults(turn, builder.toString().trim());
	}

	private UnitMember parseChild(final StartElement element, final QName parent, final Iterable<XMLEvent> stream)
			throws SPFormatException, XMLStreamException {
		final String name = element.getName().getLocalPart().toLowerCase();
		for (final YAReader<?, ?> reader : readers) {
			if (reader.isSupportedTag(name)) {
				final Object retval;
				retval = reader.read(element, parent, stream);
				if (retval instanceof UnitMember) {
					return (UnitMember) retval;
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
			if (event instanceof StartElement &&
					isSupportedNamespace(((StartElement) event).getName())) {
				if ("orders".equalsIgnoreCase(((StartElement) event)
						.getName().getLocalPart())) {
					parseOrders((StartElement) event, retval, stream);
				} else if ("results".equalsIgnoreCase(((StartElement) event)
						.getName().getLocalPart())) {
					parseResults((StartElement) event, retval, stream);
				} else {
					retval.addMember(parseChild((StartElement) event,
						element.getName(), stream));
				}
			} else if (event instanceof Characters) {
				orders.append(((Characters) event).getData());
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			}
			final String tempOrders = orders.toString().trim();
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

	public static void writeOrders(final ThrowingConsumer<String, IOException> ostream, final String tag, final int turn, final String orders,
	                               final int indent) throws IOException {
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

	private void writeChild(final ThrowingConsumer<String, IOException> ostream, final UnitMember child, final Integer indent)
			throws IOException {
		for (final YAReader<?, ?> reader : readers) {
			if (reader.canWrite(child)) {
				reader.writeRaw(ostream, child, indent);
				return;
			}
		}
		throw new IllegalStateException(String.format(
			"After checking %d readers, don't know how to write a %s",
			readers.size(), child.getClass().getName()));
	}

	@Override
	public void write(final ThrowingConsumer<String, IOException> ostream, final IUnit obj, final int indent) throws IOException {
		writeTag(ostream, "unit", indent);
		writeProperty(ostream, "owner", obj.getOwner().getPlayerId());
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
