package com.griddynamics.qa.sprimber.reporter.service.dev;

import com.griddynamics.qa.sprimber.reporter.service.ReportDescriptionService;
import lombok.val;

import java.util.List;

import static com.griddynamics.qa.sprimber.reporter.holder.ReportDescriptionHolder.getReportsByServiceClass;
import static com.griddynamics.qa.sprimber.reporter.holder.ReportDescriptionHolder.putReport;
import static com.griddynamics.qa.sprimber.reporter.resolver.assertj.AssertJReportByteCodeResolver.ASSERTJ_SUPPORTED_CLASS_NAMES;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static org.springframework.util.StringUtils.commaDelimitedListToSet;

public class DevConsoleReportDescriptionResolver extends ReportDescriptionService {

    private static final boolean DEV_ACTIVE = commaDelimitedListToSet(getProperty("spring.profiles.active"))
            .contains("sr_dev");

    public DevConsoleReportDescriptionResolver() {
        if (DEV_ACTIVE) {
            getRuntime().addShutdownHook(new Thread(this::printAllReports));
        }
    }

    @Override
    public List<String> getSupportedClassNames() {
        return ASSERTJ_SUPPORTED_CLASS_NAMES;
    }

    @Override
    public void sendReportDescription(Object descriptionInfo, String description, String status) {
        if (DEV_ACTIVE) {
            if (!alreadyProcessedDescriptionInfo(descriptionInfo)) {
                val report = format(REPORT_DESCRIPTION_MASK, description, status);
                putReport(getClass(), report);
                System.out.println(report);
            }
        }
    }

    private void printAllReports() {
        getReportsByServiceClass().forEach((service, reports) -> {
            System.out.println("\nReportDescriptionService: " + service);
            System.out.println("Reports:");
            reports.forEach(report -> System.out.print(report + "\n"));
        });
    }

}
