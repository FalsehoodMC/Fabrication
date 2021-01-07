; Comments are indicated with a semicolon.
; When's the last time *you* wrote an INI file?

; All toggles may be set to "false", "true", or "unset". "unset" adopts the
; default value, even if the defaults change between versions or due to other
; reasons.

; You can upgrade your config to the latest default config with all of its
; comments without losing your settings by renaming this file to
; "fabrication.ini.old". You will lose any new comments or formatting, but your
; values will be carried over.

; << FEATURE: general >>
[general]
	; << FEATURE: general.runtime_checks >>
	runtime_checks=true
	
	; << FEATURE: general.reduced_motion >>
	reduced_motion=false
	
	; << FEATURE: general.profile >>
	profile=light

; << FEATURE: fixes >>
[fixes]
	; << FEATURE: fixes.sync_attacker_yaw >>
	sync_attacker_yaw=unset
	
	; << FEATURE: fixes.furnace_minecart_pushing >>
	furnace_minecart_pushing=unset
	
	; << FEATURE: fixes.use_player_list_name_in_tag >>
	use_player_list_name_in_tag=unset
	
	; << FEATURE: fixes.better_pause_freezing >>
	better_pause_freezing=unset
	
	; << FEATURE: fixes.inanimates_can_be_invisible >>
	inanimates_can_be_invisible=unset
	
	; << FEATURE: fixes.omniscent_player >>
	omniscent_player=unset
	
	; << FEATURE: fixes.uncap_menu_fps >>
	uncap_menu_fps=unset
	
	; << FEATURE: fixes.adventure_tags_in_survival >>
	adventure_tags_in_survival=unset
	
	; << FEATURE: fixes.stable_cacti >>
	stable_cacti=unset
	
	; << FEATURE: fixes.boundless_levels >>
	boundless_levels=unset
	
	; << FEATURE: fixes.ghast_charging >>
	ghast_charging=unset
	
	; << FEATURE: fixes.no_night_vision_flash >>
	no_night_vision_flash=unset

; << FEATURE: utility >>
[utility]
	; << FEATURE: utility.mods_command >>
	mods_command=unset
	
	; << FEATURE: utility.taggable_players >>
	taggable_players=unset
	
	; << FEATURE: utility.legacy_command_syntax >>
	legacy_command_syntax=unset
	
	; << FEATURE: utility.books_show_enchants >>
	books_show_enchants=unset
	
	; << FEATURE: utility.tools_show_important_enchant >>
	tools_show_important_enchant=unset
	
	; << FEATURE: utility.despawning_items_blink >>
	despawning_items_blink=unset
	
	; << FEATURE: utility.canhit >>
	canhit=unset
	
	; << FEATURE: utility.item_despawn >>
	item_despawn=unset
	
	; << FEATURE: utility.i_and_more >>
	i_and_more=unset

; << FEATURE: tweaks >>
[tweaks]
	; << FEATURE: tweaks.creepers_explode_when_on_fire >>
	creepers_explode_when_on_fire=unset
	
	; << FEATURE: tweaks.tridents_in_void_return >>
	tridents_in_void_return=unset
	
	; << FEATURE: tweaks.less_annoying_fire >>
	less_annoying_fire=unset
	
	; << FEATURE: tweaks.less_restrictive_note_blocks >>
	less_restrictive_note_blocks=unset
	
	; << FEATURE: tweaks.cactus_walk_doesnt_hurt_with_boots >>
	cactus_walk_doesnt_hurt_with_boots=unset
	
	; << FEATURE: tweaks.cactus_brush_doesnt_hurt_with_chest >>
	cactus_brush_doesnt_hurt_with_chest=unset
	
	; << FEATURE: tweaks.bush_walk_doesnt_hurt_with_armor >>
	bush_walk_doesnt_hurt_with_armor=unset
	
	; << FEATURE: tweaks.shulker_bullets_despawn_on_death >>
	shulker_bullets_despawn_on_death=unset
	
	; << FEATURE: tweaks.arrows_work_in_water >>
	arrows_work_in_water=unset
	
	; << FEATURE: tweaks.reverse_note_block_tuning >>
	reverse_note_block_tuning=unset
	
	; << FEATURE: tweaks.campfires_place_unlit >>
	campfires_place_unlit=unset
	
	; << FEATURE: tweaks.rainbow_experience >>
	rainbow_experience=unset
	
	; << FEATURE: tweaks.long_levelup_sound_at_30 >>
	long_levelup_sound_at_30=unset
	
	; << FEATURE: tweaks.flammable_cobwebs >>
	flammable_cobwebs=unset
	
	; << FEATURE: tweaks.normal_fog_with_night_vision >>
	normal_fog_with_night_vision=unset
	
	; << FEATURE: tweaks.play_note_blocks_in_creative >>
	play_note_blocks_in_creative=unset
	
	; << FEATURE: tweaks.ghost_chest_woo_woo >>
	ghost_chest_woo_woo=unset

