package forge.net.client.state;

import forge.net.protocol.incoming.Packet;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public interface IClientState {
    boolean processPacket(Packet data);
}
