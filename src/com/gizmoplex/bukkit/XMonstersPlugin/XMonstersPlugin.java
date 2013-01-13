package com.gizmoplex.bukkit.XMonstersPlugin;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Monster;
import org.bukkit.plugin.java.JavaPlugin;


public final class XMonstersPlugin extends JavaPlugin
{


  // Private data members
  private File _configFile;
  private YamlConfiguration _config;
  private boolean _monstersEnabled;


  /***
   * Called when the the plugin is disabled.
   */
  @Override
  public void onDisable()
  {
    super.onDisable();

    // Save config data
    savePluginConfig();

  }


  /***
   * Called when the plugin is enabled.
   */
  @Override
  public void onEnable()
  {
    super.onEnable();
    
    PluginCommand cmd;
    

    // Load plugin configuration
    if (!loadPluginConfig())
    {
      getLogger().severe("Unable to load plugin configuration.");
      setEnabled(false);
      return;
    }

    // Apply settings to the game
    applySettings();

    // Register commands
    cmd = getCommand("monsters");
    cmd.setExecutor(new MonstersCommandExecutor());
    cmd.setTabCompleter(new MonstersTabCompleter());

    // Log message the plugin has been loaded
    getLogger().info("XMonsters plugin enabled.");

  }


  /***
   * Loads plugin configuration from config.yaml.
   * 
   * @return If successful, true is returned. Otherwise, false is returned.
   */
  private boolean loadPluginConfig()
  {

    // Create the config file object
    _configFile = new File(getDataFolder() + File.separator + "config.yaml");

    // If config file exists, load it
    if (_configFile.exists())
    {
      // Create new YamlConfiguration object
      _config = YamlConfiguration.loadConfiguration(_configFile);
    }
    else
    {
      _config = new YamlConfiguration();
    }

    // Set default values
    if (!_config.isBoolean("monstersEnabled"))
      _config.set("monstersEnabled", false);

    // Load configuration values
    _monstersEnabled = _config.getBoolean("monstersEnabled");

    // Save the configuration (in case defaults were loaded)
    if (!savePluginConfig())
      return (false);

    // Return successfully
    return (true);

  }


  /***
   * Save plugin configuration to config.yaml.
   * 
   * @return
   */
  private boolean savePluginConfig()
  {

    _config.set("monstersEnabled", _monstersEnabled);

    // Save the configuration to file
    try
    {
      _config.save(_configFile);
    }
    catch (Exception e)
    {
      return (false);
    }

    // Return successfully
    return (true);
  }


  /***
   * Apply plugin settings to the game.
   */
  private void applySettings()
  {
    Iterator<World> i;
    World world;
    Collection<Monster> monsters;
    Iterator<Monster> j;
    Monster monster;

    // For each world
    i = getServer().getWorlds().iterator();
    while (i.hasNext())
    {
      // Get the world
      world = i.next();

      world.setSpawnFlags(_monstersEnabled, world.getAllowAnimals());

      // If monsters not allowed, remove any existing monsters
      if (!_monstersEnabled)
      {
        // Get collection of all monsters
        monsters = world.getEntitiesByClass(Monster.class);

        // Remove each monster
        j = monsters.iterator();
        while (j.hasNext())
        {
          // Get the monster
          monster = j.next();

          // Remove the monster
          monster.remove();
        }

      }

    }

  }


  public class MonstersCommandExecutor implements CommandExecutor
  {


    /***
     * Handles the "monsters" command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
        String[] args)
    {
      String arg;

      // Must be 0 or 1 arguments
      if (args.length != 0 && args.length != 1)
      {
        sender.sendMessage("Invalid number of arguments.");
        return (false);
      }

      // If no arguments
      if (args.length == 0)
      {
        if (_monstersEnabled)
          sender.sendMessage("Monsters are turned on.");
        else
          sender.sendMessage("Monsters are turned off.");

        return (true);
      }
      // Get the command argument
      arg = args[0];

      // Parameter must be "on" or "off"
      if (!arg.equals("on") && !arg.equals("off"))
      {
        sender.sendMessage("Invalid option.");
        return (false);
      }

      // Update the settings
      if (arg.equals("on"))
        _monstersEnabled = true;
      else
        _monstersEnabled = false;

      // Apply monsters setting
      applySettings();

      // Message
      if (_monstersEnabled)
        sender.sendMessage("Monsters turned on.");
      else
        sender.sendMessage("Monsters turned off.");

      // Return successfully
      return (true);

    }

  }


  /***
   * TabCompleter for monsters command
   */
  private class MonstersTabCompleter implements TabCompleter
  {


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd,
        String alias, String[] args)
    {
      List<String> values = new ArrayList<String>();

      // If this is the first argument
      if (args.length == 1)
      {
        values.add("on");
        values.add("off");
      }

      return (values);
      
    }

  }

}