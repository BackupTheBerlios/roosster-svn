/*
 * This file is part of ROOSSTER.
 * Copyright 2004, Benjamin Reitzammer <benjamin@roosster.org>
 * All rights reserved.
 *
 * ROOSSTER is free software; you can redistribute it and/or modify
 * it under the terms of the Artistic License.
 *
 * You should have received a copy of the Artistic License
 * along with ROOSSTER; if not, go to
 * http://www.opensource.org/licenses/artistic-license.php for details
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.roosster.web;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.servlet.http.*;
import javax.servlet.ServletException;

import org.roosster.OperationException;
import org.roosster.InitializeException;
import org.roosster.Dispatcher;
import org.roosster.Registry;
import org.roosster.Configuration;
import org.roosster.Output;

/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 * @version $Id: RoossterServlet.java,v 1.1 2004/12/03 14:30:16 firstbman Exp $
 */
public class RoossterServlet extends HttpServlet
{
    private static Logger LOG = Logger.getLogger(RoossterServlet.class.getName());

    /**
     *
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException
    {
        resp.getWriter().print("HELLO ROOSSTER");
    }

    
    /**
     *
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
               throws ServletException, IOException
    {
        doPost(req, resp);
    }
}
