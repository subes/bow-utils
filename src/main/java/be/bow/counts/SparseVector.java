package be.bow.counts;

import java.util.Arrays;

public class SparseVector {

    public int[] inds;
    public float[] vals;
    public int size;

    public SparseVector() {
        this.inds = new int[0];
        this.vals = new float[0];
        size = 0;
    }

    public void addValue(int ind, float value) {
        int currInd = -1;
        for (int i = 0; i < inds.length; i++)
            if (inds[i] == ind)
                currInd = i;
        if (currInd != -1)
            vals[currInd] += value;
        else {
            if (size >= inds.length) {
                inds = Arrays.copyOf(inds, inds.length * 2 + 1);
                vals = Arrays.copyOf(vals, vals.length * 2 + 1);
            }
            inds[size] = ind;
            vals[size] = value;
            size++;
        }
    }

    public double get(int ind) {
        for (int i = 0; i < inds.length; i++)
            if (inds[i] == ind)
                return vals[i];
        return 0.0;
    }

    public void trim(int newSize) {
        if (newSize < size) {
            float[] newVals = Arrays.copyOf(vals, size);
            Arrays.sort(newVals);
            int[] newInds = new int[newSize];
            float smallestVal = newVals[0];
            for (int i = 0; i < newSize; i++) {
                double val = newVals[newVals.length - i - 1];
                for (int j = 0; j < size; j++)
                    if (vals[j] == val) {
                        newInds[i] = inds[j];
                        vals[j] = smallestVal;
                    }
            }
            inds = newInds;
            vals = Arrays.copyOfRange(newVals, size - newSize, size);
            size = newSize;
        }
    }

    public String toString() {
        String result = "[ ";
        for (int i = 0; i < size; i++) {
            result += inds[i] + "=" + vals[i];
            if (i < size - 1)
                result += ", ";
        }
        result += "]";
        return result;
    }

    public void addValues(SparseVector vector) {
        for (int i = 0; i < vector.size; i++) {
            addValue(vector.inds[i], vector.vals[i]);
        }
    }
}
