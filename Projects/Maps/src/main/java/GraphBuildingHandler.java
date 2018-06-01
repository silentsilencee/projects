import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Parses OSM XML files using an XML SAX parser. Used to construct the graph of roads for
 * pathfinding, under some constraints.
 * See OSM documentation on
 * <a href="http://wiki.openstreetmap.org/wiki/Key:highway">the highway tag</a>,
 * <a href="http://wiki.openstreetmap.org/wiki/Way">the way XML element</a>,
 * <a href="http://wiki.openstreetmap.org/wiki/Node">the node XML element</a>,
 * and the java
 * <a href="https://docs.oracle.com/javase/tutorial/jaxp/sax/parsing.html">SAX parser tutorial</a>.
 * <p>
 * You may find the CSCourseGraphDB and CSCourseGraphDBHandler examples useful.
 * <p>
 * The idea here is that some external library is going to walk through the XML
 * file, and your override method tells Java what to do every time it gets to the next
 * element in the file. This is a very common but strange-when-you-first-see it pattern.
 * It is similar to the Visitor pattern we discussed for graphs.
 *
 * @author Alan Yao, Maurice Lee
 */
public class GraphBuildingHandler extends DefaultHandler {
    /**
     * Only allow for non-service roads; this prevents going on pedestrian streets as much as
     * possible. Note that in Berkeley, many of the campus roads are tagged as motor vehicle
     * roads, but in practice we walk all over them with such impunity that we forget cars can
     * actually drive on them.
     */
    private static final Set<String> ALLOWED_HIGHWAY_TYPES = new HashSet<>(Arrays.asList
            ("motorway", "trunk", "primary", "secondary", "tertiary", "unclassified",
                    "residential", "living_street", "motorway_link", "trunk_link", "primary_link",
                    "secondary_link", "tertiary_link"));
    private final GraphDB g;
    boolean connected = false;
    ArrayList<Long> connect = new ArrayList<>();
    Long number;
    private String activeState = "";

    /**
     * Create a new GraphBuildingHandler.
     *
     * @param g The graph to populate with the XML data.
     */
    public GraphBuildingHandler(GraphDB g) {
        this.g = g;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        /* Some example code on how you might begin to parse XML files. */
        if (qName.equals("node")) {
            /* We encountered a new <node...> tag. */
            activeState = "node";
            number = Long.parseLong(attributes.getValue("id"));
            double lon = Double.parseDouble(attributes.getValue("lon"));
            double lat = Double.parseDouble(attributes.getValue("lat"));
            GraphDB.Node a = new GraphDB.Node(number, lat, lon);
            g.addNode(a);
            /* TODO Use the above information to save a "node" to somewhere. */
            /* Hint: A graph-like structure would be nice. */

        } else if (qName.equals("way")) {
            /* We encountered a new <way...> tag. */
            activeState = "way";
        } else if (activeState.equals("way") && qName.equals("nd")) {
            /* While looking at a way, we found a <nd...> tag. */
            //System.out.println("Id of a node in this way: " + attributes.getValue("ref"));
            Long id = Long.parseLong(attributes.getValue("ref"));
            connect.add(id);

            /* TODO Use the above id to make "possible" connections between the nodes in this way */
            /* Hint1: It would be useful to remember what was the last node in this way. */
            /* Hint2: Not all ways are valid. So, directly connecting the nodes here would be
            cumbersome since you might have to remove the connections if you later see a tag that
            makes this way invalid. Instead, think of keeping a list of possible connections and
            remember whether this way is valid or not. */

        } else if (activeState.equals("way") && qName.equals("tag")) {
            /* While looking at a way, we found a <tag...> tag. */
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");
            if (k.equals("maxspeed")) {
                int gub = 9; //System.out.println("Max Speed: " + v);
                /* TODO set the max speed of the "current way" here. */
            } else if (k.equals("highway")) {
                //System.out.println("Highway type: " + v);
                /* TODO Figure out whether this way and its connections are valid. */
                if (ALLOWED_HIGHWAY_TYPES.contains(v)) {
                    connected = true;
                }
            } else if (k.equals("name")) {
                //System.out.println("Way Name: " + v);
                int r = 7;
            }
//            System.out.println("Tag with k=" + k + ", v=" + v + ".");
        } else if (activeState.equals("node") && qName.equals("tag") && attributes.getValue("k")
                .equals("name")) {
            /* While looking at a node, we found a <tag...> with k="name". */
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");
            g.get(number).extraInfo.put(k, v);

            /* Hint: Since we found this <tag...> INSIDE a node, we should probably remember which
            node this tag belongs to. Remember XML is parsed top-to-bottom, so probably it's the
            last node that you looked at (check the first if-case). */
//            System.out.println("Node's name: " + attributes.getValue("v"));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("way")) {
            /* We are done looking at a way. (We finished looking at the nodes, speeds, etc...)*/
            /* Hint1: If you have stored the possible connections for this way, here's your
            chance to actually connect the nodes together if the way is valid. */
//            System.out.println("Finishing a way...");
            if (connected) {
                for (int i = 0; i < connect.size(); i++) {
                    if (i != connect.size() - 1) {
                        if (!g.get(connect.get(i)).connected.contains(i + 1)) {
                            if (connect.get(i) != connect.get(i + 1)) {
                                g.get(connect.get(i)).connected.add(connect.get(i + 1));
                            }
                        }
                    }
                    if (i != 0) {
                        if (!g.get(connect.get(i)).connected.contains(i - 1)) {
                            if (connect.get(i) != connect.get(i - 1)) {
                                g.get(connect.get(i)).connected.add(connect.get(i - 1));
                            }
                        }
                    }
                    g.get(connect.get(i)).connection = true;
                }
                connect = new ArrayList<>();
                connected = false;
            }
            connect = new ArrayList<>();
        }
    }
}

//  make the nodes connected in the graph


