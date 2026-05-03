package jp.reitou_mugicha.supportCrossplay.feature;

import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class FeatureRandomRollEnchant implements Listener
{
    private final String ROLL_MENU_TITLE = "エンチャントガチャ";
    private final int ROLL_COST = 3;

    private final int ROLL_BUTTON_INDEX = 13;

    private final List<Enchantment> ENCHANT_POOL = new ArrayList<>();
    private final Set<UUID> rollingPlayers = new HashSet<>();

    public FeatureRandomRollEnchant()
    {
        initEnchantPool();
    }

    private void initEnchantPool()
    {
        Enchantment[] all = Enchantment.values();
        for (Enchantment enchantment : all)
        {
            if (enchantment == null) continue;
            int max = enchantment.getMaxLevel();
            if (max > 0) ENCHANT_POOL.add(enchantment);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;

        if (event.getClickedBlock().getType() == Material.ENCHANTING_TABLE)
        {
            Player player = event.getPlayer();

            if (ENCHANT_POOL.isEmpty())
            {
                player.sendMessage(ChatColor.RED + "登録済みエンチャントが存在しません！");
                return;
            }
            openRollMenu(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getClickedInventory() == null) return;
        if (!event.getView().getTitle().equals(ROLL_MENU_TITLE)) return;

        event.setCancelled(true);

        if (event.getRawSlot() != ROLL_BUTTON_INDEX) return;

        Player player = (Player) event.getWhoClicked();

        if (rollingPlayers.contains(player.getUniqueId()))
        {
            player.sendMessage(ChatColor.RED + "現在ルーレット中です！");
            return;
        }

        if (canRouletteStart(player))
        {
            rollingPlayers.add(player.getUniqueId());
            startRouletteAnimation(player);
        }
        else
        {
            player.sendMessage(ChatColor.RED + "抽選には " + ROLL_COST + "レベルが必要です！");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        }
    }

    private boolean canRouletteStart(Player player)
    {
        return player.getExpToLevel() >= ROLL_COST;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();

        if (rollingPlayers.contains(id))
        {
            finishRouletteOffline(player);
            rollingPlayers.remove(id);
        }
    }

    private void startRouletteAnimation(Player player)
    {
        player.closeInventory();

        player.setLevel(player.getLevel() - ROLL_COST);

        int totalTicks = 120;
        int intervalStart = 2;
        int intervalEnd = 12;

        final int[] ticks = {0};
        final int[] counter = {0};

        final BukkitTask[] holder = new BukkitTask[1];

        holder[0] = Bukkit.getScheduler().runTaskTimer(SupportCrossplay.getInstance(), () -> {

            BukkitTask task = holder[0];

            if (!player.isOnline())
            {
                task.cancel();
                finishRouletteOffline(player);
                rollingPlayers.remove(player.getUniqueId());
                return;
            }

            int interval = intervalStart + (intervalEnd - intervalStart) * ticks[0] / totalTicks;

            if (counter[0] % interval == 0)
            {
                Enchantment ench = ENCHANT_POOL.get(SupportCrossplay.random.nextInt(ENCHANT_POOL.size()));
                player.sendTitle(ChatColor.AQUA + "" + ChatColor.BOLD + ench.getKey().getKey().toUpperCase(),
                        ChatColor.WHITE + "ルーレット中...", 0, interval + 20, 0);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
            }

            ticks[0]++;
            counter[0]++;

            if (ticks[0] >= totalTicks)
            {
                task.cancel();
                finishRoulette(player);
                rollingPlayers.remove(player.getUniqueId());
            }

        }, 0L, 1L);
    }

    private void finishRoulette(Player player)
    {
        ItemStack book = rollRandomEnchantBook();
        player.getInventory().addItem(book);

        if (player.isOnline())
        {
            player.sendTitle(
                    ChatColor.GOLD + "" + ChatColor.BOLD + "結果！",
                    book.getItemMeta().getDisplayName(),
                    10, 40, 10
            );

            player.getWorld().spawnParticle(
                    Particle.EXPLOSION,
                    player.getLocation().add(0, 1, 0),
                    80, 0.5, 0.5, 0.5, 0.2
            );

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        }

        player.sendMessage(ChatColor.GREEN + "エンチャントを獲得しました！ → " + book.getItemMeta().getDisplayName());
    }

    private void finishRouletteOffline(Player player)
    {
        ItemStack book = rollRandomEnchantBook();
        player.getInventory().addItem(book);

        player.sendMessage(ChatColor.GREEN + "ログアウト時にエンチャント結果を付与しました → " + book.getItemMeta().getDisplayName());
    }

    private ItemStack rollRandomEnchantBook()
    {
        Enchantment enchantment = ENCHANT_POOL.get(SupportCrossplay.random.nextInt(ENCHANT_POOL.size()));
        int level = getRandomLevel(enchantment);

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();

        meta.addStoredEnchant(enchantment, level, true);
        meta.setDisplayName("§b§l" + enchantment.getKey().getKey().toUpperCase() + " §fLv." + level);

        book.setItemMeta(meta);
        return book;
    }

    private int getRandomLevel(Enchantment enchantment)
    {
        int max = enchantment.getMaxLevel();
        return 1 + SupportCrossplay.random.nextInt(max);
    }

    private void openRollMenu(Player player)
    {
        Inventory gui = Bukkit.createInventory(null, 9 * 3, ROLL_MENU_TITLE);

        ItemStack rollButton = new ItemStack(Material.LIME_STAINED_GLASS);
        ItemMeta rollMeta = rollButton.getItemMeta();
        rollMeta.setDisplayName(ChatColor.RED + "抽選する");
        rollMeta.setLore(List.of(ChatColor.YELLOW + "エンチャントの欠片 " + ROLL_COST + " 個でランダムな本が手に入ります。"));
        rollMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        rollMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        rollButton.setItemMeta(rollMeta);
        gui.setItem(ROLL_BUTTON_INDEX, rollButton);

        ItemStack blank = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta blankMeta = blank.getItemMeta();
        blankMeta.setDisplayName(" ");
        blank.setItemMeta(blankMeta);

        for (int i = 0; i < 9 * 3; i++)
        {
            if (gui.getItem(i) == null || gui.getItem(i).getType() == Material.AIR)
            {
                gui.setItem(i, blank);
            }
        }

        player.openInventory(gui);
    }
}