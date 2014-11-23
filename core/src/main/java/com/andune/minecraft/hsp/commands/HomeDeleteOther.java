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
package com.andune.minecraft.hsp.commands;

import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.commands.uber.UberCommand;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.util.HomeUtil;

import javax.inject.Inject;

/**
 * @author andune
 */
@UberCommand(uberCommand = "home", subCommand = "deleteOther",
        aliases = {"do"}, help = "Delete another player's home")
public class HomeDeleteOther extends BaseCommand {
    @Inject
    private Storage storage;
    @Inject
    private HomeUtil util;

    @Override
    public String[] getCommandAliases() {
        return new String[]{"hdo"};
    }

    @Override
    public String getUsage() {
        return server.getLocalizedMessage(HSPMessages.CMD_HOMEDELETEOTHER_USAGE);
    }

    @Override
    public boolean execute(CommandSender p, String cmd, String[] args) {
        if (!defaultCommandChecks(p))
            return false;

        if (args.length < 1) {
            return false;
        }

        final String playerName = args[0];
        String worldName = null;
        String homeName = null;

        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith("w:")) {
                worldName = args[i].substring(2);
            } else {
                if (homeName != null) {
                    p.sendMessage(server.getLocalizedMessage(HSPMessages.TOO_MANY_ARGUMENTS));
                    return true;
                }
                homeName = args[i];
            }
        }

        if (worldName == null) {
            if (p instanceof Player) {
                worldName = ((Player) p).getWorld().getName();
            }
        }

        com.andune.minecraft.hsp.entity.Home home;
        if (homeName != null) {
            home = storage.getHomeDAO().findHomeByNameAndPlayer(homeName, playerName);
        } else {
            if (worldName != null)
                home = util.getDefaultHome(playerName, worldName);
            else {
                // we weren't given enough arguments to do anything useful,
                // print command usage syntax
                return false;
            }
        }

        if (home != null) {
            try {
                storage.getHomeDAO().deleteHome(home);
                if (homeName != null)
                    p.sendMessage(server.getLocalizedMessage(HSPMessages.CMD_HOMEDELETEOTHER_HOME_DELETED,
                            "home", homeName, "player", playerName));
                else
                    p.sendMessage(server.getLocalizedMessage(HSPMessages.CMD_HOMEDELETEOTHER_DEFAULT_HOME_DELETED,
                            "player", playerName, "world", worldName));
            } catch (StorageException e) {
                p.sendMessage(server.getLocalizedMessage(HSPMessages.GENERIC_ERROR));
                log.warn("Error caught in /" + getCommandName(), e);
            }
        } else if (homeName != null) {
            p.sendMessage(server.getLocalizedMessage(HSPMessages.CMD_HOMEDELETEOTHER_NO_HOME_FOUND,
                    "home", homeName, "player", playerName));
        } else
            p.sendMessage(server.getLocalizedMessage(HSPMessages.CMD_HOMEDELETEOTHER_NO_DEFAULT_HOME_FOUND,
                    "player", playerName, "world", worldName));

        return true;
    }

}
