import java.util.ArrayList;
import java.time.LocalDate;

/**
 * Represents a horse in the StableMate system.
 * 
 * A Horse tracks basic identity (name, breed, color, etc.), ownership,
 * compatibility with other horses, body measurements, and care schedule
 * information (annual vet, deworming, farrier).
 */
public class Horse {
    
    private String id;
    private String name, breed, color, sex, food, status;
    private Rider owner;
    private double hands, weight;
    private ArrayList<Horse> comp;
    private ArrayList<String> compIDs;
    private LocalDate lastAnnual;
    private LocalDate lastDeworm;
    private LocalDate lastFarrier;
    private int annualFreq, dewormFreq, farrierFreq;
    
    /**
     * Creates a Horse with default values.
     * 
     * ID and all Strings are initialized to null, owner and compatibility lists
     * are null, and care frequencies are set to reasonable defaults
     * (annual = 365 days, deworm = 90 days, farrier = 84 days).
     */
	public Horse() {
        this.id = null;
        this.name = null;
        this.breed = null;
        this.color = null;
        this.sex = null;
        this.food = null;
        this.status = null;
        this.owner = null;
        this.comp = null; // Compatibility list
        this.hands = 0.0;
        this.weight = 0.0;
        this.lastAnnual = null;
        this.lastDeworm = null;
        this.lastFarrier = null;
        this.annualFreq = 365; // default
        this.dewormFreq = 90;  // default
        this.farrierFreq = 84; // default
    }
    
    /**
     * Creates a Horse with all fields specified and auto-generates a unique ID.
     * The ID is also registered with the IDGenerator.
     * 
     * @param name         The horse's name.
     * @param breed        The horse's breed.
     * @param color        The horse's color.
     * @param sex          The horse's sex (e.g., Gelding, Mare, Stallion).
     * @param food         The type of feed this horse is given.
     * @param status       The boarding/usage status (lesson, boarded, resident).
     * @param owner        The Rider who owns the horse (may be null).
     * @param comp         List of compatible horses (for turnout, etc.).
     * @param hands        The horse's height in hands.
     * @param weight       The horse's weight in pounds.
     * @param lastAnnual   Date of last annual vet visit (may be null).
     * @param lastDeworm   Date of last deworming (may be null).
     * @param lastFarrier  Date of last farrier visit (may be null).
     * @param annualFreq   Days between annual vet visits.
     * @param dewormFreq   Days between deworming.
     * @param farrierFreq  Days between farrier visits.
     */
    public Horse(String name, String breed, String color, String sex, String food, String status, Rider owner,
                 ArrayList<Horse> comp, double hands, double weight, LocalDate lastAnnual, LocalDate lastDeworm,
                 LocalDate lastFarrier, int annualFreq, int dewormFreq, int farrierFreq) {
        
        this.id = IDGenerator.horseID();
        this.name = name;
        this.breed = breed;
        this.color = color;
        this.sex = sex;
        this.food = food;
        this.status = status;
        this.owner = owner;
        this.comp = comp;
        this.hands = hands;
        this.weight = weight;
        this.lastAnnual = lastAnnual;
        this.lastDeworm = lastDeworm;
        this.lastFarrier = lastFarrier;
        this.annualFreq = annualFreq;
        this.dewormFreq = dewormFreq;
        this.farrierFreq = farrierFreq;
        
        IDGenerator.registerExisting(this.id);
    }
    
    // =======================
    //        GETTERS
    // =======================

    /**
     * Returns the horse's name.
     * 
     * @return The name of the horse.
     */
    public String getName() { return this.name; }
    
    /**
     * Returns the horse's breed.
     * 
     * @return The breed.
     */
    public String getBreed() { return this.breed; }
    
    /**
     * Returns the horse's color.
     * 
     * @return The color.
     */
    public String getColor() { return this.color; }
    
    /**
     * Returns the horse's sex (e.g., Gelding, Mare).
     * 
     * @return The sex value.
     */
    public String getSex() { return this.sex; }
    
    /**
     * Returns the horse's type of feed.
     * 
     * @return The food description.
     */
    public String getFood() { return this.food; }
    
    /**
     * Returns the current status of the horse (lesson, boarded, resident).
     * 
     * @return The status string.
     */
    public String getStatus() { return this.status; }
    
    /**
     * Returns the owner of this horse.
     * 
     * @return The Rider who owns the horse, or null if none.
     */
    public Rider getOwner() { return this.owner; }
    
    /**
     * Returns the list of compatible horses (turnout buddies).
     * 
     * @return The compatibility list (may be null).
     */
    public ArrayList<Horse> getComp() { return this.comp; }
    
