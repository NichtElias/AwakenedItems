package party.elias.awakeneditems;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = AwakenedItems.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final String TL_KEY = AwakenedItems.MODID + ".configuration.";

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue LEVEL_XP_BASE;
    private static final ModConfigSpec.DoubleValue LEVEL_XP_MULTIPLIER;

    private static final ModConfigSpec.IntValue LEVEL_XP_PER_BROKEN_BLOCK;
    private static final ModConfigSpec.IntValue LEVEL_XP_PER_MELEE_ATTACK;
    private static final ModConfigSpec.IntValue LEVEL_XP_PER_TOOL_USE;
    private static final ModConfigSpec.IntValue LEVEL_XP_PER_SHOT;
    private static final ModConfigSpec.IntValue LEVEL_XP_PER_ARMOR_HIT;
    private static final ModConfigSpec.IntValue LEVEL_XP_PER_FISH;
    private static final ModConfigSpec.IntValue LEVEL_XP_PER_CURIO_HECTOTICK;
    private static final ModConfigSpec.IntValue LEVEL_XP_PER_GLIDER_HECTOTICK;

    static final ModConfigSpec SPEC;

    static {
        BUILDER.comment("Config values regarding item leveling").push("level");

        LEVEL_XP_BASE = BUILDER.comment("The required xp for the next level are given by xpMultiplier^currentLevel * xpBase")
                .translation(TL_KEY + "level.xpBase")
                .defineInRange("xpBase", 500, 1, Integer.MAX_VALUE);
        LEVEL_XP_MULTIPLIER = BUILDER.comment("The required xp for the next level are given by xpMultiplier^currentLevel * xpBase")
                .translation(TL_KEY + "level.xpMultiplier")
                .defineInRange("xpMultiplier", 1.25, -Double.MAX_VALUE, Double.MAX_VALUE);

        LEVEL_XP_PER_BROKEN_BLOCK = BUILDER.comment("The amount of xp a block breaking tool gains per block you break with it")
                .translation(TL_KEY + "level.xpPerBrokenBlock")
                .defineInRange("xpPerBrokenBlock", 1, 0, Integer.MAX_VALUE);
        LEVEL_XP_PER_MELEE_ATTACK = BUILDER.comment("The amount of xp a melee weapon gains per attack")
                .translation(TL_KEY + "level.xpPerMeleeAttack")
                .defineInRange("xpPerMeleeAttack", 2, 0, Integer.MAX_VALUE);
        LEVEL_XP_PER_TOOL_USE = BUILDER.comment("The amount of xp a tool gains per right click use")
                .translation(TL_KEY + "level.xpPerToolUse")
                .defineInRange("xpPerToolUse", 1, 0, Integer.MAX_VALUE);
        LEVEL_XP_PER_SHOT = BUILDER.comment("The amount of xp a bow or crossbow gains per shot")
                .translation(TL_KEY + "level.xpPerShot")
                .defineInRange("xpPerShot", 4, 0, Integer.MAX_VALUE);
        LEVEL_XP_PER_ARMOR_HIT = BUILDER.comment("The amount of xp an armor piece gains per damage you take")
                .translation(TL_KEY + "level.xpPerArmorHit")
                .defineInRange("xpPerArmorHit", 4, 0, Integer.MAX_VALUE);
        LEVEL_XP_PER_FISH = BUILDER.comment("The amount of xp a fishing rod gains each time you fish")
                .translation(TL_KEY + "level.xpPerFish")
                .defineInRange("xpPerFish", 4, 0, Integer.MAX_VALUE);
        LEVEL_XP_PER_CURIO_HECTOTICK = BUILDER.comment("The amount of xp a curio gains every 100 ticks while in the owner's inventory")
                .translation(TL_KEY + "level.xpPerCurioHectotick")
                .defineInRange("xpPerCurioHectotick", 2, 0, Integer.MAX_VALUE);
        LEVEL_XP_PER_GLIDER_HECTOTICK = BUILDER.comment("The amount of xp a glider (elytra, etc.) gains every 100 ticks while flying")
                .translation(TL_KEY + "level.xpPerGliderHectotick")
                .defineInRange("xpPerGliderHectotick", 4, 0, Integer.MAX_VALUE);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static class Level {
        public static int xpBase;
        public static double xpMultiplier;

        public static int xpPerBrokenBock;
        public static int xpPerMeleeAttack;
        public static int xpPerToolUse;
        public static int xpPerShot;
        public static int xpPerArmorHit;
        public static int xpPerFish;
        public static int xpPerCurioHectotick;
        public static int xpPerGliderHectotick;
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        Level.xpBase = LEVEL_XP_BASE.get();
        Level.xpMultiplier = LEVEL_XP_MULTIPLIER.get();

        Level.xpPerBrokenBock = LEVEL_XP_PER_BROKEN_BLOCK.get();
        Level.xpPerMeleeAttack = LEVEL_XP_PER_MELEE_ATTACK.get();
        Level.xpPerToolUse = LEVEL_XP_PER_TOOL_USE.get();
        Level.xpPerShot = LEVEL_XP_PER_SHOT.get();
        Level.xpPerArmorHit = LEVEL_XP_PER_ARMOR_HIT.get();
        Level.xpPerFish = LEVEL_XP_PER_FISH.get();
        Level.xpPerCurioHectotick = LEVEL_XP_PER_CURIO_HECTOTICK.get();
        Level.xpPerGliderHectotick = LEVEL_XP_PER_GLIDER_HECTOTICK.get();

    }
}
