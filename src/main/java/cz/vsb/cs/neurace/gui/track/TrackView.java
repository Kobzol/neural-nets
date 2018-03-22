package cz.vsb.cs.neurace.gui.track;

import cz.vsb.cs.neurace.gui.Config;
import cz.vsb.cs.neurace.gui.MainWindow;
import cz.vsb.cs.neurace.track.Track;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

/**
 * Zobrazí náhled na trať
 */
public class TrackView extends JComponent implements PropertyChangeListener{

	/** zobrazovaná trať */
	private Track track;
        /** vykreslování objektů */
        ObjectPainter op;
        /** vykreslování trati */
        TrackPainter tp;
        /** velikost okna náhledu [px] */
        int size;

	/**
	 * Konstruktor
	 * @param track
	 */
	public TrackView(Track track, int size) {
		this.track = track;
                this.size = size;
                float scale = 1.0f;
                op = new ObjectPainter(scale);
                op.setDrawOutline(false);
                tp = new TrackPainter(track, scale);
                tp.setUseTextures(Config.get().getBoolean("textures"));
                tp.setLineColor(Color.WHITE);
	}

	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(size, size);
	}

	@Override
	public void paintComponent(Graphics g) {
            if(track != null) {
                Graphics2D g2 = (Graphics2D) g;

                if(size != 0) {
                    float max = Math.max(track.getWidth(), track.getHeight());
                    float s = (size/max);
                    g2.scale(s, s);
                }

                tp.paintRoad(g2);
                tp.drawLine(g2);
                tp.drawCheckpoints(g2);

                op.paintObjects(track.getObjects(), g2);
            }
	}
      

	/** 
	 * Vykreslovaná trať
	 * @return
	 */
	public Track getTrack() {
		return track;
	}

        public void setTrack(Track track) {
            this.track = track;
            tp.setTrack(track);
        }
        
        
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String prop = evt.getPropertyName();
            if(prop.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                File file = (File) evt.getNewValue();
                if (file != null && file.getName().endsWith(".ntr") && file.exists()) {
                    try {
                        this.setTrack(new Track(file));
                    } catch (Exception ex) {
                        MainWindow.get().errorMsg(ex.getMessage());
                        //ex.printStackTrace();
                    }
                }
            }
            this.repaint();
        }
}
