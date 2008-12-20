/**
 *
 * Copyright 1997-2005 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package ucar.unidata.repository;


import org.w3c.dom.*;



import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.WikiUtil;

import ucar.unidata.xml.XmlUtil;
import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;



import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


import java.sql.*;
/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class WikiOutputHandler extends OutputHandler {



    /** _more_ */
    public static final OutputType OUTPUT_WIKI = new OutputType("Wiki",
                                                     "wiki.view",
                                                     OutputType.TYPE_HTML,
                                                     "", ICON_WIKI);

    public static final OutputType OUTPUT_WIKI_HISTORY = new OutputType("Wiki History",
                                                                        "wiki.history",
                                                                        OutputType.TYPE_HTML,
                                                                        "", ICON_WIKI);


    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public WikiOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_WIKI);
        addType(OUTPUT_WIKI_HISTORY);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     * @param state _more_
     * @param types _more_
     * @param links _more_
     * @param forHeader _more_
     *
     *
     * @throws Exception _more_
     */
    protected void getEntryLinks(Request request, State state,
                                 List<Link> links)
            throws Exception {

        if (state.entry == null) {
            return;
        }
        if (state.entry.getType().equals(WikiPageTypeHandler.TYPE_WIKIPAGE)) {
            links.add(makeLink(request, state.entry, OUTPUT_WIKI));
            links.add(makeLink(request, state.entry, OUTPUT_WIKI_HISTORY));
        }
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputEntry(Request request, Entry entry) throws Exception {

        OutputType   output = request.getOutput();
        if(output.equals(OUTPUT_WIKI_HISTORY)) {
            return outputWikiHistory(request, entry);
        }

        String   wikiText = "";
        Object[] values   = entry.getValues();
        if ((values != null) && (values.length > 0) && (values[0] != null)) {
            wikiText = (String) values[0];
        }
        StringBuffer sb = new StringBuffer(wikifyEntry(request, entry,
                              wikiText));
        return makeLinksResult(request, msg("Wiki"), sb, new State(entry));
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputWikiHistory(Request request, Entry entry)
            throws Exception {
        StringBuffer sb = new StringBuffer();

        Statement stmt = getDatabaseManager().select(Tables.WIKIHISTORY.COLUMNS,
                                                     Tables.WIKIHISTORY.NAME,
                                                     Clause.eq(Tables.WIKIHISTORY.COL_ENTRY_ID, entry.getId()),
                                                     " order by " + Tables.WIKIHISTORY.COL_DATE +" asc ");

        SqlUtil.Iterator  iter         = SqlUtil.getIterator(stmt);
        ResultSet         results;


        /*CREATE TABLE wikihistory (entry_id varchar(200),
                          version  int,
 		          user_id varchar(200),
			  date ramadda.datetime, 
			  description varchar(2000),
			  wikitext ramadda.clob);*/
        sb.append(HtmlUtil.open(HtmlUtil.TAG_TABLE));
        sb.append(HtmlUtil.row(HtmlUtil.cols(HtmlUtil.b(msg("Version")),
                                             HtmlUtil.b(msg("User")),
                                             HtmlUtil.b(msg("Date")),
                                             HtmlUtil.b(msg("Description")))));
        int version = 1;
        List<WikiPageHistory> history = new ArrayList<WikiPageHistory>();
        while ((results = iter.next()) != null) {
            while (results.next()) {
                int col = 2;
                WikiPageHistory wph = new WikiPageHistory(version++,
                                                          getUserManager().findUser(results.getString(col++),true),
                                                          getDatabaseManager().getDate(results, col++),
                                                          results.getString(col++));
                history.add(wph);
            }
        }

        for(int i = history.size()-1;i>=0;i--) {
            WikiPageHistory wph = history.get(i);
            sb.append(HtmlUtil.row(HtmlUtil.cols(""+wph.getVersion(),
                                                 wph.getUser().getLabel(),
                                                 getRepository().formatDate(wph.getDate()),
                                                 wph.getDescription())));
        }

        sb.append(HtmlUtil.close(HtmlUtil.TAG_TABLE));

        stmt.close();
        return makeLinksResult(request, msg("Wiki History"),
                               sb, new State(entry));
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGroup(Request request, Group group,
                              List<Group> subGroups, List<Entry> entries)
            throws Exception {
        return outputEntry(request, group);
    }




}

