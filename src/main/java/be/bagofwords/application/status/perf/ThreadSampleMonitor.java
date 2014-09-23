package be.bagofwords.application.status.perf;

import be.bagofwords.application.ApplicationContextFactory;
import be.bagofwords.application.CloseableComponent;
import be.bagofwords.application.EnvironmentProperties;
import be.bagofwords.application.annotations.EagerBowComponent;
import be.bagofwords.counts.Counter;
import be.bagofwords.ui.UI;
import be.bagofwords.util.SafeThread;
import be.bagofwords.web.BaseController;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import spark.Request;
import spark.Response;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

@EagerBowComponent
public class ThreadSampleMonitor extends BaseController implements CloseableComponent {

    public static final int MAX_NUM_OF_SAMPLES = 10000;

    private final TraceSampler traceSampler;
    private final Counter<Trace> relevantTracesCounter;
    private final Counter<Trace> lessRelevantTracesCounter;

    private boolean saveThreadSamplesToFile;
    private String locationForSavedThreadSamples;
    private String applicationName;

    /**
     * Constructor to be used in spring context
     */

    @Autowired
    public ThreadSampleMonitor(EnvironmentProperties environmentProperties, ApplicationContextFactory applicationContextFactory) {
        this(environmentProperties.saveThreadSamplesToFile(), environmentProperties.getThreadSampleLocation(), applicationContextFactory.getApplicationName());
    }

    /**
     * Constructor to be used outside spring context
     *
     * @param saveThreadSamplesToFile
     * @param locationForSavedThreadSamples
     * @param applicationName
     */

    public ThreadSampleMonitor(boolean saveThreadSamplesToFile, String locationForSavedThreadSamples, String applicationName) {
        super("perf");
        this.saveThreadSamplesToFile = saveThreadSamplesToFile;
        this.locationForSavedThreadSamples = locationForSavedThreadSamples;
        this.applicationName = applicationName;
        this.relevantTracesCounter = new Counter<>();
        this.lessRelevantTracesCounter = new Counter<>();
        this.traceSampler = new TraceSampler();
        this.traceSampler.start();
    }

    @Override
    protected synchronized String handleRequest(Request request, Response response) throws Exception {
        StringBuilder result = new StringBuilder();
        synchronized (relevantTracesCounter) {
            synchronized (lessRelevantTracesCounter) {
                result.append("Collected " + relevantTracesCounter.getTotal() + " samples.");
                result.append("<h1>Relevant traces</h1><pre>");
                ThreadSamplesPrinter.printTopTraces(result, relevantTracesCounter);
                result.append("</pre>");
                result.append("<h1>Other traces</h1><pre>");
                ThreadSamplesPrinter.printTopTraces(result, lessRelevantTracesCounter);
                result.append("</pre>");
            }
        }
        return result.toString();
    }

    @Override
    public void close() {
        traceSampler.terminateAndWait();
        if (saveThreadSamplesToFile) {
            saveThreadSamplesToFile();
        }
    }

    private void saveThreadSamplesToFile() {
        try {
            synchronized (relevantTracesCounter) {
                synchronized (lessRelevantTracesCounter) {
                    File file = new File(locationForSavedThreadSamples + "_" + applicationName + "_" + System.currentTimeMillis() / (60 * 60 * 1000) + ".txt");
                    StringBuilder sb = new StringBuilder();
                    sb.append("Traces for " + applicationName + " on " + DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm") + "\n\n");
                    sb.append("-- Relevant traces --\n\n");
                    ThreadSamplesPrinter.printTopTraces(sb, relevantTracesCounter);
                    sb.append("\n\n-- Less relevant traces --\n\n");
                    ThreadSamplesPrinter.printTopTraces(sb, lessRelevantTracesCounter);
                    FileUtils.writeStringToFile(file, sb.toString());
                }
            }
        } catch (IOException exp) {
            UI.writeError("Failed to save thread samples!", exp);
        }
    }

    public void clearSamples() {
        synchronized (relevantTracesCounter) {
            relevantTracesCounter.clear();
        }
        synchronized (lessRelevantTracesCounter) {
            lessRelevantTracesCounter.clear();
        }
    }

    public Counter<Trace> getRelevantTracesCounter() {
        return relevantTracesCounter;
    }

    public Counter<Trace> getLessRelevantTracesCounter() {
        return lessRelevantTracesCounter;
    }

    private class TraceSampler extends SafeThread {

        public TraceSampler() {
            super("traceSampler", true);
        }

        @Override
        protected void runInt() throws Exception {
            while (!isTerminateRequested()) {
                Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
                for (Thread thread : stackTraces.keySet()) {
                    StackTraceElement[] thisTrace = stackTraces.get(thread);
                    String threadName = thread.getName();
                    if (!threadName.equals("traceSampler") && thisTrace.length > 0) {
                        String methodName = thisTrace[0].getMethodName();
                        boolean notRelevantThread = threadName.equals("SparkServerThread") || threadName.equals("Signal Dispatcher") || threadName.equals("Finalizer");
                        notRelevantThread |= threadName.equals("DateCache") || threadName.startsWith("qtp") || threadName.equals("Reference Handler") || threadName.startsWith("HashSessionScavenger");
                        notRelevantThread |= methodName.equals("accept0") || methodName.equals("accept") || methodName.equals("sleep") || methodName.equals("epollWait") || methodName.equals("socketAccept");
                        notRelevantThread |= threadName.equals("ChangedValueListener") && methodName.equals("socketRead0");
                        notRelevantThread |= inReadNextActionMethod(threadName, thisTrace);
                        Trace parent = null;
                        for (int i = thisTrace.length - 1; i >= 0; i--) {
                            StackTraceElement element = thisTrace[i];
                            Trace trace = new Trace(element.getClassName() + "." + element.getMethodName() + "(" + element.getFileName() + ":" + element.getLineNumber() + ")", parent);
                            if (notRelevantThread) {
                                synchronized (lessRelevantTracesCounter) {
                                    lessRelevantTracesCounter.inc(trace);
                                }
                            } else {
                                synchronized (relevantTracesCounter) {
                                    relevantTracesCounter.inc(trace);
                                }
                            }
                            parent = trace;
                        }
                    }
                }
                synchronized (relevantTracesCounter) {
                    relevantTracesCounter.trim(MAX_NUM_OF_SAMPLES / 2);
                }
                synchronized (lessRelevantTracesCounter) {
                    lessRelevantTracesCounter.trim(MAX_NUM_OF_SAMPLES / 2);
                }
                Thread.sleep(200);
            }
        }

        private boolean inReadNextActionMethod(String threadName, StackTraceElement[] thisTrace) {
            if (threadName.startsWith("DatabaseServerRequestHandler")) {
                for (StackTraceElement traceElement : thisTrace) {
                    if (traceElement.getMethodName().equals("readNextAction")) {
                        return true;
                    }
                }
            }
            return false;
        }

    }

}
