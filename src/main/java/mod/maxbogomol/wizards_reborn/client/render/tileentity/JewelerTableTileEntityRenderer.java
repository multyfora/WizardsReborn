package mod.maxbogomol.wizards_reborn.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.maxbogomol.wizards_reborn.WizardsRebornClient;
import mod.maxbogomol.wizards_reborn.client.event.ClientTickHandler;
import mod.maxbogomol.wizards_reborn.common.tileentity.JewelerTableTileEntity;
import mod.maxbogomol.wizards_reborn.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

public class JewelerTableTileEntityRenderer implements BlockEntityRenderer<JewelerTableTileEntity> {

    public JewelerTableTileEntityRenderer() {}

    @Override
    public void render(JewelerTableTileEntity table, float partialTicks, PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();

        Vec3 pos = table.getBlockRotatePos();

        ms.pushPose();
        ms.translate(pos.x(), pos.y(), pos.z());
        ms.mulPose(Axis.YP.rotationDegrees(table.getBlockRotate()));
        ms.mulPose(Axis.XP.rotationDegrees((ClientTickHandler.ticksInGame + partialTicks) * 2));
        RenderUtils.renderCustomModel(WizardsRebornClient.JEWELER_TABLE_STONE_MODEl, ItemDisplayContext.FIXED, false, ms, buffers, light, overlay);
        ms.popPose();

        ms.pushPose();
        ms.translate(0.5F, 0.703125F, 0.5F);
        ms.mulPose(Axis.YP.rotationDegrees(table.getBlockRotate()));
        ms.mulPose(Axis.XP.rotationDegrees(90F));
        ms.mulPose(Axis.ZP.rotationDegrees(-3F));
        ms.translate(0, -0.0725, 0);
        ms.scale(0.5F,0.5F,0.5F);
        mc.getItemRenderer().renderStatic(table.itemHandler.getStackInSlot(0), ItemDisplayContext.FIXED, light, overlay, ms, buffers, table.getLevel(), 0);
        ms.popPose();

        ms.pushPose();
        ms.translate(0.5F, 0.703125F + 0.03125F, 0.5F);
        ms.mulPose(Axis.YP.rotationDegrees(table.getBlockRotate()));
        ms.mulPose(Axis.XP.rotationDegrees(90F + 5F));
        ms.mulPose(Axis.ZP.rotationDegrees(15F));
        ms.translate(0.125F, -0.0625F, 0);
        ms.scale(0.5F,0.5F,0.5F);
        mc.getItemRenderer().renderStatic(table.itemHandler.getStackInSlot(1), ItemDisplayContext.FIXED, light, overlay, ms, buffers, table.getLevel(), 0);
        ms.popPose();

        ms.pushPose();
        ms.translate(0.5F, 0.703125F + 0.03125F, 0.5F);
        ms.mulPose(Axis.YP.rotationDegrees(table.getBlockRotate()));
        ms.mulPose(Axis.XP.rotationDegrees(90F + 7F));
        ms.mulPose(Axis.ZP.rotationDegrees(-15F));
        ms.translate(-0.125F, -0.0625F, 0);
        ms.scale(0.5F,0.5F,0.5F);
        mc.getItemRenderer().renderStatic(table.itemOutputHandler.getStackInSlot(0), ItemDisplayContext.FIXED, light, overlay, ms, buffers, table.getLevel(), 0);
        ms.popPose();
    }
}
