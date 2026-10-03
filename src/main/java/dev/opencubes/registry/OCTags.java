package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

/**
 * Tags replace the string-matching config lists the original used to describe how blocks
 * behave, which means packs can extend them without touching a config file.
 */
public final class OCTags {

    private OCTags() {}

    public static final class Blocks {

        /** Blocks an elevator sees straight through; they never count towards the limit. */
        public static final TagKey<Block> ELEVATOR_TRANSPARENT = tag("elevator_transparent");

        /** Blocks that stop an elevator outright, however many are allowed in the way. */
        public static final TagKey<Block> ELEVATOR_BLOCKING = tag("elevator_blocking");

        /**
         * Blocks that always count toward {@code maxBlocksPassed}, even when also tagged
         * {@link #ELEVATOR_TRANSPARENT}. Datapacks use this to force-count passable blocks.
         */
        public static final TagKey<Block> ELEVATOR_INCREMENT = tag("elevator_increment");

        /** Extra blocks the crane magnet may lift (beyond MODEL / sand / stairs defaults). */
        public static final TagKey<Block> MAGNET_LIFTABLE = tag("magnet_liftable");

        /** Blocks the crane magnet must never lift. */
        public static final TagKey<Block> MAGNET_BLACKLIST = tag("magnet_blacklist");

        /** Blocks ignored by the cartographer height sampler (treated as air). */
        public static final TagKey<Block> CARTOGRAPHER_INVISIBLE = tag("cartographer_invisible");

        /** Light blocks a Mini Me may break while wandering (flowers, torches, crops). */
        public static final TagKey<Block> MINI_ME_BREAKABLE = tag("mini_me_breakable");

        private Blocks() {}

        private static TagKey<Block> tag(String path) {
            return TagKey.create(Registries.BLOCK, OCConstants.id(path));
        }
    }

    public static final class EntityTypes {

        public static final TagKey<EntityType<?>> MAGNET_LIFTABLE =
                TagKey.create(Registries.ENTITY_TYPE, OCConstants.id("magnet_liftable"));

        public static final TagKey<EntityType<?>> MAGNET_BLACKLIST =
                TagKey.create(Registries.ENTITY_TYPE, OCConstants.id("magnet_blacklist"));

        private EntityTypes() {}
    }

    public static final class BlockEntityTypes {

        public static final TagKey<BlockEntityType<?>> MAGNET_LIFTABLE =
                TagKey.create(Registries.BLOCK_ENTITY_TYPE, OCConstants.id("magnet_liftable_block_entities"));

        private BlockEntityTypes() {}
    }

    public static final class Fluids {

        /**
         * Common experience fluid tag (`c:experience`) for cross-mod XP pipes. This is NeoForge's
         * own key rather than a hand-built one so the tag file lands where everybody else looks.
         */
        public static final TagKey<Fluid> EXPERIENCE = Tags.Fluids.EXPERIENCE;

        private Fluids() {}
    }

    public static final class Items {

        /** Every bucket-shaped item this mod adds (`c:buckets`). */
        public static final TagKey<Item> BUCKETS = Tags.Items.BUCKETS;

        /** Buckets holding liquid experience (`c:buckets/experience`). */
        public static final TagKey<Item> BUCKETS_EXPERIENCE =
                TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "buckets/experience"));

        private Items() {}
    }
}
