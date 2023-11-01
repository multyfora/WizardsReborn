package mod.maxbogomol.wizards_reborn.common.spell.ray;

import mod.maxbogomol.wizards_reborn.WizardsReborn;
import mod.maxbogomol.wizards_reborn.api.crystal.CrystalUtils;
import mod.maxbogomol.wizards_reborn.common.entity.SpellProjectileEntity;
import mod.maxbogomol.wizards_reborn.common.network.PacketHandler;
import mod.maxbogomol.wizards_reborn.common.network.spell.FireRaySpellEffectPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class FireRaySpell extends RaySpell {
    public FireRaySpell(String id, int points) {
        super(id, points);
        addCrystalType(WizardsReborn.FIRE_CRYSTAL_TYPE);
    }

    @Override
    public Color getColor() {
        return new Color(225, 127, 103);
    }

    @Override
    public void onImpact(HitResult ray, Level world, SpellProjectileEntity projectile, Player player, Entity target) {
        super.onImpact(ray, world, projectile, player, target);

        if (target.tickCount % 10 == 0) {
            ItemStack stack = player.getItemInHand(player.getUsedItemHand());
            removeWissen(stack, projectile.getStats());
            int focusLevel = CrystalUtils.getStatLevel(projectile.getStats(), WizardsReborn.FOCUS_CRYSTAL_STAT);
            target.hurt(new DamageSource(target.damageSources().onFire().typeHolder(), projectile, player), (float) (1.5f + (focusLevel * 0.5)));
            int fire = target.getRemainingFireTicks() + 5;
            if (fire > 10) {
                fire = 10;
            }
            target.setSecondsOnFire(fire);
        }
    }

    @Override
    public void onImpact(HitResult ray, Level world, SpellProjectileEntity projectile, Player player) {
        super.onImpact(ray, world, projectile, player);

        if (player != null) {
            if (player.isShiftKeyDown()) {
                int focusLevel = CrystalUtils.getStatLevel(projectile.getStats(), WizardsReborn.FOCUS_CRYSTAL_STAT);

                Color color = getColor();
                float r = color.getRed() / 255f;
                float g = color.getGreen() / 255f;
                float b = color.getBlue() / 255f;

                if (projectile.tickCount % (15 - (focusLevel * 3)) == 0) {
                    Vec3 vec = getBlockHitOffset(ray, projectile, -0.1f);
                    BlockPos blockPos = new BlockPos((int) vec.x(), (int) vec.y(), (int) vec.z());
                    BlockState blockState = world.getBlockState(blockPos);
                    if (!CampfireBlock.canLight(blockState) && !CandleBlock.canLight(blockState) && !CandleCakeBlock.canLight(blockState)) {
                        if (BaseFireBlock.canBePlacedAt(world, blockPos, Direction.UP)) {
                            world.playSound(null, blockPos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.1F, world.getRandom().nextFloat() * 0.4F + 0.8F);
                            BlockState blockstate1 = BaseFireBlock.getState(world, blockPos);
                            world.setBlock(blockPos, blockstate1, 11);

                            ItemStack stack = player.getItemInHand(player.getUsedItemHand());
                            removeWissen(stack, projectile.getStats());

                            PacketHandler.sendToTracking(world, player.getOnPos(), new FireRaySpellEffectPacket((float) blockPos.getX() + 0.5f, (float) blockPos.getY() + 0.2f, (float) blockPos.getZ() + 0.5f, r, g, b));
                        }
                    } else {
                        world.playSound(player, blockPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
                        world.setBlock(blockPos, blockState.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)), 11);
                        world.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);

                        ItemStack stack = player.getItemInHand(player.getUsedItemHand());
                        removeWissen(stack, projectile.getStats());

                        PacketHandler.sendToTracking(world, player.getOnPos(), new FireRaySpellEffectPacket((float) blockPos.getX() + 0.5f, (float) blockPos.getY() + 0.5f, (float) blockPos.getZ() + 0.5f, r, g, b));
                    }
                }
            }
        }
    }
}
