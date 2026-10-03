package dev.opencubes.compat.patchouli;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.UnaryOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.slf4j.Logger;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;

/**
 * Book screenshot of any size. Patchouli's image page reads a fixed 200×200 corner of a texture it
 * assumes to be 256×256, so anything else comes out cropped. This reads the real size, trims the
 * transparent margin and fits what is left into the usual 100×100 window, keeping its ratio. The
 * frame is drawn as four corners of the book's image border so it hugs the picture.
 *
 * <p>Built from the {@code opencubes:fitted_image} template, which passes {@code #image}.
 */
public class FittedImageComponent implements ICustomComponent {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int BORDER_U = 405;
    private static final int BORDER_V = 149;
    private static final int BORDER_SIZE = 106;
    private static final int BORDER_INSET = 3;
    private static final int MAX_INNER = BORDER_SIZE - 2 * BORDER_INSET;
    private static final int PAGE_WIDTH = 116;
    private static final int BOOK_TEXTURE_WIDTH = 512;
    private static final int BOOK_TEXTURE_HEIGHT = 256;

    String image;

    private transient ResourceLocation texture;
    private transient int top;
    private transient int textureWidth;
    private transient int textureHeight;
    private transient int cropX;
    private transient int cropY;
    private transient int cropWidth;
    private transient int cropHeight;
    private transient int drawWidth;
    private transient int drawHeight;
    private transient boolean filtered;

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup, HolderLookup.Provider registries) {
        image = lookup.apply(IVariable.wrap(image, registries)).asString();
    }

    @Override
    public void build(int componentX, int componentY, int pageNum) {
        top = componentY;
        texture = ResourceLocation.tryParse(image);
        if (texture == null || !measure(texture)) {
            textureWidth = textureHeight = cropWidth = cropHeight = 256;
            cropX = cropY = 0;
        }
        float scale = Math.min(MAX_INNER / (float) cropWidth, MAX_INNER / (float) cropHeight);
        drawWidth = Math.max(1, Math.round(cropWidth * scale));
        drawHeight = Math.max(1, Math.round(cropHeight * scale));
        filtered = false;
    }

    private boolean measure(ResourceLocation location) {
        Resource resource = Minecraft.getInstance().getResourceManager().getResource(location).orElse(null);
        if (resource == null) {
            return false;
        }
        try (InputStream stream = resource.open(); NativeImage pixels = NativeImage.read(stream)) {
            textureWidth = pixels.getWidth();
            textureHeight = pixels.getHeight();
            int minX = textureWidth;
            int minY = textureHeight;
            int maxX = -1;
            int maxY = -1;
            for (int y = 0; y < textureHeight; y++) {
                for (int x = 0; x < textureWidth; x++) {
                    if ((pixels.getPixelRGBA(x, y) >>> 24) != 0) {
                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                    }
                }
            }
            if (maxX < 0) {
                minX = minY = 0;
                maxX = textureWidth - 1;
                maxY = textureHeight - 1;
            }
            cropX = minX;
            cropY = minY;
            cropWidth = maxX - minX + 1;
            cropHeight = maxY - minY + 1;
            return true;
        } catch (IOException e) {
            LOGGER.warn("Could not read book image {}", location, e);
            return false;
        }
    }

    @Override
    public void render(GuiGraphics graphics, IComponentRenderContext context, float pticks, int mouseX, int mouseY) {
        if (texture == null) {
            return;
        }
        if (!filtered) {
            // Screenshots are shrunk a lot; nearest sampling would turn them to noise.
            Minecraft.getInstance().getTextureManager().getTexture(texture).setFilter(true, false);
            filtered = true;
        }
        int frameWidth = drawWidth + 2 * BORDER_INSET;
        int frameHeight = drawHeight + 2 * BORDER_INSET;
        int frameX = (PAGE_WIDTH - frameWidth) / 2;
        int frameY = top + (BORDER_SIZE - frameHeight) / 2;

        graphics.setColor(1F, 1F, 1F, 1F);
        RenderSystem.enableBlend();
        graphics.blit(texture, frameX + BORDER_INSET, frameY + BORDER_INSET, drawWidth, drawHeight,
                cropX, cropY, cropWidth, cropHeight, textureWidth, textureHeight);
        drawFrame(graphics, context.getBookTexture(), frameX, frameY, frameWidth, frameHeight);
    }

    private static void drawFrame(GuiGraphics graphics, ResourceLocation book, int x, int y, int width, int height) {
        int left = width / 2;
        int right = width - left;
        int upper = height / 2;
        int lower = height - upper;
        int farU = BORDER_U + BORDER_SIZE - right;
        int farV = BORDER_V + BORDER_SIZE - lower;
        blitBorder(graphics, book, x, y, BORDER_U, BORDER_V, left, upper);
        blitBorder(graphics, book, x + left, y, farU, BORDER_V, right, upper);
        blitBorder(graphics, book, x, y + upper, BORDER_U, farV, left, lower);
        blitBorder(graphics, book, x + left, y + upper, farU, farV, right, lower);
    }

    private static void blitBorder(GuiGraphics graphics, ResourceLocation book, int x, int y, int u, int v, int w, int h) {
        graphics.blit(book, x, y, u, v, w, h, BOOK_TEXTURE_WIDTH, BOOK_TEXTURE_HEIGHT);
    }
}
