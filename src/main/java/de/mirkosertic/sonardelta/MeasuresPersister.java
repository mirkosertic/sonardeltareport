package de.mirkosertic.sonardelta;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class MeasuresPersister {

    public static final Logger LOGGER = LoggerFactory.getLogger(MeasuresPersister.class);

    private static final MeasuresPersister PERSISTER = new MeasuresPersister();

    public static MeasuresPersister getInstance() {
        return PERSISTER;
    }

    public interface ValueExtractor {
        Object getFrom(MetricInformation aInformation);
    }

    private class MetricInformation {

        private String description;
        private Double beforeAnalysis;
        private Double afterAnalysis;
    }

    private final Map<String, MetricInformation> metricInformation;
    private Date analysisStart;
    private Date lastAnalysis;

    MeasuresPersister() {
        metricInformation = new HashMap<>();
    }

    private MetricInformation getOrCreateFor(String aKey) {
        MetricInformation theResult = metricInformation.get(aKey);
        if (theResult == null) {
            theResult = new MetricInformation();
            metricInformation.put(aKey, theResult);
        }
        return theResult;
    }

    public void registerMetricKeyWithDescription(String aMetricKey, String aDescription) {
        getOrCreateFor(aMetricKey).description = aDescription;
    }

    public void logBeforeAnalysis(String aMetricKey, Double aValue) {
        getOrCreateFor(aMetricKey).beforeAnalysis = aValue;
    }

    public void logAfterAnalysis(String aMetricKey, Double aValue) {
        getOrCreateFor(aMetricKey).afterAnalysis = aValue;
    }

    public void writeReportsTo(FileSystem aFileSystem, Settings aSettings) {
        File theWorkingDirectory = aFileSystem.workDir();
        LOGGER.info("Working directory is {}", theWorkingDirectory);

        File theBeforeReportFile = new File(theWorkingDirectory, aSettings.getString(Constants.KEY_BEFORE_ANALYSIS_REPORT_FILENAME));
        LOGGER.info("Writing before analysis metrics to {}", theBeforeReportFile);
        writeMapTo(theBeforeReportFile, new ValueExtractor() {
            @Override
            public Object getFrom(MetricInformation aInformation) {
                return aInformation.beforeAnalysis;
            }
        }, "");

        File theAfterReportFile = new File(theWorkingDirectory, aSettings.getString(Constants.KEY_AFTER_ANALYSIS_REPORT_FILENAME));
        LOGGER.info("Writing after analysis metrics to {}", theAfterReportFile);
        writeMapTo(theAfterReportFile, new ValueExtractor() {
            @Override
            public Object getFrom(MetricInformation aInformation) {
                return aInformation.afterAnalysis;
            }
        }, "");

        File theDescriptionsFile = new File(theWorkingDirectory, aSettings.getString(Constants.KEY_METRICS_DESCRIPTOR_FILENAME));
        LOGGER.info("Writing metrics descriptions to {}", theDescriptionsFile);
        writeMapTo(theDescriptionsFile, new ValueExtractor() {
            @Override
            public Object getFrom(MetricInformation aInformation) {
                return aInformation.description;
            }
        }, "");

        String theReportTemplate = aSettings.getString(Constants.KEY_SUMMARY);

        Configuration theConfiguration = new Configuration(Configuration.VERSION_2_3_22);
        theConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        theConfiguration.setLocale(new Locale(aSettings.getString(Constants.KEY_LOCALE)));

        File theSummaryFile = new File(theWorkingDirectory, aSettings.getString(Constants.KEY_SUMMARY_FILENAME));
        try (PrintWriter theWriter = new PrintWriter(new FileWriter(theSummaryFile))) {

            Template theTemplate = new Template("report", new StringReader(theReportTemplate),
                    theConfiguration);

            Map<String, Object> theDataToRender = new HashMap<>();
            for (Entry<String, MetricInformation> theEntry : metricInformation.entrySet()) {

                MetricInformation theInformation = theEntry.getValue();

                double theOld = toDefaultIfNull(theInformation.beforeAnalysis, 0d);
                double theNew = toDefaultIfNull(theInformation.afterAnalysis, 0d);

                Map<String, Object> theDelta = new HashMap<>();
                theDelta.put("absolute", theNew - theOld);

                Map<String, Object> theMetric = new HashMap<>();
                theMetric.put("description", toDefaultIfNull(theInformation.description, ""));
                theMetric.put("old", theOld);
                theMetric.put("new", theNew);
                theMetric.put("delta", theDelta);

                theDataToRender.put(theEntry.getKey(), theMetric);
            }

            if (lastAnalysis != null) {
                theDataToRender.put("lastAnalysis", lastAnalysis);
            }
            if (analysisStart != null) {
                theDataToRender.put("analysisStart", analysisStart);
            }

            StringWriter theStringWriter = new StringWriter();
            theTemplate.process(theDataToRender, theStringWriter);

            // We want unix file endings here...
            String theReport = theStringWriter.toString().replaceAll("\r\n", "\n");

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

    private void writeMapTo(File aFile, ValueExtractor aExtractor, String aPrefix) {
        try (PrintWriter theWriter = new PrintWriter(new FileWriter(aFile))) {
            for (Entry<String, MetricInformation> theEntry : metricInformation.entrySet()) {
                theWriter.print(aPrefix);
                theWriter.print(theEntry.getKey());
                theWriter.print("=");
                Object theValue = aExtractor.getFrom(theEntry.getValue());
                if (theValue != null) {
                    theWriter.println(theValue);
                } else {
                    theWriter.println();
                }
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