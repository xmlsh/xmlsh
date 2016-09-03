/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.internal.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Namespaces;
import org.xmlsh.core.Options;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.NameValueMap;
import org.xmlsh.util.Util;
import org.xmlsh.xpath.EvalDefinition;
import net.sf.saxon.om.CodedName;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.MutableNodeInfo;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.linked.DocumentImpl;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Type;

public class xedit extends XCommand {

  enum Operation {
    DELETE, ADD, REPLACE, RENAME
  };

  enum Match {
    XPATH, XQUERY, XSLT
  };

  enum Value {
    XPATH, XQUERY, VALUE
  };

  private DocumentBuilder mBuilder;
  private XPathCompiler mCompiler;
  private Processor mProcessor;

  private void setupBuilders() throws IOException {
    /*
     * mProcessor = new Processor(false);
     * mProcessor.setConfigurationProperty(FeatureKeys.TREE_MODEL,
     * net.sf.saxon.event.Builder.LINKED_TREE);
     */
    mProcessor = Shell.getProcessor();

    mCompiler = mProcessor.newXPathCompiler();
    mBuilder = mProcessor.newDocumentBuilder();
    mBuilder.setTreeModel(TreeModel.LINKED_TREE);

    Namespaces ns = getEnv().getNamespaces();
    if(ns != null) {
      for(String prefix : ns.keySet()) {
        String uri = ns.get(prefix);
        mCompiler.declareNamespace(prefix, uri);

      }

    }

  }

