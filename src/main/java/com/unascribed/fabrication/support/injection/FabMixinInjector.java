package com.unascribed.fabrication.support.injection;

import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.struct.Target;

import java.util.List;

public class FabMixinInjector extends Injector {

	Injector realInjector;

	public FabMixinInjector(InjectionInfo info, String annotationType, Injector realInjector) {
		super(info, annotationType);
		this.realInjector = realInjector;
	}

	@Override
	public void inject(Target target, InjectionNodes.InjectionNode node) {
		realInjector.inject(target, List.of(node));
	}

}
