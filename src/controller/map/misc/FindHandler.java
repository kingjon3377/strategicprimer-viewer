package controller.map.misc;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.viewer.IViewerModel;
import org.eclipse.jdt.annotation.Nullable;
import view.map.main.FindDialog;

/**
 * A class to respond to "find" and "find next" menu items.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class FindHandler implements ActionListener {
	/**
	 * The "find" dialog, if this is for a map viewer.
	 */
	@Nullable
	private FindDialog finder = null;
	/**
	 * The main window.
	 */
	private final Frame parent;
	/**
	 * The driver model.
	 */
	private final IViewerModel model;
	/**
	 * Constructor.
	 * @param mainWindow the main window
	 * @param dmodel the driver model
	 */
	public FindHandler(final Frame mainWindow, final IViewerModel dmodel) {
		model = dmodel;
		parent = mainWindow;
	}
	/**
	 * Handle a menu item.
	 *
	 * @param evt the event to handle
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public void actionPerformed(final ActionEvent evt) {
		if ("find a fixture".equalsIgnoreCase(evt.getActionCommand())) {
			getFindDialog().setVisible(true);
		} else if ("find next".equalsIgnoreCase(evt.getActionCommand())) {
			getFindDialog().search();
		}
	}
	/**
	 * Get a FindDialog.
	 * @return a FindDialog
	 */
	@SuppressWarnings("SynchronizedMethod")
	private synchronized FindDialog getFindDialog() {
		final FindDialog temp = finder;
		if (temp == null) {
			final FindDialog local = new FindDialog(parent, model);
			finder = local;
			return local;
		} else {
			return temp;
		}
	}
}