  @Override
  public int run(List<XValue> args) throws Exception {

    Operation opt_op = null;
    // What to operate on
    Axis opt_axis = Axis.SELF; // Add/Replace/delete axis

    // matchng
    Match opt_match_type = null; // xpath / xquery / match|patterh
    String opt_match = null;
    Value opt_value_type = null;  // value // xpath // xquery
    XValue opt_value = null;

    // Options opts = new Options(
    // "i=input:,e=xpath:,n,v,r=replace:,a=add:,d=delete,m=matches:,rx=replacex:,ren=rename:"
    // ,

    // Backwards compatible for now
    Options opts = new Options(
        "axis:,i=input:,n,v,mv=match-value:,mx=match-xpath:,mq=match-xquery:,"
            + "o=op:,r=replace:,a=add:,d=delete,vx=value-xpath:,vq=value-xquery:,v,rx=replacex:,ren-rename:,e=xpath:,m=matches:",
        SerializeOpts.getOptionDefs());
    opts.parse(args);

    setupBuilders();

    XdmNode context = null;

    // boolean bReadStdin = false ;
    SerializeOpts serializeOpts = getSerializeOpts(opts);
    if(!opts.hasOpt("n")) { // Has XML data input
      OptionValue ov = opts.getOpt("i");

      // If -i argument is an XML expression take the first node as the context
      if(ov != null && ov.getValue().isXdmItem()) {
        XdmItem item = ov.getValue().asXdmItem();
        if(item instanceof XdmNode)
          // context = (XdmNode) item ; //
          // builder.build(((XdmNode)item).asSource());
          // context = (XdmNode) ov.getValue().toXdmValue();
          context = importNode((XdmNode) item);

      }
      if(context == null) {
        InputPort insrc = null;
        if(ov != null && !ov.getValue().toString().equals("-"))
          insrc = getInput(ov.getValue());
        else {
          insrc = getStdin();
        }
        context = build(insrc.asSource(serializeOpts));
      }
    }

    List<XValue> xvargs = opts.getRemainingArgs();

    if(opts.hasOpt("v")) {
      // Read pairs from args to set
      for(int i = 0; i < xvargs.size() / 2; i++) {
        String name = xvargs.get(i * 2).toString();
        mCompiler.declareVariable(new net.sf.saxon.s9api.QName(name));
      }
    }

    if(opts.hasOpt("op"))
      opt_op = Operation.valueOf(opts.getOptStringRequired("op").toUpperCase());
    else if(opts.hasOpt("replace"))
      opt_op = Operation.REPLACE;
    else if(opts.hasOpt("add"))
      opt_op = Operation.ADD;
    else if(opts.hasOpt("delete"))
      opt_op = Operation.DELETE;
    else if(opts.hasOpt("rename"))
      opt_op = Operation.RENAME;

    if(opts.hasOpt("replace")) {
      opt_value = opts.getOptValue("replace");
      opt_value_type = Value.VALUE;
    }

    if(opts.hasOpt("rx")) {
      opt_value = opts.getOptValue("rx");
      opt_value_type = Value.XPATH;
    }
    if(opts.hasOpt("rq")) {
      opt_value = opts.getOptValue("rq");
      opt_value_type = Value.XQUERY;
    }
    if(opts.hasOpt("axis"))
      opt_axis = Axis.valueOf(opts.getOptStringRequired("axis").toUpperCase());

    if(opts.hasOpt("mx")) {
      opt_match = opts.getOptStringRequired("mx");
      opt_match_type = Match.XPATH;
    }
    if(opts.hasOpt("e")) {
      opt_match = opts.getOptStringRequired("e");
      opt_match_type = Match.XPATH;
    }

    if(opts.hasOpt("mq")) {
      opt_match = opts.getOptStringRequired("mq");
      opt_match_type = Match.XQUERY;
    }
    if(opts.hasOpt("mv")) {
      opt_match = opts.getOptStringRequired("mv");
      opt_match_type = Match.XSLT;
    }
    if(opts.hasOpt("m")) { // compat
      opt_match = opts.getOptStringRequired("m");
      opt_match_type = Match.XSLT;
    }

    if(opts.hasOpt("vx")) {
      opt_value = opts.getOptValue("vx");
      opt_value_type = Value.XPATH;
    }

    if(opts.hasOpt("vq")) {
      opt_value = opts.getOptValue("vq");
      opt_value_type = Value.XQUERY;
    }
    if(opts.hasOpt("value")) {
      opt_value = opts.getOptValue("value");
      opt_value_type = Value.VALUE;
    }

    if(opts.hasOpt("axis"))
      opt_axis = Axis.valueOf(opts.getOptStringRequired("axis"));

    if(opt_match_type == null || opt_match == null)
      throw new InvalidArgumentException(
          "option xpath , xquery or matches must be specified");

    if(opt_op == null)
      throw new InvalidArgumentException("option opperation required");

    if(opt_op != Operation.DELETE && opt_value == null)
      throw new InvalidArgumentException("option value required");

    XPathSelector eval_matchx = null;
    XQueryEvaluator eval_matchq = null;

    switch(opt_match_type){
    case XPATH:
      eval_matchx = parseXpath(opt_match);
      break;
    case XSLT:
      eval_matchx = parseXslt(opt_match);
      break;
    case XQUERY:
      eval_matchq = parseXQuery(opt_match);
      break;
    }

    if(opts.hasOpt("v")) {
      // Read pairs from args to set
      for(int i = 0; i < xvargs.size() / 2; i++) {
        String name = xvargs.get(i * 2).toString();
        XValue value = xvargs.get(i * 2 + 1);
        if(eval_matchx != null)
          eval_matchx.setVariable(new net.sf.saxon.s9api.QName(name),
              value.toXdmValue());
      }
    }

    XPathSelector valuex = null;
    XQueryEvaluator valueq = null;

    switch(opt_value_type){
    case XPATH:
      valuex = parseXpath(opt_value.toString());
      break;
    case XQUERY:
      valueq = parseXQuery(opt_value.toString());
      break;

    case VALUE:

      break;
    }

    Iterable<XdmItem> results = getResults(eval_matchx, context,
        opt_match_type == Match.XSLT);
    for(XdmItem item : results) {
      Object obj = item.getUnderlyingValue();
      if(obj instanceof MutableNodeInfo) {
        MutableNodeInfo node = (MutableNodeInfo) obj;
        XValue xv = null;
        switch(opt_value_type){
        case XPATH:
          xv = eval_xpath(item, valuex);
          break;
        case XQUERY:
          xv = eval_xquery(item, valueq);
          break;

        default:
          xv = opt_value;
        }

        switch(opt_op){
        case REPLACE:
          replace(node, xv, opt_axis);
          break;
        case ADD:
          add(node, xv, opt_axis);
          break;
        case DELETE:
          delete(node, opt_axis);
          break;
        case RENAME:
          rename(node, xv);
          break;
        }

      }
    }

    OutputPort stdout = getStdout();
    Util.writeXdmValue(context, stdout.asDestination(serializeOpts));
    stdout.writeSequenceTerminator(serializeOpts);

    return 0;

  }

