/*
 * Copyright (C) 2017 Bengt Martensson
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.harctoolbox.transmogrifiergui;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.irp.IctImporter;
import org.harctoolbox.irp.IrSequenceParsers;
import org.harctoolbox.irp.ThingsLineParser;

public class TableInternalFrame extends javax.swing.JInternalFrame {

    private final static Logger logger = Logger.getLogger(TableInternalFrame.class.getName());

    private static TableKit loadModSequences(Map<String, ModulatedIrSequence> modSequences) {
        RawIrSequence.RawTableModel rawTableModel = new RawIrSequence.RawTableModel();
        Double sum = 0.0;
        for (Map.Entry<String, ModulatedIrSequence> kvp : modSequences.entrySet()) {
            ModulatedIrSequence modSeq = kvp.getValue();
            Double freq = modSeq.getFrequency();
            if (sum != null)
                sum = freq != null ? sum + freq : null;
            IrSequence irSequence = new IrSequence(modSeq);
            RawIrSequence sequence = new RawIrSequence(irSequence, kvp.getKey());
            rawTableModel.addSequence(sequence);
        }
        Double average = sum == null ? null : sum / modSequences.size();
        return new TableKit(rawTableModel, new RawIrSequence.RawTableColumnModel(), average);
    }

    private static TableKit loadSequences(Map<String, IrSequence> signals) {
        RawIrSignal.RawTableModel rawTableModel = new RawIrSignal.RawTableModel();
        for (Map.Entry<String, IrSequence> kvp : signals.entrySet()) {
            IrSequence sequence = kvp.getValue();
            IrSignal irSignal = new IrSignal(sequence, null, null, null, null);
            RawIrSignal signal = new RawIrSignal(irSignal, kvp.getKey());
            rawTableModel.addSignal(signal);
        }
        return new TableKit(rawTableModel, new RawIrSignal.RawTableColumnModel());
    }

    private static class TableKit {

        private TableModel tableModel;
        private TableColumnModel tableColumnModel;
        private Double frequency;

        TableKit(TableModel tableModel, TableColumnModel tableColumnModel, Double frequency) {
            this.tableModel = tableModel;
            this.tableColumnModel = tableColumnModel;
            this.frequency = frequency;
        }

        TableKit() {
            this(new RawIrSignal.RawTableModel(), new RawIrSignal.RawTableColumnModel(), null);
        }

        TableKit(TableModel tableModel, TableColumnModel tableColumnModel) {
            this(tableModel, tableColumnModel, null);
        }

        /**
         * @return the tableModel
         */
        public TableModel getTableModel() {
            return tableModel;
        }

        /**
         * @return the tableColumnModel
         */
        public TableColumnModel getTableColumnModel() {
            return tableColumnModel;
        }

        /**
         * @return the frequency
         */
        public Double getFrequency() {
            return frequency;
        }
    }

    private TableModel tableModel;
    private TableColumnModel tableColumnModel;
    private Double frequency;

    //private RawIrSignal.RawTableModel rawTableModel;

    /**
     * Creates new form TableInternalFrame.
     * @param tableKit
     */
    private TableInternalFrame(TableKit tableKit, String title) {
        this.tableModel = tableKit.getTableModel();
        this.tableColumnModel = tableKit.getTableColumnModel();
        this.frequency = tableKit.getFrequency();
        initComponents();
        setTitle(title);
    }

    public TableInternalFrame() {
        this(new TableKit(), "Unnamed");
    }

    public TableInternalFrame(File importFile) throws IOException, InvalidArgumentException {
        this(loadFile(importFile), importFile.getName());
    }

    private static TableKit loadFile(File importFile) throws IOException, InvalidArgumentException {
        try {
            Map<String, ModulatedIrSequence> modSequences = IctImporter.parse(importFile.getCanonicalPath());
            if (modSequences.isEmpty())
                throw new InvalidArgumentException("No parseable sequences found.");
            return loadModSequences(modSequences);
        } catch (ParseException ex) {
            logger.log(Level.INFO, "Parsing of {0} as ict failed", importFile);
            ThingsLineParser<IrSequence> irSignalParser = new ThingsLineParser<>(
                    (List<String> line) -> {
                        return IrSequenceParsers.parseProntoOrRaw(line, Parameters.getTrailingGap());
                    }
            );
            Map<String, IrSequence> sequences = irSignalParser.readNamedThings(importFile.getCanonicalPath(), Parameters.getEncoding());
            if (sequences.isEmpty())
                throw new InvalidArgumentException("No parseable sequences found.");
            return loadSequences(sequences);
        }
    }

//    private void analyze(Map<String, ModulatedIrSequence> modulatedIrSequences) {
//        Map<String, IrSequence> irSequences = new LinkedHashMap<>(modulatedIrSequences.size());
//        irSequences.putAll(modulatedIrSequences);
//        double sum = 0.0;
//        for (ModulatedIrSequence mis : modulatedIrSequences.values())
//            sum += mis.getFrequency();
//
//        double frequency = sum / modulatedIrSequences.keySet().size();
//        analyze(irSequences, frequency);
//    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);

        table.setModel(tableModel);
        table.setColumnSelectionAllowed(true);
        jScrollPane1.setViewportView(table);
        table.setColumnModel(tableColumnModel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 795, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 795, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 144, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
