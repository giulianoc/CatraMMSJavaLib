package com.catrammslib.entity;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Created by multi on 09.06.18.
 */
public class DeliveryServer implements Serializable{
    private Long deliveryServerKey;
    private String label;
    private String type;
    private Long originDeliveryServerKey;
    private Boolean external;
    private Boolean enabled;

    // it is used by the GUI, true publicServerName is used, false internalServerName is used
    // private Boolean publicEncoderNameToBeUsed;
    private String publicIP;
    private String internalIP;
    private String hostname;
    private Double latitude;
    private Double longitude;
    private Long maxTXBandwidthInGbps;

    Date selectedLastTime;
    Date cpuUsageUpdateTime;
	private Long cpuUsage;
    Date bandwidthUsageUpdateTime;
    private Long txAvgBandwidthUsage;
    private Long rxAvgBandwidthUsage;

	private JSONArray workspacesAssociated;

    // gestito dalla GUI, true se è un "nodo" da sistemare.
    // Ad esempio si trova in stato 'edge' con un originDeliveryServerKey che si trova a sua volta in stato edge anzicche origin o mid-origin
    private Boolean error;
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryServer encoder = (DeliveryServer) o;
        /*
        // in alcuni casi usiamo una List<Encoder> dove ogni encoder è duplicato a meno del PublicEncoderNameToBeUsed
        // per differenziare l'encoder che utilizza il public IP da quello che utilizza il private IP
        if (publicEncoderNameToBeUsed != null && encoder.getPublicEncoderNameToBeUsed() != null)
            return deliveryServerKey.equals(encoder.deliveryServerKey) && publicEncoderNameToBeUsed.equals(encoder.getPublicEncoderNameToBeUsed());
        else
         */
            return deliveryServerKey.equals(encoder.deliveryServerKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deliveryServerKey);
    }

    /*
    public String getEncoderName()
    {
        if (getPublicEncoderNameToBeUsed())
            return publicServerName;
        else
            return internalServerName;
    }
     */

    public DeliveryServer clone()
    {
        DeliveryServer deliveryServer = new DeliveryServer();
        deliveryServer.setDeliveryServerKey(deliveryServerKey);
        deliveryServer.setLabel(label);
        deliveryServer.setType(type);
        deliveryServer.setOriginDeliveryServerKey(originDeliveryServerKey);
        deliveryServer.setExternal(external);
        deliveryServer.setEnabled(enabled);
        // deliveryServer.setPublicEncoderNameToBeUsed(publicEncoderNameToBeUsed);
        deliveryServer.setPublicIP(publicIP);
        deliveryServer.setInternalIP(internalIP);
        deliveryServer.setHostname(hostname);
        deliveryServer.setLatitude(latitude);
        deliveryServer.setLongitude(longitude);
        deliveryServer.setMaxTXBandwidthInGbps(maxTXBandwidthInGbps);
        deliveryServer.setSelectedLastTime(selectedLastTime);
        deliveryServer.setCpuUsageUpdateTime(cpuUsageUpdateTime);
        deliveryServer.setCpuUsage(cpuUsage);
        deliveryServer.setBandwidthUsageUpdateTime(bandwidthUsageUpdateTime);
        deliveryServer.setTxAvgBandwidthUsage(txAvgBandwidthUsage);
        deliveryServer.setRxAvgBandwidthUsage(rxAvgBandwidthUsage);
        deliveryServer.setWorkspacesAssociated(workspacesAssociated);

        return deliveryServer;
    }

    public Long getTxAvgBandwidthUsageInMbps() {
        return txAvgBandwidthUsage == null ? 0L : (txAvgBandwidthUsage * 8) / 1000000;
    }

    public Long getRxAvgBandwidthUsageInMbps() {
        return rxAvgBandwidthUsage == null ? 0L : (rxAvgBandwidthUsage * 8) / 1000000;
    }

    public Long getDeliveryServerKey() {
        return deliveryServerKey;
    }

    public void setDeliveryServerKey(Long deliveryServerKey) {
        this.deliveryServerKey = deliveryServerKey;
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

	public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }


    public String getPublicIP() {
        return publicIP;
    }

    public void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }

    public String getInternalIP() {
        return internalIP;
    }

    public void setInternalIP(String internalIP) {
        this.internalIP = internalIP;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Long getMaxTXBandwidthInGbps() {
        return maxTXBandwidthInGbps;
    }

    public void setMaxTXBandwidthInGbps(Long maxTXBandwidthInGbps) {
        this.maxTXBandwidthInGbps = maxTXBandwidthInGbps;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
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

    public Boolean getExternal() {
        return external;
    }

    public void setExternal(Boolean external) {
        this.external = external;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getOriginDeliveryServerKey() {
        return originDeliveryServerKey;
    }

    public void setOriginDeliveryServerKey(Long originDeliveryServerKey) {
        this.originDeliveryServerKey = originDeliveryServerKey;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public JSONArray getWorkspacesAssociated() {
		return workspacesAssociated;
	}

	public void setWorkspacesAssociated(JSONArray workspacesAssociated) {
		this.workspacesAssociated = workspacesAssociated;
	}
}
