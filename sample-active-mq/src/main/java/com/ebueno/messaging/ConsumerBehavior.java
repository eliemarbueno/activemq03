package com.ebueno.messaging;

/**
 * Defines the behavior of message consumers
 */
public enum ConsumerBehavior {
    PERSISTENT,  // Keeps the connection active
    SINGLE_READ  // Releases the connection after reading
}