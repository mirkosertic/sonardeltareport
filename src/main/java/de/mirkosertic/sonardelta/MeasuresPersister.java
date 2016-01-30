package de.mirkosertic.sonardelta;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.Map.Entry;

public class MeasuresPersister {

    public static final Logger LOGGER = LoggerFactory.getLogger(MeasuresPersister.class);

    private static final MeasuresPersister PERSISTER = new MeasuresPersister();

    public static MeasuresPersister getInstance() {
        return PERSISTER;
    }

    private final Map<String, Double> beforeAnalysis;
    private final Map<String, Double> afterAnalysis;
    private final Map<String, String> metricToDescription;
    private Date analysisStart;
    private Date lastAnalysis;

    MeasuresPersister() {
        beforeAnalysis = new HashMap<>();
        afterAnalysis = new HashMap<>();
        metricToDescription = new HashMap<>();
    }

    public void registerMetricKeyWithDescription(String aKey, String aDescription) {
        metricToDescription.put(aKey, aDescription);
    }

    public void logBeforeAnalysis(String aMetricKey, Double aValue) {
        if (aValue != null) {
            beforeAnalysis.put(aMetricKey, aValue);
        }
    }

    public void logAfterAnalysis(String aMetricKey, Double aValue) {
        if (aValue != null) {
            afterAnalysis.put(aMetricKey, aValue);
        }
    }

    public void writeReportsTo(FileSystem aFileSystem, Settings aSettings) {
        File theWorkingDirectory = aFileSystem.workDir();
        LOGGER.info("Working directory is {}", theWorkingDirectory);

        File theBeforeReportFile = new File(theWorkingDirectory, aSettings.getString(Constants.KEY_BEFORE_ANALYSIS_REPORT_FILENAME));
        LOGGER.info("Writing before analysis metrics to {}", theBeforeReportFile);
        writeMapTo(theBeforeReportFile, beforeAnalysis);

        File theAfterReportFile = new File(theWorkingDirectory, aSettings.getString(Constants.KEY_AFTER_ANALYSIS_REPORT_FILENAME));
        LOGGER.info("Writing after analysis metrics to {}", theAfterReportFile);
        writeMapTo(theAfterReportFile, afterAnalysis);

        File theDescriptionsFile = new File(theWorkingDirectory, aSettings.getString(Constants.KEY_METRICS_DESCRIPTOR_FILENAME));
        LOGGER.info("Writing metrics descriptions to {}", theDescriptionsFile);
        writeMapTo(theDescriptionsFile, metricToDescription);

        String theReportTemplate = aSettings.getString(Constants.KEY_SUMMARY);

        Configuration theConfiguration = new Configuration(Configuration.VERSION_2_3_22);
        theConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        theConfiguration.setLocale(new Locale(aSettings.getString(Constants.KEY_LOCALE)));

        File theSummaryFile = new File(theWorkingDirectory, aSettings.getString(Constants.KEY_SUMMARY_FILENAME));
        try (PrintWriter theWriter = new PrintWriter(new FileWriter(theSummaryFile))) {

            Template theTemplate = new Template("report", new StringReader(theReportTemplate),
                    theConfiguration);

            Map<String, Object> theDataToRender = new HashMap<>();
            Set<String> theMetricKeys = new HashSet<>();
            theMetricKeys.addAll(metricToDescription.keySet());
            theMetricKeys.addAll(beforeAnalysis.keySet());
            theMetricKeys.addAll(afterAnalysis.keySet());
            for (String theKey : theMetricKeys) {

                double theOld = toDefaultIfNull(beforeAnalysis.get(theKey), 0d);
                double theNew = toDefaultIfNull(afterAnalysis.get(theKey), 0d);

                Map<String, Object> theDelta = new HashMap<>();
                theDelta.put("absolute", theNew - theOld);

                Map<String, Object> theMetric = new HashMap<>();
                theMetric.put("description", toDefaultIfNull(metricToDescription.get(theKey), ""));
                theMetric.put("old", theOld);
                theMetric.put("new", theNew);
                theMetric.put("delta", theDelta);

                theDataToRender.put(theKey, theMetric);
            }

            if (lastAnalysis != null) {
                theDataToRender.put("lastAnalysis", lastAnalysis);
            }
            if (analysisStart != null) {
                theDataToRender.put("analysisStart", analysisStart);
            }

            StringWriter theStringWriter = new StringWriter();
            theTemplate.process(theDataToRender, theStringWriter);

            String theReport = theStringWriter.toString();

            LOGGER.info("============= Summary ===============");
            LOGGER.info(theReport);
            LOGGER.info("=====================================");

            LOGGER.info("Writing summary to {}", theSummaryFile);

            theWriter.print(theReport);
        } catch (Exception e) {
            //LOGGER.error("Error writing summary", e);
            throw new RuntimeException(e);
        }
    }

    private <T> T toDefaultIfNull(T aValue, T aDefault) {
        if (aValue == null) {
            return aDefault;
        }
        return aValue;
    }

    private <T> void writeMapTo(File aFile, Map<String, T> aValues) {
        try (PrintWriter theWriter = new PrintWriter(new FileWriter(aFile))) {
            for (Entry<String, T> theEntry : aValues.entrySet()) {
                theWriter.print(theEntry.getKey());
                theWriter.print("=");
                theWriter.println(theEntry.getValue());
            }
        } catch (Exception e) {
            LOGGER.error("Error writing to file", e);
        }
    }

    public void logAnalysisStart(Date aDate) {
        analysisStart = aDate;
    }

    public void logLastAnalysis(Date aDate) {
        lastAnalysis = aDate;
    }
}