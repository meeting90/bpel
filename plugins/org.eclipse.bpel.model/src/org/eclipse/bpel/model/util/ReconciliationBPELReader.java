package org.eclipse.bpel.model.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;

import org.apache.xerces.parsers.DOMParser;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Assign;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.BPELPackage;
import org.eclipse.bpel.model.BPELPlugin;
import org.eclipse.bpel.model.Branches;
import org.eclipse.bpel.model.Catch;
import org.eclipse.bpel.model.CatchAll;
import org.eclipse.bpel.model.Compensate;
import org.eclipse.bpel.model.CompensateScope;
import org.eclipse.bpel.model.CompensationHandler;
import org.eclipse.bpel.model.CompletionCondition;
import org.eclipse.bpel.model.Condition;
import org.eclipse.bpel.model.Copy;
import org.eclipse.bpel.model.Correlation;
import org.eclipse.bpel.model.CorrelationPattern;
import org.eclipse.bpel.model.CorrelationSet;
import org.eclipse.bpel.model.CorrelationSets;
import org.eclipse.bpel.model.Correlations;
import org.eclipse.bpel.model.Documentation;
import org.eclipse.bpel.model.Else;
import org.eclipse.bpel.model.ElseIf;
import org.eclipse.bpel.model.Empty;
import org.eclipse.bpel.model.EndpointReferenceRole;
import org.eclipse.bpel.model.EventHandler;
import org.eclipse.bpel.model.Exit;
import org.eclipse.bpel.model.Expression;
import org.eclipse.bpel.model.ExtensibleElement;
import org.eclipse.bpel.model.Extension;
import org.eclipse.bpel.model.Extensions;
import org.eclipse.bpel.model.FaultHandler;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.ForEach;
import org.eclipse.bpel.model.From;
import org.eclipse.bpel.model.FromPart;
import org.eclipse.bpel.model.If;
import org.eclipse.bpel.model.Import;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.Links;
import org.eclipse.bpel.model.MessageExchange;
import org.eclipse.bpel.model.MessageExchanges;
import org.eclipse.bpel.model.OnAlarm;
import org.eclipse.bpel.model.OnEvent;
import org.eclipse.bpel.model.OnMessage;
import org.eclipse.bpel.model.OpaqueActivity;
import org.eclipse.bpel.model.PartnerActivity;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.PartnerLinks;
import org.eclipse.bpel.model.Pick;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Query;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.RepeatUntil;
import org.eclipse.bpel.model.Reply;
import org.eclipse.bpel.model.Rethrow;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.ServiceRef;
import org.eclipse.bpel.model.Source;
import org.eclipse.bpel.model.Sources;
import org.eclipse.bpel.model.Target;
import org.eclipse.bpel.model.Targets;
import org.eclipse.bpel.model.TerminationHandler;
import org.eclipse.bpel.model.Throw;
import org.eclipse.bpel.model.To;
import org.eclipse.bpel.model.ToPart;
import org.eclipse.bpel.model.Validate;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.Variables;
import org.eclipse.bpel.model.Wait;
import org.eclipse.bpel.model.While;
import org.eclipse.bpel.model.extensions.BPELActivityDeserializer;
import org.eclipse.bpel.model.extensions.BPELExtensionDeserializer;
import org.eclipse.bpel.model.extensions.BPELExtensionRegistry;
import org.eclipse.bpel.model.extensions.BPELUnknownExtensionDeserializer;
import org.eclipse.bpel.model.extensions.ServiceReferenceDeserializer;
import org.eclipse.bpel.model.impl.DocumentationImpl;
import org.eclipse.bpel.model.impl.FromImpl;
import org.eclipse.bpel.model.impl.OnEventImpl;
import org.eclipse.bpel.model.impl.OnMessageImpl;
import org.eclipse.bpel.model.impl.PartnerActivityImpl;
import org.eclipse.bpel.model.impl.ToImpl;
import org.eclipse.bpel.model.messageproperties.Property;
import org.eclipse.bpel.model.messageproperties.util.MessagepropertiesConstants;
import org.eclipse.bpel.model.proxy.CorrelationSetProxy;
import org.eclipse.bpel.model.proxy.LinkProxy;
import org.eclipse.bpel.model.proxy.MessageProxy;
import org.eclipse.bpel.model.proxy.PartnerLinkProxy;
import org.eclipse.bpel.model.proxy.PartnerLinkTypeProxy;
import org.eclipse.bpel.model.proxy.PropertyProxy;
import org.eclipse.bpel.model.proxy.RoleProxy;
import org.eclipse.bpel.model.proxy.VariableProxy;
import org.eclipse.bpel.model.proxy.XSDElementDeclarationProxy;
import org.eclipse.bpel.model.proxy.XSDTypeDefinitionProxy;
import org.eclipse.bpel.model.resource.BPELLinkResolver;
import org.eclipse.bpel.model.resource.BPELResource;
import org.eclipse.bpel.model.resource.BPELVariableResolver;
import org.eclipse.bpel.model.resource.LineCapturingDOMParser;
import org.eclipse.bpel.model.resource.LinkResolver;
import org.eclipse.bpel.model.resource.VariableResolver;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.wst.wsdl.Message;
import org.eclipse.wst.wsdl.PortType;
import org.eclipse.wst.wsdl.WSDLElement;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

public class ReconciliationBPELReader implements ErrorHandler {

	// The process we are reading
	private Process process = null;
	// The resource we are reading from
	private Resource fResource = null;
	// The document builder controls various DOM characteristics
	private DocumentBuilder docBuilder = null;
	// Registry for extensibility element serializers and deserializers
	private BPELExtensionRegistry extensionRegistry = BPELExtensionRegistry
			.getInstance();

	private DOMParser fDOMParser;

	/** The XML Error handler */
	ErrorHandler fErrorHandler = null;

	/**
	 * The WS-BPEL Specification says how to resolve variables, taking into
	 * account scopes, etc. Technically, no one should override this behavior,
	 * but replacing this field with another implementation could allow you to
	 * optimize the search or provide different behavior.
	 */
	public static VariableResolver VARIABLE_RESOLVER = new BPELVariableResolver();

	/**
	 * The WS-BPEL Specification says how to resolve links, taking into account
	 * scopes, etc. Technically, no one should override this behavior, but
	 * replacing this field with another implementation could allow you to
	 * optimize the search or provide different behavior.
	 */

	public static LinkResolver LINK_RESOLVER = new BPELLinkResolver();

	/**
	 * Construct a new BPELReader using the given DocumentBuilder to determine
	 * how the DOM tree is constructed.
	 * 
	 * @param builder
	 *            the document builder to use when parsing the file
	 * @throws IOException
	 *             if no document builder is specified
	 */
	public ReconciliationBPELReader(DocumentBuilder builder) throws IOException {
		if (builder == null) {
			throw new IOException(BPELPlugin.INSTANCE
					.getString("%BPELReader.missing_doc_builder"));
		}
		this.docBuilder = builder;
	}

	/**
	 * @param parser
	 */
	public ReconciliationBPELReader(DOMParser parser) {
		this.fDOMParser = parser;
	}

	public ReconciliationBPELReader(Process process) {
		this.process = process;
		this.fResource = process.eResource();
	}
	
	public ReconciliationBPELReader() {
		this.fDOMParser = new LineCapturingDOMParser();

		// domParser.setProperty("http://xml.org/sax/features/namespaces",true);
		try {
			fDOMParser.setFeature(
					"http://apache.org/xml/features/dom/defer-node-expansion",
					false);
			fDOMParser.setFeature("http://apache.org/xml/features/xinclude",
					false);
		} catch (SAXNotRecognizedException e) {
			BPELPlugin.log("Not Recognized DOM Parser Feature", e);
		} catch (SAXNotSupportedException e) {
			BPELPlugin.log("Not Supported DOM Parser Feature", e);
		}
	}

	/**
	 * Set the error handler
	 * 
	 * @param errorHandler
	 */

	public void setErrorHandler(ErrorHandler errorHandler) {
		fErrorHandler = errorHandler;
	}

	void armErrorHandler() {

		assert (docBuilder != null || fDOMParser != null);

		if (docBuilder != null) {
			docBuilder.setErrorHandler(fErrorHandler != null ? fErrorHandler
					: this);
		} else {
			fDOMParser.setErrorHandler(fErrorHandler != null ? fErrorHandler
					: this);
		}
	}

	Document read(InputSource inputSource) throws IOException, SAXException {
		assert (docBuilder != null || fDOMParser != null) : "No document builder/parser set";

		if (docBuilder != null) {
			return docBuilder.parse(inputSource);
		}
		fDOMParser.parse(inputSource);
		return fDOMParser.getDocument();
	}

	/**
	 * Return the resource that was used to read in this BPEL process.
	 * 
	 * @return the resource that was used to read in this BPEL process.
	 */

	public Resource getResource() {
		return fResource;
	}

	/**
	 * In pass 1, we parse and create the structural elements and attributes,
	 * and add the process to the EMF resource's contents
	 * 
	 * @param document
	 *            the DOM document to parse
	 */
	protected void pass1(Document document) {
		Process p = xml2Resource(document);
		if (p != null) {
			fResource.getContents().add(p);
		}
	}	

