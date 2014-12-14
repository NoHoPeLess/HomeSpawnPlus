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
package com.andune.minecraft.hsp.util;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.Server;
import com.andune.minecraft.commonlib.server.api.World;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.entity.Spawn;
import com.andune.minecraft.hsp.entity.SpawnImpl;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.PlayerDAO;
import com.andune.minecraft.hsp.storage.dao.SpawnDAO;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Common routines related to management of spawns.
 *
 * @author andune
 */
@Singleton
public class SpawnUtil {
    private final Logger log = LoggerFactory.getLogger(SpawnUtil.class);
    private final ConfigCore configCore;
    private final Server server;
    private final SpawnDAO spawnDAO;
    private final PlayerDAO playerDAO;

    /* A cache for the defaultSpawnWorld once we find it.
     */
    private String defaultSpawnWorld;

    @Inject
    public SpawnUtil(ConfigCore configCore, Server server, SpawnDAO spawnDAO, PlayerDAO playerDAO) {
        this.configCore = configCore;
        this.server = server;
        this.spawnDAO = spawnDAO;
        this.playerDAO = playerDAO;
    }

    /**
     * Set a named spawn at the given location.
     *
     * @param spawnName the name of the spawn
     * @param l         the location of the sapwn
     * @param updatedBy who is updating this spawn
     */
    public void setNamedSpawn(String spawnName, Location l, String updatedBy) throws StorageException {
        Spawn spawn = spawnDAO.findSpawnByName(spawnName);

        // if we get an object back, we already have a Spawn set for this spawnName, so we
        // just update the x/y/z location of it.
        if (spawn != null) {
            spawn.setLocation(l);
            spawn.setUpdatedBy(updatedBy);
        }
        // this is a new spawn for this world/group combo, create a new object
        else {
            spawn = new SpawnImpl(l, updatedBy);
            spawn.setName(spawnName);
        }

        spawnDAO.saveSpawn(spawn);
    }

    public void setFirstSpawn(Location l, String updatedBy) throws StorageException {
        // terrible implementation leakage, but easiest thing for now; and at least
        // it's abstracted behind a util method we can easily change later
        setNamedSpawn(SpawnDAO.NEW_PLAYER_SPAWN, l, updatedBy);
    }

    /**
     * Set the default spawn for a given world.
     *
     * @param l
     * @param updatedBy
     */
    public void setDefaultWorldSpawn(Location l, String updatedBy) throws StorageException {
        // not happy with external static reference, but it's a holdover of
        // a legacy design that has key names rather than flag attributes.
        // Refactoring would require a data change which would affect all
        // installed clients. Just leaving it alone for now.
        setGroupSpawn(Storage.HSP_WORLD_SPAWN_GROUP, l, updatedBy);
    }

    /**
     * Return the default spawn for a given world.
     *
     * @param worldName the world
     * @return a Spawn object for the spawn on the given world
     */
    public Spawn getDefaultWorldSpawn(String worldName) {
        return getGroupSpawn(Storage.HSP_WORLD_SPAWN_GROUP, worldName);
    }

    /**
     * Set the spawn for a given world and group.
     *
     * @param group     the group this spawn is related to. Can be null, in which case this update sets the default for the given world.
     * @param l
     * @param updatedBy
     */
    public void setGroupSpawn(String group, Location l, String updatedBy) throws StorageException {
        Spawn spawn = spawnDAO.findSpawnByWorldAndGroup(l.getWorld().getName(), group);

        // if we get an object back, we already have a Spawn set for this world/group combo, so we
        // just update the x/y/z location of it.
        if (spawn != null) {
            spawn.setLocation(l);
            spawn.setUpdatedBy(updatedBy);
        }
        // this is a new spawn for this world/group combo, create a new object
        else {
            spawn = new SpawnImpl(l, updatedBy);
            spawn.setGroup(group);
        }

        spawnDAO.saveSpawn(spawn);
    }

    /**
     * Given a group and world name, find the associated group spawn, if any.
     *
     * @param group     the group
     * @param worldName the world
     * @return the Spawn object, possibly null
     */
    public Spawn getGroupSpawn(String group, String worldName) {
        Spawn spawn = null;

        if (group == null)
            spawn = spawnDAO.findSpawnByWorld(worldName);
        else
            spawn = spawnDAO.findSpawnByWorldAndGroup(worldName, group);

        if (spawn == null && configCore.isVerboseLogging())
            log.warn("Could not find or load group spawn for '{}' on world {}!", group, worldName);

        return spawn;
    }

