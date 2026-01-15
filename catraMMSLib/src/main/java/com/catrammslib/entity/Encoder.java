package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import org.json.JSONArray;

/**
 * Created by multi on 09.06.18.
 */
public class Encoder implements Serializable{
    private Long encoderKey;
    private String label;
    private Boolean external;
    private Boolean enabled;
    private String protocol;

    // it is used by the GUI, true publicServerName is used, false internalServerName is used
    private Boolean publicEncoderNameToBeUsed;
    private String publicServerName;
    private String internalServerName;
    private Long port;

	private Boolean running;
    Date selectedLastTime;
    Date cpuUsageUpdateTime;
	private Long cpuUsage;
    Date bandwidthUsageUpdateTime;
    private Long txAvgBandwidthUsage;
    private Long rxAvgBandwidthUsage;

	private JSONArray workspacesAssociated;

	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Encoder encoder = (Encoder) o;
        // in alcuni casi usiamo una List<Encoder> dove ogni encoder è duplicato a meno del PublicEncoderNameToBeUsed
        // per differenziare l'encoder che utilizza il public IP da quello che utilizza il private IP
        if (publicEncoderNameToBeUsed != null && encoder.getPublicEncoderNameToBeUsed() != null)
            return encoderKey.equals(encoder.encoderKey) && publicEncoderNameToBeUsed.equals(encoder.getPublicEncoderNameToBeUsed());
        else
            return encoderKey.equals(encoder.encoderKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encoderKey);
    }

    public String getEncoderName()
    {
        if (getPublicEncoderNameToBeUsed())
            return publicServerName;
        else
            return internalServerName;
    }

    public Encoder clone()
    {
        Encoder encoder = new Encoder();
        encoder.setEncoderKey(encoderKey);
        encoder.setLabel(label);
        encoder.setExternal(external);
        encoder.setEnabled(enabled);
        encoder.setProtocol(protocol);
        encoder.setPublicEncoderNameToBeUsed(publicEncoderNameToBeUsed);
        encoder.setPublicServerName(publicServerName);
        encoder.setInternalServerName(internalServerName);
        encoder.setPort(port);
        encoder.setRunning(running);
        encoder.setSelectedLastTime(selectedLastTime);
        encoder.setCpuUsageUpdateTime(cpuUsageUpdateTime);
        encoder.setCpuUsage(cpuUsage);
        encoder.setBandwidthUsageUpdateTime(bandwidthUsageUpdateTime);
        encoder.setTxAvgBandwidthUsage(txAvgBandwidthUsage);
        encoder.setRxAvgBandwidthUsage(rxAvgBandwidthUsage);
        encoder.setWorkspacesAssociated(workspacesAssociated);

        return encoder;
    }

    public Long getTxAvgBandwidthUsageInMbps() {
        return (txAvgBandwidthUsage * 8) / 1000000;
    }

    public Long getRxAvgBandwidthUsageInMbps() {
        return (rxAvgBandwidthUsage * 8) / 1000000;
    }

    public Long getEncoderKey() {
        return encoderKey;
    }

    public void setEncoderKey(Long encoderKey) {
        this.encoderKey = encoderKey;
    }

    public Long getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(Long cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

    public Date getSelectedLastTime() {
        return selectedLastTime;
    }

    public void setSelectedLastTime(Date selectedLastTime) {
        this.selectedLastTime = selectedLastTime;
    }

    public Date getCpuUsageUpdateTime() {
        return cpuUsageUpdateTime;
    }

    public void setCpuUsageUpdateTime(Date cpuUsageUpdateTime) {
        this.cpuUsageUpdateTime = cpuUsageUpdateTime;
    }

    public Date getBandwidthUsageUpdateTime() {
        return bandwidthUsageUpdateTime;
    }

    public void setBandwidthUsageUpdateTime(Date bandwidthUsageUpdateTime) {
        this.bandwidthUsageUpdateTime = bandwidthUsageUpdateTime;
    }

    public Boolean getRunning() {
		return running;
	}

	public void setRunning(Boolean running) {
		this.running = running;
	}

	public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    public String getPublicServerName() {
		return publicServerName;
	}

	public void setPublicServerName(String publicServerName) {
		this.publicServerName = publicServerName;
	}

	public String getInternalServerName() {
		return internalServerName;
	}

	public void setInternalServerName(String internalServerName) {
		this.internalServerName = internalServerName;
	}

    public Long getTxAvgBandwidthUsage() {
        return txAvgBandwidthUsage;
    }

    public void setTxAvgBandwidthUsage(Long txAvgBandwidthUsage) {
        this.txAvgBandwidthUsage = txAvgBandwidthUsage;
    }

    public Long getRxAvgBandwidthUsage() {
        return rxAvgBandwidthUsage;
    }

    public void setRxAvgBandwidthUsage(Long rxAvgBandwidthUsage) {
        this.rxAvgBandwidthUsage = rxAvgBandwidthUsage;
    }

    public Long getPort() {
        return port;
    }

    public void setPort(Long port) {
        this.port = port;
    }

    public Boolean getExternal() {
        return external;
    }

    public void setExternal(Boolean external) {
        this.external = external;
    }

    public Boolean getPublicEncoderNameToBeUsed() {
        return publicEncoderNameToBeUsed;
    }

    public void setPublicEncoderNameToBeUsed(Boolean publicEncoderNameToBeUsed) {
        this.publicEncoderNameToBeUsed = publicEncoderNameToBeUsed;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

	public JSONArray getWorkspacesAssociated() {
		return workspacesAssociated;
	}

	public void setWorkspacesAssociated(JSONArray workspacesAssociated) {
		this.workspacesAssociated = workspacesAssociated;
	}
}
