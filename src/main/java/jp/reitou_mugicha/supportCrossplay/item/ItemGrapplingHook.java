package jp.reitou_mugicha.supportCrossplay.item;

import de.tr7zw.nbtapi.NBTItem;
import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ItemGrapplingHook implements Listener {
    private static final ItemStack Item;
    private static final NamespacedKey recipeKey = new NamespacedKey(SupportCrossplay.getInstance(), "grappling_hook");
    private static final String ITEM_TAG = "grappling_hook";
    private final Set<UUID> flyingPlayers = new HashSet<>();

    private final int MAX_USES = 50;

    static {
        ItemStack item = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bグラップリング");
        meta.setLore(List.of("§7釣り針が接地した状態で再度クリックすると高速移動します。"));
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString(SupportCrossplay.KEY, ITEM_TAG);
        item = nbtItem.getItem();

        Item = item.clone();

        SupportCrossplay.addItem(item);
        registerRecipe();
    }

    public static ItemStack getItem() {
        return Item.clone();
    }

    public static void registerRecipe() {
        if (Bukkit.getRecipe(recipeKey) != null) return;

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, Item);
        recipe.shape("  F", " SB", "S  ");
        recipe.setIngredient('F', Material.FISHING_ROD);
        recipe.setIngredient('S', Material.STICK);
        recipe.setIngredient('B', Material.SLIME_BALL);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;

        NBTItem nbt = new NBTItem(item);
        if (!nbt.hasKey(SupportCrossplay.KEY) || !nbt.getString(SupportCrossplay.KEY).equals(ITEM_TAG)) return;

        FishHook hook = event.getHook();
        PlayerFishEvent.State state = event.getState();

        if (state == PlayerFishEvent.State.FISHING) {
            hook.setVelocity(hook.getVelocity().multiply(1.7));
        } else if (state == PlayerFishEvent.State.REEL_IN || state == PlayerFishEvent.State.IN_GROUND || state == PlayerFishEvent.State.CAUGHT_ENTITY) {
            if (hook.isOnGround() || isTouchingWall(hook)) {
                pullPlayer(player, hook, item);
            }
        } else if (state == PlayerFishEvent.State.CAUGHT_FISH || state == PlayerFishEvent.State.BITE) {
            event.setCancelled(true);
            hook.remove();
        }
    }

    private void pullPlayer(Player player, FishHook hook, ItemStack item) {
        Location pLoc = player.getEyeLocation();
        Location hLoc = hook.getLocation();
        Vector velocity = hLoc.toVector().subtract(pLoc.toVector());

        flyingPlayers.add(player.getUniqueId());

        double multiplier = 1.7;
        velocity.normalize().multiply(multiplier);
        velocity.setY(velocity.getY() * 0.6 + 0.8);

        player.setVelocity(velocity);
        player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.2f, 0.5f);
        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 0.8f, 1.5f);

        player.getWorld().spawnParticle(Particle.CRIT, hLoc, 20, 0.3, 0.3, 0.3, 0.5);

        hook.remove();
        damageItem(player, item);

        new BukkitRunnable() {
            @Override
            public void run() {
                flyingPlayers.remove(player.getUniqueId());
            }
        }.runTaskLater(SupportCrossplay.getInstance(), 120L);
    }

    private void damageItem(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable) {
            int unbreakingLevel = item.getEnchantmentLevel(Enchantment.UNBREAKING);
            if (SupportCrossplay.random.nextInt(unbreakingLevel + 1) > 0) {
                return;
            }

            int maxDurability = Material.FISHING_ROD.getMaxDurability();
            int damagePerUse = maxDurability / MAX_USES;
            int newDamage = damageable.getDamage() + damagePerUse;

            if (newDamage >= maxDurability) {
                item.setAmount(0);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            } else {
                damageable.setDamage(newDamage);
                item.setItemMeta(damageable);
            }
        }
    }

    private boolean isTouchingWall(FishHook hook) {
        Location loc = hook.getLocation();
        double r = 0.5;
        for (double x = -r; x <= r; x += r) {
            for (double y = -r; y <= r; y += r) {
                for (double z = -r; z <= r; z += r) {
                    if (loc.clone().add(x, y, z).getBlock().getType().isSolid()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (flyingPlayers.contains(player.getUniqueId())) {
                event.setCancelled(true);
                flyingPlayers.remove(player.getUniqueId());
            }
        }
    }
}