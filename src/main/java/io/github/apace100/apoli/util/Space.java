package io.github.apace100.apoli.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public enum Space {
    WORLD,
    LOCAL, LOCAL_HORIZONTAL, LOCAL_HORIZONTAL_NORMALIZED,
    VELOCITY, VELOCITY_NORMALIZED, VELOCITY_HORIZONTAL, VELOCITY_HORIZONTAL_NORMALIZED;

    /**
     * @author Alluysl
     * Provides the matrix transform from the base specified by the input vector to the cardinal base.
     * The input vector is the Z (forward) axis of the base, while the calculated X axis is orthogonal to the "left" of Z. Y is such as Z is the cross product of X and Y.
     * If the input vector were to be vertical, the yaw is used to infer the X and Y vectors of the base.
     * After determining the vectors of the base it builds the transformation matrix by laying each into a column (if you consider vectors as being in columns for multiplications).
     * @param vector the input vector the base is inferred from (forward vector of local space)
     * @param yaw the yaw of local space
     * @return the transformation matrix from local to global space
     * */
    private static Matrix3f getBaseTransformMatrixFromNormalizedDirectionVector(Vec3d vector, float yaw){
        double xX, xZ, // X vector
            zX = 0.0D, zY = vector.getY(), zZ = 0.0D; // Z vector

        if (Math.abs(zY) != 1.0F) { // Z not vertical, can infer X from it
            // Z
            zX = vector.getX();
            zZ = vector.getZ();
            // X (orthogonal to the projection of Z on the global XZ plane)
            xX = vector.getZ();
            xZ = -vector.getX();
            // Normalize X
            float xFactor = (float)(1 / Math.sqrt(xX * xX + xZ * xZ));
            xX *= xFactor;
            xZ *= xFactor;
        } else {
            // If the orientation vector points straight up or straight down, use the yaw to determine the X vector (it's "on the left")
            // The pitch doesn't affect the X vector as it's a rotation around that same vector
            float trigonometricYaw = -yaw * 0.0174532925F; // pi / 180 = 0.0174532925
            xX = MathHelper.cos(trigonometricYaw);
            xZ = -MathHelper.sin(trigonometricYaw);
        }

        Matrix3f res = new Matrix3f();
        // X
        res.set(0, 0, (float)xX);
        res.set(1, 0, 0.0F); // X vector is horizontal, set its Y component (a10 (mathematically a21)) to 0
        res.set(2, 0, (float)xZ);
        // Y (cross product of Z and X, simplified by the fact that X has a Y component of 0)
        res.set(0, 1, (float)(zY * xZ));
        res.set(1, 1, (float)(zZ * xX - zX * xZ));
        res.set(2, 1, (float)(-zY * xX));
        // Z
        res.set(0, 2, (float)zX);
        res.set(1, 2, (float)zY);
        res.set(2, 2, (float)zZ);
        return res;
    }

    /**
     * @author Alluysl
     * Transforms a vector from local space to global space. The base inferred from its forward vector is orthogonal.
     * @param baseForwardVector the base's forward (Z) vector
     * @param vector the vector to transform
     * @param baseYaw the yaw of the base (used in case the forward vector lacks information to infer the base)
     * @param normalizeBase whether to normalize the base, if so all three vectors of the base will be normalized, otherwise they'll all have the length of the input forward vector
     * */
    public static void transformVectorToBase(Vec3d baseForwardVector, Vector3f vector, float baseYaw, boolean normalizeBase) {

        double baseScaleD = baseForwardVector.length();
        if (baseScaleD <= 0.007D){ // tweak value if too high, may be a bit too aggressive
            vector.zero();
        } else {
            float baseScale = (float)baseScaleD;

            Vec3d normalizedBase = baseForwardVector.normalize(); // the function called below assumes the base is normalized to simplify calculations (Y calculated as cross product of Z and X guaranteed to be normalized if X and Z are normalized)

            Matrix3f transformMatrix = getBaseTransformMatrixFromNormalizedDirectionVector(normalizedBase, baseYaw);
            if (!normalizeBase) // if the base wasn't supposed to get normalized, re-scale to compensate for the prior normalization
                transformMatrix.scale(baseScale, baseScale, baseScale);
            vector.mul(transformMatrix); // matrix multiplication, vector is now in the new base :D
        }
    }

    /**
     * @author apace100
     * @author Alluysl
     * Transforms a vector from the local space of this instance to global space.
     * The "local" space may be world space (no transformation), or relative to the entity's facing (LOCAL), its velocity (VELOCITY), et cetera
     * @param vector the vector to transform
     * @param entity the entity to align the local space to
     * */
    public void toGlobal(Vector3f vector, Entity entity){
        Vec3d baseForwardVector;

        switch (this){

            case WORLD:
                break;

            case LOCAL:
            case LOCAL_HORIZONTAL:
            case LOCAL_HORIZONTAL_NORMALIZED:
                baseForwardVector = entity.getRotationVector();
                if (this != LOCAL) // horizontal
                    baseForwardVector = new Vec3d(baseForwardVector.getX(), 0, baseForwardVector.getZ());
                transformVectorToBase(baseForwardVector, vector, entity.getYaw(), this == LOCAL_HORIZONTAL_NORMALIZED);
                break;

            case VELOCITY:
            case VELOCITY_NORMALIZED:
            case VELOCITY_HORIZONTAL:
            case VELOCITY_HORIZONTAL_NORMALIZED:
                baseForwardVector = entity.getVelocity();
                if (this == VELOCITY_HORIZONTAL || this == VELOCITY_HORIZONTAL_NORMALIZED)
                    baseForwardVector = new Vec3d(baseForwardVector.getX(), 0, baseForwardVector.getZ());
                transformVectorToBase(baseForwardVector, vector, entity.getYaw(), this == VELOCITY_NORMALIZED || this == VELOCITY_HORIZONTAL_NORMALIZED);
                break;
        }
    }
}
