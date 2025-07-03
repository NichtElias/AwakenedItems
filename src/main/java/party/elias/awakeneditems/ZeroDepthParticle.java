package party.elias.awakeneditems;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class ZeroDepthParticle extends TextureSheetParticle {

    private static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public @Nullable BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(() -> AwakenedItems.highlight_particle_shader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.disableDepthTest();
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public String toString() {
            return "NO_DEPTH";
        }
    };

    private final SpriteSet spriteSet;

    protected ZeroDepthParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.quadSize = 0.2f;
        this.spriteSet = spriteSet;
        this.setSpriteFromAge(spriteSet);
        this.hasPhysics = false;
        this.gravity = 0;
        this.setParticleSpeed(xSpeed * (Math.random() * 2.0 - 1.0), ySpeed * (Math.random() * 2.0 - 1.0), zSpeed * (Math.random() * 2.0 - 1.0));
        this.setLifetime(1);
        this.setColor((float)0x55 / 0xFF, 1, 1);
    }

    @Override
    public void tick() {
        this.setSpriteFromAge(spriteSet);
        super.tick();
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 15 << 20 | 15 << 4;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RENDER_TYPE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ZeroDepthParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet).scale(0.2f);
        }
    }
}
