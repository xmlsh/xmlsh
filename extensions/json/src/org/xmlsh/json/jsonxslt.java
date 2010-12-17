/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.json;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javanet.staxutils.XMLStreamUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.BuildingStreamWriter;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.xml.sax.ContentHandler;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.OmittingXMLStreamWriter;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.VariableInputPort;
import org.xmlsh.core.VariableOutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.schema.Schema;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

import com.sun.org.apache.xerces.internal.xs.XSConstants;



public class jsonxslt  extends XCommand{

		
		private static final String CONTEXT_TYPE = "type";
		private static final String CONTEXT_ATTRIBUTE = "attribute";
		private static final String CONTEXT_ELEMENT = "element";
		private static final String CONTEXT_DOCUMENT = "document";

		
		private static XdmNode 	parse( XSAnnotation xanno , SerializeOpts opts ) throws XPathException, SaxonApiException, CoreException
		{
			
			
			XVariable var = new XVariable("anon", new XValue());
			OutputPort out = new VariableOutputPort( var );
			
			ContentHandler handler  = out.asContentHandler(opts );
			xanno.writeAnnotation(handler, XSAnnotation.SAX_CONTENTHANDLER);
			out.flush();
			out.close();
			
			
			XdmValue xv = var.getValue().xpath("//json").asXdmValue();
			
			/*
			 * Reserialize with a namespacer reduicer
			 */
			
			var = new XVariable( "anon" , new XValue());

			out = new VariableOutputPort( var );
			Util.writeXdmValue(xv ,out.asDestination(opts ));
			out.flush();
			out.close();
			return var.getValue().asXdmNode();
			
		}
		
		
		
		private class Annotation extends XValue
		{
			
			
			
			public Annotation( XSAnnotation xanno ) throws XPathException, SaxonApiException, CoreException{
				
				
			
				super( parse( xanno , mSerializeOpts ) );
				
				
				
			}
			
		}
		
		@SuppressWarnings("serial")
		private static class AnnotationList extends ArrayList<Annotation>
		{


			public AnnotationList()
			{
				super();
			}
			public AnnotationList(int length) {
				super(length);
			}

			/* (non-Javadoc)
			 * @see java.util.ArrayList#add(java.lang.Object)
			 */
			@Override
			public boolean add(Annotation e) {
				if( e == null || e.isNull() )
					throw new RuntimeException("Invalid Null Annotation");
				
				
				return super.add(e);
			}
			
			
			
			
		}
		
		
		@SuppressWarnings("serial")
		private	 class	Path extends ArrayList<QName>
		{

			public Path(QName name) {
				super();
				add(name);
			}

			public Path(Path parent, QName name) {
				super( parent.size() + 1 );
				this.addAll(parent);
				this.add(name);
				
				
			}

			void serialize( XMLStreamWriter sw ) throws XMLStreamException{
				sw.writeStartElement("path");
				for( QName q : this ){
					writeQName( sw , "name" , q );
							
				}
				sw.writeEndElement();
			}
			
		};
		
		
		
		private class	AnnotationEntry 
		{
			QName 				mName;			// required
			int					mPriority;		// required 
			Path				mPath;			// may be null
			String 				mContext;		// required
			QName				mType; 			// may be null 
			AnnotationList		mAnnotation;	// required
			public AnnotationEntry(String context , int priority , QName name , Path path , QName type, AnnotationList annotation) {
				super();
				mName = name ;
				mPriority = priority ;
				mPath = path ;
				
				mContext = context ;
				mType = type;
				mAnnotation = annotation;
			}
			
			
			public void  serialize( XMLStreamWriter sw ) throws SaxonApiException, XMLStreamException, XPathException, CoreException
			{

				sw.writeStartElement(mContext);
				sw.writeAttribute("priority" , String.valueOf(mPriority));
				
				
				if( ! mContext.equals(CONTEXT_DOCUMENT)){
					writeQName( sw , "name" , mName );
					
					if( mType != null )
						writeQName( sw , "type" , mType );
				}
				if( mPath != null ){
					mPath.serialize(sw);
					
				}

				if( mAnnotation != null ){
						
					for( Annotation a : mAnnotation ){


						XVariable var = new XVariable("anon", a );
						VariableInputPort inp = new VariableInputPort( var );
						XMLStreamReader sr = inp.asXMLStreamReader(mSerializeOpts);
						
						XMLStreamUtils.copy( sr , new OmittingXMLStreamWriter(sw) );
						sr.close();
						
						
						
						
					}
				}
				
				sw.writeEndElement();
				
			}
			
			
		};
		
