package mod.maxbogomol.wizards_reborn.common.knowledge;

import mod.maxbogomol.wizards_reborn.api.knowledge.Knowledge;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class ItemTagKnowledge extends Knowledge {
    @Deprecated
    public final TagKey<Item> tag;

    public ItemStack item;

    public ItemTagKnowledge(String id, boolean articles, int points, TagKey<Item> tag, ItemStack item) {
        super(id, articles, points);
        this.tag = tag;
        this.item = item;
    }

    @Override
    public boolean canReceived(Player player) {
        List<ItemStack> items = player.inventoryMenu.getItems();
        for (ItemStack stack : items) {
            if (stack.is(tag)) {
                return true;
            }
        }
        return false;
    }

    public TagKey<Item> getTag() {
        return tag;
    }

    @OnlyIn(Dist.CLIENT)
    public ItemStack getIcon() {
        return item;
    }
}
