package io.medatarun.platform.kernel

/**
 * An event for communication between modules when you don't know the recipients
 *
 * Please don't use this concept in a module for itself, the goal is to communicate mostly business events
 * across the extension platform. No infrastructure here, just business events.
 */
interface Event