    /**
     * Returns the horse's height in hands.
     * 
     * @return Height in hands.
     */
    public double getHands() { return this.hands; }
    
    /**
     * Returns the horse's weight in pounds.
     * 
     * @return Weight in pounds.
     */
    public double getWeight() { return this.weight; }
    
    /**
     * Returns the unique ID of this horse.
     * 
     * @return Horse ID string.
     */
    public String getID() { return id; }
    
    /**
     * Returns the date of the last annual vet visit.
     * 
     * @return Last annual date, or null if not recorded.
     */
    public LocalDate getLastAnnual() { return this.lastAnnual; }
    
    /**
     * Returns the date of the last deworming.
     * 
     * @return Last deworming date, or null if not recorded.
     */
    public LocalDate getLastDeworm() { return this.lastDeworm; }
    
    /**
     * Returns the date of the last farrier visit.
     * 
     * @return Last farrier date, or null if not recorded.
     */
    public LocalDate getLastFarrier() { return this.lastFarrier; }
    
    /**
     * Returns the number of days between annual vet checks.
     * 
     * @return Annual frequency in days.
     */
    public int getAnnualFreq() { return this.annualFreq; }
    
    /**
     * Returns the number of days between deworming treatments.
     * 
     * @return Deworm frequency in days.
     */
    public int getDewormFreq() { return this.dewormFreq; }
    
    /**
     * Returns the number of days between farrier visits.
     * 
     * @return Farrier frequency in days.
     */
    public int getFarrierFreq() { return this.farrierFreq; }
    
    // =========================
    //   CARE DATE CALCULATORS
    // =========================

    /**
     * Calculates the next annual vet due date based on lastAnnual
     * and annualFreq.
     * 
     * @return The next annual due date, or null if lastAnnual is null.
     */
    public LocalDate getNextAnnualDue() {
        if (lastAnnual == null) return null;
        return lastAnnual.plusDays(annualFreq);
    }
    
    /**
     * Calculates the next deworming due date based on lastDeworm
     * and dewormFreq.
     * 
     * @return The next deworm due date, or null if lastDeworm is null.
     */
    public LocalDate getNextDewormDue() {
        if (lastDeworm == null) return null;
        return lastDeworm.plusDays(dewormFreq);
    }
    
    /**
     * Calculates the next farrier due date based on lastFarrier
     * and farrierFreq.
     * 
     * @return The next farrier due date, or null if lastFarrier is null.
     */
    public LocalDate getNextFarrierDue() {
        if (lastFarrier == null) return null;
        return lastFarrier.plusDays(farrierFreq);
    }
    
    // =========================
    //   OVERDUE HELPERS
    // =========================

    /**
     * Determines if the annual vet visit is overdue relative to a given date.
     * 
     * @param today The date to compare against (typically LocalDate.now()).
     * @return true if the next annual date is on or before today.
     */
    public boolean isAnnualOverdue(LocalDate today) {
        LocalDate next = getNextAnnualDue();
        return (next != null) && (!next.isAfter(today));
    }
    
    /**
     * Determines if the deworming is overdue relative to a given date.
     * 
     * @param today The date to compare against.
     * @return true if the next deworm date is on or before today.
     */
    public boolean isDewormOverdue(LocalDate today) {
        LocalDate next = getNextDewormDue();
        return (next != null) && (!next.isAfter(today));
    }
    
    /**
     * Determines if the farrier visit is overdue relative to a given date.
     * 
     * @param today The date to compare against.
     * @return true if the next farrier date is on or before today.
     */
    public boolean isFarrierOverdue(LocalDate today) {
        LocalDate next = getNextFarrierDue();
        return (next != null) && (!next.isAfter(today));
    }
    
    // =======================
    //        SETTERS
    // =======================

    /**
     * Sets the horse's name.
     * 
     * @param name New name.
     */
    public void setName(String name){ this.name = name; }
    
    /**
     * Sets the horse's breed.
     * 
     * @param breed New breed.
     */
    public void setBreed(String breed){ this.breed = breed; }
    
    /**
     * Sets the horse's color.
     * 
     * @param color New color.
     */
    public void setColor(String color){ this.color = color; }
    
    /**
     * Sets the horse's sex.
     * 
     * @param sex New sex value.
     */
    public void setSex(String sex){ this.sex = sex; }
    
    /**
     * Sets the horse's food description.
     * 
     * @param food New food value.
     */
    public void setFood(String food){ this.food = food; }
    
    /**
     * Sets the horse's usage/boarding status.
     * 
     * @param status New status (lesson, boarded, resident).
     */
    public void setStatus(String status){ this.status = status; }
    
