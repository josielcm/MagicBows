package me.josielcm.magicbows.utils.enable;

import me.josielcm.magicbows.MagicBows;
import me.josielcm.magicbows.commands.MBCmd;
import me.josielcm.magicbows.managers.ArrowManager;
import me.josielcm.magicbows.managers.CancelAction;
import me.josielcm.magicbows.managers.InvManager;

public class Registers {

    public static void regListeners(){
        MagicBows plugin = MagicBows.getPlugin();

        plugin.getServer().getPluginManager().registerEvents(new ArrowManager(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new InvManager(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new CancelAction(plugin), plugin);
    }

    public static void regCommands(){
        MagicBows plugin = MagicBows.getPlugin();

        plugin.getCommand("magicbow").setExecutor(new MBCmd(plugin));
    }
}
