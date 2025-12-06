/**
 * Represents a rider in the StableMate system. Riders can be owners, boarders, or
 * students. Each rider has identifying information such as
 * name, contact info, type, age, and weight. A unique ID is assigned at creation.
 */
public class Rider {
	
	//Owner, student, boarder
	private String id;
	private String name, phoneNumber, email, type;
	private int age;
	private double weight;
	
	/**
     * Creates a Rider with all fields initialized to default values.
     * The ID is left null and must be set later when loaded from file.
     */
	public Rider() {
		
		this.id = null;
		this.name = null;
		this.phoneNumber = null;
		this.email = null;
		this.type = null;
		this.age = 0;
		this.weight = 0;
		
	}
	
	/**
     * Creates a new Rider with the given attributes.
     * A unique ID is automatically generated and registered.
     * 
     * @param name         The rider's full name.
     * @param phoneNumber  The rider's 10-digit phone number.
     * @param email        The rider's email address.
     * @param type         The rider category (owner, boarder, student, etc.).
     * @param weight       The rider's weight in pounds.
     * @param age          The rider's age in years.
     */
	public Rider(String name, String phoneNumber, String email, String type, double weight, int age) {
		
		this.id = IDGenerator.riderID();
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.type = type;
		this.age = age;
		this.weight = weight;
		
		IDGenerator.registerExisting(this.id);
	}
	
	// =======================
    //        GETTERS
    // =======================

    /**
     * Returns the rider's full name.
     * 
     * @return The rider's name.
     */
	public String getName(){ return this.name; }
	
	/**
     * Returns the rider's phone number.
     * 
     * @return The 10-digit phone number.
     */
	public String getPhoneNumber(){ return this.phoneNumber; }
	
	/**
     * Returns the rider's email.
     * 
     * @return The email address.
     */
	public String getEmail(){ return this.email; }
	
	/**
     * Returns the rider type (owner, boarder, student, etc.).
     * 
     * @return The rider type.
     */
	public String getType(){ return this.type; }
	
	/**
     * Returns the rider's age.
     * 
     * @return The age in years.
     */
	public int getAge(){ return this.age; }
	
	/**
     * Returns the rider's weight.
     * 
     * @return The weight in pounds.
     */
	public double getWeight(){ return this.weight; }
	
	/**
     * Returns the rider's unique ID.
     * 
     * @return The generated rider ID.
     */
	public String getID() { return id; }
	
	// =======================
    //        SETTERS
    // =======================

    /**
     * Sets the rider's name.
     * 
     * @param name New full name.
     */
	public void setName(String name){ this.name = name; }
	
	/**
     * Sets the rider's phone number.
     * 
     * @param phoneNumber A valid 10-digit number.
     */
	public void setPhoneNumber(String phoneNumber){ this.phoneNumber = phoneNumber; }
	
	/**
     * Sets the rider's email address.
     * 
     * @param email New email string.
     */
	public void setEmail(String email){ this.email = email; }
	
	/**
     * Sets the rider type (owner, boarder, student, etc.).
     * 
     * @param type New rider type.
     */
	public void setType(String type){ this.type = type; }
	
	/**
     * Sets the rider's age.
     * 
     * @param age Age in years.
     */
	public void setAge(int age){ this.age = age; }
	
	/**
     * Sets the rider's weight.
     * 
     * @param weight Weight in pounds.
     */
	public void setWeight(double weight){ this.weight = weight; }
	
	/**
     * Sets the rider's ID, but only if no ID has been assigned yet.
     * Used when loading existing riders from file.
     * 
     * @param id The ID value to assign.
     * @throws IllegalStateException if called after the ID was already set.
     */
	public void setID(String id) { 
		
		if (this.id != null) {
			
			throw new IllegalStateException("ID cannot be changed once set.");
		}
		
		this.id = id; 
		IDGenerator.registerExisting(id);
	}
	
	/**
     * Returns a formatted String representation of the Rider, including
     * name, contact info, type, and age.
     * 
     * @return Human-readable rider information.
     */
	public String toString() {
		
		return "Name: " + getName() + " Phone #: " + getPhoneNumber() + " Email: " + getEmail() + " Type: " + getType() + " Age: " + getAge() + "yo"; 
	}
}