    /**
     * Sets the owner of this horse.
     * 
     * @param owner Rider to assign as owner.
     */
    public void setOwner(Rider owner){ this.owner = owner; }
    
    /**
     * Sets the compatibility list of other horses.
     * 
     * @param comp List of compatible horses.
     */
    public void setComp(ArrayList<Horse> comp){ this.comp = comp; }
    
    /**
     * Sets the horse's height in hands.
     * 
     * @param hands New height (hands).
     */
    public void setHands(double hands){ this.hands = hands; }
    
    /**
     * Sets the horse's weight in pounds.
     * 
     * @param weight New weight value.
     */
    public void setWeight(double weight){ this.weight = weight; }
    
    /**
     * Sets the date of the last annual vet visit.
     * 
     * @param lastAnnual Date of last annual visit (may be null).
     */
    public void setLastAnnual(LocalDate lastAnnual) { this.lastAnnual = lastAnnual; } 
    
    /**
     * Sets the date of the last farrier visit.
     * 
     * @param lastFarrier Date of last farrier visit (may be null).
     */
    public void setLastFarrier(LocalDate lastFarrier) { this.lastFarrier = lastFarrier; } 
    
    /**
     * Sets the date of the last deworming.
     * 
     * @param lastDeworm Date of last deworming (may be null).
     */
    public void setLastDeworm(LocalDate lastDeworm) { this.lastDeworm = lastDeworm; } 
    
    /**
     * Sets the number of days between annual vet visits.
     * 
     * @param annualFreq New annual frequency in days.
     */
    public void setAnnualFreq(int annualFreq){ this.annualFreq = annualFreq; }
    
    /**
     * Sets the number of days between deworming treatments.
     * 
     * @param dewormFreq New deworm frequency in days.
     */
    public void setDewormFreq(int dewormFreq){ this.dewormFreq = dewormFreq; }
    
    /**
     * Sets the number of days between farrier visits.
     * 
     * @param farrierFreq New farrier frequency in days.
     */
    public void setFarrierFreq(int farrierFreq){ this.farrierFreq = farrierFreq; }
    
    /**
     * Sets the horse's ID exactly once.
     * 
     * If an ID is already present, this will throw an IllegalStateException,
     * preventing IDs from being changed after assignment. The ID is also
     * registered with the IDGenerator.
     * 
     * @param id Unique horse ID to assign.
     * @throws IllegalStateException if the ID is already set.
     */
    public void setID(String id) { 
        if (this.id != null) {
            throw new IllegalStateException("ID cannot be changed once set.");
        }
        this.id = id; 
        IDGenerator.registerExisting(id);
    }
    
    /**
     * Returns a String representation of the Horse, including key fields
     * and care information.
     * 
     * @return Human-readable horse information.
     */
    public String toString() {
        return "Name: " + getName() +
               " Breed: " + getBreed() +
               " Color: " + getColor() +
               " Sex: " + getSex() +
               " Hands: " + getHands() +
               " Weight: " + getWeight() +
               " Food: " + getFood() +
               " Status: " + getStatus() +
               " Owner: " + getOwner() +
               " Compatible Horses: " + (getComp() == null ? "[]" : getComp().toString()) +
               " Last annual checkup date: " + getLastAnnual() +
               " Last deworm date: " + getLastDeworm() +
               " Last farrier date: " + getLastFarrier();
    }
    
    // =======================
    //   COMPATIBILITY IDS
    // =======================

    /**
     * Returns the list of compatibility IDs for this horse.
     * These IDs correspond to other Horse objects and are used
     * when re-linking compatibility after loading from file.
     * 
     * @return List of compatible horse IDs (may be null).
     */
    public ArrayList<String> getCompIDs() {
        return compIDs;
    }
    
    /**
     * Sets the list of compatibility IDs for this horse.
     * 
     * @param compIDs List of horse IDs that represent compatible horses.
     */
    public void setCompIDs(ArrayList<String> compIDs) {
        this.compIDs = compIDs;
    }
    
    /**
     * Returns the list of compatible Horse objects.
     * This is a duplicate-style getter of getComp() kept for
     * convenience in some parts of the code.
     * 
     * @return List of compatible Horse objects (may be null).
     */
    public ArrayList<Horse> getComps() {
        return comp;
    }
    
    /**
     * Sets the list of compatible Horse objects.
     * This is a duplicate-style setter of setComp(ArrayList) kept for
     * convenience where this naming is used.
     * 
     * @param comp List of compatible horses.
     */
    public void setComps(ArrayList<Horse> comp) {
        this.comp = comp;
    }
}

