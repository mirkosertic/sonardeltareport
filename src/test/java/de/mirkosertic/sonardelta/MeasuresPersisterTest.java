package de.mirkosertic.sonardelta;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MeasuresPersisterTest {

    @Test
    public void testDefaultTemplate() throws IOException {
        MeasuresPersister thePersister = new MeasuresPersister();
        thePersister.registerMetricKeyWithDescription("coverage", "Code-Coverage");
        thePersister.logBeforeAnalysis("coverage", 1d);
        thePersister.logAfterAnalysis("coverage", 2d);

        thePersister.registerMetricKeyWithDescription("ncloc", "NCLOG");
        thePersister.logBeforeAnalysis("ncloc", 3d);
        thePersister.logAfterAnalysis("ncloc", 3d);

        thePersister.registerMetricKeyWithDescription("blocker_violations", "Blocker");
        thePersister.logBeforeAnalysis("blocker_violations", 5d);
        thePersister.logAfterAnalysis("blocker_violations", 6d);

        thePersister.registerMetricKeyWithDescription("critical_violations", "Critical");
        thePersister.logBeforeAnalysis("critical_violations", 7d);
        thePersister.logAfterAnalysis("critical_violations", 8d);

        thePersister.registerMetricKeyWithDescription("major_violations", "Major");
        thePersister.logBeforeAnalysis("major_violations", 9d);
        thePersister.logAfterAnalysis("major_violations", 10d);

        thePersister.registerMetricKeyWithDescription("minor_violations", "Minor");
        thePersister.logBeforeAnalysis("minor_violations", 11d);
        thePersister.logAfterAnalysis("minor_violations", 12d);

        thePersister.registerMetricKeyWithDescription("info_violations", "Info");
        thePersister.logBeforeAnalysis("info_violations", 14d);
        thePersister.logAfterAnalysis("info_violations", 13d);

        Settings theSettings = mock(Settings.class);
        when(theSettings.getString(eq(Constants.KEY_BEFORE_ANALYSIS_REPORT_FILENAME))).thenReturn(Constants.DEFAULT_BEFORE_ANALYSIS_REPORT_FILENAME);
        when(theSettings.getString(eq(Constants.KEY_AFTER_ANALYSIS_REPORT_FILENAME))).thenReturn(Constants.DEFAULT_AFTER_ANALYSIS_REPORT_FILENAME);
        when(theSettings.getString(eq(Constants.KEY_METRICS_DESCRIPTOR_FILENAME))).thenReturn(Constants.DEFAULT_METRICS_DESCRIPTOR_FILENAME);
        when(theSettings.getString(eq(Constants.KEY_SUMMARY_FILENAME))).thenReturn(Constants.DEFAULT_SUMMARY_FILENAME);
        when(theSettings.getString(eq(Constants.KEY_SUMMARY))).thenReturn(Constants.DEFAULT_SUMMARY);

        FileSystem theFilesystem = mock(FileSystem.class);

        Path theTempDirectory = Files.createTempDirectory("junit");

        when(theFilesystem.workDir()).thenReturn(theTempDirectory.toFile());

        thePersister.writeReportsTo(theFilesystem, theSettings);

        try (Reader theReader = new FileReader(new File(theTempDirectory.toFile(), Constants.DEFAULT_SUMMARY_FILENAME))) {
            String theContent = IOUtils.toString(theReader);

            assertEquals("Code-Coverage went from 1 to 2 (+1.00).\n"
                    + "\n"
                    + "NCLOG went from 3 to 3 (+0).\n"
                    + "\n"
                    + "\n"
                    + "Blocker went from 5 to 6 (+1).\n"
                    + "Critical went from 7 to 8 (+1).\n"
                    + "Major went from 9 to 10 (+1).\n"
                    + "Minor went from 11 to 12 (+1).\n"
                    + "Info went from 14 to 13 (-1).\n", theContent);
        }
    }
}