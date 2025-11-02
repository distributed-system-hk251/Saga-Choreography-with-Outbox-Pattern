-- Create database
CREATE DATABASE IF NOT EXISTS notificationdb;
USE notificationdb;

-- Create table
CREATE TABLE IF NOT EXISTS notifications (
    id INT NOT NULL AUTO_INCREMENT,
    order_id INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- Indexes for performance
CREATE INDEX idx_notifications_order_id ON notifications(order_id);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- Insert sample data 
INSERT INTO notifications (order_id, type, message) VALUES 
    (101, 'info', 'Your order has been created.'),
    (102, 'alert', 'Your payment is pending.'),
    (103, 'info', 'Your payment was successful.'),
    (104, 'warning', 'Your order may be delayed.'),
    (105, 'error', 'There was an issue with your payment.');
