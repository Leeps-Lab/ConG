/**
 * Copyright (c) 2012, University of California
 * All rights reserved.
 * 
 * Redistribution and use is governed by the LICENSE.txt file included with this
 * source code and available at http://leeps.ucsc.edu/cong/wiki/license
 **/

/*
 * PayoutTable.java
 *
 * Created on Jan 20, 2010, 12:30:45 PM
 */
package edu.ucsc.leeps.fire.server;

import java.awt.Dimension;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import javax.swing.JFileChooser;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author jpettit
 */
public class PayoutTable extends javax.swing.JDialog {

    /** Creates new form PayoutTable */
    public PayoutTable(Number showUp, Number threshold, Number rate, Collection<LocalClient> clients) {
        setModal(false);
        initComponents();

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setNumRows(clients.size());
        int row = 0;
        for (LocalClient client : clients) {
            model.setValueAt(client.name, row, 0);
            model.setValueAt(client.totalPoints, row, 1);
            model.setValueAt(threshold, row, 2);
            model.setValueAt(rate, row, 3);
            model.setValueAt(showUp, row, 4);
            model.setValueAt(
                    ((client.totalPoints - threshold.doubleValue())
                    * rate.doubleValue())
                    + showUp.doubleValue(),
                    row, 5);
            row++;
        }
        // hacks to make things layout correctly
        table.setMinimumSize(new Dimension(table.getPreferredSize().width, table.getRowCount() * table.getRowHeight()));
        table.setMaximumSize(table.getMinimumSize());
        jScrollPane1.setMinimumSize(new Dimension(table.getPreferredSize().width, (table.getRowCount() + 1) * table.getRowHeight() + 1));
        jScrollPane1.setMaximumSize(jScrollPane1.getMinimumSize());
        doLayout();
        pack();
        setMinimumSize(new Dimension(getWidth(), getHeight() + 10));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        printButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Subject", "Points", "Threshold", "Conversion", "Show-Up", "Payout"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(table);
        table.getColumnModel().getColumn(0).setResizable(false);
        table.getColumnModel().getColumn(1).setResizable(false);
        table.getColumnModel().getColumn(2).setResizable(false);
        table.getColumnModel().getColumn(3).setResizable(false);
        table.getColumnModel().getColumn(4).setResizable(false);
        table.getColumnModel().getColumn(5).setResizable(false);

        printButton.setText("Print");
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(298, 298, 298)
                .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 93, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(printButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 93, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(printButton)
                    .add(saveButton)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
        try {
            table.print();
        } catch (PrinterException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_printButtonActionPerformed

    public void saveTable(File file) {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(file));
            StringBuilder csvString = new StringBuilder();
            for (int col = 0; col < table.getColumnCount(); col++) {
                Object value = table.getModel().getColumnName(col);
                csvString.append(value.toString());
                if (col != table.getColumnCount() - 1) {
                    csvString.append(",");
                }
            }
            w.write(csvString.toString());
            w.newLine();
            for (int row = 0; row < table.getRowCount(); row++) {
                csvString = new StringBuilder();
                for (int col = 0; col < table.getColumnCount(); col++) {
                    Object value = table.getValueAt(row, col);
                    csvString.append(value.toString());
                    if (col != table.getColumnCount() - 1) {
                        csvString.append(",");
                    }
                }
                w.write(csvString.toString());
                w.newLine();
            }
            w.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().endsWith(".csv")) {
                f = new File(f.getAbsolutePath() + ".csv");
            }
            saveTable(f);
        }
    }//GEN-LAST:event_saveButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton printButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
