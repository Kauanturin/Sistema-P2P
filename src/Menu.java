import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;

public class Menu {
    private final DHT dht;
    private final Set<Peer> peers;
    private final FileManager fileManager;

    public Menu(DHT dht, Set<Peer> peers, FileManager fileManager) {
        this.dht = dht;
        this.peers = peers;
        this.fileManager = fileManager;
    }

    public void iniciar() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("===== MENU =====");
            System.out.println("1. Adicionar arquivo local");
            System.out.println("2. Buscar arquivo em peers");
            System.out.println("3. Listar arquivos locais (GUID : nome)");
            System.out.println("4. Listar peers conhecidos");
            System.out.println("5. Sair");
            System.out.print("> ");

            String opcao = scanner.nextLine();
            switch (opcao) {
                case "1":
                    System.out.print("Nome do arquivo: ");
                    try {
                        String nome = scanner.nextLine();
                        String guid = fileManager.adicionarArquivo(nome);
                        dht.put(guid, nome);
                        System.out.println("Arquivo adicionado com GUID: " + guid);
                    } catch (IOException e) {
                        System.out.println("Erro ao adicionar arquivo: " + e.getMessage());
                    }
                    break;

                case "2":
                    System.out.print("Nome do arquivo a buscar: ");
                    String nomeBusca = scanner.nextLine();
                    boolean encontrado = false;

                    for (Peer peer : peers) {
                        try (Socket socket = new Socket(peer.getAddress(), peer.getPort());
                             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                             InputStream is = socket.getInputStream();
                             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

                            pw.println("GET:" + nomeBusca);
                            String resposta = br.readLine();

                            if ("FOUND".equals(resposta)) {
                                File destino = new File(fileManager.getPasta(), nomeBusca);
                                try (FileOutputStream fos = new FileOutputStream(destino)) {
                                    byte[] buffer = new byte[4096];
                                    int bytesRead;
                                    while ((bytesRead = is.read(buffer)) != -1) {
                                        fos.write(buffer, 0, bytesRead);
                                    }
                                }
                                System.out.println("Arquivo baixado de " + peer);
                                encontrado = true;
                                break;
                            } else {
                                System.out.println("Arquivo não encontrado em " + peer);
                            }

                        } catch (IOException e) {
                            System.out.println("Erro com peer " + peer + ": " + e.getMessage());
                        }
                    }
                    if (!encontrado) {
                        System.out.println("Arquivo não encontrado em nenhum peer.");
                    }
                    break;

                case "3":
                    dht.getAll().forEach((guid, nome) -> System.out.println(guid + " : " + nome));
                    break;

                case "4":
                    System.out.println("Peers conhecidos:");
                    peers.forEach(peer -> System.out.println(peer));
                    break;

                case "5":
                    System.exit(0);
                    break;

                default:
                    System.out.println("Opção inválida.");
            }
        }
    }
}
