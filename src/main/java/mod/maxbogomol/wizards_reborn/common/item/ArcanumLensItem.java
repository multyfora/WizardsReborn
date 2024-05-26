package mod.maxbogomol.wizards_reborn.common.item;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.maxbogomol.wizards_reborn.WizardsReborn;
import mod.maxbogomol.wizards_reborn.WizardsRebornClient;
import mod.maxbogomol.wizards_reborn.api.wissen.IWissenItem;
import mod.maxbogomol.wizards_reborn.api.wissen.WissenItemUtils;
import mod.maxbogomol.wizards_reborn.api.wissen.WissenUtils;
import mod.maxbogomol.wizards_reborn.client.event.ClientTickHandler;
import mod.maxbogomol.wizards_reborn.client.render.WorldRenderHandler;
import mod.maxbogomol.wizards_reborn.common.config.Config;
import mod.maxbogomol.wizards_reborn.common.network.ArcanumLensBurstEffectPacket;
import mod.maxbogomol.wizards_reborn.common.network.PacketHandler;
import mod.maxbogomol.wizards_reborn.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Random;

public class ArcanumLensItem extends ArcanumItem implements IGuiParticleItem {
    private static Random random = new Random();

    public ArcanumLensItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide()) {
            int wissen = random.nextInt(2000, 3000);

            List<ItemStack> items = WissenUtils.getWissenItems(player);
            List<ItemStack> itemsOff = WissenUtils.getWissenItemsOff(items);
            items.removeAll(itemsOff);

            for (ItemStack item : items) {
                if (item.getItem() instanceof IWissenItem wissenItem) {
                    WissenItemUtils.existWissen(item);
                    int itemWissenRemain = WissenItemUtils.getAddWissenRemain(item, wissen, wissenItem.getMaxWissen());
                    if (wissen - itemWissenRemain > 0) {
                        WissenItemUtils.addWissen(item, wissen - itemWissenRemain, wissenItem.getMaxWissen());
                        wissen = wissen - itemWissenRemain;
                    }
                }
            }

            if (!player.isCreative()) {
                stack.setCount(stack.getCount() - 1);
            }

            PacketHandler.sendToTracking(world, player.getOnPos(), new ArcanumLensBurstEffectPacket((float) player.getX(), (float) player.getY() + (player.getBbHeight() / 2), (float) player.getZ()));
            world.playSound(WizardsReborn.proxy.getPlayer(), player.blockPosition(), WizardsReborn.WISSEN_BURST_SOUND.get(), SoundSource.PLAYERS, 0.5f, (float) (1.3f + ((random.nextFloat() - 0.5D) / 2)));
            world.playSound(WizardsReborn.proxy.getPlayer(), player.blockPosition(), WizardsReborn.CRYSTAL_BREAK_SOUND.get(), SoundSource.PLAYERS, 1f, (float) (1.0f + ((random.nextFloat() - 0.5D) / 4)));
        }

        player.getCooldowns().addCooldown(this, 50);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void renderParticle(PoseStack pose, LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, int guiOffset) {
        float ticks = ClientTickHandler.ticksInGame + Minecraft.getInstance().getPartialTick() * 0.1f;
        float offset = (float) (0.75f + Math.abs(Math.sin(Math.toRadians(ticks * 0.7f)) * 0.25f));

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        MultiBufferSource.BufferSource buffersource = WorldRenderHandler.getDelayedRender();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(WizardsRebornClient::getGlowingShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        TextureAtlasSprite sparkle = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation(WizardsReborn.MOD_ID, "particle/sparkle"));

        pose.pushPose();
        pose.translate(x + 8, y + 8, 100);
        pose.mulPose(Axis.ZP.rotationDegrees(ticks));
        RenderUtils.spriteGlowQuadCenter(pose, buffersource, 0, 0, 20f * offset, 20f * offset, sparkle.getU0(), sparkle.getU1(), sparkle.getV0(), sparkle.getV1(), Config.wissenColorR(), Config.wissenColorG(), Config.wissenColorB(), 1F);
        buffersource.endBatch();
        pose.popPose();

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }
}
