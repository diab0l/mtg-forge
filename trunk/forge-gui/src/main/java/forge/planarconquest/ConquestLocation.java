package forge.planarconquest;

import java.util.ArrayList;
import java.util.List;

import forge.planarconquest.ConquestPlane.Region;

public class ConquestLocation {
    private ConquestPlane plane;
    private int regionIndex;
    private int row;
    private int col;
    private List<ConquestLocation> neighbors;

    public ConquestLocation() {
    }

    public ConquestLocation(ConquestPlane plane0, int regionIndex0, int row0, int col0) {
        plane = plane0;
        regionIndex = regionIndex0;
        row = row0;
        col = col0;
    }

    public ConquestPlane getPlane() {
        return plane;
    }

    public Region getRegion() {
        if (regionIndex == -1 || regionIndex == plane.getRegions().size()) {
            return null; //indicates we're on portal row
        }
        return plane.getRegions().get(regionIndex);
    }

    public int getRegionIndex() {
        return regionIndex;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public List<ConquestLocation> getNeighbors() {
        if (neighbors == null) { //cache neighbors for performance
            neighbors = getNeighbors(plane, regionIndex, row, col);
        }
        return neighbors;
    }

    public static List<ConquestLocation> getNeighbors(ConquestPlane plane0, int regionIndex0, int row0, int col0) {
        int regionCount = plane0.getRegions().size();
        List<ConquestLocation> locations = new ArrayList<ConquestLocation>();

        //add location above
        if (row0 < Region.ROWS_PER_REGION - 1) {
            locations.add(new ConquestLocation(plane0, regionIndex0, row0 + 1, col0));
        }
        else if (regionIndex0 < regionCount - 1) {
            locations.add(new ConquestLocation(plane0, regionIndex0 + 1, 0, col0));
        }

        //add location below
        if (row0 > 0) {
            locations.add(new ConquestLocation(plane0, regionIndex0, row0 - 1, col0));
        }
        else if (regionIndex0 > 0) {
            locations.add(new ConquestLocation(plane0, regionIndex0 - 1, Region.ROWS_PER_REGION - 1, col0));
        }

        //add location to left
        if (col0 > 0) {
            locations.add(new ConquestLocation(plane0, regionIndex0, row0, col0 - 1));
        }

        //add location to right
        if (col0 < Region.COLS_PER_REGION - 1) {
            locations.add(new ConquestLocation(plane0, regionIndex0, row0, col0 + 1));
        }

        return locations;
    }
}
