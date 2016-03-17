package util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.AbstractAction;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A wrapper around an ActionListener that extends AbstractAction, for the exceedingly
 * common case of a JDK method requiring an Action when we don't need more than
 * ActionListener functionality.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class ActionWrapper extends AbstractAction {
	/**
	 * The ActionListener we wrap.
	 */
	private final ActionListener wrapped;
	/**
	 * Constructor.
	 * @param alist the listener to wrap
	 */
	public ActionWrapper(final ActionListener alist) {
		wrapped = alist;
	}

	/**
	 * Handle an event by passing it to the wrapped listener.
	 * @param evt the event to handle
	 */
	@Override
	public final void actionPerformed(@Nullable final ActionEvent evt) {
		wrapped.actionPerformed(evt);
	}

	/**
	 * Prevent serialization.
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent deserialization.
	 * @param in ignored
	 * @throws IOException always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent cloning.
	 * @return nothing
	 * @throws CloneNotSupportedException always
	 */
	@Override
	public final ActionWrapper clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Cloning is not allowed.");
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ActionWrapper around " + wrapped.toString();
	}
}
