package com.catrammslib.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by multi on 09.06.18.
 */
public class DeliveryServersPool implements Serializable{
    private Long deliveryServersPoolKey;
    private String label;
    private List<DeliveryServer> deliveryServerList = new ArrayList<>();


    public String getDeliveryServersInfo()
    {
        String deliveryServersInfo = "";
        for (DeliveryServer deliveryServer: deliveryServerList)
            deliveryServersInfo += (deliveryServer.getLabel() + "<br/>");

        return deliveryServersInfo;
    }

    public Long getDeliveryServersPoolKey() {
        return deliveryServersPoolKey;
    }

    public void setDeliveryServersPoolKey(Long deliveryServersPoolKey) {
        this.deliveryServersPoolKey = deliveryServersPoolKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<DeliveryServer> getDeliveryServerList() {
        return deliveryServerList;
    }

    public void setDeliveryServerList(List<DeliveryServer> deliveryServerList) {
        this.deliveryServerList = deliveryServerList;
    }
}
