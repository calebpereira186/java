import java.util.*;

/** Console app: animals in a 30 km^2 park, with ASCII map and birth-to-death life history. */
public class WildlifeTracker {
    static final double SIDE = Math.sqrt(30);   // 30 km^2 square park
    static final int DAYS = 60;
    static final String[] TYPES = {"Lion", "Tiger", "Deer", "Bear", "Leopard"};
    static final char[] SYMBOLS = {'L', 'T', 'D', 'B', 'P'};
    static final String[] ACTIVITIES = {"Eating", "Resting", "Hunting/Grazing", "Drinking water", "Roaming"};
    static final String[] CAUSES = {"Old age", "Disease", "Injury", "Hunted"};

    static class Animal {
        int id, type, birth, death = -1;
        String cause = "Still alive";
        double[][] pos = new double[DAYS][];     // position per day, null if not alive
        List<String> log = new ArrayList<>();
        String name() { return TYPES[type] + " #" + id; }
    }

    static List<Animal> animals = new ArrayList<>();

    static void simulate(int n) {
        Random r = new Random();
        for (int i = 1; i <= n; i++) {
            Animal a = new Animal();
            a.id = i; a.type = r.nextInt(TYPES.length); a.birth = r.nextInt(DAYS - 10);
            int end = a.birth + 10 + r.nextInt(11);              // lives 10-20 days
            double x = r.nextDouble() * SIDE, y = r.nextDouble() * SIDE;
            a.log.add("Day " + a.birth + ": BORN at " + at(x, y));
            for (int d = a.birth; d <= Math.min(end, DAYS - 1); d++) {
                x = clamp(x + r.nextGaussian() * 0.4);
                y = clamp(y + r.nextGaussian() * 0.4);
                a.pos[d] = new double[]{x, y};
                if (d == end) {
                    a.death = d; a.cause = CAUSES[r.nextInt(CAUSES.length)];
                    a.log.add("Day " + d + ": DIED (" + a.cause + ") at " + at(x, y));
                } else if (d > a.birth) {
                    a.log.add("Day " + d + ": " + ACTIVITIES[r.nextInt(ACTIVITIES.length)] + " at " + at(x, y));
                }
            }
            animals.add(a);
        }
    }

    static double clamp(double v) { return Math.max(0, Math.min(SIDE, v)); }
    static String at(double x, double y) { return String.format("(%.2f, %.2f) km", x, y); }

    static void summary(int day) {
        int[] count = new int[TYPES.length];
        for (Animal a : animals) if (a.pos[day] != null) count[a.type]++;
        System.out.printf("%nPark area: 30 km² (%.2f km x %.2f km)%nDay %d population:%n", SIDE, SIDE, day);
        for (int i = 0; i < TYPES.length; i++) System.out.printf("  %-8s %d%n", TYPES[i], count[i]);
    }

    static void asciiMap(int day) {
        int cols = 40, rows = 20;
        char[][] grid = new char[rows][cols];
        for (char[] row : grid) Arrays.fill(row, '.');
        for (Animal a : animals) {
            if (a.pos[day] == null) continue;
            int c = Math.min(cols - 1, (int) (a.pos[day][0] / SIDE * cols));
            int r = Math.min(rows - 1, (int) (a.pos[day][1] / SIDE * rows));
            grid[r][c] = SYMBOLS[a.type];
        }
        System.out.println("\nMap on day " + day + "  (L=Lion T=Tiger D=Deer B=Bear P=Leopard)");
        System.out.println("+" + "-".repeat(cols) + "+");
        for (char[] row : grid) System.out.println("|" + new String(row) + "|");
        System.out.println("+" + "-".repeat(cols) + "+");
    }

    static void listAll() {
        System.out.printf("%n%-4s %-9s %-6s %-6s %s%n", "ID", "Type", "Born", "Died", "Cause");
        for (Animal a : animals)
            System.out.printf("%-4d %-9s %-6d %-6s %s%n", a.id, TYPES[a.type], a.birth,
                    a.death < 0 ? "-" : String.valueOf(a.death), a.cause);
    }

    static void history(int id) {
        for (Animal a : animals) {
            if (a.id != id) continue;
            System.out.println("\n=== Life cycle of " + a.name() + " ===");
            a.log.forEach(System.out::println);
            return;
        }
        System.out.println("No animal with ID " + id);
    }

    static int readInt(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(sc.nextLine().trim());
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println("Enter a number between " + min + " and " + max + ".");
        }
    }

    public static void main(String[] args) {
        simulate(25);
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- WILDLIFE TRACKER ---");
            System.out.println("1. Population summary (by day)");
            System.out.println("2. Show map (by day)");
            System.out.println("3. List all animals");
            System.out.println("4. Animal life history (birth to death)");
            System.out.println("5. Google Maps link of the park");
            System.out.println("0. Exit");
            int choice = readInt(sc, "Choice: ", 0, 5);
            switch (choice) {
                case 1 -> summary(readInt(sc, "Day (0-" + (DAYS - 1) + "): ", 0, DAYS - 1));
                case 2 -> asciiMap(readInt(sc, "Day (0-" + (DAYS - 1) + "): ", 0, DAYS - 1));
                case 3 -> listAll();
                case 4 -> history(readInt(sc, "Animal ID (1-" + animals.size() + "): ", 1, animals.size()));
                case 5 -> System.out.println("https://www.google.com/maps/@19.2147,72.9106,12z");
                case 0 -> { System.out.println("Bye!"); return; }
            }
        }
    }
}
