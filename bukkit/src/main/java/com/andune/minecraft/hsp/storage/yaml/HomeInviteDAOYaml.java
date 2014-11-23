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
package com.andune.minecraft.hsp.storage.yaml;

import com.andune.minecraft.commonlib.FeatureNotImplemented;
import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.entity.HomeInvite;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.HomeInviteDAO;
import com.andune.minecraft.hsp.storage.yaml.serialize.SerializableHomeInvite;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author andune
 */
public class HomeInviteDAOYaml extends AbstractDAOYaml<HomeInvite, SerializableHomeInvite> implements HomeInviteDAO {
    private static final String CONFIG_SECTION = "homeInvites";

    public HomeInviteDAOYaml(final File file, final YamlConfiguration yaml) {
        super(CONFIG_SECTION);
        this.yaml = yaml;
        this.file = file;
    }

    public HomeInviteDAOYaml(final File file) {
        this(file, null);
    }

    @Override
    public HomeInvite findHomeInviteById(int id) {
        HomeInvite homeInvite = null;

        Set<HomeInvite> homeInvites = findAllObjects();
        if (homeInvites != null && homeInvites.size() > 0) {
            for (HomeInvite h : homeInvites) {
                if (id == h.getId()) {
                    homeInvite = h;
                    break;
                }
            }
        }

        return homeInvite;
    }

    @Override
    public HomeInvite findInviteByHomeAndInvitee(Home home, String invitee) {
        HomeInvite homeInvite = null;

        Set<HomeInvite> homeInvites = findAllObjects();
        if (homeInvites != null && homeInvites.size() > 0) {
            for (HomeInvite h : homeInvites) {
                if (home.equals(h.getHome()) && invitee.equals(h.getInvitedPlayer())) {
                    homeInvite = h;
                    break;
                }
            }
        }

        return homeInvite;
    }

    @Override
    public Set<HomeInvite> findInvitesByHome(Home home) {
        Set<HomeInvite> set = new HashSet<HomeInvite>(5);

        Set<HomeInvite> homeInvites = findAllObjects();
        if (homeInvites != null && homeInvites.size() > 0) {
            for (HomeInvite h : homeInvites) {
                if (home.equals(h.getHome())) {
                    set.add(h);
                }
            }
        }

        return set;
    }

    @Override
    public Set<HomeInvite> findAllAvailableInvites(String invitee) {
        Set<HomeInvite> set = new HashSet<HomeInvite>(5);

        Set<HomeInvite> homeInvites = findAllObjects();
        if (homeInvites != null && homeInvites.size() > 0) {
            for (HomeInvite h : homeInvites) {
                if (invitee.equals(h.getInvitedPlayer())) {
                    set.add(h);
                }
            }
        }

        return set;
    }

    @Override
    public Set<HomeInvite> findAllPublicInvites() {
        return findAllAvailableInvites(HomeInvite.PUBLIC_HOME);
    }

    @Override
    public Set<HomeInvite> findAllOpenInvites(String player) {
        Set<HomeInvite> set = new HashSet<HomeInvite>(5);

        Set<HomeInvite> homeInvites = findAllObjects();
        if (homeInvites != null && homeInvites.size() > 0) {
            for (HomeInvite h : homeInvites) {
                if (player.equals(h.getHome().getPlayerName())) {
                    set.add(h);
                }
            }
        }

        return set;
    }

    @Override
    public Set<HomeInvite> findAllHomeInvites() {
        return super.findAllObjects();
    }

    @Override
    public void saveHomeInvite(HomeInvite homeInvite) throws StorageException {
        super.saveObject(homeInvite);
    }

    @Override
    public void deleteHomeInvite(HomeInvite homeInvite) throws StorageException {
        super.deleteObject(homeInvite);
    }

    @Override
    protected SerializableHomeInvite newSerializable(HomeInvite object) {
        return new SerializableHomeInvite(object);
    }

    @Override
    public int purgePlayer(String playerName) {
        throw new FeatureNotImplemented();
    }

    @Override
    public Set<String> getAllPlayerNames() {
        throw new FeatureNotImplemented();
    }

    @Override
    public int purgePlayerData(long purgeTime) {
        throw new FeatureNotImplemented();
    }

    @Override
    public int purgeWorldData(String world) {
        throw new FeatureNotImplemented();
    }
}
