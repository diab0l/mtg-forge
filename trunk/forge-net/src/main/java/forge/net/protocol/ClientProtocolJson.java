package forge.net.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import forge.net.protocol.toclient.*;
import forge.net.protocol.toserver.*;
import forge.util.TextUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/** 
 * The expected incoming message format is: /{opcode} {json-object}, where opcode is a string among keys of headerToClassInbound
 * If string starts with a non-slash character, the whole line is considered a chat packet
 *
 */
public class ClientProtocolJson implements ClientProtocol<IPacketSrv, IPacketClt> {
    private final static Map<String, Class<? extends IPacketSrv>> headerToClassInbound = new HashMap<String, Class<? extends IPacketSrv>>();
    private final static Map<Class<? extends IPacketClt>, String> classToHeaderOutbound = new HashMap<Class<? extends IPacketClt>, String>();

    // Static ctor to fill maps
    static { 
        // The what remote part sends us
        headerToClassInbound.put("echo", EchoPacketSrv.class);
        headerToClassInbound.put("s", ChatPacketSrv.class);
        headerToClassInbound.put("auth", AuthorizePacketSrv.class);

        // The what we reply there
        classToHeaderOutbound.put(WelcomePacketClt.class, "welcome");
        classToHeaderOutbound.put(AuthResultPacketClt.class, "auth");
        classToHeaderOutbound.put(ChatPacketClt.class, "s");
        classToHeaderOutbound.put(EchoPacketClt.class, "echo");
        classToHeaderOutbound.put(ErrorNoStateForPacketClt.class, "err:packet-state");
        classToHeaderOutbound.put(ErrorIncorrectPacketClt.class, "err:packet");
    }

    private final Gson gson = new Gson(); // looks like a single instance per class is enough

    @Override
    public IPacketSrv decodePacket(String data) {
        if ( '/' != data.charAt(0) )
            return new ChatPacketSrv(data);
        
        data = data.substring(1);
        String[] parts = TextUtil.split(data, ' ', 2);

        Class<? extends IPacketSrv> packetClass = headerToClassInbound.get(parts[0]);
        if( null == packetClass )
            return new IncorrectPacketSrv("Unknown header: " + parts[0]);

        String args = parts.length > 1 ? parts[1] : null;
        if ( StringUtils.isBlank(args) )
            args = "{}"; // assume default empty object

        try {
            return gson.fromJson(args, packetClass);
        } catch( JsonParseException  ex ) {
            return new IncorrectPacketSrv("Invalid json: " + args);
        }
    }

    @Override
    public String encodePacket(IPacketClt packet) {
        Class<? extends IPacketClt> packetClass = packet.getClass();
        String prefix = classToHeaderOutbound.get(packetClass);
        return String.format("/%s %s", prefix != null ? prefix : "/!unserialized!: " + packetClass.getName(), gson.toJson(packet));
    }

}
