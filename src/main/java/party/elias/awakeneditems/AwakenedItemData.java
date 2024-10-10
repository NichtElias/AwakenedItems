package party.elias.awakeneditems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

public record AwakenedItemData(int level, int xp, UUID owner) {

    public AwakenedItemData(UUID owner) {
        this(0, 0, owner);
    }

    public static final Codec<AwakenedItemData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("level").forGetter(AwakenedItemData::level),
                    Codec.INT.fieldOf("xp").forGetter(AwakenedItemData::xp),
                    UUIDUtil.CODEC.fieldOf("owner").forGetter(AwakenedItemData::owner)
            ).apply(instance, AwakenedItemData::new)
    );

    public static final StreamCodec<ByteBuf, AwakenedItemData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, AwakenedItemData::level,
            ByteBufCodecs.INT, AwakenedItemData::xp,
            UUIDUtil.STREAM_CODEC, AwakenedItemData::owner,
            AwakenedItemData::new
    );

    public static final AwakenedItemData DEFAULT = new AwakenedItemData(0, 0, UUID.fromString("0-0-0-0-0"));

    private int getRequiredXp() {
        return 8 << level;
    }

    public AwakenedItemData addXp(int amount) {
        return new AwakenedItemData(level, xp + amount, owner);
    }

    public AwakenedItemData withLevel(int level) {
        return new AwakenedItemData(level, xp, owner);
    }

    public AwakenedItemData withXp(int xp) {
        return new AwakenedItemData(level, xp, owner);
    }

    public AwakenedItemData checkLevelUp(Level world, ItemStack stack) {
        int xp = this.xp;
        int level = this.level;

        while (xp >= getRequiredXp()) {
            xp -= getRequiredXp();
            level++;

            onItemLevelUp(stack, withLevel(level).withXp(xp), world);
        }

        return new AwakenedItemData(level, xp, owner);
    }

    public static void onItemLevelUp(ItemStack stack, AwakenedItemData aiData, Level world) {
        if (!world.isClientSide()) {
            Player owner = world.getPlayerByUUID(aiData.owner);
            if (owner != null) {
                stack.set(AwakenedItems.AWAKENED_ITEM_COMPONENT, aiData);
                owner.sendSystemMessage(Utils.formattedItemChatMessage(stack, Component.translatable("chat.awakeneditems.aimsg.levelup", aiData.level)));
            }
        }
    }
}
