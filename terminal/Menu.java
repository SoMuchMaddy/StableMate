import java.util.ArrayList;

/**
 * The Menu class represents an individual menu screen in the StableMate program
 * 
 * name - A name for internal identification (such as "horses")
 * title - What is printed tro the consolex (such as "Horses")
 * options - A list of MenuOption objects for the choices
 */
public class Menu {
	
	private String name, title;
	private ArrayList<MenuOption> options = new ArrayList<>();
	
	/**
	 * Constructs a new Menu with a given internal and display title
	 * 
	 * @param name A shorter name for indentification
	 * @param title The text to be displayed at the toop of the menu
	 */
	public Menu(String name, String title) {
		
		this.name = name;
		this.title = title;
	}
	
	/**
	 * Adds a new MenuOption to the Menu
	 * 
	 * @param option The MenuOption to add
	 */
	public void addOption(MenuOption option) { options.add(option); }
	
	/**
	 * Returns the internal name of the Menu
	 * 
	 * @return The menu name
	 */
	public String getName() { return name; }
	
	/**
	 * Returns the list of options that are in this Menu
	 * 
	 * @return An ArrayList of MenuOptions
	 */
	public ArrayList<MenuOption> getOptions() { return options; }
	
	/**
	 * Displays the Menu title and all its options to the console
	 */
	public void display() {
		
		System.out.println("\n=== " + title + " ===");
		for (MenuOption opt : options){
			System.out.println("(" + opt.getKey() + ") " + opt.getLabel());
		}
		System.out.println("Choice: ");
	}
}
