/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2015 Andune (andune.alleria@gmail.com)
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
package com.andune.minecraft.hsp.config;

import com.andune.minecraft.commonlib.Initializable;

import javax.inject.Singleton;

/**
 * @author andune
 */
@Singleton
@ConfigOptions(fileName = "homeInvites.yml", basePath = "homeInvite")
public class ConfigHomeInvites extends ConfigBase implements Initializable {
    /**
     * Return the time (in seconds) for a sent invite to expire.
     *
     * @return
     */
    public int getTimeout() {
        return super.getInt("timeout");
    }

    /**
     * Determine if invites are enabled for bedHomes.
     *
     * @return
     */
    public boolean allowBedHomeInvites() {
        return super.getBoolean("allowBedHomeInvites");
    }

    /**
     * Determine if home invites should use shared home warmups instead of
     * their own warmups.
     *
     * @return
     */
    public boolean useHomeWarmup() {
        return super.getBoolean("useHomeWarmup");
    }

    /**
     * Determine if home invites should use shared home cooldowns instead
     * of their own cooldowns.
     *
     * @return
     */
    public boolean useHomeCooldown() {
        return super.getBoolean("useHomeCooldown");
    }

    /**
     * Determine whether public home invites are allowed.
     *
     * @return
     */
    public boolean allowPublicInvites() {
        return super.getBoolean("allowPublicInvites");
    }

    /**
     * Return the default permanent timeout. -1 indicates no default,
     * 0 indicates forever, and any other string is parsed as if it was a
     * time string just like the /homeinvite command: ie. "5m" is 5 minutes.
     *
     * @return
     */
    public String getDefaultPermanentTimeout() {
        return super.getString("defaultPermanentTimeout");
    }
}
