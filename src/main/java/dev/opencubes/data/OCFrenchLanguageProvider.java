package dev.opencubes.data;

import dev.opencubes.OCConstants;
import dev.opencubes.content.button.BigButtonMaterial;
import dev.opencubes.content.guide.GuideShape;
import dev.opencubes.content.paint.StencilPattern;
import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCItems;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.common.data.LanguageProvider;

/** French (France) translations. Item names follow OpenBlocks fr_fr where sensible. */
public class OCFrenchLanguageProvider extends LanguageProvider {

    private static final Map<DyeColor, String> DYE_FR = new EnumMap<>(DyeColor.class);

    static {
        DYE_FR.put(DyeColor.WHITE, "Blanc");
        DYE_FR.put(DyeColor.ORANGE, "Orange");
        DYE_FR.put(DyeColor.MAGENTA, "Magenta");
        DYE_FR.put(DyeColor.LIGHT_BLUE, "Bleu clair");
        DYE_FR.put(DyeColor.YELLOW, "Jaune");
        DYE_FR.put(DyeColor.LIME, "Vert clair");
        DYE_FR.put(DyeColor.PINK, "Rose");
        DYE_FR.put(DyeColor.GRAY, "Gris");
        DYE_FR.put(DyeColor.LIGHT_GRAY, "Gris clair");
        DYE_FR.put(DyeColor.CYAN, "Cyan");
        DYE_FR.put(DyeColor.PURPLE, "Violet");
        DYE_FR.put(DyeColor.BLUE, "Bleu");
        DYE_FR.put(DyeColor.BROWN, "Marron");
        DYE_FR.put(DyeColor.GREEN, "Vert");
        DYE_FR.put(DyeColor.RED, "Rouge");
        DYE_FR.put(DyeColor.BLACK, "Noir");
    }

    public OCFrenchLanguageProvider(PackOutput output) {
        super(output, OCConstants.MOD_ID, "fr_fr");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + OCConstants.MOD_ID + ".main", "OpenCubes");
        add(OCItems.INFO_BOOK.get(), "Domination mondiale");
        add("book.opencubes.world_domination.landing",
                "Ascenseurs, grues, XP liquide, peinture, planeurs et le reste.$(br2)"
                        + "Chaque entrée décrit le craft et le comportement.");
        add("book.opencubes.world_domination.subtitle", "Manuel de terrain pratique");
        add("opencubes.misc.info_book_missing_patchouli",
                "Installez Patchouli pour lire ce livre.");

        for (DyeColor colour : DyeColor.values()) {
            String name = DYE_FR.getOrDefault(colour, titleCase(colour.getName()));
            add(OCBlocks.ELEVATORS.get(colour).get(), "Ascenseur " + name.toLowerCase());
            add(OCBlocks.ROTATING_ELEVATORS.get(colour).get(), "Ascenseur rotatif " + name.toLowerCase());
            add(OCBlocks.FLAGS.get(colour).get(), "Drapeau " + name.toLowerCase());
            add(OCBlocks.WOOL_SLABS.get(colour).get(), "Dalle de laine " + name.toLowerCase());
            add(OCBlocks.WOOL_STAIRS.get(colour).get(), "Escalier en laine " + name.toLowerCase());
            add(OCItems.SLEEPING_BAGS.get(colour).get(), "Sac de couchage " + name.toLowerCase());
        }

        for (BigButtonMaterial material : BigButtonMaterial.ALL) {
            add(OCBlocks.BIG_BUTTONS.get(material).get(), "Gros bouton " + materialFr(material));
        }

