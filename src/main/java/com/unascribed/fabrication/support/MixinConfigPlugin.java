package com.unascribed.fabrication.support;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import com.google.common.reflect.ClassPath;
import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.support.injection.FabRefMap;
import com.unascribed.fabrication.support.injection.FailsoftCallbackInjectionInfo;
import com.unascribed.fabrication.support.injection.FailsoftModifyArgInjectionInfo;
import com.unascribed.fabrication.support.injection.FailsoftModifyArgsInjectionInfo;
import com.unascribed.fabrication.support.injection.FailsoftModifyConstantInjectionInfo;
import com.unascribed.fabrication.support.injection.FailsoftModifyVariableInjectionInfo;
import com.unascribed.fabrication.support.injection.FailsoftRedirectInjectionInfo;
import com.unascribed.fabrication.support.injection.FabInjector;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import com.unascribed.fabrication.support.injection.FakeMixinHack;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.util.asm.MethodNodeEx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class MixinConfigPlugin implements IMixinConfigPlugin {

	private static final SetMultimap<String, String> configKeysForDiscoveredClasses = HashMultimap.create();
	public static boolean loadComplete = false;

	@Override
	public void onLoad(String mixinPackage) {
		FabConf.reload();
		Mixins.registerErrorHandlerClass("com.unascribed.fabrication.support.MixinErrorHandler_THIS_ERROR_HANDLER_IS_FOR_SOFT_FAILURE_IN_FABRICATION_ITSELF_AND_DOES_NOT_IMPLY_FABRICATION_IS_RESPONSIBLE_FOR_THE_BELOW_ERROR");
		FabLog.warn("Fabrication is about to inject into Mixin to add support for failsoft mixins.");
		FabLog.warn("THE FOLLOWING WARNINGS ARE NOT AN ERROR AND DO NOT IMPLY FABRICATION IS RESPONSIBLE FOR A CRASH.");
		InjectionInfo.register(FailsoftCallbackInjectionInfo.class);
		InjectionInfo.register(FailsoftModifyArgInjectionInfo.class);
		InjectionInfo.register(FailsoftModifyArgsInjectionInfo.class);
		InjectionInfo.register(FailsoftRedirectInjectionInfo.class);
		InjectionInfo.register(FailsoftModifyVariableInjectionInfo.class);
		InjectionInfo.register(FailsoftModifyConstantInjectionInfo.class);
		FabLog.warn("Injection complete.");
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		FabLog.debug("☕ Profile: "+FabConf.getProfileName().toLowerCase(Locale.ROOT));
		return discoverClassesInPackage("com.unascribed.fabrication.mixin", true);
	}

	@SuppressWarnings("unchecked")
	public static List<String> discoverClassesInPackage(String pkg, boolean truncate) {
		FabLog.debug("Starting discovery pass...");
		try {
			int count = 0;
			int enabled = 0;
			int skipped = 0;
			List<String> rtrn = Lists.newArrayList();
			for (ClassInfo ci : getClassesInPackage(pkg)) {
				// we want nothing to do with inner classes and the like
				if (ci.getName().contains("$")) continue;
				count++;
				String truncName = ci.getName().substring(pkg.length()+1);
				FabLog.debug("--");
				FabLog.debug((Math.random() < 0.01 ? "👅" : "👀")+" Considering "+truncName);
				ClassReader cr = new ClassReader(ci.asByteSource().read());
				ClassNode cn = new ClassNode();
				cr.accept(cn, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
				boolean eligible = true;
				List<String> eligibilityFailures = Lists.newArrayList();
				List<String> eligibilityNotes = Lists.newArrayList();
				List<String> eligibilitySuccesses = Lists.newArrayList();
				boolean anyRestrictions = false;
				if (cn.visibleAnnotations != null) {
					for (AnnotationNode an : cn.visibleAnnotations) {
						if (an.desc.equals("Lcom/unascribed/fabrication/support/EligibleIf;")) {
							if (an.values == null) continue;
							for (int i = 0; i < an.values.size(); i += 2) {
								anyRestrictions = true;
								String k = (String)an.values.get(i);
								Object v = an.values.get(i+1);
								if (k.equals("configAvailable")) {
									if (!FabConf.defaultContains((String)v)) {
										FabLog.debug("🙈 Dev error! Exploding.");
										throw FabConf.devError(cn.name.substring(pkg.length()+1).replace('/', '.')+" references an unknown config key "+v+"\n\nDid you forget to add it to features.txt and run build-features.sh?");
									}
									if (FabConf.limitRuntimeConfigs() && !FabConf.isEnabled((String) v))
										eligible = false;
									if (FabConf.isBanned((String)v)) {
										eligibilityFailures.add("Required config setting "+ FabConf.remap((String)v)+" is banned");
										eligible = false;
									} else {
										eligibilitySuccesses.add("Required config key "+ FabConf.remap((String)v)+" is not banned");
									}
									configKeysForDiscoveredClasses.put(ci.getName(), (String)v);
								} else if (k.equals("envMatches")) {
									String[] arr = (String[])v;
									if (arr[0].equals("Lcom/unascribed/fabrication/support/Env;")) {
										Env e = Env.valueOf(arr[1]);
										Env curEnv = Agnos.getCurrentEnv();
										if (!curEnv.matches(e)) {
											eligibilityFailures.add("Environment is incorrect (want "+e.name().toLowerCase(Locale.ROOT)+", got "+curEnv.name().toLowerCase(Locale.ROOT)+")");
											eligible = false;
										} else {
											eligibilitySuccesses.add("Environment is correct ("+e.name().toLowerCase(Locale.ROOT)+")");
										}
									}
								} else if (k.equals("modLoaded")) {
									for (String s : (List<String>)v) {
										if (!Agnos.isModLoaded(s)) {
											eligibilityFailures.add("Required mod "+s+" is not loaded");
											eligible = false;
										} else {
											eligibilitySuccesses.add("Required mod "+s+" is loaded");
										}
									}
								} else if (k.equals("modNotLoaded")) {
									for (String s : (List<String>)v) {
										if (Agnos.isModLoaded(s)) {
											eligibilityFailures.add("Conflicting mod "+s+" is loaded");
											eligible = false;
										} else {
											eligibilitySuccesses.add("Conflicting mod "+s+" is not loaded");
										}
									}
								} else if (k.equals("classPresent")) {
									for (String s : (List<String>)v) {
										try {
											Class.forName(s, false, MixinConfigPlugin.class.getClassLoader());
											eligibilitySuccesses.add("Required class "+s+" is present");
										} catch (ClassNotFoundException e) {
											eligibilityFailures.add("Required class "+s+" is not present");
											eligible = false;
										}
									}
								} else if (k.equals("classNotPresent")) {
									for (String s : (List<String>)v) {
										try {
											Class.forName(s, false, MixinConfigPlugin.class.getClassLoader());
											eligibilityFailures.add("Conflicting class "+s+" is present");
											eligible = false;
										} catch (ClassNotFoundException e) {
											eligibilitySuccesses.add("Conflicting class "+s+" is not present");
										}
									}
								} else if (k.equals("anyConfigAvailable")) {
									boolean allBanned = true;
									boolean runtimeCheck = FabConf.limitRuntimeConfigs();
									for (String s : (List<String>)v) {
										s = FabConf.remap(s);
										if (runtimeCheck && FabConf.isEnabled(s))
											runtimeCheck = false;
										if (FabConf.isBanned(s)) {
											eligibilityNotes.add("Relevant config setting "+s+" is banned");
										} else {
											allBanned = false;
											eligibilitySuccesses.add("Relevant config setting "+s+" is not banned");
										}
										configKeysForDiscoveredClasses.put(ci.getName(), s);
									}
									if (runtimeCheck)
										eligible = false;
									if (allBanned) {
										eligibilityFailures.add("All of the relevant config settings are banned");
										eligible = false;
									}
								} else if (k.equals("specialConditions")) {
									List<String[]> li = (List<String[]>)v;
									if (li.isEmpty()) {
										eligibilityNotes.add("Special conditions is present but empty - ignoring");
									} else {
										for (String[] e : li) {
											if (!"Lcom/unascribed/fabrication/support/SpecialEligibility;".equals(e[0])) {
												eligibilityNotes.add("Unknown special condition type "+e[0]+" - ignoring");
											} else {
												try {
													SpecialEligibility se = SpecialEligibility.valueOf(e[1]);
													if (FabConf.isMet(se)) {
														eligibilitySuccesses.add("Special condition "+se+" is met");
													} else {
														eligibilityFailures.add("Special condition "+se+" is not met");
														eligible = false;
													}
												} catch (IllegalArgumentException ex) {
													eligibilityFailures.add("Unknown special condition "+e[1]);
													eligible = false;
												}
											}
										}
									}
								} else {
									FabLog.warn("Unknown annotation setting "+k);
								}
							}
						}
					}
				}
				if (!anyRestrictions) {
					eligibilityNotes.add("No restrictions on eligibility");
				}
				for (String s : eligibilityNotes) {
					FabLog.debug("  ℹ️ "+s);
				}
				for (String s : eligibilitySuccesses) {
					FabLog.debug("  ✅ "+s);
				}
				for (String s : eligibilityFailures) {
					FabLog.debug("  ❌ "+s);
				}
				if (eligible) {
					enabled++;
					FabLog.debug("👍 Eligibility requirements met. Applying "+truncName);
					rtrn.add(truncate ? truncName : ci.getName());
				} else {
					skipped++;
					FabLog.debug("✋ Eligibility requirements not met. Skipping "+truncName);
				}
			}
			FabLog.debug("Discovery pass complete. Found "+count+" candidates, enabled "+enabled+", skipped "+skipped+".");
			return rtrn;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Iterable<ClassInfo> getClassesInPackage(String pkg) {
		try (InputStream is = MixinConfigPlugin.class.getClassLoader().getResourceAsStream("classes.txt")) {
			if (is != null) {
				List<ClassInfo> rtrn = Lists.newArrayList();
				BufferedReader br = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
				String prefix = pkg.replace('.', '/')+"/";
				while (true) {
					String line = br.readLine();
					if (line == null) break;
					if (line.startsWith(prefix)) {
						rtrn.add(new BareClassInfo(line, MixinConfigPlugin.class.getClassLoader()));
					}
				}
				return rtrn;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			return Iterables.transform(ClassPath.from(MixinConfigPlugin.class.getClassLoader()).getTopLevelClassesRecursive(pkg), GuavaClassInfo::new);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		if (FabConf.limitRuntimeConfigs()) finalizeIsEnabled(targetClass);
		fakeMixinHack(targetClass, mixinClassName);
		FabInjector.apply(targetClass);
		lithiumCompat(targetClass, mixinClassName);
		if (targetClass.visibleAnnotations != null) {
			targetClass.visibleAnnotations.removeIf(an -> an.desc.startsWith("Lcom/unascribed/fabrication"));
		}
	}

	public static void fakeMixinHack(ClassNode targetClass, String mixinClassName){
		try {
			FakeMixinHack sih = Class.forName(mixinClassName, false, MixinConfigPlugin.class.getClassLoader()).getAnnotation(FakeMixinHack.class);
			if (sih != null){
				List<FabInjector.ToInject> toInject = new ArrayList<>();
				for (Class<?> cl : sih.value()){
					for (Method mthd : cl.getMethods()){
						ModifyReturn mr = mthd.getAnnotation(ModifyReturn.class);
						Hijack hi = mthd.getAnnotation(Hijack.class);
						String[] method = null;
						String[] target = null;
						String desc = null;
						if (mr != null) {
							method = mr.method();
							target = mr.target();
							desc = "Lcom/unascribed/fabrication/support/injection/ModifyReturn;";
						} else if (hi != null) {
							method = hi.method();
							target = hi.target();
							desc = "Lcom/unascribed/fabrication/support/injection/Hijack;";
						}
						if (target != null && method != null && desc != null) {
							toInject.add(new FabInjector.ToInject(
									Arrays.stream(method).map(s -> FabRefMap.methodMap(cl.getName(), s)).collect(Collectors.toList()),
									Arrays.stream(target).map(s -> FabRefMap.targetMap(cl.getName(), s)).collect(Collectors.toList()),
									cl.getName().replace('.', '/'),
									mthd.getName(),
									Type.getMethodDescriptor(mthd),
									Opcodes.INVOKESTATIC,
									desc,
									cl.getName()
							));
						}

					}
				}
				FabInjector.apply(targetClass, toInject);
			}
		} catch (Exception ignore) {}
	}

	public static void lithiumCompat(ClassNode targetClass, String mixinClassName){
		if(Agnos.isModLoaded("lithium") && "com.unascribed.fabrication.mixin.e_mechanics.colorful_redstone.MixinRedstoneWireBlock".equals(mixinClassName)) {
			targetClass.methods.forEach(methodNode -> {
				if (methodNode instanceof MethodNodeEx && "getReceivedPowerFaster".equals(((MethodNodeEx) methodNode).getOriginalName())){
					methodNode.visibleAnnotations.forEach(annotationNode -> {
						if (!"Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;".equals(annotationNode.desc)) return;
						for (int i=0; i<annotationNode.values.size(); i++){
							if ("mixin".equals(annotationNode.values.get(i))){
								i++;
								if (i<annotationNode.values.size() && "me.jellysquid.mods.lithium.mixin.block.redstone_wire.RedstoneWireBlockMixin".equals(annotationNode.values.get(i))){
									LabelNode label = new LabelNode(new Label());
									InsnList earlyRet = new InsnList();
									earlyRet.add(new LdcInsnNode("*.colorful_redstone"));
									earlyRet.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/unascribed/fabrication/FabConf", "isEnabled", "(Ljava/lang/String;)Z", false));
									earlyRet.add(new JumpInsnNode(Opcodes.IFEQ, label));
									earlyRet.add(new InsnNode(Opcodes.RETURN));
									earlyRet.add(label);
									methodNode.instructions.insert(earlyRet);
								}
								break;
							}
						}
					});
				}
			});
		}
	}

	public static void finalizeIsEnabled(ClassNode targetClass){
		targetClass.methods.forEach(methodNode -> {
					for (AbstractInsnNode insnNode : methodNode.instructions){
						if (insnNode instanceof MethodInsnNode) {
							MethodInsnNode insn = (MethodInsnNode) insnNode;
							if (insn.getOpcode() == Opcodes.INVOKESTATIC && "com/unascribed/fabrication/FabConf".equals(insn.owner) && "(Ljava/lang/String;)Z".equals(insn.desc)) {
								AbstractInsnNode prevInsn = insn.getPrevious();
								if (prevInsn.getOpcode() == Opcodes.LDC){
									Object key = ((LdcInsnNode)prevInsn).cst;
									if ("isEnabled".equals(insn.name)){
										methodNode.instructions.insertBefore(prevInsn, new InsnNode(FabConf.isEnabled((String)key)? Opcodes.ICONST_1 : Opcodes.ICONST_0));
									}else if ("isAnyEnabled".equals(insn.name)){
										methodNode.instructions.insertBefore(prevInsn, new InsnNode(FabConf.isAnyEnabled((String)key)? Opcodes.ICONST_1 : Opcodes.ICONST_0));
									}else continue;
									methodNode.instructions.remove(prevInsn);
									methodNode.instructions.remove(insn);
									FabLog.debug("Removed IsEnabled Check from : "+targetClass.name+";"+methodNode.name+methodNode.desc);
								}
							}
						}
					}
		});
	}

	private interface ClassInfo {

		String getName();
		ByteSource asByteSource();

	}

	private static class BareClassInfo implements ClassInfo {

		private final String name;
		private final ClassLoader loader;

		public BareClassInfo(String name, ClassLoader loader) {
			this.name = name;
			this.loader = loader;
		}

		@Override
		public String getName() {
			return name.replace('/', '.').replace(".class", "");
		}

		@Override
		public ByteSource asByteSource() {
			return Resources.asByteSource(loader.getResource(name));
		}

	}

	private static class GuavaClassInfo implements ClassInfo {

		private final ClassPath.ClassInfo delegate;

		public GuavaClassInfo(ClassPath.ClassInfo delegate) {
			this.delegate = delegate;
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public ByteSource asByteSource() {
			return delegate.asByteSource();
		}

	}
	
}
