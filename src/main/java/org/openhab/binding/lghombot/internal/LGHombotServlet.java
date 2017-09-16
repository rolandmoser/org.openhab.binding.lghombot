/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lghombot.internal;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openhab.binding.lghombot.LGHombotBindingConstants;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet serves snapshot.js and hombot html page
 *
 * @author Roland Moser
 *
 */
public class LGHombotServlet extends HttpServlet {

    private static final long serialVersionUID = -3657478205103256079L;

    final static private Logger logger = LoggerFactory.getLogger(LGHombotServlet.class);

    protected void setHttpService(HttpService httpService) {
        try {
            httpService.registerServlet("/" + LGHombotBindingConstants.BINDING_ID, this, null,
                    httpService.createDefaultHttpContext());
            httpService.registerResources("/" + LGHombotBindingConstants.BINDING_ID + "/static", "/patch/web", null);
            logger.info("Started LG Hombot at /" + LGHombotBindingConstants.BINDING_ID);
        } catch (Exception e) {
            logger.error("Could not register lghombot servlet: {}", e.getMessage());
        }
    }

    protected void unsetHttpService(HttpService httpService) {
        httpService.unregister("/" + LGHombotBindingConstants.BINDING_ID);
        logger.info("Stopped LG Hombot");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestedFile = req.getPathInfo();
        if (!requestedFile.equals("/")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String address = req.getParameter("address");
        String refresh = req.getParameter("refresh");
        if (refresh == null) {
            refresh = "1000";
        }

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<html");
        out.println("    <head>");
        out.println("        <title>LG Hombot Camera</title>");
        out.println("        <script type=\"text/javascript\" src=\"static/snapshot.js\"></script>");
        out.println("    </head>");
        out.println("    <body onload=\"drawSnapshot('" + address + "'," + refresh + ")\">");
        out.println("        <div id=\"cam\" style=\"width: 100%; text-align:center;\">");
        out.println("           <canvas id=\"cam_canvas\" width=\"320\" height=\"240\" style=\"display: inline;\"/>");
        out.println("        </div>");
        out.println("    </body>");
        out.println("</html>");
    }
}