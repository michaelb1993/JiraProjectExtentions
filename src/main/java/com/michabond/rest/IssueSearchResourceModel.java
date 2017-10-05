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
    private String id;

    @XmlElement
    private String text;

    @XmlElement
    private String cf;

    public IssueSearchResourceModel() {
    }

    public IssueSearchResourceModel(Issue issue) {
        this.id = issue.getKey();
        this.text = issue.getSummary();
        CustomField cf = ComponentAccessor.getCustomFieldManager().getCustomFieldObject("customfield_10000");
        String cf_value = String.valueOf(issue.getCustomFieldValue(cf));
    }

    /**
     * Id
     */

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Text
     */

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
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
