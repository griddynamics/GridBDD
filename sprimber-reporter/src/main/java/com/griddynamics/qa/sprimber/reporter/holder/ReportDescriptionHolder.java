package com.griddynamics.qa.sprimber.reporter.holder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("unused")
public class ReportDescriptionHolder {

    private static final Map<Class, List<String>> REPORTS_BY_SERVICE_CLASS = new ConcurrentHashMap<>();

    public static void putReport(Class<?> serviceClass, String report) {
        REPORTS_BY_SERVICE_CLASS.computeIfPresent(serviceClass, (service, reports) -> {
            reports.add(report);
            return reports;
        });
        REPORTS_BY_SERVICE_CLASS.putIfAbsent(serviceClass, newArrayList(report));
    }

    public static Map<Class, List<String>> getReportsByServiceClass() {
        return REPORTS_BY_SERVICE_CLASS;
    }

    public static List<String> getReports() {
        return REPORTS_BY_SERVICE_CLASS.values().stream()
                .flatMap(Collection::stream)
                .collect(toList());
    }

}
