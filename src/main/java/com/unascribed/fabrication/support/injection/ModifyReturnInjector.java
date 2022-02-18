package com.unascribed.fabrication.support.injection;

import com.unascribed.fabrication.FabLog;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.util.asm.MethodNodeEx;

import java.util.*;

public class ModifyReturnInjector {
	public static class ToInject{
		public List<String> method;
		public List<String> target;
		public String owner;
		public String name;
		public String desc;
		public String annotation;

		public ToInject(List<String> method, List<String> target, String owner, String name, String desc, String annotation){
			this.method = method;
			this.target = target;
			this.owner = owner;
			this.name = name;
			this.desc = desc;
			this.annotation = annotation;
		}
	}
	public static final Set<String> dejavu = new HashSet<>();

	//TODO figure out how to write to mixin refmap
	public static void apply(ClassNode targetClass){
		List<ToInject> injects = new ArrayList<>();
		targetClass.methods.forEach(methodNode -> {
			if (!(methodNode instanceof MethodNodeEx)) return;
			methodNode.visibleAnnotations.forEach(annotationNode -> {
				if ((
						"Lcom/unascribed/fabrication/support/injection/ModifyReturn;".equals(annotationNode.desc)
						|| "Lcom/unascribed/fabrication/support/injection/Hijack;".equals(annotationNode.desc)
					) &&dejavu.add(targetClass.name+methodNode.name+methodNode.desc)
				) {
					injects.add(new ToInject((List<String>) annotationNode.values.get(annotationNode.values.indexOf("method") + 1), (List<String>) annotationNode.values.get(annotationNode.values.indexOf("target") + 1), targetClass.name, methodNode.name, methodNode.desc, annotationNode.desc));
				}
			});
		});
		targetClass.methods.forEach(methodNode -> {
			injects.stream().forEach(toInject -> {
				for (String m : toInject.method){
					if (!m.equals(methodNode.name+methodNode.desc)) continue;
					for (AbstractInsnNode insnNode : methodNode.instructions){
						if (insnNode instanceof MethodInsnNode) {
							MethodInsnNode insn = (MethodInsnNode) insnNode;
							for (String target : toInject.target) {
								if (target.charAt(0) == 'L') target = target.substring(1);
								if (target.startsWith(insn.owner)) {
									char d = target.charAt(insn.owner.length());
									if ((d == '.' || d == ';') && target.substring(insn.owner.length() + 1).equals(insn.name + insn.desc)) {
										InsnList mod = new InsnList();
										List<Type> argTypes = new ArrayList<>();
										if (insn.getOpcode() != Opcodes.INVOKESTATIC)
											argTypes.add(Type.VOID_TYPE);
										Type targetType = Type.getMethodType(target.substring(target.indexOf('(')));
										argTypes.addAll(Arrays.asList(targetType.getArgumentTypes()));
										Type toInjectType = Type.getMethodType(toInject.desc);
										int countDesc = toInjectType.getArgumentTypes().length;
										int max = methodNode.maxLocals;
										//TODO trace var origin at least till method calls
										//TODO non-ALOAD capture
										if ("Lcom/unascribed/fabrication/support/injection/ModifyReturn;".equals(toInject.annotation)) {
											if (--countDesc > 0) {
												for (int c = 0; c < argTypes.size(); c++) {
													methodNode.instructions.insertBefore(insn, new VarInsnNode(getStoreOpcode(argTypes.get(argTypes.size()-1-c).getSort()), max++));
												}
												methodNode.maxLocals=max;
												for (Type argType : argTypes) {
													int opcode = getLoadOpcode(argType.getSort());
													mod.add(new VarInsnNode(opcode, --max));
													methodNode.instructions.insertBefore(insn, new VarInsnNode(opcode, max));
												}
												for (int c = 0; c < countDesc - argTypes.size(); c++) {
													mod.add(new VarInsnNode(Opcodes.ALOAD, c));
												}
											}
											mod.add(new MethodInsnNode(Opcodes.INVOKESTATIC, toInject.owner, toInject.name, toInject.desc, false));
											methodNode.instructions.insert(insn, mod);
											FabLog.debug("Completed ModifyReturn Injection : " + m + "\t" + target);
										}else if ("Lcom/unascribed/fabrication/support/injection/Hijack;".equals(toInject.annotation)) {
											for (int c = 0; c < argTypes.size(); c++) {
												methodNode.instructions.insertBefore(insn, new VarInsnNode(getStoreOpcode(argTypes.get(argTypes.size() - 1 - c).getSort()), max++));
											}
											LabelNode label = new LabelNode(new Label());
											LabelNode label2 = new LabelNode(new Label());
											boolean optionalReturn = toInjectType.getReturnType().getSort() != Type.BOOLEAN;
											mod.add(new MethodInsnNode(Opcodes.INVOKESTATIC, toInject.owner, toInject.name, toInject.desc, false));
											if (optionalReturn) {
												mod.add(new VarInsnNode(Opcodes.ASTORE, max));
												mod.add(new VarInsnNode(Opcodes.ALOAD, max));
												mod.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/util/Optional", "isPresent", "()Z", false));
											}
											mod.add(new JumpInsnNode(optionalReturn ? Opcodes.IFEQ : Opcodes.IFNE, label));
											if (optionalReturn) {
												mod.add(new VarInsnNode(Opcodes.ALOAD, max));
												mod.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/util/Optional", "get", "()"+targetType.getReturnType(), false));
												mod.add(new JumpInsnNode(Opcodes.GOTO, label2));
												mod.add(label);
											}
											methodNode.maxLocals=max+1;
											for (Type argType : argTypes) {
												int opcode = getLoadOpcode(argType.getSort());
												mod.add(new VarInsnNode(opcode, --max));
												if (countDesc-->0)
													methodNode.instructions.insertBefore(insn, new VarInsnNode(opcode, max));
											}
											for (int c = 0; c < countDesc; c++) {
												methodNode.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, c));
											}

											methodNode.instructions.insertBefore(insn, mod);
											methodNode.instructions.insert(insn, optionalReturn? label2 : label);
											FabLog.debug("Completed Hijack Injection : " + m + "\t" + target);
										}
									}
								}
							}
						}
					}
				}
			});
		});
	}
	public static int getStoreOpcode(int type){
		return getLoadOpcode(type) + 33;
	}
	public static int getLoadOpcode(int type){
		switch (type){
			case Type.BOOLEAN: case Type.BYTE: case Type.CHAR: case Type.SHORT: case Type.INT: return Opcodes.ILOAD;
			case Type.LONG: return Opcodes.LLOAD;
			case Type.FLOAT: return Opcodes.FLOAD;
			case Type.DOUBLE: return Opcodes.DLOAD;
			default: return Opcodes.ALOAD;
		}
	}
}
