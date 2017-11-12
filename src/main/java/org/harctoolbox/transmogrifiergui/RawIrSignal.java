/*
Copyright (C) 2013 Bengt Martensson.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see http://www.gnu.org/licenses/.
*/

package org.harctoolbox.transmogrifiergui;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.OddSequenceLengthException;

/**
 * Note: Editing of the sequences is not implemented (yet).
 *
 */
public class RawIrSignal extends NamedIrSignal {

    private final static Logger logger = Logger.getLogger(TableInternalFrame.class.getName());

    //private static boolean generateCcf = true;
    //private static boolean decode = true;

//    /**
//     * @param aGenerateCcf the generateCcf to set
//     */
//    public static void setGenerateCcf(boolean aGenerateCcf) {
//        generateCcf = aGenerateCcf;
//    }

//    /**
//     * @param aDecode the decode to set
//     */
//    public static void setDecode(boolean aDecode) {
//        decode = aDecode;
//    }

    private IrSignal irSignal;
    //private String analyzerString;
    //private DecodeIR.DecodedSignal[] decodes;

    public RawIrSignal(IrSignal irSignal, String name/*, String comment, boolean invokeAnalyzer*/) {
        super(name/*, comment*/);
        this.irSignal = irSignal;
    }

//    public RawIrSignal(Command command, boolean invokeAnalyzer) throws IrpMasterException {
//        this(command.toIrSignal(), command.getName(), command.getComment(), invokeAnalyzer);
//    }

//    private void setIrSignal(IrSignal irSignal, boolean invokeAnalyzer) {
//        this.irSignal = irSignal;
//        decodes = DecodeIR.decode(irSignal);
//        if (invokeAnalyzer  && irSignal.getIntroLength() > 0) // Analyzer misbehaves on zero length signals, be careful.
//            analyzerString = ExchangeIR.newAnalyzer(irSignal).toString();
//    }
//
//    public Command toCommand() {
//        Command command = new Command(getName(), getComment(), irSignal);
//        return command;
//    }

    public IrSignal getIrSignal() {
        return irSignal;
    }

//    public DecodeIR.DecodedSignal getDecode(int i) {
//        return decodes[i];
//    }

//    public String getDecodeString() {
//        return DecodeIR.DecodedSignal.toPrintString(decodes, false);
//    }
//
//    public int getNoDecodes() {
//        return decodes.length;
//    }
//
//    public String getAnalyzerString() {
//        return analyzerString;
//    }

    public void setFrequency(double newFrequency) {
        irSignal = new IrSignal(irSignal.getIntroSequence(), irSignal.getRepeatSequence(), irSignal.getEndingSequence(), newFrequency, irSignal.getDutyCycle());
    }

    public void setIntroSequence(String str) throws OddSequenceLengthException {
        irSignal = new IrSignal(new IrSequence(str), irSignal.getRepeatSequence(), irSignal.getEndingSequence(), irSignal.getFrequency(), irSignal.getDutyCycle());
    }

    public void setRepeatSequence(String str) throws OddSequenceLengthException {
        irSignal = new IrSignal(irSignal.getIntroSequence(), new IrSequence(str), irSignal.getEndingSequence(), irSignal.getFrequency(), irSignal.getDutyCycle());
    }

    public void setEndingSequence(String str) throws OddSequenceLengthException {
        irSignal = new IrSignal(irSignal.getIntroSequence(), irSignal.getRepeatSequence(), new IrSequence(str), irSignal.getFrequency(), irSignal.getDutyCycle());
    }

    @Override
    public String csvString(String separator) {
        StringBuilder str = new StringBuilder(super.csvString(separator));
        str.append(irSignal.getFrequency()).append(separator);
        str.append(irSignal.getIntroSequence().toString(true)).append(separator); // FIXME
        str.append(irSignal.getRepeatSequence().toString(true)).append(separator);
        str.append(irSignal.getEndingSequence().toString(true)).append(separator);
        //str.append(DecodeIR.DecodedSignal.toPrintString(decodes, true));
        str.append(separator);
        //str.append(analyzerString).append(separator);
        return str.toString();
    }

    @Override
    public String toPrintString() {
        return csvString(", ");
    }

    private static class CapturedIrSignalColumns extends NamedIrSignal.AbstractColumnFunction {

        private static final int[] widths = {
            /*10, 40,*/50, 75, 75, 10, /*75,*/ /*75, 75,*/ 10, /*75,*/ /*40,*/ 10
        };
        private static final String[] columnNames = new String[] {
            /*"#", "Date",*/ "Name", "Intro", "Repetition", "Ending", /*"Name",*/ /*"Decode", "Analyze",*/ "Sel.", /*"Comment",*/ /*"Frequency",*/ "C. IrSignal"
        };
        private static final Class<?>[] classes = new Class<?>[] {
            /*Integer.class, String.class,*/ String.class, String.class, String.class, String.class, /*String.class, String.class,*/ Boolean.class, /*String.class,*/ /*Integer.class,*/ RawIrSignal.class
        };

