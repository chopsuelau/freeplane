/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
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
package org.freeplane.features.icon;

import java.util.List;

import org.freeplane.features.filter.condition.StringConditionAdapter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
abstract class TagCondition extends StringConditionAdapter {
    static final String SEARCH_IN_CATEGORIES = "SEARCH_IN_CATEGORIES";

	final private boolean searchesInCategories;
	final private String comparedValue;

    /**
	 */
    TagCondition(final String comparedValue, final boolean matchCase,
			final boolean matchApproximately,
			final boolean matchWordwise, boolean ignoreDiacritics,
			boolean searchesInCategories) {
		super(matchCase, matchApproximately, matchWordwise, ignoreDiacritics);
        this.comparedValue = comparedValue;
        this.searchesInCategories = searchesInCategories;
	}

	@Override
	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		if(searchesInCategories())
		    child.setAttribute(SEARCH_IN_CATEGORIES, "true");
	}

    /*
     * (non-Javadoc)
     * @see
     * freeplane.controller.filter.condition.Condition#checkNode(freeplane.modes
     * .MindMapNode)
     */
    @Override
    public boolean checkNode(final NodeModel node) {
        final IconController iconController = IconController.getController();
        final List<Tag> tags = iconController.getTags(node);
        if(searchesInCategories()) {
            final List<CategorizedTag> categorizedTags = iconController.getCategorizedTags(tags, node.getMap().getIconRegistry());
            for (CategorizedTag tag : categorizedTags) {
                if (checkTag(tag))
                    return true;
            }
        }
        for (Tag tag : tags) {
            if (checkTag(tag))
                return true;

        }
        return false;
    }

    protected boolean checkTag(Tag tag) {
        return checkText(tag.getContent());
    }

    abstract protected boolean checkTag(CategorizedTag tag);

    protected abstract boolean checkText(String content);

    @Override
    protected Object conditionValue() {
        return comparedValue;
    }

    public boolean searchesInCategories() {
        return searchesInCategories;
    }
}