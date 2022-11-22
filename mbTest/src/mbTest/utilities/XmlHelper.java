package mbTest.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.powin.modbusfiles.reports.SystemInfo;

public class XmlHelper {
	// TO DO: Can we consolidate the constructors
	// TO DO: Change the first constructor to a File object

	private static File cFileName;
	private static Document doc;

	public XmlHelper(File fileName) {
		cFileName = fileName;
		doc = getDoc(fileName);
	}

	public XmlHelper(String fileContents) {
		doc = getDocFromString(fileContents);
	}

	public XmlHelper() {
		// TODO Auto-generated constructor stub
	}

	private Document getDoc(File fileName) {
		try (InputStream is = new FileInputStream(fileName)) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(is);
			return doc;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Document getDocFromString(String fileContents) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(fileContents));
			Document doc = db.parse(is);
			return doc;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private NodeList getElementsByTagName(String tagName) {
		NodeList listOfStaff = doc.getElementsByTagName(tagName);
		return listOfStaff;
	}

	private Node getNodeByNodeValue(NodeList elements, String nodeName, String nodeValue) {
		Node searchNode = null;
		for (int i = 0; i < elements.getLength(); i++) {
			Node node = elements.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String id = node.getAttributes().getNamedItem(nodeName).getTextContent();
				if (nodeValue.equals(id.trim())) {
					searchNode = node;
				}
			}
		}
		return searchNode;
	}

	private Node getNodeByNodeName(Node node, String nodeName) {
		Node searchNode = null;
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			searchNode = childNodes.item(i);
			if (searchNode.getNodeType() == Node.ELEMENT_NODE) {
				if (nodeName.equalsIgnoreCase(searchNode.getNodeName())) {
					break;
				}
			}
		}
		return searchNode;
	}

	private Node getNodeByNodeName(NodeList nodeList, String nodeName) {
		Node searchNode = null;
		for (int i = 0; i < nodeList.getLength(); i++) {
			searchNode = nodeList.item(i);
			if (searchNode.getNodeType() == Node.ELEMENT_NODE) {
				if (nodeName.equalsIgnoreCase(searchNode.getNodeName())) {
					break;
				}
			}
		}
		return searchNode;
	}

	private void setNodeValue(Node node, String newNodeValue) {
		node.setTextContent(newNodeValue);
	}

	private void writeDoc(Document doc, String outputFilenameLocal, String outputFileNameRemote) {
		if (SystemInfo.isTurtleLocal()) {// local file
			FileHelper.setFullPermissions(outputFilenameLocal);
			try (FileOutputStream output = new FileOutputStream(outputFilenameLocal)) {
				writeXml(doc, output);
			} catch (IOException | TransformerException e) {
				e.printStackTrace();
			}
		} else {
			try (FileOutputStream output = new FileOutputStream("/home/powin/localTurtle.xml")) {// copy locally
				writeXml(doc, output);
				FileHelper.copyFilesToTurtle("/home/powin/localTurtle.xml", outputFileNameRemote);// copy from local to
																									// remote
			} catch (IOException | TransformerException e) {
				e.printStackTrace();
			}
		}
	}

	// write doc to output stream
	private static void writeXml(Document doc, OutputStream output) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		Transformer transformer = transformerFactory.newTransformer();
		// pretty print
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(output);
		transformer.transform(source, result);
	}

	private void addElement(Node parentNode, String elementName, List<String> attributeNameList,
			List<String> attributeValueList) {
		Element element = doc.createElement(elementName);
		for (int index = 0; index < attributeNameList.size(); index++) {
			element.setAttribute(attributeNameList.get(index), attributeValueList.get(index));
		}
		// element.appendChild(doc.createTextNode("1000"));
		parentNode.appendChild(element);
	}

	private Node getAttributeFromNode(Node node, String attributeName) {
		return node.getAttributes().getNamedItem(attributeName);
	}

	public void editTurtleXml(String fieldName, String fieldValue) {
		XmlHelper xml = null;
		if (Constants.LOCAL_HOST.equals(PowinProperty.TURTLEHOST.toString())) {
			xml = new XmlHelper(new File(Constants.LOCALHOST_TURTLE_XML));
		} else {
			String fileContents = FileHelper.getConfigFileContents(Constants.REMOTE_TURTLE_XML, "");
			xml = new XmlHelper(fileContents);
		}
		NodeList listOfParameters = xml.getElementsByTagName("Parameter");
		Node parameter = xml.getNodeByNodeValue(listOfParameters, "name", fieldName);
		if (parameter != null) {// if parameter exists, edit
			xml.setAttribute(parameter, "value", fieldValue);
		} else {// if it does not exist, create
			xml.addElementWithAttributes(fieldName, fieldValue, listOfParameters.item(0).getParentNode());
		}
		xml.writeDoc(xml.doc, Constants.LOCALHOST_TURTLE_XML, Constants.REMOTE_TURTLE_XML);
	}
	
	public void deleteNodeFromTurtleXml(String fieldName) {
		XmlHelper xml = null;
		if (Constants.LOCAL_HOST.equals(PowinProperty.TURTLEHOST.toString())) {
			xml = new XmlHelper(new File(Constants.LOCALHOST_TURTLE_XML));
		} else {
			String fileContents = FileHelper.getConfigFileContents(Constants.REMOTE_TURTLE_XML, "");
			xml = new XmlHelper(fileContents);
		}
		NodeList listOfParameters = xml.getElementsByTagName("Parameter");
		Node parameter = xml.getNodeByNodeValue(listOfParameters, "name", fieldName);
		if (parameter != null) {// if parameter exists, edit
			deleteNode(parameter);
		} 
		xml.writeDoc(xml.doc, Constants.LOCALHOST_TURTLE_XML, Constants.REMOTE_TURTLE_XML);
	}

	public void deleteNode(Node node) {
		node.getParentNode().removeChild(node);
	}

	private void setAttribute(Node parentNode, String attributeName, String attributeValue) {
		Node value = getAttributeFromNode(parentNode, "value");
		setNodeValue(value, attributeValue);
	}

	private void addElementWithAttributes(String fieldName, String fieldValue, Node parentNode) {
		List<String> attributeNameList = new ArrayList<>();
		List<String> attributeValueList = new ArrayList<>();
		attributeNameList.add("name");
		attributeNameList.add("value");
		attributeValueList.add(fieldName);
		attributeValueList.add(fieldValue);
		addElement(parentNode, "Parameter", attributeNameList, attributeValueList);
	}

	public String queryTurtleXml(String fieldName) {
		XmlHelper xml = null;
		String fieldValue = "";
		if (Constants.LOCAL_HOST.equals(PowinProperty.TURTLEHOST.toString())) {
			xml = new XmlHelper(new File(Constants.LOCALHOST_TURTLE_XML));
		} else {
			String fileContents = FileHelper.getConfigFileContents(Constants.REMOTE_TURTLE_XML, "");
			xml = new XmlHelper(fileContents);
		}
		NodeList listOfParameters = xml.getElementsByTagName("Parameter");
		Node parameter = xml.getNodeByNodeValue(listOfParameters, "name", fieldName);

		if (parameter != null)
			fieldValue = xml.getAttributeFromNode(parameter, "value").getNodeValue();
		return fieldValue;
	}
}
