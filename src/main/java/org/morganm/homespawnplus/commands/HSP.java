/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2012 Mark Morgan.
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
 * 
 * Contributors:
 *     Mark Morgan - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.morganm.homespawnplus.commands;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.entity.UUIDHistory;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.HomeDAO;
import org.morganm.homespawnplus.storage.yaml.HomeDAOYaml;
import org.morganm.homespawnplus.storage.yaml.SpawnDAOYaml;
import org.morganm.homespawnplus.storage.yaml.StorageYaml;

/**
 * @author morganm
 *
 */
public class HSP extends BaseCommand {
	private static final Logger log = HomeSpawnPlus.log;
	private String logPrefix;
	
	@Override
	public String getUsage() {
		return	util.getLocalizedMessage(HSPMessages.CMD_HSP_USAGE);
	}

	@Override
	public org.morganm.homespawnplus.command.Command setPlugin(HomeSpawnPlus plugin) {
		this.logPrefix = HomeSpawnPlus.logPrefix;
		return super.setPlugin(plugin);
	}

	@Override
	public boolean execute(ConsoleCommandSender console, org.bukkit.command.Command command, String[] args) {
		return executePrivate(console, command, args);
	}

	@Override
	public boolean execute(Player p, Command command, String[] args) {
		return executePrivate(p, command, args);
	}
	
	private boolean executePrivate(CommandSender p, Command command, String[] args) {
		if( !isEnabled() || !plugin.hasPermission(p, HomeSpawnPlus.BASE_PERMISSION_NODE+".admin") )
			return false;
		
		if( args.length < 1 ) {
			return false;
//			printUsage(p, command);
		}
		// admin command to clean up any playerName-case-caused dups
		else if( args[0].startsWith("dedup") || args[0].equals("dd") ) {
			p.sendMessage("Starting async HSP database home playerName dup cleanup");
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new DeDupDatabaseRunner(p));
		}
		// admin command to switch all database player names to lowercase
		else if( args[0].startsWith("lowercase") || args[0].equals("lc") ) {
			p.sendMessage("Starting async HSP database playerName-to-lowercase conversion");
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new LowerCaseDatabaseRunner(p));
		}
		else if( args[0].startsWith("reloadc") || args[0].equals("rc") ) {
			boolean success = false;
			try {
				plugin.loadConfig(true);
				
				// also call hookWarmups, in case admin changed the warmup settings
				plugin.hookWarmups();
				
				success = true;
			}
			catch(Exception e) {
				e.printStackTrace();
				util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_ERROR_RELOADING);
//				util.sendMessage(p, "Error loading config data, not successful.  Check your server logs.");
			}
			
			if( success )
				util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_CONFIG_RELOADED);
//				util.sendMessage(p, "Config data reloaded.");
		}
		else if( args[0].startsWith("reloadd") || args[0].equals("rd") ) {
			// purge the existing cache
			plugin.getStorage().purgeCache();
			// now reload it by grabbing all of the objects
			plugin.getStorage().getHomeDAO().findAllHomes();
			plugin.getStorage().getSpawnDAO().findAllSpawns();

			util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_DATA_RELOADED);
//			util.sendMessage(p, "Data cache purged and reloaded");
		}
		else if( args[0].equals("test") ) {
			org.morganm.homespawnplus.entity.Home home = plugin.getStorage().getHomeDAO().findDefaultHome("world", "morganm");
			p.sendMessage("Found home with id "+home.getId());
			org.morganm.homespawnplus.entity.Spawn spawn = plugin.getStorage().getSpawnDAO().findSpawnById(1);
			p.sendMessage("Found spawn with id "+spawn.getId());
			try {
				Float yaw = Float.valueOf(new Random(System.currentTimeMillis()).nextInt(360));
				File file = new File("plugins/HomeSpawnPlus/data.yml");
				
				HomeDAOYaml homeDAO = new HomeDAOYaml(file);
				homeDAO.load();
				home.setYaw(yaw);
				homeDAO.saveHome(home);
				
				home = homeDAO.findDefaultHome("world", "morganm");
				p.sendMessage("YML: Found home with yaw "+home.getYaw());

				SpawnDAOYaml spawnDAO = new SpawnDAOYaml(file);
				spawnDAO.load();
				spawn.setYaw(yaw);
				spawnDAO.saveSpawn(spawn);
				
				spawn = spawnDAO.findSpawnById(1);
				p.sendMessage("YML: Found spawn with yaw "+spawn.getYaw());
			}
			catch(Exception e) {
				p.sendMessage("Caught exception: "+e.getMessage());
				e.printStackTrace();
			}
		}
        // UUID history lookup
        else if( args[0].equals("uhl") ) {
            new UUIDHistoryLookup(p, args).run();
        }
		else if( args[0].equals("test2") ) {
			Set<org.morganm.homespawnplus.entity.Home> allHomes = plugin.getStorage().getHomeDAO().findAllHomes();
			p.sendMessage("allHomes.size="+allHomes.size());
		}