  private XQueryEvaluator parseXQuery(String string)
      throws SaxonApiException, IOException {
    Processor processor = Shell.getProcessor();
    XQueryCompiler compiler = processor.newXQueryCompiler();

    // Declare the extension function namespace
    // This can be overridden by user declarations
    compiler.declareNamespace("xmlsh", EvalDefinition.kXMLSH_EXT_NAMESPACE);
    NameValueMap<String> ns = getEnv().getNamespaces();
    if(ns != null) {
      for(String prefix : ns.keySet()) {
        String uri = ns.get(prefix);
        compiler.declareNamespace(prefix, uri);

      }
    }

    XQueryExecutable xq = compiler.compile(string);
    return xq.load();

  }

  private XPathSelector parseXpath(String match) throws SaxonApiException {
    XPathExecutable expr;
    expr = mCompiler.compile(match);
    XPathSelector eval = expr.load();
    return eval;
  }

  private XPathSelector parseXslt(String match) throws SaxonApiException {
    XPathExecutable expr;
    expr = mCompiler.compilePattern(match);
    XPathSelector eval = expr.load();
    return eval;
  }

  private Iterable<XdmItem> getResults(XPathSelector eval, XdmNode root,
      boolean opt_matches) throws SaxonApiException {

    if(!opt_matches) {
      if(root != null)
        eval.setContextItem(root);
      return eval;
    }
    ArrayList<XdmItem> results = new ArrayList<XdmItem>();
    if(root == null)
      return results;

    XdmSequenceIterator iter = root.axisIterator(Axis.DESCENDANT_OR_SELF);
    while(iter.hasNext()) {
      XdmItem item = iter.next();
      eval.setContextItem(item);
      if(eval.effectiveBooleanValue())
        results.add(item);
      if(item instanceof XdmNode) {
        XdmSequenceIterator aiter = ((XdmNode) item)
            .axisIterator(Axis.ATTRIBUTE);
        while(aiter.hasNext()) {
          XdmItem item2 = aiter.next();
          eval.setContextItem(item2);
          if(eval.effectiveBooleanValue())
            results.add(item2);
        }

      }
    }
    return results;
  }

  private void delete(MutableNodeInfo node, Axis opt_axis) {
    AxisIterator iter = node.iterateAxis(opt_axis.getAxisNumber());
    while(true) {
      NodeInfo n = iter.next();
      ((MutableNodeInfo) n).delete();
    }

  }

  private void add(MutableNodeInfo node, XValue add, Axis opt_axis)
      throws IndexOutOfBoundsException,
      SaxonApiUncheckedException, SaxonApiException, InvalidArgumentException {

    for(XdmItem item : add.toXdmValue()) {
      if(item.isAtomicValue())
        node.replaceStringValue(node.getStringValue() + item.toString());
      else {
        XdmNode inode = (XdmNode) item;
        if(isAttribute(inode)) {
          NodeInfo anode = inode.getUnderlyingNode();
          addAttribute(node, anode.getPrefix(), anode.getURI(),
              anode.getLocalPart(), anode.getStringValue());
        }
        else {
          node.insertChildren(new NodeInfo[] { getNodeInfo(inode) }, false,
              true);
        }
      }

    }
  }

