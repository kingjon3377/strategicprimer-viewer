package drivers.turnrunning.applets;

import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.mobile.IUnit;
import drivers.common.cli.ICLIHelper;

import legacy.idreg.IDRegistrar;

import drivers.resourceadding.ResourceAddingCLIHelper;

import drivers.turnrunning.ITurnRunningModel;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

/* package */ final class SimpleProductApplet extends AbstractTurnApplet {
	private final String name;
	private final List<String> commands;
	private final String description;
	private final ITurnRunningModel model;
	private final ICLIHelper cli;
	private final ResourceAddingCLIHelper raHelper;

	public SimpleProductApplet(final String name, final String description, final ITurnRunningModel model,
	                           final ICLIHelper cli, final IDRegistrar idf) {
		super(model, cli);
		this.name = name;
		commands = Collections.singletonList(name);
		this.description = description;
		this.model = model;
		this.cli = cli;
		raHelper = new ResourceAddingCLIHelper(cli, idf);
	}

	@Override
	public List<String> getCommands() {
		return commands;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public @Nullable String run() {
		final StringBuilder builder = new StringBuilder();
		boolean another;
		switch (cli.inputBooleanInSeries("Add resources to the map?", name + "resources")) {
			case YES -> {
				another = true;
			}
			case NO -> {
				another = false;
			}
			case QUIT -> {
				return null;
			}
			case EOF -> { // TODO: Somehow signal EOF to callers
				return null;
			}
			default -> throw new IllegalStateException("Exhaustive switch wasn't");
		}
		while (another) {
			final IResourcePile resource = raHelper.enterResource();
			if (Objects.isNull(resource)) {
				break;
			}
			if (model.addExistingResource(resource,
					Optional.ofNullable(model.getSelectedUnit()).map(IUnit::owner)
							.orElse(model.getMap().getCurrentPlayer()))) {
				builder.append("Added ").append(resource).append(". ");
			} else {
				cli.println("Failed to find a fortress to add to in any map");
			}
			switch (cli.inputBoolean("Add another resource?")) {
				case YES -> {
					another = true;
				}
				case NO -> {
					another = false;
				}
				case QUIT -> {
					return builder.toString();
				}
				case EOF -> { // TODO: Somehow signal EOF to callers
					return null;
				}
				default -> throw new IllegalStateException("Exhaustive switch wasn't");
			}
		}
		final String addendum = cli.inputMultilineString("Description of results:");
		if (Objects.isNull(addendum)) {
			return null;
		} else {
			builder.append(addendum);
			return builder.toString();
		}
	}
}
