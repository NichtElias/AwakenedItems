package party.elias.awakeneditems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public UUID getOwner() {
        return owner;
    }

    public AwakenedItemData withLevel(int level) {
        return new AwakenedItemData(level, xp, owner);
    }

    public AwakenedItemData withXp(int xp) {
        return new AwakenedItemData(level, xp, owner);
    }
}
