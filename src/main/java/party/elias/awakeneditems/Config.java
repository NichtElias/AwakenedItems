package party.elias.awakeneditems;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = AwakenedItems.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final String TL_KEY = AwakenedItems.MODID + ".configuration.";

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /*
    private static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    private static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);
     */

    private static final ModConfigSpec.IntValue LEVEL_XP_BASE;
    private static final ModConfigSpec.DoubleValue LEVEL_XP_MULTIPLIER;

    private static final ModConfigSpec.IntValue LEVEL_XP_PER_BROKEN_BLOCK;
    private static final ModConfigSpec.IntValue LEVEL_XP_PER_MELEE_ATTACK;
    private static final ModConfigSpec.IntValue LEVEL_XP_PER_TOOL_USE;
    private static final ModConfigSpec.IntValue LEVEL_XP_PER_SHOT;
    private static final ModConfigSpec.IntValue LEVEL_XP_PER_ARMOR_HIT;
    private static final ModConfigSpec.IntValue LEVEL_XP_PER_FISH;

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
    }

    /*
    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
     */

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

        /*
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // convert the list of strings into a set of items
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet());
         */
    }
}
