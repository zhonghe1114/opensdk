package com.qooapp.opensdk.sample.model;

/**
 *
 * @email devel@qoo-app.com
 */
public class Product {
    /**
     * "coin_1_v0": {
     "product_id": "coin_1_v0",
     "name": "1 iQ",
     "description": "QooApp's currency.",
     "prices": [
     {
     "channel": "paypal",
     "amount": "0.03",
     "currency": "USD",
     "subchannel": "paypal"
     }
     ]
     }
     */
    private int id;
    private String name;
    private String product_id;
    private String description;
    private double amount;
    private String currency;
    private String channel;
    private String token;
    private String purchase_id;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPurchase_id() {
        return purchase_id;
    }

    public void setPurchase_id(String purchase_id) {
        this.purchase_id = purchase_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductId() {
        return product_id;
    }

    public void setProductId(String product_id) {
        this.product_id = product_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
