package com.fiberlink.jira.plugin.customfields;

import com.atlassian.jira.issue.customfields.converters.StringConverter;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.customfields.impl.SelectCFType;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.issue.fields.config.FieldConfig;

import java.util.*;

import com.opensymphony.user.User;

import com.fiberlink.jira.plugin.workflow.utilities.utils.FiberlinkUtilities;

public class RestrictedSelectCFType extends SelectCFType {

    public RestrictedSelectCFType(CustomFieldValuePersister customFieldValuePersister, StringConverter stringConverter,
                                  SelectConverter selectConverter, OptionsManager optionsManager,
                                  GenericConfigManager genericConfigManager) {
        super(customFieldValuePersister, stringConverter, selectConverter, optionsManager, genericConfigManager);
    }

    public Map getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        Map m = super.getVelocityParameters(issue, field, fieldLayoutItem);
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getUser();
        m.put("editable", FiberlinkUtilities.isAllowedToSee("" + field.getIdAsLong(), user, issue, "edit"));
        m.put("viewable", FiberlinkUtilities.isAllowedToSee("" + field.getIdAsLong(), user, issue, "view"));
        return m;

    }

    public String getChangelogValue(CustomField field, Object value) {
        return null;
    }

    public String getChangelogString(CustomField field, Object value) {
        return null;
    }

    public Object getValueFromIssue(CustomField field, Issue issue) {

        String callingClassName = Thread.currentThread().getStackTrace()[4].getClassName();
        if (FiberlinkUtilities.isCalledByIndexer(callingClassName)) {
            return super.getValueFromIssue(field, issue);
        }


        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getUser();
        if (FiberlinkUtilities.isAllowedToSee("" + field.getIdAsLong(), user, issue, "view").booleanValue()) {
            return super.getValueFromIssue(field, issue);
        } else {
            return null;
        }
    }

    public void updateValue(CustomField customField, Issue issue, Object value) {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getUser();
        if (FiberlinkUtilities.isAllowedToSee("" + customField.getIdAsLong(), user, issue, "edit").booleanValue()) {
            if (value == null || !(((String) value).equals("p#w/B0uX2i5"))) {
                super.updateValue(customField, issue, value);
            }
            FiberlinkUtilities.updateIssueIndex(issue);
        }
    }

    public void validateFromParams(CustomFieldParams relevantParams, ErrorCollection errorCollectionToAddTo, FieldConfig config) {
        // No validation;
    }

    public void createValue(CustomField customField, Issue issue, Object value) {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getUser();
        if (FiberlinkUtilities.isAllowedToSee("" + customField.getIdAsLong(), user, issue, "edit").booleanValue()) {
            if (value == null || !(((String) value).equals("p#w/B0uX2i5"))) {
                super.createValue(customField, issue, value);
            }
            FiberlinkUtilities.updateIssueIndex(issue);
        }
    }
}
