package legacy.xmlio.fluidxml;

import common.map.fixtures.mobile.MaturityModel;
import common.map.fixtures.mobile.worker.WorkerStats;
import common.xmlio.SPFormatException;
import common.xmlio.Warning;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.UnsupportedPropertyException;
import impl.xmlio.exceptions.UnsupportedTagException;
import impl.xmlio.exceptions.UnwantedChildException;
import legacy.idreg.IDRegistrar;
import legacy.map.HasImage;
import legacy.map.HasKind;
import legacy.map.ILegacyPlayerCollection;
import legacy.map.fixtures.Implement;
import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.mobile.AnimalImpl;
import legacy.map.fixtures.mobile.AnimalOrTracks;
import legacy.map.fixtures.mobile.AnimalTracks;
import legacy.map.fixtures.mobile.IWorker;
import legacy.map.fixtures.mobile.Immortal;
import legacy.map.fixtures.mobile.ImmortalAnimal;
import legacy.map.fixtures.mobile.SimpleImmortal;
import legacy.map.fixtures.mobile.Worker;
import legacy.map.fixtures.mobile.worker.IJob;
import legacy.map.fixtures.mobile.worker.IMutableJob;
import legacy.map.fixtures.mobile.worker.ISkill;
import legacy.map.fixtures.mobile.worker.Job;
import legacy.map.fixtures.mobile.worker.Skill;
import org.javatuples.Pair;
import org.jspecify.annotations.Nullable;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

import static java.util.function.Predicate.not;

