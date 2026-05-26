// Import Platform untuk menginisialisasi JavaFX runtime tanpa membuka window
import javafx.application.Platform;
// Import FXMLLoader untuk memuat dan memvalidasi file FXML
import javafx.fxml.FXMLLoader;
// Import Parent sebagai root node dari scene graph FXML
import javafx.scene.Parent;

/**
 * Kelas utilitas untuk menguji apakah semua file FXML dapat dimuat tanpa error.
 *
 * <p>Kelas ini memvalidasi bahwa semua file tampilan (FXML) di folder
 * {@code /view/} dapat di-parse oleh JavaFX FXMLLoader tanpa menghasilkan
 * exception. Ini berguna untuk mendeteksi kesalahan sintaks FXML, controller
 * yang hilang, atau resource yang tidak ditemukan sebelum menjalankan
 * aplikasi secara penuh.</p>
 *
 * <p>File FXML yang divalidasi:</p>
 * <ul>
 *   <li>Welcome.fxml — halaman selamat datang / pemilihan role</li>
 *   <li>Login.fxml — halaman login admin</li>
 *   <li>AdminPanel.fxml — panel admin untuk CRUD data</li>
 *   <li>ChatView.fxml — tampilan chatbot untuk pengguna</li>
 * </ul>
 */
public class FxmlTester {
    /**
     * Method main — menjalankan validasi semua file FXML.
     * Menggunakan {@code Platform.startup()} untuk menginisialisasi JavaFX toolkit
     * tanpa membuka window, lalu mencoba memuat setiap file FXML.
     * Jika semua berhasil, mencetak "ALL OK" dan keluar dengan kode 0.
     * Jika ada yang gagal, mencetak stack trace dan keluar dengan kode 1.
     *
     * @param args argumen command-line (tidak digunakan)
     */
    public static void main(String[] args) {
        // Inisialisasi JavaFX toolkit tanpa membuat window (headless mode)
        Platform.startup(() -> {
            try {
                // Tes memuat file Welcome.fxml (halaman selamat datang)
                System.out.println("Testing Welcome.fxml");
                FXMLLoader.load(FxmlTester.class.getResource("/com/rplbo/app/rpl_wedmateassistant/view/Welcome.fxml"));
                // Tes memuat file Login.fxml (halaman login admin)
                System.out.println("Testing Login.fxml");
                FXMLLoader.load(FxmlTester.class.getResource("/com/rplbo/app/rpl_wedmateassistant/view/Login.fxml"));
                // Tes memuat file AdminPanel.fxml (panel admin CRUD)
                System.out.println("Testing AdminPanel.fxml");
                FXMLLoader.load(FxmlTester.class.getResource("/com/rplbo/app/rpl_wedmateassistant/view/AdminPanel.fxml"));
                // Tes memuat file ChatView.fxml (tampilan chatbot)
                System.out.println("Testing ChatView.fxml");
                FXMLLoader.load(FxmlTester.class.getResource("/com/rplbo/app/rpl_wedmateassistant/view/ChatView.fxml"));
                // Semua file FXML berhasil dimuat tanpa error
                System.out.println("ALL OK");
                // Keluar dengan kode sukses
                System.exit(0);
            } catch (Exception e) {
                // Cetak detail error jika ada file FXML yang gagal dimuat
                e.printStackTrace();
                // Keluar dengan kode error
                System.exit(1);
            }
        });
    }
}
