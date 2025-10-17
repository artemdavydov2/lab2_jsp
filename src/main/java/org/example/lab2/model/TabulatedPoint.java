package org.example.lab2.model;

// Рядок таблиці табулювання: x, y (може бути null), валідним або містити повідомлення про помилку ОДР
public class TabulatedPoint {
    private final double x;
    private final Double y;
    private final boolean valid;
    private final String message;

    public TabulatedPoint(double x, Double y, boolean valid, String message) {
        this.x = x; this.y = y; this.valid = valid; this.message = message;
    }

    public double getX() { return x; }
    public Double getY() { return y; }
    public boolean isValid() { return valid; }
    public String getMessage() { return message; }
}