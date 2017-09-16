/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lghombot.handler;

import static org.openhab.binding.lghombot.LGHombotBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lghombot.internal.LGHombotCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGHombotHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Roland Moser - Initial contribution
 */
public class LGHombotHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(LGHombotHandler.class);

    private LGHombotCommunication communication = new LGHombotCommunication();

    private ScheduledFuture<?> refreshJob;

    public LGHombotHandler(@NonNull Thing thing) {
        super(thing);
    }

    private void ReadStatus() {
        synchronized (communication) {
            try {
                String address = (String) getThing().getConfiguration().get(CONFIGURATION_ADDRESS);

                Properties prop = communication.GetStatus(address);
                this.updateProperty(PROPERTY_VERSION, prop.getProperty("JSON_VERSION"));
                this.updateProperty(PROPERTY_PROGRAMVERSION, prop.getProperty("LGSRV_VERSION"));

                // changeable values
                this.updateState(CHANNEL_NAME, StringType.valueOf(prop.getProperty("JSON_NICKNAME")));
                this.updateState(CHANNEL_CLEANMODE, StringType.valueOf(prop.getProperty("JSON_MODE")));
                if (prop.getProperty("JSON_REPEAT").equals("true")) {
                    this.updateState(CHANNEL_REPEAT, OnOffType.ON);
                } else if (prop.getProperty("JSON_REPEAT").equals("false")) {
                    this.updateState(CHANNEL_REPEAT, OnOffType.OFF);
                }
                if (prop.getProperty("JSON_TURBO").equals("true")) {
                    this.updateState(CHANNEL_TURBO, OnOffType.ON);
                } else if (prop.getProperty("JSON_TURBO").equals("false")) {
                    this.updateState(CHANNEL_TURBO, OnOffType.OFF);
                }

                // TODO voice not available on JSON interface
                // TODO read/reset schedule

                // read-only values
                this.updateState(CHANNEL_STATE, StringType.valueOf(prop.getProperty("JSON_ROBOT_STATE")));
                this.updateState(CHANNEL_BATTERYLEVEL, DecimalType.valueOf(prop.getProperty("JSON_BATTPERC")));
                this.updateState(CHANNEL_CURRENTBUMPING, DecimalType.valueOf(prop.getProperty("CLREC_CURRENTBUMPING")));
                {
                    DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH/mm/ss.SSSSSS");
                    LocalDateTime dt = LocalDateTime.from(f.parse(prop.getProperty("CLREC_LAST_CLEAN")));

                    DateTimeFormatter f2 = DateTimeFormatter.ofPattern(DateTimeType.DATE_PATTERN);
                    this.updateState(CHANNEL_LASTCLEANING, DateTimeType.valueOf(dt.format(f2)));
                }
                this.updateState(CHANNEL_CPUIDLE, DecimalType.valueOf(prop.getProperty("CPU_IDLE")));
                this.updateState(CHANNEL_CPUUSER, DecimalType.valueOf(prop.getProperty("CPU_USER")));
                this.updateState(CHANNEL_CPUSYS, DecimalType.valueOf(prop.getProperty("CPU_SYS")));
                this.updateState(CHANNEL_CPUNICE, DecimalType.valueOf(prop.getProperty("CPU_NICE")));

                this.updateState(CHANNEL_SUMCMD, DecimalType.valueOf(prop.getProperty("LGSRV_SUMCMD")));
                this.updateState(CHANNEL_SUMCMDSEC, DecimalType.valueOf(prop.getProperty("LGSRV_SUMCMDSEC")));
                this.updateState(CHANNEL_NUMHTTP, DecimalType.valueOf(prop.getProperty("LGSRV_NUMHTTP")));
                {
                    String value = prop.getProperty("LGSRV_MEMUSAGE");
                    BigDecimal factor = new BigDecimal(1);
                    if (value.endsWith(" B")) {
                        factor = new BigDecimal(1);
                        value = value.substring(0, value.length() - 2);
                    } else if (value.endsWith(" kB") || value.endsWith(" KB")) {
                        factor = new BigDecimal(1000);
                        value = value.substring(0, value.length() - 3);
                    } else if (value.endsWith(" MB")) {
                        factor = new BigDecimal(1000000);
                        value = value.substring(0, value.length() - 3);
                    } else if (value.endsWith(" GB")) {
                        factor = new BigDecimal(1000000000);
                        value = value.substring(0, value.length() - 3);
                    }
                    DecimalType memusage = DecimalType.valueOf(value);
                    BigDecimal value2 = memusage.toBigDecimal().multiply(factor);
                    this.updateState(CHANNEL_MEMUSAGE, DecimalType.valueOf(value2.toString()));
                }

                updateStatus(ThingStatus.ONLINE);
            } catch (MalformedURLException e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            } catch (IOException e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    private void SendCommand(String s) {
        synchronized (communication) {
            if (getThing().getStatus() == ThingStatus.OFFLINE) {
                reconnect();
            }

            if (getThing().getStatus() != ThingStatus.ONLINE) {
                return;
            }

            try {
                String address = (String) getThing().getConfiguration().get(CONFIGURATION_ADDRESS);
                logger.debug("SendCommand({}, {})", address, s);
                communication.SendCommand(address, s);
            } catch (MalformedURLException e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            } catch (IOException e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    private void reconnect() {
        ReadStatus();

        // synchronize local time
        LocalDateTime now = LocalDateTime.now();
        String command = String.format(
                "{\"TIME_SET\":{\"DATE\":\"%04d%02d%02d\",\"HOUR\":\"%02d\",\"MINUTE\":\"%02d\",\"SECOND\":\"%02d\"}}",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.getHour(), now.getMinute(),
                now.getSecond());
        SendCommand(command);
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String jsonCommand;

        if (channelUID.getId().equals(CHANNEL_NAME) && (command instanceof StringType)) {
            jsonCommand = "{\"NICKNAME\":{\"SET\":\"" + command.toString() + "\"}}";
        } else if (channelUID.getId().equals(CHANNEL_COMMAND) && (command instanceof StringType)) {
            String value = command.toString();
            jsonCommand = "{\"COMMAND\":\"" + value + "\"}";
        } else if (channelUID.getId().equals(CHANNEL_CLEANMODE) && (command instanceof StringType)) {
            String value = command.toString();
            jsonCommand = "{\"COMMAND\":{\"CLEAN_MODE\":\"CLEAN_" + value + "\"}}";
        } else if (channelUID.getId().equals(CHANNEL_VOICE) && (command instanceof StringType)) {
            String value = command.toString();
            jsonCommand = "{\"COMMAND\":{\"VOICE\":\"" + value + "\"}}";
        } else if (channelUID.getId().equals(CHANNEL_REPEAT) && (command instanceof OnOffType)) {
            String value;
            if (command == OnOffType.ON) {
                value = "true";
            } else if (command == OnOffType.OFF) {
                value = "false";
            } else {
                return;
            }
            jsonCommand = "{\"COMMAND\":{\"REPEAT\":\"" + value + "\"}}";
        } else if (channelUID.getId().equals(CHANNEL_TURBO) && (command instanceof OnOffType))

        {
            String value;
            if (command == OnOffType.ON) {
                value = "true";
            } else if (command == OnOffType.OFF) {
                value = "false";
            } else {
                return;
            }
            jsonCommand = "{\"COMMAND\":{\"TURBO\":\"" + value + "\"}}";
        } else {
            return;
        }
        SendCommand(jsonCommand);
    }

    @Override
    public void initialize() {
        reconnect();
        startAutomaticRefresh();
    }

    private void startAutomaticRefresh() {
        BigDecimal refresh = (BigDecimal) getThing().getConfiguration().get(CONFIGURATION_REFRESH);

        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            ReadStatus();
        }, 0, refresh.longValue(), TimeUnit.SECONDS);
    }
}
