# Arcane Staffs (Fabric 1.21.11)

Five elemental staffs. Right-click to cast. Each one has a cooldown and loses 1 durability per cast.

| Staff | Cooldown | Durability | What it does |
|---|---|---|---|
| Staff of Embers (fire) | 1.25 s | 250 | Fire beam, 24 blocks. 7 damage + sets targets on fire around the impact. |
| Staff of Frost (ice) | 1.5 s | 250 | Frost beam, 20 blocks. 4 damage, Slowness IV and freezing around the impact. |
| Staff of Storms (lightning) | 4 s | 120 | Calls a real lightning bolt up to 40 blocks away. |
| Staff of the Earth | 2.5 s | 200 | Ground shockwave in a cone: 6 damage, launches enemies upward, slows them. |
| Staff of Gales (wind) | 2 s | 300 | Blasts enemies away. Sneak + cast = dash through the air. |

## Recipes

All five use the same diagonal shape:

```
. . E
. D .
S . .
```
S = stick, D = diamond, E = element item:
fire = blaze rod, ice = blue ice, lightning = lightning rod, earth = dripstone block, wind = breeze rod.

## Building

Needs JDK 21.

Easiest route: generate a fresh project at https://fabricmc.net/develop/template (Minecraft 1.21.11,
Yarn mappings, mod id `arcanestaffs`, package `dev.maioral.arcanestaffs`), then copy this project's `src/` folder over it.

Or use this folder directly: run `gradle wrapper --gradle-version latest` once (needs Gradle installed),
then `./gradlew build`. The jar ends up in `build/libs/`. Drop it in `mods/` with Fabric API.

If Gradle can't find a version in `gradle.properties`, grab the current numbers from https://fabricmc.net/develop.

## Adding a new staff

1. Add a method in `magic/Spells.java`, e.g. `public static void poison(ServerWorld world, PlayerEntity caster)`.
2. Register it in `ModItems.java`: `staff("poison_staff", 40, 200, Spells::poison)`, and add it to the creative tab list.
3. Add a texture, model, item definition, recipe and lang lines (copy any existing staff's files).

## Getting the jar without installing anything

Push this folder to a GitHub repository. The workflow in `.github/workflows/build.yml` builds it automatically;
open the run under the Actions tab and download `arcanestaffs-jar`; it contains only `arcanestaffs-1.0.0.jar`

