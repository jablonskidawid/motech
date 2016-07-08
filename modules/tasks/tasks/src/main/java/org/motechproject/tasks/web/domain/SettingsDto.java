package org.motechproject.tasks.web.domain;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

public class SettingsDto {

    private String taskPossibleErrors;

    private String taskLogActivities;


    public String getTaskPossibleErrors() {
        return taskPossibleErrors;
    }

    public void setTaskPossibleErrors(String taskPossibleErrors) {
        this.taskPossibleErrors = taskPossibleErrors;
    }

    @JsonIgnore
    public boolean isValid() {
        if (StringUtils.isEmpty(taskPossibleErrors) || !StringUtils.isNumeric(taskPossibleErrors)) {
            return false;
        }
        return true;
    }

    public String getTaskLogActivities() {
        return taskLogActivities;
    }

    public void setTaskLogActivities(String taskLogActivities) {
        this.taskLogActivities = taskLogActivities;
    }
}
