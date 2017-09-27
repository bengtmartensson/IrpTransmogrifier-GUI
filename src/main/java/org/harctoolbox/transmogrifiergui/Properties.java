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

import java.util.ArrayList;
import java.util.List;
import org.harctoolbox.analyze.Burst;
import org.harctoolbox.irp.BitDirection;
import org.harctoolbox.irp.IrpObject;

public class Properties {
    private static Properties instance = new Properties();

    public static Properties getInstance() {
        return instance;
    }

    private String encoding = "UTF-8";
    private boolean repeatFinder = true;
    private Double trailingGap = 20000d;
    private Double absoluteTolerance = 150d;
    private Double relativeTolerance = 0.2d;
    private Double frequency = 38000d;
    private boolean sorterOnRawTable = false;
    private Double maxRoundingError = Burst.Preferences.DEFAULTMAXROUNDINGERROR;
    private Double maxUnits = Burst.Preferences.DEFAULTMAXUNITS;
    private Double maxMicroSeconds = Burst.Preferences.DEFAULTMAXMICROSECONDS;
    private String timeBaseString = null;
    private BitDirection bitDirection = BitDirection.lsb;
    private boolean extent = false;
    private int maxParameterWidth = 8;
    private List<Integer> parameterWidths = new ArrayList<>(0);
    private boolean invert = false;
    private int radix = 16;
    private boolean usePeriods = false;
    private final boolean tsvOptimize = false;

    /**
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * @return the repeatFinder
     */
    public boolean isRepeatFinder() {
        return repeatFinder;
    }

    /**
     * @param repeatFinder the repeatFinder to set
     */
    public void setRepeatFinder(boolean repeatFinder) {
        this.repeatFinder = repeatFinder;
    }

    /**
     * @return the trailingGap
     */
    public Double getTrailingGap() {
        return trailingGap;
    }

    /**
     * @param trailingGap the trailingGap to set
     */
    public void setTrailingGap(Double trailingGap) {
        this.trailingGap = trailingGap;
    }

    /**
     * @return the absoluteTolerance
     */
    public Double getAbsoluteTolerance() {
        return absoluteTolerance;
    }

    /**
     * @param absoluteTolerance the absoluteTolerance to set
     */
    public void setAbsoluteTolerance(Double absoluteTolerance) {
        this.absoluteTolerance = absoluteTolerance;
    }

    /**
     * @return the relativeTolerance
     */
    public Double getRelativeTolerance() {
        return relativeTolerance;
    }

    /**
     * @param relativeTolerance the relativeTolerance to set
     */
    public void setRelativeTolerance(Double relativeTolerance) {
        this.relativeTolerance = relativeTolerance;
    }

    /**
     * @return the frequency
     */
    public Double getFrequency() {
        return frequency;
    }

    /**
     * @param frequency the frequency to set
     */
    public void setFrequency(Double frequency) {
        this.frequency = frequency;
    }

    /**
     * @return the sorterOnRawTabel
     */
    public boolean isSorterOnRawTable() {
        return sorterOnRawTable;
    }

    /**
     * @param sorterOnRawTable the sorterOnRawTabel to set
     */
    public void setSorterOnRawTable(boolean sorterOnRawTable) {
        this.sorterOnRawTable = sorterOnRawTable;
    }

    /**
     * @return the maxRoundingError
     */
    public Double getMaxRoundingError() {
        return maxRoundingError;
    }

    /**
     * @param maxRoundingError the maxRoundingError to set
     */
    public void setMaxRoundingError(Double maxRoundingError) {
        this.maxRoundingError = maxRoundingError;
    }

    /**
     * @return the maxUnits
     */
    public Double getMaxUnits() {
        return maxUnits;
    }

    /**
     * @param maxUnits the maxUnits to set
     */
    public void setMaxUnits(Double maxUnits) {
        this.maxUnits = maxUnits;
    }

    /**
     * @return the maxMicroSeconds
     */
    public Double getMaxMicroSeconds() {
        return maxMicroSeconds;
    }

    /**
     * @param maxMicroSeconds the maxMicroSeconds to set
     */
    public void setMaxMicroSeconds(Double maxMicroSeconds) {
        this.maxMicroSeconds = maxMicroSeconds;
    }

    /**
     * @return the extent
     */
    public boolean isExtent() {
        return extent;
    }

    /**
     * @param extent the extent to set
     */
    public void setExtent(boolean extent) {
        this.extent = extent;
    }

    /**
     * @return the parameterWidths
     */
    public List<Integer> getParameterWidths() {
        return parameterWidths;
    }

    /**
     * @param parameterWidths the parameterWidths to set
     */
    public void setParameterWidths(List<Integer> parameterWidths) {
        this.parameterWidths = parameterWidths;
    }

    /**
     * @return the invert
     */
    public boolean isInvert() {
        return invert;
    }

    /**
     * @param invert the invert to set
     */
    public void setInvert(boolean invert) {
        this.invert = invert;
    }

    /**
     * @return the bitDirection
     */
    public BitDirection getBitDirection() {
        return bitDirection;
    }

    /**
     * @param bitDirection the bitDirection to set
     */
    public void setBitDirection(BitDirection bitDirection) {
        this.bitDirection = bitDirection;
    }

    /**
     * @return the maxParameterWidth
     */
    public int getMaxParameterWidth() {
        return maxParameterWidth;
    }

    /**
     * @param maxParameterWidth the maxParameterWidth to set
     */
    public void setMaxParameterWidth(int maxParameterWidth) {
        this.maxParameterWidth = maxParameterWidth;
    }

    /**
     * @return the timeBaseString
     */
    public String getTimeBaseString() {
        return timeBaseString;
    }

    /**
     * @param timeBaseString the timeBaseString to set
     */
    public void setTimeBaseString(String timeBaseString) {
        this.timeBaseString = timeBaseString;
    }

    /**
     * @return the radix
     */
    public int getRadix() {
        return radix;
    }

    /**
     * @param radix the radix to set
     */
    public void setRadix(int radix) {
        this.radix = radix;
    }

    /**
     * @return the usePeriods
     */
    public boolean isUsePeriods() {
        return usePeriods;
    }

    /**
     * @param usePeriods the usePeriods to set
     */
    public void setUsePeriods(boolean usePeriods) {
        this.usePeriods = usePeriods;
    }

    /**
     * @return the tsvOptimize
     */
    public boolean isTsvOptimize() {
        return tsvOptimize;
    }
}
