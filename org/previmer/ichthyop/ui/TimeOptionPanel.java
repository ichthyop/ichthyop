/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TimeOptionPanel.java
 *
 * Created on Feb 2, 2010, 10:02:01 AM
 */
package org.previmer.ichthyop.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.BorderFactory;
import org.jdesktop.application.Task;
import org.previmer.ichthyop.calendar.Calendar1900;
import org.previmer.ichthyop.calendar.ClimatoCalendar;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.XBlock;
import org.previmer.ichthyop.io.XParameter;

/**
 *
 * @author pverley
 */
public class TimeOptionPanel extends JBlockPanel implements PropertyChangeListener, ActionListener {

    /** Creates new form TimeOptionPanel */
    public TimeOptionPanel() {
        this(null);
    }

    public TimeOptionPanel(XBlock block) {
        initComponents();
        setBlock(block);
        if (null != block) {
            //putValues();
        }
        addListeners();
    }

    private void addListeners() {
        txtFieldTimeOrigin.addPropertyChangeListener("value", this);
        txtFieldInitialTime.addPropertyChangeListener("value", this);
        txtFieldTimeStep.addPropertyChangeListener("value", this);
        rdBtnForward.addActionListener(this);
        rdBtnBackward.addActionListener(this);
        rdBtnClimato.addActionListener(this);
        rdBtnGregorian.addActionListener(this);
    }

    private void putValues() {

        // Block info
        pnlBlockInfo.setBorder(BorderFactory.createTitledBorder(getBlock().getTreePath()));
        StringBuffer info = new StringBuffer("<html><i>");
        info.append(getBlock().getDescription());
        info.append("</i></html>");
        lblBlockInfo.setText(info.toString());

        lblRetrieveInfo.setText("");

        // Direction in time
        if (getBlock().getXParameter("time_arrow").getValue().matches("forward")) {
            rdBtnForward.doClick();
        } else {
            rdBtnBackward.doClick();
        }

        // Type of calendar
        if (getBlock().getXParameter("calendar_type").getValue().matches("climato")) {
            rdBtnClimato.doClick();
        } else {
            rdBtnGregorian.doClick();
        }

        // Time origin
        if (rdBtnGregorian.isSelected()) {
            txtFieldTimeOrigin.setEnabled(true);
            txtFieldTimeOrigin.setValue(getBlock().getXParameter("time_origin"));
        }

        // Initial time
        txtFieldInitialTime.setValue(Long.valueOf(getBlock().getXParameter("initial_time").getValue()).longValue());

        // Transport duration
        txtFieldDuration.setValue(Long.valueOf(getBlock().getXParameter("transport_duration").getValue()).longValue());

        // Time step
        txtFieldTimeStep.setValue(Integer.valueOf(getBlock().getXParameter("time_step").getValue()));
    }

    public Task retrieveNCInfo() {
        return null;
    }

    public void propertyChange(PropertyChangeEvent evt) {

        if (evt.getPropertyName().matches("value")) {
            firePropertyChange("xicfile", null, null);
        }

        Object source = evt.getSource();

        if (source == txtFieldTimeOrigin) {
            if (txtFieldTimeOrigin.isEditValid()) {
                txtFieldInitialTime.setCalendar(new Calendar1900(txtFieldTimeOrigin.getText()));
                txtFieldFirstTime.setCalendar(new Calendar1900(txtFieldTimeOrigin.getText()));
                getBlock().getXParameter("time_origin").setValue(txtFieldFirstTime.getText());
            }
        }

        if (source == txtFieldInitialTime) {
            if (txtFieldInitialTime.isEditValid()) {
                getBlock().getXParameter("initial_time").setValue(String.valueOf(txtFieldInitialTime.getTimeInSeconds()));
            }
        }

        if (source == txtFieldDuration) {
            if (txtFieldDuration.isEditValid()) {
                getBlock().getXParameter("transport_duration").setValue(String.valueOf(txtFieldDuration.getDurationInSeconds()));
            }
        }

        if (source == txtFieldTimeStep) {
            if (txtFieldTimeStep.isEditValid()) {
                getBlock().getXParameter("time_step").setValue(((Long) txtFieldTimeStep.getValue()).toString());
            }
        }
    }

