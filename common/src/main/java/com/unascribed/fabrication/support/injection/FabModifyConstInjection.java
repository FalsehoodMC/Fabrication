package com.unascribed.fabrication.support.injection;

import com.unascribed.fabrication.FabLog;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.injection.invoke.ModifyConstantInjector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.struct.Target;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

public class FabModifyConstInjection extends ModifyConstantInjector {
	public FabModifyConstInjection(InjectionInfo info) {
		super(info);
	}

	@Override
	public void addTargetNode(Target target, List<InjectionNodes.InjectionNode> myNodes, AbstractInsnNode insn, Set<InjectionPoint> nominators) {
		InjectionNodes.InjectionNode node = target.getInjectionNode(insn);
		//Meta.KEY = "redirector"
		if (node != null) {
			Object other = node.getDecoration("redirector");
			node.decorate("redirector", null);
			super.addTargetNode(target, myNodes, insn, nominators);
			node.decorate("redirector", other);
			return;
		}
		super.addTargetNode(target, myNodes, insn, nominators);
	}

	@Override
	public boolean preInject(InjectionNodes.InjectionNode node) {
		return true;
	}

	@Override
	public void inject(Target target, InjectionNodes.InjectionNode node) {
		try {
			Field orig = node.getClass().getDeclaredField("originalTarget");
			Field cur = node.getClass().getDeclaredField("currentTarget");
			orig.setAccessible(true);
			cur.setAccessible(true);
			cur.set(node, orig.get(node));
		} catch (Exception e) {
			FabLog.error("FabMixinInjector failed to reflect ModifyConst", e);
		}
		super.inject(target, node);
	}

}
