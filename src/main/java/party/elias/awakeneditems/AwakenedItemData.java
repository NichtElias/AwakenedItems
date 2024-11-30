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

public record AwakenedItemData(UUID owner, int level, int xp, Flags flags, List<PersonalityTrait> personality) {

    public AwakenedItemData(UUID owner, List<PersonalityTrait> personality) {
        this(owner, 0, 0, new Flags(0), personality);
    }

    public static final Codec<AwakenedItemData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("owner").forGetter(AwakenedItemData::owner),
                    Codec.INT.fieldOf("level").forGetter(AwakenedItemData::level),
                    Codec.INT.fieldOf("xp").forGetter(AwakenedItemData::xp),
                    Flags.CODEC.optionalFieldOf("flags", new Flags(0)).forGetter(AwakenedItemData::flags),
                    Codec.list(PersonalityTrait.CODEC).fieldOf("personality").forGetter(AwakenedItemData::personality)
            ).apply(instance, AwakenedItemData::new)
    );

    public static final StreamCodec<ByteBuf, AwakenedItemData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AwakenedItemData::owner,
            ByteBufCodecs.INT, AwakenedItemData::level,
            ByteBufCodecs.INT, AwakenedItemData::xp,
            Flags.STREAM_CODEC, AwakenedItemData::flags,
            PersonalityTrait.STREAM_CODEC.apply(ByteBufCodecs.list()), AwakenedItemData::personality,
            AwakenedItemData::new
    );

    public AwakenedItemData withLevel(int level) {
        return new AwakenedItemData(owner, level, xp, flags, personality);
    }

    public AwakenedItemData withXp(int xp) {
        return new AwakenedItemData(owner, level, xp, flags, personality);
    }

    public AwakenedItemData withHeldByOwner(boolean heldByOwner) {
        return new AwakenedItemData(owner, level, xp, flags.set(Flags.Flag.HELD_BY_OWNER, heldByOwner), personality);
    }

    public AwakenedItemData withFlagSet(Flags.Flag flag, boolean value) {
        return new AwakenedItemData(owner, level, xp, flags.set(flag, value), personality);
    }

    public boolean heldByOwner() {
        return flags.get(Flags.Flag.HELD_BY_OWNER);
    }

    public boolean isFlagSet(Flags.Flag flag) {
        return flags.get(flag);
    }

    public static class Flags {

        static final Codec<Flags> CODEC = Codec.INT.xmap(Flags::new, codecFlags -> codecFlags.flags);
        static final StreamCodec<ByteBuf, Flags> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

        private final int flags;

        Flags(int flags) {
            this.flags = flags;
        }

        public boolean get(Flag flag) {
            return (flags & (1 << flag.ordinal())) != 0;
        }

        public Flags set(Flag flag, boolean value) {
            int mask = 1 << flag.ordinal();
            if (value)
                return new Flags(flags | mask);
            else
                return new Flags(flags & ~mask);
        }

        public enum Flag {
            HELD_BY_OWNER,
            MILESTONE_XP,
            MILESTONE_REQUIREMENTS;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            Flags flags1 = (Flags) o;
            return flags == flags1.flags;
        }

        @Override
        public int hashCode() {
            return flags;
        }
    }
}
