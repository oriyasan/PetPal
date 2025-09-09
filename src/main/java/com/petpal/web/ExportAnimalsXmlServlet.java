package com.petpal.web;

import com.petpal.model.Animal;
import com.petpal.service.AnimalService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;



/**
 * ExportAnimalsXmlServlet
 * -----------------------
 * Purpose: Exposes a simple HTTP endpoint (/export/animals.xml) that returns all animals as XML.
 * Why: Demonstrates classic Servlets (topic III in course) alongside JSF/JPA.
 * How:
 *   - Opens a read-only JPA EntityManager
 *   - Runs a JPQL query to fetch animals (+category, +owner)
 *   - Streams a well-formed XML document to the HTTP response
 * Notes:
 *   - Stateless, no session changes
 *   - Read-only (GET only)
 *   - Uses application/xml content type and UTF-8
 */


//================================================================================================================================//

@WebServlet("/export/animals.xml")
public class ExportAnimalsXmlServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("PetPalPU");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/xml; charset=UTF-8");

        EntityManager em = emf.createEntityManager();

        try (PrintWriter out = resp.getWriter()) {
            List<Animal> animals = em.createQuery(
                    "SELECT a FROM Animal a " +
                    "JOIN FETCH a.category " +
                    "JOIN FETCH a.owner " +
                    "ORDER BY a.timestamp DESC", Animal.class)
                    .getResultList();

            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<animals>");

            for (Animal a : animals) {
                out.println("  <animal>");
                out.println("    <id>" + a.getId() + "</id>");
                out.println("    <name>" + esc(a.getName()) + "</name>");
                out.println("    <category>" + esc(a.getCategory().getName()) + "</category>");
                out.println("    <gender>" + esc(a.getGender()) + "</gender>");
                out.println("    <age>" + a.getAge() + "</age>");
                out.println("    <owner>" + esc(a.getOwner().getUsername()) + "</owner>");
                if (a.getTimestamp() != null) {
                    out.println("    <timestamp>" + a.getTimestamp() + "</timestamp>");
                }
                out.println("  </animal>");
            }
            out.println("</animals>");
        } finally {
            em.close();
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s
            .replace("&","&amp;")
            .replace("<","&lt;")
            .replace(">","&gt;")
            .replace("\"","&quot;")
            .replace("'","&apos;");
    }
}