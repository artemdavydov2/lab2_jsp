package org.example.lab2.controller;

import org.example.lab2.model.SeriesCalculator;
import org.example.lab2.model.TabulatedPoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Цей контролер обробляє запити для обчислення ряду arctan(x) та табулювання ln(x-1)/(4-x)
@WebServlet(name = "CalcController", urlPatterns = {"/calc"})
public class CalcController extends HttpServlet {

    // GET: просто перенаправлення на index.xhtml
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/index.xhtml").forward(req, resp);
    }

    // POST: обробка обох завдань в залежності від параметра "action"
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("series".equals(action)) {
            handleSeries(req, resp);
        } else if ("tabulate".equals(action)) {
            handleTabulation(req, resp);
        } else {
            req.setAttribute("error", "Unknown action");
            req.getRequestDispatcher("/index.xhtml").forward(req, resp);
        }
    }

    // Обробка завдання 2.1 (ряд arctan(x))
    private void handleSeries(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Double x = tryParseDouble(req.getParameter("x"));
        Integer n = tryParseInt(req.getParameter("n"));
        Double e1 = tryParseDouble(req.getParameter("e1"));
        Double e2 = tryParseDouble(req.getParameter("e2"));

        req.setAttribute("x", x);
        req.setAttribute("n", n);
        req.setAttribute("e1", e1);
        req.setAttribute("e2", e2);

        // Якщо вхідні дані некоректні, то повертаємо повідомлення про помилку
        if (x == null || n == null || e1 == null || e2 == null) {
            req.setAttribute("seriesError", "Будь ласка, заповніть коректно поля x, n, e1, e2.");
            req.getRequestDispatcher("/index.xhtml").forward(req, resp);
            return;
        }
        // Якщо значення |x| >= 1, то повертаємо поввідомлення про помилку
        if (Math.abs(x) >= 1.0) {
            req.setAttribute("seriesError", "Для ряду arctan(x) необхідно |x| < 1 (R = 1).");
            req.getRequestDispatcher("/index.xhtml").forward(req, resp);
            return;
        }
        // Якщо n < 0, то повертаємо повідомлення про помилку
        if (n < 0) {
            req.setAttribute("seriesError", "n має бути невід’ємним.");
            req.getRequestDispatcher("/index.xhtml").forward(req, resp);
            return;
        }
        // Якщо e1 <= 0 або e2 <= 0, то повертаємо повідомлення про помилку
        if (e1 <= 0 || e2 <= 0) {
            req.setAttribute("seriesError", "e1 та e2 мають бути додатними.");
            req.getRequestDispatcher("/index.xhtml").forward(req, resp);
            return;
        }

        SeriesCalculator calc = new SeriesCalculator();
        double sumN = calc.sumFirstNTerms(x, n);
        SeriesCalculator.SumWithCount r1 = calc.sumTermsGreaterThanE(x, e1);
        SeriesCalculator.SumWithCount r2 = calc.sumTermsGreaterThanE(x, e2);
        double exact = calc.exactAtan(x);
        double diffN = Math.abs(sumN - exact);

        req.setAttribute("seriesSumN", sumN);
        req.setAttribute("seriesResultE1", r1);
        req.setAttribute("seriesResultE2", r2);
        req.setAttribute("seriesExact", exact);
        req.setAttribute("seriesDiffN", diffN);

        req.getRequestDispatcher("/index.xhtml").forward(req, resp);
    }

    // Обробка завдання 2.2 (табулювання ln(x-1)/(4-x))
    private void handleTabulation(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Double a = tryParseDouble(req.getParameter("a"));
        Double b = tryParseDouble(req.getParameter("b"));
        Double h = tryParseDouble(req.getParameter("h"));

        req.setAttribute("a", a);
        req.setAttribute("b", b);
        req.setAttribute("h", h);

        if (a == null || b == null || h == null) {
            req.setAttribute("tabError", "Будь ласка, заповніть коректно поля a, b, h.");
            req.getRequestDispatcher("/index.xhtml").forward(req, resp);
            return;
        }
        if (h <= 0) {
            req.setAttribute("tabError", "h має бути додатним.");
            req.getRequestDispatcher("/index.xhtml").forward(req, resp);
            return;
        }
        if (b < a) {
            req.setAttribute("tabError", "b має бути ≥ a.");
            req.getRequestDispatcher("/index.xhtml").forward(req, resp);
            return;
        }

        List<TabulatedPoint> table = tabulateLnOverLinear(a, b, h);
        req.setAttribute("tabulated", table);

        req.getRequestDispatcher("/index.xhtml").forward(req, resp);
    }

    // Табулювання функції ln(x-1)/(4-x) на відрізку [a, b] з кроком h
    private List<TabulatedPoint> tabulateLnOverLinear(double a, double b, double h) {
        final double EPS = 1e-12;
        List<TabulatedPoint> list = new ArrayList<>();
        int steps = (int) Math.floor((b - a) / h + 1e-9);
        int total = steps + 1;
        for (int i = 0; i < total; i++) {
            double x = a + i * h;
            if (x > b + 1e-10) break;
            list.add(evalPoint(x, EPS));
        }
        double lastX = a + steps * h;
        if (b - lastX > 1e-9) {
            list.add(evalPoint(b, EPS));
        }
        return list;
    }

    // Обчислення значення функції ln(x-1)/(4-x) з перевіркою ОДР
    private TabulatedPoint evalPoint(double x, double eps) {
        // Перевірка області визначення, якщо x ≤ 1, то ln(x-1) не визначено
        if (x <= 1.0 + eps) {
            return new TabulatedPoint(x, null, false, "x > 1 (ОДР ln(x-1))");
        }
        if (Math.abs(x - 4.0) < 1e-12) {
            return new TabulatedPoint(x, null, false, "x ≠ 4 (ділення на нуль)");
        }
        double y = Math.log(x - 1.0) / (4.0 - x);
        return new TabulatedPoint(x, y, true, "");
    }

    // Парсинг для Double або null
    private static Double tryParseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    // Парсинг для Integer або null
    private static Integer tryParseInt(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return null; }
    }
}