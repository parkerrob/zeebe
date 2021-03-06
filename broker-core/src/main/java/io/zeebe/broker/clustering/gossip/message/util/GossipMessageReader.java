/*
 * Zeebe Broker Core
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.zeebe.broker.clustering.gossip.message.util;

import java.util.Iterator;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

import io.zeebe.broker.clustering.gossip.data.Peer;
import io.zeebe.broker.clustering.gossip.data.RaftMembershipList;
import io.zeebe.clustering.gossip.GossipDecoder;
import io.zeebe.clustering.gossip.GossipDecoder.PeersDecoder;
import io.zeebe.clustering.gossip.GossipDecoder.PeersDecoder.EndpointsDecoder;
import io.zeebe.clustering.gossip.GossipDecoder.PeersDecoder.RaftMembershipsDecoder;
import io.zeebe.transport.SocketAddress;
import io.zeebe.util.buffer.BufferReader;

public class GossipMessageReader implements BufferReader, Iterator<Peer>
{
    private Iterator<PeersDecoder> iterator;

    private final GossipDecoder bodyDecoder = new GossipDecoder();

    private final Peer currentPeer = new Peer();

    @Override
    public void wrap(final DirectBuffer values, final int offset, final int length)
    {
        bodyDecoder.wrap(values, offset, GossipDecoder.BLOCK_LENGTH, GossipDecoder.SCHEMA_VERSION);
        iterator = bodyDecoder.peers().iterator();
    }

    @Override
    public boolean hasNext()
    {
        return iterator.hasNext();
    }

    @Override
    public Peer next()
    {
        final PeersDecoder decoder = iterator.next();

        currentPeer.reset();

        currentPeer.heartbeat()
            .generation(decoder.generation())
            .version(decoder.version());

        for (final EndpointsDecoder endpointsDecoder : decoder.endpoints())
        {
            final SocketAddress endpoint;
            switch (endpointsDecoder.endpointType())
            {
                case CLIENT:
                    endpoint = currentPeer.clientEndpoint();
                    break;
                case MANAGEMENT:
                    endpoint = currentPeer.managementEndpoint();
                    break;
                case REPLICATION:
                    endpoint = currentPeer.replicationEndpoint();
                    break;
                default:
                    throw new RuntimeException("Unknown endpoint type for peer: " + endpointsDecoder.endpointType());
            }

            final MutableDirectBuffer hostBuffer = endpoint.getHostBuffer();
            final int hostLength = endpointsDecoder.hostLength();

            endpoint.port(endpointsDecoder.port());
            endpoint.hostLength(hostLength);
            endpointsDecoder.getHost(hostBuffer, 0, hostLength);
            endpoint.host();
        }

        final RaftMembershipList raftMemberships = currentPeer.raftMemberships();
        for (final RaftMembershipsDecoder raftMembershipsDecoder : decoder.raftMemberships())
        {
            raftMemberships.add(raftMembershipsDecoder);
        }

        currentPeer.state(decoder.state())
            .changeStateTime(-1L);

        return currentPeer;
    }

}
