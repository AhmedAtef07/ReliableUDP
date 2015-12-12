import logic.Client;
import logic.SelectiveRepeatServer;

import java.io.IOException;

/**
 * Created by ahmedatef on 12/5/15.
 */
public class Main {
  public static void main(String[] args) throws InterruptedException, IOException {
//    Server server = new Server(7787);
    SelectiveRepeatServer selectiveRepeatServer = new SelectiveRepeatServer(7787, 3);
    Thread.sleep(370);
    Client client = new Client("localhost", 7787);

    Thread.sleep(400);
    client.sendShakeHandPacketToServer();
  }
}
