package org.camunda.tngp.protocol.wf;

import org.camunda.tngp.protocol.taskqueue.MessageHeaderDecoder;
import org.camunda.tngp.util.buffer.BufferReader;

import org.agrona.DirectBuffer;

public class StartWorkflowInstanceResponseReader implements BufferReader
{
    protected MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
    protected StartWorkflowInstanceResponseDecoder bodyDecoder = new StartWorkflowInstanceResponseDecoder();

    public long wfInstanceId()
    {
        return bodyDecoder.id();
    }

    public void wrap(DirectBuffer buffer, int offset, int length)
    {
        headerDecoder.wrap(buffer, offset);
        bodyDecoder.wrap(buffer, offset + headerDecoder.encodedLength(), headerDecoder.blockLength(), headerDecoder.version());
    }
}