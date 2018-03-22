package cz.vsb.cs.neurace.gui.track;

import cz.vsb.cs.neurace.gui.MainWindow;
import cz.vsb.cs.neurace.gui.MainWindow.Status;
import cz.vsb.cs.neurace.gui.Resources;
import cz.vsb.cs.neurace.gui.TrackEditorTab;
import cz.vsb.cs.neurace.race.RacePhysics;
import cz.vsb.cs.neurace.track.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.vecmath.Point2f;

/**
 * Plocha s tratí, která umoznuje její editaci.
 */
public class TrackEdit extends JComponent 
    implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, TrackChangeListener {

    
	/**
	 * Stavy editoru tratě
	 */
	public enum EditorStatus {
		/** nová trať, pridávají se body */
		NEW, 
		/** označují se body */
		SELECT, 
		/** vloží se další bod do hotové tratě */
		ADD,
                /** přidávájí se objekty */
                ADD_OBJECT,
                /** mění povrchy tratě */
                SURFACE,
                /** maže objekty */
                BULLDOZER,
                /** přidávají se další body zdi */
                ADD_WALL_POINTS
	}
        
	/** označený bod */
	private SelectPoint selectPoint;

        private TrackObject selectedObject;
	/** stav editoru */
	private EditorStatus status;
	/** editovaná trať */
	private Track track;
	/** nově přidaný bod */
	private TrackPoint newPoint;
        /** určuje, jestli byla trať změněna */
        private boolean trackChanged = false;

        private String objectType = null;
        private Surface surface = null;
        private float scale = 3.0f;
        ObjectPainter op;
        TrackPainter tp;
        private WallObject editedWall = null;
        private Point2f wallPoint = null;
        private TrackObject dragedObject  = null;
        private TrackObject addedObject  = null;
        
        private RacePhysics physics;

        Cursor defaultCursor = this.getCursor();
        Cursor crossCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);

        private Point2f center = new Point2f(0,0);
        private int offsetX = 0;
        private int offsetY = 0;
        
        private java.util.List<WallObject> walls = new ArrayList<WallObject>();
        

	/**
	 * Konstruktor
	 * @param track
	 */
	public TrackEdit(Track track) {
		this.track = track;
                for(TrackObject o: track.getObjects()) {
                    if(o.getClass() == WallObject.class) {
                        walls.add((WallObject) o);
                    }
                }
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
                this.addMouseWheelListener(this);
                this.addKeyListener(this);
		this.status = track.getPoints().length == 0 ? EditorStatus.NEW : EditorStatus.SELECT;
                op = new ObjectPainter(scale);
                op.setDrawCross(true);
                tp = new TrackPainter(track, scale);
                tp.setUseTextures(false);
                
                track.addTrackChangeListener(this);
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
		return new Dimension((int)(track.getWidth()*scale), (int)(track.getHeight()*scale));
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
                tp.paintRoad(g2);
                tp.drawLine(g2);
                op.paintObjects(track.getObjects(), g2);

                boolean first = true;
                for (TrackPoint p : track.getPoints()) {
                    paintPoint(p, g2);
                    if (first) {
                        paintPointFirst(p, g2);
                        first = false;
                    }
		}
                
                if (selectPoint != null) {
                    selectPoint.paint(g2, scale);
		}
	}


	/**
	 * Vykreslí bod.
	 * @param p
	 * @param g2
	 */
	private void paintPoint(TrackPoint p, Graphics2D g2) {
		int x = (int) (p.getX()*scale);
		int y = (int) (p.getY()*scale);
                g2.setColor(Color.BLACK);
		g2.drawLine(x - 3, y, x + 3, y);
		g2.drawLine(x, y - 3, x, y + 3);
                if(p.isCheckpoint()) {
                    g2.setColor(Color.blue);
                    g2.drawOval(x - 5, y - 5, 10, 10);
                }
	}

	/**
	 * Vykreslí označení pro první bod.
	 * @param p
	 * @param g2
	 */
	private void paintPointFirst(TrackPoint p, Graphics2D g2) {
                float px = p.getX()*scale;
                float py = p.getY()*scale;
                float a = px - p.getFromX()*scale;
		float b = py - p.getFromY()*scale;
		double theta = Math.atan(b / a);
		if (a >= 0) {
			theta += Math.PI;
		}
		g2.translate(px,py);
		g2.rotate(theta);
                g2.setColor(Color.BLACK);
		g2.drawLine(-5, 7, 5, 7);
		g2.drawLine(5, 7, 0, 10);
		g2.drawLine(5, 7, 0, 4);
		g2.drawLine(-5, -7, 5, -7);
		g2.drawLine(5, -7, 0, -10);
		g2.drawLine(5, -7, 0, -4);
		g2.rotate(-theta);
		g2.translate(-px, -py);
	}
        
        

        @Override
        public void mousePressed(MouseEvent e) {
                
            if(e.getButton() == MouseEvent.BUTTON1) {
                if(status == EditorStatus.SELECT) {
			if (selectPoint == null || !selectPoint.selectPart(e.getX()/scale, e.getY()/scale)) {
				TrackPoint spoint = trackFindPoint(e.getX()/scale, e.getY()/scale);
				if (spoint != null) {
					selectPoint = new SelectPoint(track, spoint, e.getX()/scale, e.getY()/scale);
					repaint();
                                        TrackEditorTab.get().selected(true);
				} else {
                                        dragedObject = trackFindObject(e.getX(), e.getY());
                                        if(dragedObject == null) {
                                            wallPoint = findWallPoint(e.getX(), e.getY());
                                        }
                                        selectPoint = null;
                                        TrackEditorTab.get().selected(false);
                                        repaint();
				} 

			}
		} else if (status == EditorStatus.ADD) {
			TrackPoint near = null;
			float min = Float.MAX_VALUE;
			TrackPoint[] points = track.getPoints();
			for (TrackPoint point : points) {
				if (point.getLineFrom() != null) {
					LinePoint[] linePoints = point.getLineFrom().getLinePoints();
					for (LinePoint lp : linePoints) {
						float l = (float) Math.sqrt((lp.x - e.getX()) *
								(lp.x - e.getX()/scale) + (lp.y - e.getY()/scale) *
								(lp.y - e.getY()/scale));
						if (l < min) {
							min = l;
							near = point;
						}
					}
				}
			}
			int i = 0;
			TrackPoint[] n = new TrackPoint[points.length + 1];
			for (TrackPoint point : points) {
				n[i++] = point;
				if (point == near) {
					newPoint = new TrackPoint(e.getX()/scale , e.getY()/scale, track.getBasicSurface());
                                        n[i++] = newPoint;
					selectPoint = new SelectPoint(track, newPoint, e.getX()/scale, e.getY()/scale);
				}
			}
			track.setPoints(n);
			repaint();
			TrackEditorTab.get().selected(true);
		}
                else if(status == EditorStatus.ADD_OBJECT) {
                    if(addedObject.getClass() == BlockObject.class) {
                        BlockObject block = (BlockObject)addedObject;
                        BlockObject newObj = (BlockObject)addObject(e.getX()/scale, e.getY()/scale, block.getAngle());
                        if(physics != null) {
                            physics.createBlock(newObj);
                        }
                    }
                    else {
                        CylinderObject newObj = (CylinderObject)addObject(e.getX()/scale, e.getY()/scale, 0);
                        if(physics != null) {
                            physics.createCylinder(newObj);
                        }
                    }
                    track.changed();
                    repaint();
                }
                else if(status == EditorStatus.ADD_WALL_POINTS) {
                    if(editedWall.getPosX() == -1) {
                        track.addObject(editedWall);
                        walls.add(editedWall);
                        editedWall.setPosX(e.getX()/scale);
                        editedWall.setPosY(e.getY()/scale);
                    }
                    else {
                        editedWall.addPoint(new Point2f(e.getX()/scale, e.getY()/scale));
                    }
                    track.changed();
                    repaint();
                }
                else if(status == EditorStatus.SURFACE) {
                    TrackPoint spoint = trackFindPoint((int)(e.getX()/scale), (int)(e.getY()/scale));
                    if (spoint != null) {
                       spoint.setSurface(surface);
                    }
                    track.changed();
                    repaint();
                }
                else if(status == EditorStatus.BULLDOZER) {
                    TrackObject obj = trackFindObject(e.getX(), e.getY());
                    if(obj != null) {
                        if(physics != null) {
                            physics.destroyObject(obj);
                        }
                        track.getObjects().remove(obj);
                        track.changed();
                        repaint();
                    }
                    else if(findWallPoint(e.getX(), e.getY()) != null) {
                        if(physics != null) {
                            physics.destroyObject(editedWall);
                        }
                        track.getObjects().remove(editedWall);
                        editedWall = null;
                        track.changed();
                        repaint();
                    }
                }
                else if (status == EditorStatus.NEW) {
			newPoint = new TrackPoint(e.getX()/scale, e.getY()/scale, track.getBasicSurface());
			trackAddPoint(newPoint);
                        track.changed();
			selectPoint = new SelectPoint(track, newPoint, e.getX()/scale, e.getY()/scale);
			repaint();
                }
            } //BUTTON1
            else if(e.getButton() == MouseEvent.BUTTON3) {
                if (status == EditorStatus.NEW && track.getPoints().length > 1) {
                    track.close();
                    track.changed();
                    selectPoint = null;
                    repaint();
                    status = EditorStatus.SELECT;
                    this.setCursor(defaultCursor);
                    MainWindow.get().setStatus(Status.TRACKLOAD);
                }
                else if(track.getPoints().length > 1){
                    if(status == EditorStatus.ADD_WALL_POINTS) {
                        if(physics != null) {
                            physics.createWall(editedWall);
                        }
                        addedObject = null;
                    }
                    else if(addedObject != null) {
                        track.getObjects().remove(addedObject);
                        addedObject = null;
                        this.repaint();
                    }
                    status = EditorStatus.SELECT;
                    this.setCursor(defaultCursor);
                    TrackEditorTab.get().setSelectionMode();
                }
            }
            else if(e.getButton() == MouseEvent.BUTTON2) {
                setScale(3.0f);
                this.setSize(this.getPreferredSize());
                moveToCenter();
            }
	}
        
        
        /**
         * Přidá objekt na trať
         * @param x pozice v metrech
         * @param y pozice v metrech
         * @param angle úhel v radiánech
         * @return přidaný objekt
         */
        public TrackObject addObject(float x, float y, float angle) {
            TrackObject obj = Resources.get().getObject(objectType);
            if(obj.getClass() == BlockObject.class) {
                BlockObject newObj = new BlockObject((BlockObject)obj);
                newObj.setPosX(x);
                newObj.setPosY(y);
                newObj.setAngle(angle);
                track.addObject(newObj);
                return newObj;
            }
            else if(obj.getClass() == CylinderObject.class) {
                CylinderObject newObj = new CylinderObject((CylinderObject)obj);
                newObj.setPosX(x);
                newObj.setPosY(y);
                track.addObject(newObj);
                return newObj;
            }
            return null;
        }

	/**
	 * Vloží bod do tratě.
	 * @param p
	 */
	private void trackAddPoint(TrackPoint p) {
		track.addPoint(p);
		if (track.getPoints().length >= 2) {
			TrackEditorTab.get().canSelect();
		}
	}
        
        

	

	/**
	 * Vyhledá bod na souřadnicích [x,y]
	 * @param x
	 * @param y
	 * @return bod, pokud existuje, jinak null
	 */
	private TrackPoint trackFindPoint(float x, float y) {
		for (TrackPoint p : track.getPoints()) {
			if (p.length(x, y) < 5) {
				return p;
			}
		}
		return null;
	}

        /**
	 * Vyhledá objekt na souřadnicích [x,y]
	 * @param x
	 * @param y
	 * @return objekt, pokud existuje, jinak null
	 */
        private TrackObject trackFindObject(int x, int y) {
            Point2f mouse = new Point2f((x/scale), (y/scale));
            for (TrackObject o : track.getObjects()) {
                Point2f p = new Point2f(o.getPosX(), o.getPosY());
                if (p.distance(mouse) < 2) {
                   return o;
                }
            }
            return null;
        }
        
        /**
	 * Vyhledá bod zdi na souřadnicích [x,y]
	 * @param x
	 * @param y
	 * @return bod zdi, pokud existuje, jinak null
	 */
        private Point2f findWallPoint(int x, int y) {
            Point2f mouse = new Point2f((x/scale), (y/scale));
            for (WallObject w : walls) {
                for(Point2f p: w.getPoints()) {
                    if(p.distance(mouse) < 2) {
                        editedWall = w;
                        return p;
                    }
                }
            }
            return null;
        }

        /**
         * Vytvoří nebo odstraní checkpoint ve vybraném bodě
         */
        public void setCheckpoint() {
            TrackPoint p = selectPoint.getPoint();
            p.setCheckpoint(!p.isCheckpoint());
            track.changed();
            repaint();
        }

        
        @Override
        public void mouseMoved(MouseEvent e) {
            if(status == EditorStatus.ADD_OBJECT) {
                addedObject.move(e.getX()/scale, e.getY()/scale);
                repaint();
            }
            center.set(e.getX()/scale, e.getY()/scale);
            Point m = this.getParent().getMousePosition();
            if(m != null) {
                offsetX = m.x;
                offsetY = m.y;
            }
	}

        @Override
	public void mouseDragged(MouseEvent e) {
		if (status == EditorStatus.NEW && newPoint != null) {
			newPoint.setVectors(e.getX()/scale, e.getY()/scale);
			repaint();
		} else if (status == EditorStatus.SELECT && selectPoint != null) {
			selectPoint.move(e.getX()/scale, e.getY()/scale);
                        track.changed();
                        //trackChanged = true;
			repaint();
                } else if (status == EditorStatus.SELECT && dragedObject != null) {
			dragedObject.move(e.getX()/scale, e.getY()/scale);
                        //trackChanged = true;
                        track.changed();
			repaint();
		} 
                else if (status == EditorStatus.SELECT && wallPoint != null) {
			wallPoint.setX(e.getX()/scale);
                        wallPoint.setY(e.getY()/scale);
                        //trackChanged = true;
                        track.changed();
			repaint();
		} else if (status == EditorStatus.ADD && newPoint != null) {
			newPoint.setVectors(e.getX()/scale, e.getY()/scale);
                        track.changed();
			repaint();
		}
             
	}

        @Override
	public void mouseClicked(MouseEvent e) {
	}

        @Override
	public void mouseEntered(MouseEvent e) {
            this.requestFocus();
            if(status != EditorStatus.SELECT) {
                this.setCursor(crossCursor);
            }
            if(status == EditorStatus.ADD_OBJECT) {
                addedObject = addObject(e.getX()/scale, e.getY()/scale, 0);
            }
	}

        @Override
	public void mouseExited(MouseEvent e) {
            this.setCursor(defaultCursor);
            if(status == EditorStatus.ADD_OBJECT) {
                track.getObjects().remove(addedObject);
                addedObject = null;
                this.repaint();
            }
	}

        @Override
	public void mouseReleased(MouseEvent e) {
		/*if (status == EditorStatus.ADD) {
                    status = EditorStatus.SELECT;
		}*/
                if(physics != null) {
                    if(dragedObject != null) {
                        physics.destroyObject(dragedObject);
                        physics.createObject(dragedObject);
                        dragedObject = null;
                    }
                    else if(wallPoint != null) {
                        physics.destroyObject(editedWall); 
                        physics.createWall(editedWall);
                        wallPoint = null;
                    }
                }
                dragedObject = null;
                if(trackChanged) {
                    track.changed();
                }
                
	}

	/**
	 * Vrátí editovanou trať
	 * @return
	 */
	public Track getTrack() {
		return track;
	}

	/**
	 * Změní stav na výběr bodů.
	 */
	public void selectPoint() {
		if (!track.isClosed()) {
			track.close();
			selectPoint = null;
			repaint();
			MainWindow.get().setStatus(Status.TRACKLOAD);
		}
		status = EditorStatus.SELECT;
	}

	/**
	 * Změní stav a další stlačení tlačítka myši přidá další bod.
	 */
	public void newPoint() {
		status = EditorStatus.ADD;
	}

        public void bulldozer() {
		status = EditorStatus.BULLDOZER;
	}

        public void setObjectMode(String type) {
		status = EditorStatus.ADD_OBJECT;
                this.objectType = type;
	}
        
        public void setWallMode(float height, float width) {
		status = EditorStatus.ADD_WALL_POINTS;
                this.editedWall = new WallObject(-1, -1, "wall", height, width);
                
	}

        public void setSurfaceMode(Surface surface) {
		status = EditorStatus.SURFACE;
                this.surface = surface;
	}

	/**
	 * Odstraní vybraný bod.
	 */
	public void removePoint() {
		TrackPoint[] p = track.getPoints();
		if (p.length > 2 && selectPoint != null) {
			TrackPoint[] n = new TrackPoint[p.length - 1];
			for (int i = 0,  j = 0; i < p.length; i++) {
				if (p[i] != selectPoint.getPoint()) {
					n[j++] = p[i];
				}
			}
			selectPoint = null;
			track.setPoints(n);
			repaint();
			TrackEditorTab.get().selected(false);
		}
                else if(selectedObject != null) {
                    track.getObjects().remove(selectedObject);
                    track.changed();
                    repaint();
                    TrackEditorTab.get().selected(false);
                }
	}

	/**
	 * Otočí směr tratě.
	 */
	public void revert() {
		TrackPoint[] p = track.getPoints();
		TrackPoint[] n = new TrackPoint[p.length];
		for (int i = 0; i < p.length; i++) {
			n[(p.length - i) % p.length] = p[i];
			p[i].swapFromTo();
		}
		track.setPoints(n);
		repaint();
	}
        
        

	/**
	 * Nastaví označený od jako první.
	 */
	public void setFirstPoint() {
		int index = 0;
		TrackPoint[] p = track.getPoints();
		for (int i = 0; i < p.length; i++) {
			if (p[i] == selectPoint.getPoint()) {
				index = i;
				break;
			}
		}
		TrackPoint[] n = new TrackPoint[p.length];
		for (int i = 0; i < p.length; i++) {
			n[i] = p[(index + i) % p.length];
		}
		track.setPoints(n);
		repaint();
	}

	/**
	 * Zda je označen jakýkoliv bod.
	 * @return true pokud je označen bod, jinak false
	 */
	public boolean isSelected() {
		return selectPoint != null;
	}

        /**
         * Nastaví měřítko
         * @param scale měřítko
         */
        public void setScale(float scale) {
            this.scale = scale;
            this.op.setScale(scale);
            this.tp.setScale(scale);
        }

        public RacePhysics getPhysics() {
            return physics;
        }

        public void setPhysics(RacePhysics physics) {
            this.physics = physics;
        }
        
        
        
        
        @Override
        public void trackChange() {
            tp.genRoad();
        }


        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
           //otáčení objektu
           if((addedObject != null && addedObject.getClass() == BlockObject.class) ||
                   (dragedObject != null && dragedObject.getClass() == BlockObject.class)) {
               int clicks = e.getWheelRotation();
               BlockObject block;
               if(addedObject != null) {
                   block = (BlockObject)this.addedObject;
               }
               else {
                   block = (BlockObject)this.dragedObject;
               }
               if(clicks < 0) {
                   block.setAngle(block.getAngle()+(float)Math.PI/8);
               }
               else {
                   block.setAngle(block.getAngle()-(float)Math.PI/8);
               }
               this.repaint();
               
           }
           //zoomování
           else {
               int clicks = e.getWheelRotation();
               if(clicks < 0 && scale < 10) {
                   this.setScale(scale + 0.2f);
               }
               else if(clicks > 0 && scale > getMinScale()){
                   this.setScale(scale - 0.2f);
               }
               
               this.setSize(this.getPreferredSize());
               moveToCenter();
               //this.repaint();
           }
        }


        public void moveToCenter() {
                int h = this.getParent().getHeight();
                int w = this.getParent().getWidth();
                this.scrollRectToVisible(new Rectangle((int)(center.x*scale)-offsetX,
                       (int)(center.y*scale)-offsetY, w, h));
        }
        
        public float getMinScale() {
            float h = (float)this.getParent().getHeight() / track.getHeight();
            float w = (float)this.getParent().getWidth() / track.getWidth();
            return (w < h) ? w : h;
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyChar() == '+' && scale < 10) {
               this.setScale(scale + 0.2f);
               this.setSize(this.getPreferredSize());
               moveToCenter();
            }
            else if(e.getKeyChar() == '-' && scale > getMinScale()) {
               this.setScale(scale - 0.2f);
               this.setSize(this.getPreferredSize());
               moveToCenter();
            }
            else if(e.getKeyCode() == KeyEvent.VK_DELETE && this.selectPoint != null) {
               removePoint();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
}
