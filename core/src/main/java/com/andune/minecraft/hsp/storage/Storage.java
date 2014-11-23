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
package com.andune.minecraft.hsp.storage;

import com.andune.minecraft.hsp.storage.dao.*;


/**
 * Storage interface for stored objects this plugin uses.
 *
 * @author andune
 */
public interface Storage {
    String HSP_WORLD_SPAWN_GROUP = "HSP_GLOBAL";
    String HSP_BED_RESERVED_NAME = "bed";

    /**
     * This method is called to initialize the storage system. If using a DB
     * back end, this is the method that should create the tables if they don't
     * exist.
     * <p/>
     * It is possible that this method could be called multiple times, so it is
     * this methods responsibility to keep track of whether it has already
     * initialized and deal with that situation appropriately.
     */
    void initializeStorage() throws StorageException;

    /**
     * Method to call when the storage system is to be shutdown, most commonly
     * this is used when the plugin is shutdown and can make sure any pending
     * commits have been flushed to storage.
     * <p/>
     * No further read/writes should be made to the storage system once this
     * method has been called.
     */
    void shutdownStorage();

    HomeDAO getHomeDAO();

    HomeInviteDAO getHomeInviteDAO();

    SpawnDAO getSpawnDAO();

    PlayerDAO getPlayerDAO();

    VersionDAO getVersionDAO();

    PlayerSpawnDAO getPlayerSpawnDAO();

    PlayerLastLocationDAO getPlayerLastLocationDAO();

    /**
     * Notify the backing store that it should purge any in-memory cache it has.
     */
    void purgeCache();

    /**
     * Loop through all HSP data that is associated to players (homes, player
     * records, home invites, etc) and purge any records belonging to players
     * that haven't logged in since the given purgeTime.
     *
     * @param purgeTime the time, in epoch milliseconds, that is the oldest player
     *                  data record that will be in existence after the purge
     *                  completes; anything older will have been deleted.
     * @return the number of database rows purged
     */
    int purgePlayerData(long purgeTime);

    /**
     * Purge any data related to the given world. Can be used to cleanup
     * data from deleted worlds.
     *
     * @param world
     * @return the number of database rows purged
     */
    int purgeWorldData(String world);

    void deleteAllData() throws StorageException;

    /**
     * Optional implementation: the backing store can use this to respond to applications
     * wish to defer writes, as often happens with bulk loading or perhaps if the application
     * wants to flush writes on a timed cycle. Storage backends are not required to do
     * anything at all with this, it is just a hint.
     *
     * @param deferred
     */
    void setDeferredWrites(boolean deferred);

    /**
     * For use with setDeferredWrites() above, this method instructs the backend that now
     * is a good time to flush any pending writes to storage. Again, a completely optional
     * implementation for the storage system, so there is no guarantee calling this does
     * anything. This is just a hint to the back-end storage that now is a good time to
     * flush pending writes.
     */
    void flushAll() throws StorageException;

    /**
     * Return a String name for this storage implementation, which can be used for
     * metrics purposes.
     *
     * @return
     */
    String getImplName();
}
