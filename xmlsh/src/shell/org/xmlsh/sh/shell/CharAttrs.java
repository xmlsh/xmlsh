package org.xmlsh.sh.shell;

import static org.xmlsh.sh.shell.CharAttr.ATTR_ESCAPED;
import static org.xmlsh.sh.shell.CharAttr.ATTR_HARD_QUOTE;
import static org.xmlsh.sh.shell.CharAttr.ATTR_PRESERVE;
import static org.xmlsh.sh.shell.CharAttr.ATTR_SOFT_QUOTE;

import java.util.EnumSet;

import org.apache.commons.lang3.EnumUtils;
import org.xmlsh.util.Util;
public class CharAttrs  implements Cloneable {
	private static CharAttrs[] constInstances = {
		new CharAttrs(ATTR_SOFT_QUOTE),
		new CharAttrs(ATTR_HARD_QUOTE),
				new CharAttrs(ATTR_PRESERVE),   // Do not touch, unquote or expand
						new CharAttrs(ATTR_ESCAPED )
	};

	private EnumSet<CharAttr> mSet ;
	private static CharAttrs NONE = new CharAttrs();
	
	@Override
	public CharAttrs clone() {
		return new CharAttrs( this );
		
	}
	
	public CharAttrs() {
		mSet = EnumSet.noneOf(CharAttr.class);
	}
	public CharAttrs(CharAttrs attrs){
		this.mSet = attrs.mSet.clone();
	}
	public CharAttrs(CharAttr attr){
		this.mSet = EnumSet.of(attr);
	}

	public CharAttrs(EnumSet<CharAttr> set) {
		this.mSet = set ;
	}
	public boolean isQuote() { 
		return Util.setContainsAny( mSet , ATTR_SOFT_QUOTE ,ATTR_HARD_QUOTE ); 
	}
	public static final CharAttrs valueOf(char c)
	{
		if( c == '\'')
			return newInstance(ATTR_HARD_QUOTE);
		else
			if( c == '\"')
				return newInstance(ATTR_SOFT_QUOTE);
			else
				return newInstance();
	}
	public boolean isSet( CharAttr ca ) {
		return mSet.contains(ca);
	}


	public boolean isSet( CharAttrs ca ) {
		return mSet.containsAll(ca.mSet);
	}
	
	public boolean isSet( long ca ) {
		return mSet.containsAll( EnumUtils.processBitVector(CharAttr.class, ca));
	}
	public void clear( CharAttr ca ) {
		mSet.remove( ca );
	}
	public void clear( CharAttrs ca ) {
		mSet.removeAll(ca.mSet);
	}
	public void set( CharAttr ca ) {
		mSet.add(ca);
	}
	public void set( CharAttrs ca ) {
		mSet.addAll(ca.mSet);
	}

	public static boolean isQuote( char c ) {
		return c == '\'' || c == '"';
	}
	public boolean isHardQuote()
	{
		return  isSet(ATTR_HARD_QUOTE);
	}
	public boolean isSoftQuote()
	{
		return  isSet(ATTR_SOFT_QUOTE);
	}
	public boolean isEscaped()
	{
		return  isSet(ATTR_ESCAPED);
	}
	public boolean isPreserve()
	{
		return isSet( ATTR_PRESERVE );
	}
	public byte toBits() {
		return  (byte) EnumUtils.generateBitVector(CharAttr.class, mSet);
	}
	
	public static byte toBits(EnumSet<CharAttr> attrs ) {
		return  (byte) EnumUtils.generateBitVector(CharAttr.class, attrs);
	}
	
	public static CharAttrs fromBits( byte bits ){
		return new CharAttrs( EnumUtils.processBitVector(CharAttr.class, bits));
	}
	
	@Override
	public String toString() 
	{
		return logString( new StringBuilder("CharAttrs:") ).toString();
	}
	public StringBuilder logString(StringBuilder sb) {

		Util.logString( sb , mSet );
		return sb ;
		
	}

	public static CharAttrs newInstance() {
		return new CharAttrs();
	}
	public static CharAttrs newInstance(CharAttr attr) {
		return new CharAttrs(attr);
	}

	public static CharAttrs constInstance() {
		return NONE ;
	}
	
	public static CharAttrs constInstance(CharAttr attr){
	    if( attr == null )
	    	return constInstance();
		return constInstances[ attr.ordinal()];
		
	}
	
}
