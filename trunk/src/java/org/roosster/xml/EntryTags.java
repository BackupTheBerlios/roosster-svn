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
package org.roosster.xml;

/**
 * 
 * @author <a href="mailto:benjamin@roosster.org">Benjamin Reitzammer</a>
 */
public interface EntryTags
{
    public static final String TOTAL_ATTR   = "total";
    public static final String OFFSET_ATTR  = "offset";
    public static final String LIMIT_ATTR   = "limit";
    
    public static final String HREF_ATTR    = "href";
    
    public static final String ENTRY        = "entry";
    public static final String ENTRYLIST    = "entrylist";
    
    public static final String CONTENT      = "content";
    public static final String TAGS         = "tags";
    public static final String TAG          = "tag";
    public static final String NOTE         = "note";
    
    public static final String ISSUED       = "issued";
    public static final String MODIFIED     = "modified";
    public static final String FETCHED      = "fetched";
    
    public static final String AUTHORS      = "authors";
    public static final String AUTHOR       = "author";
    public static final String NAME_ATTR    = "name";
    public static final String EMAIL_ATTR   = "email";
    
    public static final String TYPE         = "type";
    public static final String TITLE        = "title";
}
