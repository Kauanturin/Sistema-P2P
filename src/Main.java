import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java Main <portaTCP> <portaEscutaUDP>");
            return;
        }

        int portaTCP = Integer.parseInt(args[0]);
        int portaEscutaUDP = Integer.parseInt(args[1]);
        int portaBroadcast = 6000; // Porta comum para envio de broadcast

        // Inicializa componentes
        FileManager fileManager = new FileManager(portaTCP);
        DHT dht = new DHT();
        DescobridorDePeers dp = new DescobridorDePeers(portaTCP, portaEscutaUDP, portaBroadcast);

        dp.iniciar();

        // Servidor TCP para atender requisições GET:<nomeArquivo>
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(portaTCP)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    new Thread(() -> {
                        try {
                            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);

                            String linha = br.readLine();
                            if (linha != null && linha.startsWith("GET:")) {
                                String nomeArquivo = linha.substring(4);
                                File arquivo = fileManager.buscarArquivo(nomeArquivo);
                                if (arquivo != null && arquivo.exists()) {
                                    pw.println("FOUND");
                                    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(arquivo));
                                         OutputStream os = socket.getOutputStream()) {
                                        byte[] buffer = new byte[4096];
                                        int count;
                                        while ((count = bis.read(buffer)) > 0) {
                                            os.write(buffer, 0, count);
                                        }
                                        os.flush();
                                    }
                                } else {
                                    pw.println("NOTFOUND");
                                }
                            }
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Inicia o menu com os dados
        Menu menu = new Menu(dht, dp.getPeersConhecidos(), fileManager);
        menu.iniciar();
    }
}
