package de.mirkosertic.sonardelta;

public interface Constants {

    String SONAR_USERNAME = "sonardelta.username";
    String SONAR_PASSWORD = "sonardelta.password";
    String SONAR_HOST_URL = "sonar.host.url";

    String DEFAULT_BEFORE_ANALYSIS_REPORT_FILENAME = "before_analysis_measures.txt";
    String KEY_BEFORE_ANALYSIS_REPORT_FILENAME = "sonardelta.beforeanalysisreportfilename";
    String DEFAULT_AFTER_ANALYSIS_REPORT_FILENAME = "after_analysis_measures.txt";
    String KEY_AFTER_ANALYSIS_REPORT_FILENAME = "sonardelta.afteranalysisreportfilename";

    String DEFAULT_METRICS_DESCRIPTOR_FILENAME = "metrics_descriptions.txt";
    String KEY_METRICS_DESCRIPTOR_FILENAME = "sonardelta.metricsdescriptionsfilename";

    String DEFAULT_SUMMARY_FILENAME = "summary.txt";
    String KEY_SUMMARY_FILENAME = "sonardelta.summaryfilename";

    String DEFAULT_SUMMARY = "${(coverage.description)!\"\"} went from ${(coverage.old)!0} to ${(coverage.new)!0} (${(coverage.delta.absolute?string[\"+########0.00;-########0.00\"])!0}).\n\n" +
                             "${ncloc.description} went from ${ncloc.old?string[\"########0\"]} to ${ncloc.new?string[\"########0\"]} (${ncloc.delta.absolute?string[\"+########0;-########0\"]}).\n\n" +
                             "* ${blocker_violations.description} went from ${blocker_violations.old} to ${blocker_violations.new} (${blocker_violations.delta.absolute?string[\"+########0;-########0\"]}).\n" +
                             "* ${critical_violations.description} went from ${critical_violations.old} to ${critical_violations.new} (${critical_violations.delta.absolute?string[\"+########0;-########0\"]}).\n" +
                             "* ${major_violations.description} went from ${major_violations.old} to ${major_violations.new} (${major_violations.delta.absolute?string[\"+########0;-########0\"]}).\n" +
                             "* ${minor_violations.description} went from ${minor_violations.old} to ${minor_violations.new} (${minor_violations.delta.absolute?string[\"+########0;-########0\"]}).\n" +
                             "* ${info_violations.description} went from ${info_violations.old} to ${info_violations.new} (${info_violations.delta.absolute?string[\"+########0;-########0\"]}).\n\n" +
                             "All values are relative to the last official analysis at ${lastAnalysis?datetime}";
    String KEY_SUMMARY = "sonardelta.summary";

    String KEY_LOCALE = "sonardelta.locale";
    String DEFAULT_LOCALE = "en";
}
