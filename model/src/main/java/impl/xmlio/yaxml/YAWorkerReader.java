package impl.xmlio.yaxml;

import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;

import static impl.xmlio.SPWriter.IOConsumer;
import common.xmlio.SPFormatException;
import common.idreg.IDRegistrar;
import common.map.IPlayerCollection;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.Worker;
import common.map.fixtures.mobile.worker.WorkerStats;
import common.map.fixtures.mobile.worker.Job;
import common.map.fixtures.mobile.worker.IMutableJob;
import common.map.fixtures.mobile.worker.IJob;
import common.map.fixtures.mobile.worker.Skill;
import common.map.fixtures.mobile.worker.ISkill;
import common.xmlio.Warning;
import impl.xmlio.exceptions.UnwantedChildException;
import common.map.Player;

/**
 * A reader for workers.
 */
/* package */ class YAWorkerReader extends YAAbstractReader<IWorker, IWorker> {
	public static void writeSkill(IOConsumer<String> ostream, ISkill obj, int indent) 
			throws IOException {
		if (!obj.isEmpty()) {
			writeTag(ostream, "skill", indent);
			writeProperty(ostream, "name", obj.getName());
			writeProperty(ostream, "level", obj.getLevel());
			writeProperty(ostream, "hours", obj.getHours());
			closeLeafTag(ostream);
		}
	}

	public static void writeJob(IOConsumer<String> ostream, IJob obj, int indent) 
			throws IOException {
		if (obj.getLevel() <= 0 && obj.isEmpty()) {
			return;
		}
		writeTag(ostream, "job", indent);
		writeProperty(ostream, "name", obj.getName());
		writeProperty(ostream, "level", obj.getLevel());
		if (obj.isEmpty()) {
			closeLeafTag(ostream);
		} else {
			finishParentTag(ostream);
			for (ISkill skill : obj) {
				writeSkill(ostream, skill, indent + 1);
			}
			closeTag(ostream, indent, "job");
		}
	}

	private static void writeNote(IOConsumer<String> ostream, int player, String note, int indent) 
			throws IOException {
		writeTag(ostream, "note", indent);
		writeProperty(ostream, "player", player);
		ostream.accept(">"); // We don't use finishParentTag() because we don't want a newline yet
		ostream.accept(note);
		closeTag(ostream, 0, "note");
	}

	private Warning warner;
	private IPlayerCollection players;
	public YAWorkerReader (Warning warning, IDRegistrar idRegistrar, IPlayerCollection players) {
		super(warning, idRegistrar);
		warner = warning;
		this.players = players;
	}

	@FunctionalInterface
	private static interface ReadToIntFunction<Type> {
		int apply(Type item) throws SPFormatException;
	}

	private WorkerStats parseStats(StartElement element, QName parent, Iterable<XMLEvent> stream) 
			throws SPFormatException {
		requireTag(element, parent, "stats");
		expectAttributes(element, "hp", "max", "str", "dex", "con", "int", "wis", "cha");
		ReadToIntFunction<String> inner = attr -> getIntegerParameter(element, attr);
		WorkerStats retval = new WorkerStats(inner.apply("hp"), inner.apply("max"),
			inner.apply("str"), inner.apply("dex"), inner.apply("con"), inner.apply("int"),
			inner.apply("wis"), inner.apply("cha"));
		spinUntilEnd(element.getName(), stream);
		return retval;
	}

	private ISkill parseSkill(StartElement element, QName parent) throws SPFormatException {
		requireTag(element, parent, "skill");
		expectAttributes(element, "name", "level", "hours");
		// TODO: Should require no children, right? So spinUntilEnd() here, not in the caller?
		return new Skill(getParameter(element, "name"), getIntegerParameter(element, "level"),
			getIntegerParameter(element, "hours"));
	}

	private String readNote(StartElement element, QName parent, Iterable<XMLEvent> stream) 
			throws SPFormatException {
		requireTag(element, parent, "note");
		expectAttributes(element, "player");
		StringBuilder retval = new StringBuilder();
		for (XMLEvent event : stream) {
			if (event instanceof StartElement &&
					isSupportedNamespace(((StartElement) event).getName())) {
				throw new UnwantedChildException(element.getName(), (StartElement) event);
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			} else if (event instanceof Characters) {
				retval.append(((Characters) event).getData());
			}
		}
		return retval.toString().trim();
	}

	private IJob parseJob(StartElement element, QName parent, Iterable<XMLEvent> stream) 
			throws SPFormatException {
		requireTag(element, parent, "job");
		expectAttributes(element, "name", "level");
		IMutableJob retval = new Job(getParameter(element, "name"),
			getIntegerParameter(element, "level"));
		for (XMLEvent event : stream) {
			if (event instanceof StartElement &&
					isSupportedNamespace(((StartElement) event).getName())) {
				if ("skill".equalsIgnoreCase(((StartElement) event)
						.getName().getLocalPart())) {
					retval.addSkill(parseSkill((StartElement) event,
						element.getName()));
					spinUntilEnd(((StartElement) event).getName(), stream);
				} else {
					throw UnwantedChildException.listingExpectedTags(element.getName(),
						(StartElement) event, "skill");
				}
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			}
		}
		return retval;
	}

	private void writeStats(IOConsumer<String> ostream, @Nullable WorkerStats stats, int indent) 
			throws IOException {
		if (stats != null) {
			writeTag(ostream, "stats", indent);
			writeProperty(ostream, "hp", stats.getHitPoints());
			writeProperty(ostream, "max", stats.getMaxHitPoints());
			writeProperty(ostream, "str", stats.getStrength());
			writeProperty(ostream, "dex", stats.getDexterity());
			writeProperty(ostream, "con", stats.getConstitution());
			writeProperty(ostream, "int", stats.getIntelligence());
			writeProperty(ostream, "wis", stats.getWisdom());
			writeProperty(ostream, "cha", stats.getCharisma());
			closeLeafTag(ostream);
		}
	}

	@Override
	public IWorker read(StartElement element, QName parent, Iterable<XMLEvent> stream) 
			throws SPFormatException {
		requireTag(element, parent, "worker");
		expectAttributes(element, "name", "race", "image", "portrait", "id");
		Worker retval = new Worker(getParameter(element, "name"),
			getParameter(element, "race", "human"), getOrGenerateID(element));
		retval.setImage(getParameter(element, "image", ""));
		retval.setPortrait(getParameter(element, "portrait", ""));
		for (XMLEvent event : stream) {
			if (event instanceof StartElement &&
					isSupportedNamespace(((StartElement) event).getName())) {
				if ("job".equalsIgnoreCase(((StartElement) event).getName().getLocalPart())) {
					retval.addJob(parseJob((StartElement) event, element.getName(),
						stream));
				} else if ("stats".equalsIgnoreCase(((StartElement) event)
						.getName().getLocalPart())) {
					retval.setStats(parseStats((StartElement) event,
						element.getName(), stream));
				} else if ("note".equalsIgnoreCase(((StartElement) event)
						.getName().getLocalPart())) {
					retval.setNote(
						players.getPlayer(getIntegerParameter((StartElement) event,
							"player")),
						readNote((StartElement) event, element.getName(), stream));
				} else {
					throw UnwantedChildException.listingExpectedTags(element.getName(),
						(StartElement) event, "job", "stats");
				}
			} else if (isMatchingEnd(element.getName(), event)) {
				break;
			}
		}
		return retval;
	}

	@Override
	public void write(IOConsumer<String> ostream, IWorker obj, int indent) throws IOException {
		writeTag(ostream, "worker", indent);
		writeProperty(ostream, "name", obj.getName());
		if (!"human".equals(obj.getRace())) {
			writeProperty(ostream, "race", obj.getRace());
		}
		writeProperty(ostream, "id", obj.getId());
		writeImageXML(ostream, obj);
		writeNonemptyProperty(ostream, "portrait", obj.getPortrait());
		if (obj.iterator().hasNext() || obj.getStats() != null ||
				obj.getNotesPlayers().iterator().hasNext()) {
			finishParentTag(ostream);
			writeStats(ostream, obj.getStats(), indent + 1);
			for (IJob job : obj) {
				writeJob(ostream, job, indent + 1);
			}
			for (Integer player : obj.getNotesPlayers()) {
				writeNote(ostream, player, obj.getNote(player), indent + 1);
			}
			closeTag(ostream, indent, "worker");
		} else {
			closeLeafTag(ostream);
		}
	}

	@Override
	public boolean isSupportedTag(String tag) {
		return "worker".equalsIgnoreCase(tag);
	}

	@Override
	public boolean canWrite(Object obj) {
		return obj instanceof IWorker;
	}
}
