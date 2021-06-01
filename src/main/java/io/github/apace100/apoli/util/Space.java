package io.github.apace100.apoli.util;

import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public enum Space {
    WORLD, LOCAL, LOCAL_HORIZONTAL, VELOCITY, VELOCITY_NORMALIZED, VELOCITY_HORIZONTAL, VELOCITY_HORIZONTAL_NORMALIZED;

    public static void rotateVectorToBase(Vec3d newBase, Vec3f vector) {
        Vec3d globalForward = new Vec3d(0, 0, 1);
        Vec3d v = globalForward.crossProduct(newBase).normalize();
        double c = Math.acos(globalForward.dotProduct(newBase));
        Quaternion quat = new Quaternion(new Vec3f((float)v.x, (float)v.y, (float)v.z), (float)c, false);
        vector.rotate(quat);
    }
}
