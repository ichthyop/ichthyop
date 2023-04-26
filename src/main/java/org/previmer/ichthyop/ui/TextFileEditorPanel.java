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
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothee BROCHIER,
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class TextFileEditorPanel extends javax.swing.JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 993380461617899469L;

    /** Creates new form TextFileEditorPanel */
    public TextFileEditorPanel() {
        initComponents();
    }

    public void editFile(String filename) {
        txtFieldFile.setText(filename);
        editorPane.setText("");
        if (!new File(filename).exists()) {
            save();
        }
        try {
            editorPane.read(new File(filename).toURI().toURL().openStream(), "");
        } catch (IOException ex) {
            Logger.getLogger(TextFileEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getFilename() {
        return txtFieldFile.getText();
    }

    private void save() {
        try {
            URL url = new File(txtFieldFile.getText()).toURI().toURL();
            if (url == null) {
                return;
            }
            FileWriter fw = null;
            try {
                File f = new File(url.getPath());
                fw = new FileWriter(f);
                fw.write(editorPane.getText());
                btnSave.setEnabled(false);
            } catch (IOException ex) {
                Logger.getLogger(TextFileEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (fw != null) {
                        fw.close();
                    }
                } catch (IOException ex) {
                    //Logger.getLogger(LegendPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(TextFileEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JEditorPane();
        btnSave = new javax.swing.JButton();
        txtFieldFile = new javax.swing.JTextField();

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        editorPane.setBackground(new java.awt.Color(254, 254, 254));
        editorPane.setName("editorPane"); // NOI18N
        editorPane.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                editorPaneKeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(editorPane);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico22/save.png"))); // NOI18N
        btnSave.setText("Save");
        btnSave.setEnabled(false);
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnSave.setName("btnSave"); // NOI18N
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        txtFieldFile.setEditable(false);
        txtFieldFile.setFont(new java.awt.Font("DejaVu Sans", 0, 12));
        txtFieldFile.setText("file name");
        txtFieldFile.setName("txtFieldFile"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(btnSave)
                    .addComponent(txtFieldFile, javax.swing.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtFieldFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSave)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed

        save();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void editorPaneKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_editorPaneKeyTyped

        btnSave.setEnabled(true);
    }//GEN-LAST:event_editorPaneKeyTyped
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSave;
    private javax.swing.JEditorPane editorPane;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField txtFieldFile;
    // End of variables declaration//GEN-END:variables
}
