package dev.opencubes.content.imaginary;

import net.minecraft.world.item.ItemStack;

public enum ImaginationGlassesKind {
    PENCIL {
        @Override
        public boolean check(ImaginaryProperty property, ItemStack glasses, ImaginaryBlockEntity block) {
            return block.isPencil() ^ block.isInverted();
        }
    },
    CRAYON {
        @Override
        public boolean check(ImaginaryProperty property, ItemStack glasses, ImaginaryBlockEntity block) {
            Integer colour = ImaginationGlassesItem.getCrayonColour(glasses);
            return (!block.isPencil() && colour != null && colour == block.colour()) ^ block.isInverted();
        }
    },
    TECHNICOLOR {
        @Override
        public boolean check(ImaginaryProperty property, ItemStack glasses, ImaginaryBlockEntity block) {
            if (property == ImaginaryProperty.VISIBLE) {
                return true;
            }
            return block.isInverted();
        }
    },
    ADMIN {
        @Override
        public boolean check(ImaginaryProperty property, ItemStack glasses, ImaginaryBlockEntity block) {
            return true;
        }
    };

    public abstract boolean check(ImaginaryProperty property, ItemStack glasses, ImaginaryBlockEntity block);
}
