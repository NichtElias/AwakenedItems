package party.elias.awakeneditems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ParticleUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientUtils {
    public static Level getLevel() {
        return Minecraft.getInstance().level;
    }

    private static void addHighlightParticleAt(ClientLevel level, Vec3 pos) {
        level.addParticle(AwakenedItems.HIGHLIGHT_PARTICLE.get(), true, pos.x, pos.y, pos.z, 0, 0, 0);
    }

    public static void highlightEdge(ClientLevel level, Vec3 a, Vec3 b, int resolution) {
        double step = 1f / resolution;
        for (double d = step; d < 1; d += step) {
            addHighlightParticleAt(level, a.lerp(b, d));
        }
    }

    public static void highlightBlock(BlockPos pos) {
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 east = new Vec3(1, 0, 0);
        Vec3 south = new Vec3(0, 0, 1);

        ClientLevel level = Minecraft.getInstance().level;

        if (level != null) {
            Vec3 blockOrigin = new Vec3(pos.getX(), pos.getY(), pos.getZ());
            Vec3 blockAntiOrigin = blockOrigin.add(new Vec3(1, 1, 1));

            double step = 1d / 32;

            //edges
            for (double d = step; d < 1; d += step) {
                addHighlightParticleAt(level, blockOrigin.add(up.scale(d)));
                addHighlightParticleAt(level, blockOrigin.add(east.scale(d)));
                addHighlightParticleAt(level, blockOrigin.add(south.scale(d)));

                addHighlightParticleAt(level, blockAntiOrigin.subtract(up.scale(d)));
                addHighlightParticleAt(level, blockAntiOrigin.subtract(east.scale(d)));
                addHighlightParticleAt(level, blockAntiOrigin.subtract(south.scale(d)));

                addHighlightParticleAt(level, blockOrigin.add(up).add(east.scale(d)));
                addHighlightParticleAt(level, blockOrigin.add(up).add(south.scale(d)));
                addHighlightParticleAt(level, blockOrigin.add(east).add(up.scale(d)));
                addHighlightParticleAt(level, blockOrigin.add(south).add(up.scale(d)));
                addHighlightParticleAt(level, blockAntiOrigin.subtract(up).subtract(east.scale(d)));
                addHighlightParticleAt(level, blockAntiOrigin.subtract(up).subtract(south.scale(d)));
            }

            //vertices
            addHighlightParticleAt(level, blockOrigin);
            addHighlightParticleAt(level, blockOrigin.add(up));
            addHighlightParticleAt(level, blockOrigin.add(east));
            addHighlightParticleAt(level, blockOrigin.add(south));
            addHighlightParticleAt(level, blockAntiOrigin);
            addHighlightParticleAt(level, blockAntiOrigin.subtract(up));
            addHighlightParticleAt(level, blockAntiOrigin.subtract(east));
            addHighlightParticleAt(level, blockAntiOrigin.subtract(south));
        }
    }

    private static Set<Vec3i> verticesOf(BlockPos pos) {
        Set<Vec3i> vertices = new HashSet<>();

        vertices.add(pos);
        vertices.add(pos.offset(1, 0, 0));
        vertices.add(pos.offset(0, 1, 0));
        vertices.add(pos.offset(1, 1, 0));
        vertices.add(pos.offset(0, 0, 1));
        vertices.add(pos.offset(1, 0, 1));
        vertices.add(pos.offset(0, 1, 1));
        vertices.add(pos.offset(1, 1, 1));

        return vertices;
    }

    public static void highlightBlocks(Set<BlockPos> blocks) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level != null) {
            Set<BlockEdge> edges = new HashSet<>();
            Set<Vec3i> vertices = new HashSet<>();

            for (BlockPos pos : blocks) {
                edges.addAll(BlockEdge.edgesOfBlock(pos));
                vertices.addAll(verticesOf(pos));
            }

            edges = edges.stream().filter(edge -> {
                List<BlockPos> highlighted = edge.getSurroundingBlocks().stream().filter(blocks::contains).toList();
                return highlighted.size() == 1 || highlighted.size() == 3 ||
                        (highlighted.size() == 2 && highlighted.get(0).distManhattan(highlighted.get(1)) > 1);
            }).collect(Collectors.toSet());

            for (BlockEdge edge : edges) {
                highlightEdge(level, edge.a(), edge.b(), 4);
            }

            for (Vec3i vertex : vertices) {
                if (BlockEdge.edgesFromVertex(vertex).stream().anyMatch(edges::contains)) {
                    addHighlightParticleAt(level, Vec3.atLowerCornerOf(vertex));
                }
            }
        }
    }

    static class BlockEdge {
        private final BlockPos origin;
        private final Direction.Axis axis;

        public BlockEdge(BlockPos origin, Direction direction) {
            if (direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
                this.origin = origin.relative(direction);
            } else {
                this.origin = origin;
            }
            this.axis = direction.getAxis();
        }

        public Set<BlockPos> getSurroundingBlocks() {
            HashSet<BlockPos> blocks = new HashSet<>();

            switch (axis) {
                case X -> {
                    blocks.add(origin);
                    blocks.add(origin.north());
                    blocks.add(origin.below());
                    blocks.add(origin.north().below());
                }
                case Y -> {
                    blocks.add(origin);
                    blocks.add(origin.north());
                    blocks.add(origin.west());
                    blocks.add(origin.north().west());
                }
                case Z -> {
                    blocks.add(origin);
                    blocks.add(origin.west());
                    blocks.add(origin.below());
                    blocks.add(origin.west().below());
                }
            }

            return blocks;
        }

        public static Set<BlockEdge> edgesOfBlock(BlockPos pos) {
            HashSet<BlockEdge> edges = new HashSet<>();

            edges.add(new BlockEdge(pos, Direction.UP));
            edges.add(new BlockEdge(pos, Direction.EAST));
            edges.add(new BlockEdge(pos, Direction.SOUTH));

            edges.add(new BlockEdge(pos.above(), Direction.EAST));
            edges.add(new BlockEdge(pos.above(), Direction.SOUTH));

            edges.add(new BlockEdge(pos.east(), Direction.SOUTH));
            edges.add(new BlockEdge(pos.east(), Direction.UP));

            edges.add(new BlockEdge(pos.south(), Direction.EAST));
            edges.add(new BlockEdge(pos.south(), Direction.UP));

            edges.add(new BlockEdge(pos.offset(1, 1, 1), Direction.DOWN));
            edges.add(new BlockEdge(pos.offset(1, 1, 1), Direction.WEST));
            edges.add(new BlockEdge(pos.offset(1, 1, 1), Direction.NORTH));

            return edges;
        }

        public static Set<BlockEdge> edgesFromVertex(Vec3i vertex) {
            HashSet<BlockEdge> edges = new HashSet<>();

            edges.add(new BlockEdge(new BlockPos(vertex), Direction.UP));
            edges.add(new BlockEdge(new BlockPos(vertex), Direction.DOWN));
            edges.add(new BlockEdge(new BlockPos(vertex), Direction.EAST));
            edges.add(new BlockEdge(new BlockPos(vertex), Direction.WEST));
            edges.add(new BlockEdge(new BlockPos(vertex), Direction.SOUTH));
            edges.add(new BlockEdge(new BlockPos(vertex), Direction.NORTH));

            return edges;
        }

        public Vec3 a() {
            return Vec3.atLowerCornerOf(origin);
        }

        public Vec3 b() {
            return Vec3.atLowerCornerOf(origin.relative(axis, 1));
        }

        public boolean containsVertex(Vec3i vertex) {
            return origin.equals(vertex) || origin.relative(axis, 1).equals(vertex);
        }

        @Override
        public int hashCode() {
            return this.origin.hashCode() + this.axis.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BlockEdge edge) {
                return this.origin.equals(edge.origin) && this.axis == edge.axis;
            }
            return false;
        }
    }
}
