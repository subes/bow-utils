package be.bow.application.status.perf;

import be.bow.counts.Counter;
import be.bow.util.NumUtils;

import java.util.ArrayList;
import java.util.List;

public class ThreadSamplesPrinter {

    private static final double MIN_FRACTION = 0.001;

    public static void printTopTraces(StringBuilder result, Counter<Trace> traces) {
        List<Trace> sortedTraces = new ArrayList<>(traces.sortedKeys());
        double total = 0;
        for (Trace trace : sortedTraces) {
            if (trace.getParent() == null) {
                total += traces.get(trace);
            }
        }
        for (int i = 0; i < sortedTraces.size(); i++) {
            Trace parentTrace = sortedTraces.get(i);
            if (parentTrace.getParent() == null) {
                printTrace(0, "", result, parentTrace, traces, total, sortedTraces, false);
            }
        }
    }

    private static void printTrace(int level, String combinedIndentation, StringBuilder result, Trace parentTrace, Counter<Trace> traces, double total, List<Trace> sortedTraces, boolean printHorizontalLine) {
        double fraction = traces.get(parentTrace) / total;
        if (fraction > MIN_FRACTION) {
            String indentation = combinedIndentation + (printHorizontalLine ? "\\" : " ");
            int numOfChildren = countNumberOfChildren(parentTrace, traces, sortedTraces, total);
            result.append(indentation + NumUtils.makeNicePercent(fraction) + "% " + parentTrace.getLine() + "\n");
            //Add subtraces
            int numOfChildrenPrinted = 0;
            for (Trace subtrace : sortedTraces) {
                if (subtrace.getParent() != null && subtrace.getParent().equals(parentTrace)) {
                    char trackingLine = level % 2 == 0 ? '|' : '!';
                    printTrace(level + 1, combinedIndentation + " " + (numOfChildrenPrinted < numOfChildren - 1 ? " " + trackingLine : "  "), result, subtrace, traces, total, sortedTraces, numOfChildren > 0);
                    numOfChildrenPrinted++;
                }
            }
        }
    }

    private static int countNumberOfChildren(Trace parentTrace, Counter<Trace> traces, List<Trace> sortedTraces, double total) {
        int numOfChildren = 0;
        for (Trace subtrace : sortedTraces) {
            if (subtrace.getParent() != null && subtrace.getParent().equals(parentTrace)) {
                double fraction = traces.get(subtrace) / total;
                if (fraction > MIN_FRACTION) {
                    numOfChildren++;
                }
            }
        }
        return numOfChildren;
    }
}
