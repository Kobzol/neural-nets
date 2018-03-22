package cz.vsb.cs.neurace.track;

/**
 * Bod tratě.
 */
public class TrackPoint {

	/** souřadnice bodu */
	private float x,  y;
	/** souřadnice vektoru pro vstupní křivku */
	private float toX,  toY;
	/** souřadnice vektoru pro výstupní křivku */
	private float fromX,  fromY;
	/** vcházející a vycházející křivka */
	private Line lineFrom,  lineTo;
        /** povrch trati za tímto bodem */
        private Surface surface;
        /** šířka trati za tímto bodem */
        private float roadWidth = 40;
        /** určuje jestli je v bodě checkpoint */
        private boolean checkpoint = true;

	/**
	 * Konstruktor, který nastavuje souřadnice bodu a vektory nulové délky.
	 * @param x x souřadnice
	 * @param y y souřadnice
	 */
	public TrackPoint(float x, float y) {
		this(x, y, x, y, x, y);
                this.surface = Surface.ASPHALT;
	}


        
        /**
	 * Konstruktor, který nastavuje souřadnice bodu a vektory nulové délky.
	 * @param x x souřadnice
	 * @param y y souřadnice
	 */
	public TrackPoint(float x, float y, Surface s) {
		this(x, y, x, y, x, y);
                this.surface = s;
	}



	/**
	 * Konstruktor, který nastavuje souřadnice bodu vektorů.
	 * @param x x souřadnice bodu
	 * @param y x souřadnice bodu
	 * @param toX x souřadnice vektoru vstupní křivky
	 * @param toY y souřadnice vektoru vstupní křivky
	 * @param fromX x souřadnice vektoru výstupní křivky
	 * @param fromY y souřadnice vektoru výstupní křivky
	 */
	public TrackPoint(float x, float y, float toX, float toY, float fromX, float fromY) {
		this.x = x;
		this.y = y;
		this.toX = toX;
		this.toY = toY;
		this.fromX = fromX;
		this.fromY = fromY;
                this.surface = Surface.ASPHALT;
	}

        /**
	 * Konstruktor, který nastavuje souřadnice bodu vektorů.
	 * @param x x souřadnice bodu
	 * @param y x souřadnice bodu
	 * @param toX x souřadnice vektoru vstupní křivky
	 * @param toY y souřadnice vektoru vstupní křivky
	 * @param fromX x souřadnice vektoru výstupní křivky
	 * @param fromY y souřadnice vektoru výstupní křivky
         * @param s povrch trati za tímto bodem
	 */
        public TrackPoint(float x, float y, float toX, float toY, float fromX, float fromY,
                Surface s, float roadWidth) {
		this.x = x;
		this.y = y;
		this.toX = toX;
		this.toY = toY;
		this.fromX = fromX;
		this.fromY = fromY;
                this.surface = s;
                this.roadWidth = roadWidth;
	}

	/**
	 * Konstruktor, který nenastavuje pozice.
	 */
	public TrackPoint() {
	}

        public boolean isCheckpoint() {
            return checkpoint;
        }

        public void setCheckpoint(boolean checkpoint) {
            this.checkpoint = checkpoint;
        }



	/**
	 * Vrátí souřadnici x bodu.
	 * @return souřadnice x bodu
	 */
	public float getX() {
		return x;
	}

	/**
	 * Vrátí souřadnici y bodu.
	 * @return souřadnice y bodu
	 */
	public float getY() {
		return y;
	}

	/**
	 * Vrátí souřadnici x vektoru vstupní křivky.
	 * @return souřadnice x vektoru vstupní křivky
	 */
	public float getToX() {
		return toX;
	}

	/**
	 * Vrátí souřadnici y vektoru vstupní křivky.
	 * @return souřadnice y vektoru vstupní křivky
	 */
	public float getToY() {
		return toY;
	}

	/**
	 * Vrátí souřadnici x vektoru vstupní křivky.
	 * @return souřadnice x vektoru vstupní křivky
	 */
	public float getFromX() {
		return fromX;
	}

	/**
	 * Vrátí souřadnici y vektoru výstupní křivky.
	 * @return souřadnice y vektoru výstupní křivky
	 */
	public float getFromY() {
		return fromY;
	}

	/**
	 * Nastaví vstupní křivku.
	 * @param lineFrom vstupní křivka.
	 */
	public void setLineTo(Line lineTo) {
		this.lineTo = lineTo;
	}

	/**
	 * Vrátí vstupní křivku.
	 * @return vstupní křivka.
	 */
	public Line getLineTo() {
		return lineTo;
	}

	/**
	 * Nastaví výstupní křivku.
	 * @param lineFrom výstupní křivka.
	 */
	public void setLineFrom(Line lineFrom) {
		this.lineFrom = lineFrom;
	}

