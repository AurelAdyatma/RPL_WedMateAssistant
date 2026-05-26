package com.rplbo.app.rpl_wedmateassistant.controller;

// Import kelas JavaFX untuk membangun tampilan dan navigasi
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

// Import IOException untuk menangani error saat memuat file FXML
import java.io.IOException;

/**
 * Controller untuk halaman Login Admin (Login.fxml).
 *
 * <p>Halaman ini memvalidasi kredensial admin dengan pencocokan sederhana
 * (hardcoded: username = "admin", password = "admin"). Jika berhasil,
 * pengguna diarahkan ke {@code AdminPanel.fxml}.</p>
 *
 * <p>Tanggung jawab:</p>
 * <ul>
 *   <li>Menangani input username dan password dari pengguna</li>
 *   <li>Memvalidasi kredensial login admin</li>
 *   <li>Menampilkan pesan error jika login gagal</li>
 *   <li>Mengarahkan ke halaman Admin Panel jika login berhasil</li>
 *   <li>Menyediakan tombol kembali ke halaman Welcome</li>
 * </ul>
 */
public class LoginController {

    // ── FXML Injections ───────────────────────────────────────────────────────

    /** Field input untuk username admin */
    @FXML private TextField txtUsername;
    /** Field input untuk password admin (karakter disembunyikan) */
    @FXML private PasswordField txtPassword;
    /** Tombol untuk melakukan proses login */
    @FXML private Button btnLogin;
    /** Label untuk menampilkan pesan error ketika login gagal */
    @FXML private Label lblError;

    /** Tombol untuk kembali ke halaman Welcome */
    @FXML private Button btnKembali;

    /**
     * Method initialize() dipanggil otomatis oleh JavaFX setelah FXML dimuat.
     * Menyembunyikan label error agar tidak terlihat saat pertama kali halaman dibuka.
     */
    @FXML
    public void initialize() {
        // Sembunyikan label error saat halaman pertama kali ditampilkan
        lblError.setVisible(false);
        // Pastikan label error tidak memakan ruang layout
        lblError.setManaged(false);
    }

    /**
     * Handler untuk tombol "Kembali".
     * Mengembalikan pengguna ke halaman Welcome (Welcome.fxml).
     */
    @FXML
    private void handleKembali() {
        try {
            // Mendapatkan stage (window) saat ini dari scene tombol login
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            // Memuat file FXML halaman Welcome
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rplbo/app/rpl_wedmateassistant/view/Welcome.fxml"));
            Parent root = loader.load();
            // Membuat scene baru dengan ukuran 1280x800
            Scene scene = new Scene(root, 1280, 800);
            // Mengganti scene pada stage yang sama
            stage.setScene(scene);
            // Mengatur judul window
            stage.setTitle("Welcome to WedMate");
            // Memposisikan window di tengah layar
            stage.centerOnScreen();
        } catch (IOException e) {
            // Mencetak stack trace jika terjadi error saat memuat FXML
            e.printStackTrace();
        }
    }

    /**
     * Handler untuk tombol "Login".
     * Memvalidasi kredensial yang diinputkan oleh pengguna.
     * Jika username = "admin" dan password = "admin", arahkan ke Admin Panel.
     * Jika salah, tampilkan pesan error di label.
     */
    @FXML
    private void handleLogin() {
        // Ambil dan trim input username, handle null
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        // Ambil dan trim input password, handle null
        String password = txtPassword.getText() == null ? "" : txtPassword.getText().trim();

        // Validasi: cek apakah field kosong
        if (username.isEmpty() || password.isEmpty()) {
            // Tampilkan pesan error jika ada field yang kosong
            lblError.setText("Username dan Password tidak boleh kosong!");
            lblError.setVisible(true);
            lblError.setManaged(true);
            return;
        }

        try {
            // Validasi kredensial admin (hardcoded untuk kesederhanaan)
            if (username.equalsIgnoreCase("admin") && password.equals("admin")) {
                // Login berhasil — arahkan ke halaman Admin Panel
                loadView("/com/rplbo/app/rpl_wedmateassistant/view/AdminPanel.fxml", "Admin Panel");
            } else {
                // Login gagal — tampilkan pesan error
                lblError.setText("Username atau Password salah!");
                lblError.setVisible(true);
                lblError.setManaged(true);
            }
        } catch (IOException e) {
            // Tangani error jika file FXML gagal dimuat
            e.printStackTrace();
            lblError.setText("Gagal memuat tampilan!");
            lblError.setVisible(true);
            lblError.setManaged(true);
        }
    }

    /**
     * Helper method untuk memuat file FXML dan mengganti scene pada window saat ini.
     *
     * @param fxmlFile path relatif ke file FXML yang akan dimuat
     * @param title    judul yang akan ditampilkan di title bar window
     * @throws IOException jika file FXML tidak ditemukan atau gagal dimuat
     */
    private void loadView(String fxmlFile, String title) throws IOException {
        // Mendapatkan stage saat ini dari scene tombol login
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        // Memuat file FXML yang ditentukan
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        // Membuat scene baru dengan ukuran 1280x800 dan memasangnya ke stage
        Scene scene = new Scene(root, 1280, 800);
        stage.setScene(scene);
        // Mengatur judul window sesuai parameter
        stage.setTitle(title);
        // Memposisikan window di tengah layar
        stage.centerOnScreen();
        // Menampilkan window
        stage.show();
    }
}
