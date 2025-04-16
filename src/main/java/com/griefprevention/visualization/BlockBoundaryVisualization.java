package com.griefprevention.visualization;

import com.griefprevention.util.IntVector;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.util.BoundingBox;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

public abstract class BlockBoundaryVisualization extends BoundaryVisualization
{

    private final int step;
    private final BoundingBox displayZoneArea;
    protected final Collection<BlockElement> elements = new HashSet<>();

    /**
     * Construct a new {@code BlockBoundaryVisualization} with a step size of {@code 1} and a display radius of
     * {@code 75}.
     *
     * @param world the {@link World} being visualized in
     * @param visualizeFrom the {@link IntVector} representing the world coordinate being visualized from
     * @param height the height of the visualization
     */
    protected BlockBoundaryVisualization(@NotNull World world, @NotNull IntVector visualizeFrom, int height)
    {
        this(world, visualizeFrom, height, 1, 75);
    }

    /**
     * Construct a new {@code BlockBoundaryVisualization}.
     *
     * @param world the {@link World} being visualized in
     * @param visualizeFrom the {@link IntVector} representing the world coordinate being visualized from
     * @param height the height of the visualization
     * @param step the distance between individual side elements
     * @param displayZoneRadius the radius in which elements are visible from the visualization location
     */
    protected BlockBoundaryVisualization(
            @NotNull World world,
            @NotNull IntVector visualizeFrom,
            int height,
            int step,
            int displayZoneRadius)
    {
        super(world, visualizeFrom, height);
        this.step = step;
        this.displayZoneArea = new BoundingBox(
                visualizeFrom.add(-displayZoneRadius, -displayZoneRadius, -displayZoneRadius),
                visualizeFrom.add(displayZoneRadius, displayZoneRadius, displayZoneRadius));
    }

    @Override
    protected void apply(@NotNull Player player, @NotNull PlayerData playerData) {
        super.apply(player, playerData);
        elements.forEach(element -> element.draw(player, world));
    }

    @Override
    protected void draw(@NotNull Player player, @NotNull Boundary boundary)
    {
        BoundingBox area = boundary.bounds();
        BoundingBox displayZone = displayZoneArea;

        Consumer<IntVector> addCorner = addCornerElements(boundary);
        Consumer<IntVector> addSide = addSideElements(boundary);

        // Add sides first so corners can override them.
        if (area.getLength() > 2)
        {
            for (int x = area.getMinX() + 1; x < area.getMaxX(); x++)
            {
                if (x == area.getMinX() + 1 || x == area.getMaxX() - 1) {
                    // Bloki przy rogach
                    addDisplayed(displayZone, new IntVector(x, height, area.getMinZ()), addCornerAdjacentElements(boundary));
                    addDisplayed(displayZone, new IntVector(x, height, area.getMaxZ()), addCornerAdjacentElements(boundary));
                } else {
                    addDisplayed(displayZone, new IntVector(x, height, area.getMinZ()), addSide);
                    addDisplayed(displayZone, new IntVector(x, height, area.getMaxZ()), addSide);
                }
            }
        }

        if (area.getWidth() > 2)
        {
            for (int z = area.getMinZ() + 1; z < area.getMaxZ(); z++)
            {
                if (z == area.getMinZ() + 1 || z == area.getMaxZ() - 1) {
                    // Bloki przy rogach
                    addDisplayed(displayZone, new IntVector(area.getMinX(), height, z), addCornerAdjacentElements(boundary));
                    addDisplayed(displayZone, new IntVector(area.getMaxX(), height, z), addCornerAdjacentElements(boundary));
                } else {
                    addDisplayed(displayZone, new IntVector(area.getMinX(), height, z), addSide);
                    addDisplayed(displayZone, new IntVector(area.getMaxX(), height, z), addSide);
                }
            }
        }

        // Add corners last to override any other elements created by very small claims.
        addDisplayed(displayZone, new IntVector(area.getMinX(), height, area.getMaxZ()), addCorner);
        addDisplayed(displayZone, new IntVector(area.getMaxX(), height, area.getMaxZ()), addCorner);
        addDisplayed(displayZone, new IntVector(area.getMinX(), height, area.getMinZ()), addCorner);
        addDisplayed(displayZone, new IntVector(area.getMaxX(), height, area.getMinZ()), addCorner);

        // Add high corners and borders 30 blocks above the claim
        int highY = height + 30;
        
        // Add high corners
        addDisplayedForced(new IntVector(area.getMinX(), highY, area.getMaxZ()), addHighCornerElements(boundary));
        addDisplayedForced(new IntVector(area.getMaxX(), highY, area.getMaxZ()), addHighCornerElements(boundary));
        addDisplayedForced(new IntVector(area.getMinX(), highY, area.getMinZ()), addHighCornerElements(boundary));
        addDisplayedForced(new IntVector(area.getMaxX(), highY, area.getMinZ()), addHighCornerElements(boundary));

        // Add high borders
        if (area.getLength() > 2)
        {
            for (int x = area.getMinX() + 1; x < area.getMaxX(); x++)
            {
                addDisplayedForced(new IntVector(x, highY, area.getMinZ()), addHighSideElements(boundary));
                addDisplayedForced(new IntVector(x, highY, area.getMaxZ()), addHighSideElements(boundary));
            }
        }

        if (area.getWidth() > 2)
        {
            for (int z = area.getMinZ() + 1; z < area.getMaxZ(); z++)
            {
                addDisplayedForced(new IntVector(area.getMinX(), highY, z), addHighSideElements(boundary));
                addDisplayedForced(new IntVector(area.getMaxX(), highY, z), addHighSideElements(boundary));
            }
        }
    }

