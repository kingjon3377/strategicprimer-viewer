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

/* package */ class HuntingApplet extends HuntGeneralApplet {
	public HuntingApplet(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		super("fight and process", model, cli, idf);
	}

	@Override
	public String getDescription() {
		return "search for wild animals";
	}

	@Override
	public List<String> getCommands() {
		return Collections.singletonList("hunt");
	}

	@Override
	@Nullable
	public String run() {
		return impl("hunt", huntingModel::hunt);
	}
}
