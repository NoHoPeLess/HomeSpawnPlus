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
package com.andune.minecraft.hsp.storage.dao;

import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.entity.HomeInvite;
import com.andune.minecraft.hsp.storage.StorageException;

import java.util.Set;

/**
 * @author andune
 */
public interface HomeInviteDAO extends PurgePlayer {
    /**
     * Given the primary id, find the HomeInvite object.
     *
     * @param id
     * @return
     */
    HomeInvite findHomeInviteById(int id);

    /**
     * Given a specific home and a specific invitee, find the HomeInvite
     * object. This will return null if no such relationship exists.
     *
     * @param home
     * @param invitee
     * @return
     */
    HomeInvite findInviteByHomeAndInvitee(Home home, String invitee);

    /**
     * Given a home, return all HomeInvite objects that might be attached to it.
     *
     * @param home
     * @return
     */
    Set<HomeInvite> findInvitesByHome(Home home);

    /**
     * Given an invitee, return all available invites open to them.
     *
     * @param invitee
     * @return
     */
    Set<HomeInvite> findAllAvailableInvites(String invitee);

    /**
     * Return all available public invites.
     *
     * @return
     */
    Set<HomeInvite> findAllPublicInvites();

    /**
     * Given a player, return all open invites that we have
     * outstanding.
     *
     * @param invitee
     * @return
     */
    Set<HomeInvite> findAllOpenInvites(String player);

    Set<HomeInvite> findAllHomeInvites();

    void saveHomeInvite(HomeInvite homeInvite) throws StorageException;

    void deleteHomeInvite(HomeInvite homeInvite) throws StorageException;

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.Storage#purgePlayerData(java.lang.Long)
     */
    int purgePlayerData(long purgeTime);

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.Storage#purgeWorldData(java.lang.String)
     */
    int purgeWorldData(String world);
}
