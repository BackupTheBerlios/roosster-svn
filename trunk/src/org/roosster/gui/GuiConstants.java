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
package org.roosster.gui;

public interface GuiConstants 
{
    public static final int SEARCH_TAB_INDEX    = 0;
    public static final int EDIT_TAB_INDEX      = 1;

    public static final String TABBED_PANE          = "tabbedPane";
    public static final String EDIT_TAB             = "editTab";
    public static final String SEARCH_TAB           = "searchTab";

    // the global message label
    public static final String INFO_MSG_LABEL       = "infomessagesLabel";
    public static final String ERR_MSG_LABEL        = "errormessagesLabel";
    
    // fields and label names in the Search-Tab    
    public static final String QUERY_FIELD          = "queryField";
    public static final String SEARCH_RESULT        = "searchresultTable";
    public static final String EMPTYRESULT_LABEL    = "emptyresultLabel";
    public static final String PAGER_LABEL          = "pagerLabel";
    public static final String PAGERSIZE_BOX        = "pagersizeBox";
    public static final String PAGERBACK_BUTTON     = "pagerbackButton";
    public static final String PAGERFORWARD_BUTTON  = "pagerforwardButton";

    // fields and label names in the Edit-Tab
    public static final String URL_BUTTON           = "urlButton";
    public static final String TITLE_FIELD          = "titleField";
    public static final String TAGS_FIELD           = "tagsField";
    public static final String TAGS_LABEL           = "tagsLabel";
    public static final String NOTE_FIELD           = "noteField";
    public static final String TYPE_FIELD           = "typeField";
    public static final String AUTHOR_FIELD         = "authorField";
    public static final String AUTHOREMAIL_FIELD    = "authoremailField";
    public static final String TAGS_LIST            = "tagsList";
    
    // fields and label names in the add Tab
    public static final String ADDURL_FIELD         = "addurlField";
    public static final String ADDFETCH_FIELD       = "addfetchcontentCheckbox";
    public static final String ADDPUBLIC_FIELD      = "addpublicCheckbox";
    public static final String ADDFORCE_FIELD       = "addforceCheckbox";
    
    //
    public static final String DELICIOUSPASS_FIELD  = "syndeliciouspassField";
    public static final String DELICIOUSUSER_FIELD  = "syndelicioususerField";
    public static final String DELICIOUS_DIALOG     = "syndeliciousDialog";
}


