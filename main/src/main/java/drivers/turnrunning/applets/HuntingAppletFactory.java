package drivers.turnrunning.applets;

import drivers.common.cli.ICLIHelper;

import common.idreg.IDRegistrar;

import drivers.turnrunning.ITurnRunningModel;

import com.google.auto.service.AutoService;

@AutoService(TurnAppletFactory.class)
public class HuntingAppletFactory implements TurnAppletFactory {
	@Override
	public TurnApplet create(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		return new HuntingApplet(model, cli, idf);
	}
}
