package drivers.turnrunning.applets;

import common.map.fixtures.IResourcePile;
import common.map.fixtures.mobile.IUnit;
import drivers.common.cli.ICLIHelper;

import common.idreg.IDRegistrar;

import drivers.resourceadding.ResourceAddingCLIHelper;

import drivers.turnrunning.ITurnRunningModel;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

/* package */ class SimpleProductApplet extends AbstractTurnApplet {
	private final String name;
	private final List<String> commands;
	private final String description;
	private final ITurnRunningModel model;
	private final ICLIHelper cli;
	private final ResourceAddingCLIHelper raHelper;

	public SimpleProductApplet(final String name, final String description, final ITurnRunningModel model, final ICLIHelper cli,
	                           final IDRegistrar idf) {
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
		final Boolean respOne =  cli.inputBooleanInSeries("Add resources to the map?", name + "resources");
		if (respOne == null) {
			return null;
		} else {
			another = respOne;
		}
		while (another) {
			final IResourcePile resource = raHelper.enterResource();
			if (resource == null) {
				break;
			}
			if (!model.addExistingResource(resource,
					Optional.ofNullable(model.getSelectedUnit()).map(IUnit::getOwner)
						.orElse(model.getMap().getCurrentPlayer()))) {
				cli.println("Failed to find a fortress to add to in any map");
			}
			final Boolean respTwo = cli.inputBoolean("Add another resource?");
			if (respTwo == null) {
				return null;
			} else {
				another = respTwo;
			}
		}
		final String addendum = cli.inputMultilineString("Description of results:");
		if (addendum == null) {
			return null;
		} else {
			return builder.toString();
		}
	}
}
