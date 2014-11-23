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
package com.andune.minecraft.hsp;

import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.hsp.guice.BukkitInjectorFactory;
import com.andune.minecraft.hsp.server.bukkit.config.BukkitConfigStorage;
import com.andune.minecraft.hsp.storage.ebean.StorageEBeans;
import com.andune.minecraft.hsp.util.LogUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the implementation of Bukkit's Plugin interface. This is
 * abstracted from the rest of the plugin so as to minimize impact to the code
 * when Bukkit makes API changes and to simplify supporting MC-API, Spout or
 * other frameworks.
 *
 * @author andune
 */
public class HomeSpawnPlusBukkit extends JavaPlugin {
    private HomeSpawnPlus mainClass;

    @Override
    public void onEnable() {
        LoggerFactory.setLoggerPrefix("[HomeSpawnPlus] ");

        // disable reflections spam; it's a bug that prints warnings that look alarming
        Logger.getLogger("org.reflections").setLevel(Level.OFF);

        File debugFlagFile = new File(getDataFolder(), "devDebug");
        if (debugFlagFile.exists())
            LogUtil.enableDebug();

        try {
            BukkitInjectorFactory factory = new BukkitInjectorFactory(this,
                    new BukkitConfigStorage(getStorageConfig()));
            mainClass = new HomeSpawnPlus(factory);
            mainClass.onEnable();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Caught exception loading plugin, shutting down", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    /**
     * Find and load the storage configuration, this is required prior to
     * handing off control to the core injection routines.
     *
     * @return
     * @throws Exception
     */
    private YamlConfiguration getStorageConfig() throws Exception {
        YamlConfiguration storageConfig = new YamlConfiguration();
        YamlConfiguration defaultStorageConfig = new YamlConfiguration();

        // use file if it exists
        File storageConfigFile = new File(getDataFolder(), "config/core.yml");
        if (storageConfigFile.exists()) {
            storageConfig.load(new File(getDataFolder(), "config/core.yml"));
        } else {
            // otherwise try the old-style single config file
            storageConfigFile = new File(getDataFolder(), "config.yml");
            if (storageConfigFile.exists()) {
                storageConfig.load(new File(getDataFolder(), "config.yml"));
            }
            // otherwise use the default file in the JAR
            else {
                storageConfig.load(super.getResource("config/core.yml"));
            }
        }

        // set defaults to in-JAR config to cover any missing values
        defaultStorageConfig.load(super.getResource("config/core.yml"));
        storageConfig.setDefaults(defaultStorageConfig);

        return storageConfig;
    }

    @Override
    public void onDisable() {
        if (mainClass != null)
            mainClass.onDisable();
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        return StorageEBeans.getDatabaseClasses();
    }

    public File _getJarFile() {
        return super.getFile();
    }
}
