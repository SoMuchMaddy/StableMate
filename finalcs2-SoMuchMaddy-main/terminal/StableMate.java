import java.util.ArrayList;
import java.io.File;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Scanner;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * The main class for the StableMate program.
 * 
 * This class is responsible for:
 * 
 *   Loading and saving data for Riders, Horses, Lessons, and Stable
 *   Building and displaying menus
 *   Handling all user interaction and routing to actions
 *   Maintaining care and lesson priority queues
 * 
 * The program is a console-based application that reads from and writes to
 * flat text files in the ../data directory.
 */
public class StableMate {
	
	/// List of all objects in the program, accessed by internal name
	private static ArrayList<Menu> menus = new ArrayList<>();
	private static ArrayList<Horse> horses = new ArrayList<>();
	private static ArrayList<Lesson> lessons = new ArrayList<>();
	private static HashMap<String, Horse> horsesByID = new HashMap<>();
	private static ArrayList<Rider> riders = new ArrayList<>();
	private static HashMap<String, Rider> ridersByID = new HashMap<>();
	private static PriorityQueue<Horse> annualPQ;
	private static PriorityQueue<Horse> dewormPQ;
	private static PriorityQueue<Horse> farrierPQ;
	private static PriorityQueue<Lesson> lessonsPQ;
	private static Stable stable;
	
	/**
	 * Comparator for ordering lessons by date, then by time.
	 * Lessons with earlier dates/times come first. Null dates/times are
	 * treated as least specific and ordered before non-null values.
	 */
	private static final Comparator<Lesson> LESSONS_COMPARATOR = new Comparator<Lesson>() {
		@Override
		
		public int compare(Lesson l1, Lesson l2) {
			
			LocalDate d1 = l1.getDate();
			LocalTime t1 = l1.getTime();
			LocalDate d2 = l2.getDate();
			LocalTime t2 = l2.getTime();
			
			if ((d1 == null && d2 == null) && (t1 == null && t2 == null)) return 0;
			
			if (d1 == null) return -1;
			if (d2 == null) return 1;
			
			int cmp = d1.compareTo(d2);
			if (cmp != 0) return cmp; //If different dates return as is
			
			if (t1 == null && t2 == null) return 0; //Compare times if same date
			if (t1 == null) return -1;
			if (t2 == null) return 1;
			
			return t1.compareTo(t2);
		}
	};
	
	/**
	 * Comparator for ordering horses by next annual due date.
	 * Null dates are ordered before non-null dates.
	 */
	private static final Comparator<Horse> ANNUAL_COMPARATOR = new Comparator<Horse>() {
		@Override
		
		public int compare(Horse h1, Horse h2) {
			
			LocalDate d1 = h1.getNextAnnualDue();
			LocalDate d2 = h2.getNextAnnualDue();
			
			if (d1 == null && d2 == null) return 0;
			if (d1 == null) return -1;
			if (d2 == null) return 1;
			
			return d1.compareTo(d2);
		}
	};
	
	/**
	 * Comparator for ordering horses by next deworm due date.
	 * Null dates are ordered before non-null dates.
	 */
	private static final Comparator<Horse> DEWORM_COMPARATOR = new Comparator<Horse>() {
		@Override
		
		public int compare(Horse h1, Horse h2) {
			
			LocalDate d1 = h1.getNextDewormDue();
			LocalDate d2 = h2.getNextDewormDue();
			
			if (d1 == null && d2 == null) return 0;
			if (d1 == null) return -1;
			if (d2 == null) return 1;
			
			return d1.compareTo(d2);
		}
	};
	
	/**
	 * Comparator for ordering horses by next farrier due date.
	 * Null dates are ordered before non-null dates.
	 */
	private static final Comparator<Horse> FARRIER_COMPARATOR = new Comparator<Horse>() {
		@Override
		
		public int compare(Horse h1, Horse h2) {
			
			LocalDate d1 = h1.getNextFarrierDue();
			LocalDate d2 = h2.getNextFarrierDue();
			
			if (d1 == null && d2 == null) return 0;
			if (d1 == null) return -1;
			if (d2 == null) return 1;
			
			return d1.compareTo(d2);
		}
	};
	
	/// Shared console scanner for reading user input
	private static final Scanner console = new Scanner(System.in);
	
	/**
	 * The entry point of the StableMate program.
	 * 
	 * This method:
	 * 
	 *   Loads data files for Riders, Horses, Stable, and Lessons
	 *   Initializes priority queues and menus
	 *   Runs the main menu loop until the user chooses to quit
	 *   Saves all data back to files before exiting
	 * 
	 * @param args command-line arguments (unused)
	 */
	public static void main(String[] args) {
		
		loadRidersFromFile("../data/riders.txt");
		loadHorsesFromFile("../data/horses.txt");
		loadStableFromFile("../data/stable.txt");
		loadLessonsFromFile("../data/lessons.txt");
		initializeQueues();
		
		buildMenus(); //Creates all the menus
		
		String current = "main"; //Start at main Menu
		
		//Program loop that continues until told to quit
		while (!current.equals("quit")) {
			
			Menu active = getMenuByName(current); //The active Menu
			active.display(); //Display the current menu
			
			String input = console.nextLine().trim().toLowerCase();
			
			//Find the selected MenuOption (by comparing option key to input String)
			MenuOption selected = null;
			boolean flag = false;
			for (MenuOption o : active.getOptions()) {
				
				if ((flag != true) && (o.getKey().equals(input))) {
					
					selected = o;
					flag = true;
				}
			}
			
			//If the user input doesn't match any option
			if (selected == null) { 
				
				System.out.println("Invalid selection. Please try again.");
			} else {
				
				//Run the associated action (if there is one)
				if (selected.getActionName() != null) {
					
					runAction(selected.getActionName());
				}
				
				//Move to next menu for the selected option
				current = selected.getNextMenuName();
			}
		}
		
		saveRidersToFile("../data/riders.txt");
		saveHorsesToFile("../data/horses.txt");
		saveStableToFile("../data/stable.txt");
		saveLessonsToFile("../data/lessons.txt");
		System.out.println("Goodbye!");
	}
	
	/**
	 * Searches a list of menus for one with the given internal name.
	 * 
	 * @param name The internal name of the menu to search for
	 * @return The Menu that matches, or null if none is found
	 */
	private static Menu getMenuByName(String name) {
		
		for (Menu m : menus) {
			if (m.getName().equals(name)) {
				return m;
			}
		}
		
		return null;
	}
	
	/**
	 * Runs an action associated with a menu option.
	 * 
	 * This method maps a string action name to the appropriate method
	 * in this class. If an action is not recognized, a message is printed.
	 * 
	 * @param actionName the name of the action to run
	 */
	private static void runAction(String actionName) { 
		
		switch(actionName) {
			
			case "stableOverview":
				stableOverview();
				break;
				
			case "editStableName":
				editStableName();
				break;
				
			case "editYearBuilt":
				editYearBuilt();
				break;
			
			case "editStableAcreage":
				editStableAcreage();
				break;
				
			case "editMonthlyCost":
				editMonthlyCost();
				break;
				
			case "editMonthlyIncome":
				editMonthlyIncome();
				break;
				
			case "seeAllRiders":
				seeAllRiders();
				break;
				
			case "seeOwners":
				seeOwners();
				break;
				
			case "seeBoarders":
				seeBoarders();
				break;
				
			case "seeStudents":
				seeStudents();
				break;
				
			case "removeRider":
				removeRider();
				break;
				
			case "editRider":
				editRider();
				break;
				
			case "addRider":
				addRider();
				break;
				
			case "removeAllRiders":
				removeAllRiders();
				break;
				
			case "seeAllHorses":
				seeAllHorses();
				break;
				
			case "seeLessonHorses":
				seeLessonHorses();
				break;
				
			case "seeBoardedHorses":
				seeBoardedHorses();
				break;
				
			case "seeResidentHorses":
				seeResidentHorses();
				break;
				
			case "addHorse":
				addHorse();
				break;
				
			case "removeHorse":
				removeHorse();
				break;
				
			case "editHorse":
				editHorse();
				break;
				
			case "removeAllHorses":
				removeAllHorses();
				break;
				
			case "showNextAnnuals":
				showNextAnnuals();
				break;
				
			case "showNextDeworms":
				showNextDeworms();
				break;
				
			case "showNextFarriers":
				showNextFarriers();
				break;
				
			case "overdueAnnuals":
				overdueAnnuals();
				break;
				
			case "overdueDeworms":
				overdueDeworms();
				break;
				
			case "overdueFarriers":
				overdueFarriers();
				break;
				
			case "seeLessons":
				seeLessons();
				break;
				
			case "addLesson":
				addLesson();
				break;
				
			case "editLesson":
				editLesson();
				break;
				
			case "removeLesson":
				removeLesson();
				break;
				
			case "removeAllLessons":
				removeAllLessons();
				break;
			
			default:
				System.out.println("Action not implemented: " + actionName);
		}
	}
	
