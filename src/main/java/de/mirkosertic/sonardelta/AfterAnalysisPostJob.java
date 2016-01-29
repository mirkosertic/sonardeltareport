package de.mirkosertic.sonardelta;

import java.io.File;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.PostJob;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.MeasuresFilter;
import org.sonar.api.resources.Project;

@Phase(name = Phase.Name.POST)
public class AfterAnalysisPostJob implements PostJob {

    public static final Logger LOGGER = LoggerFactory.getLogger(AfterAnalysisPostJob.class);

    private static final class GetAllFilter implements MeasuresFilter<Collection<Measure>> {

        @Override
        public Collection<Measure> filter(Collection<Measure> aCollection) {
            return aCollection;
        }
    };

    private final FileSystem fileSystem;
    private final Settings settings;
    private final MeasuresPersister persister;

    public AfterAnalysisPostJob(Settings aSettings, FileSystem aFileSystem) {
        this(aSettings, aFileSystem, MeasuresPersister.getInstance());
    }

    AfterAnalysisPostJob(Settings aSettings, FileSystem aFileSystem, MeasuresPersister aPersister) {
        fileSystem = aFileSystem;
        settings = aSettings;
        persister = aPersister;
    }

    public void executeOn(Project aProject, SensorContext aSensorContext) {
        LOGGER.info("Collecting metrics after analysis");

        for (Measure theMeasure : aSensorContext.getMeasures(new GetAllFilter())) {

            String theKey = theMeasure.getMetricKey();

            persister.registerMetricKeyWithDescription(theKey, theMeasure.getMetric().getDescription());

            LOGGER.debug("Got new data for metric {}", theKey);

            Double theValue = theMeasure.getValue();

            persister.logAfterAnalysis(theKey, theValue);
        }

        persister.writeReportsTo(fileSystem, settings);
    }
}