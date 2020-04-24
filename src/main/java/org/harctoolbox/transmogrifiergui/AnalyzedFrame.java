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

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.harctoolbox.analyze.Analyzer;
import org.harctoolbox.analyze.Burst;
import org.harctoolbox.analyze.NoDecoderMatchException;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.irp.BitCounter;
import org.harctoolbox.irp.Expression;
import org.harctoolbox.irp.NameEngine;
import org.harctoolbox.irp.NameUnassignedException;
import org.harctoolbox.irp.Protocol;

public class AnalyzedFrame extends javax.swing.JInternalFrame {

    private Analyzer analyzer;
    private Properties properties = Properties.getInstance();
    private boolean eliminateVars = true;
    private String source = null;

    /**
     * Creates new form AnalyzedFrame
     * @param source
     */
    public AnalyzedFrame(String source) {
        this.source = source;
        initComponents();
        setTitle(source + " [Analysis]");
    }

    public AnalyzedFrame(String source, Map<String, IrSequence> irSequences, double frequency) throws InvalidArgumentException {
        this(source);
        analyzer = new Analyzer(irSequences.values(), frequency, properties.isRepeatFinder(),
                properties.getAbsoluteTolerance(), properties.getRelativeTolerance());
        analyze(analyzer, irSequences.keySet());
    }

    public AnalyzedFrame() {
        this("unnamed");
    }

    private class FilteredStream extends FilterOutputStream {

        FilteredStream(OutputStream aStream) {
            super(aStream);
        }

        @Override
        public void write(byte b[]) throws IOException {
            String aString = new String(b, "US-ASCII");
            timingsTextArea.append(aString);
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            String aString = new String(b, off, len, "US-ASCII");
            timingsTextArea.append(aString);
            //consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
        }
    }

