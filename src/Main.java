import logic.Client;
import logic.Server;

/**
 * Created by ahmedatef on 12/5/15.
 */
public class Main {
  public static void main(String[] args) throws InterruptedException {
    Server server = new Server(7787);
    Thread.sleep(3000);
    Client client = new Client(7787);
  }
}
