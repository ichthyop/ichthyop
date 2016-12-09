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

import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.ichthyop.manager.SimulationManager;

/**
 *
 * @author pverley
 */
public class JRunProgressPanel extends javax.swing.JPanel {

    /** Creates new form JRunProgressPanel */
    public JRunProgressPanel() {
        initComponents();
        hideBars();
    }

    public void resetProgressBar() {
        progressBarCurrent.setValue(0);
        lblTimeLeftCurrent.setText(getResourceMap().getString("lblTimeLeftCurrent.text"));
        progressBarGlobal.setValue(0);
        lblTimeLeftGlobal.setText(getResourceMap().getString("lblTimeLeftGlobal.text"));
    }

    private ResourceMap getResourceMap() {
        return Application.getInstance().getContext().getResourceMap(JRunProgressPanel.class);
    }

    public void hideBars() {
        lblProgressCurrent.setVisible(false);
        progressBarCurrent.setVisible(false);
        lblTimeLeftCurrent.setVisible(false);
        lblProgressGlobal.setVisible(false);
        progressBarGlobal.setVisible(false);
        lblTimeLeftGlobal.setVisible(false);
    }

    public void setupProgress() {
        // show progress bar for current simulation
        lblProgressCurrent.setVisible(true);
        progressBarCurrent.setVisible(true);
        lblTimeLeftCurrent.setVisible(true);

        // show global progress bar for SERIAL runs only
        if (getSimulationManager().getNumberOfSimulations() > 1) {
            lblProgressGlobal.setVisible(true);
            progressBarGlobal.setVisible(true);
            lblTimeLeftGlobal.setVisible(true);
        } else {
            lblProgressGlobal.setVisible(false);
            progressBarGlobal.setVisible(false);
            lblTimeLeftGlobal.setVisible(false);
        }
    }

    public void printProgress() {

        if (progressBarCurrent.isVisible()) {
            progressBarCurrent.setValue((int) (getSimulationManager().progressCurrent() * 100));
            StringBuffer strBf = new StringBuffer();
            strBf.append(getSimulationManager().getTimeManager().stepToString());
            strBf.append(" - ");
            strBf.append(getSimulationManager().timeLeftCurrent());
            lblTimeLeftCurrent.setText(strBf.toString());
        }

        if (progressBarGlobal.isVisible()) {
            progressBarGlobal.setValue((int) (getSimulationManager().progressGlobal() * 100));
            StringBuffer strBf = new StringBuffer("Simulation ");
            strBf.append(getSimulationManager().indexSimulationToString());
            strBf.append(" - ");
            strBf.append(getSimulationManager().timeLeftGlobal());
            lblTimeLeftGlobal.setText(strBf.toString());
        }
    }

    private SimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblProgressCurrent = new javax.swing.JLabel();
        progressBarCurrent = new javax.swing.JProgressBar();
        lblTimeLeftCurrent = new javax.swing.JLabel();
        lblProgressGlobal = new javax.swing.JLabel();
        progressBarGlobal = new javax.swing.JProgressBar();
        lblTimeLeftGlobal = new javax.swing.JLabel();
        lblIcon = new javax.swing.JLabel();

        setOpaque(false);

        lblProgressCurrent.setText("Current simulation");
        lblProgressCurrent.setForeground(java.awt.Color.white);
        lblProgressCurrent.setName("lblProgressCurrent"); // NOI18N

        progressBarCurrent.setName("progressBarCurrent"); // NOI18N
        progressBarCurrent.setStringPainted(true);

        lblTimeLeftCurrent.setText("time left");
        lblTimeLeftCurrent.setForeground(new java.awt.Color(255, 255, 255));
        lblTimeLeftCurrent.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        lblTimeLeftCurrent.setName("lblTimeLeftCurrent"); // NOI18N

        lblProgressGlobal.setText("Global run");
        lblProgressGlobal.setForeground(new java.awt.Color(255, 255, 255));
        lblProgressGlobal.setName("lblProgressGlobal"); // NOI18N

        progressBarGlobal.setName("progressBarGlobal"); // NOI18N
        progressBarGlobal.setStringPainted(true);

        lblTimeLeftGlobal.setText("time left");
        lblTimeLeftGlobal.setForeground(java.awt.Color.white);
        lblTimeLeftGlobal.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        lblTimeLeftGlobal.setName("lblTimeLeftGlobal"); // NOI18N

        lblIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/ichthyop/ui/resources/images/ico256/run-simulation.png"))); // NOI18N
        lblIcon.setName("lblIcon"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblProgressCurrent)
                            .addComponent(lblProgressGlobal))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(progressBarGlobal, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                            .addComponent(progressBarCurrent, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblTimeLeftCurrent)
                            .addComponent(lblTimeLeftGlobal)))
                    .addComponent(lblIcon, javax.swing.GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblIcon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblProgressCurrent)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(progressBarCurrent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblTimeLeftCurrent)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblProgressGlobal)
                    .addComponent(lblTimeLeftGlobal)
                    .addComponent(progressBarGlobal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblProgressCurrent;
    private javax.swing.JLabel lblProgressGlobal;
    private javax.swing.JLabel lblTimeLeftCurrent;
    private javax.swing.JLabel lblTimeLeftGlobal;
    private javax.swing.JProgressBar progressBarCurrent;
    private javax.swing.JProgressBar progressBarGlobal;
    // End of variables declaration//GEN-END:variables
}