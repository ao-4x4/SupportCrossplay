package jp.reitou_mugicha.supportCrossplay.command;

import jp.reitou_mugicha.supportCrossplay.DatapackInstaller;
import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import jp.reitou_mugicha.supportCrossplay.data.GeneralConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
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

        if (args.length == 2 && args[0].equalsIgnoreCase("economy"))
        {
            switch (args[1])
            {
                case "off":
                    GeneralConfig.config.set("economy", false);
                    sender.sendMessage(ChatColor.GOLD + "経済用の最適化を " + ChatColor.RED + "オフ" + ChatColor.GOLD + " にしました。");
                    break;
                case "on":
                    GeneralConfig.config.set("economy", true);
                    sender.sendMessage(ChatColor.GOLD + "経済用の最適化を " + ChatColor.GREEN + "オン" + ChatColor.GOLD + " にしました。");
                    break;
            }
            GeneralConfig.saveConfig();
        }
        else if (args.length <= 2 && args[0].equalsIgnoreCase("install"))
        {
            if (args.length == 2)
            {
                World world = Bukkit.getWorld(args[1]);
                if (world == null)
                {
                    sender.sendMessage(ChatColor.RED + "ワールドが存在しません。");
                    return false;
                }

                new DatapackInstaller(SupportCrossplay.getInstance()).installTo(world.getWorldFolder());
                sender.sendMessage(ChatColor.GREEN + world.getName() + "にデータパックをインストールしました。\nワールドを再起動することで適用されます。");

                return true;
            }
            else if (args.length == 1)
            {
                new DatapackInstaller(SupportCrossplay.getInstance()).installForAllWorlds();
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
                    "economy",
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