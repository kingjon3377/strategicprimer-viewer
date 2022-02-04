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

import com.google.auto.service.AutoService;

@AutoService(TurnAppletFactory.class)
public class HuntingAppletFactory implements TurnAppletFactory {
	@Override
	public TurnApplet create(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) {
		return new HuntingApplet(model, cli, idf);
	}
}