        add(OCBlocks.ROPE_LADDER.get(), "Échelle de corde");
        add(OCBlocks.FAN.get(), "Ventilateur");
        add(OCBlocks.BEAR_TRAP.get(), "Piège à ours");
        add(OCBlocks.TANK.get(), "Réservoir");
        add(OCBlocks.XP_DRAIN.get(), "Drain d'XP");
        add(OCBlocks.XP_SHOWER.get(), "Douche d'XP");
        add(OCBlocks.XP_BOTTLER.get(), "Embouteilleur d'XP");
        add(OCBlocks.VACUUM_HOPPER.get(), "Entonnoir à vide");
        add(OCBlocks.ITEM_DROPPER.get(), "Lanceur avancé");
        add(OCBlocks.BLOCK_BREAKER.get(), "Briseur de blocs");
        add(OCBlocks.BLOCK_PLACER.get(), "Placeur de blocs");
        add(OCBlocks.AUTO_ANVIL.get(), "Enclume auto");
        add(OCBlocks.AUTO_ENCHANTMENT_TABLE.get(), "Table d'enchantement auto");
        add(OCBlocks.GRAVE.get(), "Tombe");
        add(OCBlocks.TROPHY.get(), "Trophée");
        add("block.opencubes.trophy.entity", "Trophée de %s");
        add("opencubes.jade.trophy.type", "Type : %s");
        add("opencubes.jade.trophy.cooldown", "Recharge : %ss");
        add("opencubes.jade.trophy.ready", "Prêt");
        add("opencubes.jade.trophy.drop", "Butin : %sx %s");
        add("config.jade.plugin_opencubes.trophy", "Trophée");
        add("opencubes.jade.tank.fluid", "%s : %sB / %sB");
        add("opencubes.jade.tank.empty", "Vide : 0B / %sB");
        add("opencubes.jade.tank.count", "%s réservoirs connectés");
        add("config.jade.plugin_opencubes.tank", "Réservoir");
        add("opencubes.jade.luggage.special", "Spécial");
        add("opencubes.jade.luggage.slots", "%s / %s emplacements");
        add("config.jade.plugin_opencubes.luggage", "Bagage");
        add("opencubes.luggage.tooltip.entry", "%sx %s");
        add("opencubes.luggage.tooltip.more", "... et %s de plus");
        add(OCItems.XP_BUCKET.get(), "Seau d'XP");
        add(OCItems.WRENCH.get(), "Clé");
        add(OCItems.SLIMALYZER.get(), "Slimalyseur");
        add(OCItems.PEDOMETER.get(), "Podomètre");
        add(OCItems.DEV_NULL.get(), "/dev/null");
        add(OCItems.GOLDEN_EYE.get(), "Œil doré");
        add(OCItems.LUGGAGE.get(), "Bagage");
        add(OCBlocks.CANVAS.get(), "Toile");
        add(OCBlocks.GLASS_CANVAS.get(), "Toile de verre");
        add(OCBlocks.PAINT_CAN.get(), "Pot de peinture");
        add(OCBlocks.PAINT_MIXER.get(), "Mélangeur de peinture");
        add(OCBlocks.DRAWING_TABLE.get(), "Table à dessin");
        add(OCBlocks.BUILDING_GUIDE.get(), "Guide de construction");
        add(OCBlocks.ENHANCED_BUILDING_GUIDE.get(), "Guide de construction amélioré");
        add(OCBlocks.HEIGHT_MAP_PROJECTOR.get(), "Projecteur de carte de hauteur");
        add(OCItems.EMPTY_MAP.get(), "Carte vide");
        add(OCItems.HEIGHT_MAP.get(), "Carte de hauteur");
        add(OCItems.CARTOGRAPHER.get(), "Cartographe");
        add("entity.opencubes.cartographer", "Cartographe");
        add("opencubes.misc.cartographer_progress", "Cartographie : %s%%");
        add(OCItems.BEAM.get(), "Poutre");
        add(OCItems.LINE.get(), "Câble");
        add(OCItems.CRANE_ENGINE.get(), "Moteur de grue");
        add(OCItems.CRANE_MAGNET.get(), "Aimant de grue");
        add(OCItems.CRANE_BACKPACK.get(), "Sac à dos grue");
        add(OCItems.CRANE_CONTROL.get(), "Contrôle de grue");
        add("opencubes.tooltip.crane_control",
                "Équipe le sac à dos grue. Maintenir = descendre, sneak+maintenir = monter, clic gauche = grab/lâcher.");
        add("opencubes.tooltip.sky", "Affiche le ciel tant qu'il est alimenté.");
        add("opencubes.tooltip.sky_inverted", "Affiche le ciel tant qu'il n'est pas alimenté, à l'envers.");
        add(OCItems.GLIDER_WING_LEFT.get(), "Aile de planeur gauche");
        add(OCItems.GLIDER_WING_RIGHT.get(), "Aile de planeur droite");
        add(OCItems.HANG_GLIDER.get(), "Deltaplane");
        add(OCItems.THERMAL_ELYTRA.get(), "Élytres thermiques");
        add(OCItems.SONIC_GLASSES.get(), "Lunettes soniques");
        add(OCItems.PENCIL.get(), "Crayon");
        add(OCItems.CRAYON.get(), "Crayon de couleur");
        add(OCItems.PENCIL_GLASSES.get(), "Lunettes crayon");
        add(OCItems.CRAYON_GLASSES.get(), "Lunettes crayon de couleur");
        add(OCItems.TECHNICOLOR_GLASSES.get(), "Lunettes technicolor fantastiques");
        add(OCItems.ADMIN_GLASSES.get(), "Lunettes de badass");
        add(OCBlocks.IMAGINARY.get(), "Bloc imaginaire");
        add(OCBlocks.SKY_BLOCK.get(), "Bloc ciel");
        add(OCBlocks.INVERTED_SKY_BLOCK.get(), "Bloc ciel inversé");
        add("opencubes.misc.glider_engaged", "Deltaplane déployé");
        add("opencubes.misc.glider_stowed", "Deltaplane rangé");
        add("opencubes.misc.imaginary_uses", "Utilisations : %s");
        add("opencubes.misc.color", "Couleur : %s");
        add("opencubes.misc.mode.block", "Mode : Bloc");
        add("opencubes.misc.mode.panel", "Mode : Panneau");
        add("opencubes.misc.mode.half_panel", "Mode : Demi-panneau");
        add("opencubes.misc.mode.stairs", "Mode : Escaliers");
        add("opencubes.misc.mode.inverted_block", "Mode : Bloc inversé");
        add("opencubes.misc.mode.inverted_panel", "Mode : Panneau inversé");
        add("opencubes.misc.mode.inverted_half_panel", "Mode : Demi-panneau inversé");
        add("opencubes.misc.mode.inverted_stairs", "Mode : Escaliers inversés");
        add("key.categories.opencubes", "OpenCubes");
        add("key.opencubes.variometer", "Variomètre on/off");
        add("key.opencubes.variometer_volume_up", "Volume variomètre +");
        add("key.opencubes.variometer_volume_down", "Volume variomètre -");
        add("opencubes.misc.variometer_volume", "Volume du variomètre : %s%%");
        add("opencubes.misc.variometer_on", "Variomètre : ON");
        add("opencubes.misc.variometer_off", "Variomètre : OFF");
        add(OCItems.CURSOR.get(), "Curseur");
        add("opencubes.misc.cursor_bound", "Lié à %s, %s, %s");
        add("opencubes.misc.cursor_unbound", "Non lié - utilisez un bloc en sneak");
        add("opencubes.misc.cursor_wrong_dim", "La cible du curseur est dans une autre dimension");
        add("opencubes.misc.cursor_too_far", "La cible du curseur est trop loin");
        add("opencubes.misc.cursor_no_xp", "Pas assez d'expérience pour utiliser le Curseur");
        add("advancements.opencubes.root.title", "OpenCubes");
        add("advancements.opencubes.root.description", "Installez OpenCubes et commencez");
        add("advancements.opencubes.brick_dropped.title", "C'est juste dégoûtant");
        add("advancements.opencubes.brick_dropped.description", "Lâchez une brique");
        add("advancements.opencubes.stack_overflow.title", "Débordement de piles");
        add("advancements.opencubes.stack_overflow.description", "Mettez un /dev/null dans un /dev/null");
        add("stat.opencubes.bricks_dropped", "Briques abandonnées");
        add(OCBlocks.TEMPORARY_SCAFFOLDING.get(), "Échafaudage temporaire");
        add(OCBlocks.LIQUID_SPONGE.get(), "Éponge à liquides");
        add(OCItems.LIQUID_SPONGE_ON_A_STICK.get(), "Éponge à liquides sur un bâton");
        add(OCItems.SPONGE_ON_A_STICK.get(), "Éponge sur un bâton");
        add(OCItems.WET_SPONGE_ON_A_STICK.get(), "Éponge mouillée sur un bâton");
        add(OCBlocks.HEALER.get(), "Soigneur");
        add(OCBlocks.SPRINKLER.get(), "Arroseur");
        add("container.opencubes.sprinkler", "Arroseur");
        add("container.opencubes.sprinkler.water", "Eau : %s / %s mB");
        add(OCBlocks.ARCHERY_TARGET.get(), "Cible de tir");
        add(OCBlocks.ITEM_CANNON.get(), "Canon à objets");
        add(OCItems.POINTER.get(), "Pointeur");
        add(OCBlocks.GOLDEN_EGG.get(), "Œuf doré");
        add(OCBlocks.VILLAGE_HIGHLIGHTER.get(), "Surligneur de village");
        add("entity.opencubes.mini_me", "Mini-moi");
        add("enchantment.opencubes.unstable", "Instable");
        add("enchantment.opencubes.flim_flam", "Flim Flam");
        add("enchantment.opencubes.last_stand", "Dernier combat");
        add(OCItems.EPIC_ERASER.get(), "Gomme épique");
        add(OCItems.TASTY_CLAY.get(), "Argile savoureuse");
        add("key.opencubes.drop_brick", "Lâcher une brique");
        add("opencubes.misc.flim_flammed", "Vous avez été flim-flammé !");
        add("commands.opencubes.flimflam.ok", "%s flim-flammé avec %s");
        add("commands.opencubes.flimflam.fail", "Impossible de flim-flammer %s avec %s");
        add("commands.opencubes.luck.read", "Chance de %s : %s");
        add("commands.opencubes.luck.set", "Chance de %s maintenant : %s");
        add("opencubes.misc.pointer_selected", "Canon sélectionné en %s, %s, %s");
        add("opencubes.misc.pointer_aimed", "Visé vers %s, %s, %s");
        add("entity.opencubes.magnet", "Aimant");
        add("entity.opencubes.mounted_block", "Bloc monté");
        add("container.opencubes.height_map_projector", "Projecteur de carte de hauteur");
        add("opencubes.misc.map_scale", "Échelle : 1:%s");
        add("opencubes.misc.map_id", "Carte #%s");
        add("opencubes.misc.map_center", "Centre : %s, %s");
        add(OCItems.PAINT_BRUSH.get(), "Pinceau");
        add(OCItems.SQUEEGEE.get(), "Raclette");
        add(OCItems.STENCIL.get(), "Pochoir");
        add(OCItems.UNPREPARED_STENCIL.get(), "Pochoir non préparé");
        add(OCItems.SKETCHING_PENCIL.get(), "Crayon à croquis");
        add(OCItems.GLYPH.get(), "Glyphe");
        add("item.opencubes.glyph.named", "Glyphe « %s »");
        add("entity.opencubes.luggage", "Bagage");
        add("entity.opencubes.golden_eye", "Œil doré");
        add("entity.opencubes.glyph", "Glyphe");
        add("container.opencubes.paint_mixer", "Mélangeur de peinture");
        add("container.opencubes.drawing_table", "Table à dessin");
        add("opencubes.gui.paint_mixer.mix", "Mélanger");
        add("opencubes.gui.paint_mixer.color", "Couleur");
        add("opencubes.gui.paint_mixer.slot.milk", "Seau de lait ou pot de peinture vide");
        add("opencubes.gui.paint_mixer.slot.milk_short", "Lait");
        add("opencubes.gui.paint_mixer.slot.ink", "Encre");
        add("opencubes.gui.paint_mixer.slot.cyan", "Teinture cyan");
        add("opencubes.gui.paint_mixer.slot.magenta", "Teinture magenta");
        add("opencubes.gui.paint_mixer.slot.yellow", "Teinture jaune");
        add("opencubes.gui.paint_mixer.slot.black", "Teinture noire");
        add("opencubes.gui.paint_mixer.slot.output", "Pot de peinture mélangé");
        add("opencubes.gui.paint_mixer.slot.output_short", "Pot");
        add("opencubes.misc.village_highlighter.status",
                "Village : %s villageois, %s lits, signal %s");
        add("opencubes.misc.paint_amount", "Peinture : %s");
        add("opencubes.misc.tank_contents", "%s : %s mB");
        add("opencubes.misc.change_box_size", "Taille : (%s, %s, %s) → (%s, %s, %s)");
        add("opencubes.misc.change_mode", "Forme : %s");
        add("opencubes.misc.total_blocks", "Total de blocs : %s");

