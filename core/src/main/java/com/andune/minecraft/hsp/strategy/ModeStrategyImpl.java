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
package com.andune.minecraft.hsp.strategy;

import com.andune.minecraft.hsp.strategy.ModeStrategy;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.andune.minecraft.hsp.strategy.StrategyMode;
import com.andune.minecraft.hsp.strategy.StrategyResult;


/** Base strategy for Strategy Modes. Implements common logic.
 * 
 * @author morganm
 *
 */
public abstract class ModeStrategyImpl extends BaseStrategy implements ModeStrategy {
	@Override
	public abstract StrategyMode getMode();
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.strategy.ModeStrategy#isAdditive()
	 */
	@Override
	public boolean isAdditive() { return false; }
	
	@Override
	public StrategyResult evaluate(StrategyContext context) {
	    context.addMode(this);
		logVerbose("Evaluated mode change strategy, new mode = "+context.getCurrentModes().toString());
		return null;
	}
}
