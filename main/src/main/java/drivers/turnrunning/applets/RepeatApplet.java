package drivers.turnrunning.applets;

import legacy.idreg.IDRegistrar;
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
		appletChooser = new AppletChooser<>(cli,
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
	public @Nullable String run() {
		final StringBuilder buffer = new StringBuilder();
		while (true) {
			final Either<TurnApplet, Boolean> command = appletChooser.chooseApplet();
			if (command == null) {
				return null;
			}
			final Boolean bool = command.fromRight().orElse(null);
			final TurnApplet applet = command.fromLeft().orElse(null);
			if (Boolean.FALSE.equals(bool)) {
				return null;
			} else if (applet != null) {
				final String results = applet.run();
				if (results == null) {
					return null;
				}
				buffer.append(results);
			} else {
				continue;
			}
			final Boolean cont = cli.inputBoolean("Create more results for this unit?");
			if (cont == null) {
				return null;
			} else if (!cont) {
				break;
			}
		}
		return buffer.toString();
	}
}