  private boolean isAttribute(XdmNode inode) {
    return inode.getNodeKind() == XdmNodeKind.ATTRIBUTE;
  }

  private boolean isAttribute(NodeInfo node) {
    return node.getNodeKind() == Type.ATTRIBUTE;
  }

  private void addAttribute(MutableNodeInfo node, String prefix, String uri,
      String local, String value) {

    NamePool pool = node.getNamePool();
    int nameCode = pool.allocate(prefix, uri, local);

    CodedName name = new CodedName(nameCode, pool);
    node.addAttribute(name, BuiltInAtomicType.UNTYPED_ATOMIC, value, 0);
    if(!Util.isEmpty(prefix)) {

      node.addNamespace(name.getNamespaceBinding(), false);
    }
  }

  private void replace(MutableNodeInfo node, XValue replace, Axis opt_axis)
      throws IndexOutOfBoundsException,
      SaxonApiUncheckedException, SaxonApiException, InvalidArgumentException {

    switch(node.getNodeKind()){
    // String children types
    case Type.ATTRIBUTE:
    case Type.COMMENT:
    case Type.PROCESSING_INSTRUCTION:
    case Type.TEXT:
    case Type.WHITESPACE_TEXT:
    case Type.ELEMENT:
    case Type.DOCUMENT:
      node.replace(getNodeInfoList(node, replace.toXdmValue()), false);
      break;
    default:
      throw new InvalidArgumentException(
          "Unexpected node type: " + node.getNodeKind());
    }

    /*
     * } else {
     * switch( node.getNodeKind() ) {
     * case Type.ATTRIBUTE:
     * // Attributes can only be replaced by attributes
     * if( replace.isAtomic() )
     * node.replaceStringValue( replace.toString());
     * else
     * if( ! (replace.isXdmNode() && replace.asXdmNode().getNodeKind() ==
     * XdmNodeKind.ATTRIBUTE ))
     * throw new InvalidArgumentException("Unexpected replacement node type: " +
     * replace.asXdmNode().getNodeKind() );
     * XdmNode xnode = (XdmNode) replace.asXdmNode();
     * node.replace( new NodeInfo[] { getNodeInfo( xnode ) } , true );
     * break ;
     * 
     * 
     * case Type.COMMENT :
     * case Type.PROCESSING_INSTRUCTION:
     * case Type.TEXT:
     * case Type.WHITESPACE_TEXT:
     * 
     * if( replace.isAtomic() )
     * node.replaceStringValue(getName());
     * else
     * if( replace.isXdmNode() )
     * node.replace( new NodeInfo[] { getNodeInfo( replace.asXdmNode() ) } ,
     * true );
     * else
     * node.replaceStringValue( replace.toString() );
     * break ;
     * 
     * case Type.ELEMENT :
     * case Type.DOCUMENT :
     * if( replace.isXdmNode() ) {
     * if( replace.asXdmNode().getNodeKind() != XdmNodeKind.ATTRIBUTE )
     * node.replace( new NodeInfo[] { getNodeInfo( replace.asXdmNode() ) } ,
     * true );
     * else
     * node.replace( new NodeInfo[] {createTextNode( node , replace.toString()
     * )} , true );
     * }
     * else
     * break ;
     * default :
     * throw new InvalidArgumentException("Unexpected node type: " +
     * node.getNodeKind() );
     * }
     * }
     */

  }

  private void rename(MutableNodeInfo node, XValue xv) {

    QName qn = xv.asQName(getShell());

    NamePool pool = node.getNamePool();
    int newNameCode = pool.allocate(qn.getPrefix(), qn.getNamespaceURI(),
        qn.getLocalPart());

    CodedName name = new CodedName(newNameCode, pool);
    node.rename(name);

  }

  private XValue eval_xpath(XdmItem item, XPathSelector valuex)
      throws SaxonApiException {

    valuex.setContextItem(item);

    // Convert to string and turn into an XdmItem
    XValue xv = XValue.newXValue(valuex.evaluate());
    return xv;
  }

