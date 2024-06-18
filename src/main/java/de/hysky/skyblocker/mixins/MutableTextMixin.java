package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.injected.AlignedText;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MutableText.class)
public abstract class MutableTextMixin implements AlignedText {
	@Unique
	@Nullable
	private MutableText alignedWith = null;
	@Unique
	private int xOffset = Integer.MIN_VALUE;
	@Unique
	// Null if this is the first of the chain, not null otherwise & always points to the first of the aligned text chain
	private MutableText firstOfChain = null;

	@Override
	public @NotNull MutableText chainAlign(@NotNull MutableText text, int xOffset) {
		this.alignedWith = text;
		this.xOffset = xOffset;
		if (firstOfChain == null) text.setFirstOfChain((MutableText) (Object) this);
		else text.setFirstOfChain(firstOfChain);
		return text;
	}

	@Override
	public @NotNull MutableText align(@NotNull MutableText text, int xOffset) {
		this.alignedWith = text;
		this.xOffset = xOffset;
		return (MutableText) (Object) this;
	}

	@Override
	public @Nullable MutableText getAlignedText() {
		return alignedWith;
	}

	@Override
	public int getXOffset() {
		return xOffset;
	}

	@Override
	public MutableText getFirstOfChain() {
		return firstOfChain;
	}

	@Override
	public void setFirstOfChain(MutableText text) {
		firstOfChain = text;
	}
}
