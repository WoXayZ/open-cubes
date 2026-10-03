# OpenCubes

A community-driven reimplementation of the ideas behind
[OpenBlocks](https://github.com/OpenMods/OpenBlocks), rebuilt from scratch on modern
Minecraft and NeoForge APIs.

This is **not** a source port of the 1.12 mod. The original code is a reference; the
gameplay is the specification.

## Source hierarchy

The reference repositories are consulted in a fixed order, each for one purpose only:

```
Original gameplay → OpenBlocks → Corrections → OpenBlocks Reopened → Implementation → NeoForge 1.21 APIs
```

- **OpenBlocks** - reference for the original behaviour.
- **OpenBlocks Reopened** - reference for bug fixes and gameplay improvements only, read once and recorded as behaviour. No dependency on its code, architecture or tooling.
- **OpenModsLib** - source of algorithms and utilities, and only where no modern equivalent exists.

Nothing is ported because the original used it. If Java 21, NeoForge or vanilla already
solves the problem, that is the solution.

## Ground rules

Settled before the first line of code. Full text in
[§0 of the inventory](docs/FEATURE-INVENTORY.md).

- **No Mixin without a documented exception, no reflection at all.** Access Transformers are fine - NeoForge's are declarative and build-time. The order is public API, then vanilla API, then AT, then a justified Mixin.
- **We commit to visible behaviour and to recipes.** We do not commit to 1.12 world compatibility, and we do not ship a public API for other mods.
- **All textures are new**, drawn for the project. Models are updated and reused where the shape still holds; sounds are judged one at a time; translation strings are not art and are reused.
- **A feature is finished when it passes its in-game checklist**, not when it compiles. Every feature ships with one.

## Target

| | |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.247 |
| Java | 21 |
| Mod id | `opencubes` |
| Licence | MIT |

## Status

Early development. The build runs, the mod loads, and the first two features are in.

| Step | State |
| --- | --- |
| Feature inventory of the original mods | Done - [`docs/FEATURE-INVENTORY.md`](docs/FEATURE-INVENTORY.md) |
| Project policies | Done - §0 of the inventory |
| Design decisions | Done - §14, nothing blocking |
| Project skeleton (Gradle, registries, datagen) | Done - phase 0 of §13 |
| Elevator and Rotating Elevator | Code done - smoke-tested |
| Big Button, 13 materials | Code done - smoke-test at will |
| Jaded Ladder, Rope Ladder, Fan, Bear Trap, Flag | Code done - smoke-test at will |
| Liquid XP + Tank (single-block render) | Code done - smoke-test at will |
| XP Drain, XP Shower, XP Bottler | Code done - smoke-test at will |
| Vacuum Hopper, Item Dropper, Block Breaker, Block Placer | Code done - smoke-test at will |
| Auto Anvil, Auto Enchantment Table | Code done - smoke-test at will |
| Grave + `/opencubes inventory` | Code done - smoke-test at will |
| Trophy (datapack types + behaviours) | Code done - smoke-test at will |
| Phase 9 items (Luggage, /dev/null, Sleeping Bag, Slimalyzer, Pedometer, Wrench, Golden Eye) | Code done - smoke-test at will |
| Phase 10 painting (Canvas, Mixer, Brush, Stencil, Drawing Table, Glyph…) | Code done - smoke-test at will |
| Phase 11 Building Guide + Enhanced Guide + shape generators | Code done - smoke-test at will |
| Phase 12 height maps (Empty/Height Map, Cartographer, Projector) | Code done - smoke-test at will |
| Phase 13 crane (Backpack, Control, Magnet, Mounted Block) | Code done - smoke-test at will |

81+ blocks + paint tools + Glyph + building guides + height maps. Full checklists wait until the whole mod is in; quick smoke tests as we go.

## Building

```
./gradlew build        # the mod jar, in build/libs
./gradlew runClient    # a development client
./gradlew runData      # regenerate everything under src/generated
```

Gradle provisions its own JDK 21 if the machine does not have one. Generated resources are
committed, so `runData` should produce no diff unless you meant it to.

## Credits

OpenBlocks and OpenModsLib are MIT licensed, © the OpenMods team. OpenCubes reuses their
ideas, their algorithms and some of their model geometry, with gratitude. All textures are
original to this project.

[OpenBlocks Reopened](https://github.com/ACGaming/OpenBlocksReopened) by ACGaming and
contributors is the maintained 1.12.2 fork. Its bug reports and fixes tell us which
original behaviours were wrong. OpenCubes shares no code with it.
