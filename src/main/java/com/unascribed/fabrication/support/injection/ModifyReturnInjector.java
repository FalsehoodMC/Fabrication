package com.unascribed.fabrication.support.injection;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.util.asm.MethodNodeEx;

import java.util.*;

public class ModifyReturnInjector {
	public static class ToInject{
		public List<String> method;
		public String target;
		public String owner;
		public String name;
		public String desc;

		public ToInject(List<String> method, String target, String owner, String name, String desc){
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
					injects.add(new ToInject((List<String>) annotationNode.values.get(annotationNode.values.indexOf("method")+1), (String)annotationNode.values.get(annotationNode.values.indexOf("target")+1), targetClass.name, methodNode.name, methodNode.desc));
			});
		});
		targetClass.methods.forEach(methodNode -> {
			injects.stream().forEach(toInject -> {
				for (String m : toInject.method){
					if (!m.equals(methodNode.name+methodNode.desc)) continue;
					for (AbstractInsnNode insnNode : methodNode.instructions){
						if (insnNode instanceof MethodInsnNode){
							MethodInsnNode insn = (MethodInsnNode)insnNode;
							if (toInject.target.charAt(0) == 'L') toInject.target = toInject.target.substring(1);
							if (toInject.target.startsWith(insn.owner)){
								char d = toInject.target.charAt(insn.owner.length());
								if ((d == '.' || d == ';') && toInject.target.substring(insn.owner.length()+1).equals(insn.name+insn.desc)){
									InsnList mod = new InsnList();
									int countTarget = Type.getMethodType(toInject.target.substring(toInject.target.indexOf('('))).getArgumentTypes().length;
									if (insn.getOpcode() != Opcodes.INVOKESTATIC) countTarget++;
									int countDesc = Type.getMethodType(toInject.desc).getArgumentTypes().length;
									if (countDesc>1) {
										int max = methodNode.maxLocals;
										//TODO trace var origin at least till method calls
										for (int c=0; c<countTarget; c++){
											methodNode.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ASTORE, max++));
										}
										for (int c=0; c<countTarget; c++){
											mod.add(new VarInsnNode(Opcodes.ALOAD, --max));
											methodNode.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, max));
										}
										for (int c=0; c<countDesc-countTarget-1; c++){
											mod.add(new VarInsnNode(Opcodes.ALOAD, c));
										}
									}
									mod.add(new MethodInsnNode(Opcodes.INVOKESTATIC, toInject.owner, toInject.name, toInject.desc, false));
									methodNode.instructions.insert(insn, mod);
								}
							}
						}
					}
				}
			});
		});
	}
}
