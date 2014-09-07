package org.xmlsh.sh.logging;

import java.io.Serializable;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "Shell", category = "Core", elementType = "appender", printObject = true)
public final class ShellAppender extends AbstractAppender  {
 
 
    protected ShellAppender(String name, Filter filter, Layout<? extends Serializable> layout)
  {
    super(name, filter, layout);
  }

    @PluginFactory
    public static ShellAppender createAppender(@PluginAttribute("name") String name,
                                              @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
                                              @PluginElement("Layout") Layout<?> layout,
                                              @PluginElement("Filters") Filter filter) {
 
        if (name == null) {
            LOGGER.error("No name provided for ShellAppender");
            return null;
        }
 
        return new ShellAppender(name, filter, layout );
    }

    // @TODO: Tie this into xmlshui
    @Override
    public void append(LogEvent event)
    {
      
      // System.err.println(event.toString());
      
      
    }
}