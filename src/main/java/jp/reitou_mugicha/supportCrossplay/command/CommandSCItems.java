package jp.reitou_mugicha.supportCrossplay.command;

import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import jp.reitou_mugicha.supportCrossplay.item.ItemShikattoStick;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandSCItems implements CommandExecutor, Listener
{
    private static final String TITLE = "SupportCrossplay Items";
    private static final int SlotSize = 9 * 6;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.isOp())
        {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        Inventory gui = Bukkit.createInventory(null, SlotSize, TITLE);

        for (ItemStack item : SupportCrossplay.getCustomItems()) gui.addItem(item);

        player.openInventory(gui);

        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (!event.getView().getTitle().equals(TITLE)) return;

        Player player = (Player) event.getWhoClicked();

        if (player.isOp())
        {
            if (event.getRawSlot() >= 0 && event.getRawSlot() <= SlotSize - 1)
            {
                ItemStack item = event.getCurrentItem().clone();
                item.setAmount(1);
                player.getInventory().addItem(item);
            }
        }

        event.setCancelled(true);
    }
}