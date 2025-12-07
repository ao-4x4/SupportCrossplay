package jp.reitou_mugicha.supportCrossplay.command;

import jp.reitou_mugicha.supportCrossplay.entity.EntitySnowKiller;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandSCSpawn implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "使い方: /scspawn <mob_type> [x, y, z] | [player]");
            return true;
        }

        String mobType = args[0].toLowerCase();
        Location spawnLocation = null;

        if (args.length == 1)
        {
            if (sender instanceof Player player)
            {
                spawnLocation = player.getLocation();
            }
            else
            {
                sender.sendMessage(Component.text(ChatColor.RED + "コンソールから実行する際は座標を指定してください。"));
                return true;
            }
        }
        else if (args.length == 4)
        {
            try {
                double x = Double.parseDouble(args[1]);
                double y = Double.parseDouble(args[2]);
                double z = Double.parseDouble(args[3]);

                World world;
                if (sender instanceof Player player) {
                    world = player.getWorld();
                } else {
                    world = Bukkit.getWorlds().get(0);
                }

                spawnLocation = new Location(world, x, y, z);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "座標は数値で指定してください。");
                return true;
            }
        }
        else if (args.length == 2)
        {
            String playerName = args[1];
            Player player = Bukkit.getPlayer(playerName);
            spawnLocation = player.getLocation();
        }
        else
        {
            sender.sendMessage(ChatColor.RED + "使い方: /scspawn <mob_type> [x y z]");
            return true;
        }

        if (spawnLocation == null) return true;

        switch (mobType)
        {
            case "snow_killer":
                new EntitySnowKiller(spawnLocation);
                break;
            default:
                sender.sendMessage(Component.text(ChatColor.RED + "未対応のMOBです。"));
                return true;
        }

        sender.sendMessage(Component.text(ChatColor.GREEN + mobType + "をスポーンしました。"));

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args)
    {
        List<String> list = new ArrayList<>();

        if (args.length == 1)
        {
            list.add("snow_killer");
            return filter(list, args[0]);
        }

        if (args.length == 2)
        {
            for (Player player : Bukkit.getOnlinePlayers())
            {
                list.add(player.getName());
            }
            return filter(list, args[1]);
        }

        if (args.length == 2 || args.length == 3 || args.length == 4)
        {
            if (sender instanceof Player player)
            {
                Location loc = player.getLocation();
                list.add(String.valueOf(loc.getBlockX()));
                list.add(String.valueOf(loc.getBlockY()));
                list.add(String.valueOf(loc.getBlockZ()));
            }
            return filter(list, args[args.length - 1]);
        }

        return list;
    }

    private List<String> filter(List<String> list, String arg)
    {
        List<String> result = new ArrayList<>();
        for (String s : list)
        {
            if (s.toLowerCase().startsWith(arg.toLowerCase()))
            {
                result.add(s);
            }
        }
        return result;
    }
}