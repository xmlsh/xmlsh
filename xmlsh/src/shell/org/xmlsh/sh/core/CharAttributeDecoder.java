package org.xmlsh.sh.core;

import java.util.EnumSet;

public interface CharAttributeDecoder<E extends Enum<E>> {

	void decode( StringBuilder sb ,  char ch ,long attrs );
}
