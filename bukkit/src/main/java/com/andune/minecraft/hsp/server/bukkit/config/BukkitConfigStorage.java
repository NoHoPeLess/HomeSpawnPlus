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
package com.andune.minecraft.hsp.server.bukkit.config;

import com.andune.minecraft.hsp.config.ConfigOptions;
import com.andune.minecraft.hsp.config.ConfigStorage;
import com.andune.minecraft.hsp.storage.BaseStorageFactory;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.inject.Singleton;

/**
 * While storage is technically part of the core config, it is broken out
 * separately because the Storage sub-system has config dependencies and must
 * be injected into several objects that would cause circular injection issues.
 * So we have a separate config that can make a decision about what storage
 * system to inject before any other configs are even loaded.
 *
 * @author andune
 */
@Singleton
@ConfigOptions(fileName = "core.yml", basePath = "core")
public class BukkitConfigStorage implements ConfigStorage {
    private final YamlConfiguration yaml;

    public BukkitConfigStorage(YamlConfiguration yaml) {
        this.yaml = yaml;
    }

    @Override
    public Type getStorageType() {
        return BaseStorageFactory.getType(yaml.getString("core.storage"));
    }

    @Override
    public boolean useInMemoryCache() {
        return yaml.getBoolean("core.inMemoryCache");
    }
}
