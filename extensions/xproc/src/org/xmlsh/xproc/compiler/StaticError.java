/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;
/*
 * err:XS0001 
It is a static error if there are any loops in the connections between steps: no step can be connected to itself nor can there be any sequence of connections through other steps that leads back to itself.

See: Connections

err:XS0002 
All steps in the same scope must have unique names: it is a static error if two steps with the same name appear in the same scope.

See: Scoping of Names

err:XS0004 
It is a static error to declare two or more options on the same step with the same name.

See: p:option, p:with-option

err:XS0006 
It is a static error if the primary output port has no binding and the last step in the subpipeline does not have a primary output port.

See: p:for-each, p:viewport, Declaring pipelines

err:XS0007 
It is a static error if two subpipelines in a p:choose declare different outputs.

See: p:choose

err:XS0008 
It is a static error if any element in the XProc namespace has attributes not defined by this specification unless they are extension attributes.

See: Syntax Summaries

err:XS0009 
It is a static error if the p:group and p:catch subpipelines declare different outputs.

See: p:try

err:XS0010 
It is a static error if a pipeline contains a step whose specified inputs, outputs, and options do not match the signature for steps of that type.

See: Extension Steps

err:XS0011 
It is a static error to identify two ports with the same name on the same step.

See: Document Inputs, Parameter Inputs, p:output

err:XS0014 
It is a static error to identify more than one output port as primary.

See: p:output

err:XS0015 
It is a static error if a compound step has no contained steps.

See: Syntax Summaries

err:XS0016 
It is a static error if the select attribute is not specified.

See: p:variable, p:with-option, p:with-param

err:XS0017 
It is a static error to specify that an option is both required and has a default value.

See: p:option

err:XS0018 
If an option is required, it is a static error to invoke the step without specifying a value for that option.

See: p:option

err:XS0019 
it is a static error for a variable's document binding to refer to the output port of any step in the surrounding container's contained steps

See: p:variable

err:XS0020 
It is a static error if the binding attribute on p:namespaces is specified and its value is not the name of an in-scope binding.

See: Namespaces on variables, options, and parameters

err:XS0021 
It is a static error if the import references in a pipeline or pipeline library are circular.

See: p:library

err:XS0022 
In all cases except the p:output of a compound step, it is a static error if the port identified by a p:pipe is not in the readable ports of the step that contains the p:pipe.

See: p:pipe

err:XS0024 
It is a static error if the content of the p:inline element does not consist of exactly one element, optionally preceded and/or followed by any number of processing instructions, comments or whitespace characters.

See: p:inline

err:XS0025 
It is a static error if the expanded-QName value of the type attribute is in no namespace.

See: p:declare-step

err:XS0026 
It is a static error if the port specified on the p:log is not the name of an output port on the step in which it appears or if more than one p:log element is applied to the same port.

See: p:log

err:XS0027 
It is a static error if an option is specified with both the shortcut form and the long form.

See: Syntactic Shortcut for Option Values

err:XS0028 
It is a static error to declare an option or variable in the XProc namespace.

See: p:variable, p:option, p:with-param

err:XS0029 
It is a static error to specify a binding for a p:output inside a p:declare-step for an atomic step.

See: p:output

err:XS0030 
It is a static error to specify that more than one input port is the primary.

See: Document Inputs, Parameter Inputs

err:XS0031 
It is a static error to use an option on an atomic step that is not declared on steps of that type.

See: Syntactic Shortcut for Option Values, p:with-option

err:XS0032 
It is a static error if no binding is provided and the default readable port is undefined.

See: Document Inputs, p:variable, p:with-option, p:with-param

err:XS0033 
It is a static error to specify any kind of input other than “document” or “parameter”.

See: Parameter Inputs

err:XS0034 
It is a static error if the specified port is not a parameter input port or if no port is specified and the step does not have a primary parameter input port.

See: p:with-param

err:XS0035 
It is a static error if the declaration of a parameter input port contains a binding; parameter input port declarations must be empty.

See: Parameter Inputs

err:XS0036 
All the step types in a pipeline must have unique names: it is a static error if any step type name is built-in and/or declared or defined more than once in the same scope.

See: Scoping of Names

err:XS0037 
It is a static error if any step directly contains text nodes that do not consist entirely of whitespace.

See: Syntax Summaries

err:XS0038 
It is a static error if any required attribute is not provided.

See: Syntax Summaries

err:XS0039 
It is a static error if the port specified on the p:serialization is not the name of an output port on the pipeline in which it appears or if more than one p:serialization element is applied to the same port.

See: p:serialization

err:XS0040 
It is a static error to specify any value other than true.

See: Parameter Inputs

err:XS0041 
It is a static error to specify both binding and element on the same p:namespaces element.

See: Namespaces on variables, options, and parameters

err:XS0042 
It is a static error to attempt to provide a binding for an input port on the declaration of an atomic step.

See: Document Inputs

err:XS0044 
It is a static error if any element in the XProc namespace or any step has element children other than those specified for it by this specification. In particular, the presence of atomic steps for which there is no visible declaration may raise this error.

See: Syntax Summaries

err:XS0048 
It is a static error to use a declared step as a compound step.

See: Extension Steps

err:XS0050 
It is a static error if a pipeline attempts to import two (or more) libraries with URIs that identify steps associated with a particular version of XProc.

See: p:import

err:XS0051 
It is a static error if the except-prefixes attribute on p:namespaces does not contain a list of tokens or if any of those tokens is not a prefix bound to a namespace in the in-scope namespaces of the p:namespaces element.

See: Namespaces on variables, options, and parameters

err:XS0052 
It is a static error if the URI of a p:import cannot be retrieved or if, once retrieved, it does not point to a p:library, p:declare-step, or p:pipeline.

See: p:import

err:XS0053 
It is a static error to import a single pipeline if that pipeline does not have a type.

See: p:import

err:XS0055 
It is a static error if a primary parameter input port has no binding and the pipeline that contains the step has no primary parameter input port.

See: Parameter Inputs

err:XS0057 
It is a static error if a namespace prefix is used within the exclude-inline-prefixes attribute and there is no namespace binding in scope for that prefix.

See: p:inline

err:XS0058 
It is a static error if the value #default is used within the exclude-inline-prefixes attribute and there is no default namespace in scope.

See: p:inline


 */
public class StaticError extends Exception {

	public StaticError() {
		// TODO Auto-generated constructor stub
	}

	public StaticError(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public StaticError(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public StaticError(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}



//
//
//Copyright (C) 2008,2009 , David A. Lee.
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
