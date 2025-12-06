/**
 * A MenuOption represents a choice inside a Menu
 * 
 * key - what the user type (such as "h")
 * label - what is displayed in the menu for the choice (such as "Horses")
 * actionName - Used to decide what method to run (null if none needed)
 * nextMenuName - The name of the next method to switch to after selection
 * 
 * The Menu class uses these Objects to build
 */
public class MenuOption {
	
	private String key, label, actionName, nextMenuName;
	
	/**
	 * Constructs a new MenuOption
	 * 
	 * @param key The user input to select this option
	 * @param label The text displayed for the option
	 * @param actionName The action to run
	 * @param nextMenuName The name of the next mennu to go to
	 */
	public MenuOption(String key, String label, String actionName, String nextMenuName) {
		this.key = key;
		this.label = label;
		this.actionName = actionName;
		this.nextMenuName = nextMenuName;
	}
	
	/**
	 * Returns the key used to choose this option
	 * 
	 * @return The key
	 */
	public String getKey() { return key; }
	
	/**
	 * Returns the text label displayed
	 * 
	 * @return The label for this choice
	 */
	public String getLabel() { return label; }
	
	/**
	 * Returns the name of the action to run associated with this option (may be null if no action triggered)
	 * 
	 * @return The action name or null
	 */
	public String getActionName() { return actionName; }
	
	/**
	 * Returns the name of the next Menu to switch to when chosen
	 * 
	 * @return The name of the next Menu
	 */
	public String getNextMenuName() { return nextMenuName; }

}
