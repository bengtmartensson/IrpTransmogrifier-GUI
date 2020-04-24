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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.harctoolbox.girr.Command;
import org.harctoolbox.guicomponents.CopyClipboardText;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.MultiParser;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.ThingsLineParser;
import org.harctoolbox.irp.IrpException;
import org.harctoolbox.irscrutinizer.importer.IctImporter;

public class TableInternalFrame extends javax.swing.JInternalFrame {

    private final static Logger logger = Logger.getLogger(TableInternalFrame.class.getName());
    private static Properties properties = Properties.getInstance();

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

    private static TableKit loadModSequences(Collection<Command> cmds) throws IrpException, IrCoreException {
        Map<String, ModulatedIrSequence> map = new HashMap<>(cmds.size());
        for (Command cmd : cmds)
            map.put(cmd.getName(), cmd.toIrSignal().toModulatedIrSequence());
        return loadModSequences(map);
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

    private final String source;

    private <T extends TableModel> void enableSorter(JTable table, boolean state) {
        @SuppressWarnings("unchecked")
        TableRowSorter<T> tableRowSorter = state ? new TableRowSorter<>((T) table.getModel()) : null;
        table.setRowSorter(tableRowSorter);
    }

    // If using sorter and deleting several rows, need to compute the to-be-removed model-indexes,
    // sort them, and remove them in descending order. I presently do not care enough...
    private static void deleteTableSelectedRows(JTable table) throws ErroneousSelectionException {
        barfIfNoneSelected(table);
        if (table.getRowSorter() != null && table.getSelectedRowCount() > 1) {
            logger.severe("Deleting several rows with enabled row sorter not yet implemented");
            return;
        }
        int row = table.getSelectedRow();

        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

        for (int i = table.getSelectedRowCount(); i > 0; i--)
            tableModel.removeRow(table.convertRowIndexToModel(row + i - 1));
    }

    private static void barfIfManySelected(JTable table) throws ErroneousSelectionException {
        if (table.getSelectedRowCount() > 1)
            throw new ErroneousSelectionException("Only one row may be selected");
    }

    private static void barfIfNoneSelected(JTable table) throws ErroneousSelectionException {
        if (table.getSelectedRow() == -1)
            throw new ErroneousSelectionException("No row selected");
    }

    private static void barfIfNotExactlyOneSelected(JTable table) throws ErroneousSelectionException {
        barfIfManySelected(table);
        barfIfNoneSelected(table);
    }

    private void printTableSelectedRow() throws ErroneousSelectionException {
        barfIfNotExactlyOneSelected(table);
        int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
        NamedIrSignal.LearnedIrSignalTableModel tableModel = (NamedIrSignal.LearnedIrSignalTableModel) table.getModel();
        String str = tableModel.toPrintString(modelRow);
        System.out.println(str);
    }

    public Map<String, IrSequence> getIrSequences() {
        LinkedHashMap<String, IrSequence> result = new LinkedHashMap<>(table.getRowCount());
        NamedIrSignal.LearnedIrSignalTableModel tableMdl = (NamedIrSignal.LearnedIrSignalTableModel) table.getModel();
        for (int i = 0; i < table.getRowCount(); i++) {
            int modelRow = table.convertRowIndexToModel(i);
            RawIrSequence rawIrSequence = ((RawIrSequence.RawTableModel) tableMdl).getCapturedIrSequence(modelRow);
            result.put(rawIrSequence.getName(), rawIrSequence.getIrSequence());
        }
        return result;
    }

//    public JTable getTable() {
//        return table;
//    }

    public void applyEdit(String str) {
        //NamedIrSignal.LearnedIrSignalTableModel tableModel = (NamedIrSignal.LearnedIrSignalTableModel) table.getModel();
        int row = table.getSelectedRow();
        int column = table.getSelectedColumn();
        if (row >= 0 && column >= 0) {
            int r = table.convertRowIndexToModel(row);
            int c = table.convertColumnIndexToModel(column);
            Class<?> clazz = tableModel.getColumnClass(column);
            Object thing = str.trim().isEmpty() ? null
                    : clazz == Integer.class ? Integer.parseInt(str)
                    : clazz == Boolean.class ? Boolean.parseBoolean(str)
                    : str;
            tableModel.setValueAt(thing, r, c);
            table.repaint();
        }
    }

    public void analyze() throws InvalidArgumentException {
        Map<String, IrSequence> irSequences = getIrSequences();
        AnalyzedFrame frame = new AnalyzedFrame(source, irSequences, frequency);
        Gui.getInstance().addInternalFrame(frame);
    }

    public String normalize(String text) throws OddSequenceLengthException {
        int row = table.getSelectedRow();
        int column = table.getSelectedColumn();
        if (row < 0 || column < 0)
            return "invalid";

        int c = table.convertColumnIndexToModel(column);
        return tableModel.normalize(text, c);
    }

    private static class TableKit {

        private NamedIrSignal.LearnedIrSignalTableModel tableModel;
        private NamedIrSignal.LearnedIrSignalTableColumnModel tableColumnModel;
        private Double frequency;

        TableKit(NamedIrSignal.LearnedIrSignalTableModel tableModel, NamedIrSignal.LearnedIrSignalTableColumnModel tableColumnModel, Double frequency) {
            this.tableModel = tableModel;
            this.tableColumnModel = tableColumnModel;
            this.frequency = frequency;
        }

        TableKit() {
            this(new RawIrSequence.RawTableModel(), new RawIrSequence.RawTableColumnModel(), null);
        }

        TableKit(NamedIrSignal.LearnedIrSignalTableModel tableModel, NamedIrSignal.LearnedIrSignalTableColumnModel tableColumnModel) {
            this(tableModel, tableColumnModel, null);
        }

        /**
         * @return the tableModel
         */
        public NamedIrSignal.LearnedIrSignalTableModel getTableModel() {
            return tableModel;
        }

        /**
         * @return the tableColumnModel
         */
        public NamedIrSignal.LearnedIrSignalTableColumnModel getTableColumnModel() {
            return tableColumnModel;
        }

        /**
         * @return the frequency
         */
        public Double getFrequency() {
            return frequency;
        }
    }

    private NamedIrSignal.LearnedIrSignalTableModel tableModel;
    private NamedIrSignal.LearnedIrSignalTableColumnModel tableColumnModel;
    private Double frequency;

    //private RawIrSignal.RawTableModel rawTableModel;

    /**
     * Creates new form TableInternalFrame.
     * @param tableKit
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    private TableInternalFrame(TableKit tableKit, String source) {
        this.source = source;
        this.tableModel = tableKit.getTableModel();
        this.tableColumnModel = tableKit.getTableColumnModel();
        this.frequency = tableKit.getFrequency();
        initComponents();
        setTitle(source + " [Raw sequences]");
    }

    public TableInternalFrame() {
        this(new TableKit(), "Unnamed");
    }

    public TableInternalFrame(File importFile) throws IOException, InvalidArgumentException {
        this(loadFile(importFile), importFile.getName());
    }

    private static TableKit loadFile(File importFile) throws IOException, InvalidArgumentException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(importFile), IrCoreUtils.DEFAULT_CHARSET))) {
            Collection<Command> cmds = IctImporter.importer(reader, importFile.getCanonicalPath());

            //Map<String, ModulatedIrSequence> modSequences = IctImporter.parse(importFile.getCanonicalPath());
            if (cmds.isEmpty())
                throw new InvalidArgumentException("No parseable sequences found.");
            return loadModSequences(cmds);
        } catch (IrpException | IrCoreException ex) {
            logger.log(Level.INFO, "Parsing of {0} as ict failed", importFile);
            ThingsLineParser<IrSequence> irSignalParser = new ThingsLineParser<>(
                    (List<String> line) -> {
                        return (MultiParser.newIrCoreParser(line)).toModulatedIrSequence(ModulatedIrSequence.DEFAULT_FREQUENCY, null); // FIXME
                    }, "#"
            );
            Map<String, IrSequence> sequences = irSignalParser.readNamedThings(importFile.getCanonicalPath(), properties.getEncoding());
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

        rawTablePopupMenu = new javax.swing.JPopupMenu();
        rawSorterCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator25 = new javax.swing.JPopupMenu.Separator();
        moveUpMenuItem = new javax.swing.JMenuItem();
        moveDownMenuItem = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JPopupMenu.Separator();
        addEmptySequenceMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        rawFromClipboardMenuItem = new javax.swing.JMenuItem();
        jSeparator18 = new javax.swing.JPopupMenu.Separator();
        plotMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        printTableRowMenuItem = new javax.swing.JMenuItem();
        jSeparator28 = new javax.swing.JPopupMenu.Separator();
        rawCopyAllMenuItem = new javax.swing.JMenuItem();
        rawCopySelectionMenuItem = new javax.swing.JMenuItem();
        jSeparator29 = new javax.swing.JPopupMenu.Separator();
        hideColumnMenuItem = new javax.swing.JMenuItem();
        resetRawTableColumnsMenuItem = new javax.swing.JMenuItem();
        removeUnusedMenuItem1 = new javax.swing.JMenuItem();
        hideUninterestingColumnsMenuItem1 = new javax.swing.JMenuItem();
        analyzeMenuItem = new javax.swing.JMenuItem();
        decodeItem = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();

        rawSorterCheckBoxMenuItem.setSelected(properties.isSorterOnRawTable());
        rawSorterCheckBoxMenuItem.setText("Enable sorter");
        rawSorterCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rawSorterCheckBoxMenuItemActionPerformed(evt);
            }
        });
        rawTablePopupMenu.add(rawSorterCheckBoxMenuItem);
        rawTablePopupMenu.add(jSeparator25);

        moveUpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, java.awt.event.InputEvent.CTRL_MASK));
        moveUpMenuItem.setMnemonic('U');
        moveUpMenuItem.setText("Move Up");
        moveUpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpMenuItemActionPerformed(evt);
            }
        });
        rawTablePopupMenu.add(moveUpMenuItem);

        moveDownMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, java.awt.event.InputEvent.CTRL_MASK));
        moveDownMenuItem.setMnemonic('D');
        moveDownMenuItem.setText("Move Down");
        moveDownMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownMenuItemActionPerformed(evt);
            }
        });
        rawTablePopupMenu.add(moveDownMenuItem);
        rawTablePopupMenu.add(jSeparator11);

        addEmptySequenceMenuItem.setText("Add empty sequence");
        addEmptySequenceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addEmptySequenceMenuItemActionPerformed(evt);
            }
        });
        rawTablePopupMenu.add(addEmptySequenceMenuItem);
        rawTablePopupMenu.add(jSeparator1);

        rawFromClipboardMenuItem.setText("Create signal from clipboard data");
        rawFromClipboardMenuItem.setEnabled(false);
        rawFromClipboardMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rawFromClipboardMenuItemActionPerformed(evt);
            }
        });
        rawTablePopupMenu.add(rawFromClipboardMenuItem);
        rawTablePopupMenu.add(jSeparator18);

        plotMenuItem.setText("Plot selected");
        plotMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plotMenuItemActionPerformed(evt);
            }
        });
        rawTablePopupMenu.add(plotMenuItem);

        deleteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        deleteMenuItem.setText("Delete selected");
        deleteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMenuItemActionPerformed(evt);
            }
        });
        rawTablePopupMenu.add(deleteMenuItem);

        printTableRowMenuItem.setText("Print selected to console");
        printTableRowMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printTableRowMenuItemActionPerformed(evt);
            }
        });
        rawTablePopupMenu.add(printTableRowMenuItem);
        rawTablePopupMenu.add(jSeparator28);

        rawCopyAllMenuItem.setText("Copy all to clipboard");
        rawCopyAllMenuItem.setToolTipText("Not yet element");
        rawCopyAllMenuItem.setEnabled(false);
        rawCopyAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rawCopyAllMenuItemActionPerformed(evt);
            }
        });
        rawTablePopupMenu.add(rawCopyAllMenuItem);

        rawCopySelectionMenuItem.setText("Copy selected to clipboard");
        rawCopySelectionMenuItem.setToolTipText("Not yet implemented");
        rawCopySelectionMenuItem.setEnabled(false);
        rawCopySelectionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rawCopySelectionMenuItemActionPerformed(evt);
            }
        });
        rawTablePopupMenu.add(rawCopySelectionMenuItem);
        rawTablePopupMenu.add(jSeparator29);

        hideColumnMenuItem.setText("Hide selected column");
        hideColumnMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideColumnMenuItemActionPerformed(evt);
            }
        });
        rawTablePopupMenu.add(hideColumnMenuItem);

        resetRawTableColumnsMenuItem.setText("Reset columns");
        resetRawTableColumnsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetRawTableColumnsMenuItemActionPerformed(evt);
            }
        });
        rawTablePopupMenu.add(resetRawTableColumnsMenuItem);

        removeUnusedMenuItem1.setText("Hide unused columns");
        removeUnusedMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeUnusedMenuItem1ActionPerformed(evt);
            }
        });
        rawTablePopupMenu.add(removeUnusedMenuItem1);

        hideUninterestingColumnsMenuItem1.setText("Hide uninteresting columns");
        hideUninterestingColumnsMenuItem1.setToolTipText("Hide the columns #, Date, as well as all columns with identical content.");
        hideUninterestingColumnsMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideUninterestingColumnsMenuItem1ActionPerformed(evt);
            }
        });
        rawTablePopupMenu.add(hideUninterestingColumnsMenuItem1);

        analyzeMenuItem.setText("Analyze");
        analyzeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                analyzeMenuItemActionPerformed(evt);
            }
        });
        rawTablePopupMenu.add(analyzeMenuItem);

        decodeItem.setText("Decode");
        decodeItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decodeItemActionPerformed(evt);
            }
        });
        rawTablePopupMenu.add(decodeItem);

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);

        jScrollPane1.setComponentPopupMenu(rawTablePopupMenu);

        table.setModel(tableModel);
        table.setColumnSelectionAllowed(true);
        table.setComponentPopupMenu(rawTablePopupMenu);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tableMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(table);
        table.setColumnModel(tableColumnModel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 990, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 990, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 144, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void rawSorterCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rawSorterCheckBoxMenuItemActionPerformed
        boolean state = rawSorterCheckBoxMenuItem.isSelected();
        properties.setSorterOnRawTable(state);
        enableSorter(table, state);
        moveDownMenuItem.setEnabled(!state);
        moveUpMenuItem.setEnabled(!state);
    }//GEN-LAST:event_rawSorterCheckBoxMenuItemActionPerformed

    private void moveUpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpMenuItemActionPerformed
        tableMoveSelection(table, true);
    }//GEN-LAST:event_moveUpMenuItemActionPerformed

    private void moveDownMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownMenuItemActionPerformed
        tableMoveSelection(table, false);
    }//GEN-LAST:event_moveDownMenuItemActionPerformed

    // Requires the row sorter to be disabled
    private void tableMoveSelection(JTable table, boolean up) {
        int row = table.getSelectedRow();
        int lastRow = row + table.getSelectedRowCount() - 1;

        if (row < 0) {
            logger.severe("No signal selected");
            return;
        }
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        if (up) {
            if (row == 0) {
                logger.severe("Cannot move up");
                return;
            }
        } else { // down
            if (lastRow >= tableModel.getRowCount() - 1) {
                logger.severe("Cannot move down");
                return;
            }
        }

        if (up) {
            tableModel.moveRow(row, lastRow, row - 1);
            table.addRowSelectionInterval(row - 1, row - 1);
            table.removeRowSelectionInterval(lastRow, lastRow);
        } else {
            tableModel.moveRow(row, lastRow, row + 1);
            table.addRowSelectionInterval(lastRow + 1, lastRow + 1);
            table.removeRowSelectionInterval(row, row);
        }
    }

    private void rawFromClipboardMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rawFromClipboardMenuItemActionPerformed
        String text = (new CopyClipboardText(null)).fromClipboard();

//        try {
//            IrSignal irSignal = InterpretStringHardware.interpretString(text, IrpUtils.defaultFrequency,
//                properties.getInvokeRepeatFinder(), properties.getInvokeCleaner(),
//                properties.getAbsoluteTolerance(), properties.getRelativeTolerance());
//            RawIrSignal rawIrSignal = new RawIrSignal(irSignal, "clipboard", "Signal read from clipboard", true);
//            registerRawCommand(rawIrSignal);
//        } catch (IrpMasterException ex) {
//            logger.severe(ex);;
//        }
    }//GEN-LAST:event_rawFromClipboardMenuItemActionPerformed

    private void plotMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plotMenuItemActionPerformed
//        RawIrSignal cir = table.get..getCapturedIrSignal(table.convertRowIndexToModel(table.getSelectedRow()));
//        scrutinizeIrSignal(cir.getIrSignal());
        logger.severe("Plot not yet implemented");
    }//GEN-LAST:event_plotMenuItemActionPerformed

    private void deleteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMenuItemActionPerformed
        try {
            deleteTableSelectedRows(table);
        } catch (ErroneousSelectionException ex) {
            logger.severe(ex.getLocalizedMessage());
        }
    }//GEN-LAST:event_deleteMenuItemActionPerformed

    private void printTableRowMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printTableRowMenuItemActionPerformed
        try {
            printTableSelectedRow();
        } catch (ErroneousSelectionException ex) {
            logger.severe(ex.getLocalizedMessage());
        }
    }//GEN-LAST:event_printTableRowMenuItemActionPerformed

    private void rawCopyAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rawCopyAllMenuItemActionPerformed
//        copyTableToClipboard(rawTable, false);
        logger.severe("Not yet implemented");
    }//GEN-LAST:event_rawCopyAllMenuItemActionPerformed

    private void rawCopySelectionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rawCopySelectionMenuItemActionPerformed
//        copyTableToClipboard(rawTable, true);
        logger.severe("Not yet implemented");
    }//GEN-LAST:event_rawCopySelectionMenuItemActionPerformed

    private void hideColumnMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideColumnMenuItemActionPerformed
//        int selectedColumn = rawTable.getSelectedColumn();
//        try {
//            rawTableColumnModel.removeColumn(selectedColumn);
//        } catch (ArrayIndexOutOfBoundsException ex) {
//            guiUtils.error(selectedColumn < 0 ? "No column selected." : "No column # " + selectedColumn + ".");
//        }
        logger.severe("Not yet implemented");
    }//GEN-LAST:event_hideColumnMenuItemActionPerformed

    private void resetRawTableColumnsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetRawTableColumnsMenuItemActionPerformed
        //tableColumnModel.reset();
        logger.severe("Not yet implemented");
    }//GEN-LAST:event_resetRawTableColumnsMenuItemActionPerformed

    private void removeUnusedMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeUnusedMenuItem1ActionPerformed
//        ArrayList<Integer>list = rawTableModel.getUnusedColumns();
//        rawTableColumnModel.removeColumns(list);
        logger.severe("Not yet implemented");
    }//GEN-LAST:event_removeUnusedMenuItem1ActionPerformed

    private void hideUninterestingColumnsMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideUninterestingColumnsMenuItem1ActionPerformed
//        ArrayList<Integer>list = rawTableModel.getUninterestingColumns();
//        rawTableColumnModel.removeColumns(list);
        logger.severe("Not yet implemented");
    }//GEN-LAST:event_hideUninterestingColumnsMenuItem1ActionPerformed

    private void tableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMousePressed

    }//GEN-LAST:event_tableMousePressed

    private void tableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseReleased
        int row = table.getSelectedRow();
        int column = table.getSelectedColumn();
        if (row >= 0 && column >= 0) {
            int rowModel = table.convertRowIndexToModel(row);
            int columnModel = table.convertColumnIndexToModel(column);
            Object thing = table.getModel().getValueAt(rowModel, columnModel);
            String presentContent = thing != null ? thing.toString() : null;
            Gui.getInstance().setEditClient(presentContent, this, tableModel.isCellEditable(rowModel, columnModel));
//            JTextField editingTextField = Gui.getInstance().getEditingTextField();
//            editingTextField.setText(thing != null ? thing.toString() : null);
//            editingTextField.setEditable(tableModel.isCellEditable(row, column));
//            //editingTextField.setEnabled(parameterTableModel.getColumnClass(column) != Boolean.class);
        }
    }//GEN-LAST:event_tableMouseReleased

    private void analyzeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_analyzeMenuItemActionPerformed
        try {
            analyze();
        } catch (InvalidArgumentException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_analyzeMenuItemActionPerformed

    private void decodeItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decodeItemActionPerformed

    }//GEN-LAST:event_decodeItemActionPerformed

    private void addEmptySequenceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEmptySequenceMenuItemActionPerformed
        RawIrSequence rawIrSequence = new RawIrSequence(new IrSequence(), "unnamed");
        tableModel.addSignal(rawIrSequence);
    }//GEN-LAST:event_addEmptySequenceMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem addEmptySequenceMenuItem;
    private javax.swing.JMenuItem analyzeMenuItem;
    private javax.swing.JMenuItem decodeItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenuItem hideColumnMenuItem;
    private javax.swing.JMenuItem hideUninterestingColumnsMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator11;
    private javax.swing.JPopupMenu.Separator jSeparator18;
    private javax.swing.JPopupMenu.Separator jSeparator25;
    private javax.swing.JPopupMenu.Separator jSeparator28;
    private javax.swing.JPopupMenu.Separator jSeparator29;
    private javax.swing.JMenuItem moveDownMenuItem;
    private javax.swing.JMenuItem moveUpMenuItem;
    private javax.swing.JMenuItem plotMenuItem;
    private javax.swing.JMenuItem printTableRowMenuItem;
    private javax.swing.JMenuItem rawCopyAllMenuItem;
    private javax.swing.JMenuItem rawCopySelectionMenuItem;
    private javax.swing.JMenuItem rawFromClipboardMenuItem;
    private javax.swing.JCheckBoxMenuItem rawSorterCheckBoxMenuItem;
    private javax.swing.JPopupMenu rawTablePopupMenu;
    private javax.swing.JMenuItem removeUnusedMenuItem1;
    private javax.swing.JMenuItem resetRawTableColumnsMenuItem;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables

    private static class ErroneousSelectionException extends Exception {

        ErroneousSelectionException(String msg) {
            super(msg);
        }
    }
}
