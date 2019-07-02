package com.griddynamics.qa.sprimber.reporter.holder;

import com.griddynamics.qa.sprimber.reporter.service.ReportDescriptionService;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactories;

@SuppressWarnings("unused")
public class ReportDescriptionServiceHolder {

    private static List<ReportDescriptionService> reportDescriptionServices;

    public static void loadReportDescriptionServices(ClassLoader originClassLoader) {
        reportDescriptionServices = loadFactories(ReportDescriptionService.class, originClassLoader);
    }

    public static List<ReportDescriptionService> findReportDescriptionServices(String className) {
        return reportDescriptionServices.stream()
                .filter(service -> service.getSupportedClassNames().contains(className))
                .collect(toList());
    }

}
