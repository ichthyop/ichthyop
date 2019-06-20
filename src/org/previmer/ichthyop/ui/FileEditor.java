/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import javax.swing.AbstractCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.JTable;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.previmer.ichthyop.io.IOTools;

public class FileEditor extends AbstractCellEditor
        implements TableCellEditor, ActionListener {

    private JFileChooser fileChooser;
    private JTextField textField = new JTextField();
    protected static final String EDIT = "edit";
    private JPanel panel;

    public FileEditor(int dialogType) {
        //Set up the editor (from the table's point of view),
        //which is a button.
        //This button brings up the color chooser dialog,
        //which is the editor from the user's point of view.
        panel = createEditorUI();
        //Set up the dialog that the button brings up.
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(dialogType);
    }

    private JPanel createEditorUI() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new GridBagLayout());
        textField = new JTextField();
        textField.setEditable(true);
        JButton btn = new JButton("...");
        btn.setBorderPainted(false);
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 10));
        btn.setActionCommand(EDIT);
        btn.addActionListener(this);
        pnl.add(textField, new GridBagConstraints(0, 0, 1, 1, 100, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        pnl.add(btn, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        return pnl;
    }

    /**
     * Handles events from the editor button and from
     * the dialog's OK button.
     */
    public void actionPerformed(ActionEvent e) {
        //The user has clicked the cell, so
        //bring up the dialog.
        String path = IOTools.resolveFile(textField.getText());
        fileChooser.setSelectedFile(new File(path));
        int answer = fileChooser.showOpenDialog(panel);
        if (answer == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filename = file.getAbsolutePath();
            if (file.isDirectory() && !filename.endsWith(File.separator)) {
                filename += File.separator;
            }
            textField.setText(filename);
            fireEditingStopped();
        }
    }

    //Implement the one CellEditor method that AbstractCellEditor doesn't.
    public Object getCellEditorValue() {
        return textField.getText();
    }

    //Implement the one method defined by TableCellEditor.
    public Component getTableCellEditorComponent(JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {
        textField.setText(value.toString());
        return panel;
    }
}