		@SuppressWarnings("serial")
		private class AnnotationEntryList extends  ArrayList<AnnotationEntry>
		{

			
			
			public void serialize(XMLStreamWriter sw) throws SaxonApiException, XMLStreamException, XPathException, CoreException
			{
				sw.writeStartDocument();
				sw.writeStartElement("annotations");
				sw.flush();
				
				for( AnnotationEntry a : this ){
					a.serialize(sw);
					
				}
				sw.writeEndElement();
				sw.writeEndDocument();
			}
			
			
		}
		
		
		
		private AnnotationEntryList mAnnotations = new AnnotationEntryList();

	
		private SerializeOpts 	  mSerializeOpts ;
	
		
		public int run(List<XValue> args) throws Exception {
			Options opts = new Options("xsd:,o=output:,v,n",SerializeOpts.getOptionDefs());
			opts.parse(args);

			args = opts.getRemainingArgs();

			mSerializeOpts = getSerializeOpts(opts);

			String xsd = opts.getOptStringRequired("xsd");
			String output = opts.getOptString("o", ".");
			
			
			Shell shell = getShell();
			Schema schema = new Schema(   shell.getURI( xsd ).toString()  );
			
			
			
			AnnotationList globalAnnos = getGlobalAnnotations(schema);
			
			
			getTypeAnnotations(schema,globalAnnos );
			getElementAnnotations(schema, null,globalAnnos);
						
			File	outputDir = shell.getExplicitFile(output,false);
			if( ! outputDir.exists() )
				outputDir.mkdirs();

			XEnvironment env = shell.getEnv();
			
			
			
			
		
			if( opts.hasOpt("v")){
				XMLStreamWriter sw = this.getStdout().asXMLStreamWriter(mSerializeOpts);
				
				serialize( sw );
				sw.close();
				
				
			}
	
			if( ! opts.hasOpt("n")){
				OutputPort	xml_port   = env.getOutput( shell.getExplicitFile( outputDir, "toxml.xsl" , false ),false);
				OutputPort 	json_port  = env.getOutput( shell.getExplicitFile( outputDir, "tojson.xsl" , false ), false);
				
		
				BuildingStreamWriter b = Shell.getProcessor().newDocumentBuilder().newBuildingStreamWriter();
				
				serialize( b );
				

				
				createXSLT( b.getDocumentNode() , "create_xml.xquery" , xml_port );
				createXSLT( b.getDocumentNode() , "create_json.xquery" , json_port );
				
				
				
				xml_port.close();
				
				
				json_port.close();
			}
			
			
			return 0;
			
			
			
			
	}

		private void serialize(XMLStreamWriter sw) throws XMLStreamException, SaxonApiException, XPathException, CoreException 
		{
		
			mAnnotations.serialize(sw);
			
			
			
		}

		private void createXSLT(XdmItem annos , String script , OutputPort xmlPort) throws SaxonApiException, IOException, CoreException, URISyntaxException {
		
			
			
			XQueryExecutable exe = getXQuery(script);
			XQueryEvaluator eval = exe.load();
			
			// pass nodes as a sequence of items 
			eval.setContextItem( annos.itemAt(0) );
			eval.setDestination( xmlPort.asDestination(mSerializeOpts));
			
			eval.run();
			
			
			
			xmlPort.release();
			
			
			
		}

