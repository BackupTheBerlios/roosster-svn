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
package org.roosster.logging;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.apache.velocity.runtime.log.LogSystem;
import org.apache.velocity.runtime.RuntimeServices;


/**
 *
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public class VelocityLogSystem implements LogSystem
{
    private static Logger LOG = Logger.getLogger(VelocityLogSystem.class.getName());

    /**
     *
     */
    public void init(RuntimeServices rs) throws Exception
    {
    }
    
    
    /**
     * 
     */
    public void logVelocityMessage(int level, String message)
    {
        switch (level) {
            case LogSystem.WARN_ID:
                LOG.warning(message);
                break;
                
            case LogSystem.ERROR_ID:
                LOG.severe(message);
                break;
                
            case LogSystem.INFO_ID:
                LOG.config(message);
                break;
                
            case LogSystem.DEBUG_ID:
                LOG.finest(message);
                break;
                
            default :
                LOG.warning("PARAMETER 'level' IN VELOCITYLOGSYSTEM contains a not expected value");
                LOG.config(message);
        }
    }


}
