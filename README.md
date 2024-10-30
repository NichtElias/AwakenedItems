
# Awakened Items

[Awakened Items](https://modrinth.com/mod/awakened-items) is a mod for Minecraft introducing items that talk to the player and get stronger as they are used.

## Features

- Awaken any item. Absolutely any item.
- Awakened items will get better the more you use them (similar to Tinkers Tool Leveling). This currently only has an effect for melee weapons, block breaking tools and armor.
- Each awakened item talks to you and has a personality.

## Miscellaneous Abilities of Awakened Items

- They talk to you. Sometimes they even warn you of approaching mobs.
- They don't like being picked up by mobs and will defend themselves.
- They're loyal to their owner and won't work as well for other players.
- They don't despawn. They may get lonely though.

## Supported Items

As stated above, all items can be awakened. If you can drop it you can awaken it. That sadly doesn't mean all items will benefit from awakening.
They will all talk, but the leveling only works if the item in question fulfills certain criteria.

If the item...

- has the `c:tools/melee_weapon` tag, it's considered a melee weapon, and its attack damage increases per level.
- has the `c:tools` tag but not the `c:tools/ranged_weapon` tag and either the `minecraft:axes` tag or not the `c:tools/melee_weapons` tag, it's considered a block breaking tool and its mining speed increases per level.
- has the `c:armors` tag, it's considered armor and its armor and armor toughness values increase per level. These per-level-increases are dependent on the armors original values, so armors that don't have armor toughness don't get it from levels either.

