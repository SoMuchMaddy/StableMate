import java.util.Random;
import java.util.Set;
import java.util.HashSet;

/**
 * Utility class responsible for generating unique Rider and Horse IDs.
 * 
 * IDs follow the format:
 *   - Riders:  "R-XXXXXX"
 *   - Horses:  "H-XXXXXX"
 * 
 * Each ID contains six randomly generated characters (letters or digits).
 * 
 * The generator tracks all used IDs to ensure uniqueness within the runtime
 * of the program. Existing IDs loaded from file must be registered using
 * registerExisting(String) so they are not duplicated.
 */
public class IDGenerator {
    
    private static final Random rand = new Random();
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";

    /** Set of all assigned IDs to prevent duplicates. */
    private static final Set<String> usedIDs = new HashSet<>();

    /**
     * Generates a unique ID for a Horse.
     * 
     * @return A unique ID string beginning with "H-".
     */
    public static String horseID() {
        return generate("H-");
    }

    /**
     * Generates a unique ID for a Rider.
     * 
     * @return A unique ID string beginning with "R-".
     */
    public static String riderID() {
        return generate("R-");
    }

    /**
     * Internal method for generating IDs with a prefix.
     * Ensures uniqueness by checking against the used ID set.
     * 
     * @param prefix The ID prefix (e.g., "H-" or "R-").
     * @return A newly generated, guaranteed-unique ID.
     */
    private static String generate(String prefix) {
        String code;

        do {
            code = prefix + randomCode();
        } while (usedIDs.contains(code));   // Ensure uniqueness

        usedIDs.add(code);
        return code;
    }

    /**
     * Produces a six-character alphanumeric string used in ID generation.
     * Characters may be uppercase letters or digits.
     * 
     * @return A randomized 6-character code.
     */
    private static String randomCode() {
        char[] code = new char[6];

        for (int i = 0; i < code.length; i++) {
            if (rand.nextBoolean()) {
                code[i] = LETTERS.charAt(rand.nextInt(LETTERS.length()));
            } else {
                code[i] = NUMBERS.charAt(rand.nextInt(NUMBERS.length()));
            }
        }

        return new String(code);
    }

    /**
     * Registers an ID that already exists in data files, preventing the
     * generator from reusing it.
     * 
     * This should be called when loading Riders or Horses from disk.
     * 
     * @param id The ID to mark as already used.
     */
    public static void registerExisting(String id) {
        usedIDs.add(id);
    }
}

