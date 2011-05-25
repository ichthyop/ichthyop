/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
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
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.previmer.ichthyop.io.XBlock;
import org.previmer.ichthyop.manager.ParameterManager;
import org.previmer.ichthyop.manager.SimulationManager;

/**
 *
 * @author pverley
 */
public class BlockTree extends JTree {

    private HashMap<String, XBlock> blockMap;

    public BlockTree() {
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
        return blockMap.keySet();
    }

    public void put(String key, XBlock variable) {
        if (blockMap != null) {
            blockMap.put(key, variable);
        }
    }

    public XBlock get(String key) {
        if (blockMap != null) {
            return blockMap.get(key);
        }
        return null;
    }

    public void writeStructure(ParameterManager manager) {
        for (Enumeration e1 = getRoot().postorderEnumeration(); e1.hasMoreElements();) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e1.nextElement();
            if (node.isLeaf()) {
                XBlock block = blockMap.get(nodeToTreePath(node));
                manager.addBlock(block);
            }
        }
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
        List<XBlock> listb = new ArrayList();
        for (XBlock block : getSimulationManager().getParameterManager().readBlocks()) {
            listb.add(block);
        }
        //Collections.sort(listb);
        Collections.reverse(listb);
        blockMap = new HashMap(listb.size());
        for (XBlock block : listb) {
            insertIntoTree(block);
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

    public void refresh(DefaultMutableTreeNode node, XBlock block) {
        int leafIndex = getLeafIndex(node);
        remove(node);
        insertIntoTree(block, leafIndex, true);
    }

    public void remove(DefaultMutableTreeNode node) {
        if (node != null && !node.isRoot()) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
            blockMap.remove(nodeToTreePath(node));
            getDModel().removeNodeFromParent(node);
            if (parent != null && parent.isLeaf()) {
                remove(parent);
            }
        }
    }

    public void insertIntoTree(XBlock block) {
        insertIntoTree(block, 0, false);
    }

    public void insertIntoTree(XBlock block, int leafIndex, boolean isVisible) {

        blockMap.put(block.getTreePath(), block);
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
        StringBuffer key = new StringBuffer();
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

    public XBlock getSelectedBlock() {
        return blockMap.get(getTreePath());
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
            ResourceMap resourceMap = Application.getInstance().getContext().getResourceMap(BlockTree.class);
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