	/**
	 * Vrátí výstupní křivku.
	 * @return výstupní křivka.
	 */
	public Line getLineFrom() {
		return lineFrom;
	}

	/**
	 * Nastaví soužadnice vektorů na sejnou délku a směr.
	 * @param x x souřadnice vektoru výstupní křivky
	 * @param y y souřadnice vektoru výstupní křivky
	 */
	public void setVectors(float x, float y) {
		fromX = x;
		fromY = y;
		toX = 2 * this.x - x;
		toY = 2 * this.y - y;
		updateLines();
	}

	/**
	 * Upraví křivky.
	 */
	private void updateLines() {
		if (lineFrom != null) {
			lineFrom.generate();
		}
		if (lineTo != null) {
			lineTo.generate();
		}
	}

	/**
	 * Vypočítá vzdálenost zadaného bodu od tohoto bodu.
	 * @param x souřadnice x jiného bodu.
	 * @param y souřadnice y jiného bodu.
	 * @return vzdálenost mezi body
	 */
	public float length(float x, float y) {
		return length(x, y, this.x, this.y);
	}

	/**
	 * Vypočítá vzdálenost zadaného bodu od konce vektoru výstupní křivky.
	 * @param x souřadnice x jiného bodu.
	 * @param y souřadnice y jiného bodu.
	 * @return vzdálenost
	 */
	public float lengthFrom(float x, float y) {
		return length(x, y, this.fromX, this.fromY);
	}

	/**
	 * Vypočítá vzdálenost zadaného bodu od konce vektoru vstupní křivky.
	 * @param x souřadnice x jiného bodu.
	 * @param y souřadnice y jiného bodu.
	 * @return vzdálenost
	 */
	public double lengthTo(float x, float y) {
		return length(x, y, this.toX, this.toY);
	}

	/**
	 * Spočítá vzdálenost mezi body.
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return vzdálenost
	 */
	private float length(float x1, float y1, float x2, float y2) {
		return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	/**
	 * Posun bodu. S bodem se posouvaí i vektory.
	 * @param x nová x souřadnice bodu
	 * @param y nová y souřadnice bodu
	 */
	public void move(float x, float y) {
		float dx = x - this.x;
		float dy = y - this.y;
		this.x = x;
		this.y = y;
		fromX += dx;
		fromY += dy;
		toX += dx;
		toY += dy;
		updateLines();
	}

	/**
	 * Posun bodu pro vektor vstupní křivky.
	 * @param x
	 * @param y
	 */
	public void moveTo(float x, float y) {
		toX = x;
		toY = y;
		float fromX2 = 2 * this.x - x;
		float fromY2 = 2 * this.y - y;
		float length = length(this.x, this.y, fromX, fromY);
		float lenght2 = length(this.x, this.y, fromX2, fromY2);
		if (lenght2 != 0) {
			fromX = length * (fromX2 - this.x) / lenght2 + this.x;
			fromY = length * (fromY2 - this.y) / lenght2 + this.y;
			updateLines();
		}
	}

	/**
	 * Posun bodu pro vektor výstupní křivky.
	 * @param x
	 * @param y
	 */
	public void moveFrom(float x, float y) {
		fromX = x;
		fromY = y;
		float toX2 = 2 * this.x - x;
		float toY2 = 2 * this.y - y;
		float length = length(this.x, this.y, toX, toY);
		float lenght2 = length(this.x, this.y, toX2, toY2);
		if (lenght2 != 0) {
			toX = length * (toX2 - this.x) / lenght2 + this.x;
			toY = length * (toY2 - this.y) / lenght2 + this.y;
			updateLines();
		}
	}

	/**
	 * Délka vektoru výstupní křivky.
	 * @return délka
	 */
	public float lengthFrom() {
		return length(x, y, this.fromX, this.fromY);
	}

	/**
	 * Délka vektoru vstupní křivky.
	 * @return délka
	 */
	public float lengthTo() {
		return length(x, y, this.toX, this.toY);
	}

        public Surface getSurface() {
            return surface;
        }

        public void setSurface(Surface surface) {
            this.surface = surface;
        }

        public float getRoadWidth() {
            return roadWidth;
        }

        public void setRoadWidth(float roadWidth) {
            this.roadWidth = roadWidth;
        }


	/**
	 * Vymění vektory.
	 */
	public void swapFromTo() {
		float tmp = toX;
		toX = fromX;
		fromX = tmp;
		tmp = toY;
		toY = fromY;
		fromY = tmp;

		//Line ltmp = lineTo;
		lineTo = lineFrom;
		lineFrom = lineTo;
	}
}
