package party.elias.awakeneditems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum PersonalityTrait {
    ARROGANT(Set.of()),
    LAZY(Set.of()),
    INSECURE(Set.of()),
    SCHEMING(Set.of()),
    MERRY(Set.of());

    private final Set<String> incompatibleTraits;

    PersonalityTrait(Set<String> incompatibleTraits) {
        this.incompatibleTraits = incompatibleTraits;
    }

    public Set<String> getIncompatibleTraits() {
        return incompatibleTraits;
    }

    public Set<PersonalityTrait> getIncompatibleTraitsAsTraits() {
        return Set.copyOf(incompatibleTraits.stream().map(PersonalityTrait::valueOf).toList());
    }

    public String lower() {
        return name().toLowerCase();
    }

    public static final Map<String, PersonalityTrait> MAP = new HashMap<>();

    public static final Set<PersonalityTrait> SET = new HashSet<>();

    public static final Codec<PersonalityTrait> CODEC = Codec.STRING.comapFlatMap(
            s -> {
                try {
                    return DataResult.success(PersonalityTrait.valueOf(s));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "'" + s + "' is not a valid value for enum PersonalityTrait.");
                }
            },
            PersonalityTrait::name
    );

    public static final StreamCodec<ByteBuf, PersonalityTrait> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PersonalityTrait::name,
            PersonalityTrait::valueOf
    );

    static {
        for (PersonalityTrait trait: PersonalityTrait.values()) {
            MAP.put(trait.name(), trait);
            SET.add(trait);
        }
    }
}