    /**
     * Return the name of the default world. This could be different than what the admin
     * defined as the default world, for example if they have a typo in the config, this
     * method will still return a valid world.
     *
     * @return
     */
    public String getDefaultWorld() {
        if (defaultSpawnWorld == null)
            getDefaultSpawn();      // this will find the default spawn world and set defaultSpawnWorld variable

        return defaultSpawnWorld;
    }

    /**
     * Return the global default spawn (ie. there is only one, this is not the multi-world spawn).
     * <p/>
     * This checks, in order:
     * * The world defined by the admin in spawn.defaultWorld
     * * The world named "world" (if any)
     * * The first world it can find as returned by server.getWorlds()
     * <p/>
     * For each case, it checks our database for any spawn record.  If the world is valid, but we
     * have no spawn location on record, then we ask Bukkit what the world spawn location is and
     * update ours to be the same.
     *
     * @return
     */
    public Spawn getDefaultSpawn() {
        Spawn spawn;

        // once we find the defaultSpawnWorld, it's cached for efficiency, so if we've already
        // cached it, just use that.
        // Note that if something bizarre happens (like the default world spawn gets deleted from
        // the underlying database), this just safely falls through and looks for the default
        // world again.
        if (defaultSpawnWorld != null) {
            spawn = getDefaultWorldSpawn(defaultSpawnWorld);
            if (spawn != null)
                return spawn;
        }

        // first, try to get the default spawn based upon the config 
        World world = server.getWorld(configCore.getDefaultWorld());

        // if that didn't work, just get the first world that Bukkit has in it's list
        if (world == null)
            world = server.getWorlds().get(0);

        // Should be impossible to enter this next if(), so throw an exception if we ever get here.
        if (world == null)
            throw new NullPointerException("Couldn't find spawn world!  world is null");

        spawn = getDefaultWorldSpawn(world.getName());
        if (spawn == null) {
            // if we didn't find the spawn in our database, then get the spawn location from Bukkit
            // and update our database with that as the default spawn for that world. 
            Location l = world.getSpawnLocation();
            try {
                setDefaultWorldSpawn(l, "HomeSpawnPlus");
                spawn = getDefaultWorldSpawn(world.getName());  // now get the Spawn object we just inserted
            } catch (StorageException e) {
                log.error("Error creating default Spawn object", e);
            }

            // Ideally shouldn't ever happen, but we know how that goes ...  If there's a problem
            // storing or retrieving the object back we just inserted, then we just create a
            // new object with default world spawn coordinates and complain loudly in the logs.
            if (spawn == null) {
                log.error("could not find default Spawn - improvising!");
                spawn = new SpawnImpl(l, "HomeSpawnPlus");
                spawn.setGroup(Storage.HSP_WORLD_SPAWN_GROUP);
            }
        }

        defaultSpawnWorld = world.getName();
        return spawn;
    }

    /**
     * Called on plugin shutdown to update the logout location of all
     * players. Can also be called periodically to do the same so that
     * we have a recently recorded location in the event of a server
     * crash.
     */
    public void updateAllPlayerLocations() {
        Player[] players = server.getOnlinePlayers();
        for (int i = 0; i < players.length; i++) {
            updateQuitLocation(players[i]);
        }
    }

    /**
     * Update the logout location of a player, if enabled.
     *
     * @param p
     */
    public void updateQuitLocation(Player p) {
        if (configCore.isRecordLastLogout()) {
            log.debug("updateQuitLocation: updating last logout location for player ", p.getName());

            Location quitLocation = p.getLocation();
            com.andune.minecraft.hsp.entity.Player playerStorage = playerDAO.findPlayerByName(p.getName());
            if (playerStorage == null)
                playerStorage = new com.andune.minecraft.hsp.entity.Player(p);
            playerStorage.updateLastLogoutLocation(quitLocation);
            try {
                playerDAO.savePlayer(playerStorage);
            } catch (StorageException e) {
                log.warn("Caught exception", e);
            }
        }
    }
}
