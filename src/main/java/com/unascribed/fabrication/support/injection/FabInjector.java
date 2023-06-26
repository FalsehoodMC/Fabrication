package com.unascribed.fabrication.support.injection;

import com.google.common.base.Joiner;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.SimpleVerifier;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.refmap.IReferenceMapper;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.util.asm.MethodNodeEx;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


public class FabInjector {
	public static class ToInject{
		public final List<String> potentiallyRedirected = new ArrayList<>();
		public Map<String, String> done = new HashMap<>();
		public Map<String, String> redirect_fixed = new HashMap<>();
		public List<String> method;
		public List<String> target;
		public String owner;
		public String name;
		public String desc;
		public int access;
		public String annotation;
		public String mixin;

		public ToInject(List<String> method, List<String> target, String owner, String name, String desc, int opcode, String annotation, String mixin){
			this.method = method;
			this.target = target;
			this.owner = owner;
			this.name = name;
			this.desc = desc;
			this.access = opcode;
			this.annotation = annotation;
			this.mixin = mixin;
		}
	}

	public static Function<ClassInfo, Set<IMixinInfo>> getMixinInfoFromClassInfo = i -> Collections.emptySet();
	static {
		try {
			Field f_mixins = ClassInfo.class.getDeclaredField("mixins");
			f_mixins.setAccessible(true);
			getMixinInfoFromClassInfo = info -> {
				try {
					return (Set<IMixinInfo>) f_mixins.get(info);
				} catch (Exception e) {
					FabLog.error("FabInjector failed to reflect mixin: "+ info.getClassName(), e);
					return Collections.emptySet();
				}
			};
		} catch (Throwable e) {
			FabLog.error("FabInjector failed to reflect mixin fields, redirect fixer has been disabled", e);
		}
	}
	public static Function<IMixinInfo, IReferenceMapper> getMixinReferenceMapper = (i) -> null;
	static {
		try {
			Class<?> classMixinConfig = Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig");
			Method getMapper = classMixinConfig.getMethod("getReferenceMapper");
			getMapper.setAccessible(true);
			getMixinReferenceMapper = (i) -> {
				try {
					IMixinConfig conf = i.getConfig();
					if (classMixinConfig.isInstance(conf)) {
						return (IReferenceMapper) getMapper.invoke(conf);
					}
				} catch (Exception e) {
					FabLog.error("FabInjector failed to reflect mixin: "+ i.getClassName(), e);
				}
				return null;
			};
		} catch (Throwable e) {
			FabLog.error("FabInjector failed to reflect mixin remapper", e);
		}
	}

	public static final Set<String> dejavu = new HashSet<>();

	public static class EntryMixinMerged {
		String name;
		String desc;
		String mixin;
		String target;
		EntryMixinMerged(String name, String desc, String mixin, String target) {
			this.name = name;
			this.desc = desc;
			this.mixin = mixin;
			this.target = target;
		}
	}