        //public static final int posNumber = 0;
        //public static final int posDate = 1;
        public static final int POS_NAME = 0;
        public static final int POS_INTRO = 1;
        public static final int POS_REPETITION = 2;
        public static final int POS_ENDING = 3;
        public static final int POS_VERIFIED = 4;
        //public static final int posComment = 9;
        //public static final int posFrequency = 7;
        public static final int POS_CAPTUREDIRSIGNAL = columnNames.length - 1;

        CapturedIrSignalColumns() {
            super(columnNames, widths, classes, /*dummyArray,*/ 1);
        }

        @Override
        public int getPosName() {
            return POS_NAME;
        }

//        @Override
//        public int getPosComment() {
//            return posComment;
//        }

        @Override
        public int getPosIrSignal() {
            return POS_CAPTUREDIRSIGNAL;
        }

        @Override
        public boolean isEditable(int i) {
            return i < POS_VERIFIED;
            //return i > POS_ENDING;
        }

//        @Override
//        public int getPosDate() {
//            return posDate;
//        }
//
//        @Override
//        public int getPosNumber() {
//            return posNumber;
//        }

        @Override
        public int getPosVerified() {
            return POS_VERIFIED;
        }

        @Override
        public boolean uninterestingIfAllEqual(int column) {
            return super.uninterestingIfAllEqual(column) /* || column == posFrequency*/;
        }

        @Override
        public Object[] toObjectArray(NamedIrSignal signal) {
            if (!RawIrSignal.class.isInstance(signal))
                throw new IllegalArgumentException();
            return toObjectArray((RawIrSignal) signal);
        }

        public Object[] toObjectArray(RawIrSignal cir) {
            IrSignal irSignal = cir.getIrSignal();
            Object[] result = new Object[]{
                //cir.getNumeral(), cir.getDate(),
                cir.getName(),
                irSignal.getIntroSequence().toString(true, " ", "", ""),
                irSignal.getRepeatSequence().toString(true, " ", "", ""), irSignal.getEndingSequence().toString(true, " ", "", ""),
                /*cir.getDecodeString(), cir.getAnalyzerString(),*/ cir.getValidated(),
                /*cir.getComment(),*/ /*(int) ModulatedIrSequence.getFrequencyWithDefault(irSignal.getFrequency()),*/ cir/*, null*/};
            assert(result.length == columnNames.length);
            return result;
        }
    }

    public static class RawTableColumnModel extends NamedIrSignal.LearnedIrSignalTableColumnModel {
        public RawTableColumnModel() {
            super(new CapturedIrSignalColumns());
        }
    }

    public static class RawTableModel extends NamedIrSignal.LearnedIrSignalTableModel {
        public RawTableModel() {
            super(new CapturedIrSignalColumns());
        }

        public RawIrSignal getCapturedIrSignal(int row) {
            return validRow(row)
                    ? (RawIrSignal) getValueAt(row, CapturedIrSignalColumns.POS_CAPTUREDIRSIGNAL)
                    : null;
        }

//        @Override
//        public Command toCommand(int row) {
//            RawIrSignal rir = getCapturedIrSignal(row);
//            return rir.toCommand();
//        }

        @Override
        public void fireTableCellUpdated(int row, int column) {
            boolean invokeAnalyzer = true; // ???
            try {
                RawIrSignal rawIrSignal = getCapturedIrSignal(row);
                switch (column) {
                    case CapturedIrSignalColumns.POS_INTRO:
                        rawIrSignal.setIntroSequence((String) getValueAt(row, column));
                        break;
                    case CapturedIrSignalColumns.POS_REPETITION:
                        rawIrSignal.setRepeatSequence((String) getValueAt(row, column));
                        break;
                    case CapturedIrSignalColumns.POS_ENDING:
                        rawIrSignal.setEndingSequence((String) getValueAt(row, column));
                        break;
                    case CapturedIrSignalColumns.POS_VERIFIED:
                        rawIrSignal.setValidated((Boolean) getValueAt(row, column));
                        break;
                    case CapturedIrSignalColumns.POS_NAME:
                        rawIrSignal.setName((String) getValueAt(row, column));
                        break;
//                    case CapturedIrSignalColumns.posComment:
//                        rawIrSignal.setComment((String) getValueAt(row, column));
//                        break;
//                    case CapturedIrSignalColumns.posFrequency:
//                        rawIrSignal.setFrequency((Integer)getValueAt(row, column));
//                        break;
                    default:
                        throw new InternalError();
                }
            } catch (NumberFormatException | OddSequenceLengthException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public String toPrintString(int row) {
            RawIrSignal cir = getCapturedIrSignal(row);
            return super.toPrintString(row) + ": " + (cir != null ? cir.toPrintString() : "null");
        }

        @Override
        public String getType() {
            return "raw";
        }

        @Override
        public String normalize(String text, int c) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
