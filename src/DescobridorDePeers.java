import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DescobridorDePeers {
    private final int minhaPortaTCP;
    private final int portaEscutaUDP;
    private final int portaBroadcast;
    private final Set<Peer> peersConhecidos = Collections.synchronizedSet(new HashSet<>());

    public DescobridorDePeers(int minhaPortaTCP, int portaEscutaUDP, int portaBroadcast) {
        this.minhaPortaTCP = minhaPortaTCP;
        this.portaEscutaUDP = portaEscutaUDP;
        this.portaBroadcast = portaBroadcast;
    }

    public void iniciar() {
        new Thread(this::ouvirBroadcasts).start();
        new Thread(this::enviarBroadcasts).start();
    }

    private void ouvirBroadcasts() {
        try (DatagramSocket socket = new DatagramSocket(portaEscutaUDP, InetAddress.getByName("0.0.0.0"))) {
            socket.setBroadcast(true);
            byte[] buffer = new byte[256];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String recebido = new String(packet.getData(), 0, packet.getLength());
                if (!recebido.equals("PORTA_TCP:" + minhaPortaTCP)) {
                    String[] parts = recebido.split(":");
                    if (parts.length == 2 && "PORTA_TCP".equals(parts[0])) {
                        int peerPort = Integer.parseInt(parts[1]);
                        peersConhecidos.add(new Peer(packet.getAddress(), peerPort));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enviarBroadcasts() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            byte[] buffer = ("PORTA_TCP:" + minhaPortaTCP).getBytes();
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastAddress, portaBroadcast);
                socket.send(packet);
                Thread.sleep(5000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Set<Peer> getPeersConhecidos() {
        return peersConhecidos;
    }
}
