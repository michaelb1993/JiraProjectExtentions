package com.michabond.rest;


import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "issues")
@XmlAccessorType(XmlAccessType.FIELD)
public class IssueSearchResourceModel {

    @XmlElement
    private String key;

    @XmlElement
    private String summary;

    @XmlElement
    private String cf;

    public IssueSearchResourceModel() {
    }

    public IssueSearchResourceModel(Issue issue) {
        this.key = issue.getKey();
        this.summary = issue.getSummary();
        CustomField cf = ComponentAccessor.getCustomFieldManager().getCustomFieldObject("customfield_10000");
        String cf_value = String.valueOf(issue.getCustomFieldValue(cf));
    }

    /**
     * Key
     */

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Summary
     */

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Cf
     */

    public String getCf() {
        return cf;
    }

    public void setCf(String cf) {
        this.cf = cf;
    }
}
