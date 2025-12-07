package jp.reitou_mugicha.supportCrossplay.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandCurrentPosition implements CommandExecutor
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("このコマンドはプレイヤーのみが実行可能です。", NamedTextColor.RED));
            return false;
        }

        double x = player.getLocation().getX();
        double y = player.getLocation().getY();
        double z = player.getLocation().getZ();

        Component message = Component.text("======= " + sender.getName() + "の現在位置 =======\n", NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text("X: ", NamedTextColor.AQUA))
                .append(Component.text(String.format("%.2f", x), NamedTextColor.WHITE))
                .append(Component.text("  Y: ", NamedTextColor.AQUA))
                .append(Component.text(String.format("%.2f", y), NamedTextColor.WHITE))
                .append(Component.text("  Z: ", NamedTextColor.AQUA))
                .append(Component.text(String.format("%.2f", z), NamedTextColor.WHITE))
                .append(Component.text("\n==================================", NamedTextColor.GOLD, TextDecoration.BOLD));

        Bukkit.broadcast(message);
        return true;
    }
}
