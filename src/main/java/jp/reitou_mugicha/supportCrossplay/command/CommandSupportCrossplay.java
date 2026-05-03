package jp.reitou_mugicha.supportCrossplay.command;

import jp.reitou_mugicha.supportCrossplay.DatapackInstaller;
import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandSupportCrossplay implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args)
    {
        if (args.length == 0)
        {
            sender.sendMessage(ChatColor.GOLD + "======== Support Crossplay ========\n" + ChatColor.GREEN + "Author: " + ChatColor.RED + "reitou_mugicha\n" + ChatColor.GREEN + "Version: " + ChatColor.RED + SupportCrossplay.getInstance().getDescription().getVersion() + "\n" + ChatColor.GOLD + "=======================");
        }

        if (args.length <= 2 && args[0].equalsIgnoreCase("install"))
        {
            if (args.length == 2)
            {
                World world = Bukkit.getWorld(args[1]);
                if (world == null)
                {
                    sender.sendMessage(ChatColor.RED + "ワールドが存在しません。");
                    return false;
                }

                new DatapackInstaller(SupportCrossplay.getInstance()).installWorld(world.getWorldFolder());
                sender.sendMessage(ChatColor.GREEN + world.getName() + "にデータパックをインストールしました。\nワールドを再起動することで適用されます。");

                return true;
            }
            else if (args.length == 1)
            {
                new DatapackInstaller(SupportCrossplay.getInstance()).installServer();
                sender.sendMessage(ChatColor.GREEN + "すべてのワールドにデータパックをインストールしました。\nワールドを再起動することで適用されます。");
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args)
    {
        if (args.length == 1)
        {
            return List.of(
                    "install"
            );
        }

        if (args.length == 2)
        {
            if (args[0].equalsIgnoreCase("economy"))
            {
                return List.of("on", "off");
            }

            if (args[0].equalsIgnoreCase("install"))
            {
                return Bukkit.getWorlds()
                        .stream()
                        .map(World::getName)
                        .toList();
            }
        }

        return List.of();
    }
}