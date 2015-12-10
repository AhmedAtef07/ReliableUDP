import logic.Client;
import logic.Server;

import java.io.IOException;

/**
 * Created by ahmedatef on 12/5/15.
 */
public class Main {
  public static void main(String[] args) throws InterruptedException, IOException {
    Server server = new Server(7787);
    Thread.sleep(370);
    Client client = new Client("localhost", 7787);
    client.sendImageToServer("/home/ahmedatef/img.jpg");
//    while(true) {
//      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//      String s = br.readLine();
//      client.sendToServer(s);
//    }
  }
}