		private AnnotationList getGlobalAnnotations(Schema schema) throws XPathException,
				XMLStreamException, CoreException, SaxonApiException {
			XSObjectList annotations = schema.getAnnotations();
			
			AnnotationList value = getAnnotations( annotations );
			if( value != null )
				mAnnotations.add( new AnnotationEntry( CONTEXT_DOCUMENT , 0 , null , null , null , value));
			return value ;
		
		}



		private void getAnnotations(XSParticle particle, Path parent,  AnnotationList parentAnnos ) throws XPathException,
				XMLStreamException, CoreException, SaxonApiException {
			if( particle == null )
				return ;
			XSTerm term = particle.getTerm() ;
			if( term == null )
				return ;
			
			switch( term.getType() ){
			case	XSConstants.ELEMENT_DECLARATION :
			{
				XSElementDeclaration child = (XSElementDeclaration) term ;
				getAnnotations( child , parent , parentAnnos );
				break ;
			}
			
			
			case	XSConstants.MODEL_GROUP : ;
			{
				
				XSModelGroup group = (XSModelGroup) term ;
				XSObjectList particles = group.getParticles();
				for( int i = 0 ; i < particles.getLength() ; i++ ){
					XSParticle p = (XSParticle) particles.item(i);
					getAnnotations( p , parent, parentAnnos  );
				}
				
				break;
				
				
			}
			
			
			
			case	XSConstants.WILDCARD :
				break ;
			}
		
		}

		private Path getPath(Path parent, XSAttributeDeclaration child) {
			if( parent == null )
				return new Path( getName(child) ) ;
			else
				return new Path(parent , getName( child ) ) ;
		}
		
		
		private Path getPath(Path parent, QName child ) {
			if( parent == null )
				return new Path( child );
			else
				return new Path( parent , child ) ;
		}
		
		

		private void getElementAnnotations(Schema schema, Path parent, AnnotationList globalAnnos ) throws XPathException,
				XMLStreamException, CoreException, SaxonApiException {
			
			
			XSModel model = schema.getModel();
			XSNamedMap types  = model.getComponents(XSConstants.ELEMENT_DECLARATION);
			for( int i = 0 ; i < types.getLength() ; i++ ){
				XSObject obj = types.item(i);

				if( obj instanceof XSElementDeclaration )
					getAnnotations((XSElementDeclaration)obj, parent, globalAnnos );
				
			}
		}

		private void getAnnotations(XSElementDeclaration obj, Path parent ,  AnnotationList parentAnnos ) throws XPathException,
				XMLStreamException, CoreException, SaxonApiException {
			{

				
				 AnnotationList annotation = getAnnotations( obj.getAnnotations() );
				
				if( annotation == null )
					annotation = parentAnnos;
				
				XSTypeDefinition type = obj.getTypeDefinition();	
				
				QName elemName = getName(obj);
				
				
				if( annotation != null )
					mAnnotations.add( new AnnotationEntry( CONTEXT_ELEMENT , parent == null ? 0 : parent.size() ,  elemName,  parent ,  getName(type) , annotation  ));
				
				
				if( type instanceof XSComplexTypeDefinition ){
					XSComplexTypeDefinition ctype = (XSComplexTypeDefinition) type ;
					XSObjectList attrList = ctype.getAttributeUses();
					Path elemPath = getPath(parent,elemName);

					/*
					 * Recurse to attributes
					 */
					for( int n = 0 ; n < attrList.getLength() ; n++ ){
						XSObject attrobj = attrList.item(n);
						if( attrobj instanceof XSAttributeUse  ){
							
							XSAttributeUse attrUse = (XSAttributeUse) attrobj ;
							
							XSAttributeDeclaration attrDecl = attrUse.getAttrDeclaration();
							
							
							 AnnotationList useAnno  = getAnnotations( attrUse.getAnnotations() );
							
							
							 type = attrDecl.getTypeDefinition() ;

							
							 AnnotationList declAnno = getAnnotations( attrDecl.getAnnotations() );
							
							 /*
							  * Merge the annotations from use and type definitions 	
							  */
							 
							 AnnotationList 	a = null ;
							 if( useAnno != null || declAnno != null ){
								 a = new AnnotationList();
								 
								 if( useAnno != null )
									 a.addAll(useAnno);
								 if( declAnno != null )
									 a.addAll( declAnno );
									 
							 } else
								 a = annotation ;
									
							 

							mAnnotations.add( new AnnotationEntry( CONTEXT_ATTRIBUTE  , elemPath == null ? 0 : elemPath.size() , getName(attrDecl) , elemPath , getName(type) , a   ));
					
							
						}
						
					}
					

					/*
					 * Recuse to local elements
					 */
					
					XSParticle particle = ctype.getParticle();
					getAnnotations( particle , elemPath , annotation );
				}
			}
		}



