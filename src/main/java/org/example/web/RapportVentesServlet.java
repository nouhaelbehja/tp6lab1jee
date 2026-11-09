package org.example.web;
import org.example.service.*;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.*;
import java.time.YearMonth;
import java.util.List;

public class RapportVentesServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String param = request.getParameter("mois");
        YearMonth mois;
        try { mois = param == null ? YearMonth.now().minusMonths(1) : YearMonth.parse(param); }
        catch (java.time.format.DateTimeParseException ex) {
            response.sendError(400, "Le mois doit être au format AAAA-MM"); return;
        }
        List<VenteCategorie> lignes = new RapportVentesService().pourMois(mois);
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!doctype html><html lang='fr'><meta charset='UTF-8'><title>Rapport des ventes</title>");
        out.println("<style>body{font-family:system-ui;max-width:850px;margin:50px auto;color:#163047}table{border-collapse:collapse;width:100%}td,th{padding:16px;border-bottom:1px solid #ddd;text-align:left}th{background:#edf5f8}</style>");
        out.println("<h1>Ventes par catégorie — " + mois + "</h1>");
        out.println("<p>Commandes validées et livrées · montants en MAD</p>");
        out.println("<form><label>Mois <input type='month' name='mois' value='" + mois + "'></label> <button>Afficher</button></form>");
        out.println("<table><thead><tr><th>Catégorie</th><th>Chiffre d’affaires (MAD)</th></tr></thead><tbody>");
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (VenteCategorie ligne : lignes) {
            total = total.add(ligne.getChiffreAffaires());
            out.println("<tr><td>" + escape(ligne.getCategorie()) + "</td><td>" + ligne.getChiffreAffaires().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + "</td></tr>");
        }
        out.println("</tbody><tfoot><tr><th>Total</th><th>" + total.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + "</th></tr></tfoot></table></html>");
    }
    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}