    private void setParamInfo(XParameter xparam) {
        if (null != xparam) {
            try {
                pnlParamDescription.setBorder(BorderFactory.createTitledBorder(xparam.getLongName()));
                StringBuffer info = new StringBuffer("<html><i>");
                info.append(xparam.getDescription());
                info.append("</i></html>");
                lblParameter.setText(info.toString());
            } catch (Exception ex) {
                pnlParamDescription.setBorder(BorderFactory.createTitledBorder("Parameter description"));
                lblParameter.setText("No description available");
            }
        } else {
            pnlParamDescription.setBorder(BorderFactory.createTitledBorder("Parameter description"));
            lblParameter.setText("No description available");
        }
    }

    public void actionPerformed(ActionEvent e) {
        firePropertyChange("xicfile", null, null);

        Object source = e.getSource();

        if (source == rdBtnForward) {
            getBlock().getXParameter("time_arrow").setValue("forward");
        }

        if (source == rdBtnBackward) {
            getBlock().getXParameter("time_arrow").setValue("backward");
        }

        if (source == rdBtnClimato) {
            getBlock().getXParameter("calendar_type").setValue("climato");
        }

        if (source == rdBtnGregorian) {
            getBlock().getXParameter("calendar_type").setValue("gregorian");
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        pnlBlockInfo = new javax.swing.JPanel();
        lblBlockInfo = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btnRetrieveInfo = new javax.swing.JButton();
        lblRetrieveInfo = new javax.swing.JLabel();
        txtFieldFirstTime = new org.previmer.ichthyop.ui.param.JDateTextField();
        txtFieldMaxDuration = new org.previmer.ichthyop.ui.param.JDurationTextField();
        txtFieldDataTimeStep = new javax.swing.JFormattedTextField();
        txtFieldBestTimeStep = new javax.swing.JFormattedTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        rdBtnForward = new javax.swing.JRadioButton();
        rdBtnBackward = new javax.swing.JRadioButton();
        rdBtnClimato = new javax.swing.JRadioButton();
        rdBtnGregorian = new javax.swing.JRadioButton();
        txtFieldDuration = new org.previmer.ichthyop.ui.param.JDurationTextField();
        txtFieldInitialTime = new org.previmer.ichthyop.ui.param.JDateTextField();
        txtFieldTimeStep = new javax.swing.JFormattedTextField();
        jLabel14 = new javax.swing.JLabel();
        txtFieldTimeOrigin = new javax.swing.JFormattedTextField();
        pnlParamDescription = new javax.swing.JPanel();
        lblParameter = new javax.swing.JLabel();

        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.previmer.ichthyop.ui.IchthyopApp.class).getContext().getResourceMap(TimeOptionPanel.class);
        pnlBlockInfo.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlBlockInfo.border.title"))); // NOI18N
        pnlBlockInfo.setName("pnlBlockInfo"); // NOI18N

        lblBlockInfo.setText(resourceMap.getString("lblBlockInfo.text")); // NOI18N
        lblBlockInfo.setName("lblBlockInfo"); // NOI18N

        javax.swing.GroupLayout pnlBlockInfoLayout = new javax.swing.GroupLayout(pnlBlockInfo);
        pnlBlockInfo.setLayout(pnlBlockInfoLayout);
        pnlBlockInfoLayout.setHorizontalGroup(
            pnlBlockInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBlockInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblBlockInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 695, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlBlockInfoLayout.setVerticalGroup(
            pnlBlockInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlBlockInfoLayout.createSequentialGroup()
                .addComponent(lblBlockInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setVisible(false);

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        btnRetrieveInfo.setText(resourceMap.getString("btnRetrieveInfo.text")); // NOI18N
        btnRetrieveInfo.setName("btnRetrieveInfo"); // NOI18N

        lblRetrieveInfo.setText(resourceMap.getString("lblRetrieveInfo.text")); // NOI18N
        lblRetrieveInfo.setName("lblRetrieveInfo"); // NOI18N

        txtFieldFirstTime.setEditable(false);
        txtFieldFirstTime.setName("txtFieldFirstTime"); // NOI18N

        txtFieldMaxDuration.setEditable(false);
        txtFieldMaxDuration.setName("txtFieldMaxDuration"); // NOI18N

        txtFieldDataTimeStep.setEditable(false);
        txtFieldDataTimeStep.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        txtFieldDataTimeStep.setName("txtFieldDataTimeStep"); // NOI18N

        txtFieldBestTimeStep.setEditable(false);
        txtFieldBestTimeStep.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        txtFieldBestTimeStep.setName("txtFieldBestTimeStep"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnRetrieveInfo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblRetrieveInfo))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtFieldFirstTime, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtFieldMaxDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtFieldDataTimeStep, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtFieldBestTimeStep, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel7)))))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRetrieveInfo)
                    .addComponent(lblRetrieveInfo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtFieldFirstTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtFieldMaxDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtFieldDataTimeStep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtFieldBestTimeStep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        buttonGroup1.add(rdBtnForward);
        rdBtnForward.setSelected(true);
        rdBtnForward.setText(resourceMap.getString("rdBtnForward.text")); // NOI18N
        rdBtnForward.setName("rdBtnForward"); // NOI18N
        rdBtnForward.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                rdBtnForwardMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                txtFieldTimeStepMouseExited(evt);
            }
        });

        buttonGroup1.add(rdBtnBackward);
        rdBtnBackward.setText(resourceMap.getString("rdBtnBackward.text")); // NOI18N
        rdBtnBackward.setName("rdBtnBackward"); // NOI18N
        rdBtnBackward.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                rdBtnForwardMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                txtFieldTimeStepMouseExited(evt);
            }
        });

        buttonGroup2.add(rdBtnClimato);
        rdBtnClimato.setSelected(true);
        rdBtnClimato.setText(resourceMap.getString("rdBtnClimato.text")); // NOI18N
        rdBtnClimato.setName("rdBtnClimato"); // NOI18N
        rdBtnClimato.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                rdBtnGregorianMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                txtFieldTimeStepMouseExited(evt);
            }
        });
        rdBtnClimato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdBtnClimatoActionPerformed(evt);
            }
        });

        buttonGroup2.add(rdBtnGregorian);
        rdBtnGregorian.setText(resourceMap.getString("rdBtnGregorian.text")); // NOI18N
        rdBtnGregorian.setName("rdBtnGregorian"); // NOI18N
        rdBtnGregorian.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                rdBtnGregorianMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                txtFieldTimeStepMouseExited(evt);
            }
        });
        rdBtnGregorian.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdBtnGregorianActionPerformed(evt);
            }
        });

        txtFieldDuration.setName("txtFieldDuration"); // NOI18N
        txtFieldDuration.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                txtFieldDurationMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                txtFieldTimeStepMouseExited(evt);
            }
        });

        txtFieldInitialTime.setName("txtFieldInitialTime"); // NOI18N
        txtFieldInitialTime.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                txtFieldInitialTimeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                txtFieldTimeStepMouseExited(evt);
            }
        });

        txtFieldTimeStep.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        txtFieldTimeStep.setName("txtFieldTimeStep"); // NOI18N
        txtFieldTimeStep.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                txtFieldTimeStepMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                txtFieldTimeStepMouseExited(evt);
            }
        });

        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        try {
            txtFieldTimeOrigin.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("####/##/## ##:##")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        txtFieldTimeOrigin.setText(resourceMap.getString("txtFieldTimeOrigin.text")); // NOI18N
        txtFieldTimeOrigin.setEnabled(false);
        txtFieldTimeOrigin.setName("txtFieldTimeOrigin"); // NOI18N
        txtFieldTimeOrigin.setValue("1900/01/01 00:00");
        txtFieldTimeOrigin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                txtFieldTimeOriginMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                txtFieldTimeStepMouseExited(evt);
            }
        });

        pnlParamDescription.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlParamDescription.border.title"))); // NOI18N
        pnlParamDescription.setName("pnlParamDescription"); // NOI18N

        lblParameter.setText(resourceMap.getString("lblParameter.text")); // NOI18N
        lblParameter.setName("lblParameter"); // NOI18N

        javax.swing.GroupLayout pnlParamDescriptionLayout = new javax.swing.GroupLayout(pnlParamDescription);
        pnlParamDescription.setLayout(pnlParamDescriptionLayout);
        pnlParamDescriptionLayout.setHorizontalGroup(
            pnlParamDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlParamDescriptionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblParameter, javax.swing.GroupLayout.DEFAULT_SIZE, 659, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlParamDescriptionLayout.setVerticalGroup(
            pnlParamDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlParamDescriptionLayout.createSequentialGroup()
                .addComponent(lblParameter, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlParamDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(jLabel11)
                            .addComponent(jLabel10)
                            .addComponent(jLabel12)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(rdBtnClimato)
                                .addGap(18, 18, 18)
                                .addComponent(rdBtnGregorian))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(rdBtnForward)
                                .addGap(18, 18, 18)
                                .addComponent(rdBtnBackward))
                            .addComponent(txtFieldTimeOrigin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtFieldInitialTime, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtFieldDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(txtFieldTimeStep, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel14)))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(rdBtnForward)
                    .addComponent(rdBtnBackward))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(rdBtnClimato)
                    .addComponent(rdBtnGregorian))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(txtFieldTimeOrigin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txtFieldInitialTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(txtFieldDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(txtFieldTimeStep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlParamDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlBlockInfo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlBlockInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rdBtnClimatoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdBtnClimatoActionPerformed
        // TODO add your handling code here:
        txtFieldInitialTime.setCalendar(new ClimatoCalendar());
        txtFieldFirstTime.setCalendar(new ClimatoCalendar());
        txtFieldTimeOrigin.setEnabled(false);
    }//GEN-LAST:event_rdBtnClimatoActionPerformed

    private void rdBtnGregorianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdBtnGregorianActionPerformed
        // TODO add your handling code here:
        txtFieldTimeOrigin.setEnabled(true);
        txtFieldInitialTime.setCalendar(new Calendar1900(txtFieldTimeOrigin.getText()));
        txtFieldFirstTime.setCalendar(new Calendar1900(txtFieldTimeOrigin.getText()));
    }//GEN-LAST:event_rdBtnGregorianActionPerformed

    private void rdBtnForwardMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rdBtnForwardMouseEntered
        // TODO add your handling code here:
        setParamInfo(getBlock().getXParameter("time_arrow"));
    }//GEN-LAST:event_rdBtnForwardMouseEntered

    private void txtFieldTimeOriginMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtFieldTimeOriginMouseEntered
        // TODO add your handling code here:
        setParamInfo(getBlock().getXParameter("time_origin"));

    }//GEN-LAST:event_txtFieldTimeOriginMouseEntered

    private void txtFieldInitialTimeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtFieldInitialTimeMouseEntered
        // TODO add your handling code here:
        setParamInfo(getBlock().getXParameter("initial_time"));
    }//GEN-LAST:event_txtFieldInitialTimeMouseEntered

    private void txtFieldDurationMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtFieldDurationMouseEntered
        // TODO add your handling code here:
        setParamInfo(getBlock().getXParameter("transport_duration"));
    }//GEN-LAST:event_txtFieldDurationMouseEntered

    private void txtFieldTimeStepMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtFieldTimeStepMouseEntered
        // TODO add your handling code here:
        setParamInfo(getBlock().getXParameter("time_step"));
    }//GEN-LAST:event_txtFieldTimeStepMouseEntered

    private void txtFieldTimeStepMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtFieldTimeStepMouseExited
        // TODO add your handling code here:
        setParamInfo(null);
    }//GEN-LAST:event_txtFieldTimeStepMouseExited

    private void rdBtnGregorianMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rdBtnGregorianMouseEntered
        // TODO add your handling code here:
        setParamInfo(getBlock().getXParameter("calendar_type"));
    }//GEN-LAST:event_rdBtnGregorianMouseEntered
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRetrieveInfo;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblBlockInfo;
    private javax.swing.JLabel lblParameter;
    private javax.swing.JLabel lblRetrieveInfo;
    private javax.swing.JPanel pnlBlockInfo;
    private javax.swing.JPanel pnlParamDescription;
    private javax.swing.JRadioButton rdBtnBackward;
    private javax.swing.JRadioButton rdBtnClimato;
    private javax.swing.JRadioButton rdBtnForward;
    private javax.swing.JRadioButton rdBtnGregorian;
    private javax.swing.JFormattedTextField txtFieldBestTimeStep;
    private javax.swing.JFormattedTextField txtFieldDataTimeStep;
    private org.previmer.ichthyop.ui.param.JDurationTextField txtFieldDuration;
    private org.previmer.ichthyop.ui.param.JDateTextField txtFieldFirstTime;
    private org.previmer.ichthyop.ui.param.JDateTextField txtFieldInitialTime;
    private org.previmer.ichthyop.ui.param.JDurationTextField txtFieldMaxDuration;
    private javax.swing.JFormattedTextField txtFieldTimeOrigin;
    private javax.swing.JFormattedTextField txtFieldTimeStep;
    // End of variables declaration//GEN-END:variables

    
}