  private XValue eval_xquery(XdmItem item, XQueryEvaluator valueq)
      throws XPathException, SaxonApiException {

    valueq.setContextItem(item);

    // Convert to string and turn into an XdmItem
    XValue xv = XValue.newXValue(valueq.evaluate());
    return xv;

  }

  private NodeInfo createTextNode(MutableNodeInfo parent, String replace) {
    /*
     * net.sf.saxon.om.Orphan textNode = new
     * net.sf.saxon.om.Orphan(mProcessor.getUnderlyingConfiguration());
     * textNode.setNodeKind( Type.TEXT );
     * textNode.setStringValue( replace );
     */

    /*
     * net.sf.saxon.tree.TextImpl textNode = new net.sf.saxon.tree.TextImpl(
     * null , replace);
     */

    /*
     * try {
     * Class<?> cls = Class.forName("net.sf.saxon.tree.TextImpl");
     * Class<?> parentClass = Class.forName("net.sf.saxon.tree.ParentNodeImpl");
     * Constructor<?> cons = cls.getConstructor(parentClass , String.class );
     * NodeInfo text = (NodeInfo) cons.newInstance(null , replace );
     * return text ;
     * 
     * 
     * 
     * } catch( Exception e )
     * {
     * this.printErr("Exception loading textImpl", e);
     * return null;
     * }
     */
    // return SaxonUtil.createTextNode(replace);

    /*
     * TOTAL HACK BECAUSE WE CANT CREATE A TEXT NODE !!!!
     */
    // Make the children a text node
    parent.replaceStringValue(replace);

    Item item = parent.iterateAxis(net.sf.saxon.om.AxisInfo.CHILD).next();
    return (NodeInfo) item;

  }

  /*
   * Import the node using the builder into this object model
   */
  private XdmNode importNode(XdmNode node) throws SaxonApiException {
    Source src = node.asSource();
    return build(src);
  }

  private NodeInfo[] getNodeInfoList(MutableNodeInfo parent, XdmValue v)
      throws IndexOutOfBoundsException,
      SaxonApiUncheckedException, SaxonApiException {

    NodeInfo[] nl = new NodeInfo[v.size()];
    int i = 0;
    for(XdmItem item : v) {
      if(item.isAtomicValue())
        nl[i++] = createTextNode(parent, item.toString());
      else
        nl[i++] = getNodeInfo((XdmNode) item);

    }
    return nl;

  }

  private NodeInfo getNodeInfo(XdmNode node)
      throws IndexOutOfBoundsException, SaxonApiUncheckedException,
      SaxonApiException {

    XdmNode xnode = importNode(node);

    return ((DocumentImpl) xnode.getUnderlyingNode().getDocumentRoot())
        .getDocumentElement();
  }

  /*
   * Creates/Builds a Tree (LINKED_TREE) type node from any source
   */

  private XdmNode build(Source src) throws SaxonApiException {
    // @TODO: To get over a bug in Saxon's build() have to use the root element
    // instead of a document node to force building of a linked tree model
    // Otherwise the source is just returned unchnaged

    if(src instanceof DocumentInfo)
      src = (((DocumentInfo) src).iterateAxis(net.sf.saxon.om.AxisInfo.CHILD)
          .next());
    return mBuilder.build(src);

  }

  /*
   * Find a matching attribute to a passed in one
   * Compare URI and local part
   */

  private NodeInfo findAttribute(NodeInfo node, NodeInfo attr) {

    // Write attributes
    AxisIterator iter = node.iterateAxis(net.sf.saxon.om.AxisInfo.ATTRIBUTE);
    Item item;
    while((item = iter.next()) != null) {
      NodeInfo a = (NodeInfo) item;
      if(a.getURI().equals(attr.getURI())
          && a.getLocalPart().equals(attr.getLocalPart()))

        return a;

    }
    return null;
  }

}

//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
