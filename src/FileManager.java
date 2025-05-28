import java.io.*;
import java.nio.file.*;
import java.util.UUID;

public class FileManager {
    private final String pasta;

    public FileManager(int porta) {
        this.pasta = "peer_" + porta;
        File dir = new File(pasta);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public String adicionarArquivo(String nome) throws IOException {
        File origem = new File(nome);
        if (!origem.exists()) {
            throw new FileNotFoundException("Arquivo não encontrado");
        }
        String guid = UUID.randomUUID().toString();
        Path destino = Paths.get(pasta, origem.getName());
        Files.copy(origem.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        return guid;
    }

    public File buscarArquivo(String nome) {
        File f = new File(pasta, nome);
        return f.exists() ? f : null;
    }

    public String getPasta() {
        return pasta;
    }
}
