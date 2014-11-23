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
package com.andune.minecraft.hsp.strategies.home;

import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.strategy.*;

import java.util.Set;

/**
 * @author andune
 */
@NoArgStrategy
public class HomeNearestHome extends HomeStrategy {

    @Override
    public StrategyResult evaluate(StrategyContext context) {
        // simple algorithm for now, it's not called that often and we assume the list
        // of homes is relatively small (ie. not in the hundreds or thousands).
        final Set<? extends Home> allHomes = homeDAO.findHomesByWorldAndPlayer(
                context.getEventLocation().getWorld().getName(), context.getPlayer().getName());
        final Location playerLoc = context.getEventLocation();

        double shortestDistance = -1;
        Home closestHome = null;
        for (Home theHome : allHomes) {
            if (context.isModeEnabled(StrategyMode.MODE_HOME_NO_BED) && theHome.isBedHome())
                continue;

            if (context.isModeEnabled(StrategyMode.MODE_HOME_REQUIRES_BED) && !isBedNearby(theHome)) {
                logVerbose("Home ", theHome, " skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
                continue;
            }

            final Location theLocation = theHome.getLocation();
            if (theLocation.getWorld().equals(playerLoc.getWorld())) {    // must be same world
                double distance = theLocation.distance(playerLoc);
                if (distance < shortestDistance || shortestDistance == -1) {
                    shortestDistance = distance;
                    closestHome = theHome;
                }
            }
        }

        return new StrategyResultImpl(closestHome);
    }

    @Override
    public String getStrategyConfigName() {
        return "homeNearest";
    }

}
