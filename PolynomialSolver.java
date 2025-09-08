import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.file.*;
import java.util.*;

public class PolynomialSolver {

    // Convert a string in given base into decimal (BigInteger)
    static BigInteger decode(String value, int base) {
        return new BigInteger(value, base);
    }

    // Lagrange interpolation at x = 0 to get constant term using BigDecimal
    static BigDecimal lagrangeConstant(List<long[]> xs, List<BigInteger> ys) {
        int n = xs.size();
        MathContext mc = new MathContext(50); // high precision
        BigDecimal c = BigDecimal.ZERO;

        for (int i = 0; i < n; i++) {
            BigDecimal xi = BigDecimal.valueOf(xs.get(i)[0]);
            BigDecimal yi = new BigDecimal(ys.get(i));
            BigDecimal term = yi;

            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                BigDecimal xj = BigDecimal.valueOf(xs.get(j)[0]);
                // term *= (0 - xj) / (xi - xj)
                term = term.multiply(
                        (BigDecimal.ZERO.subtract(xj))
                                .divide(xi.subtract(xj), mc),
                        mc
                );
            }
            c = c.add(term, mc);
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
        List<long[]> xPoints = new ArrayList<>();
        List<BigInteger> yPoints = new ArrayList<>();
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
                    BigInteger y = decode(value, base);
                    long x = Long.parseLong(currentKey);
                    xPoints.add(new long[]{x});
                    yPoints.add(y);
                    temp.clear();
                    currentKey = null;
                }
            }
        }

        // take first k points
        List<long[]> subsetX = xPoints.subList(0, k);
        List<BigInteger> subsetY = yPoints.subList(0, k);

        BigDecimal c = lagrangeConstant(subsetX, subsetY);
        System.out.println("Constant term (c) ≈ " + c.toPlainString());
    }
}