    private void analyze(Analyzer analyzer, Collection<String> names) {
        Burst.Preferences burstPrefs = new Burst.Preferences(properties.getMaxRoundingError(),
                properties.getMaxUnits(), properties.getMaxMicroSeconds());
        Analyzer.AnalyzerParams params = new Analyzer.AnalyzerParams(analyzer.getFrequency(), properties.getTimeBaseString(),
                properties.getBitDirection(),
                properties.isExtent(), properties.getParameterWidths(), properties.getMaxParameterWidth(), properties.isInvert(),
                burstPrefs, new ArrayList<>(0));
        //int maxNameLength = IrCoreUtils.maxLength(names);

        // TODO: Replace by tables
        try {
            PrintStream out = new PrintStream(new FilteredStream(new ByteArrayOutputStream()), false, "US-ASCII");
            analyzer.printStatistics(out, params);
        } catch (UnsupportedEncodingException ex) {
            throw new ThisCannotHappenException();
        }

        //IrCoreUtils.maxLength(names);
        int i = 0;
        for (String name : names) {
            //for (int i = 0; i < analyzer.getNoSequences(); i++) {
            //if (analyzer.getNoSequences() > 1)
            //    out.print("#" + i + ":\t");
            //cleanedSignalsTextArea.append(name + "\t" + analyzer.cleanedIrSequence(i).toString(true) + "\n");
            cleanedSignalsTextArea.append(name + "\t" + analyzer.repeatReducedIrSignal(i).toString(true) + "\n");
            codedSignalsTextArea.append(name + "\t" + analyzer.toTimingsString(i) + "\n");

            //}
            //*  if (properties.dumpRepeatfinder) {
            //for (int i = 0; i < analyzer.getNoSequences(); i++) {
            //  if (analyzer.getNoSequences() > 1)
            //      out.print("#" + i + ":\t");
            //System.out.println(analyzer.repeatReducedIrSignal(i).toString(true));
            repeatDataTextArea.append(name + "\t" + analyzer.repeatFinderData(i).toString() + "\n");
            i++;

        }

/*        if (properties.allDecodes) {
            List<List<Protocol>> protocols = analyzer.searchAllProtocols(params, properties.decoder, commandLineArgs.regexp);
            int noSignal = 0;
            for (List<Protocol> protocolList : protocols) {
                if (protocols.size() > 1)
                    out.print((names != null ? names[noSignal] : "#" + noSignal) + ":\t");
                if (properties.statistics)
                    out.println(analyzer.toTimingsString(noSignal));
                for (Protocol protocol : protocolList)
                    printAnalyzedProtocol(protocol, properties.radix, params.isPreferPeriods(), true);
                noSignal++;
            }
        } else {*/
        List<Protocol> protocols;
        try {
            protocols = analyzer.searchBestProtocol(params, null/*properties.decoder*/, false/*commandLineArgs.regexp*/);
        } catch (NoDecoderMatchException e) {
            throw new ThisCannotHappenException();
        }
        i = 0;
        for (String name : names) {
            Protocol protocol = protocols.get(i);
            //bestDecodeWithVarsTextArea.append(name + IrCoreUtils.spaces(maxNameLength - name.length() + 1));
            //bestDecodeWithoutVarsTextArea.append(name + IrCoreUtils.spaces(maxNameLength - name.length() + 1));
            //printAnalyzedProtocol(protocols.get(index), properties.radix, params.isPreferPeriods(), properties.statistics);
            //private void printAnalyzedProtocol(Protocol protocol, int radix, boolean usePeriods, boolean printWeight) {
            if (protocol != null) {
                bestDecodeWithVarsTextArea.append(name + "\t" + protocol.toIrpString(properties.getRadix(), properties.isUsePeriods(), properties.isTsvOptimize()) + "\n");
                Protocol variablelessProtocol = protocol.substituteConstantVariables();
                bestDecodeWithoutVarsTextArea.append(name + "\t" + variablelessProtocol.toIrpString(properties.getRadix(), properties.isUsePeriods(), properties.isTsvOptimize()) + "\n");
                //if (printWeight)
                //    out.println("weight = " + protocol.weight() + "\t" + protocol.getDecoderName());

                parameterUsageTextArea.append(name);
                NameEngine definitions = protocol.getDefinitions();
                for (Map.Entry<String, Expression> definition : definitions) {
                    try {
                        String definitionName = definition.getKey();
                        int length = protocol.guessParameterLength(definitionName);
                        long num = definition.getValue().toLong();
                        parameterUsageTextArea.append("\t" + IrCoreUtils.formatIntegerWithLeadingZeros(num, properties.getRadix(), length));
                    } catch (NameUnassignedException ex) {
                        throw new ThisCannotHappenException(ex);
                    }
                }
                parameterUsageTextArea.append("\n");

            }
            i++;
        }

        Map<String, BitCounter> bitStatistics = BitCounter.scrutinizeProtocols(protocols);
        bitStatistics.entrySet().forEach((kvp) -> {
            bitUsageTextArea.append(kvp.getKey() + "\t" + kvp.getValue().toString() + "\n");
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        analyzedPopupMenu = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        timingsPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        timingsTextArea = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        cleanedSignalsTextArea = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        codedSignalsTextArea = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        repeatDataTextArea = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jScrollPane5 = new javax.swing.JScrollPane();
        bestDecodeWithVarsTextArea = new javax.swing.JTextArea();
        jScrollPane6 = new javax.swing.JScrollPane();
        bestDecodeWithoutVarsTextArea = new javax.swing.JTextArea();
        jScrollPane7 = new javax.swing.JScrollPane();
        bitUsageTextArea = new javax.swing.JTextArea();
        jScrollPane8 = new javax.swing.JScrollPane();
        parameterUsageTextArea = new javax.swing.JTextArea();

        jMenuItem1.setText("jMenuItem1");
        analyzedPopupMenu.add(jMenuItem1);

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);

        jTabbedPane1.setComponentPopupMenu(analyzedPopupMenu);

        timingsPanel.setInheritsPopupMenu(true);

        jScrollPane2.setInheritsPopupMenu(true);

        timingsTextArea.setEditable(false);
        timingsTextArea.setColumns(20);
        timingsTextArea.setRows(5);
        timingsTextArea.setToolTipText("Tmings computed by the analyzer");
        timingsTextArea.setInheritsPopupMenu(true);
        jScrollPane2.setViewportView(timingsTextArea);

        javax.swing.GroupLayout timingsPanelLayout = new javax.swing.GroupLayout(timingsPanel);
        timingsPanel.setLayout(timingsPanelLayout);
        timingsPanelLayout.setHorizontalGroup(
            timingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 637, Short.MAX_VALUE)
            .addGroup(timingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING))
        );
        timingsPanelLayout.setVerticalGroup(
            timingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 197, Short.MAX_VALUE)
            .addGroup(timingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING))
        );

        jTabbedPane1.addTab("Timings", timingsPanel);

        cleanedSignalsTextArea.setColumns(20);
        cleanedSignalsTextArea.setRows(5);
        jScrollPane1.setViewportView(cleanedSignalsTextArea);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 637, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 637, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 197, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Cleaned signals", jPanel2);

        codedSignalsTextArea.setColumns(20);
        codedSignalsTextArea.setRows(5);
        jScrollPane3.setViewportView(codedSignalsTextArea);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 637, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 637, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 197, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Coded signals", jPanel3);

        repeatDataTextArea.setColumns(20);
        repeatDataTextArea.setRows(5);
        jScrollPane4.setViewportView(repeatDataTextArea);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 637, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 637, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 197, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("RepeatData", jPanel4);

        bestDecodeWithVarsTextArea.setColumns(20);
        bestDecodeWithVarsTextArea.setRows(5);
        jScrollPane5.setViewportView(bestDecodeWithVarsTextArea);

        jTabbedPane2.addTab("With variables", jScrollPane5);

        bestDecodeWithoutVarsTextArea.setColumns(20);
        bestDecodeWithoutVarsTextArea.setRows(5);
        jScrollPane6.setViewportView(bestDecodeWithoutVarsTextArea);

        jTabbedPane2.addTab("Without variables", jScrollPane6);

        bitUsageTextArea.setColumns(20);
        bitUsageTextArea.setRows(5);
        jScrollPane7.setViewportView(bitUsageTextArea);

        jTabbedPane2.addTab("Bit usage", jScrollPane7);

        parameterUsageTextArea.setColumns(20);
        parameterUsageTextArea.setRows(5);
        jScrollPane8.setViewportView(parameterUsageTextArea);

        jTabbedPane2.addTab("Parameter usage", jScrollPane8);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 637, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.Alignment.TRAILING))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 197, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.Alignment.TRAILING))
        );

        jTabbedPane1.addTab("\"Best\" decode", jPanel5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu analyzedPopupMenu;
    private javax.swing.JTextArea bestDecodeWithVarsTextArea;
    private javax.swing.JTextArea bestDecodeWithoutVarsTextArea;
    private javax.swing.JTextArea bitUsageTextArea;
    private javax.swing.JTextArea cleanedSignalsTextArea;
    private javax.swing.JTextArea codedSignalsTextArea;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTextArea parameterUsageTextArea;
    private javax.swing.JTextArea repeatDataTextArea;
    private javax.swing.JPanel timingsPanel;
    private javax.swing.JTextArea timingsTextArea;
    // End of variables declaration//GEN-END:variables
}
