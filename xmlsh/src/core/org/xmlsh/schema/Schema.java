/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.schema;
// import org.w3c.dom.DOMImplementationRegistry;
import java.util.Stack;

import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.xmlsh.util.Util;



public class Schema 
{
	private 	XSModel		mModel;
	private		Stack<XSElementDeclaration>		mScopeStack = new Stack<XSElementDeclaration>();



	public Schema( String schema ) throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{

		// Get DOM Implementation using DOM Registry

		System.setProperty(DOMImplementationRegistry.PROPERTY,
				"org.apache.xerces.dom.DOMXSImplementationSourceImpl");
		DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();

		XSImplementation impl = 
				(XSImplementation) registry.getDOMImplementation("XS-Loader");

		XSLoader schemaLoader = impl.createXSLoader(null);

		mModel = schemaLoader.loadURI(schema);


	}

	public XSElementDeclaration pushElement( String namespace , String localname )
	{
		XSElementDeclaration  elem = null;
		/*
		 * Look for scoped element first
		 */
		if( ! mScopeStack.isEmpty() ){

			XSElementDeclaration root = mScopeStack.peek();
			XSTypeDefinition  type = root.getTypeDefinition();
			if( type.getTypeCategory() != XSTypeDefinition.COMPLEX_TYPE )
				return null ;
			XSComplexTypeDefinition ctype = (XSComplexTypeDefinition) type ;

			XSParticle particle = ctype.getParticle();
			if( particle == null )
				return null ;

			XSTerm term = particle.getTerm() ;
			if( term == null )
				return null;

			elem = findElement( term , namespace , localname );



		}

		// try a global element


		if( elem == null &&  mScopeStack.isEmpty() ){
			elem = mModel.getElementDeclaration(localname, namespace );
			if( elem == null )
				return null ; // SNH
		}
		if( elem != null )
			mScopeStack.push(elem);

		return elem ;



	}




	private XSElementDeclaration findElement(XSTerm term, String namespace, String localname) {
		switch( term.getType() ){
		case	XSConstants.ELEMENT_DECLARATION :
		{
			XSElementDeclaration elem = (XSElementDeclaration) term ;
			if( Util.isEqual(elem.getNamespace() , namespace ) && 
					Util.isEqual(term.getName(), localname) )
				return elem ;

			/* 
			 * Try substitution group 
			 */

			XSObjectList groups = mModel.getSubstitutionGroup(elem);
			if( groups != null )
			{
				for( int i = 0 ; i < groups.getLength() ; i++ ){
					XSObject obj = groups.item(i);
					if( obj.getType() == XSConstants.ELEMENT_DECLARATION &&
							Util.isEqual(obj.getNamespace() , namespace ) && 
							Util.isEqual(obj.getName(), localname) )
						return (XSElementDeclaration)obj ;

				}

			}


			return null ;
		}


		case	XSConstants.MODEL_GROUP : ;
		{
			XSModelGroup group = (XSModelGroup) term ;
			XSObjectList particles = group.getParticles();
			for( int i = 0 ; i < particles.getLength() ; i++ ){
				XSParticle particle = (XSParticle) particles.item(i);
				XSTerm pterm = particle.getTerm();
				if( pterm != null ){
					XSElementDeclaration elem = findElement( pterm  , namespace , localname );
					if( elem != null )
						return elem ;
				}
			}
			return null ;



		}



		case	XSConstants.WILDCARD :
			return null ;
		}
		return null ;


	}

	/**
	 * @return the model
	 */
	public XSModel getModel() {
		return mModel;
	}

	public void popElement() {
		if( !mScopeStack.isEmpty() )
			mScopeStack.pop();

	}


	public XSObjectList getAnnotations()
	{
		return mModel.getAnnotations();

	}


}



//
//
//Copyright (C) 2008-2014 David A. Lee.
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
