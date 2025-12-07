package jp.reitou_mugicha.supportCrossplay.feature;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class FeatureExperienceTrading implements Listener
{
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            if (player.isSneaking() && player.getInventory().getItemInMainHand().getType() == Material.EXPERIENCE_BOTTLE)
            {
                openSelectPlayerMenu(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        if (event.getView().getTitle().equals("プレイヤーを選択してください。"))
        {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

            SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null) return;

            Player targetPlayer = meta.getOwningPlayer().getPlayer();
            if (targetPlayer == null) return;

            Player clicker = (Player) event.getWhoClicked();
            openDecideAmountGui(clicker, targetPlayer);

            event.setCancelled(true);
        }
        else if (event.getView().getTitle().equals("あげる量を決定してください。"))
        {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) return;

            Inventory gui = event.getClickedInventory();
            if (gui == null) return;

            ItemStack currentItem = gui.getItem(0);
            if (currentItem == null || currentItem.getType() != Material.EXPERIENCE_BOTTLE) return;

            ItemMeta currentMeta = currentItem.getItemMeta();
            if (currentMeta == null || !currentMeta.hasDisplayName()) return;

            String displayName = currentMeta.getDisplayName();
            int giveAmount = Integer.parseInt(displayName.replaceAll("[^0-9]", ""));

            switch (clickedItem.getType()) {
                case BARRIER:
                    giveAmount = 0;
                    event.setCancelled(true);
                    break;
                case LIME_CONCRETE_POWDER:
                    giveAmount += 1;
                    event.setCancelled(true);
                    break;
                case LIME_CONCRETE:
                    giveAmount += 10;
                    event.setCancelled(true);
                    break;
                case RED_CONCRETE_POWDER:
                    giveAmount -= 1;
                    event.setCancelled(true);
                    break;
                case RED_CONCRETE:
                    giveAmount -= 10;
                    event.setCancelled(true);
                    break;
                case GOLD_BLOCK:
                    Player sender = (Player) event.getWhoClicked();
                    giveAmount = sender.getLevel();

                    currentMeta.setLore(List.of("レベル"));
                    currentItem.setItemMeta(currentMeta);

                    event.setCancelled(true);
                    break;
                case EMERALD:
                    String currentUnit = currentMeta.getLore() != null ? currentMeta.getLore().get(0) : "ポイント";
                    String newUnit = currentUnit.equals("ポイント") ? "レベル" : "ポイント";
                    currentMeta.setLore(List.of(newUnit));
                    currentItem.setItemMeta(currentMeta);
                    event.setCancelled(true);
                    break;
                case PLAYER_HEAD:
                    SkullMeta skullMeta = (SkullMeta) clickedItem.getItemMeta();
                    if (skullMeta == null || skullMeta.getOwningPlayer() == null) return;

                    Player targetPlayer = skullMeta.getOwningPlayer().getPlayer();
                    if (targetPlayer == null) return;

                    sender = (Player) event.getWhoClicked();

                    String unit = currentMeta.getLore() != null && !currentMeta.getLore().isEmpty()
                            ? currentMeta.getLore().get(0) : "ポイント";

                    if (unit.equals("ポイント")) {
                        if (sender.getTotalExperience() < giveAmount) {
                            sender.sendMessage(ChatColor.RED + "経験値が足りません。");
                        } else {
                            sender.giveExp(-giveAmount);
                            targetPlayer.giveExp(giveAmount);
                            sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + "に " + giveAmount + " ポイント送信しました。");
                            targetPlayer.sendMessage(ChatColor.GREEN + sender.getName() + "から " + giveAmount + " ポイントを受け取りました。");
                        }
                    } else if (unit.equals("レベル")) {
                        if (sender.getLevel() < giveAmount) {
                            sender.sendMessage(ChatColor.RED + "レベルが足りません。");
                        } else {
                            sender.setLevel(sender.getLevel() - giveAmount);
                            targetPlayer.setLevel(targetPlayer.getLevel() + giveAmount);
                            sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + "に " + giveAmount + " レベル送信しました。");
                            targetPlayer.sendMessage(ChatColor.GREEN + sender.getName() + "から " + giveAmount + " レベルを受け取りました。");
                        }
                    }

                    sender.closeInventory();
                    event.setCancelled(true);
                    break;
            }

            giveAmount = Math.max(giveAmount, 0);

            currentMeta.setDisplayName("あげる量: " + giveAmount);
            currentItem.setItemMeta(currentMeta);

            gui.setItem(0, currentItem);

            event.setCancelled(true);
        }
    }

    public void openSelectPlayerMenu(Player player)
    {
        int size = ((Bukkit.getOnlinePlayers().size() - 1) / 9 + 1) * 9;
        Inventory gui = Bukkit.createInventory(null, size, "プレイヤーを選択してください。");

        if (Bukkit.getOnlinePlayers().size() == 1)
        {
            player.sendMessage(ChatColor.RED + "経験値を送れる有効なプレイヤーがいません。");
            return;
        }

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(player)) continue;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target);
                meta.setDisplayName(target.getName());
                head.setItemMeta(meta);
            }

            gui.addItem(head);
        }

        player.openInventory(gui);
    }

    public void openDecideAmountGui(Player player, Player target)
    {
        Inventory gui = Bukkit.createInventory(null, 9, "あげる量を決定してください。");
        int giveAmount = 0;

        ItemStack current = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta currentMeta = current.getItemMeta();
        currentMeta.setDisplayName("あげる量: " + String.valueOf(giveAmount));
        current.setItemMeta(currentMeta);

        ItemStack reset = new ItemStack(Material.BARRIER);
        ItemMeta resetMeta = reset.getItemMeta();
        resetMeta.setDisplayName("リセット");
        reset.setItemMeta(resetMeta);

        ItemStack plusOne = new ItemStack(Material.LIME_CONCRETE_POWDER);
        ItemMeta plusOneMeta = plusOne.getItemMeta();
        plusOneMeta.setDisplayName(ChatColor.GREEN + "+1");
        plusOne.setItemMeta(plusOneMeta);

        ItemStack plusTen = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta plusTenMeta = plusTen.getItemMeta();
        plusTenMeta.setDisplayName(ChatColor.GREEN + "+10");
        plusTen.setItemMeta(plusTenMeta);

        ItemStack minusOne = new ItemStack(Material.RED_CONCRETE_POWDER);
        ItemMeta minusOneMeta = minusOne.getItemMeta();
        minusOneMeta.setDisplayName(ChatColor.RED + "-1");
        minusOne.setItemMeta(minusOneMeta);

        ItemStack minusTen = new ItemStack(Material.RED_CONCRETE);
        ItemMeta minusTenMeta = minusTen.getItemMeta();
        minusTenMeta.setDisplayName(ChatColor.RED + "-10");
        minusTen.setItemMeta(minusTenMeta);

        ItemStack all = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta allMeta = all.getItemMeta();
        allMeta.setDisplayName("持っている経験値すべて");
        all.setItemMeta(allMeta);

        ItemStack pointOrLevel = new ItemStack(Material.EMERALD);
        ItemMeta pointOrLevelMeta = pointOrLevel.getItemMeta();
        pointOrLevelMeta.setDisplayName("単位の変更");
        pointOrLevel.setItemMeta(pointOrLevelMeta);

        ItemStack send = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sendMeta = (SkullMeta) send.getItemMeta();
        if (sendMeta != null) {
            sendMeta.setOwningPlayer(target);
            sendMeta.setDisplayName(target.getName());
            send.setItemMeta(sendMeta);
        }
        sendMeta.setDisplayName("送る");
        send.setItemMeta(sendMeta);

        gui.setItem(0, current);
        gui.setItem(1, reset);
        gui.setItem(2, plusOne);
        gui.setItem(3, plusTen);
        gui.setItem(4, minusOne);
        gui.setItem(5, minusTen);
        gui.setItem(6, all);
        gui.setItem(7, pointOrLevel);
        gui.setItem(8, send);

        player.openInventory(gui);
    }
}