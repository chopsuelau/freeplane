/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.filter;

import java.awt.event.ActionEvent;

import javax.swing.ButtonModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.filter.Filter.FilteredElement;

/**
 * @author Dimitry Polivaev
 * Mar 31, 2009
 */
@SelectableAction
class SelectFilteredElementAction extends AFreeplaneAction {
	final static String NAME = "SelectFilteredElementAction";
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
    private final ButtonModel elementButtonModel;
	/**
	 *
	 */
	SelectFilteredElementAction(ButtonModel elementButtonModel, FilteredElement filteredElement) {
		super(NAME + "." + filteredElement.name());
        this.elementButtonModel = elementButtonModel;
		elementButtonModel.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				setSelected(isModelSelected());
			}
		});
		setSelected(isModelSelected());
	}

	public void actionPerformed(final ActionEvent e) {
	    if(!isModelSelected())
	        elementButtonModel.setSelected(true);
	}

	private boolean isModelSelected() {
		return elementButtonModel.isSelected();
	}
}
