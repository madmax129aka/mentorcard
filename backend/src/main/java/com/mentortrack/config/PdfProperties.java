package com.mentortrack.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mentortrack.pdf")
public class PdfProperties {

    /** Classpath location of the real Mentor Card template, page 1 of 2. */
    private String templatePage1;

    /** Classpath location of the real Mentor Card template, page 2 of 2. */
    private String templatePage2;

    public String getTemplatePage1() {
        return templatePage1;
    }

    public void setTemplatePage1(String templatePage1) {
        this.templatePage1 = templatePage1;
    }

    public String getTemplatePage2() {
        return templatePage2;
    }

    public void setTemplatePage2(String templatePage2) {
        this.templatePage2 = templatePage2;
    }
}
