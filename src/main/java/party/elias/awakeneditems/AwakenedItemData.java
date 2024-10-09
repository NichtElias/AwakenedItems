package party.elias.awakeneditems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record AwakenedItemData(int level, int xp) {

    public static final Codec<AwakenedItemData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("level").forGetter(AwakenedItemData::level),
                    Codec.INT.fieldOf("xp").forGetter(AwakenedItemData::xp)
            ).apply(instance, AwakenedItemData::new)
    );

    public static final StreamCodec<ByteBuf, AwakenedItemData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, AwakenedItemData::level,
            ByteBufCodecs.INT, AwakenedItemData::xp,
            AwakenedItemData::new
    );
}