        add(GuideShape.SPHERE.translationKey(), "Sphère");
        add(GuideShape.CYLINDER.translationKey(), "Cylindre");
        add(GuideShape.CUBOID.translationKey(), "Cuboïde");
        add(GuideShape.FULL_CUBOID.translationKey(), "Cuboïde plein");
        add(GuideShape.DOME.translationKey(), "Dôme");
        add(GuideShape.TRIANGLE.translationKey(), "Triangle");
        add(GuideShape.PENTAGON.translationKey(), "Pentagone");
        add(GuideShape.HEXAGON.translationKey(), "Hexagone");
        add(GuideShape.OCTAGON.translationKey(), "Octogone");
        add(GuideShape.AXES.translationKey(), "Axes");
        add(GuideShape.PLANES.translationKey(), "Plans");

        stencil(StencilPattern.CREEPER_FACE, "visage de creeper");
        stencil(StencilPattern.BORDER, "bordure");
        stencil(StencilPattern.STRIPES, "rayures");
        stencil(StencilPattern.CORNER, "coin");
        stencil(StencilPattern.CORNER2, "coin plein");
        stencil(StencilPattern.CORNER3, "double coin");
        stencil(StencilPattern.HOLE, "trou");
        stencil(StencilPattern.SPIRAL, "spirale");
        stencil(StencilPattern.THICKSTRIPES, "grosses rayures");
        stencil(StencilPattern.SPLAT, "éclaboussures");
        stencil(StencilPattern.STORAGE, "coffre");
        stencil(StencilPattern.HEART, "cœur");
        stencil(StencilPattern.HEART2, "cœur plein");
        stencil(StencilPattern.MUSIC, "note de musique");
        stencil(StencilPattern.BALLOON, "ballon");

