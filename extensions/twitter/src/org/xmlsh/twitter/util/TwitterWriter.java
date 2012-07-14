/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.twitter.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.util.Util;
import twitter4j.Annotations;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.MediaEntity.Size;
import twitter4j.Place;
import twitter4j.Tweet;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

public class TwitterWriter {
	
	public static final String kTWITTER_NS = "http://www.xmlsh.org/schemas/twitter";

	
	public XMLStreamWriter mWriter;



	public TwitterWriter(XMLStreamWriter writer) {
		super();
		mWriter = writer;
	}

	public final static String sCOMMON_OPTS = "c=connect:,u=user:,p=password:";
	public TwitterWriter() {
		super();
	}

	
	
	
	public void closeWriter() throws XMLStreamException {
		mWriter.flush();
		mWriter.close();
	}


	public void endDocument() throws XMLStreamException {
		mWriter.writeEndDocument();
	}


	public void endElement() throws XMLStreamException {
		mWriter.writeEndElement();
	}


	public void startElement(String localname) throws XMLStreamException {
		mWriter.writeStartElement(localname);
	}


	public void startDocument() throws XMLStreamException {
		mWriter.writeStartDocument();
	}
	
	public	  void attribute( String localName , String value ) throws XMLStreamException
	{
		mWriter.writeAttribute(localName, Util.notNull(value));
	}
	
	public	  void characters( String value ) throws XMLStreamException
	{
		mWriter.writeCharacters(value);
	}




	String formatXSDateTime(Date date) {
		if( date == null )
			date = new Date();
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
		df.setTimeZone( TimeZone.getTimeZone("UTC") );
			
			
		
		return df.format(date);
	}
	
	public String httpGetString( String sURL ) throws IOException
	{
		URL url = new URL( sURL  );
		InputStream is = url.openStream();
		String s = Util.readString(is, "UTF8");
		is.close();
		return s;
		
		
	}
	
	
	


	public void write(Tweet t) throws XMLStreamException {
		startElement("tweet");
		attribute("id",t.getId());

		// write("annotations",t.getAnnotations());
		write("created-at",t.getCreatedAt());
		write("from-user",t.getFromUserId(),t.getFromUser());
		write("geo-location",t.getGeoLocation());
		write("hash-tags",t.getHashtagEntities());
		write("iso-language-code",t.getIsoLanguageCode());
		write("location",t.getLocation());
		write("media",t.getMediaEntities());
		write("place",t.getPlace());
		write("profile-image-url",t.getProfileImageUrl());
		write("source",t.getSource());
		write("text",t.getText());
		write("to-user",t.getToUserId(),t.getToUser());
		write("url-entities",t.getURLEntities());
		write("user-mention-entities",t.getUserMentionEntities());
		
		
		
		endElement();
		
	}




	private void write(String localName, UserMentionEntity[] userMentionEntities) throws XMLStreamException {
		startElement(localName);
		for( UserMentionEntity e : userMentionEntities )
			write("entity" , e );
		endElement();
		
	}




	private void write(String localName, UserMentionEntity e) throws XMLStreamException {
		startElement(localName);
		attribute("id",e.getId());
		attribute( "screen-name" , e.getScreenName());
		attribute("start", e.getStart());
		attribute("end", e.getEnd());
		endElement();
		
	}




	private void write(String localName, User user) throws XMLStreamException {
		startElement("user"); 
		attribute("created-at" , user.getCreatedAt());
		attribute("description" ,user.getDescription());
		attribute("favorites-count" ,user.getFavouritesCount());
		user.getFollowersCount();
		user.getFriendsCount();
		attribute("id" , user.getId());
		attribute("lang" , user.getLang());
		user.getListedCount();

		attribute("name",user.getName());
		user.getProfileBackgroundColor();
		user.getProfileBackgroundImageUrl();
		user.getProfileBackgroundImageUrlHttps();
		user.getProfileImageURL();
		user.getProfileImageUrlHttps();
		user.getProfileLinkColor();
		user.getProfileSidebarBorderColor();
		user.getProfileTextColor();
		user.getRateLimitStatus();
		attribute("screen-name",user.getScreenName());
		
		user.getStatusesCount();
		user.getTimeZone();
		user.getURL();
		user.getUtcOffset();
		write("location",user.getLocation());
		write("user-status",user.getStatus());
		endElement();
		
	}




