package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.hysky.skyblocker.injected.AlignedText;
import de.hysky.skyblocker.utils.render.gui.AlignedOrderedText;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(MutableText.class)
public abstract class MutableTextMixin implements AlignedText, Text {
	//There's an implicit linked list here, where each text has a reference to the next one.
	//With the addition of firstOfChain it becomes a doubly linked list with the caveat of the reference of each text being the first of the chain rather than the previous element.
	//There's no need for a real doubly linked list with references to the previous elements, as the only operation that needs to be done is to get the first element of the chain to render the whole chain properly.
	@Unique
	@Nullable
	private MutableText alignedWith = null;
	@Unique
	private int xOffset = Integer.MIN_VALUE;

	// Null if this is the first of the chain, not null otherwise & always points to the first of the aligned text chain
	@Unique
	private MutableText firstOfChain = null;

	@Unique
	Logger logger = LoggerFactory.getLogger("Skyblocker Mutable Text");

	@Override
	public @NotNull MutableText align(@NotNull MutableText text, int xOffset) {
		this.alignedWith = text;
		if (firstOfChain == null) {
			text.setFirstOfChain((MutableText) (Object) this);
			text.setXOffset(xOffset);
			this.xOffset = 0;
		} else {
			text.setFirstOfChain(firstOfChain);
			text.setXOffset(xOffset);
		}
		return text;
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
	public void setXOffset(int xOffset) {
		this.xOffset = xOffset;
	}

	@Override
	public MutableText getFirstOfChain() {
		return firstOfChain;
	}

	@Override
	public void setFirstOfChain(MutableText text) {
		firstOfChain = text;
	}

	@WrapOperation(method = "asOrderedText", at = @At(target = "Lnet/minecraft/util/Language;reorder(Lnet/minecraft/text/StringVisitable;)Lnet/minecraft/text/OrderedText;", value = "INVOKE"))
	private OrderedText skyblocker$asOrderedText(Language instance, StringVisitable visitable, Operation<OrderedText> original) {
		if (visitable instanceof MutableText mutableText) {
			MutableText tmp = mutableText.getFirstOfChain();
			if (tmp != null) {
				List<AlignedOrderedText.Segment> segments = new ArrayList<>();
				while (tmp != null) {
					segments.add(new AlignedOrderedText.Segment(original.call(instance, tmp), tmp.getXOffset()));
					tmp = tmp.getAlignedText();
				}
				return new AlignedOrderedText(segments);
			} else { // This is the first of the chain
				tmp = mutableText.getAlignedText();
				if (tmp != null) {
					List<AlignedOrderedText.Segment> segments = new ArrayList<>();
					segments.add(new AlignedOrderedText.Segment(original.call(instance, mutableText), 0));
					while (tmp != null) {
						segments.add(new AlignedOrderedText.Segment(original.call(instance, tmp), tmp.getXOffset()));
						tmp = tmp.getAlignedText();
					}
					return new AlignedOrderedText(segments);
				}
			}
		}
		return original.call(instance, visitable);
	}
}