	/**
	 * Builds all menus used in the program and populates the menus list.
	 * 
	 * Each menu is constructed with an internal name, title, and a set of
	 * MenuOption objects, including a universal quit option.
	 */
	private static void buildMenus() {
		
		//MAIN MENU
		Menu main = new Menu("main", "StableMate Main Menu");
		main.addOption(new MenuOption("l", "Lessons", null, "lessons"));
		main.addOption(new MenuOption("h", "Horses", null, "horses"));
		main.addOption(new MenuOption("r", "Riders", null, "riders"));
		main.addOption(new MenuOption("s", "Stable", null, "stable"));
		addQuit(main);
		menus.add(main);
		
		//LESSONS MENU
		Menu lessons = new Menu("lessons", "Lessons");
		lessons.addOption(new MenuOption("s", "See lessons", "seeLessons", "lessons"));
		lessons.addOption(new MenuOption("a", "Add lesson", "addLesson", "lessons"));
		lessons.addOption(new MenuOption("e", "Edit lesson", "editLesson", "lessons"));
		lessons.addOption(new MenuOption("r", "Remove lesson", "removeLesson", "lessons"));
		lessons.addOption(new MenuOption("d", "Remove all lessons", "removeAllLessons", "lessons"));
		lessons.addOption(new MenuOption("b", "Back", null, "main"));
		addQuit(lessons);
		menus.add(lessons);
		
		//HORSES MENU
		Menu horses = new Menu("horses", "Horses");
		horses.addOption(new MenuOption("s", "See horses", null, "seeHorses"));
		horses.addOption(new MenuOption("c", "Care tasks", null, "careTasks"));
		horses.addOption(new MenuOption("a", "Add horse", "addHorse", "horses"));
		horses.addOption(new MenuOption("e", "Edit horse", "editHorse", "horses"));
		horses.addOption(new MenuOption("r", "Remove horse", "removeHorse", "horses"));
		horses.addOption(new MenuOption("d", "Remove all horses", "removeAllHorses", "horses"));
		horses.addOption(new MenuOption("b", "Back", null, "main"));
		addQuit(horses);
		menus.add(horses);
		
		//CARE TASKS MENU
		Menu careTasks = new Menu("careTasks", "Care Tasks");
		careTasks.addOption(new MenuOption("a", "Show upcoming annuals", "showNextAnnuals", "careTasks"));
		careTasks.addOption(new MenuOption("d", "Show upcoming dewormings", "showNextDeworms", "careTasks"));
		careTasks.addOption(new MenuOption("f", "Show upcoming farrier visits", "showNextFarriers", "careTasks"));
		careTasks.addOption(new MenuOption("x", "Show overdue annuals", "overdueAnnuals", "careTasks"));
		careTasks.addOption(new MenuOption("y", "Show overdue dewormings", "overdueDeworms", "careTasks"));
		careTasks.addOption(new MenuOption("z", "Show overdue farrier visits", "overdueFarriers", "careTasks"));
		careTasks.addOption(new MenuOption("b", "Back", null, "horses"));
		addQuit(careTasks);
		menus.add(careTasks);
		
		//SEE HORSES MENU
		Menu seeHorses = new Menu("seeHorses", "See Horses");
		seeHorses.addOption(new MenuOption("a", "All horses", "seeAllHorses", "seeHorses"));
		seeHorses.addOption(new MenuOption("l", "Lesson horses", "seeLessonHorses", "seeHorses"));
		seeHorses.addOption(new MenuOption("x", "Boarded horses", "seeBoardedHorses", "seeHorses"));
		seeHorses.addOption(new MenuOption("r", "Resident horses", "seeResidentHorses", "seeHorses"));
		seeHorses.addOption(new MenuOption("b", "Back", null, "horses"));
		addQuit(seeHorses);
		menus.add(seeHorses);
		
		//RIDERS MENU
		Menu riders = new Menu("riders", "Riders");
		riders.addOption(new MenuOption("s", "See riders", null, "seeRiders"));
		riders.addOption(new MenuOption("a", "Add rider", "addRider", "riders"));
		riders.addOption(new MenuOption("e", "Edit rider", "editRider", "riders"));
		riders.addOption(new MenuOption("r", "Remove rider", "removeRider", "riders"));
		riders.addOption(new MenuOption("d", "Remove all riders", "removeAllRiders", "riders"));
		riders.addOption(new MenuOption("b", "Back", null, "main"));
		addQuit(riders);
		menus.add(riders);
		
		//SEE RIDERS MENU
		Menu seeRiders = new Menu("seeRiders", "See Riders");
		seeRiders.addOption(new MenuOption("a", "All riders", "seeAllRiders", "seeRiders"));
		seeRiders.addOption(new MenuOption("o", "Owners", "seeOwners", "seeRiders"));
		seeRiders.addOption(new MenuOption("x", "Boarders", "seeBoarders", "seeRiders"));
		seeRiders.addOption(new MenuOption("s", "Students", "seeStudents", "seeRiders"));
		seeRiders.addOption(new MenuOption("b", "Back", null, "riders"));
		addQuit(seeRiders);
		menus.add(seeRiders);
		
		//STABLE MENU
		Menu stable = new Menu("stable", "Stable");
		stable.addOption(new MenuOption("o", "Overview", "stableOverview", "stable"));
		stable.addOption(new MenuOption("n", "Edit name", "editStableName", "stable"));
		stable.addOption(new MenuOption("y", "Edit year built", "editYearBuilt", "stable"));
		stable.addOption(new MenuOption("a", "Edit acreage", "editStableAcreage", "stable"));
		stable.addOption(new MenuOption("c", "Edit monthly cost", "editMonthlyCost", "stable"));
		stable.addOption(new MenuOption("i", "Edit monthly income", "editMonthlyIncome", "stable"));
		stable.addOption(new MenuOption("b", "Back", null, "main"));
		addQuit(stable);
		menus.add(stable);
	}
	
	/**
	 * Adds a universal "Quit" option to the given menu.
	 * 
	 * The Quit option uses the key 'q' and sets the next menu name to "quit".
	 * 
	 * @param menu the Menu to which the Quit option will be added
	 */
	private static void addQuit(Menu menu) {
		
		menu.addOption(new MenuOption("q", "Quit", null, "quit"));
	}
	
	// =========================
	// STABLE METHODS
	// =========================
	
	/**
	 * Displays an overview of the stable, including:
	 * 
	 * Stable name
	 * Year built
	 * Acreage
	 * Owners list
	 * Monthly cost
	 * Monthly income
	 */
	public static void stableOverview() {
		
		String name = display(stable.getName());
		String year = display("" + stable.getYearBuilt());
		String owners = display(listOwners());
		String acreage = display("" + stable.getAcreage());
		String monthlyCost = display("" + stable.getMonthlyCost());
		String monthlyIncome = display("" + stable.getMonthlyIncome());
		
		System.out.println("\n=== " + name + " ===\nYear Built: " + year + "\nAcres: " + acreage + " acres\nOwners: " 
		+ owners + "\nMonthly Cost: $" + monthlyCost + "\nMonthly Income: $" + monthlyIncome);
	}
	
	/**
	 * Builds a comma-separated list of the names of all Riders marked as "owner".
	 * 
	 * @return a string of owner names, or "None" if no owners exist
	 */
	public static String listOwners() {
		
		ArrayList<Rider> owners = new ArrayList<>();
			
		for (int i = 0; i < riders.size(); i++) {
					
			Rider ownerTemp = riders.get(i);
			if (ownerTemp.getType().toLowerCase().equals("owner")) {
						
				owners.add(ownerTemp);
			}
		
		}
		
		if (owners.size() == 0) return "None";
		
		String temp = owners.get(0).getName();
		
		for (int q = 1; q < owners.size(); q++) {
			
			temp += ", " + owners.get(q).getName();
		}
		
		return temp;
	}
	
	/**
	 * Allows the user to edit the stable's name.
	 * Prompts for a new name and updates the Stable object.
	 */
	public static void editStableName() {
		
		System.out.println("The current stable name is: " + stable.getName());
		System.out.println();
		
		String temp = readOther("Please enter new name: ");
		stable.setName(temp);
		
		System.out.println("The new stable name is: " + stable.getName());
	}
	
	/**
	 * Allows the user to edit the year the stable was built.
	 * Prompts for a positive integer and updates the Stable object.
	 */
	public static void editYearBuilt() {
		
		System.out.println("The current year built is: " + stable.getYearBuilt());
		System.out.println();
		
		int temp = readPosInt("Please enter new year (positive whole numbers only): ");
		stable.setYearBuilt(temp);
		
		System.out.println("The new year built is: " + stable.getYearBuilt());
	}
	
	/**
	 * Allows the user to edit the stable's acreage.
	 * Prompts for a positive integer and updates the Stable object.
	 */
	public static void editStableAcreage() {
		
		System.out.println("The current acreage is: " + stable.getAcreage() + " acres");
		System.out.println();
		
		int temp = readPosInt("Please enter new acreage (positive whole numbers only): ");
		stable.setAcreage(temp);
		
		System.out.println("The new acreage is: " + stable.getAcreage() + " acres");
	}
	
	/**
	 * Allows the user to edit the stable's monthly cost.
	 * Prompts for a positive double value and updates the Stable object.
	 */
	public static void editMonthlyCost() {
		
		System.out.println("The current monthly cost is: $" + stable.getMonthlyCost());
		System.out.println();
		
		double temp = readPosDouble("Please enter new monthly cost (no negative numbers): "); 
		stable.setMonthlyCost(temp);
		
		System.out.println("The new monthly cost is: $" + stable.getMonthlyCost());
	}
	
