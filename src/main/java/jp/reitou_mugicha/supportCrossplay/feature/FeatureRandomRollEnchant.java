package jp.reitou_mugicha.supportCrossplay.feature;

import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import jp.reitou_mugicha.supportCrossplay.item.ItemEnchantShard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class FeatureRandomRollEnchant implements Listener
{
    private final String ROLL_MENU_TITLE = "エンチャントガチャ";
    private final int ROLL_COST = 3;

    private final int ROLL_BUTTON_INDEX = 13;

    private final List<Enchantment> ENCHANT_POOL = new ArrayList<>();

    public FeatureRandomRollEnchant()
    {
        initEnchantPool();
    }

    private void initEnchantPool()
    {
        Enchantment[] all = Enchantment.values();
        for (Enchantment enchantment : all) {
            if (enchantment == null) continue;

            int max = enchantment.getMaxLevel();
            if (max <= 0) continue;
            ENCHANT_POOL.add(enchantment);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        if (event.getClickedBlock().getType() == Material.ENCHANTING_TABLE)
        {
            Player player = event.getPlayer();
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (ItemEnchantShard.is(mainHand))
            {
                if (ENCHANT_POOL.isEmpty())
                {
                    player.sendMessage(ChatColor.RED + "登録済みエンチャントが存在しません！");
                    player.closeInventory();
                    return;
                }
                openRollMenu(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getClickedInventory() == null) return;
        if (!event.getView().getTitle().equals(ROLL_MENU_TITLE)) return;
        event.setCancelled(true);
        if (event.getRawSlot() != ROLL_BUTTON_INDEX) return;

        Player player = (Player)event.getWhoClicked();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (ItemEnchantShard.is(mainHand) && mainHand.getAmount() >= ROLL_COST)
        {
            Roll(player);
        }
        else
        {
            player.sendMessage(ChatColor.RED + "抽選を回すには最低でも" + ROLL_COST + "個のエンチャントの欠片が必要です！");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        }
    }

    private void Roll(Player player)
    {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        mainHand.setAmount(mainHand.getAmount() - ROLL_COST);

        ItemStack book = rollRandomEnchantBook();
        player.getInventory().addItem(book);

        player.sendMessage(ChatColor.GREEN + "エンチャントを獲得しました！\n結果: " + book.getItemMeta().getDisplayName());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        player.closeInventory();
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

        // Roll Button
        ItemStack rollButton = ItemStack.of(Material.LIME_STAINED_GLASS);
        ItemMeta rollMeta = rollButton.getItemMeta();
        rollMeta.setDisplayName(ChatColor.RED + "抽選する");
        rollMeta.setLore(List.of(ChatColor.YELLOW + "エンチャントの欠片 " + ROLL_COST + " 個で" + ChatColor.YELLOW + "ランダムなエンチャント本が手に入ります。"));
        rollMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        rollMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        rollButton.setItemMeta(rollMeta);
        gui.setItem(ROLL_BUTTON_INDEX, rollButton);

        // Blank
        ItemStack blank = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta blankMeta = blank.getItemMeta();
        blankMeta.setDisplayName(" ");
        blank.setItemMeta(blankMeta);

        for (int i = 0; i < 9 * 3; i++)
        {
            ItemStack item = gui.getItem(i);
            if (item == null || item.getType() == Material.AIR)
            {
                gui.setItem(i, blank);
            }
        }

        player.openInventory(gui);
    }
}