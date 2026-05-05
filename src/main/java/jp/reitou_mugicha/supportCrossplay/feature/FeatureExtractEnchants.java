package jp.reitou_mugicha.supportCrossplay.feature;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FeatureExtractEnchants implements Listener
{
    private final Component guiTitle = Component.text("エンチャントの分離", NamedTextColor.DARK_GRAY);
    private final int TOOL_SLOT = 11;
    private final int BOOK_SLOT = 15;
    private final int EXECUTE_SLOT = 22;

    @EventHandler
    public void onGrindstoneOpen(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.GRINDSTONE) {
                Player player = event.getPlayer();
                if (player.isSneaking()) {
                    event.setCancelled(true);
                    openCustomGui(player);
                }
            }
        }
    }

    private void openCustomGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, guiTitle);

        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta paneMeta = pane.getItemMeta();
        paneMeta.displayName(Component.text(" "));
        pane.setItemMeta(paneMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, pane);
        }

        inv.setItem(TOOL_SLOT, new ItemStack(Material.AIR));
        inv.setItem(BOOK_SLOT, new ItemStack(Material.AIR));

        ItemStack button = new ItemStack(Material.ANVIL);
        ItemMeta buttonMeta = button.getItemMeta();
        buttonMeta.displayName(Component.text("分離する", NamedTextColor.GOLD));
        button.setItemMeta(buttonMeta);
        inv.setItem(EXECUTE_SLOT, button);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(guiTitle)) return;

        int slot = event.getRawSlot();
        if (slot < 36 && (slot != TOOL_SLOT && slot != BOOK_SLOT && slot != EXECUTE_SLOT)) {
            event.setCancelled(true);
            return;
        }

        if (slot == EXECUTE_SLOT) {
            event.setCancelled(true);
            handleExtraction((Player) event.getWhoClicked(), event.getInventory());
        }
    }

    private void handleExtraction(Player player, Inventory inv) {
        ItemStack tool = inv.getItem(TOOL_SLOT);
        ItemStack books = inv.getItem(BOOK_SLOT);

        if (tool == null || tool.getType() == Material.AIR || !tool.hasItemMeta()) {
            player.sendMessage(Component.text("ツールをセットしてください。", NamedTextColor.RED));
            return;
        }

        ItemMeta meta = tool.getItemMeta();
        Map<Enchantment, Integer> enchants = meta.getEnchants();

        List<Map.Entry<Enchantment, Integer>> targetEnchants = new ArrayList<>();
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            if (!entry.getKey().isCursed()) {
                targetEnchants.add(entry);
            }
        }

        if (targetEnchants.isEmpty()) {
            player.sendMessage(Component.text("分離するエンチャントがありません。", NamedTextColor.RED));
            return;
        }

        int requiredBooks = targetEnchants.size();
        if (books == null || books.getType() != Material.BOOK || books.getAmount() < requiredBooks) {
            player.sendMessage(Component.text("本が足りません。" + requiredBooks + "冊必要です。", NamedTextColor.RED));
            return;
        }

        for (Map.Entry<Enchantment, Integer> entry : targetEnchants) {
            ItemStack enchBook = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) enchBook.getItemMeta();
            bookMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
            enchBook.setItemMeta(bookMeta);

            player.getInventory().addItem(enchBook).forEach((i, item) ->
                    player.getWorld().dropItemNaturally(player.getLocation(), item));

            meta.removeEnchant(entry.getKey());
        }

        tool.setItemMeta(meta);
        books.setAmount(books.getAmount() - requiredBooks);

        player.sendMessage(Component.text("分離が完了しました！", NamedTextColor.GREEN));
        player.closeInventory();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().title().equals(guiTitle)) return;

        Inventory inv = event.getInventory();
        Player player = (Player) event.getPlayer();

        ItemStack tool = inv.getItem(TOOL_SLOT);
        ItemStack books = inv.getItem(BOOK_SLOT);

        if (tool != null) player.getInventory().addItem(tool).forEach((i, item) -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        if (books != null) player.getInventory().addItem(books).forEach((i, item) -> player.getWorld().dropItemNaturally(player.getLocation(), item));

        inv.clear();
    }
}