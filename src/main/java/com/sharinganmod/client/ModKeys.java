package com.sharinganmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ModKeys {
    private ModKeys() {}

    public static final String CATEGORY = "key.categories.sharingan";

    public static final KeyMapping SCREEN = new KeyMapping("key.sharingan.screen", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY);
    public static final KeyMapping SHARINGAN = new KeyMapping("key.sharingan.sharingan", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);
    public static final KeyMapping MANGEKYOU = new KeyMapping("key.sharingan.mangekyou", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY);
    public static final KeyMapping SKILL1 = new KeyMapping("key.sharingan.skill1", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, CATEGORY);
    public static final KeyMapping SKILL2 = new KeyMapping("key.sharingan.skill2", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY);
    public static final KeyMapping SKILL3 = new KeyMapping("key.sharingan.skill3", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY);
    public static final KeyMapping SKILL4 = new KeyMapping("key.sharingan.skill4", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY);
}