	private void attribute(String localName, Date dt) throws XMLStreamException {
		attribute(localName , Util.formatXSDateTime(dt));
		
	}




	private void write(String localName, URLEntity[] urlEntities) throws XMLStreamException {
		startElement(localName);
		for( URLEntity u : urlEntities )
			write(u);
		endElement();
		
	}




	private void write(URLEntity u) throws XMLStreamException {
		attribute("display-url",u.getDisplayURL());
		attribute("end",u.getEnd());
		attribute("expanded-url",u.getExpandedURL().toString());
		attribute("start",u.getStart());
		attribute("url",u.getURL().toString());
		
	}




	private void write(String localName, Place place) throws XMLStreamException {
		if( place != null ){
		startElement(localName);
		// TODO: Lots of place stuff
		characters(place.getName());
		endElement();
		}
		
	}




	private void write(String localName, MediaEntity[] mediaEntities) throws XMLStreamException {
		if( mediaEntities != null ){
		startElement(localName);
		for( MediaEntity m : mediaEntities)
			write("media",m);
		endElement();
		}
		
	}




	private void write(String localName, MediaEntity m) throws XMLStreamException {
		startElement(localName );
		attribute("display-url",m.getDisplayURL());
		attribute("end",m.getEnd());
		attribute("expanded-url",m.getExpandedURL().toString());
		attribute("id",m.getId());
		attribute("media-url",m.getMediaURL().toString());
		attribute("media-url-https",m.getMediaURLHttps().toString());
	
		attribute("start",m.getStart());
		attribute("url",m.getURL().toString()); 	
		Set<Entry<Integer, Size>> sizes = m.getSizes().entrySet();
		for( Entry<Integer,Size> s  : sizes ){
			startElement("size");
			attribute( "height" , s.getValue().getHeight());
			attribute( "width" , s.getValue().getWidth());
			attribute( "resize" , s.getValue().getResize());
			endElement();
			
		}
	}




	private void write(String localName, GeoLocation geoLocation) throws XMLStreamException {
		if( geoLocation != null ){
		startElement(localName);
		attribute("latitude",geoLocation.getLatitude());
		attribute("longitude",geoLocation.getLongitude());
		endElement();
		}
		
	}




	private void attribute(String localName, double d) throws XMLStreamException {
		attribute(localName , String.valueOf(d));
		
	}




	private void write(String localName, HashtagEntity[] hashtagEntities) throws XMLStreamException {
		startElement(localName);
		for( HashtagEntity e : hashtagEntities)
			write( "hash-tag" , e );
		endElement();
		
	}




	private void write(String localName, HashtagEntity e) throws XMLStreamException {
		startElement(localName);
		attribute("start" , e.getStart());
		attribute("end" , e.getEnd());
		characters(e.getText());
		endElement();
		
		
	}




	private void write(String localName, long id, String string) throws XMLStreamException {
		startElement(localName);
		attribute("id" , id );
		characters(string);
		endElement();

	}




	private void write(String localName, String value) throws XMLStreamException {
		startElement(localName);
		characters(value);
		endElement();
		
	}




	private void write(String localName, Date date) throws XMLStreamException {
		startElement(localName);
        characters( Util.formatXSDateTime(date));
        endElement();
		
	}




	private void attribute(String localName, long l) throws XMLStreamException {
		attribute(localName, String.valueOf(l));
		
	}

	 public void write(String localName , Status status) throws XMLStreamException {
		 if( status != null ){
		 startElement(localName);
			attribute("id",status.getId());

			// write("annotations",t.getAnnotations());
			write("created-at",status.getCreatedAt());
			
			write("user",status.getUser());
			write("geo-location",status.getGeoLocation());
			write("hash-tags",status.getHashtagEntities());
			
			write("media",status.getMediaEntities());
			write("place",status.getPlace());

			write("source",status.getSource());
			write("text",status.getText());

			write("url-entities",status.getURLEntities());
			write("user-mention-entities",status.getUserMentionEntities());
			
			
			
			endElement();
		 }	
     }




	public void writeDefaultNamespace() throws XMLStreamException {
		mWriter.writeDefaultNamespace(kTWITTER_NS);
		
	}




	public void startElement(String ns, String localName) throws XMLStreamException {
		mWriter.writeStartElement("",localName,ns);
		
	}

	
}

//
//
// Copyright (C) 2008-2012  David A. Lee.
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
