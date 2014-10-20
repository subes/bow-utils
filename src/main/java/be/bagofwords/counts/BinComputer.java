package be.bagofwords.counts;

import be.bagofwords.ui.UI;
import be.bagofwords.util.NumUtils;

import java.util.ArrayList;
import java.util.Collections;

public class BinComputer {

    private final ArrayList<Double> allValues;
    private final int maxSize;

    public BinComputer(int maxSize) {
        this.allValues = new ArrayList<>();
        this.maxSize = maxSize;
    }

    public void addCount(double count) {
        if (maxSize == -1 || allValues.size() < maxSize)
            allValues.add(count);
    }

    public synchronized double[] getEquiDenseBins(int size) {
        return NumUtils.getBorders(size, allValues);
    }

    public synchronized double[] getEquiWidthBins(int size) {
        Collections.sort(allValues);
        double min = allValues.get(0);
        double max = allValues.get(allValues.size() - 1);
        double binWidth = (max - min) / size;
        double[] borders = new double[size - 1];
        for (int i = 0; i < borders.length; i++) {
            borders[i] = min + (i + 1) * binWidth;
        }
        return borders;
    }

    public int[] getBinCounts(double[] bins) {
        int[] counts = new int[bins.length + 1];
        for (Double val : allValues) {
            int ind = NumUtils.getBin(bins, val);
            counts[ind]++;
        }
        return counts;
    }

    public void printBins(double[] bins) {
        int[] counts = getBinCounts(bins);
        for (int i = 0; i < counts.length; i++) {
            if (i == 0)
                UI.write("... to " + bins[i] + " has " + counts[i] + " instances.");
            else if (i == bins.length)
                UI.write(bins[i - 1] + " to ... has " + counts[i] + " instances.");
            else
                UI.write(bins[i - 1] + " to " + bins[i] + " has " + counts[i] + " instances.");
        }
    }

    public ArrayList<Double> getAllValues() {
        return allValues;
    }

    public boolean acceptsMoreData() {
        return maxSize == -1 || maxSize > allValues.size();
    }

}
