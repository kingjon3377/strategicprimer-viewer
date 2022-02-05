package drivers.turnrunning.applets;

import common.idreg.IDRegistrar;

import drivers.common.cli.ICLIHelper;

import exploration.common.HuntingModel;

import common.map.Point;

import common.map.fixtures.mobile.AnimalTracks;
import common.map.fixtures.mobile.Animal;

import drivers.turnrunning.ITurnRunningModel;

import com.google.auto.service.AutoService;

@AutoService(TurnAppletFactory.class)
public class TrappingAppletFactory implements TurnAppletFactory {
	@Override
	public TurnApplet create(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		return new TrappingApplet(model, cli, idf);
	}
}
