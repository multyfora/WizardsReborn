package mod.maxbogomol.wizards_reborn.common.network.spell;

import mod.maxbogomol.wizards_reborn.WizardsReborn;
import mod.maxbogomol.wizards_reborn.client.particle.Particles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.Random;
import java.util.function.Supplier;

public class FrostAuraSpellBurstEffectPacket {
    private final float X, Y, Z;
    private final float colorR, colorG, colorB;

    private static final Random random = new Random();

    public FrostAuraSpellBurstEffectPacket(float X, float Y, float Z, float colorR, float colorG, float colorB) {
        this.X = X;
        this.Y = Y;
        this.Z = Z;

        this.colorR = colorR;
        this.colorG = colorG;
        this.colorB = colorB;
    }

    public static FrostAuraSpellBurstEffectPacket decode(FriendlyByteBuf buf) {
        return new FrostAuraSpellBurstEffectPacket(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(X);
        buf.writeFloat(Y);
        buf.writeFloat(Z);

        buf.writeFloat(colorR);
        buf.writeFloat(colorG);
        buf.writeFloat(colorB);
    }

    public static void handle(FrostAuraSpellBurstEffectPacket msg, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(new Runnable() {
                @Override
                public void run() {
                    Level world = WizardsReborn.proxy.getWorld();

                    for (int i = 0; i < 10; i++) {
                        if (random.nextFloat() < 0.3f) {
                            Particles.create(WizardsReborn.WISP_PARTICLE)
                                    .addVelocity(((random.nextDouble() - 0.5D) / 8), ((random.nextDouble() - 0.5D) / 8) + 0.25D, ((random.nextDouble() - 0.5D) / 8))
                                    .setAlpha(0.4f, 0).setScale(0.2f, 0)
                                    .setColor(msg.colorR, msg.colorG, msg.colorB)
                                    .setLifetime(40)
                                    .enableGravity()
                                    .setSpin(2f * (random.nextFloat() - 0.5f))
                                    .spawn(world, msg.X, msg.Y, msg.Z);
                        }

                        if (random.nextFloat() < 0.5f) {
                            world.addParticle(ParticleTypes.SNOWFLAKE,
                                    msg.X + ((random.nextDouble() - 0.5D) / 2), msg.Y + ((random.nextDouble() - 0.5D) / 2), msg.Z + ((random.nextDouble() - 0.5D) / 2),
                                    ((random.nextDouble() - 0.5D) / 10), ((random.nextDouble() - 0.5D) / 10) + 0.05f, ((random.nextDouble() - 0.5D) / 10));
                        }
                    }

                    ctx.get().setPacketHandled(true);
                }
            });
        }
    }
}