        add("fluid_type.opencubes.xp_juice", "XP liquide");
        add("container.opencubes.dev_null", "/dev/null");
        add("container.opencubes.luggage", "Bagage");
        add("opencubes.misc.oh_no_ground", "Cet endroit n'est pas assez sûr pour dormir.");
        add("opencubes.misc.no_nearby_structures", "Aucune structure proche trouvée.");
        add("opencubes.misc.locked_on_nearest_structure", "Verrouillé sur %s");
        add("opencubes.misc.pedometer.tracking_started", "Suivi du podomètre démarré.");
        add("opencubes.misc.pedometer.tracking_reset", "Suivi du podomètre réinitialisé.");
        add("opencubes.misc.pedometer.start_point", "Départ : %s");
        add("opencubes.misc.pedometer.speed", "Vitesse : %s");
        add("opencubes.misc.pedometer.avg_speed", "Vitesse moyenne : %s");
        add("opencubes.misc.pedometer.total_distance", "Distance totale : %s");
        add("opencubes.misc.pedometer.straight_line_distance", "Distance à vol d'oiseau : %s");
        add("opencubes.misc.pedometer.straight_line_speed", "Vitesse à vol d'oiseau : %s");
        add("opencubes.misc.pedometer.last_check_speed", "Vitesse depuis le dernier contrôle : %s");
        add("opencubes.misc.pedometer.last_check_distance", "Distance depuis le dernier contrôle : %s");
        add("opencubes.misc.pedometer.last_check_time", "Temps depuis le dernier contrôle : %s ticks");
        add("opencubes.misc.pedometer.total_time", "Temps total : %s ticks");
        add("subtitles.opencubes.item.slimalyzer.ping", "Le slimalyseur bip");
        add("subtitles.opencubes.item.pedometer.use", "Le podomètre bip");
        add("opencubes.misc.get_witched", "Vous avez été ensorcelé !");

