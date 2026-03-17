package net/fabricmc/example/

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionsC2SPacket;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class SecurityTestMod implements ClientModInitializer {
    public static boolean desyncActive = false;
    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.security.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, "category.security"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) { client.setScreen(new SecurityMenu()); }
        });
    }

    public static class SecurityMenu extends Screen {
        public SecurityMenu() { super(Text.literal("NBT-Swap Experiment")); }

        @Override
        protected void init() {
            int x = this.width / 2 - 60;
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Desync: " + (desyncActive ? "ON" : "OFF")), b -> {
                desyncActive = !desyncActive;
            }).dimensions(x, 40, 120, 20).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Execute Swap"), b -> {
                var handler = MinecraftClient.getInstance().getNetworkHandler();
                if (handler != null) {
                    // Packet 1: Survival "Data Gate"
                    handler.sendPacket(new RecipeCategoryOptionsC2SPacket(RecipeBookCategory.CRAFTING, true, true));
                    // Packet 2: Experimental Creative Request
                    handler.sendPacket(new CreativeInventoryActionC2SPacket(36, new ItemStack(Items.COMMAND_BLOCK)));
                }
            }).dimensions(x, 70, 120, 20).build());
        }
    }
}
