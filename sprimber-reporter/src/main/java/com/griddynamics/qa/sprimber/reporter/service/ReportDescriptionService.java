package com.griddynamics.qa.sprimber.reporter.service;

import lombok.Synchronized;
import lombok.val;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class ReportDescriptionService {

    protected static final String REPORT_DESCRIPTION_MASK = "Assert that: %s, status: %s";
    protected Set<Object> processedDescriptionInfos = new CopyOnWriteArraySet<>();

    public abstract List<String> getSupportedClassNames();

    public abstract void sendReportDescription(Object descriptionInfo, String description, String status);

    @Synchronized
    protected boolean alreadyProcessedDescriptionInfo(Object descriptionInfo) {
        val processed = processedDescriptionInfos.stream()
                .anyMatch(info -> info == descriptionInfo);
        processedDescriptionInfos.add(descriptionInfo);
        return processed;
    }

}