        add("container.opencubes.big_button", "Gros bouton");
        add("container.opencubes.xp_bottler", "Embouteilleur d'XP");
        add("container.opencubes.xp_bottler.fluid", "XP liquide : %s / %s mB");
        add("container.opencubes.block_placer", "Placeur de blocs");
        add("container.opencubes.item_dropper", "Lanceur avancé");
        add("container.opencubes.item_dropper.speed", "Vitesse : %s");
        add("container.opencubes.item_dropper.redstone", "RS ×");
        add("container.opencubes.item_dropper.redstone_on", "Mis à l'échelle par le signal");
        add("container.opencubes.vacuum_hopper", "Entonnoir à vide");
        add("container.opencubes.vacuum_hopper.items", "Objets →");
        add("container.opencubes.vacuum_hopper.xp", "XP →");
        add("container.opencubes.vacuum_hopper.off", "OFF");
        add("container.opencubes.side_config.input", "Entrée ←");
        add("container.opencubes.side_config.output", "Sortie →");
        add("container.opencubes.side_config.xp", "XP ←");
        add("container.opencubes.side_config.auto_pull", "Tirage auto");
        add("container.opencubes.side_config.auto_push", "Poussée auto");
        add("container.opencubes.side_config.auto_xp", "XP auto");
        add("container.opencubes.auto_anvil", "Enclume auto");
        add("container.opencubes.auto_enchanting_table", "Table d'enchantement auto");
        add("container.opencubes.auto_enchanting_table.level", "Emplacement N%s");
        add("container.opencubes.auto_enchanting_table.power", "Plafond %s / étagères %s");

