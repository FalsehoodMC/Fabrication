package com.unascribed.fabrication.mixin.a_fixes.inanimates_can_be_invisible;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

@Mixin(EntityRenderDispatcher.class)
@EligibleIf(configAvailable="*.inanimates_can_be_invisible", envMatches=Env.CLIENT)
public abstract class MixinEntityRenderDispatcher {

	private final Map<Class<?>, Boolean> fabrication$renderersUseInvisibility = new HashMap<>();
	
	@Shadow
	public abstract <T extends Entity> EntityRenderer<? super T> getRenderer(T entity);
	
	@Inject(at=@At("HEAD"), method="shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z", cancellable=true)
	public void shouldRender(Entity e, Frustum f, double x, double y, double z, CallbackInfoReturnable<Boolean> ci) {
		if (!(e instanceof LivingEntity) && e.isInvisible()) {
			EntityRenderer<?> rend = getRenderer(e);
			Class<?> clazz = rend.getClass();
			if (!fabrication$renderersUseInvisibility.containsKey(clazz)) {
				// ...is this evil?
				// eh. who cares
				byte[] classBytes = Agnos.getClassBytes(clazz);
				if (classBytes != null) {
					try {
						ClassReader cr = new ClassReader(classBytes);
						ClassNode cn = new ClassNode();
						cr.accept(cn, 0);
						boolean usesIsInvisible = false;
						out: for (MethodNode mn : cn.methods) {
							for (AbstractInsnNode ain : mn.instructions) {
								if (ain instanceof MethodInsnNode) {
									MethodInsnNode min = (MethodInsnNode)ain;
									if (min.desc.equals("()Z") &&
											(min.name.equals("isInvisible") ||
											min.name.equals("method_5767") ||
											min.name.equals("func_82150_aj"))) {
										usesIsInvisible = true;
										break out;
									}
								}
							}
						}
						if (usesIsInvisible) {
							FabLog.info("Detected that "+clazz+" checks isInvisible; ignoring it for inanimates_can_be_invisible");
						}
						fabrication$renderersUseInvisibility.put(clazz, usesIsInvisible);
					} catch (Throwable t) {
						FabLog.warn("Can't determine if "+clazz+" checks isInvisible or not; assuming it doesn't", t);
						fabrication$renderersUseInvisibility.put(clazz, false);
					}
				} else {
					FabLog.warn("Can't determine if "+clazz+" checks isInvisible or not; assuming it doesn't (couldn't find class file)");
					fabrication$renderersUseInvisibility.put(clazz, false);
				}
			}
			if (!fabrication$renderersUseInvisibility.get(clazz)) {
				ci.setReturnValue(false);
			}
		}
	}
	
}
