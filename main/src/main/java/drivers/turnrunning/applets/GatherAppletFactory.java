package drivers.turnrunning.applets;

import com.google.auto.service.AutoService;
import common.idreg.IDRegistrar;
import drivers.common.cli.ICLIHelper;
import drivers.turnrunning.ITurnRunningModel;

@AutoService(TurnAppletFactory.class)
public class GatherAppletFactory implements TurnAppletFactory {
	@Override
	public TurnApplet create(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) {
		return new GatherApplet(model, cli, idf);
	}
}
