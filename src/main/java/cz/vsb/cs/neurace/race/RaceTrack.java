package cz.vsb.cs.neurace.race;

import cz.vsb.cs.neurace.track.Track;
import javax.vecmath.Point2f;

public class RaceTrack {
	/** Vzdálenost mezi checkpointy */
	final public static float checkpointsDistance = 100f;
	/** čary tratě */
	private RaceLine[] lines;
	/** checkpoity na trati */
	private Checkpoint[] checkpoints;


	/** trať */
	private Track track;
	/** délka tratě */
	private float length;

	/**
	 * Konstruktor
	 * @param track
	 */
	public RaceTrack(Track track) {
		this.track = track;
		this.updateTrack();
                this.checkpoints = track.getCheckpoints();
                this.lines = track.getLines();
	}

	/**
	 * Znovu přepočítá trať a věci na ni závislé
	 */
	private void updateTrack() {
            this.track.updateTrack();
            this.checkpoints = track.getCheckpoints();
            this.lines = track.getLines();
	}

        /**
         * Vypočítá startovní pozici
         * @param startNumber startovní číslo auta
         * @return startovní pozice
         */
        public RaceLine calculateStartLine(int startNumber, boolean collisions) {
            int linesCount = this.getTrack().getLines().length;
            if(collisions) {
                int offset = 1;
                int minDistance = 10;
                float distance = 0;
                float lineLength = this.getTrack().getLines()[linesCount-offset].getLength();
                while(distance+lineLength < minDistance*startNumber+minDistance*0.5f) {
                    distance += lineLength;
                    offset++;
                    lineLength = this.getTrack().getLines()[linesCount-offset].getLength();
                }
                return this.getTrack().getLines()[linesCount-offset];
            }
            else {
                return this.getTrack().getLines()[linesCount-1];
            }
        }

        /**
         * Vypočítá startovní pozici
         * @param startNumber startovní číslo auta
         * @return startovní pozice
         */
        public Point2f calculateStartPos(int startNumber, boolean collisions) {
            int linesCount = this.getTrack().getLines().length;
            if(collisions) {
                int offset = 1;
                int minDistance = 10;
                float distance = 0;
                
                float lineLength = this.getTrack().getLines()[linesCount-offset].getLength();
                while(distance+lineLength < minDistance*startNumber+minDistance*0.5f) {
                    distance += lineLength;
                    offset++;
                    lineLength = this.getTrack().getLines()[linesCount-offset].getLength();
                }

                RaceLine line = this.getTrack().getLines()[linesCount-offset];
                float l = minDistance*startNumber + minDistance*0.5f - distance;
                float x = line.getX2() - l*(float)Math.cos(line.getTheta());
                float y = line.getY2() - l*(float)Math.sin(line.getTheta());
                return new Point2f(x, y);
            }
            else {
                RaceLine line = this.getTrack().getLines()[linesCount-1];
                return new Point2f(line.getX2(), line.getY2());
            }
        }

        //přesunuto do track
        /**
	 * Znovu přepočítá trať a věci na ni závoslé
	 */
	/*public void updateTrack() {
		int i = 0;
		length = 0;
		LinkedList<RaceLine> list = new LinkedList<RaceLine>();
		for (Point p : track.getPoints()) {
			LinePoint lp1 = null;
			for (LinePoint lp2 : p.getLineFrom().getLinePoints()) {
				if (lp1 != null) {
					RaceLine rl = new RaceLine(lp1, lp2, i++);
					list.add(rl);
					length += rl.getLength();
				}
				lp1 = lp2;
			}
		}
		lines = new RaceLine[list.size()];
		list.toArray(lines);

		LinkedList<Checkpoint> listc = new LinkedList<Checkpoint>();
		i = 0;
		float d = checkpointsDistance;// delka od ostatniho checkpoitu
		float dl = 0;//celkova delka od zacatku
		for (RaceLine line : lines) {
			if ((dl < length - checkpointsDistance) && (d + line.getLength() > checkpointsDistance)) {
				float distLine = checkpointsDistance - d;
				float x = line.getX1() + distLine * (float) Math.cos(line.getTheta());
				float y = line.getY1() + distLine * (float) Math.sin(line.getTheta());
				d = line.getLength() - distLine;
				listc.add(new Checkpoint(x, y, line.getTheta(), i++));
			} else {
				d += line.getLength();
			}
			dl += line.getLength();
		}
		checkPoints = new Checkpoint[listc.size()];
		listc.toArray(checkPoints);
	}*/

	/**
	 * Najde nejbližší část tratě k bodu [x,y]. Začne prohledávat od části reference.
	 * Pokud ta je null, pak progledá delou trať.
	 * 
	 * @param x souřadnice x bodu
	 * @param y souřadnice y bodu
	 * @param reference referenční část, může být null
	 * @return nejbližší část tratě
	 */
	public RaceLine getNearestLine(float x, float y, RaceLine reference) {
		RaceLine near = reference;
		float l = Float.MAX_VALUE;
		if (reference == null) {
			// neni kde zacit - projde se cela trat.
			for (RaceLine line : lines) {
				float l2 = line.getDistance(x, y);
				if (Math.abs(l2) < Math.abs(l)) {
					l = l2;
					near = line;
				}
			}
		} else {
			// prohleda se jen dopredu a dozadu od reference.
			int i = reference.getIndex();
			while (true) {
				RaceLine line = lines[i++ % lines.length];
				float l2 = Math.abs(line.getDistance(x, y));
				if (l2 < l) {
					l = l2;
					near = line;
				} else {
					break;
				}
			}
			i = reference.getIndex() - 1;
			while (true) {
				RaceLine line = lines[(lines.length + (i-- % lines.length)) % lines.length];
				float l2 = Math.abs(line.getDistance(x, y));
				if (l2 < l) {
					l = l2;
					near = line;
				} else {
					break;
				}
			}
		}
		return near;
	}

	/**
	 * Nastavni pozice auta
	 * 
	 * @param car
	 */
	/*public void setStartPosition(Car car) {
		Point p = track.getPoints()[0];
		car.setPosition(p.getX(), p.getY(), lines[0].getTheta(), lines[0]);
	}*/

	/**
	 * Najde checkpoint, přes který projelo auto z bodu [x1,y1] do bodu [x2,y2].
	 * POkud takový existuje, pak no vrátí, jinak vrátí null.
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param last referenční checkpoint
	 * @return projetý checkpoint, pokud existuje, jinak null
	 */
	public Checkpoint getCheckPoint(float x1, float y1, float x2, float y2, Checkpoint last) {
		if (last == null) {
			for (Checkpoint cp : checkpoints) {
				if (cp.check(x1, y1, x2, y2)) {
					return cp;
				}
			}
		} else {
			Checkpoint cp = checkpoints[(last.getIndex() + 1) % checkpoints.length];
			if (cp.check(x1, y1, x2, y2)) {
				return cp;
			}
		}
		return null;
	}

        public Track getTrack() {
            return this.track;
        }

        /**
         * Vrátí další checkpoint na trati
         * @param index index aktuálního checkpointu
         * @return další checkpoint
         */
        public Checkpoint getNextCheckpoint(int index) {
            if(index < checkpoints.length-1)
                return checkpoints[index+1];
            else
                return checkpoints[0];
        }

        /**
         * Vrací checkpointy na trati
         * @return checkpointy na trati
         */
        public Checkpoint[] getCheckpoints() {
            return checkpoints;
        }
}
