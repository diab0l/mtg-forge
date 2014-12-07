package forge.planarconquest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forge.planarconquest.ConquestPlane.Region;

public class ConquestPlaneData {
    private final List<ConquestCommander> commanders = new ArrayList<ConquestCommander>();
    private final Map<Region, RegionData> regionDataLookup = new HashMap<Region, RegionData>();

    private int wins, losses;
    private int winStreakBest = 0;
    private int winStreakCurrent = 0;

    public List<ConquestCommander> getCommanders() {
        return commanders;
    }

    public void addWin() {
        wins++;
        winStreakCurrent++;
        if (winStreakCurrent > winStreakBest) {
            winStreakBest = winStreakCurrent;
        }
    }

    public void addLoss() {
        losses++;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getWinStreakBest() {
        return winStreakBest;
    }

    public int getWinStreakCurrent() {
        return winStreakCurrent;
    }

    public RegionData getRegionData(Region region) {
        RegionData regionData = regionDataLookup.get(region);
        if (regionData == null) {
            regionData = new RegionData(region);
            regionDataLookup.put(region, regionData);
        }
        return regionData;
    }

    public class RegionData {
        private final Region region;
        private final ConquestCommander[] opponents = new ConquestCommander[3];
        private ConquestCommander deployedCommander;

        private RegionData(Region region0) {
            region = region0;
            opponents[0] = region.getRandomOpponent(opponents);
            opponents[1] = region.getRandomOpponent(opponents);
            opponents[2] = region.getRandomOpponent(opponents);
        }

        public Region getRegion() {
            return region;
        }

        public ConquestCommander getOpponent(int index) {
            return opponents[index];
        }

        public void replaceOpponent(int index) {
            opponents[index] = region.getRandomOpponent(opponents);
        }

        public ConquestCommander getDeployedCommander() {
            return deployedCommander;
        }
        public void setDeployedCommander(ConquestCommander commander) {
            if (deployedCommander != null && deployedCommander.getDeployedRegion() == region) {
                deployedCommander.setDeployedRegion(null);
            }
            deployedCommander = commander;
            if (deployedCommander != null) {
                deployedCommander.setDeployedRegion(region);
            }
        }
    }
}
