/**
 * Neodynamica System Library Copyright (c) 2019 Neodynamica - University of Newcastle
 */
package com.neodynamica.lib.gp;

/**
 * State of Symbolic Regression
 *
 * @version 1.0
 * @since 1.0
 */
public enum RunState {
    ENDED,
    ERROR,
    RUNNING,
    STARTED,
    STOPPED,
    PAUSED,
    RESUMED,
}
