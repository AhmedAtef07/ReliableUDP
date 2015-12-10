package logic;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * Created by ahmedatef on 11/29/15.
 *
 * Packet Header is composed of:
 * Length{2}:Type{2}:Current{4}
 * First 2 bytes for packet length (2^16 = 65536 max length follows)
 * Then  2 bytes for PacketType either SIGNAL or DATA (Too much, but kept for consistency)
 * Then  4 bytes for packet number
 */
public class Packet {

  public enum PacketType {
    SIGNAL,
    DATA
  }

  private short length;
  private PacketType type;
  private int packetNumber;
  private Object body;
  private byte[] raw;

  private static final short HEADER_LENGTH = 8;

  public Packet(PacketType type, int packetNumber, Object body) {
    encode(type, packetNumber, body);
  }

  public Packet(byte[] raw) {
    decode(raw);
  }

  private void encode(PacketType packetType, int packetNumber, Object body) {
    short length = HEADER_LENGTH;
    byte[] data = null;

    switch(packetType) {
      case DATA:
        byte[] stringData = body.toString().getBytes();
        length += stringData.length;
        data = stringData;
        break;
      case SIGNAL:
        int signalByteLength = 2; // Signal values are represented in shorts (2 bytes).
        length += signalByteLength;
        Signal signal = (Signal)body;
        ByteBuffer byteBuffer = ByteBuffer.allocate(signalByteLength);
        byteBuffer.putShort((short)signal.ordinal());
        data = byteBuffer.array();
        break;
    }

    ByteBuffer byteBuffer = ByteBuffer.allocate(length);

    byteBuffer.putShort(length);
    System.out.println("Encoded as length: " + length);
    byteBuffer.putShort((short)packetType.ordinal());
    System.out.println("ordinal ==> " + packetType + " " + (short)packetType.ordinal());
    byteBuffer.putInt(packetNumber);
    byteBuffer.put(data);

    raw = byteBuffer.array();
    System.out.println("raw length encoded: " + raw.length);
    setLocalVariables(length, packetType, packetNumber, body, raw);
  }

  private void decode(byte[] raw) {
    System.out.println("raw length received: " + raw.length);

    ByteBuffer bb = ByteBuffer.wrap(raw);
    short length = bb.getShort();
    System.out.println("Decoded as length: " + length);
    PacketType packetType = PacketType.values()[bb.getShort()];
    System.out.println(packetType);
    int packetNumber = bb.getInt();

    Object body = null;

    switch(packetType) {
      case DATA:
        body = new String(raw, HEADER_LENGTH, length - HEADER_LENGTH);
        break;
      case SIGNAL:
        body = Signal.values()[(ByteBuffer.wrap(raw, HEADER_LENGTH, 2).getShort())];
        break;
    }

    setLocalVariables(length, packetType, packetNumber, body, raw);
  }

  private void setLocalVariables(short length, PacketType type, int packetNumber, Object body,
                                 byte[] raw) {
    this.length = length;
    this.type = type;
    this.packetNumber = packetNumber;
    this.body = body;
    this.raw = raw;
  }

  public short getLength() {
    return length;
  }

  public PacketType getType() {
    return type;
  }

  public int getPacketNumber() {
    return packetNumber;
  }

  public Object getBody() {
    return body;
  }

  public byte[] getRaw() {
    return raw;
  }

  public void sendPacketTo(PacketHandler packetHandler, InetAddress inetAddress, int port) {
    DatagramPacket sendPacket = new DatagramPacket(
            this.getRaw(),
            this.getRaw().length,
            inetAddress,
            port);

//    packetHandler.send(sendPacket);
    System.out.println("Some packet sent to a client");
  }
}

