/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 *
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 *
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package org.ichthyop.ui;

import java.awt.Component;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.ichthyop.ui.param.UIParameterSubset;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.ichthyop.manager.SimulationManager;

/**
 *
 * @author pverley
 */
public class ParameterTree extends JTree {

    private HashMap<String, UIParameterSubset> map;

    public ParameterTree() {
        super(new Object[]{});
    }

    public DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) getLastSelectedPathComponent();
    }

    public DefaultTreeModel getDModel() {
        return (DefaultTreeModel) getModel();
    }

    public DefaultMutableTreeNode getRoot() {
        return (DefaultMutableTreeNode) getModel().getRoot();
    }

    public boolean upper() {
        DefaultMutableTreeNode selectedNode = getSelectedNode();
        if (selectedNode != null) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
            if (parentNode != null) {
                int selectedNodeIndex = selectedNode.getParent().getIndex(selectedNode);
                if (selectedNodeIndex > 0) {
                    getDModel().removeNodeFromParent(selectedNode);
                    getDModel().insertNodeInto(selectedNode, parentNode, selectedNodeIndex - 1);
                    setNodeVisible(selectedNode);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean lower() {
        DefaultMutableTreeNode selectedNode = getSelectedNode();
        if (selectedNode != null) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
            if (parentNode != null) {
                int selectedNodeIndex = parentNode.getIndex(selectedNode);
                if (selectedNodeIndex < parentNode.getChildCount() - 1) {
                    getDModel().removeNodeFromParent(selectedNode);
                    getDModel().insertNodeInto(selectedNode, parentNode, selectedNodeIndex + 1);
                    setNodeVisible(selectedNode);
                    return true;
                }
            }
        }
        return false;
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public void put(String key, UIParameterSubset variable) {
        if (map != null) {
            map.put(key, variable);
        }
    }

    public UIParameterSubset get(String key) {
        if (map != null) {
            return map.get(key);
        }
        return null;
    }

    public void expandAll() {
        for (int i = 0; i < getRowCount(); i++) {
            expandRow(i);
        }

    }

    public void collapseAll() {
        for (int i = 0; i < getRowCount(); i++) {
            collapseRow(i);
        }

    }

    public void createModel() throws IOException {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(getSimulationManager().getConfigurationFile().getName());
        setModel(new DefaultTreeModel(root));
        List<String> keys = Arrays.asList(getSimulationManager().getParameterManager().getParameterSubsets());
        Collections.reverse(keys);
        map = new HashMap(keys.size());
        for (String key : keys) {
            if (!getSimulationManager().getParameterManager().findKeys(key + ".*").isEmpty()) {
                insertIntoTree(new UIParameterSubset(key));
            }
        }
        setCellRenderer(new TreeRenderer());
    }

    private DefaultMutableTreeNode getNodeInParent(DefaultMutableTreeNode parent, String nodeName) {
        return getNodeInParent(parent, nodeName, 0, true);
    }

    private DefaultMutableTreeNode getNodeInParent(DefaultMutableTreeNode parent, String nodeName, int index, boolean canCreateNode) {

        if (parent != null) {
            for (Enumeration e1 = parent.children(); e1.hasMoreElements();) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) e1.nextElement();
                if (node.getUserObject().equals(nodeName)) {
                    return node;
                }
            }
            if (canCreateNode) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(nodeName);
                getDModel().insertNodeInto(node, parent, index);
                return node;
            }
        }
        return null;
    }

    public DefaultMutableTreeNode insertNodeInParent(DefaultMutableTreeNode parent, String nodeName) {
        return insertNodeInParent(parent, nodeName, 0);
    }

    public DefaultMutableTreeNode insertNodeInParent(DefaultMutableTreeNode parent, String nodeName, int index) {
        if (parent != null) {
            for (Enumeration e1 = parent.children(); e1.hasMoreElements();) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) e1.nextElement();
                if (node.getUserObject().equals(nodeName)) {
                    return node;
                }
            }
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(nodeName);
            getDModel().insertNodeInto(node, parent, index);
            return node;
        }
        return null;
    }

    private int getLeafIndex(DefaultMutableTreeNode node) {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        return parent.getIndex(node);
    }

    public void refresh(DefaultMutableTreeNode node, UIParameterSubset block) {
        int leafIndex = getLeafIndex(node);
        remove(node);
        insertIntoTree(block, leafIndex, true);
    }

    public void remove(DefaultMutableTreeNode node) {
        if (node != null && !node.isRoot()) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
            map.remove(nodeToTreePath(node));
            getDModel().removeNodeFromParent(node);
            if (parent != null && parent.isLeaf()) {
                remove(parent);
            }
        }
    }

    public void insertIntoTree(UIParameterSubset block) {
        insertIntoTree(block, 0, false);
    }

    public void insertIntoTree(UIParameterSubset block, int leafIndex, boolean isVisible) {

        map.put(block.getTreePath(), block);
        String[] treePath = block.getTreePath().split("/");
        DefaultMutableTreeNode node = insertNodeInParent(getRoot(), treePath[0]);
        if (treePath.length > 1) {
            for (int i = 1; i < treePath.length - 1; i++) {
                node = insertNodeInParent(node, treePath[i]);
            }
            node = insertNodeInParent(node, treePath[treePath.length - 1], leafIndex);
        }
        if (isVisible) {
            setNodeVisible(node);
        }
    }

    public void setTreePathVisible(TreePath treePath) {
        if (treePath != null) {
            setSelectionPath(treePath);
            scrollPathToVisible(treePath);
        }
    }

    public void setNodeVisible(TreeNode node) {
        //System.out.println("show: " + nodeToKey((DefaultMutableTreeNode)node));
        TreePath treePath = new TreePath(((DefaultMutableTreeNode) node).getPath());
        setTreePathVisible(treePath);
    }

    public String nodeToTreePath(DefaultMutableTreeNode node) {
        TreeNode[] path = node.getPath();
        StringBuilder key = new StringBuilder();
        if (path.length < 2) {
            return null;
        }
        for (int i = 1; i < path.length; i++) {
            key.append(path[i].toString());
            key.append("/");
        }
        key.deleteCharAt(key.length() - 1);
        return key.toString();
    }

    public UIParameterSubset getParameterSet() {
        return map.get(getTreePath());
    }

    public String getTreePath() {
        return nodeToTreePath(getSelectedNode());
    }

    /*public TreePath keyToTreePath(String key) {
        //String fullKey = getRoot().toString() + "/" + key;
        return find(new TreePath(getRoot()), key.split("/"), 0, true);
    }*/
    private TreePath find(TreePath parent, Object[] nodes, int depth, boolean byName) {

        TreeNode node = (TreeNode) parent.getLastPathComponent();
        Object o = node;
        // If by name, convert node to a string
        if (byName) {
            o = o.toString();
        }
        // If equal, go down the branch
        if (o.equals(nodes[depth])) {
            // If at end, return match
            if (depth == nodes.length - 1) {
                return parent;
            }
            // Traverse children
            if (node.getChildCount() >= 0) {
                for (Enumeration e = node.children(); e.hasMoreElements();) {
                    TreeNode n = (TreeNode) e.nextElement();
                    TreePath path = parent.pathByAddingChild(n);
                    TreePath result = find(path, nodes, depth + 1, byName);
                    // Found a match
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        // No match at this branch
        return null;
    }

    private SimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }

    class TreeRenderer extends DefaultTreeCellRenderer {

        Icon iconLeaf, iconRoot, iconNode, iconNodeExpanded;

        TreeRenderer() {
            ResourceMap resourceMap = Application.getInstance().getContext().getResourceMap(ParameterTree.class);
            iconLeaf = resourceMap.getIcon("Tree.icon.leaf");
            iconRoot = resourceMap.getIcon("Tree.icon.root");
            iconNode = resourceMap.getIcon("Tree.icon.node");
            iconNodeExpanded = resourceMap.getIcon("Tree.icon.nodexp");
        }

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);

            if (row == 0) {
                setIcon(iconRoot);
                return this;
            }

            if (!leaf) {
                if (expanded) {
                    setIcon(iconNodeExpanded);
                } else {
                    setIcon(iconNode);
                }
            } else {
                setIcon(iconLeaf);
            }
            return this;
        }
    }
}
