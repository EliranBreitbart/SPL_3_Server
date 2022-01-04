package net.srv;

import net.api.BidiMessagingProtocol;
import net.api.bidi.BidiProtocol;
import net.api.encdec;

import java.util.function.Supplier;

public class TPCMain {
    public static void main(String[] args){
        BaseServer<String> baseServer = BaseServer.threadPerClient(
        Integer.decode(args[0]).intValue(), //port
                () -> new BidiProtocol(),
                () -> new encdec());
        baseServer.serve();
    }
}
