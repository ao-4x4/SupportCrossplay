package jp.reitou_mugicha.supportCrossplay.feature;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class FeatureRerollMerchant implements Listener {

    private final JavaPlugin plugin;
    
    private static class TargetInfo {
        Enchantment enchantment;
        int level;
        TargetInfo(Enchantment e, int l) { this.enchantment = e; this.level = l; }
    }

    private final Map<UUID, TargetInfo> playerTarget = new HashMap<>();
    private final Map<UUID, Enchantment> pendingEnchant = new HashMap<>();
    private final Map<UUID, Integer> playerPage = new HashMap<>();

    private final NamespacedKey enchantKey;
    private final NamespacedKey actionKey;
    private final NamespacedKey levelKey;

    public FeatureRerollMerchant(JavaPlugin plugin) {
        this.plugin = plugin;
        this.enchantKey = new NamespacedKey(plugin, "target_enchant");
        this.actionKey = new NamespacedKey(plugin, "gui_action");
        this.levelKey = new NamespacedKey(plugin, "target_level");
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.LECTERN) return;

        Player player = event.getPlayer();
        if (player.isSneaking()) {
            event.setCancelled(true);
            openEnchantSelector(player, 0);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Villager villager)) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (mainHand.getType() != Material.LECTERN) return;
        if (villager.getVillagerLevel() > 1) return;

        Villager.Profession oldProfession = villager.getProfession();
        if (oldProfession != Villager.Profession.LIBRARIAN) return;

        villager.setProfession(Villager.Profession.NONE);
        villager.setProfession(oldProfession);

        checkTrades(player, villager);
    }

    private void checkTrades(Player player, Villager villager) {
        TargetInfo target = playerTarget.get(player.getUniqueId());
        if (target == null) {
            player.sendMessage(ChatColor.GRAY + "取引を再抽選しました。");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            return;
        }

        boolean found = false;
        for (MerchantRecipe recipe : villager.getRecipes()) {
            ItemStack result = recipe.getResult();
            if (result.getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
                
                if (meta != null && meta.getStoredEnchantLevel(target.enchantment) == target.level) {
                    found = true;
                    break;
                }
            }
        }

        if (found) {
            playerTarget.remove(player.getUniqueId());

            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.getType() == Material.LECTERN) {
                int emptySlot = -1;
                for (int i = 0; i < 36; i++) {
                    if (player.getInventory().getItem(i) == null || player.getInventory().getItem(i).getType() == Material.AIR) {
                        emptySlot = i;
                        break;
                    }
                }

                if (emptySlot != -1) {
                    player.getInventory().setItem(emptySlot, itemInHand.clone());
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                } else {
                    player.getWorld().dropItemNaturally(player.getLocation(), itemInHand.clone());
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                }
            }

            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "ターゲットが出現しました。");
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        } else {
            player.sendMessage(ChatColor.GREEN + "取引を再抽選しました。");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

    public void openEnchantSelector(Player player, int page) {
        playerPage.put(player.getUniqueId(), page);
        List<Enchantment> allEnchants = new ArrayList<>();
        Registry.ENCHANTMENT.forEach(allEnchants::add);
        allEnchants.sort(Comparator.comparing(e -> e.getKey().getKey()));

        Inventory gui = Bukkit.createInventory(null, 54, "ターゲットを選択");
        int start = page * 45;
        int end = Math.min(start + 45, allEnchants.size());

        for (int i = start; i < end; i++) {
            Enchantment ench = allEnchants.get(i);
            ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            if (meta != null) {
                meta.addStoredEnchant(ench, 1, true);
                meta.setDisplayName(ChatColor.YELLOW + ench.getKey().getKey().toUpperCase());
                meta.getPersistentDataContainer().set(enchantKey, PersistentDataType.STRING, ench.getKey().toString());
                item.setItemMeta(meta);
                gui.addItem(item);
            }
        }

        if (page > 0) gui.setItem(45, createGuiItem(Material.ARROW, "§a← 前へ", "prev"));
        if (end < allEnchants.size()) gui.setItem(53, createGuiItem(Material.ARROW, "§a次へ →", "next"));
        player.openInventory(gui);
    }

    public void openLevelSelector(Player player, Enchantment enchant) {
        Inventory gui = Bukkit.createInventory(null, 9, "レベルを選択してください");
        pendingEnchant.put(player.getUniqueId(), enchant);

        for (int i = 1; i <= 5; i++) {
            ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE, i);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + "レベル " + i);
            meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, i);
            item.setItemMeta(meta);
            gui.setItem(i + 1, item);
        }
        player.openInventory(gui);
    }

    private ItemStack createGuiItem(Material material, String name, String action) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.contains("ターゲットを選択") && !title.contains("レベルを選択")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();

        if (title.contains("ターゲットを選択")) {
            String action = meta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
            if ("next".equals(action)) { openEnchantSelector(player, playerPage.get(player.getUniqueId()) + 1); return; }
            if ("prev".equals(action)) { openEnchantSelector(player, playerPage.get(player.getUniqueId()) - 1); return; }

            String keyString = meta.getPersistentDataContainer().get(enchantKey, PersistentDataType.STRING);
            if (keyString != null) {
                Enchantment target = Registry.ENCHANTMENT.get(NamespacedKey.fromString(keyString));
                openLevelSelector(player, target);
            }
        }
        else if (title.contains("レベルを選択")) {
            if (!meta.getPersistentDataContainer().has(levelKey, PersistentDataType.INTEGER)) return;

            if (!player.getInventory().contains(Material.EMERALD, 1)) {
                player.sendMessage(ChatColor.RED + "エメラルドが足りません！");
                player.closeInventory();
                return;
            }

            int level = meta.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER);
            Enchantment enchant = pendingEnchant.remove(player.getUniqueId());

            if (enchant != null) {
                player.getInventory().removeItem(new ItemStack(Material.EMERALD, 1));
                playerTarget.put(player.getUniqueId(), new TargetInfo(enchant, level));
                player.sendMessage(ChatColor.GOLD + "ターゲットを指定しました: " + ChatColor.AQUA + enchant.getKey().getKey() + " Lv" + level);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                player.closeInventory();
            }
        }
    }
}