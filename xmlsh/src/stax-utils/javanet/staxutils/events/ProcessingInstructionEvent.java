/*
 * $Id: ProcessingInstructionEvent.java,v 1.2 2004/07/15 02:11:02 cniles Exp $
 * 
 * Copyright (c) 2004, Christian Niles, unit12.net
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *		*   Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 * 
 *	    *	Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 * 
 *      *   Neither the name of Christian Niles, Unit12, nor the names of its
 *          contributors may be used to endorse or promote products derived from
 *          this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package javanet.staxutils.events;

import javax.xml.stream.Location;
import javax.xml.stream.events.ProcessingInstruction;

/**
 * {@link ProcessingInstruction} event implementation.
 * 
 * @author Christian Niles
 * @version $Revision: 1.2 $
 */
public class ProcessingInstructionEvent extends AbstractXMLEvent
        implements
            ProcessingInstruction {

    /** The PI target. */
    protected String target;

    /** The instruction data. */
    protected String data;

    public ProcessingInstructionEvent(String target, String data) {

        this.target = target;
        this.data = data;

    }

    public ProcessingInstructionEvent(String target, String data,
            Location location) {

        super(location);
        this.target = target;
        this.data = data;

    }

    public ProcessingInstructionEvent(ProcessingInstruction that) {

        super(that);
        this.target = that.getTarget();
        this.data = that.getData();

    }

    /**
     * Returns {@link #PROCESSING_INSTRUCTION}.
     */
    public int getEventType() {

        return PROCESSING_INSTRUCTION;

    }

    public String getTarget() {

        return this.target;

    }

    public String getData() {

        return this.data;

    }
    
}