//		else if( args[0].equals("domap") ) {
//			CommandRegister register = new CommandRegister(plugin);
//			register.register(new HspTest());
//		}
		else if( args[0].startsWith("backup") ) {
			Storage storage = plugin.getStorage();

			File backupFile = new File(HomeSpawnPlus.YAML_BACKUP_FILE);
			if( backupFile.exists() )
				backupFile.delete();
			
			try {
				StorageYaml backupStorage = new StorageYaml(plugin, true, backupFile);
				backupStorage.initializeStorage();

				backupStorage.setDeferredWrites(true);
				for(org.morganm.homespawnplus.entity.Home o : storage.getHomeDAO().findAllHomes()) {
					debug.devDebug("backing up Home object id ",o.getId());
					backupStorage.getHomeDAO().saveHome(o);
				}
				for(org.morganm.homespawnplus.entity.Spawn o : storage.getSpawnDAO().findAllSpawns()) {
					debug.devDebug("backing up Spawn object id ",o.getId());
					backupStorage.getSpawnDAO().saveSpawn(o);
				}
				for(org.morganm.homespawnplus.entity.Player o : storage.getPlayerDAO().findAllPlayers()) {
					debug.devDebug("backing up Player object id ",o.getId());
					backupStorage.getPlayerDAO().savePlayer(o);
				}
				for(org.morganm.homespawnplus.entity.HomeInvite o : storage.getHomeInviteDAO().findAllHomeInvites()) {
					debug.devDebug("backing up HomeInvite object id ",o.getId());
					backupStorage.getHomeInviteDAO().saveHomeInvite(o);
				}

				backupStorage.flushAll();
	
				util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_DATA_BACKED_UP, "file", HomeSpawnPlus.YAML_BACKUP_FILE);
				log.info(logPrefix+" Data backed up to file "+HomeSpawnPlus.YAML_BACKUP_FILE);
			}
			catch(StorageException e) {
				log.warning(logPrefix+" Error saving backup file"+e.getMessage());
				e.printStackTrace();
				util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_DATA_BACKUP_ERROR);
			}
		}
		else if( args[0].startsWith("restore") ) {
			if( args.length < 2 || (!"OVERWRITE".equals(args[1])
					&& !("me".equals(args[1]) && p instanceof ConsoleCommandSender)) ) {	// testing shortcut
				util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_DATA_RESTORE_USAGE, "file", HomeSpawnPlus.YAML_BACKUP_FILE);
//				util.sendMessage(p, "In order to start restore you must send the command \"/hsp restore OVERWRITE\"");
//				util.sendMessage(p, "THIS WILL OVERWRITE EXISTING DATA and restore data from file "+HomeSpawnPlus.YAML_BACKUP_FILE);
			}
			else {
				File backupFile = new File(HomeSpawnPlus.YAML_BACKUP_FILE);
				if( backupFile.exists() ) {
					final Storage storage = plugin.getStorage();
					try {
						StorageYaml backupStorage = new StorageYaml(plugin, true, backupFile);
						backupStorage.initializeStorage();
						
						storage.deleteAllData();
						storage.setDeferredWrites(true);
						
						Set<org.morganm.homespawnplus.entity.Home> homes = backupStorage.getHomeDAO().findAllHomes();
						for(org.morganm.homespawnplus.entity.Home home : homes) {
							debug.devDebug("Restoring home ",home);
							home.setLastModified(null);
							storage.getHomeDAO().saveHome(home);
						}
						Set<org.morganm.homespawnplus.entity.Spawn> spawns = backupStorage.getSpawnDAO().findAllSpawns();
						for(org.morganm.homespawnplus.entity.Spawn spawn : spawns) {
							debug.devDebug("Restoring spawn ",spawn);
							spawn.setLastModified(null);
							storage.getSpawnDAO().saveSpawn(spawn);
						}
						Set<org.morganm.homespawnplus.entity.Player> players = backupStorage.getPlayerDAO().findAllPlayers();
						for(org.morganm.homespawnplus.entity.Player player : players) {
							debug.devDebug("Restoring player ",player);
							player.setLastModified(null);
							storage.getPlayerDAO().savePlayer(player);
						}
						Set<org.morganm.homespawnplus.entity.HomeInvite> homeInvites = backupStorage.getHomeInviteDAO().findAllHomeInvites();
						for(org.morganm.homespawnplus.entity.HomeInvite homeInvite : homeInvites) {
							debug.devDebug("Restoring homeInvite ",homeInvite);
							homeInvite.setLastModified(null);
							storage.getHomeInviteDAO().saveHomeInvite(homeInvite);
						}
						
						storage.flushAll();
					}
					catch(StorageException e) {
						util.sendMessage(p, "Caught exception: "+e.getMessage());
						log.log(Level.WARNING, "Error caught in /"+getCommandName()+": "+e.getMessage(), e);
					}
					finally {
						storage.setDeferredWrites(false);
					}
					
					util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_DATA_RESTORE_SUCCESS, "file", HomeSpawnPlus.YAML_BACKUP_FILE);
//					util.sendMessage(p, "Existing data wiped and data restored from file "+HomeSpawnPlus.YAML_BACKUP_FILE);
					log.info(logPrefix+" Existing data wiped and data restored from file "+HomeSpawnPlus.YAML_BACKUP_FILE);
				}
				else
					util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_DATA_RESTORE_NO_FILE, "file", HomeSpawnPlus.YAML_BACKUP_FILE);
