package chihalu.building.support.mixin.client;

import java.util.Collection;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CreativeInventoryScreen.class)
public interface CreativeInventoryScreenInvoker {
	@Invoker("setSelectedTab")
	void building_support$setSelectedTab(ItemGroup group);

	@Invoker("refreshSelectedTab")
	void building_support$refreshSelectedTab(Collection<ItemStack> stacks);

	@Accessor("selectedTab")
	static ItemGroup building_support$getSelectedTab() {
		throw new AssertionError();
	}
}
