import java.nio.file.*;
import java.util.*;

public class PolynomialSolver {

    // Convert a string in given base into decimal
    static long decode(String value, int base) {
        return Long.parseLong(value, base);
    }

    // Lagrange interpolation at x = 0 to get constant term
    static double lagrangeConstant(List<long[]> points) {
        int n = points.size();
        double c = 0.0;

        for (int i = 0; i < n; i++) {
            double xi = points.get(i)[0];
            double yi = points.get(i)[1];
            double term = yi;

            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                double xj = points.get(j)[0];
                term *= (0 - xj) / (xi - xj); // evaluating at x=0
            }
            c += term;
        }
        return c;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java PolynomialSolver <input.json>");
            return;
        }

        // read file as text
        String jsonText = new String(Files.readAllBytes(Paths.get(args[0])));
        jsonText = jsonText.replaceAll("[\\{\\}\\\"]", ""); // strip { } and quotes

        // split into lines
        String[] lines = jsonText.split(",|\\r?\\n");

        int k = 0;
        List<long[]> points = new ArrayList<>();
        Map<String, String> temp = new HashMap<>();
        String currentKey = null;

        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("keys:")) continue;
            if (line.startsWith("k:")) {
                k = Integer.parseInt(line.split(":")[1].trim());
            } else if (line.matches("\\d+:")) {
                currentKey = line.replace(":", "").trim();
            } else if (line.startsWith("base:")) {
                temp.put("base", line.split(":")[1].trim());
            } else if (line.startsWith("value:")) {
                temp.put("value", line.split(":")[1].trim());
                if (currentKey != null) {
                    int base = Integer.parseInt(temp.get("base"));
                    String value = temp.get("value");
                    long y = decode(value, base);
                    long x = Long.parseLong(currentKey);
                    points.add(new long[]{x, y});
                    temp.clear();
                    currentKey = null;
                }
            }
        }

        // take first k points
        List<long[]> subset = points.subList(0, k);

        double c = lagrangeConstant(subset);
        System.out.println(Math.round(c));
    }
}
