package party.elias.awakeneditems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

import java.util.Map;

public record Personality(MajorTrait major, MinorTrait minor) {

    public static final Codec<Personality> UPDATE_CODEC = Codec.list(MajorTrait.CODEC).flatComapMap(
            list -> new Personality(list.getFirst(), MinorTrait.NEUTRAL),
            personality -> DataResult.error(() -> "UPDATE_CODEC can't be used for encoding.")
    );

    public static final Codec<Personality> CODEC = NeoForgeExtraCodecs.withAlternative(RecordCodecBuilder.create(instance ->
                    instance.group(
                            MajorTrait.CODEC.fieldOf("major").forGetter(Personality::major),
                            MinorTrait.CODEC.fieldOf("minor").forGetter(Personality::minor)
                    ).apply(instance, Personality::new)
    ), UPDATE_CODEC);

    public static final StreamCodec<ByteBuf, Personality> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public static Personality random() {
        return new Personality(Utils.randomChoice(MajorTrait.values()), Utils.randomChoice(MinorTrait.values()));
    }

    public enum MajorTrait { // make this one's codec like MinorTrait's once most people have updated
        ARROGANT,
        LAZY,
        INSECURE,
        ERUDITE,
        MERRY;

        public String lower() {
            return name().toLowerCase();
        }

        private static final Map<String, String> UPDATE_MAP = Map.of(
                "SCHEMING", "ERUDITE"
        );

        public static final Codec<MajorTrait> CODEC = Codec.STRING.comapFlatMap(
                s -> {
                    try {
                        if (UPDATE_MAP.containsKey(s)) return DataResult.success(MajorTrait.valueOf(UPDATE_MAP.get(s)));
                        return DataResult.success(MajorTrait.valueOf(s));
                    } catch (IllegalArgumentException e) {
                        return DataResult.error(() -> "'" + s + "' is not a valid value for enum PersonalityTrait.");
                    }
                },
                MajorTrait::name
        );
    }

    public enum MinorTrait implements StringRepresentable {
        NEUTRAL,
        REFINED,
        CRUDE;

        public String lower() {
            return name().toLowerCase();
        }

        public static final Codec<MinorTrait> CODEC = StringRepresentable.fromEnum(MinorTrait::values);

        @Override
        public String getSerializedName() {
            return name();
        }
    }
}
