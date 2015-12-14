import logic.*;

import java.io.IOException;
import java.util.Vector;

/**
 * Created by ahmedatef on 12/5/15.
 */
public class Main {
  public static void main(String[] args) throws InterruptedException, IOException {
//    Server server = new Server(7787);
//    new SelectiveRepeatServer(7787, 3, 2, 1000);
//    new StopAndWaitServer(7787, 2, 1000);
    new GoBackNServer(7787, 3, 100, 1000);
    Thread.sleep(370);
//    Client client = new Client("localhost", 7787);
    Client client = new GoBackNClient("localhost", 7787);

    Thread.sleep(400);
    client.sendShakeHandPacketToServer();

    int[] lossP = {100, 20, 10, 3};
    int fixedTimeout = 1000;
    int fixedWindowSize = 4;

    Vector<Server> s = new Vector<Server>();
    Vector<Client> c = new Vector<Client>();

//    for(int i = 0; i < lossP.length; ++i) {
//      for(int k = 0; k < 5; ++k) {
//        s.add(new StopAndWaitServer(7787 + i + k * 10, lossP[i], fixedTimeout));
//        c.add(new Client("localhost", 7787 + i + k * 10));
//        c.lastElement().sendShakeHandPacketToServer();
//      }
//    }
//
//    for(int i = 0; i < lossP.length; ++i) {
//      for(int k = 0; k < 5; ++k) {
//        s.add(new SelectiveRepeatServer(7887 + i + k * 10, lossP[i], fixedWindowSize, fixedTimeout));
//        c.add(new Client("localhost", 7887 + i + k * 10));
//        c.lastElement().sendShakeHandPacketToServer();
//      }
//    }
//
//    for(int i = 0; i < lossP.length; ++i) {
//      for(int k = 0; k < 5; ++k) {
//        s.add(new GoBackNServer(7987 + i + k * 10, lossP[i], fixedWindowSize, fixedTimeout));
//        c.add(new GoBackNClient("localhost", 7987 + i + k * 10));
//        c.lastElement().sendShakeHandPacketToServer();
//      }
//    }
  }
}