//					util.sendMessage(p, "Backup file not found, aborting restore (no data deleted). [file = "+HomeSpawnPlus.YAML_BACKUP_FILE+"]");
			}
		}
		else {
			return false;
//			printUsage(p, command);
		}

		return true;
	}

    private class UUIDHistoryLookup implements Runnable {
        final private CommandSender sender;
        final private String[] args;

        public UUIDHistoryLookup(CommandSender sender, String[] args) {
            this.sender = sender;
            this.args = args;
        }

        public void run() {
            if( args.length > 1 ) {
                final String playerName = args[1];

                UUID uuid = null;
                Set<UUIDHistory> set = null;
                try {
                    uuid = UUID.fromString(args[1]);
                    set = plugin.getStorage().getUUIDHistoryDAO().findByUUID(uuid);
                } catch(IllegalArgumentException e) {
                    // If UUID conversion failed, then we assume it's a player name
                    set = plugin.getStorage().getUUIDHistoryDAO().findByName(playerName);
                }


                if ((set == null || set.size() == 0)) {
                    if( uuid != null ) {
                        sender.sendMessage("No player names matching UUID " + uuid + " were found");
                    } else {
                        sender.sendMessage("No UUIDs matching player name " + playerName + " were found");
                    }

                    return;
                }

                if( uuid != null ) {
                    sender.sendMessage("Player name history for UUID "+uuid+":");
                } else {
                    sender.sendMessage("UUID history for player name "+playerName+":");
                }
                for(UUIDHistory uuidHistory : set) {
                    sender.sendMessage("  "
                            +uuidHistory.getDateCreated().toString()
                            +": "
                            + ((uuid == null) ? uuidHistory.getUUIDString() : uuidHistory.getName())
                    );
                }
            } else {
                sender.sendMessage("Player name or UUID argument required.");
            }
        }
    }
	
	private class DeDupDatabaseRunner implements Runnable {
		private CommandSender sender;
		
		public DeDupDatabaseRunner(CommandSender sender) {
			this.sender = sender;
		}
		
		public void run() {
			debug.debug("DeDupDatabaseRunner running");
			int dupsCleaned=0;
			try {
				plugin.getStorage().setDeferredWrites(true);
				final HomeDAO homeDAO = plugin.getStorage().getHomeDAO();

				// we fix dups by player, so we can keep the most recent home in the
				// event of duplicates. So we keep track of player homes we have fixed
				// so we can skip any others we come across as we're iterating the
				// allHomes hash.
				HashSet<String> playersFixed = new HashSet<String>(100);

				Set<org.morganm.homespawnplus.entity.Home> allHomes = homeDAO.findAllHomes();
				for(org.morganm.homespawnplus.entity.Home home : allHomes) {
					final String lcPlayerName = home.getPlayerName().toLowerCase();
					if( playersFixed.contains(lcPlayerName) )
						continue;
					debug.debug("home check for home name ",lcPlayerName);

					final HashMap<String, org.morganm.homespawnplus.entity.Home> dupCheck = new HashMap<String, org.morganm.homespawnplus.entity.Home>();
					Set<org.morganm.homespawnplus.entity.Home> playerHomes = homeDAO.findHomesByPlayer(lcPlayerName);
					// look for duplicates and delete all but the newest if we find any
					for(org.morganm.homespawnplus.entity.Home playerHome : playerHomes) {
						final String homeName = playerHome.getName();
						debug.debug("dup check for home name \"",homeName,"\"");
						// ignore no-name homes, they don't have to be unique
						if( homeName == null )
							continue;

						// have we seen this home before?
						org.morganm.homespawnplus.entity.Home dup = dupCheck.get(homeName);
						if( dup != null ) {
							debug.debug("found dup for home ",homeName);
							// determine which one is oldest and delete the oldest one
							if( dup.getLastModified().getTime() < playerHome.getLastModified().getTime() ) {
								// dup is oldest, delete it
								log.info("Deleting oldest duplicate home (id "+dup.getId()+", name "+dup.getName()+") for player "+lcPlayerName);
								homeDAO.deleteHome(dup);
								dupCheck.put(homeName, playerHome);	// record new record in our dup hash
							}
							else {
								// playerHome is oldest, delete it
								log.info("Deleting oldest duplicate home (id "+playerHome.getId()+", name "+playerHome.getName()+") for player "+lcPlayerName);
								homeDAO.deleteHome(playerHome);
							}
							dupsCleaned++;
						}
						else {
							debug.debug("no dup found for home ",homeName);
							dupCheck.put(homeName, playerHome);	// we have now, record it
						}
					}

					playersFixed.add(lcPlayerName);
				}

				plugin.getStorage().flushAll();
			}
			catch(StorageException e) {
				log.log(Level.SEVERE, "Caught exception processing /hsp dedup", e);
			}
			finally {
				plugin.getStorage().setDeferredWrites(false);
			}

			sender.sendMessage("Database playerName dups have been cleaned up. "+dupsCleaned+" total dups found and cleaned");
		}
	}
	
	private class LowerCaseDatabaseRunner implements Runnable {
		private CommandSender sender;
		
		public LowerCaseDatabaseRunner(CommandSender sender) {
			this.sender = sender;
		}
		
		public void run() {
			debug.debug("LowerCaseDatabaseRunner running");
			int conversions=0;
			try {
				plugin.getStorage().setDeferredWrites(true);
				final HomeDAO homeDAO = plugin.getStorage().getHomeDAO();

				// we fix names by player, so we can keep the most recent home in the
				// event of duplicates. So we keep track of player homes we have fixed
				// so we can skip any others we come across as we're iterating the
				// allHomes hash.
				HashSet<String> playersFixed = new HashSet<String>(100);

				Set<org.morganm.homespawnplus.entity.Home> allHomes = homeDAO.findAllHomes();
				for(org.morganm.homespawnplus.entity.Home home : allHomes) {
					final String lcPlayerName = home.getPlayerName().toLowerCase();
					if( playersFixed.contains(lcPlayerName) )
						continue;
					debug.debug("home check for home name ",lcPlayerName);

					final Set<org.morganm.homespawnplus.entity.Home> playerHomes = homeDAO.findHomesByPlayer(lcPlayerName);
					for(org.morganm.homespawnplus.entity.Home playerHome : playerHomes) {
						// set home playerName to lower case if it's not already
						if( !lcPlayerName.equals(playerHome.getPlayerName()) ) {
							log.info("Fixing playerName to lowerCase for home id "+playerHome.getId()+", home name "+playerHome.getName()+" for player "+lcPlayerName);
							playerHome.setPlayerName(lcPlayerName);
							homeDAO.saveHome(playerHome);
							conversions++;
						}
					}

					playersFixed.add(lcPlayerName);
				}

				plugin.getStorage().flushAll();
			}
			catch(StorageException e) {
				log.log(Level.SEVERE, "Caught exception processing /hsp lc conversion", e);
			}
			finally {
				plugin.getStorage().setDeferredWrites(false);
			}

			sender.sendMessage("Database playerNames converted to lowerCase complete. Processed "+conversions+" conversions");
		}
	}
	
//	private void printUsage(CommandSender p, Command command) {
//		util.sendMessage(p, command.getUsage());
//		
////		util.sendMessage(p, "Usage:");
////		util.sendMessage(p, "/"+getCommandName()+" reloadconfig - reload config files");
////		util.sendMessage(p, "/"+getCommandName()+" reloaddata - force reloading of plugin data from database");
////		util.sendMessage(p, "/"+getCommandName()+" backup - backup database to a file");
////		util.sendMessage(p, "/"+getCommandName()+" restore - restore database from a file");
//	}

}
