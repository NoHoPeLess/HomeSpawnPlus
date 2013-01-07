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
package com.andune.minecraft.hsp.guice;

import javax.inject.Singleton;

import org.reflections.Reflections;

import com.andune.minecraft.commonlib.i18n.Locale;
import com.andune.minecraft.commonlib.i18n.LocaleStringReplacerImpl;
import com.andune.minecraft.hsp.config.ConfigStorage;
import com.andune.minecraft.hsp.server.api.TeleportOptions;
import com.andune.minecraft.hsp.server.api.event.EventListener;
import com.andune.minecraft.hsp.server.api.impl.TeleportOptionsImpl;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.StorageFactory;
import com.andune.minecraft.hsp.storage.dao.HomeDAO;
import com.andune.minecraft.hsp.storage.dao.PlayerDAO;
import com.andune.minecraft.hsp.storage.dao.PlayerSpawnDAO;
import com.andune.minecraft.hsp.storage.dao.SpawnDAO;
import com.andune.minecraft.hsp.strategy.StrategyConfig;
import com.andune.minecraft.hsp.strategy.StrategyConfigImpl;
import com.andune.minecraft.hsp.strategy.StrategyEngine;
import com.andune.minecraft.hsp.strategy.StrategyEngineImpl;
import com.andune.minecraft.hsp.strategy.StrategyResultFactory;
import com.andune.minecraft.hsp.strategy.StrategyResultFactoryImpl;
import com.andune.minecraft.hsp.util.BedUtils;
import com.andune.minecraft.hsp.util.BedUtilsImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;

/** Guice IoC Injector for HomeSpawnPlus.
 * 
 * @author morganm
 *
 */
public class HSPModule extends AbstractModule {
    private final ConfigStorage configStorage;
    private Reflections reflections;
    
    public HSPModule(ConfigStorage configStorage) {
        this.configStorage = configStorage;
    }

    /* (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(EventListener.class)
            .to(com.andune.minecraft.hsp.EventListener.class);
        bind(BedUtils.class)
            .to(BedUtilsImpl.class);
        bind(Locale.class)
            .to(LocaleStringReplacerImpl.class)
            .in(Scopes.SINGLETON);
        bind(StrategyResultFactory.class)
            .to(StrategyResultFactoryImpl.class);
        bind(TeleportOptions.class)
            .to(TeleportOptionsImpl.class);
        bind(StrategyEngine.class)
            .to(StrategyEngineImpl.class);
        bind(StrategyConfig.class)
            .to(StrategyConfigImpl.class);
    }
    
    @Provides
    @Singleton
    protected ConfigStorage getConfigStorage() {
        return configStorage;
    }

    @Provides
    @Singleton
    protected Storage getStorage(StorageFactory storageFactory) {
        return storageFactory.getInstance();
    }

    @Provides
    @Singleton
    protected Reflections provideReflections() {
        if( reflections == null )
            reflections = Reflections.collect();
        return reflections;
    }
    
    @Provides
    @Singleton
    protected SpawnDAO provideSpawnDAO(Storage storage) {
        return storage.getSpawnDAO();
    }

    @Provides
    @Singleton
    protected PlayerDAO providePlayerDAO(Storage storage) {
        return storage.getPlayerDAO();
    }

    @Provides
    @Singleton
    protected PlayerSpawnDAO providePlayerSpawnDAO(Storage storage) {
        return storage.getPlayerSpawnDAO();
    }

    @Provides
    @Singleton
    protected HomeDAO provideHomeDAO(Storage storage) {
        return storage.getHomeDAO();
    }
}
