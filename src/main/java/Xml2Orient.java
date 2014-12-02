import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;


public class Xml2Orient {

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse("20141031_EXPORT_E3SUB-mikomc_R_3G_RBG01U.xml");
        //Document doc = builder.parse("20141031_EXPORT_E3SUB-mikomc_R_3G_RBG01U_UtranCell-BE09U1.xml");
        XPath xpath = XPathFactory.newInstance().newXPath();
        // XPath Query for showing all nodes value
        //XPathExpression expr = xpath.compile("//managedObject/*/text()");
        XPathExpression exprCelle = xpath.compile("//managedObject[@class='UtranCell']/@distName");
        Object result = exprCelle.evaluate(doc, XPathConstants.NODESET);

        TransactionalGraph graph = new OrientGraph("remote:localhost/celle", "root", "admin");
        HashMap<String, Vertex> vertici = new HashMap<String, Vertex>();
        NodeList celle = (NodeList) result;
        Vector<String> nomiCelle = new Vector<String>();

        for (int i = 0; i < celle.getLength(); i++) {
            //System.out.println(celle.item(i).getNodeValue());
            StringTokenizer st = new StringTokenizer(celle.item(i).getNodeValue(), "/");
            String nome = "";
            while (st.hasMoreElements()) {
                nome = st.nextToken();
            }
            //nomiCelle.add(nome.substring(10));

            Vertex vCelle = graph.addVertex("class:Cella");
            vCelle.setProperty("idCella", nome.substring(10));
            System.out.println("aggiungo cella: " + nome.substring(10));
            vertici.put(nome.substring(10), vCelle);
        }
        //System.out.println(nomiCelle.size());
        XPathExpression exprAdiacenze = xpath.compile("//managedObject[@class='vsDataUtranCell']/list[@name='reservedBy']/*/text()");
        Object resultAd = exprAdiacenze.evaluate(doc, XPathConstants.NODESET);
        NodeList adiacenze = (NodeList) resultAd;
        HashMap<String, String> adiacenzeMap = new HashMap<String, String>();
        for (int i = 0; i < adiacenze.getLength(); i++) {
            //System.out.println(i+"****"+adiacenze.item(i).getNodeValue());
            StringTokenizer st = new StringTokenizer(adiacenze.item(i).getNodeValue(), ",");
            String nome = "";
            while (st.hasMoreElements()) {
                nome = st.nextToken();
            }
            if (nome.substring(0, 13).equalsIgnoreCase("UtranRelation")) {
                adiacenzeMap.put(nome.substring(21), nome.substring(14, 20));
                Edge eAdiacenza = graph.addEdge(null, vertici.get(nome.substring(21)), vertici.get(nome.substring(14, 20)), "adiacenza");
                System.out.println("aggiungo adiacenza: " + nome.substring(21) + " con " + nome.substring(14, 20));
            }
        }


        graph.commit();
        graph.shutdown();
    }
}
