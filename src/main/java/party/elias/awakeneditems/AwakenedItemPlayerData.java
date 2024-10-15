package party.elias.awakeneditems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AwakenedItemPlayerData(int timeSinceLastItemMsg) {
    public static final Codec<AwakenedItemPlayerData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("timeSinceLastItemMsg").forGetter(AwakenedItemPlayerData::timeSinceLastItemMsg)
            ).apply(instance, AwakenedItemPlayerData::new)
    );

    public AwakenedItemPlayerData addTimeSinceLastItemMsg(int amount) {
        return new AwakenedItemPlayerData(timeSinceLastItemMsg + amount);
    }

    public AwakenedItemPlayerData withTimeSinceLastItemMsg(int timeSinceLastItemMsg) {
        return new AwakenedItemPlayerData(timeSinceLastItemMsg);
    }
}
