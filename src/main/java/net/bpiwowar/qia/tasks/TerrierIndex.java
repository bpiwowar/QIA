package net.bpiwowar.qia.tasks;

import bpiwowar.argparser.Argument;
import bpiwowar.argparser.Logger;
import bpiwowar.argparser.checkers.IOChecker;
import bpiwowar.experiments.AbstractTask;
import bpiwowar.experiments.TaskDescription;
import org.terrier.applications.TrecTerrier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.Iterator;

/**
 * @author B. Piwowarski <benjamin@bpiwowar.net>
 * @date 20/4/12
 */
@TaskDescription(name = "index", project = {"qia", "terrier"}, description = "Index files with Terrier. Standard input should be the ir-collections output")
public class TerrierIndex extends AbstractTask {
    static final private Logger LOGGER = Logger.getLogger();

    @Argument(name = "irc", help = "The IRC configuration file (default: standard input)")
    File ircFile;

    @Argument(name = "configuration", help = "The Terrier configuration file")
    File terrierConfiguration;

    @Argument(name = "out", required = true, checkers = IOChecker.ValidDirectory.class)
    File outdir;

    @Override
    public int execute() throws Throwable {
        // Parse input stream
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document irc = ircFile != null ? db.parse(ircFile) : db.parse(System.in);

        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new IRCNamespaces());

        Element documents = (Element) xpath.evaluate("/irc:task/irc:documents", irc, XPathConstants.NODE);
        System.out.format("Document path: %s%n", documents.getAttribute("path"));


        final String doctype = documents.getAttribute("type");

        if (doctype.equals("trec")) {
            // TREC-1 to ...
            System.setProperty("trec.collection.class", "TRECCollection");
            System.setProperty("TrecDocTags.doctag", "DOC");
            System.setProperty("TrecDocTags.idtag", "DOCNO");
            System.setProperty("TrecDocTags.skip", "DOCHDR");
        } else {
            LOGGER.error("Unkown document type %s", doctype);
        }

        // Set other properties
        System.setProperty("org.terrier.terms.Stopwords", "");

        System.setProperty("block.indexing", "true");
        System.setProperty("blocks.size", "1");
        System.setProperty("terrier.var", outdir.getAbsolutePath());


        System.setProperty("collection.spec", documents.getAttribute("path"));
        TrecTerrier.main(new String[]{"-i"});

        return 0;
    }

    private static class IRCNamespaces implements NamespaceContext {
        @Override
        public String getNamespaceURI(String s) {
            if (s.equals("irc"))
                return "http://ircollections.sourceforge.net";
            return null;
        }

        @Override
        public String getPrefix(String s) {
            return null;
        }

        @Override
        public Iterator getPrefixes(String s) {
            return null;
        }
    }
}
