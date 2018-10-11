/*
 * Copyright 2017 Yong Min
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.edu.zjut.myong.epic;

import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The operator of XML configuration file, which provide necessary functions to validate (via schema), parse, and get parameters.
 */
public class XMLConfiguration implements Configuration {
    /**
     * javax.xml required
     */
    public static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    public static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    public static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    // schema?
    private boolean xsdValidate = false;

    private InputStream xml;
    private InputStream schema;

    private Document document;
    private XPath xPath;

    /**
     * Inintialing parser without schema.
     * @param xml configuration file.
     */
    public XMLConfiguration(InputStream xml) {
        this.xml = xml;
    }

    /**
     * Inintialing parser with schema.
     * @param xml configuration file.
     * @param schema schema for configuration file.
     */
    public XMLConfiguration(InputStream xml, InputStream schema) {
        if (schema != null) {
            this.xsdValidate = true;
            this.schema = schema;
        }
        this.xml = xml;
    }

    @Override
    public String getModelClassName() {
        Node mod = this.getNodeByXPath("/configuration/model");
        return mod.getAttributes().getNamedItem("class").getTextContent().trim();
    }

    @Override
    public Map<String, String> getModelParameters() {
        NodeList mps = this.getNodeListByXPath("/configuration/model/param");
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < mps.getLength(); i++) {
            Node p = mps.item(i);
            params.put(p.getAttributes().getNamedItem("name").getTextContent(),
                    p.getAttributes().getNamedItem("value").getTextContent());
        }
        return params;
    }

    @Override
    public Map<String, GamePlayer> getPlayers(int groupId) {
        NodeList pts = this.getNodeListByXPath("/configuration/group[@id='" + groupId + "']/player");
        Map<String, GamePlayer> players = new HashMap<>();
        for (int i = 0; i < pts.getLength(); i++) {
            Node p = pts.item(i);
            GamePlayer player = new GamePlayer();
            player.code = p.getAttributes().getNamedItem("code").getTextContent().trim();
            player.password = p.getAttributes().getNamedItem("password").getTextContent().trim();
            player.role = GamePlayer.ROLE.valueOf(p.getAttributes().getNamedItem("role").getTextContent());
            player.AI = Boolean.parseBoolean(p.getAttributes().getNamedItem("isAI").getTextContent());
            players.put(player.code, player);
        }
        return players;
    }

    @Override
    public Map<Integer, Game> getGroups() {
        NodeList gns = this.getNodeListByXPath("/configuration/group");
        Map<Integer, Game> groups = new HashMap<>();
        for (int i = 0; i < gns.getLength(); i++) {
            Game group = new Game();
            group.id = Integer.parseInt(gns.item(i).getAttributes().getNamedItem("id").getTextContent());
            groups.put(group.id, group);
        }
        return groups;
    }

    @Override
    public String[] getSlides() {
        // 获得幻灯片文件
        NodeList slides = this.getNodeListByXPath("/configuration/stages/instruct/slide");
        if (slides.getLength() == 0)
            slides = this.getNodeListByXPath("/configuration/stages/instruct/video");
        List<String> images = new ArrayList<>();
        for (int i = 0; i < slides.getLength(); i++) {
            Node p = slides.item(i);
            images.add(p.getTextContent());
        }
        // 清理制定目录
        File fdir = new File("./web/images/ppt");
        XMLConfiguration.deleteDir(fdir);
        fdir.mkdir();
        // copy文件
        List<String> result = new ArrayList<>();
        for (String image : images) {
            File img = new File(image);
            try {
                String pth = "./web/images/ppt/" + img.getName();
                Files.copy(img.toPath(), new File(pth).toPath());
                result.add("/images/ppt/" + img.getName());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    public GameSurveyQuestion[] getQuestons() {
        NodeList qnlist = this.getNodeListByXPath("/configuration/stages/survey4test/question");
        GameSurveyQuestion[] questions = new GameSurveyQuestion[qnlist.getLength()];
        for (int i = 0; i < qnlist.getLength(); i++) {
            Node qn = qnlist.item(i);
            questions[i] = new GameSurveyQuestion();
            questions[i].setScore(Integer.parseInt(qn.getAttributes().getNamedItem("score").getTextContent()));
            NodeList children = qn.getChildNodes();
            List<String> options = new ArrayList<>();
            for (int j = 0; i < children.getLength(); j++) {
                String v = children.item(j).getTextContent();
                if (children.item(j).getNodeName().equals("subject")) {
                    questions[i].setSubject(v);
                }
                if (children.item(j).getNodeName().equals("option")) {
                    options.add(v);
                }
                if (children.item(j).getNodeName().equals("answer")) {
                    questions[i].setAnswer(v);
                    break;
                }
            }
            if (!options.isEmpty()) {
                questions[i].setOptions(options.toArray(new String[options.size()]));
            }
        }
        return questions;
    }

    @Override
    public GameProperties getGameProgperties() {
        Node node = this.getNodeByXPath("/configuration/stages/game");
        GameProperties game = new GameProperties();
        game.nRound = Integer.parseInt(node.getAttributes().getNamedItem("round").getTextContent());
        game.inherit = Boolean.parseBoolean(node.getAttributes().getNamedItem("inherit").getTextContent());
        return game;
    }

    @Override
    public int getSurveyThreshold() {
        Node tn = this.getNodeByXPath("/configuration/stages/survey4test");
        return Integer.parseInt(tn.getAttributes().getNamedItem("threshold").getTextContent());
    }

    @Override
    public long getStageDuration(Experiment.STAGE status) {
        String xpath = "";
        if (status == Experiment.STAGE.signin) {
            xpath = "/configuration/stages/signin";
        } else if (status == Experiment.STAGE.instruct) {
            xpath = "/configuration/stages/instruct";
        } else if (status == Experiment.STAGE.survey4test) {
            xpath = "/configuration/stages/survey4test";
        } else if (status == Experiment.STAGE.communication) {
            xpath = "/configuration/stages/game/communication";
        } else if (status == Experiment.STAGE.decision) {
            xpath = "/configuration/stages/game/decision";
        } else if (status == Experiment.STAGE.redistribution) {
            xpath = "/configuration/stages/game/redistribution";
        }  else if (status == Experiment.STAGE.process) {
            xpath = "/configuration/stages/game/process";
        }
        if (xpath.isEmpty())
            return -1;
        Node tn = this.getNodeByXPath(xpath);
        String dtxt = tn.getAttributes().getNamedItem("duration").getTextContent();
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            return format.parse(dtxt).getTime() - format.parse("00:00:00").getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public boolean isCommunicable() {
        Node node = this.getNodeByXPath("/configuration/stages/game/communication");
        return Boolean.parseBoolean(node.getAttributes().getNamedItem("enable").getTextContent());
    }

    @Override
    public boolean isRedistributable() {
        return this.containNode("/configuration/stages/game/redistribution");
    }

    @Override
    public String getDecisionContent() {
        Node node = this.getNodeByXPath("/configuration/stages/game/decision");
        return node.getAttributes().getNamedItem("content").getTextContent();
    }

    @Override
    public String getRedistributionContent() {
        try {
            Node node = this.getNodeByXPath("/configuration/stages/game/redistribution");
            return node.getAttributes().getNamedItem("content").getTextContent();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getProcessContent() {
        Node node = this.getNodeByXPath("/configuration/stages/game/process");
        return node.getAttributes().getNamedItem("content").getTextContent();
    }

    @Override
    public String getFinalContent() {
        Node node = this.getNodeByXPath("/configuration/stages/game/final");
        return node.getAttributes().getNamedItem("content").getTextContent();
    }

    @Override
    public String getExperimentId() {
        Node node = this.getNodeByXPath("/configuration/experiment");
        return node.getAttributes().getNamedItem("id").getTextContent();
    }

    @Override
    public String getAdminCode() {
        Node node = this.getNodeByXPath("/configuration/administrator");
        return node.getAttributes().getNamedItem("code").getTextContent();
    }

    @Override
    public String getAdminPassword() {
        Node node = this.getNodeByXPath("/configuration/administrator");
        return node.getAttributes().getNamedItem("password").getTextContent();
    }

    @Override
    public boolean buildConfiguration() {
        try {
            // xml和schema的内容首先读入
            String xmlTxt = readTextFromInputStream(xml);
            String schemaTxt = "";
            if (schema != null)
                schemaTxt = readTextFromInputStream(schema);

            // Schema检验
            if (schema != null) {
                xml = new ByteArrayInputStream(xmlTxt.getBytes());
                schema = new ByteArrayInputStream(schemaTxt.getBytes());
                buildDocument(true);
            }

            // 构建
            xml = new ByteArrayInputStream(xmlTxt.getBytes());
            schema = new ByteArrayInputStream(schemaTxt.getBytes());
            buildDocument(false);
            buildXPath();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String readTextFromInputStream(InputStream is) {
        try {
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader reader = new BufferedReader(isr);
            String lt;
            StringBuffer result = new StringBuffer();
            while ((lt = reader.readLine()) != null) {
                result.append(lt);
                result.append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 在解析xml完成后，构建xpath对象。
     * @throws Exception 异常代表构建失败。
     */
    private void buildXPath() throws Exception {
        xPath = XPathFactory.newInstance().newXPath();
        // xPath.setNamespaceContext(new DSXPathNamespaceContent());
    }

    /**
     * 解析（包括验证）xml文件。
     * @throws Exception 异常代表解析失败。
     */
    private void buildDocument(boolean isValidated) throws Exception {
        // Step 1: create a DocumentBuilderFactory and configure it
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        isValidated = isValidated & (schema != null);

        // Set namespaceAware to true to get a DOM Level 2 tree with nodes
        // containing namesapce information.  This is necessary because the
        // default value from JAXP 1.0 was defined to be false.
        // 这个最好不要加，否则在使用xpath定位的时候，需要在buildXPath方法中引入XPath.setNamespaceContext(NamespaceContext ...);
        dbf.setNamespaceAware(isValidated);

        // Set the validation mode to either: no validation, DTD
        // validation, or XSD validation
        dbf.setValidating(isValidated);

        // Set the schema source, if any.  See the JAXP 1.2 maintenance
        // update specification for more complex usages of this feature.
        // The property "http://java.sun.com/xml/jaxp/properties/schemaSource" can have values only of the following types:
        // 1. java.lang.String
        // 2. java.io.File
        // 3. java.io.InputStream
        // 4. org.xml.sax.InputSource
        // 5. java.lang.Object[] of any of the above types
        if (isValidated) {
            dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            dbf.setAttribute(JAXP_SCHEMA_SOURCE, schema);
        }

        dbf.setIgnoringComments(true);

        // Step 2: create a DocumentBuilder that satisfies the constraints
        // specified by the DocumentBuilderFactory
        DocumentBuilder db = dbf.newDocumentBuilder();

        // Set an ErrorHandler before parsing
        OutputStreamWriter errorWriter = new OutputStreamWriter(System.err, "UTF-8");
        db.setErrorHandler(new MyErrorHandler(new PrintWriter(errorWriter, true)));

        // Step 3: parse the input file
        document = db.parse(xml);
        // FOR Debug
        // echo(document);
    }

    /**
     * Document object of the configuraion file.
     * @return
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Does the configuration contain the node in given xpath.
     * @param pathString xpath。
     * @return
     */
    public boolean containNode(String pathString) {
        Node node = this.getNodeByXPath(pathString);
        return node != null;
    }

    /**
     * Getting the XML document node in given xpath.
     * @param pathString xpath
     * @return
     */
    public Node getNodeByXPath(String pathString) {
        try {
            return (Node) xPath.evaluate(pathString, document, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Getting all XML document nodes in given xpath.
     * @param pathString xpath
     * @return
     */
    public NodeList getNodeListByXPath(String pathString) {
        try {
            return (NodeList) xPath.evaluate(pathString, document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Getting the text within XML document node in given xpath.
     * @param pathString xpath
     * @return
     */
    public String getTextContent(String pathString) {
        Node node = getNodeByXPath(pathString);
        if (node != null)
            return node.getTextContent();
        else
            return null;
    }

    /**
     * For debug。用于输出解析完成的整个xml文档。
     */
    private PrintStream out = System.out;
    private int indent = 0;
    private final String basicIndent = "  ";

    /**
     * For debug。输出以特定节点为根的文档树。
     * @param n 根节点。
     */
    private void echo(Node n) {
        // Indent to the current level before printing anything
        outputIndentation();
        int type = n.getNodeType();
        switch (type) {
            case Node.ATTRIBUTE_NODE:
                out.print("ATTR:");
                printlnCommon(n);
                break;
            case Node.CDATA_SECTION_NODE:
                out.print("CDATA:");
                printlnCommon(n);
                break;
            case Node.COMMENT_NODE:
                out.print("COMM:");
                printlnCommon(n);
                break;
            case Node.DOCUMENT_FRAGMENT_NODE:
                out.print("DOC_FRAG:");
                printlnCommon(n);
                break;
            case Node.DOCUMENT_NODE:
                out.print("DOC:");
                printlnCommon(n);
                break;
            case Node.DOCUMENT_TYPE_NODE:
                out.print("DOC_TYPE:");
                printlnCommon(n);

                // Print entities if any
                NamedNodeMap nodeMap = ((DocumentType) n).getEntities();
                indent += 2;
                for (int i = 0; i < nodeMap.getLength(); i++) {
                    Entity entity = (Entity) nodeMap.item(i);
                    echo(entity);
                }
                indent -= 2;
                break;
            case Node.ELEMENT_NODE:
                out.print("ELEM:");
                printlnCommon(n);
                // Print attributes if any.  Note: element attributes are not
                // children of ELEMENT_NODEs but are properties of their
                // associated ELEMENT_NODE.  For this reason, they are printed
                // with 2x the indent level to indicate this.
                NamedNodeMap atts = n.getAttributes();
                indent += 2;
                for (int i = 0; i < atts.getLength(); i++) {
                    Node att = atts.item(i);
                    echo(att);
                }
                indent -= 2;
                break;
            case Node.ENTITY_NODE:
                out.print("ENT:");
                printlnCommon(n);
                break;
            case Node.ENTITY_REFERENCE_NODE:
                out.print("ENT_REF:");
                printlnCommon(n);
                break;
            case Node.NOTATION_NODE:
                out.print("NOTATION:");
                printlnCommon(n);
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                out.print("PROC_INST:");
                printlnCommon(n);
                break;
            case Node.TEXT_NODE:
                out.print("TEXT:");
                printlnCommon(n);
                break;
            default:
                out.print("UNSUPPORTED NODE: " + type);
                printlnCommon(n);
                break;
        }

        // Print children if any
        indent++;
        for (Node child = n.getFirstChild(); child != null;
             child = child.getNextSibling()) {
            echo(child);
        }
        indent--;
    }

    /**
     * For debug。遍历DOM树的格式化输出信息。
     */
    private void printlnCommon(Node n) {
        out.print(" nodeName=\"" + n.getNodeName() + "\"");

        String val = n.getNamespaceURI();
        if (val != null) {
            out.print(" uri=\"" + val + "\"");
        }

        val = n.getPrefix();
        if (val != null) {
            out.print(" pre=\"" + val + "\"");
        }

        val = n.getLocalName();
        if (val != null) {
            out.print(" local=\"" + val + "\"");
        }

        val = n.getNodeValue();
        if (val != null) {
            out.print(" nodeValue=");
            if (val.trim().equals("")) {
                // Whitespace
                out.print("[WS]");
            } else {
                out.print("\"" + n.getNodeValue() + "\"");
            }
        }
        out.println();
    }

    /**
     * For debug。遍历时打印缩进。
     */
    private void outputIndentation() {
        for (int i = 0; i < indent; i++) {
            out.print(basicIndent);
        }
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    /**
     * 用于解析xml时（buildDocument函数）的出错处理。
     * @version 0.0.1
     * @author Yong Min (minyung@yahoo.com)
     */
    private static class MyErrorHandler implements ErrorHandler {
        /**
         * Error handler output goes here
         */
        private PrintWriter out;

        MyErrorHandler(PrintWriter out) {
            this.out = out;
        }

        /**
         * Returns a string describing parse exception details
         *
         * @param spe
         * @return
         */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "URI=" + systemId +
                    " Line=" + spe.getLineNumber() +
                    ": " + spe.getMessage();
            return info;
        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.

        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }

        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }
}
