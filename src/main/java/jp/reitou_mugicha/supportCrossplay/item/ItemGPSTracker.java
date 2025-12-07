package jp.reitou_mugicha.supportCrossplay.item;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import de.tr7zw.nbtapi.NBTItem;
import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import jp.reitou_mugicha.supportCrossplay.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemGPSTracker implements Listener
{
    private static final ItemStack Item;
    private static final NamespacedKey recipeKey = new NamespacedKey(SupportCrossplay.getInstance(), "gps_tracker");
    private static final String ITEM_TAG = "gps_tracker";
    private static final int GPS_TRACKER_MAX_LEVEL = 4;
    private static final String UPGRADE_MENU_TITLE = "GPSトラッカーのアップグレード";

    static {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("GPSトラッカー");
        meta.setLore(Collections.singletonList("レベル: 1"));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString(SupportCrossplay.KEY, ITEM_TAG);
        nbtItem.setInteger("Level", 1);
        nbtItem.setInteger("AvailableTime", 600000);
        item = nbtItem.getItem();

        Item = item.clone();

        SupportCrossplay.addItem(item);
        registerRecipe();
    }

    public static ItemStack getItem()
    {
        return Item.clone();
    }

    public static void registerRecipe()
    {
        if (Bukkit.getRecipe(recipeKey) != null) return;

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, Item);
        recipe.shape("CCC", "CCC", "CCC");
        recipe.setIngredient('C', Material.COMPASS);

        Bukkit.addRecipe(recipe);
    }

    private ItemStack createUpgradeButton(int level)
    {
        ItemStack item = new ItemStack(Material.DIAMOND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("アップグレード");
        meta.setLore(Collections.singletonList(level < GPS_TRACKER_MAX_LEVEL ? "次のレベル: " + level : "MAX"));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (mainHandItem == null) return;
        if (!isGPSTracker(mainHandItem)) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            showUpgradeMenu(player, mainHandItem);
        }
    }

    @EventHandler
    public void onPlayerEntityInteract(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (!isGPSTracker(mainHandItem)) return;
        if (!(event.getRightClicked() instanceof Player target)) return;
        if (!player.isSneaking()) return;

        NBTItem nbt = new NBTItem(mainHandItem);
        String playerUUID = player.getUniqueId().toString();

        long availableTimeMs = nbt.getInteger("AvailableTime");
        long availableTimeMin = availableTimeMs / 1000 / 60;

        PlayerData.config.set(playerUUID + ".gpsTarget", target.getUniqueId().toString());
        PlayerData.config.set(playerUUID + ".gpsExpireTime", System.currentTimeMillis() + availableTimeMs);
        PlayerData.config.set(playerUUID + ".gpsLevel", nbt.getInteger("Level"));
        PlayerData.saveConfig();

        mainHandItem.setAmount(0);
        player.sendMessage("GPSを" + target.getName() + "に設置しました！（" + availableTimeMin + "分間有効）");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        Player player = (Player) event.getWhoClicked();
        if (!event.getView().getTitle().equals(UPGRADE_MENU_TITLE)) return;
        event.setCancelled(true);
        if (event.getRawSlot() != 13) return;

        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (mainHandItem == null || mainHandItem.getType() == Material.AIR) return;
        if (!isGPSTracker(mainHandItem)) return;

        NBTItem nbt = new NBTItem(mainHandItem);
        if (nbt.getInteger("Level") >= GPS_TRACKER_MAX_LEVEL) {
            player.sendMessage("最大レベルです！");
            player.closeInventory();
            return;
        }

        if (player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), 5)) {
            player.getInventory().removeItem(new ItemStack(Material.DIAMOND, 5));
            nbt.setInteger("Level", nbt.getInteger("Level") + 1);
            mainHandItem = nbt.getItem();

            ItemMeta meta = mainHandItem.getItemMeta();
            meta.setLore(Collections.singletonList(nbt.getInteger("Level") >= 4 ? "レベル: " + nbt.getInteger("Level") : "MAX"));
            mainHandItem.setItemMeta(meta);

            player.getInventory().setItemInMainHand(mainHandItem);
            showUpgradeMenu(player, mainHandItem);
        }
    }

    @EventHandler
    public void onServerTick(ServerTickEndEvent event)
    {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String playerUUID = player.getUniqueId().toString();
            String targetUUID = PlayerData.config.getString(playerUUID + ".gpsTarget");
            Long expireTime = PlayerData.config.getLong(playerUUID + ".gpsExpireTime");

            if (targetUUID == null) continue;

            if (expireTime < System.currentTimeMillis()) {
                PlayerData.config.set(playerUUID + ".gpsTarget", null);
                PlayerData.config.set(playerUUID + ".gpsExpireTime", null);
                PlayerData.saveConfig();

                Player target = Bukkit.getPlayer(UUID.fromString(targetUUID));
                player.sendMessage((target != null ? target.getName() : "対象") + "に付けたGPSのバッテリーが切れました。");
                continue;
            }

            Player target = Bukkit.getPlayer(UUID.fromString(targetUUID));
            if (target == null || !target.isOnline()) continue;

            int level = PlayerData.config.getInt(playerUUID + ".gpsLevel");

            double x = target.getLocation().getX();
            double y = target.getLocation().getY();
            double z = target.getLocation().getZ();

            double offsetX = (new Random().nextDouble() * 2000) - 1000;
            double offsetZ = (new Random().nextDouble() * 2000) - 1000;

            String environment = target.getWorld().getEnvironment().name().toLowerCase();
            String dimension = environment.equals("normal") ? "Overworld" : environment.equals("nether") ? "Nether" : environment.equals("the_end") ? "The End" : "???";
            String xStr = level >= 2 ? String.format("%.1f", x + offsetX) : "???";
            String yStr = String.format("%.1f", y);
            String zStr = level >= 3 ? String.format("%.1f", z + offsetZ) : "???";

            String infos = "Target: " + target.getName() + " Dimension: " + dimension + " X: " + xStr + " Y: " + yStr + " Z: " + zStr;
            Component message = Component.text("GPS: ", NamedTextColor.YELLOW)
                    .append(Component.text(infos, NamedTextColor.GREEN));

            player.sendActionBar(message);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        UUID leftPlayerUUID = event.getPlayer().getUniqueId();
        for (Player player : Bukkit.getOnlinePlayers()) {
            String playerUUID = player.getUniqueId().toString();
            String gpsTarget = PlayerData.config.getString(playerUUID + ".gpsTarget");

            if (gpsTarget != null && gpsTarget.equals(leftPlayerUUID.toString())) {
                PlayerData.config.set(playerUUID + ".gpsTarget", null);
                PlayerData.config.set(playerUUID + ".gpsExpireTime", null);
                PlayerData.saveConfig();

                player.sendMessage("追跡対象の" + event.getPlayer().getName() + "がサーバーを退出したためGPSが途切れました。");
            }
        }
    }

    private void showUpgradeMenu(Player player, ItemStack gpsTracker)
    {
        if (gpsTracker == null) return;
        NBTItem nbt = new NBTItem(gpsTracker);
        Inventory gui = Bukkit.createInventory(null, 9 * 3, UPGRADE_MENU_TITLE);

        ItemStack upgradeButton = createUpgradeButton(nbt.getInteger("Level"));
        gui.setItem(13, upgradeButton);
        player.openInventory(gui);
    }

    private boolean isGPSTracker(ItemStack item)
    {
        if (item == null || item.getType() == Material.AIR || item.getAmount() == 0) return false;
        NBTItem nbt = new NBTItem(item);
        return nbt.hasKey(SupportCrossplay.KEY) && nbt.getString(SupportCrossplay.KEY).equals(ITEM_TAG);
    }
}