/* package */ class UnitMemberHandler extends FluidBase {
	public static Worker readWorker(final StartElement element, final @Nullable Path path, final QName parent,
	                                final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                                final Warning warner, final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, path, parent, "worker");
		expectAttributes(element, path, warner, "name", "race", "portrait", "id", "image");
		final Worker retval = setImage(
				new Worker(getAttribute(element, path, "name"),
						getAttribute(element, "race", "human"),
						getOrGenerateID(element, warner, path, idFactory)),
				element, path, warner);
		retval.setPortrait(getAttribute(element, "portrait", ""));
		for (final XMLEvent event : stream) {
			switch (event) {
				case final StartElement se when isSPStartElement(event) -> {
					switch (se.getName().getLocalPart().toLowerCase()) {
						case "job" -> retval.addJob(readJob(se, path, element.getName(),
								stream, players, warner, idFactory));
						case "stats" -> retval.setStats(readStats(se, path, element.getName(),
								stream, players, warner, idFactory));
						case "note" -> retval.setNote(
								players.getPlayer(getIntegerAttribute(se, path, "player")),
								readNote(se, path, element.getName(), stream, warner));
						case "animal" -> {
							if (Objects.isNull(retval.getMount())) {
								final AnimalOrTracks animal = readAnimal(se, path, element.getName(), stream, players,
										warner, idFactory);
								if (animal instanceof final Animal a) {
									retval.setMount(a);
									break;
								}
							}
							throw new UnwantedChildException(se.getName(), element, path);
						}
						case "implement" -> retval.addEquipment(FluidResourceHandler.readImplement(se, path,
								element.getName(), stream,
								players, warner, idFactory));
						default -> throw UnwantedChildException.listingExpectedTags(element.getName(),
								se, path, "job", "stats", "note", "animal", "implement");
					}
				}
				case final EndElement ee when element.getName().equals(ee.getName()) -> {
					return retval;
				}
				default -> {
				}
			}
		}
		return retval;
	}

	private static String readNote(final StartElement element, final @Nullable Path path, final QName parent,
	                               final Iterable<XMLEvent> stream, final Warning warner)
			throws UnwantedChildException {
		requireTag(element, path, parent, "note");
		expectAttributes(element, path, warner, "player");
		final StringBuilder retval = new StringBuilder();
		for (final XMLEvent event : stream) {
			switch (event) {
				case final StartElement se when isSPStartElement(event) ->
						throw new UnwantedChildException(element.getName(), se, path);
				case final EndElement ee when element.getName().equals(ee.getName()) -> {
					return retval.toString().strip();
				}
				case final Characters c -> retval.append(c.getData());
				default -> {
				}
			}
		}
		return retval.toString().strip();
	}

	public static IJob readJob(final StartElement element, final @Nullable Path path, final QName parent,
	                           final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                           final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, path, parent, "job");
		expectAttributes(element, path, warner, "name", "level");
		final IMutableJob retval = new Job(getAttribute(element, path, "name"),
				getIntegerAttribute(element, path, "level"));
		for (final XMLEvent event : stream) {
			switch (event) {
				case final StartElement se when isSPStartElement(event) &&
						"skill".equalsIgnoreCase(se.getName().getLocalPart()) ->
						retval.addSkill(readSkill(se, path, element.getName(), stream, players, warner, idFactory));
				case final StartElement se when isSPStartElement(event) ->
						throw UnwantedChildException.listingExpectedTags(element.getName(), se, path, "skill");
				case final EndElement ee when element.getName().equals(ee.getName()) -> {
					return retval;
				}
				default -> {
				}
			}
		}
		return retval;
	}

	public static ISkill readSkill(final StartElement element, final @Nullable Path path, final QName parent,
	                               final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                               final Warning warner, final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, path, parent, "skill");
		expectAttributes(element, path, warner, "name", "level", "hours");
		requireNonEmptyAttribute(element, path, "name", true, warner);
		spinUntilEnd(element.getName(), path, stream);
		return new Skill(getAttribute(element, path, "name"),
				getIntegerAttribute(element, path, "level"),
				getIntegerAttribute(element, path, "hours"));
	}

	public static WorkerStats readStats(final StartElement element, final @Nullable Path path, final QName parent,
	                                    final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                                    final Warning warner, final IDRegistrar idFactory)
			throws UnwantedChildException, MissingPropertyException {
		requireTag(element, path, parent, "stats");
		expectAttributes(element, path, warner, "hp", "max", "str", "dex", "con", "int",
				"wis", "cha");
		spinUntilEnd(element.getName(), path, stream);
		return new WorkerStats(getIntegerAttribute(element, path, "hp"),
				getIntegerAttribute(element, path, "max"),
				getIntegerAttribute(element, path, "str"),
				getIntegerAttribute(element, path, "dex"),
				getIntegerAttribute(element, path, "con"),
				getIntegerAttribute(element, path, "int"),
				getIntegerAttribute(element, path, "wis"),
				getIntegerAttribute(element, path, "cha"));
	}

	public static void writeWorker(final XMLStreamWriter ostream, final IWorker obj, final int indentation)
			throws XMLStreamException {
		final WorkerStats stats = obj.getStats();
		final List<IJob> jobs = StreamSupport.stream(obj.spliterator(), true)
				.filter(not(IJob::isEmpty)).toList();
		final boolean hasJobs = !jobs.isEmpty();
		writeTag(ostream, "worker", indentation, !hasJobs && Objects.isNull(stats));
		writeAttributes(ostream, Pair.with("name", obj.getName()));
		if (!"human".equals(obj.getRace())) {
			writeAttributes(ostream, Pair.with("race", obj.getRace()));
		}
		writeAttributes(ostream, Pair.with("id", obj.getId()));
		writeImage(ostream, obj);
		writeNonEmptyAttributes(ostream, Pair.with("portrait", obj.getPortrait()));
		if (Objects.nonNull(stats)) {
			writeStats(ostream, stats, indentation + 1);
		}
		final Animal mount = obj.getMount();
		if (Objects.nonNull(mount)) {
			writeAnimal(ostream, mount, indentation + 1);
		}
		for (final Implement item : obj.getEquipment()) {
			FluidResourceHandler.writeImplement(ostream, item, indentation + 1);
		}
		for (final IJob job : jobs) {
			writeJob(ostream, job, indentation + 1);
		}
		for (final Integer player : obj.getNotesPlayers()) {
			writeNote(ostream, player, obj.getNote(player), indentation + 1);
		}
		if (hasJobs || Objects.nonNull(stats) || Objects.nonNull(mount) || obj.getNotesPlayers().iterator().hasNext() ||
				!obj.getEquipment().isEmpty()) {
			indent(ostream, indentation);
			ostream.writeEndElement();
		}
	}

	private static void writeNote(final XMLStreamWriter ostream, final int player, final String note,
	                              final int indentation) throws XMLStreamException {
		writeTag(ostream, "note", indentation, false);
		writeAttributes(ostream, Pair.with("player", player));
		ostream.writeCharacters(note);
		ostream.writeEndElement();
	}

	public static void writeStats(final XMLStreamWriter ostream, final WorkerStats obj, final int indentation)
			throws XMLStreamException {
		writeTag(ostream, "stats", indentation, true);
		writeAttributes(ostream, Pair.with("hp", obj.getHitPoints()),
				Pair.with("max", obj.getMaxHitPoints()),
				Pair.with("str", obj.getStrength()), Pair.with("dex", obj.getDexterity()),
				Pair.with("con", obj.getConstitution()), Pair.with("int", obj.getIntelligence()),
				Pair.with("wis", obj.getWisdom()), Pair.with("cha", obj.getCharisma()));
	}

	public static void writeJob(final XMLStreamWriter ostream, final IJob obj, final int indentation)
			throws XMLStreamException {
		final boolean hasSkills = !obj.isEmpty();
		if (obj.getLevel() <= 0 && !hasSkills) {
			return;
		}
		writeTag(ostream, "job", indentation, !hasSkills);
		writeAttributes(ostream, Pair.with("name", obj.getName()),
				Pair.with("level", obj.getLevel()));
		for (final ISkill skill : obj) {
			writeSkill(ostream, skill, indentation + 1);
		}
		if (hasSkills) {
			indent(ostream, indentation);
			ostream.writeEndElement();
		}
	}

	public static void writeSkill(final XMLStreamWriter ostream, final ISkill obj, final int indentation)
			throws XMLStreamException {
		if (!obj.isEmpty()) {
			writeTag(ostream, "skill", indentation, true);
			writeAttributes(ostream, Pair.with("name", obj.getName()),
					Pair.with("level", obj.getLevel()), Pair.with("hours", obj.getHours()));
		}
	}

	// TODO: split into Animal and Tracks methods, if at all possible
	public static AnimalOrTracks readAnimal(final StartElement element, final @Nullable Path path, final QName parent,
	                                        final Iterable<XMLEvent> stream, final ILegacyPlayerCollection players,
	                                        final Warning warner, final IDRegistrar idFactory)
			throws UnwantedChildException, MissingPropertyException {
		requireTag(element, path, parent, "animal");
		final String tag = element.getName().getLocalPart().toLowerCase();
		final String kind;
		if ("animal".equals(tag)) {
			expectAttributes(element, path, warner, "traces", "id", "count", "kind", "talking",
					"status", "wild", "born", "image");
			kind = getAttribute(element, path, "kind");
		} else {
			warner.handle(UnsupportedTagException.future(element, path));
			expectAttributes(element, path, warner, "id", "count", "image");
			kind = tag;
		}
		spinUntilEnd(element.getName(), path, stream);
		// To get the intended meaning of existing maps, we have to parse
		// traces="" as traces="true". If compatibility with existing maps
		// ever becomes unnecessary, I will change the default-value here to
		// simply `false`.
		final boolean traces = getBooleanAttribute(element, path, "traces",
				hasAttribute(element, "traces") && getAttribute(element, "traces", "").isEmpty(),
				warner);
		final boolean talking = getBooleanAttribute(element, path, "talking", false, warner);
		final String status = getAttribute(element, "status", "wild");
		final int born = getIntegerAttribute(element, "born", -1, warner);
		// TODO: We'd like the default to be 1 inside a unit and -1 outside
		final int count = getIntegerAttribute(element, "count", 1, warner);
		final int id;
		if (traces) {
			if (hasAttribute(element, "id")) {
				warner.handle(UnsupportedPropertyException.inContext(element, path, "id",
						"when tracks=\"true\""));
			}
			if (talking) {
				warner.handle(UnsupportedPropertyException.inContext(element, path, "talking",
						"when tracks=\"true\""));
			}
			if (!"wild".equals(status)) {
				warner.handle(UnsupportedPropertyException.inContext(element, path, "status",
						"when tracks=\"true\""));
			}
			if (born != -1) {
				warner.handle(UnsupportedPropertyException.inContext(element, path, "born",
						"when tracks=\"true\""));
			}
			if (count != 1) {
				warner.handle(UnsupportedPropertyException.inContext(element, path, "count",
						"when tracks=\"true\""));
			}
			return setImage(new AnimalTracks(kind), element, path, warner);
		} else {
			id = getOrGenerateID(element, warner, path, idFactory);
			return setImage(
					new AnimalImpl(kind, talking, status, id, born, count), element, path, warner);
		}
	}

	public static void writeAnimalTracks(final XMLStreamWriter ostream, final AnimalTracks obj,
	                                     final int indentation) throws XMLStreamException {
		writeTag(ostream, "animal", indentation, true);
		writeAttributes(ostream, Pair.with("kind", obj.getKind()), Pair.with("traces", true));
		writeImage(ostream, obj);
	}

	public static void writeAnimal(final XMLStreamWriter ostream, final Animal obj, final int indentation)
			throws XMLStreamException {
		writeTag(ostream, "animal", indentation, true);
		writeAttributes(ostream, Pair.with("kind", obj.getKind()));
		if (obj.isTalking()) {
			writeAttributes(ostream, Pair.with("talking", true));
		}
		if (!"wild".equals(obj.getStatus())) {
			writeAttributes(ostream, Pair.with("status", obj.getStatus()));
		}
		writeAttributes(ostream, Pair.with("id", obj.getId()));
		if (obj.getBorn() >= 0) {
			// Write turn-of-birth if and only if it is fewer turns before the current
			// turn than this kind of animal's age of maturity.
			if (MaturityModel.getCurrentTurn() < 0 ||
					!MaturityModel.getMaturityAges().containsKey(obj.getKind()) ||
					MaturityModel.getMaturityAges()
							.get(obj.getKind()) > (MaturityModel.getCurrentTurn() - obj.getBorn())) {
				writeAttributes(ostream, Pair.with("born", obj.getBorn()));
			}
		}
		if (obj.getPopulation() > 1) {
			writeAttributes(ostream, Pair.with("count", obj.getPopulation()));
		}
		writeImage(ostream, obj);
	}

	@SuppressWarnings("TypeMayBeWeakened") // This would break the method reference
	public static void writeSimpleImmortal(final XMLStreamWriter ostream, final Immortal obj, final int indentation)
			throws XMLStreamException {
		// TODO: split this method so we can get this back in the type system
		if (!(obj instanceof SimpleImmortal || obj instanceof ImmortalAnimal)) {
			throw new IllegalArgumentException("Only works with simple immortals");
		}
		writeTag(ostream, ((HasKind) obj).getKind(), indentation, true);
		writeAttributes(ostream, Pair.with("id", obj.getId()));
		writeImage(ostream, (HasImage) obj);
	}
}
