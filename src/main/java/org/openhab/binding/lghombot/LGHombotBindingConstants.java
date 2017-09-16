/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lghombot;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link LGHombotBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Roland Moser - Initial contribution
 */
public class LGHombotBindingConstants {

    public static final String BINDING_ID = "lghombot";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LGHOMBOT = new ThingTypeUID(BINDING_ID, "lghombot");

    // List of all Channel ids
    public static final String CHANNEL_NAME = "name";
    public static final String CHANNEL_COMMAND = "command";
    public static final String CHANNEL_CLEANMODE = "clean-mode";
    public static final String CHANNEL_VOICE = "voice";
    public static final String CHANNEL_REPEAT = "repeat";
    public static final String CHANNEL_TURBO = "turbo";

    // List of all Channel ids (read-only)
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_BATTERYLEVEL = "battery-level";
    public static final String CHANNEL_CURRENTBUMPING = "current-bumping";
    public static final String CHANNEL_LASTCLEANING = "last-cleaning";
    public static final String CHANNEL_CPUIDLE = "cpu-idle";
    public static final String CHANNEL_CPUUSER = "cpu-user";
    public static final String CHANNEL_CPUSYS = "cpu-sys";
    public static final String CHANNEL_CPUNICE = "cpu-nice";
    public static final String CHANNEL_SUMCMD = "sumcmd";
    public static final String CHANNEL_SUMCMDSEC = "sumcmdsec";
    public static final String CHANNEL_NUMHTTP = "numhttp";
    public static final String CHANNEL_MEMUSAGE = "memusage";

    // List of all Properties
    public static final String PROPERTY_VERSION = "version";
    public static final String PROPERTY_PROGRAMVERSION = "programVersion";

    // List of all Configurations
    public static final String CONFIGURATION_ADDRESS = "address";
    public static final String CONFIGURATION_REFRESH = "refresh";
}
