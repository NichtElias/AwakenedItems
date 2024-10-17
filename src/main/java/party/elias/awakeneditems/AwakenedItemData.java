package party.elias.awakeneditems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record AwakenedItemData(UUID owner, int level, int xp, boolean heldByOwner, List<PersonalityTrait> personality) {

    public AwakenedItemData(UUID owner, List<PersonalityTrait> personality) {
        this(owner, 0, 0, true, personality);
    }

    public static final Codec<AwakenedItemData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("owner").forGetter(AwakenedItemData::owner),
                    Codec.INT.fieldOf("level").forGetter(AwakenedItemData::level),
                    Codec.INT.fieldOf("xp").forGetter(AwakenedItemData::xp),
                    Codec.BOOL.fieldOf("heldByOwner").forGetter(AwakenedItemData::heldByOwner),
                    Codec.list(PersonalityTrait.CODEC).fieldOf("personality").forGetter(AwakenedItemData::personality)
            ).apply(instance, AwakenedItemData::new)
    );

    public static final StreamCodec<ByteBuf, AwakenedItemData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AwakenedItemData::owner,
            ByteBufCodecs.INT, AwakenedItemData::level,
            ByteBufCodecs.INT, AwakenedItemData::xp,
            ByteBufCodecs.BOOL, AwakenedItemData::heldByOwner,
            PersonalityTrait.STREAM_CODEC.apply(ByteBufCodecs.list()), AwakenedItemData::personality,
            AwakenedItemData::new
    );

    public AwakenedItemData withLevel(int level) {
        return new AwakenedItemData(owner, level, xp, heldByOwner, personality);
    }

    public AwakenedItemData withXp(int xp) {
        return new AwakenedItemData(owner, level, xp, heldByOwner, personality);
    }

    public AwakenedItemData withHeldByOwner(boolean heldByOwner) {
        return new AwakenedItemData(owner, level, xp, heldByOwner, personality);
    }
}
