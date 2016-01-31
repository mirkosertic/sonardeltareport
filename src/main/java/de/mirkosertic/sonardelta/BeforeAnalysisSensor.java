package de.mirkosertic.sonardelta;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sonar.api.batch.Phase;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Metric;
import org.sonar.wsclient.services.MetricQuery;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

@Phase(name = Phase.Name.PRE)
public class BeforeAnalysisSensor implements Sensor {

    public static final Logger LOGGER = LoggerFactory.getLogger(BeforeAnalysisSensor.class);

    private final Settings settings;
    private final MeasuresPersister persister;

    public BeforeAnalysisSensor(Settings aSettings) {
        this(aSettings, MeasuresPersister.getInstance());
    }

    BeforeAnalysisSensor(Settings aSettings, MeasuresPersister aPersister) {
        settings = aSettings;
        persister = aPersister;
    }

    @Override
    public void analyse(Project aProject, SensorContext aSensorContext) {

        LOGGER.info("Collecting metrics before analysis");

        String theSonarHostUrl = settings.getString(Constants.SONAR_HOST_URL);
        if (theSonarHostUrl.endsWith("/")) {
            theSonarHostUrl = theSonarHostUrl.substring(0, theSonarHostUrl.length() - 1);
        }
        String theUsername = settings.getString(Constants.SONAR_USERNAME);
        String thePassword = settings.getString(Constants.SONAR_PASSWORD);

        LOGGER.info("Connecting to {} with username = {}", theSonarHostUrl, theUsername);

        Sonar theSonar = Sonar.create(theSonarHostUrl, theUsername, thePassword);

        ResourceQuery theQuery = ResourceQuery.create(aProject.getKey());
        theQuery.setDepth(0);
        Resource theResource = theSonar.find(theQuery);

        persister.logAnalysisStart(new Date());

        if (theResource != null) {

            LOGGER.info("Data found for project key {}", aProject.getKey());

            persister.logLastAnalysis(theResource.getDate());

            Set<String> theMetricsKey = new HashSet<>();

            Map<String, String> theMetricsToDescription = new HashMap<>();

            MetricQuery theCollectMetricKeysQuery = MetricQuery.all();
            for (Metric theMetric : theSonar.findAll(theCollectMetricKeysQuery)) {
                theMetricsToDescription.put(theMetric.getKey(), theMetric.getDescription());
                theMetricsKey.add(theMetric.getKey());
            }

            ResourceQuery theMetricsQuery = ResourceQuery
                    .createForMetrics(aProject.getKey(), theMetricsKey.toArray(new String[theMetricsKey.size()]));
            Resource theMetrics = theSonar.find(theMetricsQuery);
            for (Measure theMeasure : theMetrics.getMeasures()) {

                String theMetricKey = theMeasure.getMetricKey();

                persister.registerMetricKeyWithDescription(theMetricKey, theMetricsToDescription.get(theMetricKey));

                LOGGER.debug("Found historic data for metric {}", theMetricKey);

                Double theValue = theMeasure.getValue();
                persister.logBeforeAnalysis(theMetricKey, theValue);
            }
        }
    }

    @Override
    public boolean shouldExecuteOnProject(Project aProject) {
        return aProject.isRoot();
    }
}