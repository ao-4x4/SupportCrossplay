package jp.reitou_mugicha.supportCrossplay.feature;

import jp.reitou_mugicha.supportCrossplay.Helpers;
import jp.reitou_mugicha.supportCrossplay.item.ItemEnchantShard;
import org.bukkit.*;
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

import java.util.Arrays;
import java.util.Objects;

public class FeatureAdvancedAnvil implements Listener
{
    private final String GUIName = "究極金床";
    private final int cost = 10;
    private final int slot1Index = 11;
    private final int slot2Index = 13;
    private final int craftIndex = 15;

    private final Enchantment[] allowedEnchantments = {
            Enchantment.UNBREAKING,
            Enchantment.POWER,
            Enchantment.SHARPNESS,
            Enchantment.FORTUNE,
            Enchantment.EFFICIENCY,
            Enchantment.LOOTING,
            Enchantment.PROTECTION,
            Enchantment.FIRE_PROTECTION,
            Enchantment.BLAST_PROTECTION,
            Enchantment.PROJECTILE_PROTECTION,
            Enchantment.THORNS
    };

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && Objects.requireNonNull(event.getClickedBlock()).getType() == Material.ANVIL && player.isSneaking())
        {
            openCustomGUI(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getView().getTitle().equals(GUIName))
        {
            Player player = (Player) event.getWhoClicked();
            Inventory inventory = event.getInventory();
            ItemStack slot1 = inventory.getItem(slot1Index);
            ItemStack slot2 = inventory.getItem(slot2Index);

            if (event.getRawSlot() == craftIndex)
            {
                event.setCancelled(true);
                if (slot1 != null && slot2 != null)
                {
                    if (slot1.getType() == Material.ENCHANTED_BOOK && slot2.getType() == Material.NETHERITE_INGOT)
                    {
                        ItemStack result = slot1.clone();
                        EnchantmentStorageMeta resultMeta = (EnchantmentStorageMeta) result.getItemMeta();
                        if (resultMeta != null)
                        {
                            Enchantment enchantment = getAllowedEnchantment(slot1);
                            int level = getAllowedEnchantmentLevel(slot1);
                            if (enchantment != null)
                            {
                                if (player.getLevel() < cost)
                                {
                                    player.sendMessage("経験値が足りません | Not enough experience");
                                    return;
                                }

                                if (level >= 10)
                                {
                                    player.sendMessage("このエンチャントは最大レベルです | This enchantment is at maximum level");
                                    return;
                                }

                                resultMeta.addStoredEnchant(enchantment, level + 1, true);
                                result.setItemMeta(resultMeta);
                                player.getInventory().addItem(result);

                                slot1.setAmount(slot1.getAmount() - 1);
                                slot2.setAmount(slot2.getAmount() - 1);

                                player.setLevel(player.getLevel() - cost);
                            }
                        }
                    }
                    else if ((Helpers.isTool(slot1) || Helpers.isArmor(slot1)) && slot2.getType() == Material.ENCHANTED_BOOK)
                    {
                        ItemStack result = slot1.clone();
                        ItemMeta resultMeta = result.getItemMeta();
                        if (resultMeta != null)
                        {
                            Enchantment enchantment = getAllowedEnchantment(slot2);
                            int level = getAllowedEnchantmentLevel(slot2);
                            if (enchantment != null)
                            {
                                if (player.getLevel() < cost)
                                {
                                    player.sendMessage("経験値が足りません | Not enough experience");
                                    return;
                                }

                                ItemMeta slot1Meta = slot1.getItemMeta();
                                if (slot1Meta.hasEnchant(enchantment) && slot1Meta.getEnchantLevel(enchantment) >= level)
                                {
                                    player.sendMessage("このアイテムにはこのエンチャントを適用できません | This item cannot be enchanted with this enchantment");
                                    return;
                                }

                                resultMeta.addEnchant(enchantment, level, true);
                                result.setItemMeta(resultMeta);
                                player.getInventory().addItem(result);

                                slot1.setAmount(slot1.getAmount() - 1);
                                slot2.setAmount(slot2.getAmount() - 1);

                                player.setLevel(player.getLevel() - cost);
                            }
                        }
                    }
                }

            }
            else
            {
                if (event.getRawSlot() != slot1Index && event.getRawSlot() != slot2Index && event.getRawSlot() < 27)
                {
                    event.setCancelled(true);
                }
            }
        }
    }

    public void openCustomGUI(Player player)
    {
        Inventory gui = Bukkit.createInventory(null, 27, GUIName);

        ItemStack blank = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta blankMeta = blank.getItemMeta();
        blankMeta.setDisplayName("無効 | Invalid");
        blank.setItemMeta(blankMeta);
        for (int i = 0; i < 27; i++)
        {
            gui.setItem(i, blank);
        }

        ItemStack craftItem = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta resultMeta = craftItem.getItemMeta();
        resultMeta.setDisplayName("クラフト | Craft");
        craftItem.setItemMeta(resultMeta);

        gui.setItem(slot1Index, null);
        gui.setItem(slot2Index, null);
        gui.setItem(craftIndex, craftItem);

        player.openInventory(gui);
    }

    public boolean hasAllowedEnchantment(ItemStack item)
    {
        EnchantmentStorageMeta storedMeta = (EnchantmentStorageMeta) item.getItemMeta();

        if (storedMeta != null)
        {
            for (Enchantment enchantment : storedMeta.getStoredEnchants().keySet())
            {
                if (Arrays.asList(allowedEnchantments).contains(enchantment))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public Enchantment getAllowedEnchantment(ItemStack item)
    {
        EnchantmentStorageMeta storedMeta = (EnchantmentStorageMeta) item.getItemMeta();

        if (storedMeta != null)
        {
            for (Enchantment enchantment : storedMeta.getStoredEnchants().keySet())
            {
                if (Arrays.asList(allowedEnchantments).contains(enchantment))
                {
                    return enchantment;
                }
            }
        }
        return null;
    }

    public int getAllowedEnchantmentLevel(ItemStack item)
    {
        EnchantmentStorageMeta storedMeta = (EnchantmentStorageMeta) item.getItemMeta();

        if (storedMeta != null)
        {
            for (Enchantment enchantment : storedMeta.getStoredEnchants().keySet())
            {
                if (Arrays.asList(allowedEnchantments).contains(enchantment))
                {
                    return storedMeta.getStoredEnchantLevel(enchantment);
                }
            }
        }
        return 0;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event)
    {
        if (event.getView().getTitle().equals(GUIName))
        {
            Player player = (Player) event.getPlayer();

            Inventory inventory = event.getInventory();

            ItemStack slot1 = inventory.getItem(slot1Index);
            ItemStack slot2 = inventory.getItem(slot2Index);

            if (slot1 != null)
            {
                player.getInventory().addItem(slot1);
            }

            if (slot2 != null)
            {
                player.getInventory().addItem(slot2);
            }
        }
    }
}