	/**
	 * Returns a list of child nodes of <code>parentElement</code> that are
	 * {@link Element}s. Returns an empty list if no elements are found.
	 * 
	 * @param parentElement
	 *            the element to find the children of
	 * @return a node list of the children of parentElement
	 */
	protected List<Element> getChildElements(Element parentElement) {
		List<Element> list = new ArrayList<Element>();
		NodeList children = parentElement.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
				list.add((Element) children.item(i));
		}
		return list;
	}

	/**
	 * Returns a list of child nodes of <code>parentElement</code> that are
	 * {@link Element}s with a BPEL namespace that have the given
	 * <code>localName</code>. Returns an empty list if no matching elements
	 * are found.
	 * 
	 * @param parentElement
	 *            the element to find the children of
	 * @param localName
	 *            the localName to match against
	 * @return a node list of the matching children of parentElement
	 */
	protected List<Element> getBPELChildElementsByLocalName(
			Element parentElement, String localName) {
		List<Element> list = new ArrayList<Element>();
		NodeList children = parentElement.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (localName.equals(node.getLocalName())
					&& BPELUtils.isBPELElement(node)) {
				list.add((Element) node);
			}
		}
		return list;
	}

	/**
	 * Returns the first child node of <code>parentElement</code> that is an
	 * {@link Element} with a BPEL namespace and the given
	 * <code>localName</code>, or <code>null</code> if a matching element
	 * is not found.
	 * 
	 * @param parentElement
	 *            the element to find the children of
	 * @param localName
	 *            the localName to match against
	 * @return the first matching element, or null if no element was found
	 */
	protected Element getBPELChildElementByLocalName(Element parentElement,
			String localName) {
		NodeList children = parentElement.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (localName.equals(node.getLocalName())
					&& BPELUtils.isBPELElement(node)) {
				return (Element) node;
			}
		}
		return null;
	}

	/**
	 * Walk from the given element up through its parents, looking for any xmlns
	 * definitions. Collect them all in a map (mapping the prefix to the
	 * namespace value) and return the map.
	 * 
	 * @param element
	 *            the element to get the xmlns definitions for
	 * @return a map of visible xmlns definitions
	 */
	protected Map<String, String> getAllNamespacesForElement(Element element) {
		Map<String, String> nsMap = new HashMap<String, String>();
		Node tempNode = element;
		while (tempNode != null && tempNode.getNodeType() == Node.ELEMENT_NODE) {
			NamedNodeMap attrs = ((Element) tempNode).getAttributes();
			for (int i = 0; i < attrs.getLength(); i++) {
				Attr attr = (Attr) attrs.item(i);
				// XML namespace attributes use the reserved namespace
				// "http://www.w3.org/2000/xmlns/".
				if (XSDConstants.XMLNS_URI_2000.equalsIgnoreCase(attr
						.getNamespaceURI())) {
					final String key = BPELUtils.getNSPrefixMapKey(attr
							.getLocalName());
					if (!nsMap.containsKey(key)) {
						nsMap.put(key, attr.getValue());
					}
				}
			}
			tempNode = tempNode.getParentNode();
		}
		return nsMap;
	}

	/**
	 * For all attributes of the given element, ensure that their namespace
	 * prefixes are in the resource's prefix-to-namespace-map.
	 * 
	 * @param eObject
	 * @param element
	 */
	protected void saveNamespacePrefix(EObject eObject, Element element) {
		Map<String, String> nsMap = null; // lazy init since it may require a
											// new map
		NamedNodeMap attrs = element.getAttributes();

		for (int i = 0; i < attrs.getLength(); i++) {
			Attr attr = (Attr) attrs.item(i);
			// XML namespace attributes use the reserved namespace
			// "http://www.w3.org/2000/xmlns/".
			if (XSDConstants.XMLNS_URI_2000.equals(attr.getNamespaceURI())) {
				if (nsMap == null) {
					nsMap = BPELUtils.getNamespaceMap(eObject);
				}
				nsMap.put(BPELUtils.getNSPrefixMapKey(attr.getLocalName()),
						attr.getValue());
			}
		}
	}

	/**
	 * Given a DOM Element, find the child element which is a BPEL activity (of
	 * some type), parse it, and return the Activity.
	 * 
	 * @param element
	 *            the element in which to find an activity
	 * @return the activity, or null if no activity could be found
	 */
	protected Activity getChildActivity(Object parent, Element element) {
		NodeList activityElements = element.getChildNodes();
		Activity childActivity = ReconciliationHelper.getActivity(parent);
		if (childActivity != null && childActivity.getElement() != null && childActivity.getElement().getParentNode() == element) {
			return childActivity;
		}
		for (int i = 0; i < activityElements.getLength(); i++) {
			if (activityElements.item(i).getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			Element activityElement = (Element) activityElements.item(i);
			Activity activity = xml2Activity(null, activityElement);

			if (activity != null) {
				return activity;
			}
		}
		return null;
	}

	/**
	 * Sets a PartnerLink element for a given EObject. The given activity
	 * element must contain an attribute named "partnerLink".
	 * 
	 * @param activityElement
	 *            the DOM element of the activity
	 * @param eObject
	 *            the EObject in which to set the partner link
	 */
	protected void setPartnerLink(Element activityElement, final EObject eObject, final EReference reference) {
		if (!activityElement.hasAttribute("partnerLink")) {
			eObject.eSet(reference, null);
			return;
		}

		final String partnerLinkName = activityElement.getAttribute("partnerLink");
		PartnerLink targetPartnerLink = BPELUtils.getPartnerLink(eObject, partnerLinkName);
		if (targetPartnerLink == null) {
			targetPartnerLink = new PartnerLinkProxy(getResource().getURI(), partnerLinkName);
		}
		eObject.eSet(reference, targetPartnerLink);
	}

	/**
	 * Sets a Variable element for a given EObject. The given activity element
	 * must contain an attribute with the given name
	 * 
	 * @param activityElement
	 *            the DOM element of the activity
	 * @param eObject
	 *            the EObject in which to set the variable
	 * @param variableAttrName
	 *            the name of the attribute containing the variable name
	 * @param reference
	 *            the EReference which is the variable pointer in EObject
	 */
	protected void setVariable(Element activityElement, final EObject eObject,
			String variableNameAttr, final EReference reference) {
		if (!activityElement.hasAttribute(variableNameAttr)) {
			eObject.eSet(reference, null);
			return;
		}

		final String variableName = activityElement.getAttribute(variableNameAttr);
		Variable targetVariable = getVariable(eObject, variableName);
		if (targetVariable == null) {
			targetVariable = new VariableProxy(getResource().getURI(), variableName);
		}
		eObject.eSet(reference, targetVariable);
	}

	/**
	 * Find a Property name in element (in the named attribute) and set it into
	 * the given EObject. If EObject is a CorrelationSet, add the property to
	 * the list of properties. If it is a To, set the property.
	 * 
	 * @param element
	 *            the DOM element containing the property name
	 * @param eObject
	 *            the EObject in which to set the property
	 * @param propertyName
	 *            the name of the attribute containing the property name
	 */
	protected void setProperties(Element element, EObject eObject, String propertyName) {
		String propertyAttribute = element.getAttribute(propertyName);

		StringTokenizer st = new StringTokenizer(propertyAttribute);
		if (eObject instanceof CorrelationSet) {
			((CorrelationSet) eObject).getProperties().clear();
		} else if (eObject instanceof To) {
			((To) eObject).setProperty(null);
		} else if (eObject instanceof From) {
			((From) eObject).setProperty(null);
		}
		while (st.hasMoreTokens()) {
			QName qName = BPELUtils.createQName(element, st.nextToken());
			Property property = new PropertyProxy(getResource().getURI(), qName);
			if (eObject instanceof CorrelationSet) {
				((CorrelationSet) eObject).getProperties().add(property);
			} else if (eObject instanceof To) {
				((To) eObject).setProperty(property);
			} else if (eObject instanceof From) {
				((From) eObject).setProperty(property);
			}
		}
	}

	/**
	 * Sets a CompensationHandler element for a given eObject.
	 */
	protected void setCompensationHandler(Element element, EObject eObject) {
		Element compensationHandlerElement = getBPELChildElementByLocalName(element, "compensationHandler");

		CompensationHandler compensationHandler = null;
		if (compensationHandlerElement != null) {
			compensationHandler = xml2CompensationHandler(compensationHandlerElement);
			xml2ExtensibleElement(compensationHandler, compensationHandlerElement);
		}
		if (eObject instanceof Invoke)
			((Invoke) eObject).setCompensationHandler(compensationHandler);
		else if (eObject instanceof Scope)
			((Scope) eObject).setCompensationHandler(compensationHandler);	}

	/**
	 * Sets a FaultHandler element for a given extensibleElement.
	 */
	protected void setFaultHandler(Element element, ExtensibleElement extensibleElement) {
		List<Element> faultHandlerElements = getBPELChildElementsByLocalName(
				element, "faultHandlers");

		FaultHandler faultHandler = null;
		if (extensibleElement instanceof Process) {
			faultHandler = ((Process) extensibleElement).getFaultHandlers();
		} else if (extensibleElement instanceof Invoke) {
			faultHandler = ((Invoke) extensibleElement).getFaultHandler();
		}
		if (faultHandlerElements.size() > 0) {
			 faultHandler = xml2FaultHandler(faultHandler, faultHandlerElements.get(0));			
		}
		if (extensibleElement instanceof Process) {
			((Process) extensibleElement).setFaultHandlers(faultHandler);
		} else if (extensibleElement instanceof Invoke) {
			((Invoke) extensibleElement).setFaultHandler(faultHandler);
		}
	}

	/**
	 * Sets a EventHandler element for a given extensibleElement.
	 */
	protected void setEventHandler(Element element, ExtensibleElement extensibleElement) {
		List<Element> eventHandlerElements = getBPELChildElementsByLocalName(element, "eventHandlers");

		EventHandler eventHandler = null;
		if (eventHandlerElements.size() > 0) {
			 eventHandler = xml2EventHandler(eventHandlerElements.get(0));
		}
		if (extensibleElement instanceof Process)
			((Process) extensibleElement).setEventHandlers(eventHandler);
		else if (extensibleElement instanceof Scope)
			((Scope) extensibleElement).setEventHandlers(eventHandler);

	}

	/**
	 * Sets the standard attributes (name, joinCondition, and
	 * suppressJoinFailure).
	 */
	protected void setStandardAttributes(Element activityElement,
			Activity activity) {

		// Set name
		Attr name = activityElement.getAttributeNode("name");

		if (name != null && name.getSpecified()) {
			activity.setName(name.getValue());
		} else {
			activity.setName(null);
		}

		// Set suppress join failure
		Attr suppressJoinFailure = activityElement.getAttributeNode("suppressJoinFailure");

		if (suppressJoinFailure != null && suppressJoinFailure.getSpecified()) {
			activity.setSuppressJoinFailure(BPELUtils.xml2boolean(suppressJoinFailure.getValue()));
		} else {
			activity.unsetSuppressJoinFailure();
		}
	}

	/**
	 * Sets name, portType, operation, partner, variable and correlation for a
	 * given PartnerActivity object.
	 */
	protected void setOperationParms(final Element activityElement,
			final PartnerActivity activity, EReference variableReference,
			EReference inputVariableReference,
			EReference outputVariableReference, EReference partnerReference) {
		// Set partnerLink
		setPartnerLink(activityElement, activity, partnerReference);

		// Set portType
		PortType portType = null;
		if (activityElement.hasAttribute("portType")) {
			portType = BPELUtils.getPortType(getResource().getURI(), activityElement, "portType");
			activity.setPortType(portType);
		} else {
			activity.setPortType(null);
		}

		// Set operation
		if (activityElement.hasAttribute("operation")) {
			if (portType != null) {
				activity.setOperation(BPELUtils.getOperation(getResource()
						.getURI(), portType, activityElement, "operation"));
			} else {
				((PartnerActivityImpl) activity)
						.setOperationName(activityElement
								.getAttribute("operation"));
			}
		} else {
			activity.setOperation(null);
		}

		// Set variable
		if (variableReference != null) {
			setVariable(activityElement, activity, "variable",
					variableReference);
		}
		if (inputVariableReference != null) {
			setVariable(activityElement, activity, "inputVariable",
					inputVariableReference);
		}
		if (outputVariableReference != null) {
			setVariable(activityElement, activity, "outputVariable",
					outputVariableReference);
		}

		// Set correlations
		Element correlationsElement = getBPELChildElementByLocalName(
				activityElement, "correlations");
		if (correlationsElement != null) {
			Correlations correlations = xml2Correlations(correlationsElement);
			activity.setCorrelations(correlations);
		} else {
			activity.setCorrelations(null);
		}
	}

	// TODO: (DU) continue from here
	/**
	 * Sets name, portType, operation, partner, variable and correlation for a
	 * given PartnerActivity object.
	 */
	protected void setOperationParmsOnMessage(final Element activityElement, final OnMessage onMessage) {
		// Set partnerLink
		setPartnerLink(activityElement, onMessage, BPELPackage.eINSTANCE.getOnMessage_PartnerLink());

		// Set portType
		PortType portType = null;
		if (activityElement.hasAttribute("portType")) {
			portType = BPELUtils.getPortType(getResource().getURI(),
					activityElement, "portType");
			onMessage.setPortType(portType);
		}

		// Set operation
		if (activityElement.hasAttribute("operation")) {
			if (portType != null) {
				onMessage.setOperation(BPELUtils.getOperation(getResource()
						.getURI(), portType, activityElement, "operation"));
			} else {
				// If portType is not specified it will be resolved lazily and
				// so will the operation.
				// Save the deserialized name so the operation can be later
				// resolved.
				((OnMessageImpl) onMessage).setOperationName(activityElement
						.getAttribute("operation"));
			}
		}

		// Set variable
		setVariable(activityElement, onMessage, "variable",
				BPELPackage.eINSTANCE.getOnMessage_Variable());

		// Set correlations
		Element correlationsElement = getBPELChildElementByLocalName(
				activityElement, "correlations");
		if (correlationsElement != null) {
			Correlations correlations = xml2Correlations(correlationsElement);
			onMessage.setCorrelations(correlations);
		} 
	}

	/**
	 * Sets name, portType, operation, partner, variable, messageType and
	 * correlation for a given PartnerActivity object.
	 */
	protected void setOperationParmsOnEvent(final Element activityElement,
			final OnEvent onEvent) {
		// Set partnerLink
		setPartnerLink(activityElement, onEvent, BPELPackage.eINSTANCE
				.getOnEvent_PartnerLink());

		// Set portType
		PortType portType = null;
		if (activityElement.hasAttribute("portType")) {
			portType = BPELUtils.getPortType(getResource().getURI(),
					activityElement, "portType");
			onEvent.setPortType(portType);
		}

		// Set operation
		if (activityElement.hasAttribute("operation")) {
			if (portType != null) {
				onEvent.setOperation(BPELUtils.getOperation(getResource()
						.getURI(), portType, activityElement, "operation"));
			} else {
				((OnEventImpl) onEvent).setOperationName(activityElement
						.getAttribute("operation"));
			}
		}

		// Set variable
		if (activityElement.hasAttribute("variable")) {
			Variable variable = BPELFactory.eINSTANCE.createVariable();

			// Set name
			String name = activityElement.getAttribute("variable");
			variable.setName(name);
			onEvent.setVariable(variable);
			// Don't set the message type of the variable, this will happen
			// in the next step.
		}

		// Set message type
		if (activityElement.hasAttribute("messageType")) {
			QName qName = BPELUtils.createAttributeValue(activityElement,
					"messageType");
			Message messageType = new MessageProxy(getResource().getURI(),
					qName);
			onEvent.setMessageType(messageType);
		}

		// Set correlations
		Element correlationsElement = getBPELChildElementByLocalName(
				activityElement, "correlations");
		if (correlationsElement != null) {
			Correlations correlations = xml2Correlations(correlationsElement);
			onEvent.setCorrelations(correlations);
		}
	}

	/**
	 * 
	 */
	protected List<EObject> parseDocument(Document document) {

		Element element = (document != null) ? document.getDocumentElement()
				: null;
		List<EObject> list = new ArrayList<EObject>();
		if (element == null) {
			return list;
		}

		if (element.getLocalName().equals("bag")) {

			for (Node n = element.getFirstChild(); n != null; n = n
					.getNextSibling()) {
				if (n instanceof Element == false) {
					continue;
				}
				EObject next = parseElement((Element) n);
				if (next != null) {
					list.add(next);
				}
			}

		} else {
			EObject next = parseElement(element);
			if (next != null) {
				list.add(next);
			}
		}
		return list;
	}

	EObject parseElement(Element element) {

		Method parseMethod = getParseMethod(element);
		if (parseMethod == null) {
			return null;
		}
		try {
			return (EObject) parseMethod.invoke(this, element);
		} catch (Throwable t) {
			t.printStackTrace();
			// 
		}
		return null;
	}

	Method getParseMethod(Element element) {
		if (BPELUtils.isBPELElement(element) == false) {
			return null;
		}

		String methodName = element.getLocalName();
		methodName = "xml2" + Character.toUpperCase(methodName.charAt(0))
				+ methodName.substring(1);

		return lookupMethod(getClass(), methodName, Element.class);
	}

	Method lookupMethod(Class<?> target, String methodName, Class<?>... args) {
		if (target == null || target == Object.class) {
			return null;
		}

		for (Method m : target.getDeclaredMethods()) {
			if (methodName.equals(m.getName()) == false
					|| m.getParameterTypes().length != args.length) {
				continue;
			}
			Class<?> argTypes[] = m.getParameterTypes();
			for (int i = 0; i < args.length; i++) {
				if (!argTypes[i].isAssignableFrom(args[i])) {
					continue;
				}
			}
			return m;
		}
		return lookupMethod(target.getSuperclass(), methodName, args);

	}

	/**
	 * Converts an XML document to a BPEL Resource object.
	 */
	protected Process xml2Resource(Document document) {
		Element processElement = (document != null) ? document
				.getDocumentElement() : null;
		if (processElement == null) {
			return null;
		}
		return xml2Process(processElement);
	}

	/**
	 * Converts an XML process to a BPEL Process object.
	 */
	@SuppressWarnings("nls")
	protected Process xml2Process(Element processElement) {
		if (!processElement.getLocalName().equals("process")) {
			return null;
		}
		if (!BPELConstants.isBPELNamespace(processElement.getNamespaceURI())) {
			return null;
		}
		
		if (process == null) {
			process = BPELFactory.eINSTANCE.createProcess();
			process.setElement(processElement);
		}

		// Save all the references to external namespaces
		saveNamespacePrefix(process, processElement);

		// Handle Process element
		if (processElement.hasAttribute("name")) {
			process.setName(processElement.getAttribute("name"));
		} else {
			process.setName(null);
		}

		if (processElement.hasAttribute("targetNamespace")) {
			process.setTargetNamespace(processElement.getAttribute("targetNamespace"));
		} else {
			process.setTargetNamespace(null);
		}

		if (processElement.hasAttribute("suppressJoinFailure")) {
			process.setSuppressJoinFailure(BPELUtils.xml2boolean(processElement
					.getAttribute("suppressJoinFailure")));
		} else {
			process.unsetSuppressJoinFailure();
		}
		

		if (processElement.hasAttribute("exitOnStandardFault")) {
			process.setExitOnStandardFault(BPELUtils.xml2boolean(processElement
					.getAttribute("exitOnStandardFault")));
		} else {
			process.setExitOnStandardFault(false);
		}

		if (processElement.hasAttribute("variableAccessSerializable")) {
			process.setVariableAccessSerializable(BPELUtils.xml2boolean(processElement
					.getAttribute("variableAccessSerializable")));
		} else {
			process.unsetVariableAccessSerializable();
		}

		if (processElement.hasAttribute("queryLanguage")) {
			process.setQueryLanguage(processElement.getAttribute("queryLanguage"));
		} else {
			process.unsetQueryLanguage();
		}

		if (processElement.hasAttribute("expressionLanguage")) {
			process.setExpressionLanguage(processElement.getAttribute("expressionLanguage"));
		} else{
			process.unsetExpressionLanguage();
		}

		// Handle Import Elements
		List<Element> childElements = getBPELChildElementsByLocalName(processElement, "import");
		syncLists(processElement, childElements, process.getImports(), new Creator() {
			public WSDLElement create(Element element) {
				return xml2Import(null, element);
			}
		});

		// Handle PartnerLinks Element
		Element partnerLinksElement = getBPELChildElementByLocalName(
				processElement, "partnerLinks");
		if (partnerLinksElement != null) {
			process.setPartnerLinks(xml2PartnerLinks(process.getPartnerLinks(), partnerLinksElement));
		} else {
			process.setPartnerLinks(null);
		}

		// Handle Variables Element
		Element variablesElement = getBPELChildElementByLocalName(
				processElement, "variables");
		if (variablesElement != null) {
			process.setVariables(xml2Variables(process.getVariables(), variablesElement));
		} else {
			process.setVariables(null);
		}

		// Handle CorrelationSets Element
		Element correlationSetsElement = getBPELChildElementByLocalName(
				processElement, "correlationSets");
		if (correlationSetsElement != null) {
			process.setCorrelationSets(xml2CorrelationSets(process.getCorrelationSets(), correlationSetsElement));
		} else {
			process.setCorrelationSets(null);
		}

		// Handle MessageExchanges Element
		Element messageExchangesElements = getBPELChildElementByLocalName(
				processElement, "messageExchanges");
		if (messageExchangesElements != null) {
			process.setMessageExchanges(xml2MessageExchanges(process.getMessageExchanges(), messageExchangesElements));
		} else {
			process.setMessageExchanges(null);
		}

		// Handle Extensions Element
		Element extensionsElement = getBPELChildElementByLocalName(
				processElement, "extensions");
		if (extensionsElement != null) {
			process.setExtensions(xml2Extensions(extensionsElement));
		} else {
			process.setExtensions(null);
		}

		// Handle FaultHandler element
		setFaultHandler(processElement, process);

		// Handle CompensationHandler element
		// In BPEL 2.0, there is no compensation handler on process
		// setCompensationHandler(processElement, process);

		// Handle EventHandler element
		setEventHandler(processElement, process);

		// Handle Activity elements
		Activity activity = xml2Activity(null, processElement);
		process.setActivity(activity);

		xml2ExtensibleElement(process, processElement);

		return process;
	}

	/**
	 * Converts an XML partnerLinks
	 */
	protected PartnerLinks xml2PartnerLinks(PartnerLinks partnerLinks, Element partnerLinksElement) {
		if (!partnerLinksElement.getLocalName().equals("partnerLinks")) {
			return null;
		}
		
		if (partnerLinks != null) {
			partnerLinks = BPELFactory.eINSTANCE.createPartnerLinks();
			partnerLinks.setElement(partnerLinksElement);
		}

		// Save all the references to external namespaces
		saveNamespacePrefix(partnerLinks, partnerLinksElement);

		for (Element e : getBPELChildElementsByLocalName(partnerLinksElement,
				"partnerLink")) {
			partnerLinks.getChildren().add(xml2PartnerLink(e));
		}
		xml2ExtensibleElement(partnerLinks, partnerLinksElement);

		return partnerLinks;
	}

	protected Variables xml2Variables(Variables variables, Element variablesElement) {		
		if (!variablesElement.getLocalName().equals("variables"))
			return null;

		if (variables == null) {
			variables = BPELFactory.eINSTANCE.createVariables();
			variables.setElement(variablesElement);
		}

		// Save all the references to external namespaces
		saveNamespacePrefix(variables, variablesElement);
		
		List<Element> childElements = getBPELChildElementsByLocalName(variablesElement, "variable");
		EList<Variable> childrenList = variables.getChildren();
		syncLists(variablesElement, childElements, childrenList, new Creator() {
			public WSDLElement create(Element element) {
				return xml2Variable(null, element);
			}			
		});		
		
		xml2ExtensibleElement(variables, variablesElement);

		// Move variables that are extensibility elements to the list of
		// children
		// JM: What is this supposed to accomplish?
//		List<Variable> toBeMoved = new BasicEList<Variable>();
//		for (Object next : variables.getExtensibilityElements()) {
//			if (next instanceof Variable) {
//				toBeMoved.add((Variable) next);
//			}
//		}
//
//		List<?> extensibility = variables.getExtensibilityElements();
//		List<Variable> children = variables.getChildren();
//		for (Variable element : toBeMoved) {
//			extensibility.remove(element);
//			children.add(element);
//		}

		return variables;
	}

	protected CorrelationSets xml2CorrelationSets(CorrelationSets correlationSets, Element correlationSetsElement) {
		if (!correlationSetsElement.getLocalName().equals("correlationSets"))
			return null;

		if (correlationSets == null) {
			correlationSets = BPELFactory.eINSTANCE
				.createCorrelationSets();
			correlationSets.setElement(correlationSetsElement);
		}

		// Save all the references to external namespaces
		saveNamespacePrefix(correlationSets, correlationSetsElement);

		for (Element e : getBPELChildElementsByLocalName(
				correlationSetsElement, "correlationSet")) {
			correlationSets.getChildren().add(xml2CorrelationSet(e));
		}

		xml2ExtensibleElement(correlationSets, correlationSetsElement);

		return correlationSets;
	}

	protected MessageExchanges xml2MessageExchanges(MessageExchanges messageExchanges,
			Element messageExchangesElement) {
		if (!messageExchangesElement.getLocalName().equals("messageExchanges"))
			return null;

		if (messageExchanges == null) {
			messageExchanges = BPELFactory.eINSTANCE
					.createMessageExchanges();
		}

		// Save all the references to external namespaces
		saveNamespacePrefix(messageExchanges, messageExchangesElement);

		for (Element e : getBPELChildElementsByLocalName(
				messageExchangesElement, "messageExchange")) {
			messageExchanges.getChildren().add(xml2MessageExchange(e));
		}

		xml2ExtensibleElement(messageExchanges, messageExchangesElement);

		return messageExchanges;
	}

	protected Extensions xml2Extensions(Element extensionsElement) {
		if (!extensionsElement.getLocalName().equals("extensions"))
			return null;

		Extensions extensions = BPELFactory.eINSTANCE.createExtensions();
		extensions.setElement(extensionsElement);

		// Save all the references to external namespaces
		saveNamespacePrefix(extensions, extensionsElement);
		for (Element e : getBPELChildElementsByLocalName(extensionsElement,
				"extension")) {
			extensions.getChildren().add(xml2Extension(e));
		}

		xml2ExtensibleElement(extensions, extensionsElement);

		return extensions;
	}

	/**
	 * Converts an XML compensationHandler element to a BPEL CompensationHandler
	 * object.
	 */
	protected CompensationHandler xml2CompensationHandler(
			Element activityElement) {
		CompensationHandler compensationHandler = BPELFactory.eINSTANCE
				.createCompensationHandler();
		compensationHandler.setElement(activityElement);

		// Save all the references to external namespaces
		saveNamespacePrefix(compensationHandler, activityElement);

		compensationHandler.setActivity(getChildActivity(compensationHandler, activityElement));

		return compensationHandler;
	}

	/**
	 * Converts an XML correlationSet element to a BPEL CorrelationSet object.
	 */
	protected CorrelationSet xml2CorrelationSet(Element correlationSetElement) {
		CorrelationSet correlationSet = BPELFactory.eINSTANCE
				.createCorrelationSet();
		correlationSet.setElement(correlationSetElement);

		// Save all the references to external namespaces
		saveNamespacePrefix(correlationSet, correlationSetElement);

		if (correlationSetElement == null)
			return correlationSet;

		// Set name
		Attr name = correlationSetElement.getAttributeNode("name");

		if (name != null && name.getSpecified())
			correlationSet.setName(name.getValue());

		setProperties(correlationSetElement, correlationSet, "properties");

		xml2ExtensibleElement(correlationSet, correlationSetElement);

		return correlationSet;
	}

	/**
	 * Converts an XML messageExchange element to a BPEL MessageExchange object.
	 */
	protected MessageExchange xml2MessageExchange(Element messageExchangeElement) {
		MessageExchange messageExchange = BPELFactory.eINSTANCE
				.createMessageExchange();

		// Save all the references to external namespaces
		saveNamespacePrefix(messageExchange, messageExchangeElement);

		if (messageExchangeElement == null)
			return messageExchange;

		// Set name
		if (messageExchangeElement.hasAttribute("name"))
			messageExchange
					.setName(messageExchangeElement.getAttribute("name"));

		xml2ExtensibleElement(messageExchange, messageExchangeElement);

		return messageExchange;
	}

	/**
	 * Converts an XML extension element to a BPEL Extension object.
	 */
	protected Extension xml2Extension(Element extensionElement) {
		Extension extension = BPELFactory.eINSTANCE.createExtension();
		extension.setElement(extensionElement);

		// Save all the references to external namespaces
		saveNamespacePrefix(extension, extensionElement);

		if (extensionElement == null)
			return extension;

		// Set namespace
		if (extensionElement.hasAttribute("namespace"))
			extension.setNamespace(extensionElement.getAttribute("namespace"));

		// Set mustUnderstand
		if (extensionElement.hasAttribute("mustUnderstand"))
			extension.setMustUnderstand(BPELUtils.xml2boolean(extensionElement
					.getAttribute("mustUnderstand")));

		xml2ExtensibleElement(extension, extensionElement);

		return extension;
	}

	/**
	 * Converts an XML partnerLink element to a BPEL PartnerLink object.
	 */
	protected PartnerLink xml2PartnerLink(Element partnerLinkElement) {
		if (!partnerLinkElement.getLocalName().equals("partnerLink"))
			return null;

		PartnerLink partnerLink = BPELFactory.eINSTANCE.createPartnerLink();
		partnerLink.setElement(partnerLinkElement);

		// Save all the references to external namespaces
		saveNamespacePrefix(partnerLink, partnerLinkElement);

		// Set name
		if (partnerLinkElement.hasAttribute("name"))
			partnerLink.setName(partnerLinkElement.getAttribute("name"));

		if (partnerLinkElement.hasAttribute("initializePartnerRole"))
			partnerLink.setInitializePartnerRole(new Boolean(partnerLinkElement
					.getAttribute("initializePartnerRole").equals("yes")));

		Attr partnerLinkTypeName = partnerLinkElement
				.getAttributeNode("partnerLinkType");
		if (partnerLinkTypeName != null && partnerLinkTypeName.getSpecified()) {
			QName sltQName = BPELUtils.createAttributeValue(partnerLinkElement,
					"partnerLinkType");

			PartnerLinkTypeProxy slt = new PartnerLinkTypeProxy(getResource()
					.getURI(), sltQName);
			partnerLink.setPartnerLinkType(slt);

			if (slt != null) {
				partnerLink.setPartnerLinkType(slt);

				if (partnerLinkElement.hasAttribute("myRole")) {
					RoleProxy role = new RoleProxy(getResource(), slt,
							partnerLinkElement.getAttribute("myRole"));
					partnerLink.setMyRole(role);
				}
				if (partnerLinkElement.hasAttribute("partnerRole")) {
					RoleProxy role = new RoleProxy(getResource(), slt,
							partnerLinkElement.getAttribute("partnerRole"));
					partnerLink.setPartnerRole(role);
				}
			}
		}

		xml2ExtensibleElement(partnerLink, partnerLinkElement);

		return partnerLink;
	}

	/**
	 * Converts an XML variable element to a BPEL Variable object.
	 */
	protected Variable xml2Variable(Variable variable, Element variableElement) {
		if (!variableElement.getLocalName().equals("variable"))
			return null;
		
		if (variable == null) {
			variable = BPELFactory.eINSTANCE.createVariable();
			variable.setElement(variableElement);
		}

		// Save all the references to external namespaces
		saveNamespacePrefix(variable, variableElement);

		// Set name
		if (variableElement.hasAttribute("name")) {
			variable.setName(variableElement.getAttribute("name"));
		} else {
			variable.setName(null);
		}

		if (variableElement.hasAttribute("messageType")) {
			QName qName = BPELUtils.createAttributeValue(variableElement,
					"messageType");
			Message messageType = new MessageProxy(getResource().getURI(),
					qName);
			variable.setMessageType(messageType);
		} else {
			variable.setMessageType(null);
		}

		// Set xsd type
		if (variableElement.hasAttribute("type")) {
			QName qName = BPELUtils.createAttributeValue(variableElement,
					"type");
			XSDTypeDefinition type = new XSDTypeDefinitionProxy(getResource()
					.getURI(), qName);
			variable.setType(type);
		} else {
			variable.setType(null);
		}

		// Set xsd element
		if (variableElement.hasAttribute("element")) {
			QName qName = BPELUtils.createAttributeValue(variableElement,
					"element");
			XSDElementDeclaration element = new XSDElementDeclarationProxy(
					getResource().getURI(), qName);
			variable.setXSDElement(element);
		} else {
			variable.setXSDElement(null);
		}

		// from-spec
		Element fromElement = getBPELChildElementByLocalName(variableElement, "from");
		if (fromElement != null) {
			From from = variable.getFrom();			
			variable.setFrom(xml2From(from, fromElement));
		}

		xml2ExtensibleElement(variable, variableElement);

		return variable;
	}

	/**
	 * Converts an XML faultHandler element to a BPEL FaultHandler object.
	 */
	protected FaultHandler xml2FaultHandler(FaultHandler faultHandler, Element faultHandlerElement) {
		String localName = faultHandlerElement.getLocalName();
		if (!(localName.equals("faultHandlers") || localName.equals("invoke")))
			return null;
		
		if (faultHandler == null) {
			faultHandler = BPELFactory.eINSTANCE.createFaultHandler();
		}

		if (localName.equals("faultHandlers")) {
			// This is "overloaded", what's the proper facade for the fault
			// handler element in this case.
			faultHandler.setElement(faultHandlerElement);
		}

		// Save all the references to external namespaces
		saveNamespacePrefix(faultHandler, faultHandlerElement);

		List<Element> childElements = getBPELChildElementsByLocalName(faultHandlerElement, "catch");
		syncLists(faultHandlerElement, childElements, faultHandler.getCatch(), new Creator() {
			public WSDLElement create(Element element) {
				return xml2Catch(null, element);
			}
		});

		Element catchAllElement = getBPELChildElementByLocalName(
				faultHandlerElement, "catchAll");
		if (catchAllElement != null) {
			CatchAll catchAll = xml2CatchAll(faultHandler.getCatchAll(), catchAllElement);
			faultHandler.setCatchAll(catchAll);
		}

		// Only do this for an element named faultHandlers. If the element is
		// named
		// invoke, then there really is no fault handler, only a series of
		// catches.
		if (faultHandlerElement.getLocalName().equals("faultHandlers")) {
			xml2ExtensibleElement(faultHandler, faultHandlerElement);
		}

		return faultHandler;
	}

	/**
	 * Converts an XML catchAll element to a BPEL CatchAll object.
	 */
	protected CatchAll xml2CatchAll(CatchAll catchAllActivity, Element catchAllElement) {
		if (!catchAllElement.getLocalName().equals("catchAll"))
			return null;

		CatchAll catchAll;
		if (catchAllActivity instanceof CatchAll) {
			catchAll = (CatchAll)catchAllActivity;
		} else {
			catchAll = BPELFactory.eINSTANCE.createCatchAll();		
			catchAll.setElement(catchAllElement);
		}

		// Save all the references to external namespaces
		saveNamespacePrefix(catchAll, catchAllElement);

		catchAll.setActivity(getChildActivity(catchAll, catchAllElement));

		xml2ExtensibleElement(catchAll, catchAllElement);

		return catchAll;
	}

	/**
	 * Converts an XML catch element to a BPEL Catch object.
	 */
	protected Catch xml2Catch(Catch _catch, Element catchElement) {
		if (_catch == null) {
			_catch = BPELFactory.eINSTANCE.createCatch();
			_catch.setElement(catchElement);
		}

		// Save all the references to external namespaces
		saveNamespacePrefix(_catch, catchElement);

		if (catchElement == null)
			return _catch;

		if (catchElement.hasAttribute("faultName")) {
			QName qName = BPELUtils.createAttributeValue(catchElement,
					"faultName");
			_catch.setFaultName(qName);
		} else {
			_catch.setFaultName(null);
		}

		if (catchElement.hasAttribute("faultVariable")) {
			if (_catch.getFaultVariable() == null) {
				// Set fault variable
				Variable variable = BPELFactory.eINSTANCE.createVariable();
				// TODO: Should not this be the variable proxy ?
				variable.setName(catchElement.getAttribute("faultVariable"));
				_catch.setFaultVariable(variable);
			}
		} else {
			_catch.setFaultVariable(null);
		}

		if (catchElement.hasAttribute("faultMessageType")) {
			QName qName = BPELUtils.createAttributeValue(catchElement,
					"faultMessageType");
			Message messageType = new MessageProxy(getResource().getURI(),
					qName);
			_catch.setFaultMessageType(messageType);
		} else {
			_catch.setFaultMessageType(null);
		}

		if (catchElement.hasAttribute("faultElement")) {
			QName qName = BPELUtils.createAttributeValue(catchElement,
					"faultElement");
			XSDElementDeclaration element = new XSDElementDeclarationProxy(
					getResource().getURI(), qName);
			_catch.setFaultElement(element);
		} else {
			_catch.setFaultElement(null);
		}

		// Set Activities
		_catch.setActivity(getChildActivity(_catch, catchElement));

		xml2ExtensibleElement(_catch, catchElement);
		return _catch;
	}

	/**
	 * Converts an XML activity element to a BPEL Activity object.
	 */
	public Activity xml2Activity(Activity activity, Element activityElement) {
		boolean checkExtensibility = true;

		if (!BPELUtils.isBPELElement(activityElement))
			return null;

		String localName = activityElement.getLocalName();
		if (localName.equals("process")) {
			activity = getChildActivity(process, activityElement);
			checkExtensibility = false;
		} else if (localName.equals("receive")) {
			activity = xml2Receive(activity, activityElement);
		} else if (localName.equals("reply")) {
			activity = xml2Reply(activityElement);
		} else if (localName.equals("invoke")) {
			activity = xml2Invoke(activity, activityElement);
		} else if (localName.equals("assign")) {
			activity = xml2Assign(activityElement);
		} else if (localName.equals("throw")) {
			activity = xml2Throw(activity, activityElement);
		} else if (localName.equals("exit")) {
			activity = xml2Exit(activity, activityElement);
		} else if (localName.equals("wait")) {
			activity = xml2Wait(activity, activityElement);
		} else if (localName.equals("empty")) {
			activity = xml2Empty(activity, activityElement);
		} else if (localName.equals("sequence")) {
			activity = xml2Sequence(activity, activityElement);
		} else if (localName.equals("if")) {
			activity = xml2If(activity, activityElement);
		} else if (localName.equals("while")) {
			activity = xml2While(activity, activityElement);
		} else if (localName.equals("pick")) {
			activity = xml2Pick(activityElement);
		} else if (localName.equals("flow")) {
			activity = xml2Flow(activity, activityElement);
		} else if (localName.equals("scope")) {
			activity = xml2Scope(activity, activityElement);
		} else if (localName.equals("compensate")) {
			activity = xml2Compensate(activityElement);
		} else if (localName.equals("compensateScope")) {
			activity = xml2CompensateScope(activityElement);
		} else if (localName.equals("rethrow")) {
			activity = xml2Rethrow(activity, activityElement);
		} else if (localName.equals("extensionActivity")) {
			// extensionActivity is a special case. It does not have any
			// standard
			// attributes or elements, nor is it an extensible element.
			// Return immediately.
			activity = xml2ExtensionActivity(activityElement);
			return activity;
		} else if (localName.equals("opaqueActivity")) {
			activity = xml2OpaqueActivity(activityElement);
		} else if (localName.equals("forEach")) {
			activity = xml2ForEach(activity, activityElement);
		} else if (localName.equals("repeatUntil")) {
			activity = xml2RepeatUntil(activity, activityElement);
		} else if (localName.equals("validate")) {
			activity = xml2Validate(activityElement);
		} else {
			return null;
		}

		setStandardElements(activityElement, activity);

		if (checkExtensibility) {
			xml2ExtensibleElement(activity, activityElement);
			// Save all the references to external namespaces
			saveNamespacePrefix(activity, activityElement);
		}

		return activity;
	}

	protected void setStandardElements(Element activityElement,
			Activity activity) {
		// Handle targets
		Element targetsElement = getBPELChildElementByLocalName(
				activityElement, "targets");
		if (targetsElement != null) {
			activity.setTargets(xml2Targets(targetsElement));
		}

		// Handle sources
		Element sourcesElement = getBPELChildElementByLocalName(
				activityElement, "sources");
		if (sourcesElement != null) {
			activity.setSources(xml2Sources(sourcesElement));
		}
	}

	protected Targets xml2Targets(Element targetsElement) {
		Targets targets = BPELFactory.eINSTANCE.createTargets();
		targets.setElement(targetsElement);

		for (Element e : getBPELChildElementsByLocalName(targetsElement,
				"target")) {
			targets.getChildren().add(xml2Target(e));
		}
		// Join condition
		Element joinConditionElement = getBPELChildElementByLocalName(
				targetsElement, "joinCondition");
		if (joinConditionElement != null) {
			targets.setJoinCondition(xml2Condition(targets.getJoinCondition(), joinConditionElement));
		}
		xml2ExtensibleElement(targets, targetsElement);

		return targets;
	}

	protected Target xml2Target(Element targetElement) {

		final Target target = BPELFactory.eINSTANCE.createTarget();
		target.setElement(targetElement);

		// Save all the references to external namespaces
		saveNamespacePrefix(target, targetElement);

		xml2ExtensibleElement(target, targetElement);

		if (targetElement.hasAttribute("linkName")) {
			final String linkName = targetElement.getAttribute("linkName");
			createLink(target, linkName);
		}
		return target;
	}

	protected Sources xml2Sources(Element sourcesElement) {
		Sources sources = BPELFactory.eINSTANCE.createSources();
		sources.setElement(sourcesElement);
		for (Element e : getBPELChildElementsByLocalName(sourcesElement,
				"source")) {
			sources.getChildren().add(xml2Source(e));
		}
		xml2ExtensibleElement(sources, sourcesElement);

		return sources;
	}

	protected Source xml2Source(Element sourceElement) {
		final String linkName = sourceElement.getAttribute("linkName");
		final Source source = BPELFactory.eINSTANCE.createSource();
		source.setElement(sourceElement);

		// Save all the references to external namespaces
		saveNamespacePrefix(source, sourceElement);

		// Read transitionCondition element
		Element transitionConditionElement = getBPELChildElementByLocalName(
				sourceElement, "transitionCondition");
		if (transitionConditionElement != null) {
			Condition transitionCondition = xml2Condition(source.getTransitionCondition(), transitionConditionElement);
			source.setTransitionCondition(transitionCondition);
		}

		xml2ExtensibleElement(source, sourceElement);
		
		createLink(source, linkName);
		return source;
	}

	/**
	 * Converts an XML scope element to a BPEL Scope object.
	 */
	protected Activity xml2Scope(Activity scopeActivity, Element scopeElement) {
		Scope scope;
		if (scopeActivity instanceof Scope) {
			scope = (Scope)scopeActivity;
		} else { 
			scope = BPELFactory.eINSTANCE.createScope();
			scope.setElement(scopeElement);
		}

		Attr isolated = scopeElement.getAttributeNode("isolated");
		if (isolated != null && isolated.getSpecified()) {
			scope.setIsolated(BPELUtils.xml2boolean(isolated.getValue()));
		} else {
			scope.unsetIsolated();
		}

		// Handle attribute exitOnStandardFault
		Attr exitOnStandardFault = scopeElement.getAttributeNode("exitOnStandardFault");
		if (exitOnStandardFault != null && exitOnStandardFault.getSpecified()) {
			scope.setExitOnStandardFault(BPELUtils.xml2boolean(exitOnStandardFault.getValue()));
		} else {
			scope.unsetExitOnStandardFault();
		}

		// Handle Variables element
		Element variablesElement = getBPELChildElementByLocalName(scopeElement, "variables");
		if (variablesElement != null) {
			Variables variables = xml2Variables(scope.getVariables(), variablesElement);
			scope.setVariables(variables);
		} else {
			scope.setVariables(null);
		}

		// Handle CorrelationSet element
		Element correlationSetsElement = getBPELChildElementByLocalName(
				scopeElement, "correlationSets");
		if (correlationSetsElement != null) {
			CorrelationSets correlationSets = xml2CorrelationSets(scope.getCorrelationSets(), correlationSetsElement);
			scope.setCorrelationSets(correlationSets);
		} else {
			scope.setCorrelationSets(null);
		}

		// Handle PartnerLinks element
		Element partnerLinksElement = getBPELChildElementByLocalName(
				scopeElement, "partnerLinks");
		if (partnerLinksElement != null) {
			PartnerLinks partnerLinks = xml2PartnerLinks(scope.getPartnerLinks(), partnerLinksElement);
			scope.setPartnerLinks(partnerLinks);
		} else {
			scope.setPartnerLinks(null);
		}

		// MessageExchanges element
		Element messageExchangesElement = getBPELChildElementByLocalName(
				scopeElement, "messageExchanges");
		if (messageExchangesElement != null) {
			MessageExchanges messageExchanges = xml2MessageExchanges(scope.getMessageExchanges(), messageExchangesElement);
			scope.setMessageExchanges(messageExchanges);
		} else {
			scope.setMessageExchanges(null);
		}

		// Handle FaultHandler element
		Element faultHandlerElement = getBPELChildElementByLocalName(
				scopeElement, "faultHandlers");
		if (faultHandlerElement != null) {
			FaultHandler faultHandler = xml2FaultHandler(scope.getFaultHandlers(), faultHandlerElement);
			scope.setFaultHandlers(faultHandler);
		} else {
			scope.setFaultHandlers(null);
		}

		// Handle CompensationHandler element
		setCompensationHandler(scopeElement, scope);

		// Handler TerminationHandler element
		Element terminationHandlerElement = getBPELChildElementByLocalName(scopeElement, "terminationHandler");
		if (terminationHandlerElement != null) {
			TerminationHandler terminationHandler = xml2TerminationHandler(scope.getTerminationHandler(), terminationHandlerElement);
			scope.setTerminationHandler(terminationHandler);
		} else {
			scope.setTerminationHandler(null);
		}

		// Handler EventHandler element
		setEventHandler(scopeElement, scope);

		setStandardAttributes(scopeElement, scope);

		// Handle activities
		NodeList scopeElements = scopeElement.getChildNodes();

		Element activityElement = null;

		if (scopeElements != null && scopeElements.getLength() > 0) {

			for (int i = 0; i < scopeElements.getLength(); i++) {
				if (scopeElements.item(i).getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}

				activityElement = (Element) scopeElements.item(i);

				if (activityElement.getLocalName().equals("faultHandlers")
						|| activityElement.getLocalName().equals(
								"compensationHandler")) {
					continue;
				}
				Activity activity;
				if (scope.getActivity() == null || scope.getActivity().getElement() != activityElement) {
					activity = xml2Activity(null, activityElement);
				} else {
					activity = scope.getActivity();
					activityElement = activity.getElement();
				}
				if (activity != null) {
					scope.setActivity(activity);
					break;
				}
			}
		}
		if (activityElement == null) {
			scope.setActivity(null);
		}

		return scope;
	}

	/**
	 * Converts an XML flow element to a BPEL Flow object.
	 */
	protected Activity xml2Flow(Activity flowActivity, Element flowElement) {
		Flow flow;
		if (flowActivity instanceof Flow) {
			flow = (Flow)flowActivity;
		} else {
			flow = BPELFactory.eINSTANCE.createFlow();
			flow.setElement(flowElement);
		}
				
		Element linksElement = getBPELChildElementByLocalName(flowElement, "links");
		if (linksElement != null) {
			Links links = xml2Links(flow.getLinks(), linksElement);
			flow.setLinks(links);
		}

		Element completionConditionElement = getBPELChildElementByLocalName(flowElement, "completionCondition");
		if (completionConditionElement != null) {
			CompletionCondition completionCondition = xml2CompletionCondition(flow.getCompletionCondition(), completionConditionElement);
			flow.setCompletionCondition(completionCondition);
		} else {
			flow.setCompletionCondition(null);
		}

		setStandardAttributes(flowElement, flow);

		syncSequences(flowElement, flow.getActivities());

		return flow;
	}

	protected Links xml2Links(Links links,Element linksElement) {
		if (!linksElement.getLocalName().equals("links"))
			return null;
		
		if (links == null) {
			links = BPELFactory.eINSTANCE.createLinks();
			links.setElement(linksElement);
		}

		// Save all the references to external namespaces
		saveNamespacePrefix(links, linksElement);

		List<Element> childElements = getBPELChildElementsByLocalName(linksElement, "link");
		EList<Link> childrenList = links.getChildren();
		syncLists(linksElement, childElements, childrenList, new Creator() {
			public WSDLElement create(Element element) {
				return xml2Link(null, element);
			}
		});		

		// extensibility elements
		xml2ExtensibleElement(links, linksElement);

		return links;
	}

	/**
	 * Converts an XML link element to a BPEL Link object.
	 */
	protected Link xml2Link(Link link, Element linkElement) {
		if (link == null) {
			link = BPELFactory.eINSTANCE.createLink();
			link.setElement(linkElement);
		}

		// Save all the references to external namespaces
		saveNamespacePrefix(link, linkElement);

		Attr name = linkElement.getAttributeNode("name");
		if (name != null && name.getSpecified()) {
			link.setName(name.getValue());
		} else {
			link.setName(null);
		}

		xml2ExtensibleElement(link, linkElement);

		return link;
	}

	/**
	 * Converts an XML pick element to a BPEL Pick object.
	 */
	protected Activity xml2Pick(Element pickElement) {
		Pick pick = BPELFactory.eINSTANCE.createPick();
		pick.setElement(pickElement);

		// Set name
		Attr name = pickElement.getAttributeNode("name");

		if (name != null && name.getSpecified())
			pick.setName(name.getValue());

		// Set createInstance
		Attr createInstance = pickElement.getAttributeNode("createInstance");

		if (createInstance != null && createInstance.getSpecified())
			pick.setCreateInstance(Boolean.valueOf(createInstance.getValue()
					.equals("yes") ? "True" : "False"));

		NodeList pickElements = pickElement.getChildNodes();

		Element pickInstanceElement = null;

		if (pickElements != null && pickElements.getLength() > 0) {

			for (int i = 0; i < pickElements.getLength(); i++) {
				if (pickElements.item(i).getNodeType() != Node.ELEMENT_NODE)
					continue;

				pickInstanceElement = (Element) pickElements.item(i);

				if (pickInstanceElement.getLocalName().equals("onAlarm")) {
					// TODO: (DU) this is a hack to make it compile
//					OnAlarm onAlarm = xml2OnAlarm(pickInstanceElement);
//					pick.getAlarm().add(onAlarm);
				} else if (pickInstanceElement.getLocalName().equals(
						"onMessage")) {
					OnMessage onMessage = xml2OnMessage(pickInstanceElement);

					pick.getMessages().add(onMessage);
				}
			}
		}

		setStandardAttributes(pickElement, pick);

		return pick;
	}

	/**
	 * Converts an XML eventHandler element to a BPEL eventHandler object.
	 */
	protected EventHandler xml2EventHandler(Element eventHandlerElement) {
		EventHandler eventHandler = BPELFactory.eINSTANCE.createEventHandler();
		eventHandler.setElement(eventHandlerElement);

		// Save all the references to external namespaces
		saveNamespacePrefix(eventHandler, eventHandlerElement);

		NodeList eventHandlerElements = eventHandlerElement.getChildNodes();
		Element eventHandlerInstanceElement = null;
		if (eventHandlerElements != null
				&& eventHandlerElements.getLength() > 0) {

			for (int i = 0; i < eventHandlerElements.getLength(); i++) {
				if (eventHandlerElements.item(i).getNodeType() != Node.ELEMENT_NODE)
					continue;
				eventHandlerInstanceElement = (Element) eventHandlerElements
						.item(i);

				if (eventHandlerInstanceElement.getLocalName()
						.equals("onAlarm")) {
					// TODO: (DU) this is a hack to make it compile
//					OnAlarm onAlarm = xml2OnAlarm(eventHandlerInstanceElement);
//					eventHandler.getAlarm().add(onAlarm);
				} else if (eventHandlerInstanceElement.getLocalName().equals(
						"onEvent")) {
					OnEvent onEvent = xml2OnEvent(eventHandlerInstanceElement);
					eventHandler.getEvents().add(onEvent);
				}
			}
		}

		xml2ExtensibleElement(eventHandler, eventHandlerElement);
		return eventHandler;
	}

	/**
	 * Converts an XML onMessage element to a BPEL OnMessage object.
	 */
	protected OnMessage xml2OnMessage(Element onMessageElement) {
		OnMessage onMessage = BPELFactory.eINSTANCE.createOnMessage();
		onMessage.setElement(onMessageElement);

		// Save all the references to external namespaces
		saveNamespacePrefix(onMessage, onMessageElement);

		// Set several parms
		setOperationParmsOnMessage(onMessageElement, onMessage);

		// Set activity
		onMessage.setActivity(getChildActivity(onMessage, onMessageElement));

		// Set the FromPart
		for (Element e : getBPELChildElementsByLocalName(onMessageElement,
				"fromPart")) {
			onMessage.getFromPart().add(xml2FromPart(e));
		}

		xml2ExtensibleElement(onMessage, onMessageElement);

		return onMessage;
	}

	/**
	 * Converts an XML onEvent element to a BPEL OnEvent object.
	 */
	protected OnEvent xml2OnEvent(Element onEventElement) {
		OnEvent onEvent = BPELFactory.eINSTANCE.createOnEvent();
		onEvent.setElement(onEventElement);

		// Save all the references to external namespaces
		saveNamespacePrefix(onEvent, onEventElement);

		// Set several parms
		setOperationParmsOnEvent(onEventElement, onEvent);

		// Set activity
		onEvent.setActivity(getChildActivity(onEvent, onEventElement));

		// Set the FromPart
		for (Element e : getBPELChildElementsByLocalName(onEventElement,
				"fromPart")) {
			onEvent.getFromPart().add(xml2FromPart(e));
		}

		// Handle CorrelationSets Element
		Element correlationSetsElement = getBPELChildElementByLocalName(
				onEventElement, "correlationSets");
		if (correlationSetsElement != null)
			onEvent.setCorrelationSets(xml2CorrelationSets(onEvent.getCorrelationSets(),
															correlationSetsElement));

		xml2ExtensibleElement(onEvent, onEventElement);

		return onEvent;
	}

	/**
	 * Converts an XML onAlarm element to a BPEL OnAlarm object.
	 */
	protected OnAlarm xml2OnAlarm(OnAlarm onAlarm, Element onAlarmElement) {		
		if (onAlarm == null) {
			onAlarm = BPELFactory.eINSTANCE.createOnAlarm();
			onAlarm.setElement(onAlarmElement);
		}

		// Save all the references to external namespaces
		saveNamespacePrefix(onAlarm, onAlarmElement);

		// Set for element
		Element forElement = getBPELChildElementByLocalName(onAlarmElement,
				"for");
		if (forElement != null) {
			onAlarm.setFor(xml2Expression(onAlarm.getFor(), forElement));
		}

		// Set until element
		Element untilElement = getBPELChildElementByLocalName(onAlarmElement,
				"until");
		if (untilElement != null) {
			Expression expression = xml2Expression(onAlarm.getUntil(), untilElement);
			onAlarm.setUntil(expression);
		}

		// Set repeatEvery element
		Element repeatEveryElement = getBPELChildElementByLocalName(
				onAlarmElement, "repeatEvery");
		if (repeatEveryElement != null) {
			Expression expression = xml2Expression(onAlarm.getRepeatEvery(), repeatEveryElement);
			onAlarm.setRepeatEvery(expression);
		}

		// Set activity
		onAlarm.setActivity(getChildActivity(onAlarm, onAlarmElement));

		xml2ExtensibleElement(onAlarm, onAlarmElement);

		return onAlarm;
	}

	/**
	 * Converts an XML while element to a BPEL While object.
	 */
	protected Activity xml2While(Activity whileActivity, Element whileElement) {
		While _while;
		if (whileActivity instanceof While) { 
			_while = (While)whileActivity;
		} else  {
			_while = BPELFactory.eINSTANCE.createWhile();
			_while.setElement(whileElement);
		}

		// Handle condition element
		Element conditionElement = getBPELChildElementByLocalName(whileElement, "condition");
		if (conditionElement != null) {
			_while.setCondition(xml2Condition(_while.getCondition(), conditionElement));
		} else {
			_while.setCondition(null);
		}

		_while.setActivity(getChildActivity(_while, whileElement));

		setStandardAttributes(whileElement, _while);

		return _while;
	}

	/**
	 * Converts an XML terminationHandler element to a BPEL TerminationHandler
	 * object.
	 */
	protected TerminationHandler xml2TerminationHandler(TerminationHandler terminationHandler, Element terminationHandlerElement) {
		if (terminationHandler == null) {
			terminationHandler = BPELFactory.eINSTANCE
				.createTerminationHandler();
			terminationHandler.setElement(terminationHandlerElement);
		}

		// Save all the references to external namespaces
		saveNamespacePrefix(terminationHandler, terminationHandlerElement);

		terminationHandler
				.setActivity(getChildActivity(terminationHandler, terminationHandlerElement));

		xml2ExtensibleElement(terminationHandler, terminationHandlerElement);

		return terminationHandler;
	}

	/**
	 * Converts an XML if element to a BPEL If object.
	 */
	protected Activity xml2If(Activity ifActivity, Element ifElement) {
		If _if;
		if (ifActivity instanceof If) {
			_if = (If)ifActivity;
		} else {
			_if = BPELFactory.eINSTANCE.createIf();
			_if.setElement(ifElement);
		}

		// Set activity
		Activity activity = getChildActivity(_if, ifElement);
		_if.setActivity(activity);
		
		// Handle condition element
		Element conditionElement = getBPELChildElementByLocalName(ifElement,
				"condition");
		if (conditionElement != null) {
			Condition condition = xml2Condition(_if.getCondition(), conditionElement);
			_if.setCondition(condition);
		} else {
			_if.setCondition(null);
		}

		// Handle elseif
		List<Element> childElements = getBPELChildElementsByLocalName(ifElement, "elseif");
		EList<ElseIf> childrenList = _if.getElseIf();
		syncLists(ifElement, childElements, childrenList, new Creator() {
			public WSDLElement create(Element element) {
				return xml2ElseIf(null, element);
			}
		});

		// Handle else
		Element elseElement = getBPELChildElementByLocalName(ifElement, "else");
		if (elseElement != null) {
			Else _else = xml2Else(_if.getElse(), elseElement);
			_if.setElse(_else);
		}

		setStandardAttributes(ifElement, _if);

		return _if;
	}
		
	/**
	 * Converts an XML elseIf element to a BPEL ElseIf object.
	 */
	protected ElseIf xml2ElseIf(ElseIf elseIf, Element elseIfElement) {
		if (elseIf == null) {
			elseIf = BPELFactory.eINSTANCE.createElseIf();
			elseIf.setElement(elseIfElement);
		}

		// Save all the references to external namespaces
		saveNamespacePrefix(elseIf, elseIfElement);

		// Handle condition element
		Element conditionElement = getBPELChildElementByLocalName(
				elseIfElement, "condition");
		if (conditionElement != null) {
			Condition condition = xml2Condition(elseIf.getCondition(), conditionElement);
			elseIf.setCondition(condition);
		}

		// Set activity
		Activity activity = getChildActivity(elseIf, elseIfElement);
		elseIf.setActivity(activity);

		return elseIf;
	}

	/**
	 * Converts an XML condition element to a BPEL Condition object.
	 */
	protected Condition xml2Condition(Condition condition, Element conditionElement) {
		if (condition == null) {
			condition = BPELFactory.eINSTANCE.createCondition();
			condition.setElement(conditionElement);
		}
		xml2Expression(condition, conditionElement);
		return condition;
	}

	/**
	 * Converts an XML expression element to a BPEL Expression object.
	 * 
	 * Accept a pre-constructed argument. This is good for sub-types of
	 * expression.
	 * 
	 * Returns the second argument as a convenience.
	 * 
	 */
	protected Expression xml2Expression(Expression expression, Element expressionElement) {
		if (expression == null) {
			expression = BPELFactory.eINSTANCE.createExpression();
			expression.setElement(expressionElement);
		}
		
		// Save all the references to external namespaces
		saveNamespacePrefix(expression, expressionElement);

		if (expressionElement == null) {
			return expression;
		}

		// Set expressionLanguage
		if (expressionElement.hasAttribute("expressionLanguage")) {
			expression.setExpressionLanguage(expressionElement.getAttribute("expressionLanguage"));
		} else {
			expression.unsetExpressionLanguage();
		}

		// Set opaque
		if (expressionElement.hasAttribute("opaque")) {
			expression.setOpaque(BPELUtils.xml2boolean(expressionElement.getAttribute("opaque")));
		} else {
			expression.unsetOpaque();
		}

		String data = getText(expressionElement);
		if (data != null) {
			expression.setBody(data);
		} else {
			expression.setBody(null);
		}

		return expression;
	}

	protected Else xml2Else(Else _else, Element elseElement) {
		if (_else == null) {
			_else = BPELFactory.eINSTANCE.createElse();
			_else.setElement(elseElement);
		}

		// Save all the references to external namespaces
		saveNamespacePrefix(_else, elseElement);

		Activity activity = getChildActivity(_else, elseElement);
		_else.setActivity(activity);

		return _else;
	}

	/**
	 * Converts an XML sequence element to a BPEL Sequence object.
	 */
	protected Activity xml2Sequence(Activity sequenceActivity, Element sequenceElement) {
		Sequence sequence;
		if (sequenceActivity instanceof Sequence) {
			sequence = (Sequence)sequenceActivity;
		} else {
			sequence = BPELFactory.eINSTANCE.createSequence();
			sequence.setElement(sequenceElement);
		}

		syncSequences(sequenceElement, sequence.getActivities());

		setStandardAttributes(sequenceElement, sequence);

		return sequence;
	}

	/**
	 * Converts an XML empty element to a BPEL Empty object.
	 */
	protected Activity xml2Empty(Activity activity, Element emptyElement) {
		Empty empty;
		if (activity instanceof Empty) { 
			empty = (Empty)activity;
		} else {
			empty = BPELFactory.eINSTANCE.createEmpty();
			empty.setElement(emptyElement);
		}

		setStandardAttributes(emptyElement, empty);

		return empty;
	}

	/**
	 * Converts an XML opaqueActivity element to a BPEL OpaqueActivity object.
	 */
	protected Activity xml2OpaqueActivity(Element opaqueActivityElement) {
		OpaqueActivity opaqueActivity = BPELFactory.eINSTANCE
				.createOpaqueActivity();
		opaqueActivity.setElement(opaqueActivityElement);

		setStandardAttributes(opaqueActivityElement, opaqueActivity);

		return opaqueActivity;
	}

	/**
	 * Converts an XML valdateXML element to a BPEL ValidateXML object.
	 */
	protected Activity xml2Validate(Element validateElement) {
		final Validate validate = BPELFactory.eINSTANCE.createValidate();
		validate.setElement(validateElement);

		setStandardAttributes(validateElement, validate);
		if (validateElement.hasAttribute("variables")) {
			String variables = validateElement.getAttribute("variables");
			StringTokenizer st = new StringTokenizer(variables);

			while (st.hasMoreTokens()) {
				final String variableName = st.nextToken();
				// We must do this as a post load runnable because the variable
				// might not
				// exist yet.
				Variable targetVariable = getVariable(validate,
						variableName);
				if (targetVariable == null) {
					targetVariable = new VariableProxy(getResource()
							.getURI(), variableName);
				}
				validate.getVariables().add(targetVariable);
			}
		}
		return validate;
	}

	/**
	 * Converts an XML rethrow element to a BPEL Rethrow object.
	 */
	protected Activity xml2Rethrow(Activity rethrowActivity, Element rethrowElement) {
		Rethrow rethrow;
		if (rethrowActivity instanceof Rethrow) {
			rethrow = (Rethrow)rethrowActivity;
		} else {
			rethrow = BPELFactory.eINSTANCE.createRethrow();
			rethrow.setElement(rethrowElement);
		}

		setStandardAttributes(rethrowElement, rethrow);

		return rethrow;
	}

	/**
	 * Converts an XML extensionactivity element to a BPEL ExtensionActivity
	 * object.
	 */
	protected Activity xml2ExtensionActivity(Element extensionActivityElement) {
		// Do not call setStandardAttributes here because
		// extensionActivityElement
		// doesn't have them.

		// Find the child element.
		List<Element> nodeList = getChildElements(extensionActivityElement);

		if (nodeList.size() == 1) {
			Element child = nodeList.get(0);
			// We found a child element. Look up a deserializer for this
			// activity and call it.
			String localName = child.getLocalName();
			String namespace = child.getNamespaceURI();
			QName qname = new QName(namespace, localName);
			BPELActivityDeserializer deserializer = extensionRegistry
					.getActivityDeserializer(qname);
			if (deserializer != null) {
				// Deserialize the DOM element and return the new Activity
				Map<String, String> nsMap = getAllNamespacesForElement(child);
//				Activity activity = deserializer.unmarshall(qname, child,
//						process, nsMap, extensionRegistry, getResource()
//								.getURI(), this);
				// TODO: (DU) this is a hack to make it compile
				Activity activity = BPELFactory.eINSTANCE.createExtensionActivity();
				// Now let's do the standard attributes and elements
				setStandardAttributes(child, activity);
				setStandardElements(child, activity);

				// Don't do extensibility because extensionActivity is not
				// extensible.
				// If individual extensionActivity subclasses are actually
				// extensible, they
				// have to do this themselves in their deserializer.
				return activity;
			}
		}
		// Fallback is to create a new extensionActivity.
		return BPELFactory.eINSTANCE.createExtensionActivity();
	}

	/**
	 * Converts an XML wait element to a BPEL Wait object.
	 */
	protected Activity xml2Wait(Activity waitActivity, Element waitElement) {
		Wait wait;
		if (waitActivity instanceof Wait) {
			wait = (Wait)waitActivity;
		} else {
			wait = BPELFactory.eINSTANCE.createWait();
			wait.setElement(waitElement);
		}

		// Set name
		Attr name = waitElement.getAttributeNode("name");

		if (name != null && name.getSpecified())
			wait.setName(name.getValue());

		// Set for element
		Element forElement = getBPELChildElementByLocalName(waitElement, "for");
		if (forElement != null) {
			Expression expression = xml2Expression(wait.getFor(), forElement);
			wait.setFor(expression);
		}

		// Set until element
		Element untilElement = getBPELChildElementByLocalName(waitElement,
				"until");
		if (untilElement != null) {
			Expression expression = xml2Expression(wait.getUntil(), untilElement);
			wait.setUntil(expression);
		}

		setStandardAttributes(waitElement, wait);

		return wait;
	}

	/**
	 * Converts an XML exit element to a BPEL Exit object.
	 */
	protected Activity xml2Exit(Activity activity, Element exitElement) {
		Exit exit;
		if (activity instanceof Exit) {
			exit = (Exit)activity;
		} else {
			exit = BPELFactory.eINSTANCE.createExit();
			exit.setElement(exitElement);
		}
		setStandardAttributes(exitElement, exit);

		return exit;
	}

	/**
	 * Converts an XML throw element to a BPEL Throw object.
	 */
	protected Activity xml2Throw(Activity throwActivity, Element throwElement) {
		Throw _throw;
		if (throwActivity instanceof Throw) {
			_throw = (Throw)throwActivity;
		} else {
			_throw = BPELFactory.eINSTANCE.createThrow();
			_throw.setElement(throwElement);
		}

		if (throwElement.hasAttribute("faultName")) {
			QName qName = BPELUtils.createAttributeValue(throwElement,
					"faultName");
			_throw.setFaultName(qName);
		} else {
			_throw.setFaultName(null);
		}

		// Set fault variable name
		setVariable(throwElement, _throw, "faultVariable", BPELPackage.eINSTANCE.getThrow_FaultVariable());
		
		setStandardAttributes(throwElement, _throw);

		return _throw;
	}

	/**
	 * Converts an XML assign element to a BPEL Assign object.
	 */
	protected Activity xml2Assign(Element assignElement) {
		Assign assign = BPELFactory.eINSTANCE.createAssign();
		assign.setElement(assignElement);

		if (assignElement.hasAttribute("validate")) {
			assign.setValidate(new Boolean(assignElement.getAttribute(
					"validate").equals("yes")));
		}

		for (Element copyElement : getBPELChildElementsByLocalName(
				assignElement, "copy")) {
			assign.getCopy().add(xml2Copy(copyElement));
		}

		setStandardAttributes(assignElement, assign);

		return assign;
	}

	/**
	 * Converts an XML copy element to a BPEL Copy object.
	 */
	protected Copy xml2Copy(Element copyElement) {
		Copy copy = BPELFactory.eINSTANCE.createCopy();
		copy.setElement(copyElement);

		// Save all the references to external namespaces
		saveNamespacePrefix(copy, copyElement);

		Element fromElement = getBPELChildElementByLocalName(copyElement,
				"from");
		if (fromElement != null) {
			From from = BPELFactory.eINSTANCE.createFrom();
			from.setElement(fromElement);

			xml2From(from, fromElement);
			copy.setFrom(from);
		}

		Element toElement = getBPELChildElementByLocalName(copyElement, "to");
		if (toElement != null) {
			To to = BPELFactory.eINSTANCE.createTo();
			to.setElement(toElement);

			xml2To(to, toElement);
			copy.setTo(to);
		}

		if (copyElement.hasAttribute("keepSrcElementName"))
			copy.setKeepSrcElementName(new Boolean(copyElement.getAttribute(
					"keepSrcElementName").equals("yes")));

		if (copyElement.hasAttribute("ignoreMissingFromData"))
			copy.setIgnoreMissingFromData(new Boolean(copyElement.getAttribute(
					"ignoreMissingFromData").equals("yes")));

		xml2ExtensibleElement(copy, copyElement);

		return copy;
	}

	/**
	 * Converts an XML toPart element to a BPEL ToPart object.
	 */
	protected ToPart xml2ToPart(Element toPartElement) {
		ToPart toPart = BPELFactory.eINSTANCE.createToPart();

		toPart.setElement(toPartElement);

		// Save all the references to external namespaces
		saveNamespacePrefix(toPart, toPartElement);

		// Handle part attribute
		if (toPartElement.hasAttribute("part"))
			toPart.setPart(toPartElement.getAttribute("part"));

		// Handle from-spec
		Element fromElement = getBPELChildElementByLocalName(toPartElement,
				"from");
		if (fromElement != null) {
			From from = BPELFactory.eINSTANCE.createFrom();
			from.setElement(fromElement);

			xml2From(from, fromElement);
			toPart.setFrom(from);
		}

		return toPart;
	}

	/**
	 * Converts an XML fromPart element to a BPEL FromPart object.
	 */
	protected FromPart xml2FromPart(Element fromPartElement) {
		FromPart fromPart = BPELFactory.eINSTANCE.createFromPart();
		fromPart.setElement(fromPartElement);

		// Save all the references to external namespaces
		saveNamespacePrefix(fromPart, fromPartElement);

		// Handle part attribute
		if (fromPartElement.hasAttribute("part"))
			fromPart.setPart(fromPartElement.getAttribute("part"));

		// Handle to-spec
		Element toElement = getBPELChildElementByLocalName(fromPartElement,
				"to");
		if (toElement != null) {
			To to = BPELFactory.eINSTANCE.createTo();
			to.setElement(toElement);

			xml2To(to, toElement);
			fromPart.setTo(to);
		}

		return fromPart;
	}

	/**
	 * Converts an XML "to" element to a BPEL To object.
	 */
	protected void xml2To(To to, Element toElement) {
		// Save all the references to external namespaces
		saveNamespacePrefix(to, toElement);

		// Set variable
		Attr variable = toElement.getAttributeNode("variable");

		if (variable != null && variable.getSpecified()) {
			setVariable(toElement, to, "variable", BPELPackage.eINSTANCE
					.getTo_Variable());
		}

		// Set part
		Attr part = toElement.getAttributeNode("part");

		if (part != null && part.getSpecified()) {
			final String partAttr = toElement.getAttribute("part");
			((ToImpl) to).setPartName(partAttr);
		}

		// Set partnerLink
		Attr partnerLink = toElement.getAttributeNode("partnerLink");

		if (partnerLink != null && partnerLink.getSpecified()) {
			setPartnerLink(toElement, to, BPELPackage.eINSTANCE
					.getTo_PartnerLink());
		}

		// Set property
		Attr property = toElement.getAttributeNode("property");

		if (property != null && property.getSpecified()) {
			setProperties(toElement, to, "property");
		}

		// Set query element
		Element queryElement = getBPELChildElementByLocalName(toElement,
				"query");
		if (queryElement != null) {
			to.setQuery(xml2Query(to.getQuery(), queryElement));
		} else {

			// must be expression
			Expression expressionObject = BPELFactory.eINSTANCE
					.createExpression();
			expressionObject.setElement(toElement);

			to.setExpression(expressionObject);

			// Set expressionLanguage
			if (toElement.hasAttribute("expressionLanguage")) {
				expressionObject.setExpressionLanguage(toElement
						.getAttribute("expressionLanguage"));
			}

			// Set expression text
			// Get the condition text
			String data = getText(toElement);
			if (data != null) {
				expressionObject.setBody(data);
			}
		}
	}

	/**
	 * Converts an XML "from" element to a BPEL From object.
	 */
	protected From xml2From(From from, Element fromElement) {
		if (from == null) {
			from = BPELFactory.eINSTANCE.createFrom();
			from.setElement(fromElement);
		}
		
		/** This is basically what's in xml2To */

		// Save all the references to external namespaces
		saveNamespacePrefix(from, fromElement);

		// Set variable
		Attr variable = fromElement.getAttributeNode("variable");

		if (variable != null && variable.getSpecified()) {
			setVariable(fromElement, from, "variable", BPELPackage.eINSTANCE
					.getFrom_Variable());
		} else {
			from.setVariable(null);
		}

		// Set part
		Attr part = fromElement.getAttributeNode("part");

		if (part != null && part.getSpecified()) {
			final String partAttr = fromElement.getAttribute("part");
			((FromImpl) from).setPartName(partAttr);
		} else {
			((FromImpl) from).setPartName(null);
		}

		// Set partnerLink
		Attr partnerLink = fromElement.getAttributeNode("partnerLink");
		if (partnerLink != null && partnerLink.getSpecified()) {
			setPartnerLink(fromElement, from, BPELPackage.eINSTANCE
					.getFrom_PartnerLink());
		} else {
			from.setPartnerLink(null);
		}

		// Set property
		Attr property = fromElement.getAttributeNode("property");
		if (property != null && property.getSpecified()) {
			setProperties(fromElement, from, "property");
		}

		// Set query element
		Element queryElement = getBPELChildElementByLocalName(fromElement,
				"query");
		if (queryElement != null) {
			from.setQuery(xml2Query(from.getQuery(), queryElement));
		} else {
			from.setQuery(null);
		}

		Attr endpointReference = fromElement.getAttributeNode("endpointReference");
		if (endpointReference != null && endpointReference.getSpecified()) {
			from.setEndpointReference(EndpointReferenceRole
					.get(endpointReference.getValue()));
		} else {
			from.setEndpointReference(null);
		}

		// Set service-ref element
		Element serviceRefElement = getBPELChildElementByLocalName(fromElement, "service-ref");
		if (serviceRefElement != null) {
			from.setServiceRef(xml2ServiceRef(from.getServiceRef(), serviceRefElement));
		} else {
			from.setServiceRef(null);
		}
		
		// Literal node
		Element literalElement = getBPELChildElementByLocalName(fromElement, "literal");
		if (literalElement != null) {
			StringBuilder elementData = new StringBuilder(256);

			NodeList nl = literalElement.getChildNodes();

			outer: for (int i = 0; i < nl.getLength(); i++) {

				Node n = nl.item(i);
				switch (n.getNodeType()) {
				case Node.ELEMENT_NODE:
					elementData.setLength(0);
					elementData.append(BPELUtils.elementToString((Element) n));
					break outer;

				case Node.TEXT_NODE:
				case Node.CDATA_SECTION_NODE:
					elementData.append(n.getTextContent());
					break;
				}
			}

			from.setUnsafeLiteral(Boolean.FALSE);
			String elementDataFinal = elementData.toString();
			if (isEmptyOrWhitespace(elementDataFinal) == false) {
				from.setUnsafeLiteral(Boolean.TRUE);
				from.setLiteral(elementDataFinal);
			}

		} else {

			// must be expression
			Expression expressionObject = from.getExpression();
			if (expressionObject == null) {
				expressionObject = BPELFactory.eINSTANCE.createExpression();
				expressionObject.setElement(fromElement);
			}

			from.setExpression(xml2Expression(expressionObject, fromElement));
		}

		// Set opaque
		Attr opaque = fromElement.getAttributeNode("opaque");
		if (opaque != null && opaque.getSpecified()) {
			from.setOpaque(BPELUtils.xml2boolean(opaque.getValue()));
		} else {
			from.unsetOpaque();
		}

		// See if there is an xsi:type attribue.
		if (fromElement.hasAttribute("xsi:type")) {
			QName qName = BPELUtils.createAttributeValue(fromElement,
					"xsi:type");
			XSDTypeDefinition type = new XSDTypeDefinitionProxy(getResource()
					.getURI(), qName);
			from.setType(type);
		} else {
			from.setType(null);
		}
		return from;
	}

	protected ServiceRef xml2ServiceRef(ServiceRef serviceRef, Element serviceRefElement) {
		if (serviceRef == null) {
			serviceRef = BPELFactory.eINSTANCE.createServiceRef();
			serviceRef.setElement(serviceRefElement);
		}

		// Set reference scheme
		if (serviceRefElement.hasAttribute("reference-scheme")) {
			String scheme = serviceRefElement
					.getAttribute("reference-scheme");
			serviceRef.setReferenceScheme(scheme);
		}

		// Set the value of the service reference

		// Determine whether or not there is an element in the child list.
		Node candidateChild = null;
		NodeList nodeList = serviceRefElement.getChildNodes();
		int length = nodeList.getLength();
		for (int i = 0; i < length; i++) {
			Node child = nodeList.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				candidateChild = child;
				break;
			}
		}
		if (candidateChild == null) {
			candidateChild = serviceRefElement.getFirstChild();
		}
		String data = getText(candidateChild);

		if (data == null) {
			// No text or CDATA node. If it's an element node, then
			// deserialize and install.
			if (candidateChild != null
					&& candidateChild.getNodeType() == Node.ELEMENT_NODE) {
				// Look if there's an ExtensibilityElement deserializer for
				// this element
				Element childElement = (Element) candidateChild;
				QName qname = new QName(childElement.getNamespaceURI(),
						childElement.getLocalName());
				BPELExtensionDeserializer deserializer = null;
				try {
					deserializer = (BPELExtensionDeserializer) extensionRegistry
							.queryDeserializer(ExtensibleElement.class,
									qname);
				} catch (WSDLException e) {
				}
				if (deserializer != null
						&& !(deserializer instanceof BPELUnknownExtensionDeserializer)) {
					// Deserialize the DOM element and add the new
					// Extensibility element to the parent
					// ExtensibleElement
					try {
						Map<String, String> nsMap = getAllNamespacesForElement(serviceRefElement);
						ExtensibilityElement extensibilityElement = deserializer
								.unmarshall(ExtensibleElement.class, qname,
										childElement, process, nsMap,
										extensionRegistry, getResource()
												.getURI());
						serviceRef.setValue(extensibilityElement);
					} catch (WSDLException e) {
						throw new WrappedException(e);
					}
				} else {
					ServiceReferenceDeserializer referenceDeserializer = extensionRegistry
							.getServiceReferenceDeserializer(serviceRef
									.getReferenceScheme());
					if (referenceDeserializer != null) {
						Object serviceReference = referenceDeserializer
								.unmarshall(childElement, process);
						serviceRef.setValue(serviceReference);
					}
				}
			}
		} else {
			serviceRef.setValue(data);
		}
		return serviceRef;
	}

	protected Query xml2Query(Query queryObject, Element queryElement) {
		if (queryObject == null) {
			queryObject = BPELFactory.eINSTANCE.createQuery();
		}

		queryObject.setElement(queryElement);

		// Set queryLanguage
		if (queryElement.hasAttribute("queryLanguage")) {
			String queryLanguage = queryElement
					.getAttribute("queryLanguage");
			queryObject.setQueryLanguage(queryLanguage);
		} else {
			queryObject.setQueryLanguage(null);
		}

		// Set query text
		// Get the condition text
		String data = getText(queryElement);
		if (data != null) {
			queryObject.setValue(data);
		} else {
			queryObject.setValue(null);
		}
		return queryObject;
	}

	/**
	 * Converts an XML import element to a BPEL Import object.
	 */
	protected Import xml2Import(Import imp, Element importElement) {
		if (!importElement.getLocalName().equals("import"))
			return null;

		if (imp == null) {
			imp = BPELFactory.eINSTANCE.createImport();
			imp.setElement(importElement);
		}

		// Save all the references to external namespaces
		saveNamespacePrefix(imp, importElement);

		// namespace
		if (importElement.hasAttribute("namespace")) {
			imp.setNamespace(importElement.getAttribute("namespace"));
		} else {
			imp.setNamespace(null);
		}

		// location
		if (importElement.hasAttribute("location")) {
			imp.setLocation(importElement.getAttribute("location"));
		} else {
			imp.setLocation(null);
		}

		// importType
		if (importElement.hasAttribute("importType")) {
			imp.setImportType(importElement.getAttribute("importType"));
		} else {
			imp.setImportType(null);
		}

		return imp;
	}

	/**
	 * Converts an XML invoke element to a BPEL Invoke object.
	 */
	protected Activity xml2Invoke(Activity invokeActivity, Element invokeElement) {
		Invoke invoke;
		if (invokeActivity instanceof Invoke) {
			invoke = (Invoke)invokeActivity;
		} else {
			invoke = BPELFactory.eINSTANCE.createInvoke();
			invoke.setElement(invokeElement);
		}

		// Set several parms
		setStandardAttributes(invokeElement, invoke);
		setOperationParms(invokeElement, invoke, null, BPELPackage.eINSTANCE
				.getInvoke_InputVariable(), BPELPackage.eINSTANCE
				.getInvoke_OutputVariable(), BPELPackage.eINSTANCE
				.getPartnerActivity_PartnerLink());

		// Set compensationHandler
		setCompensationHandler(invokeElement, invoke);

		// Set the fault handler (for catche-s and catchAll-s)
		FaultHandler faultHandler = xml2FaultHandler(invoke.getFaultHandler(), invokeElement);
		if (faultHandler != null
				&& (!faultHandler.getCatch().isEmpty() || faultHandler
						.getCatchAll() != null)) {
			// Only set this on the activity if there is at least one catch
			// clause, or a catchAll clause
			invoke.setFaultHandler(faultHandler);
		}

		// Set the ToPart
		for (Element e : getBPELChildElementsByLocalName(invokeElement,
				"toPart")) {
			invoke.getToPart().add(xml2ToPart(e));
		}
		// Set the FromPart
		for (Element e : getBPELChildElementsByLocalName(invokeElement,
				"fromPart")) {
			invoke.getFromPart().add(xml2FromPart(e));
		}
		return invoke;
	}

	/**
	 * Converts an XML reply element to a BPEL Reply object.
	 */
	protected Activity xml2Reply(Element replyElement) {
		Reply reply = BPELFactory.eINSTANCE.createReply();
		reply.setElement(replyElement);

		// Set several parms
		setStandardAttributes(replyElement, reply);
		setOperationParms(replyElement, reply, BPELPackage.eINSTANCE
				.getReply_Variable(), null, null, BPELPackage.eINSTANCE
				.getPartnerActivity_PartnerLink());

		if (replyElement.hasAttribute("faultName")) {
			QName qName = BPELUtils.createAttributeValue(replyElement,
					"faultName");
			reply.setFaultName(qName);
		}

		// Set the ToPart
		for (Element e : getBPELChildElementsByLocalName(replyElement, "toPart")) {
			reply.getToPart().add(xml2ToPart(e));
		}

		return reply;
	}

	/**
	 * Converts an XML receive element to a BPEL Receive object.
	 */
	protected Activity xml2Receive(Activity receiveActivity, Element receiveElement) {
		Receive receive;
		if (receiveActivity instanceof Receive) {
			receive = (Receive)receiveActivity;
		} else {
			receive = BPELFactory.eINSTANCE.createReceive();
			receive.setElement(receiveElement);
		}

		// Set several parms
		setStandardAttributes(receiveElement, receive);
		setOperationParms(receiveElement, receive, BPELPackage.eINSTANCE
				.getReceive_Variable(), null, null, BPELPackage.eINSTANCE
				.getPartnerActivity_PartnerLink());

		// Set createInstance
		if (receiveElement.hasAttribute("createInstance")) {
			receive.setCreateInstance(BPELUtils.xml2boolean(receiveElement.getAttribute("createInstance")));
		} else {
			receive.unsetCreateInstance();
		}

		// Set the FromPart
		for (Element e : getBPELChildElementsByLocalName(receiveElement,
				"fromPart")) {
			receive.getFromPart().add(xml2FromPart(e));
		}

		return receive;
	}

	/**
	 * Converts an XML forEach element to a BPEL ForEach object.
	 */
	protected Activity xml2ForEach(Activity forEachActivity, Element forEachElement) {		
		ForEach forEach;
		if (forEachActivity instanceof ForEach) {
			forEach = (ForEach)forEachActivity;
		} else {
			forEach = BPELFactory.eINSTANCE.createForEach();
			forEach.setElement(forEachElement);
		}

		// Set several parms
		setStandardAttributes(forEachElement, forEach);

		if (forEachElement.hasAttribute("parallel")) {
//			if (process != null) {
//				process.setSuppressJoinFailure(BPELUtils.xml2boolean(forEachElement.getAttribute("parallel"));
//			}
			forEach.setParallel(BPELUtils.xml2boolean(forEachElement.getAttribute("parallel")));
		} else {
			forEach.setParallel(false);
		}

		// Set counterName variable
		if (forEachElement.hasAttribute("counterName")) {

			String counterName = forEachElement.getAttribute("counterName");
			if (forEach.getCounterName() == null || !forEach.getCounterName().getName().equals(counterName)) {
				Variable variable = BPELFactory.eINSTANCE.createVariable();
				// TODO: How to facade this ?
				variable.setName(counterName);
				QName qName = new QName(XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001,
						"unsignedInt");
				XSDTypeDefinition type = new XSDTypeDefinitionProxy(getResource()
						.getURI(), qName);
				variable.setType(type);
				forEach.setCounterName(variable);
			}
		} else {
			forEach.setCounterName(null);
		}
		
		// Set startCounterValue element
		Element startCounterValueElement = getBPELChildElementByLocalName(forEachElement, "startCounterValue");
		if (startCounterValueElement != null) {
			forEach.setStartCounterValue(xml2Expression(forEach.getStartCounterValue(), startCounterValueElement));
		} else {
			forEach.setStartCounterValue(null);
		}

		// Set finalCounterValue element
		Element finalCounterValueElement = getBPELChildElementByLocalName(forEachElement, "finalCounterValue");
		if (finalCounterValueElement != null) {
			forEach.setFinalCounterValue(xml2Expression(forEach.getFinalCounterValue(), finalCounterValueElement));
		} else {
			forEach.setFinalCounterValue(null);
		}

		// Set completionCondition element
		Element completionConditionElement = getBPELChildElementByLocalName(forEachElement, "completionCondition");
		if (completionConditionElement != null) {
			forEach.setCompletionCondition(xml2CompletionCondition(forEach.getCompletionCondition(), completionConditionElement));
		} else {
			forEach.setCompletionCondition(null);
		}

		// Set activity
		Activity activity = getChildActivity(forEach, forEachElement);
		if (activity instanceof Scope) {
			forEach.setActivity(activity);
		} else {
			forEach.setActivity(null);
		}

		return forEach;
	}

	/**
	 * Converts an XML completionCondition element to a BPEL CompletionCondition
	 * object.
	 */
	protected CompletionCondition xml2CompletionCondition(CompletionCondition completionCondition, Element completionConditionElement) {
		if (completionCondition == null) {
			completionCondition = BPELFactory.eINSTANCE.createCompletionCondition();
			completionCondition.setElement(completionConditionElement);
		}

		// Set branches element
		Element branchesElement = getBPELChildElementByLocalName(completionConditionElement, "branches");
		if (branchesElement != null) {
			completionCondition.setBranches(xml2Branches(completionCondition.getBranches(), branchesElement));
		} else {
			completionCondition.setBranches(null);
		}

		return completionCondition;
	}

	/**
	 * Converts an XML branches element to a BPEL Branches object.
	 */
	protected Branches xml2Branches(Branches branches, Element branchesElement) {
		if (branches == null) {
			branches = BPELFactory.eINSTANCE.createBranches();
			branches.setElement(branchesElement);
		}

		xml2Expression(branches, branchesElement);

		if (branchesElement.hasAttribute("successfulBranchesOnly")) {
			branches.setCountCompletedBranchesOnly(BPELUtils.xml2boolean(branchesElement
					.getAttribute("successfulBranchesOnly")));
		} else {
			branches.unsetCountCompletedBranchesOnly();
		}
		return branches;
	}

	/**
	 * Converts an XML documentation element to a BPEL Documentation object.
	 */
	protected Documentation xml2Documentation(Documentation documentation, Element documentationElement) {
		if (documentation == null) {
			documentation = BPELFactory.eINSTANCE.createDocumentation();
		}
		((DocumentationImpl)documentation).setElement(documentationElement);

		if (documentationElement.hasAttribute("xml:lang")) {
			documentation
					.setLang(documentationElement.getAttribute("xml:lang"));
		} else {
			documentation.setLang(null);
		}
		if (documentationElement.hasAttribute("source")) {
			documentation
					.setSource(documentationElement.getAttribute("source"));
		} else {
			documentation.setSource(null);
		}
		
		String text = getText(documentationElement);
		if (text != null) {
			documentation.setValue(text);
		} else {
			documentation.setValue(null);
		}

		return documentation;
	}

	/**
	 * Converts an XML repeatUntil element to a BPEL RepeatUntil object.
	 */
	protected Activity xml2RepeatUntil(Activity repeatUntilActivity, Element repeatUntilElement) {
		RepeatUntil repeatUntil; 
		if (repeatUntilActivity instanceof RepeatUntil) {
			repeatUntil = (RepeatUntil)repeatUntilActivity;
		}else {
			repeatUntil = BPELFactory.eINSTANCE.createRepeatUntil();
			repeatUntil.setElement(repeatUntilElement);
		}		 

		// Set several parms
		setStandardAttributes(repeatUntilElement, repeatUntil);

		// Handle condition element
		Element conditionElement = getBPELChildElementByLocalName(
				repeatUntilElement, "condition");
		if (conditionElement != null) {
			repeatUntil.setCondition(xml2Condition(repeatUntil.getCondition(), conditionElement));
		} else {
			repeatUntil.setCondition(null);
		}

		repeatUntil.setActivity(getChildActivity(repeatUntil, repeatUntilElement));		
		
		return repeatUntil;
	}

	protected Correlations xml2Correlations(Element correlationsElement) {
		if (!correlationsElement.getLocalName().equals("correlations"))
			return null;

		Correlations correlations = BPELFactory.eINSTANCE.createCorrelations();
		correlations.setElement(correlationsElement);

		// Save all the references to external namespaces
		saveNamespacePrefix(correlations, correlationsElement);

		for (Element e : getBPELChildElementsByLocalName(correlationsElement,
				"correlation")) {
			correlations.getChildren().add(xml2Correlation(e));
		}

		// extensibility elements
		xml2ExtensibleElement(correlations, correlationsElement);

		return correlations;
	}

	/**
	 * Converts an XML correlation element to a BPEL Correlation object.
	 */
	protected Correlation xml2Correlation(Element correlationElement) {
		final Correlation correlation = BPELFactory.eINSTANCE
				.createCorrelation();
		correlation.setElement(correlationElement);

		// Save all the references to external namespaces
		saveNamespacePrefix(correlation, correlationElement);

		if (correlationElement == null)
			return correlation;

		// Set set
		if (correlationElement.hasAttribute("set")) {
			final String correlationSetName = correlationElement
					.getAttribute("set");
			CorrelationSet cSet = BPELUtils
					.getCorrelationSetForActivity(correlation,
							correlationSetName);
			if (cSet == null) {
				cSet = new CorrelationSetProxy(getResource().getURI(),
						correlationSetName);
			}
			correlation.setSet(cSet);
		}

		// Set initiation
		Attr initiation = correlationElement.getAttributeNode("initiate");
		if (initiation != null && initiation.getSpecified()) {
			if (initiation.getValue().equals("yes"))
				correlation.setInitiate("yes");
			else if (initiation.getValue().equals("no"))
				correlation.setInitiate("no");
			else if (initiation.getValue().equals("join"))
				correlation.setInitiate("join");
		}

		// Set pattern
		Attr pattern = correlationElement.getAttributeNode("pattern");

		if (pattern != null && pattern.getSpecified()) {
			if (pattern.getValue().equals("in"))
				correlation.setPattern(CorrelationPattern.IN_LITERAL);
			else if (pattern.getValue().equals("out"))
				correlation.setPattern(CorrelationPattern.OUT_LITERAL);
			else if (pattern.getValue().equals("out-in"))
				correlation.setPattern(CorrelationPattern.OUTIN_LITERAL);
		}

		xml2ExtensibleElement(correlation, correlationElement);

		return correlation;
	}

	protected Compensate xml2Compensate(Element compensateElement) {
		final Compensate compensate = BPELFactory.eINSTANCE.createCompensate();
		compensate.setElement(compensateElement);
		setStandardAttributes(compensateElement, compensate);
		return compensate;
	}

	protected CompensateScope xml2CompensateScope(Element compensateScopeElement) {

		final CompensateScope compensateScope = BPELFactory.eINSTANCE
				.createCompensateScope();
		compensateScope.setElement(compensateScopeElement);

		final String target = compensateScopeElement.getAttribute("target");

		if (target != null && target.length() > 0) {		
				compensateScope.setTarget(target);
		}

		setStandardAttributes(compensateScopeElement, compensateScope);

		return compensateScope;
	}

	/**
	 * Converts an XML extensible element to a BPEL extensible element
	 */

	protected void xml2ExtensibleElement(ExtensibleElement extensibleElement,
			Element element) {

		if (extensionRegistry == null) {
			return;
		}

		// Handle the documentation element first
		Element documentationElement = getBPELChildElementByLocalName(element,
				"documentation");
		if (documentationElement != null) {
			Documentation documentation = xml2Documentation(extensibleElement.getDocumentation(), documentationElement);
			extensibleElement.setDocumentation(documentation);
		} else {
			
		}

		// Get the child nodes, elements and attributes
		List<Node> nodes = new ArrayList<Node>();

		NodeList nodeList = element.getChildNodes();
		for (int i = 0, n = nodeList.getLength(); i < n; i++) {
			if (nodeList.item(i) instanceof Element) {
				final String namespaceURI = ((Element) nodeList.item(i))
						.getNamespaceURI();
				if (!(BPELConstants.isBPELNamespace(namespaceURI)))
					nodes.add(nodeList.item(i));
			}
		}

		NamedNodeMap nodeMap = element.getAttributes();
		for (int i = 0, n = nodeMap.getLength(); i < n; i++) {
			Attr attr = (Attr) nodeMap.item(i);
			if (attr.getNamespaceURI() != null
					&& !attr.getNamespaceURI().equals(
							XSDConstants.XMLNS_URI_2000)) {
				nodes.add(attr);
			}
		}

		for (Node node : nodes) {

			// TODO What is this check for? If we're actually checking for
			// the BPEL namespace, use BPELConstants instead.
			if (MessagepropertiesConstants.isMessagePropertiesNamespace(node
					.getNamespaceURI())) {
				continue;
			}

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				deserialize(extensibleElement, (Element) node);
			} else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				deserialize(extensibleElement, (Attr) node);
			}
		}
	}

	protected void deserialize(ExtensibleElement ee, Element elm) {

		QName qname = new QName(elm.getNamespaceURI(), elm.getLocalName());
		BPELExtensionDeserializer deserializer = null;
		try {
			deserializer = (BPELExtensionDeserializer) extensionRegistry
					.queryDeserializer(ExtensibleElement.class, qname);
		} catch (WSDLException e) {
			// we don't have one.
		}
		if (deserializer == null) {
			return;
		}
		// Deserialize the DOM element and add the new Extensibility element to
		// the parent
		// ExtensibleElement
		Map<String, String> nsMap = getAllNamespacesForElement(elm);
		try {
			ExtensibilityElement extensibilityElement = deserializer
					.unmarshall(ee.getClass(), qname, elm, process, nsMap,
							extensionRegistry, getResource().getURI());
			ee.addExtensibilityElement(extensibilityElement);
		} catch (WSDLException e) {
			throw new WrappedException(e);
		}
	}

	protected void deserialize(ExtensibleElement ee, Attr attr) {

		if (attr.getSpecified() == false) {
			return;
		}

		QName qname = new QName(attr.getNamespaceURI(),
				"extensibilityAttributes");
		BPELExtensionDeserializer deserializer = null;
		try {
			deserializer = (BPELExtensionDeserializer) extensionRegistry
					.queryDeserializer(ExtensibleElement.class, qname);
		} catch (WSDLException e) {
			// ignore
		}
		if (deserializer == null) {
			return;
		}

		// Create a temp element to host the extensibility attribute
		// 
		// This turns something that looks like this:
		// <bpws:X someNS:Y="Z"/>
		// into something that looks like this:
		// <someNS:extensibilityAttributes xmlns:someNS="http://the.namespace"
		// Y="Z"/>

		Element tempElement = attr.getOwnerDocument().createElementNS(
				attr.getNamespaceURI(),
				attr.getPrefix() + ":extensibilityAttributes");
		tempElement.setAttribute(BPELUtils.ATTR_XMLNS + ":" + attr.getPrefix(),
				attr.getNamespaceURI());
		tempElement.setAttribute(attr.getLocalName(), attr.getNodeValue());

		// Deserialize the temp DOM element and add the new Extensibility
		// element to the parent
		// ExtensibleElement
		Map<String, String> nsMap = getAllNamespacesForElement((Element) attr
				.getParentNode());
		try {
			ExtensibilityElement extensibilityElement = deserializer
					.unmarshall(ExtensibleElement.class, qname, tempElement,
							process, nsMap, extensionRegistry, getResource()
									.getURI());
			if (extensibilityElement != null) {
				ee.addExtensibilityElement(extensibilityElement);
			}
		} catch (WSDLException e) {
			throw new WrappedException(e);
		}
	}

	/**
	 * Returns true if the string is either null or contains just whitespace.
	 * 
	 * @param value
	 * @return true if empty or whitespace, false otherwise.
	 */

	static public boolean isEmptyOrWhitespace(String value) {
		if (value == null || value.length() == 0) {
			return true;
		}
		for (int i = 0, j = value.length(); i < j; i++) {
			if (!Character.isWhitespace(value.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the text of the given node. If the node is an element node, its
	 * children text value is returned. Otherwise, the node is assumed to be the
	 * first child node and the siblings sequence is scanned.
	 * 
	 * 
	 */

	String getText(Node node) {

		StringBuilder sb = new StringBuilder(128);

		if (node instanceof Element) {
			node = ((Element) node).getFirstChild();
		}

		boolean bCData = false;

		while (node != null) {
			switch (node.getNodeType()) {
			case Node.TEXT_NODE:
				if (bCData) {
					break;
				}
				Text text = (Text) node;
				sb.append(text.getData());
				break;
			case Node.CDATA_SECTION_NODE:
				if (bCData == false) {
					sb.setLength(0);
					bCData = true;
				}
				CDATASection cdata = (CDATASection) node;
				sb.append(cdata.getData());
				break;
			}
			node = node.getNextSibling();
		}
		String data = sb.toString();
		if (isEmptyOrWhitespace(data)) {
			return null;
		}
		return data;
	}

	/**
	 * @param eObject
	 * @param variableName
	 * @return the resolved variable
	 */
	public static Variable getVariable(EObject eObject, String variableName) {
		return VARIABLE_RESOLVER.getVariable(eObject, variableName);
	}

	/**
	 * @param activity
	 * @param linkName
	 * @return the resolved link
	 */
	public static Link getLink(Activity activity, String linkName) {
		return LINK_RESOLVER.getLink(activity, linkName);
	}
	
	private interface Creator {
		WSDLElement create(Element element);
	}
	
	private void syncLists(Element ifElement, List<Element> childElements, EList childrenList, Creator creator) {
		WSDLElement[] children = (WSDLElement[])childrenList.toArray(new WSDLElement[childrenList.size()]);
		int i, j, insertionIndex = 0; 
		for (i = 0, j = 0; i < children.length && j < childElements.size(); i++) {
			WSDLElement elseIf = children[i];
			if (elseIf.getElement() == null || elseIf.getElement().getParentNode() != ifElement) {
				childrenList.remove(insertionIndex);
			}
			Element element = childElements.get(j);
			while (elseIf.getElement() != element && j < childElements.size()) {
				childrenList.add(insertionIndex, creator.create(childElements.get(j)));
				j++;
				insertionIndex++;
			}
			if (elseIf.getElement() == element) {
				j++;
				insertionIndex++;
			}
		}
		for (int k = i; k < children.length; k++) {
			WSDLElement elseIf = children[k];
			if (elseIf.getElement() == null || elseIf.getElement().getParentNode() != ifElement) {
				childrenList.remove(elseIf);
			}
		}
		for (int k = j; k < childElements.size(); k++) {
			childrenList.add(insertionIndex, creator.create(childElements.get(k)));
		}
	}
	
	private void syncSequences(Element sequenceElement, EList<Activity> activitiesList) {
		NodeList sequenceElements = sequenceElement.getChildNodes();

		Element activityElement = null;		 
		Activity[] activities = new Activity[activitiesList.size()];
		activitiesList.toArray(activities);
		
		if (sequenceElements != null) {
			int i = 0, j = 0, insertionIndex = 0;
			for (; i < sequenceElements.getLength() && j < activities.length; j++) {
				while (sequenceElements.item(i).getNodeType() != Node.ELEMENT_NODE || ((Element) sequenceElements.item(i)).getLocalName()
						.equals("links")) {
					i++;
				}
				
				activityElement = (Element) sequenceElements.item(i);
				Activity activity = activities[j];
				if (activity.getElement() == null || activity.getElement().getParentNode() != sequenceElement){
					activitiesList.remove(insertionIndex);
					continue;
				}
				while (activityElement != activity.getElement() && i < sequenceElements.getLength()) {
					if (sequenceElements.item(i).getNodeType() != Node.ELEMENT_NODE || ((Element) sequenceElements.item(i)).getLocalName().equals("links")) {
						i++;
						continue;
					}
					activityElement = (Element) sequenceElements.item(i);
					Activity newActivity = xml2Activity(null, activityElement);
					if (newActivity != null) {
						activitiesList.add(insertionIndex, newActivity);
						insertionIndex++;
					}
					i++;
				}
				if (activityElement == activity.getElement()) {
					insertionIndex++;
					i++;
					continue;
				}
			}
			for (int k = j; k < activities.length; k++) {
				Activity activity = activities[k];
				if (activity.getElement() == null || activity.getElement().getParentNode() != sequenceElement){
					activitiesList.remove(activity);
				}
			}
			for (int k = i; k < sequenceElements.getLength(); k++) {
				if (sequenceElements.item(k).getNodeType() != Node.ELEMENT_NODE || ((Element) sequenceElements.item(i)).getLocalName().equals("links")) {
					continue;
				}
				activityElement = (Element) sequenceElements.item(k);
				Activity newActivity = xml2Activity(null, activityElement);
				if (newActivity != null) {
					activitiesList.add(newActivity);
				}
	
			}
		} else {
			activitiesList.clear();
		}
	}	

	private void createLink(final Target target, final String linkName) {
		Link link = getLink(target.getActivity(), linkName);
		if (link != null)
			target.setLink(link);
		else
			target.setLink(new LinkProxy(getResource().getURI(),
					linkName));
	}
	
	private void createLink(final Source source, final String linkName) {
		Link link = getLink(source.getActivity(), linkName);
		if (link != null)
			source.setLink(link);
		else
			source.setLink(new LinkProxy(getResource().getURI(),
					linkName));
	}

	/**
	 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
	 */
	@SuppressWarnings("boxing")
	public void error(SAXParseException exception) {

		String message = java.text.MessageFormat.format(
				"Error in {0} [{2}:{3}] {4}", exception.getPublicId(),
				exception.getSystemId(), exception.getLineNumber(), exception
						.getColumnNumber(), exception.getLocalizedMessage());
		BPELPlugin.logMessage(message, exception, IStatus.ERROR);
	}

	/**
	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	@SuppressWarnings("boxing")
	public void fatalError(SAXParseException exception) {
		String message = java.text.MessageFormat.format(
				"Fatal Error in {0} [{2}:{3}] {4}", exception.getPublicId(),
				exception.getSystemId(), exception.getLineNumber(), exception
						.getColumnNumber(), exception.getLocalizedMessage());
		BPELPlugin.logMessage(message, exception, IStatus.ERROR);
	}

	/**
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	@SuppressWarnings("boxing")
	public void warning(SAXParseException exception) {
		String message = java.text.MessageFormat.format(
				"Warning in {0} [{2}:{3}] {4}", exception.getPublicId(),
				exception.getSystemId(), exception.getLineNumber(), exception
						.getColumnNumber(), exception.getLocalizedMessage());
		BPELPlugin.logMessage(message, exception, IStatus.WARNING);

	}
}