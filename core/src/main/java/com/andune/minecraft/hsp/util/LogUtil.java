/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
/**
 *
 */
package com.andune.minecraft.hsp.util;

import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.LoggerLog4j;
import com.andune.minecraft.commonlib.log.LogUtilLog4j;

import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Specific to JUL at the moment even though HSP leverages SLF4J and
 * can therefore use other loggers. At some point this could be modified
 * to detect the logging platform and do whatever is appropriate for that
 * given platform.
 *
 * @author andune
 */
public class LogUtil {
    private static com.andune.minecraft.commonlib.Logger log = LoggerFactory.getLogger(LogUtil.class);
    private static boolean debugEnabled = false;
    private static Level previousLevel = null;
    private static Level previousRootLevel = null;

    /**
     * Enable debugging by modifying appropriate log handlers to log at debug
     * level.
     */
    public static void enableDebug() {
        if (!debugEnabled) {
            debugEnabled = true;

            // TODO: hacky kludge method for now, make fancy later
            boolean useLog4j = false;
            try {
                Class.forName("org.apache.logging.log4j.Logger");
                useLog4j = true;
            } catch(ClassNotFoundException e) {
            }

            if( useLog4j ) {
                LoggerFactory.setLoggerImpl(LoggerLog4j.class);
                new LogUtilLog4j().enableDebug("com.andune.minecraft", null);

                // re-initialize this class logger
                log = LoggerFactory.getLogger(LogUtil.class);
            }
            else {
                previousLevel = Logger.getLogger("com.andune.minecraft").getLevel();
    //            Logger.getLogger("com.andune.minecraft.hsp").setLevel(Level.ALL);
                Logger.getLogger("com.andune.minecraft").setLevel(Level.ALL);

                Handler handler = getRootFileHandler(Logger.getLogger("Minecraft"));
                if( handler != null ) {
                    previousRootLevel = handler.getLevel();
                    handler.setLevel(Level.ALL);
                }
            }

            log.debug("DEBUG ENABLED");
        }
    }

    public static void disableDebug() {
        if (debugEnabled) {
            debugEnabled = false;
            log.debug("DEBUG DISABLED");

//            Logger.getLogger("com.andune.minecraft.hsp").setLevel(previousLevel);
            Logger.getLogger("com.andune.minecraft").setLevel(previousLevel);
            previousLevel = null;

            Handler handler = getRootFileHandler(Logger.getLogger("Minecraft"));
            handler.setLevel(previousRootLevel);
            previousRootLevel = null;
        }
    }

    private static Handler getRootFileHandler(Logger log) {
        Handler handler = null;

        // recurse up to root logger right away
        Logger parent = log.getParent();
        if (parent != null)
            handler = getRootFileHandler(parent);

        // now from root logger on down, we look for the first
        // FileHandler we find
        if (handler == null) {
            Handler[] handlers = log.getHandlers();
            for (int i = 0; i < handlers.length; i++) {
                if (handlers[i] instanceof FileHandler) {
                    handler = handlers[i];
                    break;
                }
            }
        }

        return handler;
    }
}
