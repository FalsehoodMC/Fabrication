package com.unascribed.fabrication.support.injection;

import com.unascribed.fabrication.FabLog;
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
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.util.asm.MethodNodeEx;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class FabInjector {
	public static class ToInject{
		public final List<String> potentiallyRedirected = new ArrayList<>();
		public Map<String, String> done = new HashMap<>();
		public List<String> method;
		public List<String> target;
		public String owner;
		public String name;
		public String desc;
		public int access;
		public String annotation;

		public ToInject(List<String> method, List<String> target, String owner, String name, String desc, int opcode, String annotation){
			this.method = method;
			this.target = target;
			this.owner = owner;
			this.name = name;
			this.desc = desc;
			this.access = opcode;
			this.annotation = annotation;
		}
	}
	public static final Set<String> dejavu = new HashSet<>();

	public static class EntryMixinMerged {
		String name;
		String desc;
		String mixin;
		Redirect redirect;
		EntryMixinMerged(String name, String desc, String mixin, Redirect redirect) {
			this.name = name;
			this.desc = desc;
			this.mixin = mixin;
			this.redirect = redirect;
		}
	}

	public static void apply(ClassNode targetClass){
		List<ToInject> injects = new ArrayList<>();
		List<EntryMixinMerged> redirects = new ArrayList<>();
		targetClass.methods.forEach(methodNode -> {
			if (!(methodNode instanceof MethodNodeEx)) return;
			AnnotationNode inject = null;
			String mixin = null;
			for(AnnotationNode annotationNode : methodNode.visibleAnnotations){
				if ((
						"Lcom/unascribed/fabrication/support/injection/ModifyReturn;".equals(annotationNode.desc)
						|| "Lcom/unascribed/fabrication/support/injection/Hijack;".equals(annotationNode.desc)
					) &&dejavu.add(targetClass.name+methodNode.name+methodNode.desc)
				) {
					inject = annotationNode;
				} else if ("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;".equals(annotationNode.desc)){
					mixin = (String) annotationNode.values.get(annotationNode.values.indexOf("mixin") + 1);
					if (methodNode.name.startsWith("redirect$")) {
						try {
							for (Method m : Class.forName(mixin, false, Mixin.class.getClassLoader()).getMethods()) {
								if (m.isAnnotationPresent(Redirect.class) && methodNode.name.endsWith("$"+m.getName())) {
									redirects.add(new EntryMixinMerged(methodNode.name, methodNode.desc, mixin, m.getAnnotation(Redirect.class)));
								}
							}
						} catch (Exception ignore) {}
					}
				}
			};
			if (inject != null && mixin != null) {
				final String mix = mixin;
				injects.add(new ToInject(
						((List<String>) inject.values.get(inject.values.indexOf("method") + 1)).stream().map(s -> FabRefMap.methodMap(mix, s)).collect(Collectors.toList()),
						((List<String>) inject.values.get(inject.values.indexOf("target") + 1)).stream().map(s -> FabRefMap.targetMap(mix, s)).collect(Collectors.toList()),
						targetClass.name,
						methodNode.name,
						methodNode.desc,
						methodNode.access,
						inject.desc
				));
			}
		});
		injects.forEach(toInject -> redirects.forEach(redirect -> {
			if (redirect.redirect.at() == null) return;
			//TODO target should probably match other formats?
			String target = FabRefMap.targetMap(redirect.mixin, redirect.redirect.at().target());
			if (toInject.target.contains(target) && target.endsWith(redirect.desc.substring(redirect.desc.indexOf(';')+1))) {
				toInject.potentiallyRedirected.add(redirect.name+redirect.desc);
				FabLog.warn("FabInjector found a Redirect from "+redirect.mixin+";"+redirect.name+";"+" which has been added to "+toInject.owner+";"+toInject.name);
			}
		}));
		targetClass.methods.forEach(methodNode -> injects.forEach(toInject -> {
			for (String m : toInject.method){
				if (!m.equals(methodNode.name+methodNode.desc)) continue;
				for (AbstractInsnNode insnNode : methodNode.instructions){
					if (insnNode instanceof MethodInsnNode) {
						MethodInsnNode insn = (MethodInsnNode) insnNode;
						for (String target : toInject.target) {
							String unchangedTarget = target;
							if (target.charAt(0) == 'L') target = target.substring(1);
							if (target.startsWith(insn.owner)) {
								char d = target.charAt(insn.owner.length());
								if ((d == '.' || d == ';') && target.substring(insn.owner.length() + 1).equals(insn.name + insn.desc)) {
									if (performInjection(methodNode, insn, toInject, target)) {
										toInject.done.put(m, unchangedTarget);
										String type = toInject.annotation.substring(toInject.annotation.lastIndexOf('/'), toInject.annotation.length()-1);
										FabLog.debug("Completed "+type+" Injection : " + toInject.owner + ";" + m + "\t" + unchangedTarget);
									}
								}
							}
						}
						if (toInject.owner.equals(insn.owner)) {
							for (String target : toInject.potentiallyRedirected) {
								if (target.equals(insn.name + insn.desc)) {
									if (performInjection(methodNode, insn, toInject, target)) {
										String type = toInject.annotation.substring(toInject.annotation.lastIndexOf('/'), toInject.annotation.length() - 1);
										FabLog.debug("Completed " + type + " Injection over existing Redirect : " + toInject.owner + ";" + m + "\t" + target);
									}
								}
							}
						}
					}
				}
			}
		}));
		injects.forEach(ti -> ti.method.forEach(m -> ti.target.forEach(t ->{
			if (!t.equals(ti.done.get(m))) FabLog.error("FabInjector failed to find injection point for "+ti.owner+";"+m+"\t"+t);
		})));
	}

	public static boolean performInjection(MethodNode methodNode, MethodInsnNode insn, ToInject toInject, String target) {
		boolean toInjectIsStatic = (toInject.access & Opcodes.ACC_STATIC) != 0;
		InsnList mod = new InsnList();
		List<Type> argTypes = new ArrayList<>();
		if (insn.getOpcode() != Opcodes.INVOKESTATIC)
			argTypes.add(Type.VOID_TYPE);
		Type targetType = Type.getMethodType(target.substring(target.indexOf('(')));
		argTypes.addAll(Arrays.asList(targetType.getArgumentTypes()));
		Type toInjectType = Type.getMethodType(toInject.desc);
		Type[] toInjectArgTypes = toInjectType.getArgumentTypes();
		int countDesc = toInjectArgTypes.length;
		int max = methodNode.maxLocals;
		InsnList oldVars = new InsnList();
		InsnList newVars = new InsnList();
		//TODO probably never. continue the variable trace after the first method to further reduce allocation
		if ("Lcom/unascribed/fabrication/support/injection/ModifyReturn;".equals(toInject.annotation)) {
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
						newVars.insert(new VarInsnNode(getLoadOpcode(sort), max));
						methodNode.instructions.insertBefore(varTrace == null ? insn : varTrace, new VarInsnNode(getStoreOpcode(sort), max++));
					}
				}
				methodNode.maxLocals=max;
				if (!toInjectIsStatic) {
					mod.add(new VarInsnNode(getStoreOpcode(targetType.getReturnType().getSort()), methodNode.maxLocals));
					mod.add(new VarInsnNode(Opcodes.ALOAD, 0));
					mod.add(new VarInsnNode(getLoadOpcode(targetType.getReturnType().getSort()), methodNode.maxLocals));
					methodNode.maxLocals += 1;
				}
				for (AbstractInsnNode a : newVars) {
					if (countDesc-->0) mod.add(a.clone(new HashMap<>()));
				}
				for (AbstractInsnNode a : oldVars) {
					if (countDesc-->0) mod.add(a.clone(new HashMap<>()));
				}
				methodNode.instructions.insertBefore(varTrace == null ? insn : varTrace, newVars);
				for (int c = toInjectIsStatic ? 0 : 1; c < countDesc; c++) {
					mod.add(new VarInsnNode(getLoadOpcode(toInjectArgTypes[toInjectArgTypes.length-countDesc+c].getSort()), c));
				}
			} else if (!toInjectIsStatic) {
				mod.add(new VarInsnNode(getStoreOpcode(targetType.getReturnType().getSort()), methodNode.maxLocals));
				mod.add(new VarInsnNode(Opcodes.ALOAD, 0));
				mod.add(new VarInsnNode(getLoadOpcode(targetType.getReturnType().getSort()), methodNode.maxLocals));
				methodNode.maxLocals += 1;
			}
			mod.add(new MethodInsnNode(toInjectIsStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL, toInject.owner, toInject.name, toInject.desc, false));
			methodNode.instructions.insert(insn, mod);
			return true;
		} else if ("Lcom/unascribed/fabrication/support/injection/Hijack;".equals(toInject.annotation)) {
			for (int c = 0; c < argTypes.size(); c++) {
				methodNode.instructions.insertBefore(insn, new VarInsnNode(getStoreOpcode(argTypes.get(argTypes.size() - 1 - c).getSort()), max++));
			}
			LabelNode label = new LabelNode(new Label());
			LabelNode label2 = new LabelNode(new Label());
			boolean optionalReturn = toInjectType.getReturnType().getSort() != Type.BOOLEAN;
			mod.add(new MethodInsnNode(toInjectIsStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL, toInject.owner, toInject.name, toInject.desc, false));
			if (optionalReturn) {
				mod.add(new VarInsnNode(Opcodes.ASTORE, max));
				mod.add(new VarInsnNode(Opcodes.ALOAD, max));
			}
			mod.add(new JumpInsnNode(optionalReturn ? Opcodes.IFNULL : Opcodes.IFNE, label));
			if (optionalReturn) {
				mod.add(new VarInsnNode(Opcodes.ALOAD, max));
				mod.add(new FieldInsnNode(Opcodes.GETFIELD, "com/unascribed/fabrication/support/injection/HijackReturn", "object", "Ljava/lang/Object;"));
				castHijackReturnResult(targetType.getReturnType(), mod);
				mod.add(new JumpInsnNode(Opcodes.GOTO, label2));
				mod.add(label);
			}
			methodNode.maxLocals=max+1;
			if (!toInjectIsStatic) methodNode.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
			for (Type argType : argTypes) {
				int opcode = getLoadOpcode(argType.getSort());
				mod.add(new VarInsnNode(opcode, --max));
				if (countDesc-->0)
					methodNode.instructions.insertBefore(insn, new VarInsnNode(opcode, max));
			}
			for (int c = toInjectIsStatic ? 0 : 1; c < countDesc; c++) {
				methodNode.instructions.insertBefore(insn, new VarInsnNode(getLoadOpcode(toInjectArgTypes[toInjectArgTypes.length-countDesc+c].getSort()), c));
			}

			methodNode.instructions.insertBefore(insn, mod);
			methodNode.instructions.insert(insn, optionalReturn? label2 : label);
			return true;
		}
		return false;
	}

	public static void castHijackReturnResult(Type desc, InsnList mod) {
		switch (desc.getSort()) {
			case Type.BOOLEAN:
				mod.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Boolean"));
				mod.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false));
				break;
			case Type.BYTE:
				mod.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Byte"));
				mod.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false));
				break;
			case Type.CHAR:
				mod.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Character"));
				mod.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false));
				break;
			case Type.SHORT:
				mod.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Short"));
				mod.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false));
				break;
			case Type.INT:
				mod.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Integer"));
				mod.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
				break;
			case Type.LONG:
				mod.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Long"));
				mod.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false));
				break;
			case Type.FLOAT:
				mod.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Float"));
				mod.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false));
				break;
			case Type.DOUBLE:
				mod.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Double"));
				mod.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false));
				break;
			default:
				String descStr = desc.toString();
				mod.add(new TypeInsnNode(Opcodes.CHECKCAST, descStr.substring(1, descStr.length() - 1)));
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
