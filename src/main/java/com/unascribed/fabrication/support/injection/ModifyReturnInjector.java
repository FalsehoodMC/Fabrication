package com.unascribed.fabrication.support.injection;

import com.unascribed.fabrication.FabLog;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.util.asm.MethodNodeEx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
	//TODO error reporting (besides failsoft)
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
		targetClass.methods.forEach(methodNode -> injects.forEach(toInject -> {
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
											for (AbstractInsnNode a : newVars) {
												if (countDesc-->0) mod.add(a.clone(new HashMap<>()));
											}
											for (AbstractInsnNode a : oldVars) {
												if (countDesc-->0) mod.add(a.clone(new HashMap<>()));
											}
											methodNode.instructions.insertBefore(varTrace == null ? insn : varTrace, newVars);
											for (int c = 0; c < countDesc; c++) {
												mod.add(new VarInsnNode(getLoadOpcode(toInjectArgTypes[toInjectArgTypes.length-countDesc+c].getSort()), c));
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
											methodNode.instructions.insertBefore(insn, new VarInsnNode(getLoadOpcode(toInjectArgTypes[toInjectArgTypes.length-countDesc+c].getSort()), c));
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
		}));
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
