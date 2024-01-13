package mod.maxbogomol.wizards_reborn.common.network.spell;

import mod.maxbogomol.wizards_reborn.WizardsReborn;
import mod.maxbogomol.wizards_reborn.client.particle.Particles;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.Random;
import java.util.function.Supplier;

public class MagicSproutSpellEffectPacket {
    private static float X, Y, Z;
    private static float colorR, colorG, colorB;

    private static Random random = new Random();

    public MagicSproutSpellEffectPacket(float X, float Y, float Z, float colorR, float colorG, float colorB) {
        this.X = X;
        this.Y = Y;
        this.Z = Z;

        this.colorR = colorR;
        this.colorG = colorG;
        this.colorB = colorB;
    }

    public static MagicSproutSpellEffectPacket decode(FriendlyByteBuf buf) {
        return new MagicSproutSpellEffectPacket(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(X);
        buf.writeFloat(Y);
        buf.writeFloat(Z);

        buf.writeFloat(colorR);
        buf.writeFloat(colorG);
        buf.writeFloat(colorB);
    }

    public static void handle(MagicSproutSpellEffectPacket msg, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(new Runnable() {
                @Override
                public void run() {
                    Level world = WizardsReborn.proxy.getWorld();

                    for (int i = 0; i < 10; i++) {
                        if (random.nextFloat() < 0.6f) {
                            Particles.create(WizardsReborn.SPARKLE_PARTICLE)
                                    .addVelocity(((random.nextDouble() - 0.5D) / 50), ((random.nextDouble() - 0.5D) / 50), ((random.nextDouble() - 0.5D) / 50))
                                    .setAlpha(0.3f, 0).setScale(0.1f, 0.3f)
                                    .setColor(colorR, colorG, colorB)
                                    .setLifetime(40)
                                    .spawn(world, X + ((random.nextDouble() - 0.5D) / 2), Y + ((random.nextDouble() - 0.5D) / 2), Z + ((random.nextDouble() - 0.5D) / 2));
                        }
                        if (random.nextFloat() < 0.3f) {
                            Particles.create(WizardsReborn.STEAM_PARTICLE)
                                    .addVelocity(((random.nextDouble() - 0.5D) / 50), ((random.nextDouble() - 0.5D) / 50), ((random.nextDouble() - 0.5D) / 50))
                                    .setAlpha(0.15f, 0).setScale(0.1f, 1.5f)
                                    .setColor(colorR, colorG, colorB)
                                    .setLifetime(80)
                                    .setSpin((0.1f * (float) ((random.nextDouble() - 0.5D) * 2)))
                                    .spawn(world, X + ((random.nextDouble() - 0.5D) / 2), Y + ((random.nextDouble() - 0.5D) / 2), Z + ((random.nextDouble() - 0.5D) / 2));
                        }
                    }

                    ctx.get().setPacketHandled(true);
                }
            });
        }
    }
}