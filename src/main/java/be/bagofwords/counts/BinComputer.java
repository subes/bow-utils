package be.bagofwords.counts;

import be.bagofwords.logging.Log;
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
        if (bins.length == 0) {
            boolean allSameValue = false;
            double sameVal = -1;
            if (allValues.size() > 0) {
                allSameValue = true;
                sameVal = allValues.get(0);
                for (Double value : allValues) {
                    if (sameVal != value) {
                        allSameValue = false;
                        break;
                    }
                }
            }
            if (allSameValue) {
                Log.i("no bins (all values have value=" + sameVal + ") found " + counts[0] + " instances");
            } else {
                Log.i("no bins? found " + counts[0] + " instances");
            }
        } else {
            for (int i = 0; i < counts.length; i++) {
                if (i == 0)
                    Log.i("... to " + bins[i] + " has " + counts[i] + " instances.");
                else if (i == bins.length)
                    Log.i(bins[i - 1] + " to ... has " + counts[i] + " instances.");
                else
                    Log.i(bins[i - 1] + " to " + bins[i] + " has " + counts[i] + " instances.");
            }
        }
    }

    public ArrayList<Double> getAllValues() {
        return allValues;
    }

    public boolean acceptsMoreData() {
        return maxSize == -1 || maxSize > allValues.size();
    }

}