	/**
	 * Allows the user to edit the stable's monthly income.
	 * Prompts for a positive double value and updates the Stable object.
	 */
	public static void editMonthlyIncome() {
		
		System.out.println("The current monthly income is: $" + stable.getMonthlyIncome());
		System.out.println();
		
		double temp = readPosDouble("Please enter new monthly income (no negative numbers): "); 
		stable.setMonthlyIncome(temp);
		
		System.out.println("The new monthly income is: $" + stable.getMonthlyIncome());
	}
	
	/**
	 * Helper method to display a value, printing "Empty" if null or blank.
	 * 
	 * @param value the value to display
	 * @return "Empty" for null/blank strings, otherwise value as a String
	 */
	private static String display(Object value) {
		
		if (value == null) return "Empty";
		if (value instanceof String && ((String) value).length() == 0) return "Empty";
		return value.toString();
	}
	
	/**
	 * Loads stable information from the given file.
	 * 
	 * Expected file format (one per line):
	 * 
	 * Stable name
	 * Year built (int)
	 * Acreage (int)
	 * Monthly cost (double)
	 * Monthly income (double)
	 * 
	 * Owners are inferred from the current list of riders with type "owner".
	 * 
	 * @param file the path to the stable data file
	 */
	private static void loadStableFromFile(String file) {
		
		try (Scanner in = new Scanner(new File(file))) {
			String name = in.nextLine();
			int yearBuilt = Integer.parseInt(in.nextLine());
			int acreage = Integer.parseInt(in.nextLine());
			double monthlyCost = Double.parseDouble(in.nextLine());
			double monthlyIncome = Double.parseDouble(in.nextLine());
			
			ArrayList<Rider> owners = new ArrayList<>();
			
			for (int i = 0; i < riders.size(); i++) {
					
				Rider ownerTemp = riders.get(i);
				if (ownerTemp.getType().toLowerCase().equals("owner")) {
						
					owners.add(ownerTemp);
				}
				
			}
			
			stable = new Stable();
			stable.setName(name);
			stable.setYearBuilt(yearBuilt);
			stable.setAcreage(acreage);
			stable.setMonthlyCost(monthlyCost);
			stable.setMonthlyIncome(monthlyIncome);
			stable.setOwners(owners);
			
		} catch (FileNotFoundException e) {
			System.out.println("stable.txt not found, starting with empty stable.");
			stable = new Stable();
		}
	}
	
	/**
	 * Saves the current stable information to the given file.
	 * 
	 * @param file the path to the stable data file
	 */
	public static void saveStableToFile(String file) {
		
		try (PrintStream out = new PrintStream(new File(file))) {
			
			out.println(stable.getName());
			out.println(stable.getYearBuilt());
			out.println(stable.getAcreage());
			out.println(stable.getMonthlyCost());
			out.println(stable.getMonthlyIncome());
			
		} catch (FileNotFoundException e) {
			System.out.println("Error saving stable data: " + e.getMessage());
		}
	}
	
	// =========================
	// RIDERS METHODS
	// =========================
	
	/**
	 * Displays all riders grouped by type.
	 * 
	 * This method calls:
	 * seeOwners()
	 * seeBoarders()
	 * seeStudents()
	 */
	public static void seeAllRiders() {
		
		seeOwners();
		seeBoarders();
		seeStudents();
	}
	
	/**
	 * Prints a list of riders filtered by type with a given header.
	 * 
	 * @param header the header text to print (e.g. "Owners")
	 * @param type the rider type to filter on (e.g. "owner", "boarder", "student")
	 */
	public static void printRidersByType(String header, String type) {
		
		System.out.println(" === " + header + " ===");
		
		boolean flag = false;
		for (Rider r : riders) {
			
			if (r.getType().equalsIgnoreCase(type)) {
				
				flag = true;
				
				System.out.println("\nName: " + r.getName() + "\nAge: " + r.getAge() + "yo\nWeight: " + r.getWeight() 
				+ "lb\nPhone Number: " + r.getPhoneNumber() + "\nEmail: " + r.getEmail());
			}
		}
		
		if (flag == false) {
			System.out.println("No " + header.toLowerCase() + " found.");
		}
	}
	
	/**
	 * Displays all riders whose type is "owner".
	 */
	public static void seeOwners() { printRidersByType("Owners", "owner"); }
	
	/**
	 * Displays all riders whose type is "boarder".
	 */
	public static void seeBoarders() { printRidersByType("Boarders", "boarder"); }
	
	/**
	 * Displays all riders whose type is "student".
	 */
	public static void seeStudents() { printRidersByType("Students", "student"); }
	
	/**
	 * Prompts the user to add a new rider to the system.
	 * 
	 * This method:
	 * 
	 *   Asks for rider type (Owner/Boarder/Student)
	 *   Collects basic details (name, age, weight, phone, email)
	 *   Creates a new Rider
	 *   Adds the rider to the list and ID map
	 */
	public static void addRider() {
		
		System.out.println("Type? ");
		System.out.println("1) Owner");
		System.out.println("2) Boarder");
		System.out.println("3) Student");
		
		int choice = readIntRange("Enter choice (1-3): ", 1, 3);
		
		String type = "student"; //Default type
		if (choice == 1) type = "owner"; if (choice == 2) type = "boarder"; 
		
		String name = readFullName("Name? (First Last): ");
		int age = readPosInt("Age? (positive whole number): ");
		double weight = readPosDouble("Weight in pounds? (decimal ok): ");
		String phoneNumber = readPhoneNumber("Phone number? (0123456789): ");
		String email = readOther("Email? ");
		
		Rider temp = new Rider(name, phoneNumber, email, type, weight, age);
		riders.add(temp);
		ridersByID.put(temp.getID(), temp);
		
		System.out.println(name + " has been added as a " + temp.getType());
	}
	
	/**
	 * Removes a single rider selected by index.
	 * 
	 * If the rider is an owner of any horses, those horses have their owner set
	 * to null. The rider is also removed from the ID map.
	 */
	public static void removeRider() {
		
		if (riders.isEmpty()) {
			System.out.println("No riders to remove.");
			return;
		}
		
		displayRiders();
		int index = readIntRange("Enter rider number to be removed: ", 1, riders.size()) - 1;
		
		
		Rider removed = riders.remove(index);
		
		ridersByID.remove(removed.getID());
		
		for (Horse h : horses) {
			if (h.getOwner() == removed) {
				h.setOwner(null);
			}
		}
		
		System.out.println("Removed rider: " + removed.getName());
	}
	
	/**
	 * Edits a single rider selected by index.
	 * 
	 * The user can edit:
	 * 
	 * Name
	 * Type
	 * Age
	 * Weighyt
	 * Phone number
	 * Email
	 */
	public static void editRider() {
		
		if (riders.isEmpty()) {
			System.out.println("No riders to edit.");
			return;
		}
		
		displayRiders();
		int index = readIntRange("Enter rider number to edit:", 1, riders.size()) -1;
		
		Rider temp = riders.get(index);
		
		System.out.println("\nEditing: " + temp.getName());
		System.out.println("1) Name");
		System.out.println("2) Type (Owner / Boarder / Student)");
		System.out.println("3) Age");
		System.out.println("4) Weight");
		System.out.println("5) Phone Number");
		System.out.println("6) Email");
		System.out.println("Enter choice: ");
		
		int choice = readIntRange("Select option:", 1, 6);
		
		switch (choice) {
			
			case 1:
				temp.setName(readFullName("New name (First Last):"));
				break;
			case 2:
				System.out.println("1) Owner\n2) Boarder\n3) Student");
				int type = readIntRange("Choose type:", 1, 3);
				if (type == 1) {
					temp.setType("owner");
				} else if (type == 2) {
					temp.setType("boarder");
				} else {
					temp.setType("student");
				}
				break;
			case 3:
				temp.setAge(readPosInt("New age (whole number):"));
				break;
			case 4:
				temp.setWeight(readPosDouble("New weight (pounds):"));
				break;
			case 5:
				temp.setPhoneNumber(readPhoneNumber("New phone number (0123456789):"));
				break;
			case 6:
				temp.setEmail(readOther("New email:"));
				break;
		}
		
		System.out.println("Rider updated.");
	}
	
	/**
	 * Displays all riders with their index, name, type, and email.
	 * Used by other methods to let the user choose a rider.
	 */
	private static void displayRiders() {
		
		System.out.println("Riders:");
		
		if(riders.size() == 0){ System.out.println("No riders."); }
		
		for (int i = 0; i < riders.size(); i++) {
			
			Rider temp = riders.get(i);
			System.out.println((i + 1) + ") " + temp.getName() + " - " + temp.getType() + " - " + temp.getEmail());
		}
	}
	
	/**
	 * Removes all riders from the system after a confirmation prompt.
	 * 
	 * This clears:
	 * 
	 *   The riders list
	 *   The rider ID map
	 * 
	 * All horses with owners will have their owner set to null.
	 */
	public static void removeAllRiders() {
		
		if (riders.isEmpty()) { 
			System.out.println("No riders to be removed."); 
			return;
		}
		
		System.out.println("Are you sure? (YES/NO)");
		String answer = console.nextLine().trim().toLowerCase();
		
		if (!answer.equals("yes")){
			System.out.println("Cancelled.");
			return;
		}
		
		ridersByID.clear();
		riders.clear();
		
		for (Horse h : horses) {
			h.setOwner(null);
		}
		
		System.out.println("All riders have been removed.");
	}
	
