package com.michabond.rest;


import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement(name = "issues")
@XmlAccessorType(XmlAccessType.FIELD)
public class IssueSearchResourceModel {

    private static final String[] CF_ARRAY = {
            "customfield_10000",
            "customfield_10001",
            "customfield_10002",
            "customfield_10003",
            "customfield_10004"
    };
    @XmlElement
    private String id;

    @XmlElement
    private String text;

    @XmlElement
    private Map<String, Object> args;

    public IssueSearchResourceModel() {
    }

    public IssueSearchResourceModel(Issue issue) {
        this.id = issue.getKey();
        this.text = issue.getSummary();
        this.args = new HashMap<>();
        for (String cfKey : CF_ARRAY) {
            CustomField cf = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(cfKey);
            String cfName = String.valueOf(cf);
            String cfValue = String.valueOf(issue.getCustomFieldValue(cf));
            this.args.put(cfName, cfValue);
        }
        this.args.put("str", "this is a string");
        this.args.put("int", 3);
        this.args.put("arr", new Object[] {1, 2.3, true, "well done"});
        Map<String, Object> subMap = new HashMap<>();
        subMap.put("arr", new Object[] {0, false, "inception"});
        this.args.put("subMap", subMap);
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

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }

}
