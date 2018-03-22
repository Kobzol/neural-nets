package cz.vsb.cs.neurace.gui.track;

import cz.vsb.cs.neurace.gui.Resources;
import cz.vsb.cs.neurace.track.BlockObject;
import cz.vsb.cs.neurace.track.CylinderObject;
import cz.vsb.cs.neurace.track.TrackObject;
import cz.vsb.cs.neurace.track.WallObject;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;


/**
 * Vykreslení objektů
 * @author Petr Hamalčík
 */
public class ObjectPainter {
    /** měřítko, ve kterém jsou vykresleny objekty */
    private float scale;
    /** vykreslování obrysu kolizního modelu */
    private boolean drawOutline = true;
    /**  vykreslování křížku ve středu objektu */
    private boolean drawCross = false;


    public ObjectPainter(float scale) {
        this.scale = scale;
    }

    /**
     * Nastaví vykreslování křížku ve středu objektu
     */
    public void setDrawCross(boolean value) {
        this.drawCross = value;
    }

    /**
     * Nastaví vykreslování obrysu kolizního modelu
     */
    public void setDrawOutline(boolean value) {
        this.drawOutline = value;
    }

    public boolean getDrawCross() {
        return drawCross;
    }

    public boolean getDrawOutline() {
        return drawOutline;
    }

    
    /**
     * Vykreslí všechny objekty na trati
     * @param list seznam objektů
     * @param g grafika
     */
    public void paintObjects(List<TrackObject> list, Graphics g) {
        for(TrackObject obj : list) {
            paintObject(obj, g);
        }
    }

    /**
     * Vykreslí objekt na trati
     * @param obj objekt
     * @param g grafika
     */
    public void paintObject(TrackObject obj, Graphics g) {
        if(obj.getClass() == WallObject.class) {
            paintWall((WallObject) obj, g);
        }
        else {
            Vector2f size = Resources.get().getImageSize(obj.getType());
            Graphics2D g2 = (Graphics2D)g;
            
            AffineTransform start = g2.getTransform();
            g2.translate((int)(obj.getPosX()*scale), (int)(obj.getPosY()*scale));
            if(obj.getClass() == BlockObject.class) {
                BlockObject block = (BlockObject) obj;
                g2.rotate(block.getAngle());
            }
            BufferedImage img = Resources.get().getImage(obj.getType());
            if(img != null) {
                g2.drawImage(img, 0 - (int)(size.x/2*scale), 0 - (int)(size.y/2*scale),
                        (int)(size.x*scale), (int)(size.y*scale), null);
            }
            else {
                System.err.println("Missing image: " + obj.getType());
            }
            int x = 0;
            int y = 0;

            //středový křížek
            if(drawCross) {
                
                int cSize = 3;
                g2.setColor(Color.LIGHT_GRAY);
                //g2.setColor(Color.BLACK);
                g2.drawLine(x-cSize, y, x+cSize, y);
                g2.drawLine(x, y-cSize, x, y+cSize);
            }

            //kolizní model
            if(drawOutline) {
                g2.setColor(Color.BLUE);
                if(obj.getClass() == BlockObject.class) {
                    BlockObject b = (BlockObject)obj;
                    g2.drawRect(x - (int)(b.getLength()/2*scale), y - (int)(b.getWidth()/2*scale),
                            (int)(b.getLength()*scale), (int)(b.getWidth()*scale));
                }
                else if(obj.getClass() == CylinderObject.class) {
                    CylinderObject b = (CylinderObject)obj;
                    int radius = (int)(b.getRadius()*scale);
                    g2.drawOval(x - radius, y - radius, radius*2, radius*2);
                }
            }
            g2.setTransform(start);
        }
    }

    /**
     * Vykreslí zeď
     * @param wall
     * @param g
     */
    private void paintWall(WallObject wall, Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int x = (int)(wall.getPosX()*scale);
        int y = (int)(wall.getPosY()*scale);
        
        
        
        g2.setColor(Color.DARK_GRAY);
        Stroke stroke = g2.getStroke();
        g2.setStroke(new BasicStroke(wall.getWidth()*scale, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        //Point2f last = null;
        GeneralPath path = new GeneralPath();
        path.moveTo(x, y);
        for(Point2f p : wall.getPoints()) {
            /*if(last == null) {
                g2.drawLine(x, y, (int)(p.x * scale), (int)(p.y * scale));
            }
            else {
                g2.drawLine((int)(last.x * scale), (int)(last.y * scale),
                        (int)(p.x * scale), (int)(p.y * scale));
            }*/
            path.lineTo((int)(p.x * scale), (int)(p.y * scale));
            
            //last = p;
        }
        g2.draw(path);
        g2.setStroke(stroke);
        
        if(drawCross) {
            g2.setColor(Color.LIGHT_GRAY);
            int cSize = 3;
            g2.drawLine(x-cSize, y, x+cSize, y);
            g2.drawLine(x, y-cSize, x, y+cSize);
            
            for(Point2f p: wall.getPoints()) {
                x = (int)(p.x*scale);
                y = (int)(p.y*scale);
                g2.drawLine(x-cSize, y, x+cSize, y);
                g2.drawLine(x, y-cSize, x, y+cSize);
            }
        }
    }


    /**
     * @return měřítko, ve kterém jsou vykresleny objekty
     */
    public float getScale() {
        return scale;
    }

    /**
     * Nastaví měřítko, ve kterém jsou vykresleny objekty
     * @param scale měřítko
     */
    public void setScale(float scale) {
        this.scale = scale;
    }
}

