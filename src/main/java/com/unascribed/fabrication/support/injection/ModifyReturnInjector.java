package com.unascribed.fabrication.support.injection;

import com.unascribed.fabrication.FabLog;
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

		public ToInject(List<String> method, List<String> target, String owner, String name, String desc){
			this.method = method;
			this.target = target;
			this.owner = owner;
			this.name = name;
			this.desc = desc;
		}
	}
	public static final Set<String> dejavu = new HashSet<>();

	public static void apply(ClassNode targetClass){
		List<ToInject> injects = new ArrayList<>();
		targetClass.methods.forEach(methodNode -> {
			if (!(methodNode instanceof MethodNodeEx)) return;
			methodNode.visibleAnnotations.forEach(annotationNode -> {
				if (!"Lcom/unascribed/fabrication/support/injection/ModifyReturn;".equals(annotationNode.desc)) return;
				if (dejavu.add(targetClass.name+methodNode.name+methodNode.desc))
					injects.add(new ToInject((List<String>) annotationNode.values.get(annotationNode.values.indexOf("method") + 1), (List<String>) annotationNode.values.get(annotationNode.values.indexOf("target") + 1), targetClass.name, methodNode.name, methodNode.desc));
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
										argTypes.addAll(Arrays.asList(Type.getMethodType(target.substring(target.indexOf('('))).getArgumentTypes()));
										int countDesc = Type.getMethodType(toInject.desc).getArgumentTypes().length;
										if (countDesc > 1) {
											int max = methodNode.maxLocals;
											//TODO trace var origin at least till method calls
											for (int c = 0; c < argTypes.size(); c++) {
												methodNode.instructions.insertBefore(insn, new VarInsnNode(getStoreOpcode(argTypes.get(argTypes.size()-1-c).getSort()), max++));
											}
											for (int c = 0; c < argTypes.size(); c++) {
												int opcode = getLoadOpcode(argTypes.get(argTypes.size()-1-c).getSort());
												mod.add(new VarInsnNode(opcode, --max));
												methodNode.instructions.insertBefore(insn, new VarInsnNode(opcode, max));
											}
											for (int c = 0; c < countDesc - argTypes.size() - 1; c++) {
												mod.add(new VarInsnNode(Opcodes.ALOAD, c));
											}
										}
										mod.add(new MethodInsnNode(Opcodes.INVOKESTATIC, toInject.owner, toInject.name, toInject.desc, false));
										methodNode.instructions.insert(insn, mod);
										FabLog.debug("Completed ModifyReturn Injection : "+m+"\t"+target);
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
			case Type.INT: return Opcodes.ILOAD;
			case Type.LONG: return Opcodes.LLOAD;
			case Type.FLOAT: return Opcodes.FLOAD;
			case Type.DOUBLE: return Opcodes.DLOAD;
			default: return Opcodes.ALOAD;
		}
	}
}
