/*
 * IchthyopView.java
 */
package org.previmer.ichthyop.ui;

import java.awt.Color;
import java.awt.Toolkit;
import javax.swing.event.TreeSelectionEvent;
import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task;
import org.previmer.ichthyop.Template;
import org.previmer.ichthyop.manager.SimulationManager;

/**
 * The application's main frame.
 */
public class NewConfigView extends FrameView implements TreeSelectionListener {

    public NewConfigView(SingleFrameApplication app) {
        super(app);
        JFrame frame = new JFrame();
        frame.setName(getClass().getSimpleName());
        setFrame(frame);

        setStatusBar(statusBar);
        statusBar.connectToLogger(getLogger());

        resourceMap = getResourceMap();
        getFrame().setTitle(getResourceMap().getString("Application.title") + " - " + resourceMap.getString("View.title"));

        initComponents();
        createTemplateTree();
        getFrame().setIconImage(resourceMap.getImageIcon("Application.icon").getImage());

        btnSave.getAction().setEnabled(false);
        templateTree.addTreeSelectionListener(this);
    }
    
    @Action
    public void changePath() {
        JFileChooser chooser = new JFileChooser(location);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnPath = chooser.showDialog(getFrame(), resourceMap.getString("changePath.Action.shortDescription"));
        if (returnPath == JFileChooser.APPROVE_OPTION) {
            location = chooser.getSelectedFile();
            textFieldPath.setText(location.getAbsolutePath());
            textFieldFile.setText(getFilename(textFieldPath.getText(), textFieldName.getText()));
            if (!location.canWrite()) {
                lblChecker.setForeground(Color.RED);
                lblChecker.setText(resourceMap.getString("changePath.msg.invalid"));
                lblChecker.setIcon(resourceMap.getIcon("lblChecker.icon.invalid"));
                btnSave.getAction().setEnabled(false);
                Toolkit.getDefaultToolkit().beep();
            } else {
                btnSave.getAction().setEnabled(!textFieldTemplate.getText().startsWith("Not selected"));
                lblChecker.setText("");
                lblChecker.setIcon(null);
            }
        }
    }

    private File getFileTemplate(String templateName) throws Exception {
        File dst = new File(textFieldFile.getText());
        if (dst.exists()) {
            int answer = JOptionPane.showConfirmDialog(getFrame(), dst.getAbsolutePath() + " " + resourceMap.getString("save.dialog.overwrite"), resourceMap.getString("save.dialog.title"), JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return null;
            }
        }
        Template.createTemplate(templateName, dst);
        return dst;
    }

