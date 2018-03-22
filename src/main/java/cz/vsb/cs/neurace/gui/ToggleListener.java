package cz.vsb.cs.neurace.gui;

/**
 *
 * @author Petr Hamalcik
 */
public interface ToggleListener {
    public enum ButtonType { FOLLOW, OPTIONS}
    
    public void toggleButton(ButtonType type);
}
