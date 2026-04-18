package com.elfmcys.yesstevemodel.client.compat;

import net.minecraftforge.fml.common.Loader;

public final class Mods {
    static final String AQUA_ACROBATICS = "aquaacrobatics";
    static boolean AA_INSTALLED = false;
    static final String OCEANIC_EXPANSE = "oe";
    static boolean OE_INSTALLED = false;
    static final String TRIDENT_MOD = "trident";
    static boolean TM_INSTALLED = false;
    static final String FMC = "futuremc";
    static boolean FMC_INSTALLED = false;
    static final String J_CROSSBOW = "crossbow";
    static boolean JCROSSBOW_INSTALLED = false;
    static final String S_CROSSBOWS = "crossbows";
    static boolean SCROSSBOWS_INSTALLED = false;
    static final String DEEPER_DEPTHS = "deeperdepths";
    static boolean DD_INSTALLED = false;
    static final String MEK_MIXIN = "mekmixinhelp";
    static boolean MEK_MIXIN_INSTALLED = false;
    static final String UNIVERSAL_TWEAKS = "universaltweaks";
    static boolean UT_INSTALLED = false;
    static final String CARRY_ON = "carryon";
    static boolean CARRY_ON_INSTALLED = false;
    static final String CLEANROOM = "cleanroom";
    static boolean CRL_INSTALLED = false;
    static final String BAUBLES = "baubles";
    static boolean BAUBLES_INSTALLED = false;

    public static void init() {
        AA_INSTALLED = Loader.isModLoaded(AQUA_ACROBATICS);
        OE_INSTALLED = Loader.isModLoaded(OCEANIC_EXPANSE);
        TM_INSTALLED = Loader.isModLoaded(TRIDENT_MOD);
        FMC_INSTALLED = Loader.isModLoaded(FMC);
        JCROSSBOW_INSTALLED = Loader.isModLoaded(J_CROSSBOW);
        SCROSSBOWS_INSTALLED = Loader.isModLoaded(S_CROSSBOWS);
        DD_INSTALLED = Loader.isModLoaded(DEEPER_DEPTHS);
        MEK_MIXIN_INSTALLED = Loader.isModLoaded(MEK_MIXIN);
        UT_INSTALLED = Loader.isModLoaded(UNIVERSAL_TWEAKS);
        CARRY_ON_INSTALLED = Loader.isModLoaded(CARRY_ON);
        CRL_INSTALLED = Loader.isModLoaded(CLEANROOM);
        BAUBLES_INSTALLED = Loader.isModLoaded(BAUBLES);

        CarryOnCompat.init();
        RenderArmCompat.init();
    }
}
