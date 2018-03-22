package cz.vsb.cs.neurace.gui;

import cz.vsb.cs.neurace.gui.LeaderboardTab.Table;
import java.util.List;

/**
 *
 * @author Petr Hamalcik
 */
public interface DBListener {
    public void updateCompleted(Table table, List<String[]> data, String msg);
}
