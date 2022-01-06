package net.srv;
import net.api.bidi.BidiProtocol;
import net.api.encdec;

public class ReactorMain {
    public static void main(String[] args) {
        Reactor<String> reactor = new Reactor<String>(10,
                Integer.decode(args[0]).intValue(), //port
                () -> new BidiProtocol(),
                () -> new encdec());
        reactor.serve();
    }
}