    protected Block getVisibleLocation(@NotNull IntVector vector)
    {
        Block block = vector.toBlock(world);
        BlockFace direction = (isTransparent(block)) ? BlockFace.DOWN : BlockFace.UP;

        while (block.getY() >= world.getMinHeight() &&
                block.getY() < world.getMaxHeight() - 1 &&
                (!isTransparent(block.getRelative(BlockFace.UP)) || isTransparent(block)))
        {
            block = block.getRelative(direction);
        }

        return block;
    }

    protected boolean isTransparent(@NotNull Block block)
    {
        Material blockMaterial = block.getType();

        if (blockMaterial.isAir() || blockMaterial == Material.WATER || 
            Tag.FENCES.isTagged(blockMaterial) || Tag.FENCE_GATES.isTagged(blockMaterial) ||
            Tag.SIGNS.isTagged(blockMaterial) || Tag.WALLS.isTagged(blockMaterial) ||
            Tag.WALL_SIGNS.isTagged(blockMaterial))
            return true;

        return block.getType().isTransparent();
    }

    /**
     * Create a {@link Consumer} that adds a corner element for the given {@link IntVector}.
     *
     * @param boundary the {@code Boundary}
     * @return the corner element consumer
     */
    protected abstract @NotNull Consumer<@NotNull IntVector> addCornerElements(@NotNull Boundary boundary);

    /**
     * Create a {@link Consumer} that adds a side element for the given {@link IntVector}.
     *
     * @param boundary the {@code Boundary}
     * @return the side element consumer
     */
    protected abstract @NotNull Consumer<@NotNull IntVector> addSideElements(@NotNull Boundary boundary);

    /**
     * Create a {@link Consumer} that adds elements adjacent to corners.
     *
     * @param boundary the {@code Boundary}
     * @return the corner adjacent element consumer
     */
    protected abstract @NotNull Consumer<@NotNull IntVector> addCornerAdjacentElements(@NotNull Boundary boundary);

    protected boolean isAccessible(@NotNull BoundingBox displayZone, @NotNull IntVector coordinate)
    {
        return displayZone.contains2d(coordinate) && coordinate.isChunkLoaded(world);
    }

    /**
     * Add a display element if accessible.
     *
     * @param displayZone the zone in which elements may be displayed
     * @param coordinate the coordinate being displayed
     * @param addElement the function for obtaining the element displayed
     */
    protected void addDisplayed(
            @NotNull BoundingBox displayZone,
            @NotNull IntVector coordinate,
            @NotNull Consumer<@NotNull IntVector> addElement)
    {
        if (isAccessible(displayZone, coordinate)) {
            addElement.accept(coordinate);
        }
    }

    /**
     * Add a display element without checking display zone.
     */
    protected void addDisplayedForced(@NotNull IntVector coordinate, @NotNull Consumer<@NotNull IntVector> addElement)
    {
        if (coordinate.isChunkLoaded(world)) {
            addElement.accept(coordinate);
        }
    }

    /**
     * Create a {@link Consumer} that adds high corner elements.
     *
     * @param boundary the {@code Boundary}
     * @return the high corner element consumer
     */
    protected abstract @NotNull Consumer<@NotNull IntVector> addHighCornerElements(@NotNull Boundary boundary);

    /**
     * Create a {@link Consumer} that adds high side elements.
     *
     * @param boundary the {@code Boundary}
     * @return the high side element consumer
     */
    protected abstract @NotNull Consumer<@NotNull IntVector> addHighSideElements(@NotNull Boundary boundary);

    @Override
    public void revert(@Nullable Player player)
    {
        // If the player cannot visualize the blocks, they should already be effectively reverted.
        if (!canVisualize(player))
        {
            return;
        }

        // Elements do not track the boundary they're attached to - all elements are reverted individually instead.
        this.elements.forEach(element -> element.erase(player, world));
    }

    @Override
    protected void erase(@NotNull Player player, @NotNull Boundary boundary)
    {
        this.elements.forEach(element -> element.erase(player, world));
    }

}
