package dev.lazurite.rayon.examplemod.render;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import dev.lazurite.rayon.examplemod.ExampleMod;
import dev.lazurite.rayon.examplemod.entity.LivingCubeEntity;
import dev.lazurite.rayon.examplemod.render.model.RectangularPrismModel;
import dev.lazurite.rayon.impl.util.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Quaternion;

@Environment(EnvType.CLIENT)
public class LivingCubeEntityRenderer extends EntityRenderer<LivingCubeEntity> {
    public static final Identifier texture = new Identifier(ExampleMod.MODID, "textures/entity/rectangular_prism.png");
    private final RectangularPrismModel model;

    public LivingCubeEntityRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
        this.shadowRadius = 0.2F;
        this.model = new RectangularPrismModel(16, 16, 16);
        this.model.child = false;
    }

    public void render(LivingCubeEntity cubeEntity, float yaw, float delta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        Vector3f bounds = cubeEntity.getRigidBody().getCollisionShape().boundingBox(new Vector3f(), new com.jme3.math.Quaternion(), new BoundingBox()).getExtent(new Vector3f()).multLocal(-1);
        Quaternion rot = QuaternionHelper.bulletToMinecraft(cubeEntity.getPhysicsRotation(new com.jme3.math.Quaternion(), delta));

        matrixStack.push();
        matrixStack.multiply(rot);
        matrixStack.translate(bounds.x, bounds.y, bounds.z);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(model.getLayer(this.getTexture(cubeEntity)));
        model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.pop();

        super.render(cubeEntity, yaw, delta, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public boolean shouldRender(LivingCubeEntity cubeEntity, Frustum frustum, double x, double y, double z) {
        return cubeEntity.shouldRender(x, y, z);
    }

    @Override
    public Identifier getTexture(LivingCubeEntity cubeEntity) {
        return texture;
    }
}