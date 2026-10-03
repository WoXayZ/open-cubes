# OpenCubes

The OpenBlocks toolbox, rewritten for Minecraft **1.21.1** / **NeoForge 21.1.247**. Version **1.0**.

Hang gliders, elevators, tanks, graves, a chest that follows you, and a crane you wear. It is a new mod. It does not load a 1.12 world.

The in-game guide is a book called World Domination. Craft a book with a clay ball. The book needs Patchouli.

---

## What's in it

**Getting around.** Elevators (jump to go up, sneak to go down) and rotating elevators. Fans, rope ladders, jaded ladders. The hang glider rides thermals; sneak to dive, V for the variometer. Thermal elytra do the same on vanilla wings. A sleeping bag skips the night without moving your spawn.

**Building.** The building guide ghosts in a shape so you can follow it. Village highlighters trace a village. Sky blocks are windows onto the sky. Imaginary blocks are sketches only you see, unless you hand out the glasses. Paint, a mixer, brushes, stencils, a drawing table and canvas.

**Machines.** Tanks, including liquid XP: drain, shower, bottler, healer. Vacuum hoppers pull in items and XP. Block breaker, block placer, item dropper. Auto anvil and auto enchanting table. The item cannon fires stacks from a chest at whatever you point at. Sprinklers water crops.

**The rest.** Graves keep your inventory. Luggage follows you and picks up drops; lightning doubles its size. The crane backpack lifts blocks from a distance. Trophies drop from mobs you kill and react when you right-click them. Golden egg, bear trap, sponges, sonic glasses, flags, big buttons, height maps and a projector.

Right-click a paint can with the hang glider or the thermal elytra to colour the fabric. The wooden frame stays wood. A painted glider keeps its colour through the smithing recipe.

---

## Requirements

| Dependency | Required | Version |
| --- | --- | --- |
| Minecraft | yes | 1.21.1 |
| NeoForge | yes | 21.1.0+ |
| Java | yes | 21 |
| [Patchouli](https://www.curseforge.com/minecraft/mc-mods/patchouli) | no | 1.21.1-90+ (the guide book) |
| [Curios API](https://www.curseforge.com/minecraft/mc-mods/curios) | no | 9.5.0+ (crane backpack, grave loot slots) |
| [JEI](https://www.curseforge.com/minecraft/mc-mods/jei) | no | 19.21.0+ |
| [Jade](https://www.curseforge.com/minecraft/mc-mods/jade) | no | 15.10.0+ |

Built against NeoForge 21.1.247, Patchouli 1.21.1-93, Curios 9.5.1, JEI 19.43.0 and Jade 15.10.5.

---

## Building

```bash
./gradlew jar
```

On Windows: `gradlew.bat jar`.

The jar lands in `build/libs/opencubes-1.0.jar`.

```bash
./gradlew runClient
./gradlew runData
```

`runData` regenerates `src/generated`. Those files are committed, so a clean run should not change them.

---

## Links

- Source: https://github.com/WoXayZ/open-cubes
- Issues: https://github.com/WoXayZ/open-cubes/issues

## Credits

Inspired by [OpenBlocks](https://github.com/OpenMods/OpenBlocks) and OpenModsLib, MIT, © the OpenMods team, and by [OpenBlocks Reopened](https://github.com/ACGaming/OpenBlocksReopened). OpenCubes shares no code with either. Not an official continuation.

## License

MIT.
