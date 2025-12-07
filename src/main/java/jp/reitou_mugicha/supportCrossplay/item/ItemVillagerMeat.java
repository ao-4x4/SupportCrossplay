package jp.reitou_mugicha.supportCrossplay.item;

import de.tr7zw.nbtapi.NBTItem;
import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class ItemVillagerMeat implements Listener
{
    private static final ItemStack Item;
    private static final String ITEM_TAG = "villager_meat";

    static {
        ItemStack item = new ItemStack(Material.BRICK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("村人の肉");
        meta.setLore(List.of("村人から得られた肉。食べれそうではある。"));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString(SupportCrossplay.KEY, ITEM_TAG);
        item = nbtItem.getItem();

        Item = item.clone();

        SupportCrossplay.addItem(item);
    }

    public static ItemStack getItem()
    {
        return Item.clone();
    }
    public static boolean is(ItemStack item)
    {
        if (item == null) return false;
        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.hasKey(SupportCrossplay.KEY) && nbtItem.getString(SupportCrossplay.KEY).equals(ITEM_TAG);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!ItemVillagerMeat.is(mainHand)) return;
        mainHand.setAmount(mainHand.getAmount() - 1);

        player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 10));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 5));
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 15));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 2));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        if (event.getEntity() instanceof Villager villager)
        {
            World world = villager.getWorld();
            ItemStack result = getItem();
            result.setAmount(SupportCrossplay.random.nextInt(5));
            world.dropItemNaturally(villager.getLocation(), result);
        }
    }
}
