package view.map.details;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseListener;

import model.map.events.AbstractTownEvent;
import model.map.events.IEvent;
import model.map.events.TownStatus;
import model.viewer.FixtureTransferable;

/**
 * A Chit to represent an event. TODO: Make it look different for different
 * Events.
 *
 * @author Jonathan Lovelace
 *
 */
public class EventChit extends Chit {
	/**
	 * The margin we allow around the chit itself.
	 */
	private static final double MARGIN = 0.15;

	/**
	 * Constructor.
	 *
	 * @param event the fortress this chit represents
	 * @param listener the object listening for clicks on this chit.
	 */
	public EventChit(final IEvent event, final MouseListener listener) {
		super(listener, new FixtureTransferable(event));
		final StringBuilder builder = new StringBuilder("<html><p>");
		builder.append(event.getText());
		if (event instanceof AbstractTownEvent
				&& TownStatus.Active.equals(((AbstractTownEvent) event)
						.status())) {
			final int cursor = builder.indexOf(" (roll it up)");
			builder.replace(cursor, cursor + 13, "");
		}
		builder.append("</p></html>");
		desc = builder.toString();
	}

	/**
	 * The description of the event, HTML-ized so it'll wrap.
	 */
	private final String desc;

	/**
	 * @return A description of the event this chit represents.
	 */
	@Override
	public String describe() {
		return desc;
	}

	/**
	 * Paint the chit.
	 *
	 * @param pen the graphics context
	 */
	@Override
	public final void paint(final Graphics pen) {
		super.paint(pen);
		paintChit(pen);
	}

	/**
	 * Paint the contents of the chit. This is split out so we can override it
	 * in subclasses without losing access to the superclass's paint().
	 *
	 * @param pen the graphics context
	 */
	protected void paintChit(final Graphics pen) {
		final Color saveColor = pen.getColor();
		pen.setColor(Color.RED);
		pen.fillRoundRect(((int) Math.round(getWidth() * MARGIN)) + 1,
				((int) Math.round(getHeight() * MARGIN)) + 1,
				(int) Math.round(getWidth() * (1.0 - MARGIN * 2.0)),
				(int) Math.round(getHeight() * (1.0 - MARGIN * 2.0)),
				(int) Math.round(getWidth() * (MARGIN / 2.0)),
				(int) Math.round(getHeight() * (MARGIN / 2.0)));
		pen.setColor(saveColor);
		pen.fillRoundRect(
				((int) Math.round(getWidth() / 2.0 - getWidth() * MARGIN)) + 1,
				((int) Math.round(getHeight() / 2.0 - getHeight() * MARGIN)) + 1,
				(int) Math.round(getWidth() * MARGIN * 2.0),
				(int) Math.round(getHeight() * MARGIN * 2.0),
				(int) Math.round(getWidth() * MARGIN / 2.0),
				(int) Math.round(getHeight() * MARGIN / 2.0));
	}
}
