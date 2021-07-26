### Todo

- src/main/java/com/unascribed/fabrication/client/SpriteLava.java
- src/main/java/com/unascribed/fabrication/client/SpriteLavaFlow.java
- src/main/java/com/unascribed/fabrication/client/FabricationConfigScreen.java
- src/main/java/com/unascribed/fabrication/mixin/_general/atlas_tracking/SpriteAccessor.java
- src/main/java/com/unascribed/fabrication/mixin/i_woina/block_logo/MixinTitleScreen.java
- build.gradle

### Broken
- atlas_viewer
- config_screen
- woina.old_lava
- woina.block_logo
- mechanics.grindstone_disenchanting
- mechanics.slowfall_splash_on_inanimates
- weird_tweaks.repelling_void

com.unascribed.fabrication.mixin.f_balance.anvil_damage_only_on_fall.MixinAnvilScreenHandler failed to apply! Force-disabling *.anvil_damage_only_on_fall


### Visually Broken
- woina.old_tooltip (has a darker background in trading screen)
- pedantry.oak_is_apple & tnt_is_dynamite
  - Shows up in resource pack menu don't know if intended or not


### Untested

- experiments.packed_atlases
  - dunno how to test since atlas_viewer is broken
- weird_tweaks.disable_equip_sound
  - i think it works?