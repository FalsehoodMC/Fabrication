package com.unascribed.fabrication.mixin.a_fixes.multiline_sign_paste;

import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sun.jna.Platform;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import com.google.common.base.Joiner;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.text.Text;

import java.util.function.Supplier;

@Mixin(AbstractSignEditScreen.class)
@EligibleIf(configAvailable="*.multiline_sign_paste", envMatches=Env.CLIENT)
public abstract class MixinAbstractSignEditScreen extends Screen {
	@Shadow
	@Final
	protected String[] text;

	@Shadow
	private SelectionManager selectionManager;

	@Shadow
	@Final
	protected SignBlockEntity blockEntity;

	@Shadow
	private int currentRow;

	protected MixinAbstractSignEditScreen(Text title) {
		super(title);
	}

	@FabInject(at=@At("TAIL"), method="init()V")
	public void init(CallbackInfo ci) {
		this.selectionManager = new SelectionManager(() -> this.text[this.currentRow], (text) -> {
			this.text[this.currentRow] = text;
			this.blockEntity.setTextOnRow(this.currentRow, Text.literal(text));
		}, SelectionManager.makeClipboardGetter(this.client), SelectionManager.makeClipboardSetter(this.client), (text) -> this.client.textRenderer.getWidth(text) <= 90) {
			@Override
			public void paste() {
				Supplier<String> supplier = FabRefl.Client.getClipboardGetter(this);
				String text = supplier.get();
				String[] lines = text.split("\r?\n");
				if (lines.length <=1) {
					super.paste();
					return;
				}
				for (int i=0; i<lines.length; i++) {
					String line = lines[i];
					FabRefl.Client.setClipboardGetter(this, () -> line);
					super.paste();
					if (i+1<lines.length) {
						currentRow = currentRow + 1 & 3;
						this.putCursorAtEnd();
					}
				}
				FabRefl.Client.setClipboardGetter(this, supplier);
			}
		};
	}

	@FabInject(at=@At("HEAD"), method="keyPressed(III)Z", cancellable=true)
	public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> ci) {
		if (keyCode == GLFW.GLFW_KEY_C && hasControlDown() && hasShiftDown() && !hasAltDown()) {
			SelectionManager.setClipboard(client, Joiner.on(Platform.isWindows() ? "\r\n" : "\n").join(text));
			ci.setReturnValue(true);
		}
	}

}
