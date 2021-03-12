package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by multi on 08.06.18.
 */
public class WorkflowVariable implements Serializable{

    private String name;
    private String description;
    private String type;
    private String stringValue;
    private long longValue;
    private double doubleValue;
    private boolean booleanValue;
    private Date datetimeValue;

    public WorkflowVariable() {
    }

    public WorkflowVariable(String name, String stringValue) {
        setName(name);
        setValue(stringValue);
    }
    public WorkflowVariable(String name, long longValue) {
        setName(name);
        setValue(longValue);
    }
    public WorkflowVariable(String name, double doubleValue) {
        setName(name);
        setValue(doubleValue);
    }
    public WorkflowVariable(String name, boolean booleanValue) {
        setName(name);
        setValue(booleanValue);
    }
    public WorkflowVariable(String name, Date datetimeValue) {
        setName(name);
        setValue(datetimeValue);
    }

    public WorkflowVariable clone()
    {
        WorkflowVariable workflowVariable = new WorkflowVariable();

        workflowVariable.setName(getName());
        workflowVariable.setDescription(getDescription());
        workflowVariable.setType(getType());
        workflowVariable.setStringValue(getStringValue());
        workflowVariable.setLongValue(getLongValue());
        workflowVariable.setDoubleValue(getDoubleValue());
        workflowVariable.setBooleanValue(isBooleanValue());
        workflowVariable.setDatetimeValue(getDatetimeValue());

        return workflowVariable;
    }

    @Override
    public String toString() {
        return "WorkflowVariable{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", stringValue='" + stringValue + '\'' +
                ", longValue=" + longValue +
                ", doubleValue=" + doubleValue +
                ", booleanValue=" + booleanValue +
                ", datetimeValue=" + datetimeValue +
                '}';
    }

    public void setValue(String stringValue) {
        this.stringValue = stringValue;
        this.type = "string";
    }
    public void setValue(long longValue) {
        this.longValue = longValue;
        this.type = "integer";
    }
    public void setValue(double doubleValue) {
        this.doubleValue = doubleValue;
        this.type = "decimal";
    }
    public void setValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
        this.type = "boolean";
    }
    public void setValue(Date datetimeValue) {
        this.datetimeValue = datetimeValue;
        this.type = "datetime"; // really it could be also datetime-millisecs
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public boolean isBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public Date getDatetimeValue() {
        return datetimeValue;
    }

    public void setDatetimeValue(Date datetimeValue) {
        this.datetimeValue = datetimeValue;
    }
}
