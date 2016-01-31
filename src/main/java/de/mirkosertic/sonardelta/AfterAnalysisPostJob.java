package de.mirkosertic.sonardelta;

import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sonar.api.batch.Phase;
import org.sonar.api.batch.PostJob;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.MeasuresFilter;
import org.sonar.api.measures.RuleMeasure;
import org.sonar.api.resources.Project;

@Phase(name = Phase.Name.POST)
public class AfterAnalysisPostJob implements PostJob {

    public static final Logger LOGGER = LoggerFactory.getLogger(AfterAnalysisPostJob.class);

    private static final class GetOnlyDirectMeasures implements MeasuresFilter<Collection<Measure>> {

        @Override
        public Collection<Measure> filter(Collection<Measure> aCollection) {
            Collection<Measure> theResult = new HashSet<>();
            for (Measure theMeasure : aCollection) {
                if (!(theMeasure instanceof RuleMeasure)) {
                    theResult.add(theMeasure);
                }
            }
            return theResult;
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
        if (aProject.isRoot()) {
            LOGGER.info("Collecting metrics after analysis");

            for (Measure theMeasure : aSensorContext.getMeasures(aProject, new GetOnlyDirectMeasures())) {

                String theKey = theMeasure.getMetricKey();

                persister.registerMetricKeyWithDescription(theKey, theMeasure.getMetric().getDescription());

                LOGGER.debug("Got new data for metric {}", theKey);

                Double theValue = theMeasure.getValue();

                persister.logAfterAnalysis(theKey, theValue);
            }

            persister.writeReportsTo(fileSystem, settings);
        } else {
            LOGGER.info("Skipping report for {} as it is not the root", aProject);
        }
    }
}