package com.griddynamics.qa.sprimber.reporter.template;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ReportMethodTemplates {

    public static final String PROCESS_REPORT_CODE_BLOCK_MASK =
            "{" +
                    "try {" +

                        // invoke origin method
                        "%s" +

                        //send report with 'success' status
                        "%s" +

                    "} catch (Throwable t) {" +

                        //send report with 'failed' status
                        "%s" +
                        "org.apache.commons.lang3.exception.ExceptionUtils.rethrow(t);" +
                    "}" +
            "}";

    public static final String ASSERTJ_SEND_REPORT_CODE_BLOCK_MASK =
            // List of ReportDescriptionService's
            "java.util.List reportDescriptionServices = " +
                    // find by target className
                    "com.griddynamics.qa.sprimber.reporter.holder.ReportDescriptionServiceHolder" +
                        ".findReportDescriptionServices(%s);" +

                    "for (int i = 0; i < reportDescriptionServices.size(); i++) {" +

                        "com.griddynamics.qa.sprimber.reporter.service.ReportDescriptionService service  = " +
                        // get target ReportDescriptionService
                        "(com.griddynamics.qa.sprimber.reporter.service.ReportDescriptionService)" +
                            "reportDescriptionServices.get(i);" +

                        // send description
                        "service.sendReportDescription($%s, java.lang.String.valueOf($%s.description()), %s);" +
                    "}";

}