        // Panneau de configuration des faces et écrans des machines associés.
        add("container.opencubes.side_config.toggle", "Configurer les faces");
        add("container.opencubes.side_config.title", "Config. des faces");
        add("container.opencubes.side_config.on", "ON");
        add("container.opencubes.side_config.off", "OFF");
        add("container.opencubes.side_config.face_tooltip", "%s : %s");
        add("container.opencubes.side_config.face.down", "Dessous");
        add("container.opencubes.side_config.face.up", "Dessus");
        add("container.opencubes.side_config.face.north", "Nord");
        add("container.opencubes.side_config.face.south", "Sud");
        add("container.opencubes.side_config.face.west", "Ouest");
        add("container.opencubes.side_config.face.east", "Est");
        add("container.opencubes.side_config.row.item_in", "Entrée d'objets");
        add("container.opencubes.side_config.row.item_out", "Sortie d'objets");
        add("container.opencubes.side_config.row.xp_in", "Entrée d'XP liquide");
        add("container.opencubes.side_config.row.xp_out", "Sortie d'XP liquide");
        add("container.opencubes.side_config.auto_pull.tip",
                "Aspire les objets des blocs voisins sur les faces d'entrée.");
        add("container.opencubes.side_config.auto_push.tip",
                "Envoie les objets terminés vers les blocs voisins sur les faces de sortie.");
        add("container.opencubes.side_config.auto_xp.tip",
                "Échange l'XP liquide avec les blocs voisins sur les faces XP.");
        add("container.opencubes.vacuum_hopper.suction", "Aspiration : %s");
        add("container.opencubes.vacuum_hopper.buffer", "XP %s%%");
        add("container.opencubes.vacuum_hopper.toggle_hint",
                "Clic accroupi à main nue sur le bloc pour activer ou couper l'aspiration.");
        add("container.opencubes.item_dropper.speed_label", "Vitesse");
        add("container.opencubes.item_dropper.speed_tip",
                "Vitesse de lancement des objets. Maintenez Maj pour ±10.");
        add("container.opencubes.item_dropper.redstone_label", "Redstone");
        add("container.opencubes.item_dropper.redstone_short", "RS");
        add("container.opencubes.item_dropper.redstone_tip",
                "Ajuste la vitesse de lancement selon la force du signal de redstone.");
        add("container.opencubes.auto_enchanting_table.slot", "N%s");
        add("container.opencubes.auto_enchanting_table.cap", "Plaf. %s");
        add("container.opencubes.auto_enchanting_table.shelves", "Étagères %s");
        add("container.opencubes.auto_enchanting_table.next", "Suivant");
        add("container.opencubes.auto_enchanting_table.next.tip",
                "Passe à l'une des trois offres d'enchantement.");
        add("container.opencubes.auto_enchanting_table.cap.tip",
                "Limite le nombre d'étagères utilisables. Maintenez Maj pour ±10.");
        add("container.opencubes.side_config.info", "Infos machine");
        add("container.opencubes.auto_anvil.info",
                "Combine outils et modificateurs avec l'XP liquide du réservoir interne. Configurez les faces objets et XP.");
        add("container.opencubes.auto_enchanting_table.info",
                "Enchante les objets avec de l'XP liquide. Les bibliothèques proches augmentent le plafond ; choisissez un emplacement et une limite.");
        add("container.opencubes.xp_bottler.info",
                "Remplit des fioles avec de l'XP liquide. Aspire et pousse selon les faces configurées.");
        add("container.opencubes.vacuum_hopper.info",
                "Aspire objets et orbes d'XP proches, puis les pousse par les faces configurées. Clic accroupi à main nue pour couper l'aspiration.");
        add("container.opencubes.building_guide.info",
                "Projette une forme fantôme pour bâtir. Ajustez la taille par axe, tournez et choisissez la couleur des marqueurs.");
        add("container.opencubes.enhanced_building_guide.info",
                "Comme le guide de construction. Clic droit avec un bloc sous alimentation pour placer une cellule ; en creatif avec de l'obsidienne au-dessus, remplit la forme. Les materiaux sont pris dans les inventaires adjacents.");
        add("container.opencubes.item_dropper.info",
                "Éjecte des objets sur une impulsion redstone. Utilisez +/- pour la vitesse ; Maj pour de plus grands pas.");
        add("container.opencubes.block_placer.info",
                "Place un bloc de son inventaire 3x3 dans l'espace face à lui sur une impulsion redstone.");
        add("container.opencubes.sprinkler.info",
                "Tire de l'eau en dessous, hydrate les terres agricoles et accélère les cultures. La poudre d'os dans la grille booste la croissance.");
        add("container.opencubes.paint_mixer.info",
                "Mélange du lait ou un pot de peinture avec des teintures cyan, magenta, jaune et noire pour remplir un pot de la couleur choisie.");
        add("container.opencubes.drawing_table.info",
                "Transforme un pochoir non préparé en pochoir de peinture ou en glyphe mural. Choisissez-le dans le panneau de droite, puis Couper.");
        add("container.opencubes.height_map_projector.info",
                "Projette une carte d'altitude en hologramme au-dessus du bloc. Les flèches font tourner l'affichage.");
        add("container.opencubes.drawing_table.cut", "Couper");
        add("container.opencubes.drawing_table.cut.tip",
                "Découpe le pochoir non préparé de gauche selon la sélection du panneau de droite.");
        add("container.opencubes.drawing_table.selected", "Sélection :");
        add("container.opencubes.drawing_table.need_input", "Mettez un pochoir à gauche");
        add("container.opencubes.drawing_table.output_full", "Retirez d'abord le résultat");
        add("container.opencubes.drawing_table.tab.stencils", "Pochoirs");
        add("container.opencubes.drawing_table.tab.stencils.tip",
                "Masques de peinture : posez-en un sur un bloc, puis peignez par-dessus au pinceau.");
        add("container.opencubes.drawing_table.tab.glyphs", "Glyphes");
        add("container.opencubes.drawing_table.tab.glyphs.tip",
                "Lettres et signes à coller aux murs, pour écrire des noms et des étiquettes.");
        add("container.opencubes.drawing_table.toggle", "Afficher ou masquer la liste des motifs");
        add("opencubes.misc.glyph_place_tip",
                "Clic droit sur un mur pour le coller là où vous visez ; frappez-le pour le reprendre.");
        add("opencubes.misc.stencil_place_tip",
                "Clic droit sur une face pour le poser, encore pour le tourner, accroupi pour le reprendre.");
        add("opencubes.misc.stencil_brush_tip",
                "Peignez par-dessus au pinceau : seuls les trous prennent la couleur.");

