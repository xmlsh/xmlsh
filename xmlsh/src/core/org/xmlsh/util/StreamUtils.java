package org.xmlsh.util;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class StreamUtils {



    /*
     *   List<String> list = StreamUtils.asStream( iterator).filter( t -> t.startsWith("A")).collect( toList() );
     */
        public static <T> Stream<T> asStream(Iterator<T> sourceIterator) {
            return asStream(sourceIterator, false);
        }

        public static <T> Stream<T> asStream(Iterator<T> sourceIterator, boolean parallel) {
            Iterable<T> iterable = () -> sourceIterator;
            return StreamSupport.stream(iterable.spliterator(), parallel);
        }


}
