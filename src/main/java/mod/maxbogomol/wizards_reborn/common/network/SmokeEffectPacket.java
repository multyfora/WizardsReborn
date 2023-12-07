package mod.maxbogomol.wizards_reborn.common.network;

import mod.maxbogomol.wizards_reborn.WizardsReborn;
import mod.maxbogomol.wizards_reborn.client.particle.Particles;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.Random;
import java.util.function.Supplier;

public class SmokeEffectPacket {
    private static float posX;
    private static float posY;
    private static float posZ;

    private static float velX;
    private static float velY;
    private static float velZ;

    private static float colorR, colorG, colorB;

    private static Random random = new Random();

    public SmokeEffectPacket(float posX, float posY, float posZ, float velX, float velY, float velZ, float colorR, float colorG, float colorB) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;

        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;

        this.colorR = colorR;
        this.colorG = colorG;
        this.colorB = colorB;
    }

    public static SmokeEffectPacket decode(FriendlyByteBuf buf) {
        return new SmokeEffectPacket(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(posX);
        buf.writeFloat(posY);
        buf.writeFloat(posZ);

        buf.writeFloat(velX);
        buf.writeFloat(velY);
        buf.writeFloat(velZ);

        buf.writeFloat(colorR);
        buf.writeFloat(colorG);
        buf.writeFloat(colorB);
    }

    public static void handle(SmokeEffectPacket msg, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(new Runnable() {
                @Override
                public void run() {
                    Level world = WizardsReborn.proxy.getWorld();

                    for (int i = 0; i < 40; i++) {
                        Particles.create(WizardsReborn.STEAM_PARTICLE)
                                .addVelocity(velX + ((random.nextDouble() - 0.5D) / 20), velY + ((random.nextDouble() - 0.5D) / 20), velZ + ((random.nextDouble() - 0.5D) / 20))
                                .setAlpha(0.05f, 0).setScale(0.1f, 2)
                                .setColor(colorR, colorG, colorB)
                                .setLifetime(500 + random.nextInt(100))
                                .setSpin((0.1f * (float) ((random.nextDouble() - 0.5D) * 2)))
                                .spawn(world, posX + ((random.nextDouble() - 0.5D) / 3), posY + ((random.nextDouble() - 0.5D) / 3), posZ + ((random.nextDouble() - 0.5D) / 3));
                    }
                    ctx.get().setPacketHandled(true);
                }
            });
        }
    }
}
