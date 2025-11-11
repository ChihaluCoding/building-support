package chihalu.building.support.mixin.client;

import chihalu.building.support.customtabs.CustomTabsManager;
import chihalu.building.support.config.BuildingSupportConfig;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import chihalu.building.support.client.accessor.ItemGroupIconAccessor;

@Mixin(ItemGroup.class)
public abstract class ItemGroupMixin implements ItemGroupIconAccessor {
	@Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
	private void utility_toolkit$overrideCustomTabName(CallbackInfoReturnable<Text> cir) {
		ItemGroup self = (ItemGroup) (Object) this;
		if (CustomTabsManager.getInstance().isCustomTabGroup(self)) {
			String name = BuildingSupportConfig.getInstance().getCustomTabName();
			cir.setReturnValue(Text.literal(name));
		}
	}

	@Accessor("icon")
	@Mutable
	protected abstract void utility_toolkit$setIcon(ItemStack stack);

	@Override
	public void utility_toolkit$resetIconCache() {
		utility_toolkit$setIcon(null);
	}
}
