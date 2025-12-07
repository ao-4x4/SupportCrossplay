package jp.reitou_mugicha.supportCrossplay.entity;

import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Optional;

public class EntitySnowKiller extends SnowGolem
{
    public EntitySnowKiller(Location location)
    {
        super(EntityType.SNOW_GOLEM, ((CraftWorld) location.getWorld()).getHandle());

        ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
        this.getBukkitEntity().setMetadata("SnowKiller", new FixedMetadataValue(SupportCrossplay.Instance, Optional.of(true)));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 10D, 2, 300.0F));

        this.setPosRaw(location.getX(), location.getY(), location.getZ());

        level.addFreshEntity(this);
    }

    private int shootCooldown = 0;
    @Override
    public void tick()
    {
        super.tick();

        if (shootCooldown > 0) {
            shootCooldown--;
        }

        if (this.getTarget() instanceof Player player) {
            double distance = this.distanceTo(player);
            if (distance <= 16.0D && this.hasLineOfSight(player) && shootCooldown <= 0) {
                this.performRangedAttack(player, (float)distance);
                shootCooldown = 2;
            }
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor)
    {
        ItemStack snowballItem = new ItemStack(Items.SNOWBALL);
        EntityDamageSnowball damageSnowball = new EntityDamageSnowball(this.level(), this, snowballItem);

        double deltaX = target.getX() - this.getX();
        double deltaY = target.getEyeY() - this.getEyeY();
        double deltaZ = target.getZ() - this.getZ();
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        double velocity = 1.8F;
        double gravity = 0.03F;
        double angle = Math.atan((velocity * velocity - Math.sqrt(velocity * velocity * velocity * velocity - gravity * (gravity * distance * distance + 2 * deltaY * velocity * velocity))) / (gravity * distance));

        if (Double.isNaN(angle)) {
            angle = Math.atan2(deltaY, distance);
        }

        double motionY = Math.sin(angle) * velocity;
        double horizontalVelocity = Math.cos(angle) * velocity;
        double motionX = (deltaX / distance) * horizontalVelocity;
        double motionZ = (deltaZ / distance) * horizontalVelocity;

        damageSnowball.setPos(this.getX(), this.getEyeY(), this.getZ());
        damageSnowball.shoot(motionX, motionY, motionZ, (float)velocity, 0.0F);

        this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(damageSnowball);
    }
}
