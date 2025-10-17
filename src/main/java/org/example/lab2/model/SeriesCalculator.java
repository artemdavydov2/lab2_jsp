package org.example.lab2.model;

// Обчислення ряду arctan(x) за допомогою його розкладу в ряд Тейлора
public class SeriesCalculator {
    private final int maxIterations;

    public SeriesCalculator() { this(2_000_000); }
    public SeriesCalculator(int maxIterations) { this.maxIterations = maxIterations; }

    // k-й доданок ряду arctan(x): (-1)^k * x^(2k+1) / (2k+1)
    private double term(int k, double x) {
        double pow = Math.pow(x, 2 * k + 1);
        double sign = (k % 2 == 0) ? 1.0 : -1.0;
        return sign * pow / (2 * k + 1);
    }

    // Сума перших n доданків ряду arctan(x)
    public double sumFirstNTerms(double x, int n) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        double sum = 0.0;
        for (int k = 0; k < n; k++) sum += term(k, x);
        return sum;
    }

    // Сума доданків з |term| > e і кількість таких доданків (обрив при першому |term| <= e)
    public SumWithCount sumTermsGreaterThanE(double x, double e) {
        if (e <= 0) throw new IllegalArgumentException("e must be positive");
        double sum = 0.0; int count = 0;
        for (int k = 0; k < maxIterations; k++) {
            double t = term(k, x);
            if (Math.abs(t) > e) { sum += t; count++; } else break;
        }
        return new SumWithCount(sum, count);
    }

    // Точне значення для порівняння (використовуємо вбудовану функцію)
    public double exactAtan(double x) { return Math.atan(x); }

    // DTO: сума і кількість — тепер із геттерами для JSF/EL
    public static class SumWithCount {
        private final double sum;
        private final int count;

        public SumWithCount(double sum, int count) {
            this.sum = sum;
            this.count = count;
        }

        public double getSum() { return sum; }
        public int getCount() { return count; }
    }
}