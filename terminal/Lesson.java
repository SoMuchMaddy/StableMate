
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a scheduled riding lesson in the StableMate system.
 * 
 * A Lesson contains a rider, a horse, a date and time, and the duration of
 * the session in minutes. Lessons do not store unique IDs since their
 * identity comes from their time slot and linked participants.
 */
public class Lesson {
    
    private LocalDate date;
    private LocalTime time;
    private Horse horse;
    private Rider rider;
    private int duration; // in minutes

    /**
     * Creates an empty Lesson with all fields initialized to null or zero.
     */
    public Lesson() {
        this.horse = null;
        this.rider = null;
        this.date = null;
        this.time = null;
        this.duration = 0;
    }

    /**
     * Creates a Lesson with the given horse, rider, date, time, and duration.
     * 
     * @param horse     The horse being used for the lesson.
     * @param rider     The rider participating in the lesson.
     * @param date      The scheduled date of the lesson.
     * @param time      The scheduled start time of the lesson.
     * @param duration  Duration of the lesson in minutes.
     */
    public Lesson(Horse horse, Rider rider, LocalDate date, LocalTime time, int duration) {
        this.horse = horse;
        this.rider = rider;
        this.date = date;
        this.time = time;
        this.duration = duration;
    }

    // =======================
    //        GETTERS
    // =======================

    /**
     * Returns the horse used for the lesson.
     * 
     * @return The horse.
     */
    public Horse getHorse(){ return this.horse; }

    /**
     * Returns the rider participating in the lesson.
     * 
     * @return The rider.
     */
    public Rider getRider(){ return this.rider; }

    /**
     * Returns the date of the lesson.
     * 
     * @return The lesson date.
     */
    public LocalDate getDate(){ return this.date; }

    /**
     * Returns the time of the lesson.
     * 
     * @return The lesson start time.
     */
    public LocalTime getTime(){ return this.time; }

    /**
     * Returns the duration of the lesson in minutes.
     * 
     * @return Duration in minutes.
     */
    public int getDuration(){ return this.duration; }

    // =======================
    //        SETTERS
    // =======================

    /**
     * Sets the horse for the lesson.
     * 
     * @param horse The new horse.
     */
    public void setHorse(Horse horse){ this.horse = horse; }

    /**
     * Sets the rider for the lesson.
     * 
     * @param rider The new rider.
     */
    public void setRider(Rider rider){ this.rider = rider; }

    /**
     * Sets the lesson date.
     * 
     * @param date The new date.
     */
    public void setDate(LocalDate date){ this.date = date; }

    /**
     * Sets the lesson time.
     * 
     * @param time The new time.
     */
    public void setTime(LocalTime time){ this.time = time; }

    /**
     * Sets the duration of the lesson in minutes.
     * 
     * @param duration New duration value.
     */
    public void setDuration(int duration){ this.duration = duration; }

    /**
     * Returns a formatted, human-readable representation of the Lesson.
     * 
     * @return String summarizing the lesson details.
     */
    public String toString() {
        return "Lesson on " + getDate() + " at " + getTime() + " for " + 
               getRider() + " on " + getHorse() + " for " + getDuration() + " minutes.";
    }
}

