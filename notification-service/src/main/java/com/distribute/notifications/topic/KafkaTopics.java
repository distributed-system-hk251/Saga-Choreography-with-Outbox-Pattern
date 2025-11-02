package com.distribute.notifications.topic;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String ORDER_CREATED = "ORDER_CREATED";
    public static final String STOCK_RESERVE_SUCCEEDED = "STOCK_RESERVE_SUCCEEDED";
    public static final String STOCK_RESERVE_FAILED = "STOCK_RESERVE_FAILED";
    public static final String STOCK_RESERVE_RELEASE = "STOCK_RESERVE_RELEASE";
    public static final String PAYMENT_AUTHORIZE = "PAYMENT_AUTHORIZE";
    public static final String PAYMENT_AUTHORIZE_FAILED = "PAYMENT_AUTHORIZE_FAILED";
    public static final String PAYMENT_REFUND = "PAYMENT_REFUND";
    public static final String PAYMENT_AUTHORIZE_SUCCEEDED = "PAYMENT_AUTHORIZE_SUCCEEDED";
    public static final String NOTIFICATION_SEND = "NOTIFICATION_SEND";

}
