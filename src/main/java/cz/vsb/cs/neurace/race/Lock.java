package cz.vsb.cs.neurace.race;

/**
 * Zámek pro různé účely. Zastaví vlákno metodou suspend, iná metoda ho probudí metodou resume.
 *
 */
public class Lock {

	/**
	 * Zmrazí aktualní vlákno.
	 */
	public void suspend() {
		synchronized (this) {
			try {
				wait();
			} catch (InterruptedException e) {
                                System.err.println(e.getMessage());
				//e.printStackTrace();
			}
		}
	}

	/**
	 * Probudí všechna zmrazena vlákna.
	 */
	public void resume() {
		synchronized (this) {
			notifyAll();
		}
	}
}
