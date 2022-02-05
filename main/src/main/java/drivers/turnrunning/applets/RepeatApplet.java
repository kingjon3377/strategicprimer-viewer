package drivers.turnrunning.applets;

import common.idreg.IDRegistrar;
import drivers.common.cli.AppletChooser;
import drivers.common.cli.ICLIHelper;
import drivers.turnrunning.ITurnRunningModel;
import either.Either;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.Nullable;

/* package */class RepeatApplet implements TurnApplet {
	public RepeatApplet(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		this.model = model;
		this.cli = cli;
		this.idf = idf;
		appletChooser = new AppletChooser<TurnApplet>(cli,
			StreamSupport.stream(ServiceLoader.load(TurnAppletFactory.class).spliterator(), false)
				.filter(RepeatApplet::isNotRepeat).map(this::getApplet).toArray(TurnApplet[]::new));
	}

	private final ITurnRunningModel model;
	private final ICLIHelper cli;
	private final IDRegistrar idf;

	private static final List<String> COMMANDS = Collections.singletonList("repeat");

	@Override
	public List<String> getCommands() {
		return COMMANDS;
	}

	@Override
	public String getDescription() {
		return "Run multiple commands for a single unit";
	}

	private static boolean isNotRepeat(final TurnAppletFactory factory) {
		return !(factory instanceof RepeatAppletFactory);
	}

	private TurnApplet getApplet(final TurnAppletFactory factory) {
		return factory.create(model, cli, idf);
	}

	private final AppletChooser<TurnApplet> appletChooser;

	@Override
	@Nullable
	public String run() {
		final StringBuilder buffer = new StringBuilder();
		while (true) {
			Either<TurnApplet, Boolean> command = appletChooser.chooseApplet();
			Boolean bool = command.fromRight().orElse(null);
			TurnApplet applet = command.fromLeft().orElse(null);
			if (bool != null && !bool) {
				return null;
			} else if (applet != null) {
				String results = applet.run();
				if (results == null) {
					return null;
				}
				buffer.append(results);
			} else {
				continue;
			}
			Boolean cont = cli.inputBoolean("Create more results for this unit?");
			if (cont == null) {
				return null;
			} else if (!cont) {
				break;
			}
		}
		return buffer.toString();
	}
}
