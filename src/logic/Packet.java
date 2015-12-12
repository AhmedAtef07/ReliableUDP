package logic;

import java.nio.ByteBuffer;
import java.util.Arrays;

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

  enum State {
    VIRGIN,
    AWAITING_RESPONSE,
    ACK_RECEIVED
  }

  private short length;
  private PacketType type;
  private int packetId;
  private Object body;
  private byte[] raw;
  private State state = State.VIRGIN;

  public static final short HEADER_LENGTH = 8;

//  public Packet(PacketType type, Object body) {
//    new Packet(type, -1, body);
//  }

  public Packet(PacketType type, int packetId, Object body) {
    encode(type, packetId, body);
  }

  public Packet(byte[] raw) {
    decode(raw);
  }

  private void encode(PacketType packetType, int packetId, Object body) {
    short length = HEADER_LENGTH;
    byte[] data = null;

    switch(packetType) {
      case DATA:
        byte[] stringData = (byte[])body;
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
    byteBuffer.putShort((short)packetType.ordinal());
    byteBuffer.putInt(packetId);
    byteBuffer.put(data);

    raw = byteBuffer.array();
    setLocalVariables(length, packetType, packetId, body, raw);
  }

  private void decode(byte[] raw) {
    ByteBuffer bb = ByteBuffer.wrap(raw);

    short length = bb.getShort();
    PacketType packetType = PacketType.values()[bb.getShort()];
    int packetId = bb.getInt();

    Object body = null;

    switch(packetType) {
      case DATA:
        body = Arrays.copyOfRange(raw, HEADER_LENGTH, length);
        break;
      case SIGNAL:
        body = Signal.values()[(ByteBuffer.wrap(raw, HEADER_LENGTH, 2).getShort())];
        break;
    }

    setLocalVariables(length, packetType, packetId, body, raw);
  }

  private void setLocalVariables(short length, PacketType type, int packetId, Object body,
                                 byte[] raw) {
    this.length = length;
    this.type = type;
    this.packetId = packetId;
    this.body = body;
    this.raw = raw;
  }

  public short getLength() {
    return length;
  }

  public PacketType getType() {
    return type;
  }

  public int getPacketId() {
    return packetId;
  }

  public Object getBody() {
    return body;
  }

  public byte[] getRaw() {
    return raw;
  }

  @Override
  public String toString() {
    switch(type) {
      case DATA:
        return new String((byte[])body);
      case SIGNAL:
        return ((Signal)body).toString();
    }
    return null;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }
}

