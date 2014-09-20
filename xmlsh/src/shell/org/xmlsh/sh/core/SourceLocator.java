package org.xmlsh.sh.core;

public interface SourceLocator {

	/**
	 * @return the source
	 */

	public String getName();

	public String getSource();

	public String getSource(boolean relpath);

	/**
	 * @return the startline
	 */
	public int getStartline();

	/**
	 * @return the startColumn
	 */
	public int getStartColumn();

	/**
	 * @return the endLine
	 */
	public int getEndLine();

	/**
	 * @return the endColumn
	 */
	public int getEndColumn();

	/**
	 * @return the startLine
	 */
	public int getStartLine();

}