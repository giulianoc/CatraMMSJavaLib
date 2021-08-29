package com.catrammslib.entity;

import com.catrammslib.CatraMMSWorkflow;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by multi on 08.06.18.
 */
public class WorkflowVariable implements Serializable{

    private static final Logger mLogger = Logger.getLogger(CatraMMSWorkflow.class);

    private String name;
    private String description;
    private boolean nullVariable;
    private String type;
    private String stringValue;
    private long longValue;
    private double doubleValue;
    private boolean booleanValue;
    private Date datetimeValue;
    private JSONObject jsonObjectValue;
    private JSONArray jsonArrayValue;

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
    public WorkflowVariable(String name, JSONObject jsonObjectValue) {
        setName(name);
        setValue(jsonObjectValue);
    }
    public WorkflowVariable(String name, JSONArray jsonArrayValue) {
        setName(name);
        setValue(jsonArrayValue);
    }

    public WorkflowVariable clone()
    {
        WorkflowVariable workflowVariable = new WorkflowVariable();

        workflowVariable.setName(getName());
        workflowVariable.setDescription(getDescription());
        workflowVariable.setNullVariable(isNullVariable());
        workflowVariable.setType(getType());
        workflowVariable.setStringValue(getStringValue());
        workflowVariable.setLongValue(getLongValue());
        workflowVariable.setDoubleValue(getDoubleValue());
        workflowVariable.setBooleanValue(isBooleanValue());
        workflowVariable.setDatetimeValue(getDatetimeValue());
        workflowVariable.setJsonObjectValue(getJsonObjectValue());
        workflowVariable.setJsonArrayValue(getJsonArrayValue());

        return workflowVariable;
    }

    @Override
    public String toString() {
        return "WorkflowVariable{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", nullVariable='" + nullVariable + '\'' +
                ", type='" + type + '\'' +
                ", stringValue='" + stringValue + '\'' +
                ", longValue=" + longValue +
                ", doubleValue=" + doubleValue +
                ", booleanValue=" + booleanValue +
                ", datetimeValue=" + datetimeValue +
                ", jsonObjectValue=" + (jsonObjectValue == null ? "{}" : jsonObjectValue.toString()) +
                ", jsonArrayValue=" + (jsonArrayValue == null ? "{}" : jsonArrayValue.toString()) +
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
    public void setValue(JSONObject jsonObjectValue) {
        this.jsonObjectValue = jsonObjectValue;
        this.type = "jsonObject";
    }
    public void setValue(JSONArray jsonArrayValue) {
        this.jsonArrayValue = jsonArrayValue;
        this.type = "jsonArray";
    }


    // used workflowAsLibraryProperties.xhtml
    public String getsJsonObjectValue() {
        return jsonObjectValue == null ? "{}" : jsonObjectValue.toString();
    }

    // used workflowAsLibraryProperties.xhtml
    public void setsJsonObjectValue(String sJsonObjectValue)
    {
        try {
            if (sJsonObjectValue == null || sJsonObjectValue.isEmpty())
                jsonObjectValue = new JSONObject();
            else
                jsonObjectValue = new JSONObject(sJsonObjectValue);
        }
        catch (Exception e)
        {
            jsonObjectValue = new JSONObject();

            mLogger.error("Exception: " + e);
        }
    }

    // used workflowAsLibraryProperties.xhtml
    public String getsJsonArrayValue() {
        return jsonArrayValue == null ? "[]" : jsonArrayValue.toString();
    }

    // used workflowAsLibraryProperties.xhtml
    public void setsJsonArrayValue(String sJsonArrayValue)
    {
        try {
            if (sJsonArrayValue == null || sJsonArrayValue.isEmpty())
                jsonArrayValue = new JSONArray();
            else
                jsonArrayValue = new JSONArray(sJsonArrayValue);
        }
        catch (Exception e)
        {
            jsonArrayValue = new JSONArray();

            mLogger.error("Exception: " + e);
        }
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

    public boolean isNullVariable() {
        return nullVariable;
    }

    public void setNullVariable(boolean nullVariable) {
        this.nullVariable = nullVariable;
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

    public JSONObject getJsonObjectValue() {
        return jsonObjectValue;
    }

    public void setJsonObjectValue(JSONObject jsonObjectValue) {
        this.jsonObjectValue = jsonObjectValue;
    }

    public JSONArray getJsonArrayValue() {
        return jsonArrayValue;
    }

    public void setJsonArrayValue(JSONArray jsonArrayValue) {
        this.jsonArrayValue = jsonArrayValue;
    }

    public Date getDatetimeValue() {
        return datetimeValue;
    }

    public void setDatetimeValue(Date datetimeValue) {
        this.datetimeValue = datetimeValue;
    }
}