        add("subtitles.opencubes.block.elevator.activate", "L'ascenseur siffle");
        add("subtitles.opencubes.block.bear_trap.open", "Le piège à ours s'ouvre");
        add("subtitles.opencubes.block.bear_trap.close", "Le piège à ours claque");
        add("subtitles.opencubes.block.bottler.done", "L'embouteilleur termine");
        add("subtitles.opencubes.block.grave.rob", "La tombe est pillée");
        add("opencubes.misc.grave_msg", "%s le jour %s");
        add("opencubes.misc.grave_of", "Ci-gît %s");
        add("commands.opencubes.inventory.stored", "Inventaire de %s enregistré sous %s");
        add("commands.opencubes.inventory.restored", "Inventaire de %s restauré depuis %s");
        add("commands.opencubes.inventory.missing", "Aucune sauvegarde d'inventaire nommée %s");

        add("block.opencubes.xp_juice", "XP liquide");

        // Écran du guide de construction, qui remplace les anciens clics sur les faces.
        add("container.opencubes.building_guide.shape", "Forme");
        add("container.opencubes.building_guide.size", "Taille");
        add("container.opencubes.building_guide.colour", "Couleur des marqueurs");
        add("container.opencubes.building_guide.mirror", "Copier cette taille sur le côté opposé");
        add("container.opencubes.building_guide.rotate_ccw", "Pivoter dans le sens antihoraire");
        add("container.opencubes.building_guide.rotate_cw", "Pivoter dans le sens horaire");
        add("container.opencubes.building_guide.facing", "Orientation : %s");
        add("container.opencubes.building_guide.blocks", "Marqueurs : %s");
        add("container.opencubes.direction.down", "Bas");
        add("container.opencubes.direction.up", "Haut");
        add("container.opencubes.direction.north", "Nord");
        add("container.opencubes.direction.south", "Sud");
        add("container.opencubes.direction.west", "Ouest");
        add("container.opencubes.direction.east", "Est");

