package drivers.turnrunning.applets;

import common.map.Player;
import common.map.Point;

import exploration.common.HuntingModel;

import common.map.fixtures.mobile.AnimalTracks;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.AnimalPlurals;

import drivers.common.cli.ICLIHelper;

import common.idreg.IDRegistrar;

import drivers.resourceadding.ResourceAddingCLIHelper;

import drivers.turnrunning.ITurnRunningModel;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/* package */ class FishingApplet extends HuntGeneralApplet {
	public FishingApplet(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) {
		super("try to catch and process", model, cli, idf);
	}

	@Override
	public String getDescription() {
		return "search for aquatic animals";
	}

	@Override
	public List<String> getCommands() {
		return Collections.singletonList("fish");
	}

	@Override
	@Nullable
	public String run() {
		return impl("fish", huntingModel::fish);
	}
}
