package io.github.startsmercury.visual_snowy_leaves.impl.client.sodium;

import org.lwjgl.system.MemoryUtil;

public class VslIndexAttribute {
    public static void set(long ptr, int index) {
        MemoryUtil.memPutInt(ptr, index);
    }

    public static int get(long ptr) {
        return MemoryUtil.memGetInt(ptr);
    }

    private VslIndexAttribute() {}
}