        add("opencubes.misc.vacuum_on", "Aspiration activée");
        add("opencubes.misc.vacuum_off", "Aspiration désactivée");
    }

    private static String materialFr(BigButtonMaterial material) {
        return switch (material.id()) {
            case "stone" -> "en pierre";
            case "oak" -> "en chêne";
            case "spruce" -> "en sapin";
            case "birch" -> "en bouleau";
            case "jungle" -> "en jungle";
            case "acacia" -> "en acacia";
            case "dark_oak" -> "en chêne noir";
            case "mangrove" -> "en palétuvier";
            case "cherry" -> "en cerisier";
            case "bamboo" -> "en bambou";
            case "crimson" -> "cramoisi";
            case "warped" -> "biscornu";
            case "polished_blackstone" -> "en pierre noire polie";
            default -> titleCase(material.id()).toLowerCase();
        };
    }

    private void stencil(StencilPattern pattern, String name) {
        add("item.opencubes.stencil." + pattern.id(), "Pochoir " + name);
    }

    private static String titleCase(String id) {
        StringBuilder builder = new StringBuilder(id.length());
        boolean capitalise = true;
        for (char c : id.toCharArray()) {
            if (c == '_' || c == ' ') {
                builder.append(' ');
                capitalise = true;
            } else {
                builder.append(capitalise ? Character.toUpperCase(c) : c);
                capitalise = false;
            }
        }
        return builder.toString();
    }
}
