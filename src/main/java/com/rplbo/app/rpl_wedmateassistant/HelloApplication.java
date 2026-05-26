package com.rplbo.app.rpl_wedmateassistant;

// Import kelas DatabaseManager untuk mengelola koneksi database SQLite
import com.rplbo.app.rpl_wedmateassistant.database.DatabaseManager;
// Import kelas DataSeeder untuk mengisi data awal ke database
import com.rplbo.app.rpl_wedmateassistant.database.DataSeeder;
// Import kelas Application sebagai base class aplikasi JavaFX
import javafx.application.Application;
// Import FXMLLoader untuk memuat file FXML sebagai tampilan UI
import javafx.fxml.FXMLLoader;
// Import Scene untuk membuat scene/layar yang akan ditampilkan di stage
import javafx.scene.Scene;
// Import Stage sebagai window utama aplikasi JavaFX
import javafx.stage.Stage;

// Import IOException untuk menangani kesalahan saat memuat file FXML
import java.io.IOException;

/**
 * Kelas utama aplikasi WedMate Assistant yang meng-extend {@link Application} dari JavaFX.
 *
 * <p>Kelas ini bertanggung jawab untuk:</p>
 * <ul>
 *   <li>Menginisialisasi database SQLite saat aplikasi pertama kali dijalankan</li>
 *   <li>Mengisi data awal (seed) ke database jika tabel masih kosong</li>
 *   <li>Memuat tampilan Welcome (Welcome.fxml) sebagai halaman pertama</li>
 *   <li>Menutup koneksi database secara aman saat aplikasi ditutup</li>
 * </ul>
 *
 * <p>Aplikasi dijalankan melalui kelas {@link Launcher} yang memanggil
 * {@code Application.launch(HelloApplication.class, args)}.</p>
 */
public class HelloApplication extends Application {

    /**
     * Method start() dipanggil otomatis oleh JavaFX saat aplikasi diluncurkan.
     * Bertugas menginisialisasi database, memuat tampilan awal, dan menampilkan window.
     *
     * @param stage stage/window utama yang disediakan oleh JavaFX runtime
     * @throws IOException jika file Welcome.fxml gagal dimuat
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Inisialisasi database: membuat tabel-tabel jika belum ada
        DatabaseManager.getInstance().initDB();
        // Mengisi data awal (pakaian, paket sewa, knowledge base) jika tabel masih kosong
        new DataSeeder().seed();

        // Memuat file FXML untuk halaman Welcome sebagai tampilan pertama
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("/com/rplbo/app/rpl_wedmateassistant/view/Welcome.fxml")
        );

        // Membuat scene dengan ukuran 1280x800 pixel dari FXML yang sudah dimuat
        Scene scene = new Scene(fxmlLoader.load(), 1280, 800);
        // Mengatur judul window aplikasi
        stage.setTitle("Welcome to WedMate");
        // Memasang scene ke stage
        stage.setScene(scene);
        // Memposisikan window di tengah layar
        stage.centerOnScreen();
        // Menampilkan window ke pengguna
        stage.show();
    }

    /**
     * Method stop() dipanggil otomatis oleh JavaFX saat aplikasi ditutup.
     * Bertugas menutup koneksi database SQLite secara aman untuk mencegah kebocoran resource.
     */
    @Override
    public void stop() {
        // Menutup koneksi database SQLite agar tidak terjadi resource leak
        DatabaseManager.getInstance().closeConnection();
    }
}