    @Action
    public Task save() {

        TEMPLATE template = TEMPLATE.getTemplate(textFieldTemplate.getText());
        File cfgFile = null;
        try {
            cfgFile = getFileTemplate(template.getFilename());
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Failed to create new configuration file ==> " + ex.toString(), ex);
            return null;
        }
        if (cfgFile != null) {
            getLogger().log(Level.INFO, resourceMap.getString("save.msg.created") + " " + cfgFile.getPath());
            cancel(); // close the view
            return IchthyopApp.getIchthyopView().loadConfigurationFile(cfgFile);
        }
        return null;
    }

    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) templateTree.getLastSelectedPathComponent();
        if (node != null && node.isLeaf()) {
            String strTemplate = node.getUserObject().toString();
            textFieldTemplate.setText(strTemplate);
            btnSave.getAction().setEnabled(verifyName(textFieldName.getText()));
            descriptionTextPane.setText(TEMPLATE.getTemplate(strTemplate).getDescription());
            textFieldName.setText(TEMPLATE.getTemplate(strTemplate).getCfgName());
            textFieldFile.setText(getFilename(textFieldPath.getText(), textFieldName.getText()));

        } else {
            descriptionTextPane.setText("");
        }
    }

    @Action
    public void cancel() {
        //getLogger().info(resourceMap.getString("view.close.text"));
        try {
            getFrame().setVisible(false);
            finalize();
        } catch (Throwable ex) {
        }
    }

    private Logger getLogger() {
        return SimulationManager.getLogger();
    }

    private boolean verifyName(String name) {
        Pattern p = Pattern.compile("[\\w&&[\\S]]+");
        return p.matcher(name).matches();
    }

    private String getFilename(String path, String name) {
        String filename = path;
        if (!filename.endsWith(File.separator)) {
            filename += File.separator;
        }
        filename += name;
        filename += ".xml";
        return filename;
    }

    private void createTemplateTree() {

        DefaultMutableTreeNode treeNode1 = new DefaultMutableTreeNode("Config templates");

        DefaultMutableTreeNode treeNode2 = new DefaultMutableTreeNode("MARS");
        DefaultMutableTreeNode treeNode3 = new DefaultMutableTreeNode(TEMPLATE.MANGA_2D_OPENDAP);
        treeNode2.add(treeNode3);
        treeNode3 = new DefaultMutableTreeNode(TEMPLATE.MANGA_3D_OPENDAP);
        treeNode2.add(treeNode3);
        treeNode3 = new DefaultMutableTreeNode(TEMPLATE.MENOR_3D_OPENDAP);
        treeNode2.add(treeNode3);
        treeNode3 = new DefaultMutableTreeNode(TEMPLATE.MARS2D);
        treeNode2.add(treeNode3);
        treeNode3 = new DefaultMutableTreeNode(TEMPLATE.MARS3D);
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);

        treeNode2 = new DefaultMutableTreeNode("ROMS");
        treeNode3 = new DefaultMutableTreeNode(TEMPLATE.ROMS2D);
        treeNode2.add(treeNode3);
        treeNode3 = new DefaultMutableTreeNode(TEMPLATE.ROMS3D);
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);

        treeNode2 = new DefaultMutableTreeNode("OPA");
        treeNode3 = new DefaultMutableTreeNode(TEMPLATE.OPA3D_NEMO);
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        
        treeNode2 = new DefaultMutableTreeNode("Evol");
        treeNode3 = new DefaultMutableTreeNode(TEMPLATE.ICHTHYOPEVOLROMS3D);
        treeNode2.add(treeNode3);
        treeNode3 = new DefaultMutableTreeNode(TEMPLATE.ICHTHYOPEVOLNEMO3D);
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        
        treeNode2 = new DefaultMutableTreeNode("Miscellaneous");
        treeNode3 = new DefaultMutableTreeNode(TEMPLATE.GENERIC);
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        templateTree.setModel(new DefaultTreeModel(treeNode1));
    }

    private static String getDefaultPath() {
        String path = System.getProperty("user.dir");
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        path += "cfg";
        return path;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        newFilePanel = new javax.swing.JPanel();
        btnChoosePath = new javax.swing.JButton();
        lblName = new javax.swing.JLabel();
        textFieldPath = new javax.swing.JTextField();
        lblPath = new javax.swing.JLabel();
        lblFile = new javax.swing.JLabel();
        textFieldFile = new javax.swing.JTextField();
        textFieldName = new javax.swing.JTextField();
        lblType = new javax.swing.JLabel();
        textFieldTemplate = new javax.swing.JTextField();
        lblChecker = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        templateTree = new javax.swing.JTree();
        jScrollPane3 = new javax.swing.JScrollPane();
        descriptionTextPane = new javax.swing.JTextPane();
        pnlInfo = new javax.swing.JPanel();
        lblInfo = new javax.swing.JLabel();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        mainPanel.setName("mainPanel"); // NOI18N

        newFilePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        newFilePanel.setName("newFilePanel"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(NewConfigView.class, this);
        btnChoosePath.setAction(actionMap.get("changePath")); // NOI18N
        btnChoosePath.setName("btnChoosePath"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(NewConfigView.class);
        lblName.setText(resourceMap.getString("lblName.text")); // NOI18N
        lblName.setName("lblName"); // NOI18N

        textFieldPath.setEditable(false);
        textFieldPath.setText(getDefaultPath());
        textFieldPath.setName("textFieldPath"); // NOI18N

        lblPath.setText(resourceMap.getString("lblPath.text")); // NOI18N
        lblPath.setName("lblPath"); // NOI18N

        lblFile.setText(resourceMap.getString("lblFile.text")); // NOI18N
        lblFile.setName("lblFile"); // NOI18N

        textFieldFile.setEditable(false);
        textFieldFile.setText(getFilename(getDefaultPath(), "NewConfig"));
        textFieldFile.setName("textFieldFile"); // NOI18N

        textFieldName.setText(resourceMap.getString("textFieldName.text")); // NOI18N
        textFieldName.setName("textFieldName"); // NOI18N
        textFieldName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textFieldNameKeyTyped(evt);
            }
        });

        lblType.setText(resourceMap.getString("lblType.text")); // NOI18N
        lblType.setName("lblType"); // NOI18N

        textFieldTemplate.setEditable(false);
        textFieldTemplate.setText(resourceMap.getString("textFieldTemplate.text")); // NOI18N
        textFieldTemplate.setName("textFieldTemplate"); // NOI18N

        lblChecker.setForeground(resourceMap.getColor("lblChecker.foreground")); // NOI18N
        lblChecker.setText(resourceMap.getString("lblChecker.text")); // NOI18N
        lblChecker.setName("lblChecker"); // NOI18N
        lblChecker.setText("");

        jSplitPane1.setDividerLocation(250);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        templateTree.setName("templateTree"); // NOI18N
        jScrollPane1.setViewportView(templateTree);

        jSplitPane1.setLeftComponent(jScrollPane1);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        descriptionTextPane.setContentType(resourceMap.getString("descriptionTextPane.contentType")); // NOI18N
        descriptionTextPane.setEditable(false);
        descriptionTextPane.setName("descriptionTextPane"); // NOI18N
        jScrollPane3.setViewportView(descriptionTextPane);

        jSplitPane1.setRightComponent(jScrollPane3);

        javax.swing.GroupLayout newFilePanelLayout = new javax.swing.GroupLayout(newFilePanel);
        newFilePanel.setLayout(newFilePanelLayout);
        newFilePanelLayout.setHorizontalGroup(
            newFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newFilePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(newFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 555, Short.MAX_VALUE)
                    .addGroup(newFilePanelLayout.createSequentialGroup()
                        .addGroup(newFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblType)
                            .addComponent(lblPath)
                            .addComponent(lblName)
                            .addComponent(lblFile))
                        .addGap(18, 18, 18)
                        .addGroup(newFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, newFilePanelLayout.createSequentialGroup()
                                .addComponent(textFieldPath, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnChoosePath))
                            .addComponent(textFieldName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                            .addComponent(textFieldFile, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                            .addComponent(textFieldTemplate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)))
                    .addComponent(lblChecker))
                .addContainerGap())
        );
        newFilePanelLayout.setVerticalGroup(
            newFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newFilePanelLayout.createSequentialGroup()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(newFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblType)
                    .addComponent(textFieldTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(newFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPath)
                    .addComponent(btnChoosePath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(newFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(textFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(newFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFile)
                    .addComponent(textFieldFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblChecker)
                .addContainerGap(27, Short.MAX_VALUE))
        );

        pnlInfo.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlInfo.border.title"))); // NOI18N
        pnlInfo.setName("pnlInfo"); // NOI18N

        lblInfo.setText(resourceMap.getString("lblInfo.text")); // NOI18N
        lblInfo.setName("lblInfo"); // NOI18N

        javax.swing.GroupLayout pnlInfoLayout = new javax.swing.GroupLayout(pnlInfo);
        pnlInfo.setLayout(pnlInfoLayout);
        pnlInfoLayout.setHorizontalGroup(
            pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 579, Short.MAX_VALUE)
            .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlInfoLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        pnlInfoLayout.setVerticalGroup(
            pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 112, Short.MAX_VALUE)
            .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlInfoLayout.createSequentialGroup()
                    .addComponent(lblInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        btnSave.setAction(actionMap.get("save")); // NOI18N
        btnSave.setName("btnSave"); // NOI18N

        btnCancel.setAction(actionMap.get("cancel")); // NOI18N
        btnCancel.setName("btnCancel"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(newFilePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                        .addComponent(btnSave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel)))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newFilePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnSave))
                .addContainerGap())
        );

        setComponent(mainPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void textFieldNameKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldNameKeyTyped
        // TODO add your handling code here:

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                String name = textFieldName.getText();
                if (!verifyName(name)) {
                    lblChecker.setForeground(Color.RED);
                    lblChecker.setText(resourceMap.getString("lblChecker.msg.invalid"));
                    lblChecker.setIcon(resourceMap.getIcon("lblChecker.icon.invalid"));
                    btnSave.getAction().setEnabled(false);
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    btnSave.getAction().setEnabled(!textFieldTemplate.getText().startsWith("Not selected"));
                    lblChecker.setText("");
                    lblChecker.setIcon(null);
                }
                textFieldFile.setText(getFilename(textFieldPath.getText(), name));
            }
        });
    }//GEN-LAST:event_textFieldNameKeyTyped
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnChoosePath;
    private javax.swing.JButton btnSave;
    private javax.swing.JTextPane descriptionTextPane;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel lblChecker;
    private javax.swing.JLabel lblFile;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblPath;
    private javax.swing.JLabel lblType;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel newFilePanel;
    private javax.swing.JPanel pnlInfo;
    private javax.swing.JTree templateTree;
    private javax.swing.JTextField textFieldFile;
    private javax.swing.JTextField textFieldName;
    private javax.swing.JTextField textFieldPath;
    private javax.swing.JTextField textFieldTemplate;
    // End of variables declaration//GEN-END:variables
    private File location = new File(getDefaultPath());
    private ResourceMap resourceMap;
    private JStatusBar statusBar = new JStatusBar();

    private enum TEMPLATE {

        /* enum */
        MANGA_2D_OPENDAP,
        MANGA_3D_OPENDAP,
        MENOR_3D_OPENDAP,
        MARS2D,
        MARS3D,
        ROMS2D,
        ROMS3D,
        OPA3D_NEMO,
        ICHTHYOPEVOLROMS3D,
        ICHTHYOPEVOLNEMO3D,
        GENERIC;
        /* variables */
        private String longName;
        private String description;
        private String filename;
        private String cfgname;
        private ResourceMap resourceMap = Application.getInstance().getContext().getResourceMap(NewConfigView.class);

        TEMPLATE() {
            longName = resourceMap.getString(name() + ".title");
            description = resourceMap.getString(name() + ".description");
            filename = resourceMap.getString(name() + ".xml");
            cfgname = resourceMap.getString(name() + ".cfg");
        }

        @Override
        public String toString() {
            return longName;
        }

        public String getDescription() {
            return description;
        }

        public String getFilename() {
            return filename;
        }

        public String getCfgName() {
            SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy'_'MM'_'dd");
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(System.currentTimeMillis());
            dtFormat.setCalendar(calendar);
            StringBuffer sb = new StringBuffer();
            sb.append(dtFormat.format(calendar.getTime()));
            sb.append("_");
            String username = System.getProperty("user.name");
            if (null != username && !username.isEmpty()) {
                sb.append(username);
                sb.append("_config");
                
                
            } else {
                sb.append("my_config");
            }
            sb.append("_");
            sb.append(cfgname);
            return sb.toString();
        }

        public static TEMPLATE getTemplate(String name) {
            for (TEMPLATE template : TEMPLATE.values()) {
                if (template.toString().matches(name)) {
                    return template;
                }
            }
            return TEMPLATE.GENERIC;
        }
    }
}
