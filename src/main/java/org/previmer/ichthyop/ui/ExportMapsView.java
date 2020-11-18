/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

package org.previmer.ichthyop.ui;

import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.manager.SimulationManager;
import org.previmer.ichthyop.ui.logging.LogLevel;
import org.previmer.ichthyop.util.MetaFilenameFilter;

/**
 * The application's main frame.
 */
public class ExportMapsView extends FrameView {

    public ExportMapsView(SingleFrameApplication app, File folder) {
        super(app);
        JFrame frame = new JFrame();
        frame.setName("backupView");
        setFrame(frame);

        JStatusBar statusBar = new JStatusBar();
        setStatusBar(statusBar);
        statusBar.connectToLogger(getLogger());

        this.folder = folder;

        getFrame().setTitle(getResourceMap().getString("Application.title") + " - " + folder.getName());

        initComponents();
        getFrame().setIconImage(getResourceMap().getImageIcon("Application.icon").getImage());

        resourceMap = getResourceMap();

        btnSave.getAction().setEnabled(false);
    }

    /*
    private SimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }
    */

    @Action
    public void changePath() {
        JFileChooser chooser = new JFileChooser(backupPath);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnPath = chooser.showDialog(getFrame(), "Select folder");
        if (returnPath == JFileChooser.APPROVE_OPTION) {
            if (!chooser.getSelectedFile().equals(folder)) {
                btnSave.getAction().setEnabled(true);
                backupPath = chooser.getSelectedFile();
                textFieldPath.setEnabled(true);
                textFieldPath.setText(backupPath.toString());
            } else {
                JOptionPane.showMessageDialog(mainPanel, resourceMap.getString("changePath.wrongPath"), resourceMap.getString("changePath.Action.shortDescription"), JOptionPane.OK_OPTION);
                changePath();
            }
        }
    }

    @Action
    public Task<Object, Object> save() {
        return new SaveTask(getApplication());
    }

    private class SaveTask extends SFTask<Object, Object> {

        SaveTask(Application instance) {
            super(instance);
        }

        @Override
        protected Object doInBackground() throws Exception {

            setMessage(resourceMap.getString("save.Action.saving"));
            File[] pictures = folder.listFiles(new MetaFilenameFilter("*.png"));
            int nbFiles = pictures.length;
            for (int i = 0; i < nbFiles; i++) {
                File sfile = pictures[i];
                File dfile = rename(sfile);
                setProgress(i / (float) nbFiles);
                IOTools.copyFile(sfile, dfile);
            }
            return Integer.valueOf(nbFiles);
        }

        @Override
        void onSuccess(Object result) {
            btnSave.getAction().setEnabled(false);
            int nbFiles = (Integer) result;
            setMessage(resourceMap.getString("save.Action.succeeded") + " " + nbFiles, false, LogLevel.COMPLETE);
        }

        @Override
        void onFailure(Throwable throwable) {
            // nothing to do
        }
    }

    private File rename(File file) {
        String destDirectory = textFieldPath.getText();
        if (!destDirectory.endsWith(File.separator)) {
            destDirectory += File.separator;
        }
        String filename = file.getName();
        filename = filename.replaceFirst(folder.getName(), textFieldName.getText().trim());
        return new File(destDirectory + filename);
    }

    @Action
    public void cancel() {
        getLogger().info(resourceMap.getString("view.close.text"));
        try {
            getFrame().setVisible(false);
            finalize();
        } catch (Throwable ex) {
        }
    }

    private Logger getLogger() {
        return SimulationManager.getLogger();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        btnChoosePath = new javax.swing.JButton();
        lblName = new javax.swing.JLabel();
        textFieldPath = new javax.swing.JTextField();
        lblPath = new javax.swing.JLabel();
        textFieldName = new javax.swing.JTextField();
        textFieldName.setText(folder.getName());
        pnlInfo = new javax.swing.JPanel();
        lblInfo = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();

        mainPanel.setName("mainPanel"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(ExportMapsView.class, this);
        btnChoosePath.setAction(actionMap.get("changePath")); // NOI18N
        btnChoosePath.setName("btnChoosePath"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(ExportMapsView.class);
        lblName.setText(resourceMap.getString("lblName.text")); // NOI18N
        lblName.setName("lblName"); // NOI18N

        textFieldPath.setEditable(false);
        textFieldPath.setName("textFieldPath"); // NOI18N
        textFieldPath.setText("Path not set yet");
        textFieldPath.setEnabled(false);

        lblPath.setText(resourceMap.getString("lblPath.text")); // NOI18N
        lblPath.setName("lblPath"); // NOI18N

        textFieldName.setName("textFieldName"); // NOI18N
        textFieldName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textFieldNameKeyTyped(evt);
            }
        });

        pnlInfo.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlInfo.border.title"))); // NOI18N
        pnlInfo.setName("pnlInfo"); // NOI18N

        lblInfo.setText(resourceMap.getString("lblInfo.text")); // NOI18N
        lblInfo.setName("lblInfo"); // NOI18N

        javax.swing.GroupLayout pnlInfoLayout = new javax.swing.GroupLayout(pnlInfo);
        pnlInfo.setLayout(pnlInfoLayout);
        pnlInfoLayout.setHorizontalGroup(
            pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 457, Short.MAX_VALUE)
            .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlInfoLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 433, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        pnlInfoLayout.setVerticalGroup(
            pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 118, Short.MAX_VALUE)
            .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlInfoLayout.createSequentialGroup()
                    .addGap(19, 19, 19)
                    .addComponent(lblInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(19, Short.MAX_VALUE)))
        );

        btnCancel.setAction(actionMap.get("cancel")); // NOI18N
        btnCancel.setName("btnCancel"); // NOI18N

        btnSave.setAction(actionMap.get("save")); // NOI18N
        btnSave.setName("btnSave"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPath)
                            .addComponent(lblName))
                        .addGap(18, 18, 18)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(textFieldPath)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnChoosePath))
                            .addComponent(textFieldName, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE))))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap(286, Short.MAX_VALUE)
                .addComponent(btnSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCancel)
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPath)
                    .addComponent(btnChoosePath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(textFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnSave))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setComponent(mainPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void textFieldNameKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldNameKeyTyped
        
        if (!textFieldPath.getText().startsWith("Path")) {
            btnSave.getAction().setEnabled(true);
        }
    }//GEN-LAST:event_textFieldNameKeyTyped
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnChoosePath;
    private javax.swing.JButton btnSave;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblPath;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel pnlInfo;
    private javax.swing.JTextField textFieldName;
    private javax.swing.JTextField textFieldPath;
    // End of variables declaration//GEN-END:variables

    private File folder;
    private File backupPath = new File(System.getProperty("user.dir"));
    private ResourceMap resourceMap;
}
