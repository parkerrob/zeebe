package org.camunda.tngp.client.impl.cmd;

import org.camunda.tngp.client.impl.ClientChannelResolver;

public class DummyChannelResolver implements ClientChannelResolver
{
    protected int channelId = -1;

    @Override
    public int getChannelIdForCmd(final AbstractCmdImpl<?> cmd)
    {
        if (channelId == -1)
        {
            throw new RuntimeException("Not connected; call connect() on the client first.");
        }

        return channelId;
    }

    public void setChannelId(int channelId)
    {
        this.channelId = channelId;
    }

}