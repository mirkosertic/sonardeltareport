package de.mirkosertic.sonardelta;

import java.util.List;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import com.google.common.collect.ImmutableList;

/**
 * A Simple plugin to collect all measurement changes during a sonar build.
 */
@Properties({
        @Property(
                key = Constants.KEY_BEFORE_ANALYSIS_REPORT_FILENAME,
                name = "Name of the report file to write metrics to before analysis starts",
                defaultValue = Constants.DEFAULT_BEFORE_ANALYSIS_REPORT_FILENAME,
                global = true,
                project = true,
                module = true,
                type = PropertyType.STRING
        ),
        @Property(
                key = Constants.KEY_AFTER_ANALYSIS_REPORT_FILENAME,
                name = "Name of the report file to write metrics to after analysis finishes",
                defaultValue = Constants.DEFAULT_AFTER_ANALYSIS_REPORT_FILENAME,
                global = true,
                project = true,
                module = true,
                type = PropertyType.STRING
        ),
        @Property(
                key = Constants.KEY_METRICS_DESCRIPTOR_FILENAME,
                name = "Name of the report file to write metric descriptions",
                defaultValue = Constants.DEFAULT_METRICS_DESCRIPTOR_FILENAME,
                global = true,
                project = true,
                module = true,
                type = PropertyType.STRING
        ),
        @Property(
                key = Constants.KEY_SUMMARY_FILENAME,
                name = "Filename for the summary",
                defaultValue = Constants.DEFAULT_SUMMARY_FILENAME,
                global = true,
                project = true,
                module = true,
                type = PropertyType.STRING
        ),
        @Property(
                key = Constants.KEY_SUMMARY,
                name = "Text for summary generation",
                defaultValue = Constants.DEFAULT_SUMMARY,
                global = true,
                project = true,
                module = true,
                type = PropertyType.TEXT
        ),
        @Property(
                key = Constants.SONAR_USERNAME,
                name = "Username to connect to the Sonar Rest API",
                defaultValue = "",
                global = true,
                project = true,
                module = true,
                type = PropertyType.STRING
        ),
        @Property(
                key = Constants.SONAR_PASSWORD,
                name = "Password to connect to the Sonar Rest API",
                defaultValue = "",
                global = true,
                project = true,
                module = true,
                type = PropertyType.STRING
        )
}
)
public class Plugin extends SonarPlugin {

    public List getExtensions() {
        return ImmutableList.of(BeforeAnalysisSensor.class, AfterAnalysisPostJob.class);
    }
}