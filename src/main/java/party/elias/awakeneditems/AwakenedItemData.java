package party.elias.awakeneditems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record AwakenedItemData(UUID owner, int level, int xp, boolean heldByOwner) {

    public AwakenedItemData(UUID owner) {
        this(owner, 0, 0, true);
    }

    public AwakenedItemData(UUID owner, boolean heldByOwner) {
        this(owner, 0, 0, heldByOwner);
    }

    public AwakenedItemData(UUID owner, int level, int xp) {
        this(owner, level, xp, true);
    }

    public static final Codec<AwakenedItemData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("owner").forGetter(AwakenedItemData::owner),
                    Codec.INT.fieldOf("level").forGetter(AwakenedItemData::level),
                    Codec.INT.fieldOf("xp").forGetter(AwakenedItemData::xp),
                    Codec.BOOL.fieldOf("heldByOwner").forGetter(AwakenedItemData::heldByOwner)
            ).apply(instance, AwakenedItemData::new)
    );

    public static final StreamCodec<ByteBuf, AwakenedItemData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AwakenedItemData::owner,
            ByteBufCodecs.INT, AwakenedItemData::level,
            ByteBufCodecs.INT, AwakenedItemData::xp,
            ByteBufCodecs.BOOL, AwakenedItemData::heldByOwner,
            AwakenedItemData::new
    );

    public static final AwakenedItemData DEFAULT = new AwakenedItemData(UUID.fromString("0-0-0-0-0"));

    public AwakenedItemData withLevel(int level) {
        return new AwakenedItemData(owner, level, xp, heldByOwner);
    }

    public AwakenedItemData withXp(int xp) {
        return new AwakenedItemData(owner, level, xp, heldByOwner);
    }

    public AwakenedItemData withHeldByOwner(boolean heldByOwner) {
        return new AwakenedItemData(owner, level, xp, heldByOwner);
    }
}