		private void getTypeAnnotations(Schema schema,  AnnotationList parentAnnos) throws XPathException,	XMLStreamException, CoreException, SaxonApiException {
			XSModel model = schema.getModel();
			XSNamedMap types  = model.getComponents(XSConstants.TYPE_DEFINITION);
			for( int i = 0 ; i < types.getLength() ; i++ ){
				XSObject obj = types.item(i);

				if( obj instanceof XSTypeDefinition ){
					AnnotationList value = null ;

					XSTypeDefinition type = (XSTypeDefinition) obj ;
					if( type instanceof XSComplexTypeDefinition )
						value = getAnnotations( ((XSComplexTypeDefinition)type).getAnnotations() );
					else
						if( type instanceof XSSimpleTypeDefinition )
							value = getAnnotations( ((XSSimpleTypeDefinition)type).getAnnotations() );

					if( value == null )
						value = parentAnnos ;
					
					XSTypeDefinition baseType = type.getBaseType();
					
					
					if( value != null )
						mAnnotations.add( new AnnotationEntry(  CONTEXT_TYPE, 0 , getName(type) , null , getName(baseType) , value  ));


				}


			}
		}
		
		private QName getName(XSTypeDefinition type) {
			if( type == null )
				return null ;
			
			QName qname = new QName(Util.blankIfNull(type.getNamespace()), Util.blankIfNull(type.getName()) );
			return qname;
		
		
		}


		private QName getName(XSObject obj) {
			QName qname = new QName(obj.getNamespace(), obj.getName() );
			return qname;
		
		
		}

		private XQueryExecutable getXQuery(String string) throws SaxonApiException, IOException, URISyntaxException {

			XQueryCompiler mXQueryCompiler = Shell.getProcessor().newXQueryCompiler();

			
			InputStream isQuery = getClass().getResourceAsStream(string);
			try {
				URL url = mShell.getResource("/org/xmlsh/json/" + string );
				if( url != null ){
					URI uri = url.toURI();
					mXQueryCompiler.setBaseURI(  uri );
				}
				
				return mXQueryCompiler.compile( isQuery );
			} finally {
				isQuery.close();
			}
			
		}

		private AnnotationList  getAnnotations(XSObjectList annotations) throws XPathException, XMLStreamException, CoreException, SaxonApiException {
			
			if( annotations == null || annotations.getLength() == 0 )
				return null ;
			AnnotationList  annos = new AnnotationList( annotations.getLength() );
			
			for( int i = 0 ; i < annotations.getLength() ; i++ ){
				XSAnnotation annotation = (XSAnnotation) annotations.item(i);
				annos.add(new Annotation(annotation));
			}
			
			return annos;

			
		}


		private void writeQName( XMLStreamWriter sw , String name , QName q ) throws XMLStreamException
		{

			sw.writeStartElement(name);
			// sw.writeAttribute("prefix", q.getPrefix() );
			sw.writeAttribute("uri" , q.getNamespaceURI());
			sw.writeAttribute("localname", q.getLocalName());
			sw.writeEndElement();			
			
		}

		
}



//
//
//Copyright (C) 2008,2009,2010 David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