	public static Map<String, String> getMixinRedirects(String className) {
		ClassInfo ci = ClassInfo.fromCache(className);
		if (ci == null) return Collections.emptyMap();
		Map<String, String> discoveredRedirects = new HashMap<>();
		for (IMixinInfo inf : getMixinInfoFromClassInfo.apply(ci)) {
			ClassNode cn = inf.getClassNode(0);
			if (cn.methods == null) continue;
			for (MethodNode mth : cn.methods) {
				if (mth.visibleAnnotations == null) continue;
				for (AnnotationNode annotation : mth.visibleAnnotations) {
					if (!"Lorg/spongepowered/asm/mixin/injection/Redirect;".equals(annotation.desc)) continue;
					int at = annotation.values.indexOf("at");
					if (at != -1 && at < annotation.values.size()){
						Object atNode = annotation.values.get(at+1);
						if (atNode instanceof AnnotationNode) {
							AnnotationNode an = (AnnotationNode)atNode;
							int ani = an.values.indexOf("target");
							if (ani != -1 && ani < an.values.size()){
								Object target = an.values.get(ani+1);
								if (target instanceof String) {
									IReferenceMapper mapper = getMixinReferenceMapper.apply(inf);
									if (mapper != null) {
										String mapped = mapper.remap(inf.getClassName().replace('.', '/'), ((String) target));
										if (mapped != null) target = mapped;
									}
									discoveredRedirects.put(mth.name, (String) target);
								}
							}
						}
					}
				}
			}
		}
		return discoveredRedirects;
	}
	public static Map<String, String> transformRedirectsToOriginalNames(Map<String, String> redirects, ClassNode targetClass) {
		Map<String, String> ret = new HashMap<>();
		for (MethodNode mth : targetClass.methods) {
			if (!(mth instanceof MethodNodeEx)) continue;
			String val = redirects.get(((MethodNodeEx) mth).getOriginalName());
			if (val != null) ret.put(mth.name, val);
		}
		return ret;
	}
	public static void apply(ClassNode targetClass) {
		apply(targetClass, null);
	}
	public static void apply(ClassNode targetClass, List<ToInject> injectIn) {
		Map<String, String> existingRedirects = transformRedirectsToOriginalNames(getMixinRedirects(targetClass.name), targetClass);
		List<ToInject> injects = new ArrayList<>();
		List<EntryMixinMerged> redirects = new ArrayList<>();
		for (MethodNode methodNode : targetClass.methods) {
			if (!(methodNode instanceof MethodNodeEx)) continue;
			AnnotationNode inject = null;
			String mixin = null;
			for (AnnotationNode annotationNode : methodNode.visibleAnnotations) {
				if ((
						"Lcom/unascribed/fabrication/support/injection/ModifyReturn;".equals(annotationNode.desc)
							|| "Lcom/unascribed/fabrication/support/injection/Hijack;".equals(annotationNode.desc)
							|| "Lcom/unascribed/fabrication/support/injection/ModifyGetField;".equals(annotationNode.desc)
				) && dejavu.add(targetClass.name + methodNode.name + methodNode.desc)
				) {
					inject = annotationNode;
				} else if ("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;".equals(annotationNode.desc)) {
					mixin = (String) annotationNode.values.get(annotationNode.values.indexOf("mixin") + 1);
					if (existingRedirects.containsKey(methodNode.name)) {
						redirects.add(new EntryMixinMerged(methodNode.name, methodNode.desc, mixin, existingRedirects.get(methodNode.name)));
					}
				}
			}

			if (inject != null && mixin != null && injectIn == null) {
				final String mix = mixin;
				injects.add(new ToInject(
						((List<String>) inject.values.get(inject.values.indexOf("method") + 1)).stream().map(s -> FabRefMap.relativeMap(mix, s)).collect(Collectors.toList()),
						((List<String>) inject.values.get(inject.values.indexOf("target") + 1)).stream().map(FabRefMap::absoluteMap).collect(Collectors.toList()),
						targetClass.name,
						methodNode.name,
						methodNode.desc,
						methodNode.access | (targetClass.access & Opcodes.ACC_INTERFACE),
						inject.desc,
						mixin
				));
			}
		}
		apply(targetClass, injectIn != null ? injectIn : injects, redirects);
	}
	public static void apply(ClassNode targetClass, List<ToInject> injects, List<EntryMixinMerged> redirects){
		Map<String, String> redirectMap = new HashMap<>();
		for (ToInject toInject : injects) {
			if (toInject.annotation.equals("Lcom/unascribed/fabrication/support/injection/ModifyGetField;")) continue;
			for (EntryMixinMerged redirect : redirects) {
				String mapped = FabRefMap.absoluteMap(redirect.target);
				if (toInject.target.contains(mapped)) {
					String r = redirect.name + redirect.desc;
					toInject.potentiallyRedirected.add(r);
					redirectMap.put(r, mapped);
					FabLog.info("FabInjector found a Redirect from " + redirect.mixin + ";" + redirect.name + ";" + " which has been added to " + toInject.owner + ";" + toInject.name);
				}
			}
		}
		for (MethodNode methodNode : targetClass.methods) {
			for (ToInject toInject : injects) {
				for (String m : toInject.method) {
					if (!m.equals(methodNode.name + methodNode.desc)) continue;
					for (AbstractInsnNode insnNode : methodNode.instructions) {
						String insnOwner = null;
						String insnName = null;
						String insnDesc = null;
						if (insnNode instanceof MethodInsnNode) {
							MethodInsnNode insn = (MethodInsnNode) insnNode;
							insnOwner = insn.owner;
							insnName = insn.name;
							insnDesc = insn.desc;
						} else if (insnNode instanceof FieldInsnNode) {
							FieldInsnNode insn = (FieldInsnNode) insnNode;
							insnOwner = insn.owner;
							insnName = insn.name;
							insnDesc = ":" + insn.desc;
						}
						if (insnOwner != null && insnName != null && insnDesc != null) {
							for (String target : toInject.target) {
								String unchangedTarget = target;
								if (target.charAt(0) == 'L') target = target.substring(1);
								if (target.startsWith(insnOwner)) {
									char d = target.charAt(insnOwner.length());
									if ((d == '.' || d == ';') && target.substring(insnOwner.length() + 1).equals(insnName + insnDesc)) {
										if (performInjection(methodNode, insnNode, toInject, target, false, targetClass)) {
											toInject.done.put(m, unchangedTarget);
											String type = toInject.annotation.substring(toInject.annotation.lastIndexOf('/'), toInject.annotation.length() - 1);
											FabLog.debug("Completed " + type + " Injection : " + toInject.owner + ";" + m + "\t" + unchangedTarget);
										}
									}
								}
							}
							if (toInject.owner.equals(insnOwner)) {
								for (String target : toInject.potentiallyRedirected) {
									if (target.equals(insnName + insnDesc)) {
										if (performInjection(methodNode, insnNode, toInject, target, true, targetClass)) {
											toInject.redirect_fixed.put(m, redirectMap.get(target));
											String type = toInject.annotation.substring(toInject.annotation.lastIndexOf('/'), toInject.annotation.length() - 1);
											FabLog.debug("Completed " + type + " Injection over existing Redirect : " + toInject.owner + ";" + m + "\t" + target);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		for (ToInject ti: injects) for (String m : ti.method) for (String t : ti.target) {
			if (!t.equals(ti.done.get(m))) {
				if (t.equals(ti.redirect_fixed.get(m))) {
					FabLog.warn("FabInjector failed to find injection point for "+ti.owner+";"+m+"\t"+t+"\n may have been caused by another mods Redirect, assuming fixed");
				} else {
					FabLog.warn("FabInjector failed to find injection point for "+ti.owner+";"+m+"\t"+t+"\n located in "+ti.mixin);
					Set<String> keys = MixinConfigPlugin.getConfigKeysForDiscoveredClass(ti.mixin);
					if (!keys.isEmpty()) {
						FabLog.warn("! Force-disabling " + Joiner.on(", ").join(keys));
						for (String opt : keys) {
							FabConf.addFailure(opt);
						}
					}
				}
			}
		}
	}
	public static boolean performInjection(MethodNode methodNode, AbstractInsnNode insn, ToInject toInject, String target, boolean isRedirect, ClassNode classNode) {
		List<Runnable> undo = new ArrayList<>();
		boolean ret = performInjection(methodNode, insn, toInject, target, isRedirect, undo);
		Exception e = verifyMethodNode(classNode, methodNode);
		if (e == null) {
			return ret;
		}
		FabLog.error("Injection test failed, attempting recovery, this is likely a mod conflict, please report it\n"+e.getLocalizedMessage());
		for (Runnable u : undo) {
			if (u!=null)u.run();
		}
		return false;
	}
	public static Exception verifyMethodNode(ClassNode classNode, MethodNode method) {
		Type syperType = classNode.superName == null ? null : Type.getObjectType(classNode.superName);

		List<Type> interfaces = new ArrayList<>();
		for (String interfaceName : classNode.interfaces) {
			interfaces.add(Type.getObjectType(interfaceName));
		}
		SimpleVerifier verifier = new SimpleVerifier(Type.getObjectType(classNode.name), syperType, interfaces, (classNode.access & Opcodes.ACC_INTERFACE) != 0);
		Analyzer<BasicValue> analyzer = new Analyzer<>(verifier);
		try {
			analyzer.analyzeAndComputeMaxs(classNode.name, method);
		} catch (AnalyzerException e) {
			return e;
		}
		return null;
	}
	public static AbstractInsnNode addUndoInsn(AbstractInsnNode insnNode, InsnList list, List<Runnable> undo) {
		undo.add(()->list.remove(insnNode));
		return insnNode;
	}
	public static boolean performInjection(MethodNode methodNode, AbstractInsnNode insn, ToInject toInject, String target, boolean isRedirect, List<Runnable> undo) {
		boolean toInjectIsStatic = (toInject.access & Opcodes.ACC_STATIC) != 0;
		InsnList mod = new InsnList();
		List<Type> argTypes = new ArrayList<>();
		if (insn.getOpcode() != Opcodes.INVOKESTATIC && insn.getOpcode() != Opcodes.GETFIELD && insn.getOpcode() != Opcodes.GETSTATIC)
			argTypes.add(Type.VOID_TYPE);
		int brac = target.indexOf('(');
		Type targetType = Type.getMethodType(target.substring(brac == -1 ? target.lastIndexOf(':')+1 : brac));
		if (brac != -1)
			argTypes.addAll(Arrays.asList(targetType.getArgumentTypes()));
		Type toInjectType = Type.getMethodType(toInject.desc);
		Type[] toInjectArgTypes = toInjectType.getArgumentTypes();
		int countDesc = toInjectArgTypes.length;
		int max = methodNode.maxLocals;
		{
			int fmax = max;
			undo.add(() -> methodNode.maxLocals = fmax);
		}
		InsnList oldVars = new InsnList();
		InsnList newVars = new InsnList();
		InsnList instructions = methodNode.instructions;
		if ("Lcom/unascribed/fabrication/support/injection/ModifyGetField;".equals(toInject.annotation)) {
			if (!toInjectIsStatic) {
				mod.add(addUndoInsn(new VarInsnNode(getStoreOpcode(targetType.getReturnType().getSort()), max), instructions, undo));
				mod.add(addUndoInsn(new VarInsnNode(Opcodes.ALOAD, 0), instructions, undo));
				mod.add(addUndoInsn(new VarInsnNode(getLoadOpcode(targetType.getReturnType().getSort()), max++), instructions, undo));
			}
			if (--countDesc > 0) {
				String clazz = ((FieldInsnNode)insn).owner;
				int type = Type.getType(clazz.startsWith("L") ? clazz : "L"+clazz).getSort();
				instructions.insertBefore(insn, addUndoInsn(new VarInsnNode(getStoreOpcode(type), max), instructions, undo));
				instructions.insertBefore(insn, addUndoInsn(new VarInsnNode(getLoadOpcode(type), max), instructions, undo));
				mod.add(addUndoInsn(new VarInsnNode(getLoadOpcode(type), max++), instructions, undo));
				countDesc-=1;
			}
			methodNode.maxLocals = max;
			for (int c = toInjectIsStatic ? 0 : 1; c < countDesc; c++) {
				mod.add(addUndoInsn(new VarInsnNode(getLoadOpcode(toInjectArgTypes[toInjectArgTypes.length-countDesc+c].getSort()), c), instructions, undo));
			}

			mod.add(addUndoInsn(new MethodInsnNode(toInjectIsStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL, toInject.owner, toInject.name, toInject.desc, (toInject.access & Opcodes.ACC_INTERFACE) != 0), instructions, undo));
			instructions.insert(insn, mod);
			return true;
		}else if ("Lcom/unascribed/fabrication/support/injection/ModifyReturn;".equals(toInject.annotation)) {
			//TODO probably never. continue the variable trace after the first method to further reduce allocation
			if (--countDesc > 0) {
				AbstractInsnNode varTrace = insn.getPrevious();
				boolean isSeqVar = varTrace != null && isVariableLoader(varTrace.getOpcode());
				if (!isSeqVar) varTrace = null;
				for (int c = 0; c < argTypes.size(); c++) {
					if (isSeqVar){
						oldVars.insert(varTrace.clone(new HashMap<>()));
						AbstractInsnNode tmp = varTrace.getPrevious();
						if (tmp != null && isVariableLoader(tmp.getOpcode())) {
							varTrace = tmp;
						} else {
							isSeqVar = false;
						}
					} else {
						int sort = argTypes.get(argTypes.size()-1-c).getSort();
						newVars.insert(addUndoInsn(new VarInsnNode(getLoadOpcode(sort), max), instructions, undo));
						instructions.insertBefore(varTrace == null ? insn : varTrace, addUndoInsn(new VarInsnNode(getStoreOpcode(sort), max++), instructions, undo));
					}
				}
				methodNode.maxLocals=max;
				if (!toInjectIsStatic) {
					mod.add(addUndoInsn(new VarInsnNode(getStoreOpcode(targetType.getReturnType().getSort()), methodNode.maxLocals), instructions, undo));
					mod.add(addUndoInsn(new VarInsnNode(Opcodes.ALOAD, 0), instructions, undo));
					mod.add(addUndoInsn(new VarInsnNode(getLoadOpcode(targetType.getReturnType().getSort()), methodNode.maxLocals), instructions, undo));
					methodNode.maxLocals += 1;
				}
				if (isRedirect && !argTypes.isEmpty() && argTypes.get(0) == Type.VOID_TYPE) {
					if (newVars.size() > 0) {
						newVars.remove(newVars.getFirst());
					} else if (oldVars.size() > 0) {
						oldVars.remove(oldVars.getFirst());
					}
				}
				for (AbstractInsnNode a : newVars) {
					if (countDesc-->0) mod.add(addUndoInsn(a.clone(new HashMap<>()), instructions, undo));
				}
				for (AbstractInsnNode a : oldVars) {
					if (countDesc-->0) mod.add(addUndoInsn(a.clone(new HashMap<>()), instructions, undo));
				}
				instructions.insertBefore(varTrace == null ? insn : varTrace, newVars);
				for (int c = toInjectIsStatic ? 0 : 1; c < countDesc; c++) {
					mod.add(addUndoInsn(new VarInsnNode(getLoadOpcode(toInjectArgTypes[toInjectArgTypes.length-countDesc+c].getSort()), c), instructions, undo));
				}
			} else if (!toInjectIsStatic) {
				mod.add(addUndoInsn(new VarInsnNode(getStoreOpcode(targetType.getReturnType().getSort()), methodNode.maxLocals), instructions, undo));
				mod.add(addUndoInsn(new VarInsnNode(Opcodes.ALOAD, 0), instructions, undo));
				mod.add(addUndoInsn(new VarInsnNode(getLoadOpcode(targetType.getReturnType().getSort()), methodNode.maxLocals), instructions, undo));
				methodNode.maxLocals += 1;
			}
			mod.add(addUndoInsn(new MethodInsnNode(toInjectIsStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL, toInject.owner, toInject.name, toInject.desc, (toInject.access & Opcodes.ACC_INTERFACE) != 0), instructions, undo));
			instructions.insert(insn, mod);
			return true;
		} else if ("Lcom/unascribed/fabrication/support/injection/Hijack;".equals(toInject.annotation)) {
			for (int c = 0; c < argTypes.size(); c++) {
				instructions.insertBefore(insn, addUndoInsn(new VarInsnNode(getStoreOpcode(argTypes.get(argTypes.size() - 1 - c).getSort()), max++), instructions, undo));
			}
			LabelNode label = new LabelNode(new Label());
			LabelNode label2 = new LabelNode(new Label());
			boolean optionalReturn = toInjectType.getReturnType().getSort() != Type.BOOLEAN;
			mod.add(addUndoInsn(new MethodInsnNode(toInjectIsStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL, toInject.owner, toInject.name, toInject.desc, (toInject.access & Opcodes.ACC_INTERFACE) != 0), instructions, undo));
			if (optionalReturn) {
				mod.add(addUndoInsn(new VarInsnNode(Opcodes.ASTORE, max), instructions, undo));
				mod.add(addUndoInsn(new VarInsnNode(Opcodes.ALOAD, max), instructions, undo));
				mod.add(addUndoInsn(new JumpInsnNode(Opcodes.IFNULL, label), instructions, undo));
				mod.add(addUndoInsn(new VarInsnNode(Opcodes.ALOAD, max), instructions, undo));
				mod.add(addUndoInsn(new FieldInsnNode(Opcodes.GETFIELD, "com/unascribed/fabrication/support/injection/HijackReturn", "object", "Ljava/lang/Object;"), instructions, undo));
				castHijackReturnResult(targetType.getReturnType(), i->{
					mod.add(addUndoInsn(i, instructions, undo));
				});
				mod.add(addUndoInsn(new JumpInsnNode(Opcodes.GOTO, label2), instructions, undo));
				mod.add(addUndoInsn(label, instructions, undo));
				methodNode.maxLocals=max+1;
			} else {
				mod.add(addUndoInsn(new JumpInsnNode(Opcodes.IFNE, label), instructions, undo));
				methodNode.maxLocals=max;
			}
			if (!toInjectIsStatic) methodNode.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
			for (int i=0; i<argTypes.size();i++) {
				Type argType = argTypes.get(i);
				int opcode = getLoadOpcode(argType.getSort());
				mod.add(addUndoInsn(new VarInsnNode(opcode, --max), instructions, undo));
				if (!(i==0 && argTypes.get(0) == Type.VOID_TYPE && isRedirect) && countDesc-->0)
					instructions.insertBefore(insn, addUndoInsn(new VarInsnNode(opcode, max), instructions, undo));
			}
			for (int c = toInjectIsStatic ? 0 : 1; c < countDesc; c++) {
				instructions.insertBefore(insn, addUndoInsn(new VarInsnNode(getLoadOpcode(toInjectArgTypes[toInjectArgTypes.length-countDesc+c].getSort()), c), instructions, undo));
			}

			instructions.insertBefore(insn, mod);
			instructions.insert(insn, addUndoInsn(optionalReturn? label2 : label, instructions, undo));
			return true;
		}
		return false;
	}

	public static void castHijackReturnResult(Type desc, Consumer<AbstractInsnNode> insnConsumer) {
		switch (desc.getSort()) {
			case Type.BOOLEAN:
				insnConsumer.accept(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Boolean"));
				insnConsumer.accept(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false));
				break;
			case Type.BYTE:
				insnConsumer.accept(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Byte"));
				insnConsumer.accept(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false));
				break;
			case Type.CHAR:
				insnConsumer.accept(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Character"));
				insnConsumer.accept(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false));
				break;
			case Type.SHORT:
				insnConsumer.accept(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Short"));
				insnConsumer.accept(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false));
				break;
			case Type.INT:
				insnConsumer.accept(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Integer"));
				insnConsumer.accept(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
				break;
			case Type.LONG:
				insnConsumer.accept(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Long"));
				insnConsumer.accept(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false));
				break;
			case Type.FLOAT:
				insnConsumer.accept(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Float"));
				insnConsumer.accept(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false));
				break;
			case Type.DOUBLE:
				insnConsumer.accept(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Double"));
				insnConsumer.accept(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false));
				break;
			default:
				String descStr = desc.toString();
				insnConsumer.accept(new TypeInsnNode(Opcodes.CHECKCAST, descStr.substring(1, descStr.length() - 1)));
		}
	}

	public static int getStoreOpcode(int type){
		return getLoadOpcode(type) + 33;
	}

	public static int getLoadOpcode(int type){
		switch (type){
			case Type.BOOLEAN:
			case Type.BYTE:
			case Type.CHAR:
			case Type.SHORT:
			case Type.INT:
				return Opcodes.ILOAD;
			case Type.LONG:
				return Opcodes.LLOAD;
			case Type.FLOAT:
				return Opcodes.FLOAD;
			case Type.DOUBLE:
				return Opcodes.DLOAD;
			default:
				return Opcodes.ALOAD;
		}
	}

	public static boolean isVariableLoader(int opcode){
		switch (opcode){
			case Opcodes.ALOAD:
			case Opcodes.DLOAD:
			case Opcodes.FLOAD:
			case Opcodes.LLOAD:
			case Opcodes.ILOAD:
			case Opcodes.ACONST_NULL:
			case Opcodes.BIPUSH:
			case Opcodes.DCONST_0:
			case Opcodes.DCONST_1:
			case Opcodes.ICONST_0:
			case Opcodes.ICONST_1:
			case Opcodes.ICONST_2:
			case Opcodes.ICONST_3:
			case Opcodes.ICONST_4:
			case Opcodes.ICONST_5:
			case Opcodes.ICONST_M1:
			case Opcodes.LCONST_0:
			case Opcodes.LCONST_1:
			case Opcodes.LDC:
			case Opcodes.SIPUSH:
				return true;
			default:
				return false;
		}
	}
}
