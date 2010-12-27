package com.fiberlink.jira.plugin.workflow.utilities.utils;

import com.atlassian.core.user.GroupUtils;
import com.atlassian.jira.security.util.GroupSelectorUtils;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.fiberlink.jira.plugin.customfields.RestrictedNumberCFType;

import java.util.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FiberlinkUtilities {

    public static Boolean isAllowedToSee(String fieldID, User user, Issue issue, String screen) {
        if (user == null) {
            return true;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    System.getProperty("catalina.home") + "/field-configs/configuration.txt"));
            String str = null;

            while ((str = br.readLine()) != null) {
                if (str.startsWith("'")) {
                    continue;
                }
                if (str.indexOf(fieldID + ":" + "all:") != -1) {
                    br.close();
                    return bValidate(str, user, issue);
                }
                if (str.indexOf(fieldID + ":" + screen + ":") != -1) {
                    br.close();
                    return bValidate(str, user, issue);
                }
            }
            br.close();
            return false;
        } catch (Exception e) {
            System.out.println("Exception in isAllowed");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean bValidate(String str, User user, Issue issue) {
        StringTokenizer stkMain = new StringTokenizer(str, ":");
        if (stkMain.hasMoreTokens()) {
            stkMain.nextToken();
        }
        if (stkMain.hasMoreTokens()) {
            stkMain.nextToken();
        }

        if (!stkMain.hasMoreTokens()) {
            return true;
        }

        String[] params = stkMain.nextToken().split(";", 15);

        //code for groups
        StringTokenizer stk = new StringTokenizer(params[0], ",");
        Group gtmp;
        GroupUtils groupUtils = new GroupUtils();

        while (stk.hasMoreTokens()) {
            gtmp = groupUtils.getGroup(stk.nextToken().trim());
            if (gtmp.containsUser(user)) {
                return true;
            }
        }

        //code for specific users
        stk = new StringTokenizer(params[1], ",");

        while (stk.hasMoreTokens()) {
            if (stk.nextToken().trim().equals(user.getName())) {
                return true;
            }
        }

        if (issue == null) {
            return true;
        }

        //code for project roles.
        stk = new StringTokenizer(params[2], ",");
        Project project = issue.getProjectObject();
        ProjectRoleManager projectRoleManager = (ProjectRoleManager) ComponentManager.
                getComponentInstanceOfType(ProjectRoleManager.class);
        Long projectRoleId = null;
        ProjectRole projectRole = null;
        while (stk.hasMoreTokens()) {
            projectRoleId = new Long(stk.nextToken().trim());
            projectRole = projectRoleManager.getProjectRole(projectRoleId);
            if (projectRole != null) {
                if (projectRoleManager.isUserInProjectRole(user, projectRole, project)) {
                    return true;
                }
            }
        }
        //code for Single User Pickers.
        stk = new StringTokenizer(params[3], ",");
        CustomFieldManager fieldManager = ComponentManager.getInstance().getCustomFieldManager();
        CustomField cf = null;

        while (stk.hasMoreTokens()) {
            cf = fieldManager.getCustomFieldObject("customfield_" + stk.nextToken().trim());
            if (cf != null && cf.getValue(issue) != null) {
                if (user.getName().equals(((User) cf.getValue(issue)).getName())) {
                    return true;
                }
            }
        }

        //code for Multi User Pickers.
        stk = new StringTokenizer(params[4], ",");

        while (stk.hasMoreTokens()) {
            cf = fieldManager.getCustomFieldObject("customfield_" + stk.nextToken().trim());
            if (cf != null && cf.getValue(issue) != null) {
                if (((List) cf.getValue(issue)).contains(user)) {
                    return true;
                }
            }
        }

        //code for Group CF.
        stk = new StringTokenizer(params[5], ",");
        GroupSelectorUtils groupSelectorUtils = (GroupSelectorUtils) ComponentManager.getInstance().
                getContainer().getComponentInstanceOfType(GroupSelectorUtils.class);

        while (stk.hasMoreTokens()) {
            cf = fieldManager.getCustomFieldObject("customfield_" + stk.nextToken().trim());
            if (cf != null && cf.getValue(issue) != null) {
                if (groupSelectorUtils.isUserInCustomFieldGroup(issue, cf, user)) {
                    return true;
                }
            }
        }

        //assignee
        if (params[6].trim().equals("1")) {
            if (user.getName().equals(issue.getAssignee().getName())) {
                return true;
            }
        }
        //reporter
        if (params[7].trim().equals("1")) {
            if (user.getName().equals(issue.getReporter().getName())) {
                return true;
            }
        }
        //component lead
        if (params[8].trim().equals("1")) {
            if (issue.getComponents() != null) {
                Object[] o = issue.getComponents().toArray();
                for (int i = 0; i < o.length; i++) {
                    if (((GenericValue) o[i]).getString("lead").equals(user.getName())) {
                        return true;
                    }
                }
            }
        }

        //project lead.
        if (params[9].trim().equals("1")) {
            if (user.getName().equals(project.getLead().getName())) {
                return true;
            }
        }
        return false;
    }

    public static void updateIssueIndex(Issue issue) {
        try {
            ComponentManager.getInstance().getIndexManager().reIndex(issue);
        } catch (IndexException ex) {
            System.err.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }

    public static boolean isCalledByIndexer(String callingClassName) {
        boolean isCalledByIndexer = false;
        try {
            Class c = Class.forName(callingClassName);
            Class parentClass = c.getSuperclass();
            if (parentClass != null) {
                isCalledByIndexer = c.getSuperclass().
                        equals(AbstractCustomFieldIndexer.class);
            }
        } catch (ClassNotFoundException ex) {
            // default returns false
        }
        return isCalledByIndexer;
    }
}
