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
package com.andune.minecraft.hsp.strategies;

import javax.inject.Inject;


import com.andune.minecraft.hsp.integration.worldguard.WorldGuardInterface;
import com.andune.minecraft.hsp.integration.worldguard.WorldGuardModule;
import com.andune.minecraft.hsp.server.bukkit.BukkitLocation;
import com.andune.minecraft.hsp.strategy.BaseStrategy;
import com.andune.minecraft.hsp.strategy.NoArgStrategy;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.andune.minecraft.hsp.strategy.StrategyException;
import com.andune.minecraft.hsp.strategy.StrategyResult;
import com.andune.minecraft.hsp.strategy.StrategyResultImpl;

/** Spawn inside the WorldGuard region using the WorldGuard flag.
 * 
 * This strategy requires WorldGuard and is specific to Bukkit.
 * 
 * @author morganm
 *
 */
@NoArgStrategy
public class SpawnWorldGuardRegion extends BaseStrategy {
    @Inject private WorldGuardModule worldGuard;
    private WorldGuardInterface wgInterface;

	@Override
	public StrategyResult evaluate(StrategyContext context) {
        if( !worldGuard.isEnabled() ) {
            log.warn("Attempted to use "+getStrategyConfigName()+" without WorldGuard installed. Strategy ignored.");
            return null;
        }
        
		if( wgInterface == null )
			wgInterface = worldGuard.getWorldGuardInterface();

		org.bukkit.Location bukkitLocation = wgInterface.getWorldGuardSpawnLocation(context.getPlayer());
		return new StrategyResultImpl( new BukkitLocation(bukkitLocation) );
	}
	
	@Override
	public void validate() throws StrategyException {
        if( !worldGuard.isEnabled() )
            throw new StrategyException("Attempted to use "+getStrategyConfigName()+" without WorldGuard installed. Strategy ignored.");
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnWGregion";
	}

}