; << FEATURE: minor_mechanics >>
[minor_mechanics]
	; << FEATURE: minor_mechanics.feather_falling_five >>
	feather_falling_five=unset
		; << FEATURE: minor_mechanics.feather_falling_five_damages_boots >>
		feather_falling_five_damages_boots=unset
	
	; << FEATURE: minor_mechanics.furnace_minecart_any_fuel >>
	furnace_minecart_any_fuel=unset
	
	; << FEATURE: minor_mechanics.infibows >>
	infibows=unset
	
	; << FEATURE: minor_mechanics.note_blocks_play_on_landing >>
	note_blocks_play_on_landing=unset
	
	; << FEATURE: minor_mechanics.fire_protection_on_any_item >>
	fire_protection_on_any_item=unset
	
	; << FEATURE: minor_mechanics.observers_see_entities >>
	observers_see_entities=unset
		; << FEATURE: minor_mechanics.observers_see_entities_living_only >>
		observers_see_entities_living_only=unset
	
	; << FEATURE: minor_mechanics.exact_note_block_tuning >>
	exact_note_block_tuning=unset
	
	; << FEATURE: minor_mechanics.note_block_notes >>
	note_block_notes=unset
	
	; << FEATURE: minor_mechanics.spiders_cant_climb_glazed_terracotta >>
	spiders_cant_climb_glazed_terracotta=unset
	
	; << FEATURE: minor_mechanics.spiders_cant_climb_while_wet >>
	spiders_cant_climb_while_wet=unset
	
	; << FEATURE: minor_mechanics.water_fills_on_break >>
	water_fills_on_break=unset
	
	; << FEATURE: minor_mechanics.invisibility_splash_on_inanimates >>
	invisibility_splash_on_inanimates=unset
	
	; << FEATURE: minor_mechanics.fire_aspect_is_flint_and_steel >>
	fire_aspect_is_flint_and_steel=unset
	
	; << FEATURE: minor_mechanics.mechanism_muffling >>
	mechanism_muffling=unset
	
; << FEATURE: mechanics >>
[mechanics]
	; << FEATURE: mechanics.enhanced_moistness >>
	enhanced_moistness=unset
	
	; << FEATURE: mechanics.slowfall_splash_on_inanimates >>
	slowfall_splash_on_inanimates=unset
	
	; << FEATURE: mechanics.broken_tools_drop_components >>
	broken_tools_drop_components=unset
	
	; << FEATURE: mechanics.obsidian_tears >>
	obsidian_tears=unset
	
	; << FEATURE: mechanics.grindstone_disenchanting >>
	grindstone_disenchanting=unset
	
	; << FEATURE: mechanics.anvil_repair >>
	anvil_repair=unset
	
; << FEATURE: balance >>
[balance]
	; << FEATURE: balance.faster_obsidian >>
	faster_obsidian=unset
	
	; << FEATURE: balance.disable_prior_work_penalty >>
	disable_prior_work_penalty=unset
	
	; << FEATURE: balance.soul_speed_doesnt_damage_boots >>
	soul_speed_doesnt_damage_boots=unset
	
	; << FEATURE: balance.infinity_mending >>
	infinity_mending=unset
	
	; << FEATURE: balance.hyperspeed_furnace_minecart >>
	hyperspeed_furnace_minecart=unset
	
	; << FEATURE: balance.tridents_accept_power >>
	tridents_accept_power=unset
	
	; << FEATURE: balance.tridents_accept_sharpness >>
	tridents_accept_sharpness=unset
	
	; << FEATURE: balance.bedrock_impaling >>
	bedrock_impaling=unset
	
	; << FEATURE: balance.environmentally_friendly_creepers >>
	environmentally_friendly_creepers=unset
	
	; << FEATURE: balance.anvil_damage_only_on_fall >>
	anvil_damage_only_on_fall=unset
	
	; << FEATURE: balance.drop_more_exp_on_death >>
	drop_more_exp_on_death=unset
	
	; << FEATURE: balance.infinity_crossbows >>
	infinity_crossbows=unset
	
	; << FEATURE: balance.anvil_rename_always_costs_one >>
	anvil_rename_always_costs_one=unset
	
; << FEATURE: weird_tweaks >>
[weird_tweaks]
	; << FEATURE: weird_tweaks.endermen_dont_squeal >>
	endermen_dont_squeal=unset
	
	; << FEATURE: weird_tweaks.disable_equip_sound >>
	disable_equip_sound=unset
	
	; << FEATURE: weird_tweaks.repelling_void >>
	repelling_void=unset
	
	; << FEATURE: weird_tweaks.drop_exp_with_keep_inventory >>
	drop_exp_with_keep_inventory=unset
	
	; << FEATURE: weird_tweaks.gold_tools_useful_in_nether >>
	gold_tools_useful_in_nether=unset
	
	; << FEATURE: weird_tweaks.photoallergic_creepers >>
	photoallergic_creepers=unset
	
	; << FEATURE: weird_tweaks.photoresistant_mobs >>
	photoresistant_mobs=unset

; << FEATURE: pedantry >>
[pedantry]
	; << FEATURE: pedantry.tnt_is_dynamite >>
	tnt_is_dynamite=unset
	
	; << FEATURE: pedantry.oak_is_apple >>
	oak_is_apple=unset

; << FEATURE: woina >>
[woina]
	; << FEATURE: woina.block_logo >>
	block_logo=unset
	
	; << FEATURE: woina.old_lava >>
	old_lava=unset
	
	; << FEATURE: woina.classic_block_drops >>
	classic_block_drops=unset
	
	; << FEATURE: woina.blinking_drops >>
	blinking_drops=unset
	
	; << FEATURE: woina.janky_arm >>
	janky_arm=unset
	
	; << FEATURE: woina.flat_items >>
	flat_items=unset
	
	; << FEATURE: woina.billboard_drops >>
	billboard_drops=unset
	
	; << FEATURE: woina.oof >>
	oof=unset

; << FEATURE: situational >>
[situational]
	; << FEATURE: situational.all_damage_is_fatal >>
	all_damage_is_fatal=unset
	
	; << FEATURE: situational.weapons_accept_silk >>
	weapons_accept_silk=unset

; << FEATURE: experiments >>
[experiments]
	; << FEATURE: experiments.packed_atlases >>
	packed_atlases=unset

; Notices: (Do not edit anything past this line; it will be overwritten)
; - No notices. You're in the clear!