	/**
	 * Loads riders from the specified file.
	 * 
	 * Expected file format (per rider, one field per line):
	 * 
	 * Name
	 * ID
	 * Age (int)
	 * Weight (double)
	 * Phone number
	 * Email
	 * Type
	 * 
	 * Existing IDs are registered with IDGenerator.
	 * 
	 * @param file the riders file path
	 */
	private static void loadRidersFromFile(String file) {
		
		try (Scanner in = new Scanner(new File(file))) {
			
			while (in.hasNext()) {
				
				String name = in.nextLine();
				String id = in.nextLine();
				int age = Integer.parseInt(in.nextLine());
				double weight = Double.parseDouble(in.nextLine());
				String phoneNumber = in.nextLine();
				String email = in.nextLine();
				String type = in.nextLine();
			
				Rider temp = new Rider();
				temp.setName(name);
				temp.setID(id);
				temp.setAge(age);
				temp.setWeight(weight);
				temp.setPhoneNumber(phoneNumber);
				temp.setEmail(email);
				temp.setType(type);
				
				IDGenerator.registerExisting(id);
				riders.add(temp);
				ridersByID.put(temp.getID(), temp);
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("riders.txt not found, starting with no riders.");
			riders = new ArrayList<>();
		}
	}
	
	/**
	 * Saves all riders to the specified file.
	 * 
	 * @param file the riders file path
	 */
	public static void saveRidersToFile(String file) {
		
		try (PrintStream out = new PrintStream(new File(file))) {
			
			for (int i = 0; i < riders.size(); i++) {
				
				Rider temp = riders.get(i);
				out.println(temp.getName());
				out.println(temp.getID());
				out.println(temp.getAge());
				out.println(temp.getWeight());
				out.println(temp.getPhoneNumber());
				out.println(temp.getEmail());
				out.println(temp.getType());
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("Error saving rider data: " + e.getMessage());
		}
	}
	
	/**
	 * Looks up a Rider by their unique ID.
	 * 
	 * @param id the rider ID
	 * @return the matching rider, or null if not found
	 */
	public static Rider getRiderByID(String id) {
		
		return ridersByID.get(id);
	}
	
	// =========================
	// HORSES METHODS
	// =========================
	
	/**
	 * Displays all horses grouped by status:
	 * lesson, boarded, and resident.
	 */
	public static void seeAllHorses() {
		
		seeLessonHorses();
		seeBoardedHorses();
		seeResidentHorses();
	}
	
	/**
	 * Prints horses filtered by status with a given header.
	 * 
	 * @param header the header text (e.g. "Lesson Horses")
	 * @param status the status string (e.g. "lesson", "boarded", "resident")
	 */
	public static void printHorsesByStatus(String header, String status) {
		
		System.out.println("=== " + header + " ===");
		
		boolean flag = false;
		
		for (Horse h : horses) {
			
			if (h.getStatus().equalsIgnoreCase(status)) {
				
				flag = true;
				
				String ownerName;
				if (h.getOwner() == null) { 
					ownerName = "None";
				} else {
					ownerName = h.getOwner().getName();
				}
				
				System.out.println("\nName: " + h.getName() + "\nOwner: " + ownerName + "\nBreed: " + h.getBreed() + 
				"\nColor: " + h.getColor() + "\nHands: " + h.getHands() + " hands\nWeight: " + h.getWeight() + "lb\nSex: " 
				+ h.getSex() + "\nFood: " + h.getFood() + "\nCompatible horses: " + compToString(h) + "\nLast annual date: " 
				+ h.getLastAnnual() + "\nLast deworm date: " + h.getLastDeworm() + "\nLast farrier date: " + h.getLastFarrier());
			}
		}
		
		if (flag == false) {
			
			System.out.println("No " + header.toLowerCase() + " found");
		}
	}
	
	/**
	 * Displays all horses whose status is "lesson".
	 */
	public static void seeLessonHorses() { printHorsesByStatus("Lesson Horses", "lesson"); }
	
	/**
	 * Displays all horses whose status is "boarded".
	 */
	public static void seeBoardedHorses() { printHorsesByStatus("Boarded Horses", "boarded"); }
	
	/**
	 * Displays all horses whose status is "resident".
	 */
	public static void seeResidentHorses() { printHorsesByStatus("Resident Horses", "resident"); }
	
	/**
	 * Converts a horse's compatibility list into a comma-separated string
	 * of horse names.
	 * 
	 * @param horse the horse whose compatible partners should be listed
	 * @return a string of names, or "None" if there are no compatible horses
	 */
	public static String compToString(Horse horse) {
		
		ArrayList<Horse> comp = horse.getComp();
		
		if(comp == null || comp.size() == 0) return "None";
		
		String temp = comp.get(0).getName();
		
		for (int i = 1; i < comp.size(); i++) {
			
			temp += ", " + comp.get(i).getName();
		}
		
		return temp;
	}
	
	/**
	 * Loads horses from the specified file.
	 * 
	 * Expected file format (per horse, one field per line):
	 * 
	 *   Name
	 *   ID
	 *   Owner ID (may be blank)
	 *   Status
	 *   Breed
	 *   Color
	 *   Hands (double)
	 *   Weight (double)
	 *   Sex
	 * 	 Food
	 *   Last annual date
	 *   Last deworm date
	 *   Last farrier date
	 *   Annual frequency (int, days)
	 *   Deworm frequency (int, days)
	 *   Farrier frequency (int, days)
	 *   Compatible horse IDs (comma-separated)
	 * 
	 * Compatible horses are re-linked after all horses are loaded.
	 * 
	 * @param file the horses file path
	 */
	private static void loadHorsesFromFile(String file) {
		
		try (Scanner in = new Scanner(new File(file))) {
			
			while (in.hasNext()) {
				
				String name = in.nextLine();
				String id = in.nextLine();
				String ownerID = in.nextLine();
				Rider owner = getRiderByID(ownerID);
				String status = in.nextLine();
				String breed = in.nextLine();
				String color = in.nextLine();
				double hands = Double.parseDouble(in.nextLine());
				double weight = Double.parseDouble(in.nextLine());
				String sex = in.nextLine();
				String food = in.nextLine();
				LocalDate lastAnnual = parseDate(in.nextLine());
				LocalDate lastDeworm = parseDate(in.nextLine());
				LocalDate lastFarrier = parseDate(in.nextLine());
				int annualFreq = Integer.parseInt(in.nextLine());
				int dewormFreq = Integer.parseInt(in.nextLine());
				int farrierFreq = Integer.parseInt(in.nextLine());
				String comp = in.nextLine();
				String[] parts = comp.split(", ");
			
				Horse temp = new Horse();
				temp.setName(name);
				temp.setID(id);
				temp.setOwner(owner);
				temp.setStatus(status);
				temp.setBreed(breed);
				temp.setColor(color);
				temp.setHands(hands);
				temp.setWeight(weight);
				temp.setSex(sex);
				temp.setFood(food);
				temp.setLastAnnual(lastAnnual);
				temp.setLastDeworm(lastDeworm);
				temp.setLastFarrier(lastFarrier);
				temp.setAnnualFreq(annualFreq);
				temp.setDewormFreq(dewormFreq);
				temp.setFarrierFreq(farrierFreq);
				
				ArrayList<String> compIDs = new ArrayList<>();
				for (String p : parts) {
					
					compIDs.add(p.trim());
				}
				temp.setCompIDs(compIDs);
				
				IDGenerator.registerExisting(id);
				horses.add(temp);
				horsesByID.put(temp.getID(), temp);
			}
			
			for (Horse h : horses) {
				
				ArrayList<Horse> comps = new ArrayList<>();
				ArrayList<String> ids = h.getCompIDs();
				
				if (ids != null) {
					
					for (String cid : ids) {
						
						if (cid != null && cid.trim().length() > 0) {
							
							Horse other = getHorseByID(cid.trim());
						
							if (other != null) {
							
								comps.add(other);
							}
						}
					}
				}
				
				h.setComps(comps);
			}
		} catch (FileNotFoundException e) {
			System.out.println("horses.txt not found, starting with no horses.");
			horses = new ArrayList<>();
		}
	}
	
	/**
	 * Looks up a Horse by its unique ID.
	 * 
	 * @param id the horse ID
	 * @return the matching horse, or null if not found
	 */
	public static Horse getHorseByID(String id) {
		
		return horsesByID.get(id);
	}
	
	/**
	 * Saves all horses to the specified file.
	 * 
	 * @param file the horses file path
	 */
	public static void saveHorsesToFile(String file) {
		
		try (PrintStream out = new PrintStream(new File(file))) {
			
			for (int i = 0; i < horses.size(); i++) {
				
				Horse temp = horses.get(i);
				out.println(temp.getName());
				out.println(temp.getID());
				Rider owner = temp.getOwner();
				if (owner == null) {
					out.println("");
				} else {
					out.println(owner.getID());
				}
				out.println(temp.getStatus());
				out.println(temp.getBreed());
				out.println(temp.getColor());
				out.println(temp.getHands());
				out.println(temp.getWeight());
				out.println(temp.getSex());
				out.println(temp.getFood());
				out.println(formatDate(temp.getLastAnnual()));
				out.println(formatDate(temp.getLastDeworm()));
				out.println(formatDate(temp.getLastFarrier()));
				out.println(temp.getAnnualFreq());
				out.println(temp.getDewormFreq());
				out.println(temp.getFarrierFreq());
				
				ArrayList<Horse> comp = temp.getComp();
				
				if (comp == null || comp.size() == 0) {
					out.println("");
				} else {
					
					out.print(comp.get(0).getID());
					
					for (int j = 1; j < comp.size(); j++) {
						
						out.print(", " + comp.get(j).getID());
					}
					
					out.println();
				}
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("Error saving horse data: " + e.getMessage());
		}
	}
	
	/**
	 * Prompts the user to add a new horse to the system.
	 * 
	 * This method:
	 * 
	 *   Collects horse details and status
	 *   Looks up (or creates) the owner as a Rider
	 *   Collects care dates and frequencies
	 *   Collects compatibility with other horses
	 *   Adds the horse to the list, map, and care queues
	 */
	public static void addHorse() {
		
		String name = readOther("Name?");
		System.out.println("Status?: ");
		System.out.println("1) Lesson");
		System.out.println("2) Boarded");
		System.out.println("3) Resident");
		
		int choice = readIntRange("Enter choice (1-3): ", 1, 3);
		
		String status = "resident"; //Default status
		if (choice == 1) status = "lesson"; if (choice == 2) status = "boarded"; 
		
		String ownerName = readFullName("Owner name? (First Last):");
		String phoneNumber = readPhoneNumber("Owner phone number? (1234567890):");
		
		Rider owner = new Rider();
		boolean flag = false;
		for (int i = 0; i < riders.size() && flag == false; i++) {
			
			Rider temp = riders.get(i);
			if (temp.getName().equals(ownerName) && temp.getPhoneNumber().equals(phoneNumber)) {
				owner = temp;
				flag = true;
			} 
		}
		
		if (flag == false) {
			
			System.out.println("No existing owner found. Redirecting to create new horse owner.");
			addRider();
			owner = riders.get(riders.size() - 1);
		}
		
		String breed = readOther("Breed?:");
		String color = readOther("Color?:");
		Double hands = readPosDouble("Height? (hands):");
		Double weight = readPosDouble("Weight? (pounds):");
		String sex = readOther("Sex? (Ex: Gelding | Mare | Stallion | Filly | Colt)");
		String food = readOther("Food?:");
		LocalDate lastAnnual = readDate("Date of last annual? (YYYY-MM-DD or blank for none):");
		LocalDate lastDeworm = readDate("Date of last deworm? (YYYY-MM-DD or blank for none):");
		LocalDate lastFarrier = readDate("Date of last farrier? (YYYY-MM-DD or blank for none):");
		int annualFreq = readPosInt("Frequency of annual? (positive whole number in days):");
		int dewormFreq = readPosInt("Frequency of deworming? (positive whole number in days):");
		int farrierFreq = readPosInt("Frequency of farrier? (positive whole number in days):");
		
		ArrayList<Horse> compatibles = readCompatibleHorses();
		
		Horse temp = new Horse(name, breed, color, sex, food, status, owner, compatibles, hands, weight, lastAnnual, 
		lastDeworm, lastFarrier, annualFreq, dewormFreq, farrierFreq);
		horses.add(temp);
		horsesByID.put(temp.getID(), temp);
		annualPQ.add(temp);
		dewormPQ.add(temp);
		farrierPQ.add(temp);
		
		System.out.println(name + " has been added as a " + temp.getStatus() + " horse");
	}
	
	/**
	 * Removes a single horse selected by index.
	 * 
	 * The horse is removed from:
	 * 
	 *   The horses list
	 *   The horse ID map
	 *   All care queues
	 */
	public static void removeHorse() {
		
		if (horses.isEmpty()) {
			System.out.println("No horses to remove.");
			return;
		}
		
		displayHorses();
		
		int index = readIntRange("Enter horse number to remove:", 1, horses.size()) - 1;
		
		Horse removed = horses.remove(index);
		horsesByID.remove(removed.getID());
		
		if (annualPQ != null) annualPQ.remove(removed);
		if (dewormPQ != null) dewormPQ.remove(removed);
		if (farrierPQ != null) farrierPQ.remove(removed);
		
		System.out.println(removed.getName() + " has been removed.");
	}
	
	/**
	 * Allows the user to edit details of a single horse selected by index.
	 * 
	 * The user can change:
	 * 
	 *   Basic info (name, breed, color, sex, food)
	 *   Owner and status
	 *   Height and weight
	 *   Care dates and frequencies
	 *   Compatibility list
	 * 
	 * Care date updates also refresh the horse's entry in the relevant queue.
	 */
	public static void editHorse() {
		
		if (horses.isEmpty()) {
			System.out.println("No horses to edit.");
			return;
		}
		
		displayHorses();
		
		int index = readIntRange("Enter horse number to edit:", 1, horses.size()) - 1;
		
		Horse temp = horses.get(index);
		
		System.out.println("\nEditing: " + temp.getName());
		System.out.println("What would you like to edit?");
		System.out.println("1) Name");
		System.out.println("2) Owner");
		System.out.println("3) Status (Lesson / Boarded / Resident)");
		System.out.println("4) Breed");
		System.out.println("5) Color");
		System.out.println("6) Height");
		System.out.println("7) Weight");
		System.out.println("8) Sex");
		System.out.println("9) Food");
		System.out.println("10) Last Annual");
		System.out.println("11) Last Deworm");
		System.out.println("12) Last Farrier");
		System.out.println("13) Annual Frequency");
		System.out.println("14) Deworm Frequency");
		System.out.println("15) Farrier Frequency");
		System.out.println("16) Compatibility");
		
		int choice = readIntRange("Enter choice:", 1, 16);
		
		switch (choice) {
			
			case 1:
				temp.setName(readOther("New name:"));
				break;
			case 2:
				String ownerName = readFullName("Owner name? (First Last):");
				String phoneNumber = readPhoneNumber("Owner phone number? (0123456789):");
				
				Rider owner = null;
				
				for (Rider r : riders) {
					
					if (r.getName().equals(ownerName) && r.getPhoneNumber().equals(phoneNumber)) {
					
						owner = r;
						break;
					} 
				}
				
				if (owner == null) {
				
					System.out.println("No existing owner found. Creating new rider.");
					addRider();
					owner = riders.get(riders.size() - 1);
				}
				
				temp.setOwner(owner);
				break;
			case 3:
				System.out.println("1) Lesson\n2) Boarded\n3) Resident");
				int status = readIntRange("Select:", 1, 3);
				String newStatus;
				if (status == 1) { 
					newStatus = "lesson"; 
				} else if (status == 2) {
					newStatus = "boarded";
				} else {
					newStatus = "resident";
				}
				temp.setStatus(newStatus);
				break;
			case 4:
				temp.setBreed(readOther("New breed:"));
				break;
			case 5:
				temp.setColor(readOther("New color:"));
				break;
			case 6:
				temp.setHands(readPosDouble("New height (hands):"));
				break;
			case 7: 
				temp.setWeight(readPosDouble("New weight (pounds):"));
				break;
			case 8: 
				temp.setSex(readOther("New sex:"));
				break;
			case 9: 
				temp.setFood(readOther("New food:"));
				break;
			case 10: 
				LocalDate newA = readDate("New last annual (YYYY-MM-DD or 'null' to clear):", temp.getLastAnnual());
				aVisit(temp, newA);
				break;
			case 11: 
				LocalDate newD = readDate("New last deworm (YYYY-MM-DD or 'null' to clear):", temp.getLastDeworm());
				dVisit(temp, newD);
				break;
			case 12: 
				LocalDate newF = readDate("New last farrier (YYYY-MM-DD or 'null' to clear):", temp.getLastFarrier());
				fVisit(temp, newF);
				break;
			case 13: 
				temp.setAnnualFreq(readPosInt("New annual frequency (whole number in days):"));
				break;
			case 14: 
				temp.setDewormFreq(readPosInt("New deworming frequency (whole number in days):"));
				break;
			case 15: 
				temp.setFarrierFreq(readPosInt("New farrier frequency (whole number in days):"));
				break;
			case 16: 
				temp.setComp(readCompatibleHorses());
				break;
				
			default:
				System.out.println("No changes made.");
		}
		
		System.out.println("Horse updated.");
	}
	
	/**
	 * Displays all horses with an index, showing name, sex, and color.
	 * Used by other methods to select a horse.
	 */
	private static void displayHorses() {
		
		System.out.println("Horses:");
		
		if(horses.size() == 0){ System.out.println("No horses."); }
		
		for (int i = 0; i < horses.size(); i++) {
			
			Horse temp = horses.get(i);
			System.out.println("(" + (i + 1) + ") " + temp.getName() + " | " + temp.getSex() + " | " + temp.getColor());
		}
	}
	
	/**
	 * Removes all horses from the system after confirmation.
	 * 
	 * This clears:
	 * 
	 *   The horses list and ID map
	 *   All care queues
	 */
	public static void removeAllHorses() {
		
		if (horses.isEmpty()) { 
			System.out.println("No horses to be removed."); 
			return;
		}
		
		System.out.println("Are you sure? (YES/NO)");
		String answer = console.nextLine().trim().toLowerCase();
			
		if (!answer.equals("yes")) {
			System.out.println("Cancelled.");
			return;
		}
		
		horses.clear();
		horsesByID.clear();
		annualPQ.clear();
		dewormPQ.clear();
		farrierPQ.clear();
		
		System.out.println("All horses have been removed.");
	}
	
	/**
	 * Updates a horse's last annual date and refreshes its position
	 * in the annual priority queue.
	 * 
	 * @param h the horse
	 * @param date the new last annual date
	 */
	public static void aVisit(Horse h, LocalDate date) {
		
		h.setLastAnnual(date);
		
		//update position in priority queue
		annualPQ.remove(h);
		annualPQ.add(h);
	}
	
	/**
	 * Updates a horse's last deworm date and refreshes its position
	 * in the deworm priority queue.
	 * 
	 * @param h the horse
	 * @param date the new last deworm date
	 */
	public static void dVisit(Horse h, LocalDate date) {
		
		h.setLastDeworm(date);
		
		//update position in priority queue
		dewormPQ.remove(h);
		dewormPQ.add(h);
	}
	
	/**
	 * Updates a horse's last farrier date and refreshes its position
	 * in the farrier priority queue.
	 * 
	 * @param h the horse
	 * @param date the new last farrier date
	 */
	public static void fVisit(Horse h, LocalDate date) {
		
		h.setLastFarrier(date);
		
		//update position in priority queue
		farrierPQ.remove(h);
		farrierPQ.add(h);
	}
	
	/**
	 * Displays up to the next five horses that are due for annual vet visits,
	 * in order of soonest due date.
	 * 
	 * Uses a copy of the annual priority queue for display only.
	 */
	public static void showNextAnnuals() {
		
		if (annualPQ == null || annualPQ.isEmpty()) {
			
			System.out.println("No horses in annual queue.");
			return;
		}
		
		System.out.println("=== Next annual vet visits ===");
		//Only top handful (all that matters) not all because that could be a lot
		
		PriorityQueue<Horse> copy = new PriorityQueue<>(annualPQ);
		
		int i = 0;
		
		while (!copy.isEmpty() && i < 5) {
			
			Horse h = copy.remove();
			LocalDate due = h.getNextAnnualDue();
			if (due != null) {
				System.out.println("(" + (i + 1) + ") " + h.getName() + " | Next annual: " + due);
				i++;
			}
		}
		
		if (i == 0) {
			System.out.println("No horses with annual due dates.");
		}
	}
	
	/**
	 * Displays up to the next five horses that are due for deworming,
	 * in order of soonest due date.
	 */
	public static void showNextDeworms() {
		
		if (dewormPQ == null || dewormPQ.isEmpty()) {
			
			System.out.println("No horses in deworm queue.");
			return;
		}
		
		System.out.println("=== Next dewormings ===");
		//Only top handful (all that matters) not all because that could be a lot
		
		PriorityQueue<Horse> copy = new PriorityQueue<>(dewormPQ);
		
		int i = 0;
		
		while (!copy.isEmpty() && i < 5) {
			
			Horse h = copy.remove();
			LocalDate due = h.getNextDewormDue();
			if (due != null) {
				System.out.println("(" + (i + 1) + ") " + h.getName() + " | Next deworm: " + due);
				i++;
			}
		}
		
		if (i == 0) {
			System.out.println("No horses with deworm due dates.");
		}
	}
	
	/**
	 * Displays up to the next five horses that are due for farrier visits,
	 * in order of soonest due date.
	 */
	public static void showNextFarriers() {
		
		if (farrierPQ == null || farrierPQ.isEmpty()) {
			
			System.out.println("No horses in farrier queue.");
			return;
		}
		
		System.out.println("=== Next farrier visits ===");
		//Only top handful (all that matters) not all because that could be a lot
		
		PriorityQueue<Horse> copy = new PriorityQueue<>(farrierPQ);
		
		int i = 0;
		
		while (!copy.isEmpty() && i < 5) {
			
			Horse h = copy.remove();
			LocalDate due = h.getNextFarrierDue();
			if (due != null) {
				System.out.println("(" + (i + 1) + ") " + h.getName() + " | Next farrier: " + due);
				i++;
			}
		}
		
		if (i == 0) {
			System.out.println("No horses with farrier due dates.");
		}
	}
	
	/**
	 * Displays all horses that are overdue for annual vet checks
	 * as of today's date.
	 */
	public static void overdueAnnuals() {
		
		LocalDate today = LocalDate.now();
		System.out.println("=== Overdue annual vet checks as of " + today + " ===");
		boolean flag = false;
		int i = 1;
		
		for (Horse h : horses) {
			
			if (h.isAnnualOverdue(today)) {
				flag = true;
				System.out.println("(" + i + ")" + h.getName() + " | Last: " + h.getLastAnnual() + " | Next due: " 
				+ h.getNextAnnualDue());
				i++;
			}
		}
		
		if (flag == false) {
			System.out.println("No horses are overdue for annual vet checks.");
		}
	}
	
	/**
	 * Displays all horses that are overdue for deworming as of today's date.
	 */
	public static void overdueDeworms() {
		
		LocalDate today = LocalDate.now();
		System.out.println("=== Overdue deworming as of " + today + " ===");
		boolean flag = false;
		int i = 1;
		
		for (Horse h : horses) {
			
			if (h.isDewormOverdue(today)) {
				flag = true;
				System.out.println("(" + i + ")" + h.getName() + " | Last: " + h.getLastDeworm() + " | Next due: " 
				+ h.getNextDewormDue());
				i++;
			}
		}
		
		if (flag == false) {
			System.out.println("No horses are overdue for deworming.");
		}
	}
	
	/**
	 * Displays all horses that are overdue for farrier visits as of today's date.
	 */
	public static void overdueFarriers() {
		
		LocalDate today = LocalDate.now();
		System.out.println("=== Overdue farrier visits as of " + today + " ===");
		boolean flag = false;
		int i = 1;
		
		for (Horse h : horses) {
			
			if (h.isFarrierOverdue(today)) {
				flag = true;
				System.out.println("(" + i + ")" + h.getName() + " | Last: " + h.getLastFarrier() + " | Next due: " 
				+ h.getNextFarrierDue());
				i++;
			}
		}
		
		if (flag == false) {
			System.out.println("No horses are overdue for farrier visits.");
		}
	}
	
	// =========================
	// GENERAL / UTILITY METHODS
	// =========================
	
	/**
	 * Initializes all priority queues (annual, deworm, farrier, and lessons)
	 * and populates them with the current horses and lessons.
	 */
	private static void initializeQueues() {
		
		annualPQ = new PriorityQueue<>(ANNUAL_COMPARATOR);
		dewormPQ = new PriorityQueue<>(DEWORM_COMPARATOR);
		farrierPQ = new PriorityQueue<>(FARRIER_COMPARATOR);
		lessonsPQ = new PriorityQueue<>(LESSONS_COMPARATOR);
		
		for (Horse h : horses) {
			
			annualPQ.add(h);
			dewormPQ.add(h);
			farrierPQ.add(h);
		}
		
		for (Lesson l : lessons) {
			
			lessonsPQ.add(l);
		}
	}
	
	/**
	 * Reads a date from the user in YYYY-MM-DD format.
	 * 
	 * If the user enters a blank line, null is returned.
	 * The user is re-prompted on invalid input.
	 * 
	 * @param str the prompt message
	 * @return the parsed LocalDate, or null if left blank
	 */
	private static LocalDate readDate(String str) {
		
		while (true) {
			
			System.out.println(str);
			String temp = console.nextLine().trim();
			
			if (temp.isEmpty()) {
				return null; //no date
			}
			
			try {
				
				return LocalDate.parse(temp); // wants YYYY-MM-DD style
			} catch (Exception e) {
				System.out.println("Invalid date format. Please try again in YYYY-MM-DD format.");
			}
		}
	}
	
	/**
	 * Reads a time from the user in HH:MM 24-hour format.
	 * 
	 * If the user enters a blank line, null is returned.
	 * The user is re-prompted on invalid input.
	 * 
	 * @param str the prompt message
	 * @return the parsed LocalTime, or null if left blank
	 */
	private static LocalTime readTime(String str) {
		
		while (true) {
			
			System.out.println(str);
			String temp = console.nextLine().trim();
			
			if (temp.isEmpty()) {
				return null; 
			}
			
			try {
				
				return LocalTime.parse(temp); 
			} catch (Exception e) {
				System.out.println("Invalid time format. Please try again in 00:00 24 hour format.");
			}
		}
	}
	
	/**
	 * Reads a date from the user, displaying the current value and allowing:
	 * 
	 *   Blank input to keep current
	 *   "null" to clear the date
	 *   A new date in YYYY-MM-DD format
	 * 
	 * @param str the prompt message
	 * @param current the current date value
	 * @return the updated date (possibly unchanged), or null if cleared
	 */
	private static LocalDate readDate(String str, LocalDate current) {
		
		while (true) {
			
			String text;
			
			if (current == null) { 
				text = "none"; 
			} else {
				text = current.toString();
			}
			
			System.out.println(str + "(Current: " + text + " )");
			
			String temp = console.nextLine().trim();
			
			if (temp.isEmpty()) {
				return current; //just keep existing last date
			}
			
			if (temp.equalsIgnoreCase("null")) {
				return null; //clear
			}
			
			try {
				
				return LocalDate.parse(temp); // wants YYYY-MM-DD style
			} catch (Exception e) {
				System.out.println("Invalid date format. Please try again in YYYY-MM-DD format.");
			}
		}
	}
	
	/**
	 * Formats a LocalDate for file output.
	 * 
	 * @param date the date
	 * @return null if the date is null, otherwise the date as a String
	 */
	private static String formatDate(LocalDate date) {
		
		if (date == null) return "null";
		
		return date.toString();
	}
	
	/**
	 * Formats a LocalTime for file output.
	 * 
	 * @param time the time
	 * @return null if the time is null, otherwise the time as a String
	 */
	private static String formatTime(LocalTime time) {
		
		if (time == null) return "null";
		
		return time.toString();
	}
	
	/**
	 * Parses a date string, treating null, blank, or "null" as no date.
	 * 
	 * @param str the date string
	 * @return the parsed date, or null if the string is blank or "null"
	 */
	private static LocalDate parseDate(String str) {
		
		if (str == null) return null;
		
		str = str.trim();
		
		if(str.isEmpty() || str.equalsIgnoreCase("null")) {
			return null;
		}
		
		return LocalDate.parse(str);
	}
	
	/**
	 * Parses a time string, treating null, blank, or "null" as no time.
	 * 
	 * @param str the time string
	 * @return the parsed time, or null if the string is blank or "null"
	 */
	private static LocalTime parseTime(String str) {
		
		if (str == null) return null;
		
		str = str.trim();
		
		if(str.isEmpty() || str.equalsIgnoreCase("null")) {
			return null;
		}
		
		return LocalTime.parse(str);
	}
	
	/**
	 * Reads an integer from the user with a prompt, re-prompting
	 * until a valid integer is entered.
	 * 
	 * @param num the prompt message
	 * @return the integer entered
	 */
	private static int readInt(String num) {
		
		while (true) {
			System.out.println(num);
			String line = console.nextLine().trim();
			try {
				return Integer.parseInt(line);
			} catch (Exception e){
				System.out.println("Please type a number.");
			}
		}
	}
	
	/**
	 * Reads a positive integer (greater than zero) from the user.
	 * 
	 * @param num the prompt message
	 * @return a positive integer
	 */
	private static int readPosInt(String num) {
		
		while(true) {
			int value = readInt(num);
			if (value <= 0) {
				System.out.println("Please enter value greater than zero.");
			} else {
				return value;
			}
		}
	}
	
	/**
	 * Reads a double value from the user with a prompt, re-prompting
	 * until a valid number is entered.
	 * 
	 * @param num the prompt message
	 * @return the double value entered
	 */
	private static double readDouble(String num) {
		
		while (true) {
			System.out.println(num);
			String line = console.nextLine().trim();
			try {
				return Double.parseDouble(line);
			} catch (Exception e) {
				System.out.println("Please type a number.");
			}
		}
	}
	
	/**
	 * Reads a positive double (greater than zero) from the user.
	 * 
	 * @param num the prompt message
	 * @return a positive double
	 */
	private static double readPosDouble(String num) {
		
		while(true) {
			double value = readDouble(num);
			if (value <= 0) {
				System.out.println("Please enter value greater than zero.");
			} else {
				return value;
			}
		}
	}
	
	/**
	 * Reads a full name from the user, requiring at least one space
	 * between first and last name.
	 * 
	 * @param str the prompt message
	 * @return the full name entered
	 */
	private static String readFullName(String str) {
		
		while (true) {
			System.out.println(str);
			String name = console.nextLine().trim();
			if (name.isEmpty()) {
				System.out.println("No name entered");
			} else if (!name.contains(" ")) {
				System.out.println("Please enter both FIRST and LAST name with a space between");
			} else {
				return name;
			}
		}
	}
	
	/**
	 * Reads a phone number from the user, requiring exactly 10 digits
	 * with numeric characters only.
	 * 
	 * @param str the prompt message
	 * @return the phone number string
	 */
	private static String readPhoneNumber(String str) {
		
		while(true) {
			System.out.println(str);
			String num = console.nextLine().trim();
			if (num.length() != 10 || !isAllNumbers(num)) {
				System.out.println("Please enter 10 digit number. Numbers only.");
			} else {
				return num;
			}
		}
	}
	
	/**
	 * Returns whether a string consists entirely of digits.
	 * 
	 * @param str the string to test
	 * @return true if every character is a digit
	 */
	private static boolean isAllNumbers(String str) {
		
		for (int i = 0; i < str.length(); i++) {
			
			if(!Character.isDigit(str.charAt(i))) { 
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Reads an integer in the given inclusive range from the user.
	 * 
	 * @param str the prompt message
	 * @param min the minimum allowed value
	 * @param max the maximum allowed value
	 * @return an integer between min and max, inclusive
	 */
	private static int readIntRange(String str, int min, int max) {
		
		while (true) {
			int num = readInt(str);
			if (num < min || num > max) {
				System.out.println("Please enter number between " + min + " and " + max);
			} else {
				return num;
			}
		}
	}
	
	/**
	 * Reads a non-blank string from the user.
	 * 
	 * @param str the prompt message
	 * @return the non-empty string entered
	 */
	private static String readOther(String str) {
		
		while (true) {
			System.out.println(str);
			String temp = console.nextLine().trim();
			if (temp.isEmpty()) {
				System.out.println("Input cannot be blank");
			} else {
				return temp;
			}
		}
	}
	
	/**
	 * Allows the user to choose a list of compatible horses by index.
	 * 
	 * The user can:
	 * 
	 *   See all existing horses and their indices
	 *   Enter space-separated horse numbers
	 *   Press ENTER to indicate none
	 * 
	 * Invalid or out-of-range entries cause a re-prompt.
	 * 
	 * @return a list of compatible horses (possibly empty)
	 */
	private static ArrayList<Horse> readCompatibleHorses() {
		
		ArrayList<Horse> compatibles = new ArrayList<>();
		
		if (horses.isEmpty()) {
			System.out.println("No other horses in system yet, so no compatibility");
			return compatibles;
		}
		
		while (true) {
			System.out.println("Existing horses: ");
			for (int i = 0; i < horses.size(); i++) {
				System.out.println(" (" + (i + 1) + ") " + horses.get(i).getName() + " | " + horses.get(i).getSex());
			}
			
			System.out.println("Enter compatible horse numbers (space separated).\nPress ENTER if none.");
			
			String numbers = console.nextLine().trim();
			
			if (numbers.isEmpty()) {
				return compatibles;
			}
			
			String[] pieces = numbers.split("\\s+");
			
			ArrayList<Horse> temp = new ArrayList<>();
			boolean flag = true;
			
			for (String str : pieces) {
				
				try {
					int index = Integer.parseInt(str);
					
					if (index < 1 || index > horses.size()) {
						System.out.println("Number: " + index + " is not an option. Please choose between 1 and " + horses.size());
						flag = false;
						break;
					}
					
					Horse tempH = horses.get(index - 1);
					temp.add(tempH);
					
				} catch (NumberFormatException e) {
					System.out.println(str + " is not a whole number.");
					flag = false;
					break;
				}
			}
			
			if (flag == true) {
				return temp;
			} else {
				System.out.println("Please try again.");
			}
		}
	}
	
	// =========================
	// LESSONS METHODS
	// =========================
	
	/**
	 * Displays all lessons in order based on the lesson priority queue.
	 * 
	 * For each lesson, prints:
	 * 
	 *   Rider name
	 *   Horse name
	 *   Date
	 *   Time
	 */
	public static void seeLessons() {
		
		if (lessonsPQ == null || lessonsPQ.isEmpty()) {
			
			System.out.println("No lessons in lessons queue.");
			return;
		}
		
		System.out.println("=== Lessons ===");
		
		int i = 0;
		
		PriorityQueue<Lesson> copy = new PriorityQueue<>(lessonsPQ);
		
		while (!copy.isEmpty()) {
			
			Lesson l = copy.remove();
			
			String riderName = "None";
			if (l.getRider() != null) {
				riderName = l.getRider().getName();
			}
			String horseName = "None";
			if (l.getHorse() != null) {
				horseName = l.getHorse().getName();
			}
			String dateStr = "None";
			if (l.getDate() != null) {
				dateStr = l.getDate().toString();
			}
			String timeStr = "None";
			if (l.getTime() != null) {
				timeStr = l.getTime().toString();
			}
			
			System.out.println("(" + (i+1) + ") Rider: " + riderName + " | Horse: " + horseName + " | Date: " + dateStr
			+ " | Time: " + timeStr);
			i++;
		}
		
		if (i == 0) {
			System.out.println("No lessons.");
		}
	}
	
	/**
	 * Prompts the user to add a new lesson.
	 * 
	 * This method:
	 * 
	 *   Collects date, time, and duration
	 *   Finds or creates the rider
	 *   Finds or creates a horse
	 *   Creates a new Lesson
	 *   Adds it to the lesson list and priority queue
	 */
	public static void addLesson() {
		
		LocalDate date = readDate("Date? (YYYY-MM-DD):");
		LocalTime time = readTime("Time? (00:00 | 24 hour time):");
		int duration = readPosInt("Duration? (Whole number of minutes):");
		String riderName = readFullName("Rider name? (First Last):");
		String phoneNumber = readPhoneNumber("Rider phone number? (1234567890):");
		Rider rider = new Rider();
		boolean flag = false;
		for (int i = 0; i < riders.size() && flag == false; i++) {
			
			Rider temp = riders.get(i);
			if (temp.getName().equals(riderName) && temp.getPhoneNumber().equals(phoneNumber)) {
				rider = temp;
				flag = true;
			}
		}
		
		if (flag == false) {
			
			System.out.println("No existing rider found. Redirecting to create new horse rider.");
			addRider();
			rider = riders.get(riders.size() - 1);
		}
		
		System.out.println("=== Horse Options ===");
		int j = 1;
		boolean flag2 = false;
		Horse horse = new Horse();
		int choice;
		
		if (!horses.isEmpty()) {
			for (Horse h : horses) {
				
				String ownerName = "None";
				if (h.getOwner() != null) {
					ownerName = h.getOwner().getName();
				}
				System.out.println("(" + j + ") " + h.getName() + " | Status:" + h.getStatus() + " | Owner: " + ownerName);
				j++;
				flag2 = true;
			}
		}
		
		if (flag2 == false) {
			
			System.out.println("No existing horses found. Redirecting to create new horse.");
			addHorse();
			horse = horses.get(horses.size() - 1);
		} else {
			choice = readIntRange("Enter horse number: ", 1, horses.size());
			horse = horses.get(choice - 1);
		}
		
		Lesson temp = new Lesson(horse, rider, date, time, duration);
		lessons.add(temp);
		lessonsPQ.add(temp);
		
		System.out.println("Lesson has been added.");
	}
	
	/**
	 * Displays lessons by their index in the internal list, rather than
	 * priority order. Used when selecting a lesson for editing or removal.
	 */
	private static void displayLessonsByIndex() {
		
		System.out.println("=== Lessons (by index) ===");
		
		if (lessons.isEmpty()) {
			System.out.println("No lessons.");
			return;
		}
		
		for (int i = 0; i < lessons.size(); i++) {
			
			Lesson l = lessons.get(i);
			
			String riderName = "None";
			if (l.getRider() != null) {
				riderName = l.getRider().getName();
			}
			String horseName = "None";
			if (l.getHorse() != null) {
				horseName = l.getHorse().getName();
			}
			String dateStr = "None";
			if (l.getDate() != null) {
				dateStr = l.getDate().toString();
			}
			String timeStr = "None";
			if (l.getTime() != null) {
				timeStr = l.getTime().toString();
			}
			
			System.out.println("(" + (i+1) + ") Rider: " + riderName + " | Horse: " + horseName + " | Date: " + dateStr
			+ " | Time: " + timeStr);
			
		}
	}
	
	/**
	 * Allows the user to edit an existing lesson selected by index.
	 * 
	 * The user can modify:
	 * 
	 *   Rider
	 *   Horse
	 *   Date
	 *   Time
	 * 
	 * Lesson entries in the priority queue are updated when date or time changes.
	 */
	public static void editLesson() {
		
		if (lessons.isEmpty()) {
			System.out.println("No lessons to edit.");
			return;
		}
		
		displayLessonsByIndex();
		
		int index = readIntRange("Enter lesson number to edit:", 1, lessons.size()) - 1;
		
		Lesson temp = lessons.get(index);
		
		System.out.println("\nEditing lesson: ");
		System.out.println("What would you like to edit?");
		System.out.println("1) Rider");
		System.out.println("2) Horse");
		System.out.println("3) Date");
		System.out.println("4) Time");
		
		int choice = readIntRange("Enter choice:", 1, 4);
		
		switch (choice) {
			
			case 1:
				String riderName = readFullName("Rider name? (First Last):");
				String phoneNumber = readPhoneNumber("Rider phone number? (0123456789):");
				
				Rider rider = null;
				
				for (Rider r : riders) {
					
					if (r.getName().equals(riderName) && r.getPhoneNumber().equals(phoneNumber)) {
						
						rider = r;
						break;
					} 
				}
				
				if (rider == null) {
					
					System.out.println("No rider found. Creating new rider.");
					addRider();
					rider = riders.get(riders.size() - 1);
				}
				
				temp.setRider(rider);
				break;
			case 2:
				System.out.println("=== Horse Options ===");
				int j = 1;
				boolean flag2 = false;
				Horse horse = new Horse();
				int choice2;
				
				if (!horses.isEmpty()) {
					for (Horse h : horses) {
						
						String ownerName = "None";
						if (h.getOwner() != null) {
							ownerName = h.getOwner().getName();
							}
						System.out.println("(" + j + ") " + h.getName() + " | Status:" + h.getStatus() + " | Owner: " + ownerName);
						j++;
						flag2 = true;
					}
				}
				
				if (flag2 == false) {
			
					System.out.println("No existing horses found. Redirecting to create new horse.");
					addHorse();
					horse = horses.get(horses.size() - 1);
				} else {
					choice2 = readIntRange("Enter horse number: ", 1, horses.size());
					horse = horses.get(choice2 - 1);
				}
				temp.setHorse(horse);
				break;
			case 3: 
				lessonsPQ.remove(temp);
				temp.setDate(readDate("New date (YYYY-MM-DD):"));
				lessonsPQ.add(temp);
				break;
			case 4: 
				lessonsPQ.remove(temp);
				temp.setTime(readTime("New time (00:00 24 hour):"));
				lessonsPQ.add(temp);
				break;
				
			default:
				System.out.println("No changes made.");
		}
		
		System.out.println("Lesson updated.");
	}
	
	/**
	 * Removes a lesson selected by index.
	 * 
	 * The lesson is removed from both the lessons list and the lesson queue.
	 */
	public static void removeLesson() {
		
		if (lessons.isEmpty()) {
			System.out.println("No lessons to remove.");
			return;
		}
		
		displayLessonsByIndex();
		
		int index = readIntRange("Enter lesson number to remove:", 1, lessons.size()) - 1;
		
		Lesson removed = lessons.remove(index);
		lessonsPQ.remove(removed);
		
		System.out.println("Lesson has been removed.");
	}
	
	/**
	 * Removes all lessons after a confirmation prompt.
	 * 
	 * This clears:
	 *
	 *   The lessons list
	 *   The lessons priority queue
	 */
	public static void removeAllLessons() {
		
		if (lessons.isEmpty()) { 
			System.out.println("No lessons to be removed."); 
			return;
		}
		
		System.out.println("Are you sure? (YES/NO)");
		String answer = console.nextLine().trim().toLowerCase();
			
		if (!answer.equals("yes")) {
			System.out.println("Cancelled.");
			return;
		}
		
		lessons.clear();
		lessonsPQ.clear();
		
		System.out.println("All lessons have been removed.");
	}
	
	/**
	 * Loads lessons from the specified file.
	 * 
	 * Expected file format (per lesson, one field per line):
	 * 
	 *   Date (string for LocalDate)
	 *   Time (string for LocalTime)
	 *   Duration (int)
	 *   Rider ID (may be blank)
	 *   Horse ID (may be blank)
	 * 
	 * @param file the lessons file path
	 */
	private static void loadLessonsFromFile(String file) {
		
		try (Scanner in = new Scanner(new File(file))) {
			
			while (in.hasNext()) {
				
				LocalDate date = parseDate(in.nextLine());
				LocalTime time = parseTime(in.nextLine());
				int duration = Integer.parseInt(in.nextLine());
				String riderID = in.nextLine();
				Rider rider = getRiderByID(riderID);
				String horseID = in.nextLine();
				Horse horse = getHorseByID(horseID);
				
				Lesson temp = new Lesson();
				temp.setDate(date);
				temp.setTime(time);
				temp.setDuration(duration);
				temp.setRider(rider);
				temp.setHorse(horse);
				
				lessons.add(temp);
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("lessons.txt not found, starting with no lessons.");
			lessons = new ArrayList<>();
		}
	}
	
	/**
	 * Saves all lessons to the specified file.
	 * 
	 * @param file the lessons file path
	 */
	public static void saveLessonsToFile(String file) {
		
		try (PrintStream out = new PrintStream(new File(file))) {
			
			for (int i = 0; i < lessons.size(); i++) {
				
				Lesson temp = lessons.get(i);
				out.println(formatDate(temp.getDate()));
				out.println(formatTime(temp.getTime()));
				out.println(temp.getDuration());
				Rider rider = temp.getRider();
				if (rider == null) {
					out.println("");
				} else {
					out.println(rider.getID());
				}
				Horse horse = temp.getHorse();
				if (horse == null) {
					out.println("");
				} else {
					out.println(horse.getID());
				}
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("Error saving lesson data: " + e.getMessage());
		}
	}
	
	/**
	 * Returns a new list of lessons sorted by date and time,
	 * using the LESSONS_COMPARATOR.
	 * 
	 * @return a sorted copy of the lessons list
	 */
	public static List<Lesson> getSortedLessons() {
		
		List<Lesson> copy = new ArrayList<>(lessons);
		copy.sort(LESSONS_COMPARATOR);
		return copy;
	}
}
