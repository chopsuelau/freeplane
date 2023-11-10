/*
 * Created on 9 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.features.map.codeexplorermode;

import org.freeplane.api.ChildNodesLayout;
import org.freeplane.features.layout.LayoutController;
import org.freeplane.features.map.NodeModel;

public class CodeLayoutController extends LayoutController {
    @Override
    public ChildNodesLayout getChildNodesLayout(NodeModel node) {
        return ChildNodesLayout.TOPTOBOTTOM_RIGHT_BOTTOM;
    }
}
