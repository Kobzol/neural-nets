package cz.vsb.cs.neurace.gui.track;

import cz.vsb.cs.neurace.gui.Resources;
import cz.vsb.cs.neurace.race.Checkpoint;
import cz.vsb.cs.neurace.track.Surface;
import cz.vsb.cs.neurace.track.Track;
import cz.vsb.cs.neurace.track.TrackPoint;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

/**
 * Vykreslení trati
 * @author Petr Hamalčík
 */
public class TrackPainter {
    /** Trať */
    private Track track;
    
    
    private GeneralPath line;
    /** segmenty cesty */
    private LinkedList<GeneralPath> segments  = new LinkedList();
    /** povrchy cesty */
    private LinkedList<Surface> surfaces = new LinkedList();
    /** použít textury na vykreslení cesty a okolí */
    private boolean useTextures = true;
    /** textura použitá na okolí trati */
    TexturePaint enviroPaint = null;
    /** textura právě vykreslovaného segmentu cesty */
    TexturePaint roadPaint;
    /** barva středové čáry */
    private Color lineColor = Color.BLACK;
    
    private float scale = 5.0f;
    
    public TrackPainter(Track track, float scale) {
        this.scale = scale;
        setTrack(track);
    }
    
    
    
    
    public void paintRoad(Graphics2D g2) {
        
        if(useTextures) {
            Rectangle2D rect;

            //pozadí
            g2.setPaint(enviroPaint);
            Rectangle r = g2.getClipBounds();
            int xmax = (r.width < track.getWidth()*scale) ? r.width : (int)(track.getWidth()*scale);
            int ymax = (r.height < track.getHeight()*scale) ? r.height : (int)(track.getHeight()*scale);
            g2.fillRect(r.x, r.y, xmax, ymax);

            if(line == null) {
                return;
            }
            
            Stroke stroke = g2.getStroke();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int it = 0;
            
            //okraje cesty
            Color color = Color.LIGHT_GRAY;
            g2.setStroke(new BasicStroke(track.getRoadWidth()*scale+2*scale, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            g2.setColor(color);
            g2.draw(line);
            
            BufferedImage roadTex;
            for(GeneralPath segment: segments) {
                Surface surf = surfaces.get(it++);
                
                roadTex = Resources.get().getImage(surf.toString().toLowerCase());
                //cesta
                if(roadTex != null) {
                    rect = new Rectangle2D.Float(0, 0, roadTex.getWidth(), roadTex.getHeight());
                    roadPaint = new TexturePaint(roadTex, rect);
                    g2.setStroke(new BasicStroke(track.getRoadWidth()*scale, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
                    g2.setPaint(roadPaint);
                    g2.draw(segment);
                }
            }
            g2.setStroke(stroke);
        }
        else {
            if(track.getEnviroment() == Surface.GRASS) {
                g2.setBackground(new Color(20, 150, 20));
            }
            else if(track.getEnviroment() == Surface.SAND) {
                g2.setBackground(new Color(240, 240, 160));
            }
            else if(track.getEnviroment() == Surface.SNOW) {
                g2.setBackground(new Color(230, 230, 230));
            }
            else if(track.getEnviroment() == Surface.CONCRETE) {
                g2.setBackground(new Color(210, 210, 210));
            }
            g2.clearRect(0, 0, (int)(track.getWidth()*scale), (int)(track.getHeight()*scale));

            if(line == null) {
                return;
            }
            
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int it = 0;
            Stroke stroke = g2.getStroke();
            
            //okraje cesty
            Color color = Color.LIGHT_GRAY;
            g2.setStroke(new BasicStroke(track.getRoadWidth()*scale+2*scale, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            g2.setColor(color);
            g2.draw(line);
            
            for(GeneralPath segment: segments) {
                Surface surf = surfaces.get(it++);
                //cesta
                g2.setStroke(new BasicStroke(track.getRoadWidth()*scale, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
                if(surf == Surface.ASPHALT) {
                g2.setColor(new Color(100, 100, 100));
                }
                else if(surf == Surface.GRAVEL) {
                    g2.setColor(new Color(170, 170, 170));
                }
                else if(surf == Surface.DIRT) {
                    g2.setColor(new Color(180, 100, 50));
                }
                else if(surf == Surface.SAND) {
                    g2.setColor(new Color(240, 240, 150));
                }
                else if(surf == Surface.SNOW) {
                    g2.setColor(new Color(230, 230, 230));
                }
                else if(surf == Surface.ICE) {
                    g2.setColor(new Color(200, 220, 255));
                }
                g2.draw(segment);

            }
            g2.setStroke(stroke);
        }
    }
    
    
    public void drawLine(Graphics2D g2) {
        if(line != null) {
            g2.setColor(lineColor);
            g2.draw(line);
        }
    }
    
    
   /**
    * Vygeneruje trať pro pozdější vykreslení.
    */
    public void genRoad() {
            TrackPoint[] points = track.getPoints();
            if(points == null || points.length < 2) {
               segments.clear();
               surfaces.clear();
               line = new GeneralPath();
               return;
            }
            
            TrackPoint lp = null;
            GeneralPath segment = null;
            Surface s = null;
            segments.clear();
            surfaces.clear();
            for (TrackPoint p : points) {
                    if(p.getSurface() != s) {
                        if(segment != null)
                            segment.curveTo(lp.getFromX()*scale, lp.getFromY()*scale, p.getToX()*scale, p.getToY()*scale, p.getX()*scale, p.getY()*scale);
                        segments.add(segment = new GeneralPath());
                        s = p.getSurface();
                        surfaces.add(s);
                        segment.moveTo(p.getX()*scale, p.getY()*scale);
                    } else {
                        segment.curveTo(lp.getFromX()*scale, lp.getFromY()*scale, p.getToX()*scale, p.getToY()*scale, p.getX()*scale, p.getY()*scale);
                    }
                    lp = p;
            }
            if(track.isClosed()) {
                TrackPoint p = track.getPoints()[0];
                segment.curveTo(lp.getFromX()*scale, lp.getFromY()*scale, p.getToX()*scale, p.getToY()*scale, p.getX()*scale, p.getY()*scale);
            }
            //road.closePath();

            line = new GeneralPath();
            TrackPoint llp = null;
            for (TrackPoint p2 : points) {
                if (llp == null) {
                        line.moveTo(p2.getX()*scale, p2.getY()*scale);
                } else {
                        line.curveTo(llp.getFromX()*scale, llp.getFromY()*scale, p2.getToX()*scale, p2.getToY()*scale, p2.getX()*scale, p2.getY()*scale);
                }
                llp = p2;
            }
            if(track.isClosed()) {
                TrackPoint p2 = track.getPoints()[0];
                line.curveTo(llp.getFromX()*scale, llp.getFromY()*scale, p2.getToX()*scale, p2.getToY()*scale, p2.getX()*scale, p2.getY()*scale);
                line.closePath();
            }
    }
    
    
    public void drawCheckpoints(Graphics g2) {
        if(line != null) {
            g2.setColor(Color.RED);
            Checkpoint[] checkpoints = track.getCheckpoints();

            for(int i = 0; i < checkpoints.length; i++) {
                if(i == 1) g2.setColor(Color.BLUE);
                g2.drawLine((int)(checkpoints[i].getX1()*scale), (int)(checkpoints[i].getY1()*scale),
                        (int)(checkpoints[i].getX2()*scale), (int)(checkpoints[i].getY2()*scale));
            }
        }
    }

    public TexturePaint getEnviroTexturePaint() {
        return enviroPaint;
    }

    public void setEnviroPaint(Surface enviroment) {
        BufferedImage enviroTexture;
        enviroTexture = Resources.get().getImage(enviroment.toString().toLowerCase());
        if(enviroTexture != null) {
            Rectangle2D rect = new Rectangle2D.Float(0, 0, enviroTexture.getWidth(), enviroTexture.getHeight());
            enviroPaint = new TexturePaint(enviroTexture, rect);
        }
    }
    

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
        genRoad();
    }

    public Track getTrack() {
        return track;
    }

    public final void setTrack(Track track) {
        this.track = track;
        if(track != null) {
            track.updateTrack();
            genRoad();
            setEnviroPaint(track.getEnviroment());
        }
    }

    public boolean isUseTextures() {
        return useTextures;
    }

    public void setUseTextures(boolean useTextures) {
        this.useTextures = useTextures;
    }
        
    
}
