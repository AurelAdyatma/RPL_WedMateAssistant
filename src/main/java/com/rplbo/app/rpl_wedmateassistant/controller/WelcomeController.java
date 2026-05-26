package com.rplbo.app.rpl_wedmateassistant.controller;

// Import model User untuk membuat objek pengguna mock
import com.rplbo.app.rpl_wedmateassistant.model.User;
// Import kelas-kelas JavaFX untuk navigasi antar halaman
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

// Import IOException untuk menangani error saat memuat file FXML
import java.io.IOException;

/**
 * Controller untuk halaman Welcome / Pilih Role (Welcome.fxml).
 *
 * <p>Halaman ini menampilkan dua pilihan role kepada pengguna:</p>
 * <ul>
 *   <li><b>User (Pengguna)</b> — langsung masuk ke tampilan chatbot WedMate</li>
 *   <li><b>Admin</b> — diarahkan ke halaman login terlebih dahulu</li>
 * </ul>
 *
 * <p>Pengguna memilih role dengan mengklik kartu (card) yang tersedia,
 * lalu menekan tombol "Lanjutkan" untuk navigasi ke halaman berikutnya.</p>
 */
public class WelcomeController {

    // ── FXML Injections ───────────────────────────────────────────────────────

    /** Kartu pilihan role "User" yang bisa diklik oleh pengguna */
    @FXML private VBox cardUser;
    /** Kartu pilihan role "Admin" yang bisa diklik oleh pengguna */
    @FXML private VBox cardAdmin;

    // ── State ─────────────────────────────────────────────────────────────────

    /** Flag untuk menandai role yang dipilih. Default ke User (false = User, true = Admin) */
    // Default ke User
    private boolean isAdminSelected = false;

    /**
     * Method initialize() dipanggil otomatis oleh JavaFX setelah FXML dimuat.
     * Mengatur tampilan awal kartu agar menunjukkan "User" sebagai pilihan default.
     */
    @FXML
    public void initialize() {
        // Set awal
        updateSelection();
    }

    /**
     * Handler ketika kartu "User" diklik.
     * Mengubah pilihan aktif menjadi User dan memperbarui style kartu.
     *
     * @param event event klik mouse pada kartu User
     */
    @FXML
    private void handleUserClick(MouseEvent event) {
        // Set pilihan ke User
        isAdminSelected = false;
        // Perbarui tampilan visual kartu (highlight kartu yang dipilih)
        updateSelection();
    }

    /**
     * Handler ketika kartu "Admin" diklik.
     * Mengubah pilihan aktif menjadi Admin dan memperbarui style kartu.
     *
     * @param event event klik mouse pada kartu Admin
     */
    @FXML
    private void handleAdminClick(MouseEvent event) {
        // Set pilihan ke Admin
        isAdminSelected = true;
        // Perbarui tampilan visual kartu (highlight kartu yang dipilih)
        updateSelection();
    }

    /**
     * Memperbarui style visual kedua kartu berdasarkan pilihan yang aktif.
     * Kartu yang dipilih mendapatkan border berwarna dan background highlight,
     * sementara kartu lainnya mendapatkan border abu-abu.
     */
    private void updateSelection() {
        if (isAdminSelected) {
            // Admin dipilih: beri highlight oranye pada kartu Admin
            cardAdmin.setStyle("-fx-background-color: #F4F9FD; -fx-background-radius: 12; -fx-border-color: #D97706; -fx-border-radius: 12; -fx-border-width: 3;");
            // Reset style kartu User ke default (tidak dipilih)
            cardUser.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-border-width: 1;");
        } else {
            // User dipilih: beri highlight biru pada kartu User
            cardUser.setStyle("-fx-background-color: #F4F9FD; -fx-background-radius: 12; -fx-border-color: #3B82F6; -fx-border-radius: 12; -fx-border-width: 3;");
            // Reset style kartu Admin ke default (tidak dipilih)
            cardAdmin.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-border-width: 1;");
        }
    }

    /**
     * Handler untuk tombol "Lanjutkan".
     * Mengarahkan pengguna ke halaman yang sesuai berdasarkan role yang dipilih:
     * <ul>
     *   <li>Admin → Login.fxml (harus login dulu)</li>
     *   <li>User → ChatView.fxml (langsung ke chatbot dengan user mock)</li>
     * </ul>
     *
     * @param event event klik pada tombol Lanjutkan
     */
    @FXML
    private void handleLanjutkan(ActionEvent event) {
        try {
            // Mendapatkan stage saat ini dari sumber event (tombol yang diklik)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            if (isAdminSelected) {
                // ── Navigasi ke halaman Login Admin ───────────────────────────
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rplbo/app/rpl_wedmateassistant/view/Login.fxml"));
                Parent root = loader.load();
                
                // Membuat scene baru dan memasangnya ke stage
                Scene scene = new Scene(root, 1280, 800);
                stage.setScene(scene);
                stage.setTitle("WedMate - Login Admin");
            } else {
                // ── Navigasi ke halaman Chatbot (User) ───────────────────────
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rplbo/app/rpl_wedmateassistant/view/ChatView.fxml"));
                Parent root = loader.load();
                
                // Mendapatkan controller ChatView dan menginjeksi user mock
                ChatController chatController = loader.getController();
                // Membuat objek User dummy untuk sesi chatbot (tanpa perlu login)
                User mockUser = new User(1, "user", "password", "Pengguna", "user@example.com", "08123456789");
                // Menginjeksi user ke ChatController agar sesi dapat dimulai
                chatController.setUserLogin(mockUser);
                
                // Membuat scene baru dan memasangnya ke stage
                Scene scene = new Scene(root, 1280, 800);
                stage.setScene(scene);
                stage.setTitle("WedMate Assistant - Chatbot");
            }
            // Memposisikan window di tengah layar
            stage.centerOnScreen();
            // Menampilkan window
            stage.show();
        } catch (IOException e) {
            // Mencetak stack trace jika terjadi error saat memuat FXML
            e.printStackTrace();
        }
    }
}
