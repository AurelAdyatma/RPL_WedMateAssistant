package com.rplbo.app.rpl_wedmateassistant;

// Import kelas Application dari JavaFX untuk menjalankan aplikasi
import javafx.application.Application;

/**
 * Kelas Launcher — titik masuk (entry point) utama aplikasi WedMate Assistant.
 *
 * <p>Kelas ini diperlukan karena JavaFX membutuhkan kelas terpisah
 * (bukan subclass dari {@link Application}) sebagai main class ketika
 * aplikasi dijalankan menggunakan modul system atau dari JAR.</p>
 *
 * <p>Method {@code main()} di sini mendelegasikan peluncuran ke
 * {@link HelloApplication} yang merupakan kelas JavaFX Application sesungguhnya.</p>
 */
public class Launcher {
    /**
     * Method main — titik masuk program.
     * Memanggil {@code Application.launch()} untuk memulai lifecycle JavaFX
     * dan menjalankan {@link HelloApplication#start(javafx.stage.Stage)}.
     *
     * @param args argumen command-line yang diteruskan ke JavaFX runtime
     */
    public static void main(String[] args) {
        // Meluncurkan aplikasi JavaFX dengan HelloApplication sebagai kelas utama
        Application.launch(HelloApplication.class, args);
    }
}
