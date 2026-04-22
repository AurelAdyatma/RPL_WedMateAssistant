package com.rplbo.app.rpl_wedmateassistant.controller;

import com.rplbo.app.rpl_wedmateassistant.database.DatabaseManager;
import com.rplbo.app.rpl_wedmateassistant.database.DataSeeder;
import com.rplbo.app.rpl_wedmateassistant.database.KnowledgeBaseDAO;
import com.rplbo.app.rpl_wedmateassistant.database.PakaianDAO;
import com.rplbo.app.rpl_wedmateassistant.engine.ChatbotEngine;
import com.rplbo.app.rpl_wedmateassistant.engine.RegexMatcher.Kategori;
import com.rplbo.app.rpl_wedmateassistant.model.EntriKnowledge;
import com.rplbo.app.rpl_wedmateassistant.model.PakaianWedding;
import com.rplbo.app.rpl_wedmateassistant.model.Pesan;
import com.rplbo.app.rpl_wedmateassistant.model.Sesi;
import com.rplbo.app.rpl_wedmateassistant.model.User;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.controlsfx.control.Notifications;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller untuk tampilan percakapan utama WedMate Assistant (ChatView.fxml).
 *
 * <h3>Tanggung jawab:</h3>
 * <ul>
 *   <li>Membangun bubble chat secara programatik di dalam {@code chatBox}</li>
 *   <li>Mengelola multi-sesi di sidebar {@code listSesi}</li>
 *   <li>Meneruskan input ke {@link ChatbotEngine} dan menampilkan respons bot</li>
 *   <li>Menampilkan quick reply buttons setelah pesan bot pertama</li>
 *   <li>Notifikasi via ControlsFX saat sesi baru dibuat atau terjadi error</li>
 * </ul>
 */
public class ChatController {

    // ── FXML Injections ───────────────────────────────────────────────────────

    @FXML private SplitPane     splitPane;

    @FXML private ScrollPane    scrollChat;
    @FXML private VBox          chatBox;
    @FXML private HBox          quickReplyBox;
    @FXML private Label         lblStatus;

    @FXML private TextField     txtInput;
    @FXML private Button        btnKirim;
    @FXML private Button        btnKembali;

    // ── State ─────────────────────────────────────────────────────────────────

    private final ChatbotEngine      engine           = new ChatbotEngine();
    private final KnowledgeBaseDAO   knowledgeDAO     = new KnowledgeBaseDAO();
    private final PakaianDAO         pakaianDAO       = new PakaianDAO();
    private final DateTimeFormatter  TIME_FMT         =
            DateTimeFormatter.ofPattern("HH:mm");

    /** Sesi yang sedang aktif ditampilkan. */
    private Sesi sesiAktif;



    /** User yang sedang login (di-inject dari LoginController). */
    private User userLogin;

    /** True setelah quick reply sudah ditampilkan (hanya muncul sekali). */
    private boolean quickReplyShown = false;

    // ── Format helper ─────────────────────────────────────────────────────────

    private static final String[] QUICK_REPLIES = {
        "Lihat Busana",
        "Harga & Paket",
        "Cek Ketersediaan",
        "Info Toko"
    };

    // ═════════════════════════════════════════════════════════════════════════
    // FXML initialize
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        // 1. Inisialisasi database & seed jika kosong
        DatabaseManager.getInstance().initDB();
        new DataSeeder().seed();

        // 2. Load knowledge base ke engine
        muatKnowledgeBase();

        // 3. Buat sesi pertama
        buatSesiBaru();

        // 4. Auto-scroll saat konten chatBox bertambah
        chatBox.heightProperty().addListener((obs, oldH, newH) ->
            Platform.runLater(() ->
                scrollChat.setVvalue(1.0)
            )
        );

        // 5. Tampilkan salam awal bot
        tampilkanSalamAwal();

        // 6. Enter key di input → kirim
        txtInput.setOnAction(e -> handleKirimPesan());
    }

    @FXML
    private void handleKembali() {
        try {
            Stage stage = (Stage) btnKirim.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rplbo/app/rpl_wedmateassistant/view/Welcome.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 600);
            stage.setScene(scene);
            stage.setTitle("Welcome to WedMate");
            stage.centerOnScreen();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Public API — dipanggil dari LoginController
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Menerima user yang sudah login dari LoginController.
     * Harus dipanggil sebelum tampilan ditampilkan.
     *
     * @param user objek User yang berhasil login
     */
    public void setUserLogin(User user) {
        this.userLogin = user;
        if (sesiAktif != null && user != null) {
            sesiAktif.setUserId(user.getId());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // FXML Handlers
    // ═════════════════════════════════════════════════════════════════════════

    /** Kirim pesan saat tombol diklik atau Enter ditekan. */
    @FXML
    private void handleKirimPesan() {
        String teks = txtInput.getText().trim();
        if (teks.isEmpty()) return;

        // 1. Tampilkan bubble user
        tambahBubbleUser(teks, LocalDateTime.now());
        txtInput.clear();

        // 2. Sembunyikan quick reply setelah interaksi pertama
        sembunyikanQuickReply();

        // 3. Tampilkan indikator mengetik bot
        Label typingLabel = buatTypingLabel();
        chatBox.getChildren().add(typingLabel);

        // 4. Tunda respons bot 800ms agar terasa natural
        PauseTransition jeda = new PauseTransition(Duration.millis(800));
        jeda.setOnFinished(e -> {
            chatBox.getChildren().remove(typingLabel);
            prosesInputDanBalas(teks);
        });
        jeda.play();
    }



    // ═════════════════════════════════════════════════════════════════════════
    // Private — Sesi Management
    // ═════════════════════════════════════════════════════════════════════════

    private void buatSesiBaru() {
        sesiAktif = new Sesi();
        sesiAktif.setId(1);
        if (userLogin != null) sesiAktif.setUserId(userLogin.getId());

        // Bersihkan area chat dan reset quick reply
        chatBox.getChildren().clear();
        quickReplyShown = false;
        quickReplyBox.getChildren().clear();
        quickReplyBox.setVisible(false);
        quickReplyBox.setManaged(false);
    }



    /** Memuat knowledge base dari DB dan inject ke engine. */
    private void muatKnowledgeBase() {
        try {
            List<EntriKnowledge> entri = knowledgeDAO.findAll();
            engine.setDaftarEntri(entri != null ? entri : new ArrayList<>());
            System.out.println("[ChatController] Knowledge base dimuat: " + (entri != null ? entri.size() : 0) + " entri.");
        } catch (Exception e) {
            System.err.println("[ChatController] Gagal load knowledge base: " + e.getMessage());
            engine.setDaftarEntri(new ArrayList<>());
        }
        try {
            List<PakaianWedding> pakaian = pakaianDAO.findAll();
            engine.setDaftarPakaian(pakaian != null ? pakaian : new ArrayList<>());
            System.out.println("[ChatController] Pakaian dimuat: " + (pakaian != null ? pakaian.size() : 0) + " item.");
        } catch (Exception e) {
            System.err.println("[ChatController] Gagal load pakaian: " + e.getMessage());
            engine.setDaftarPakaian(new ArrayList<>());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Private — Chatbot Processing
    // ═════════════════════════════════════════════════════════════════════════

    /** Memproses input pengguna via engine dan menampilkan balasan bot. */
    private void prosesInputDanBalas(String inputPengguna) {
        // Simpan pesan user ke sesi
        Pesan pesanUser = new Pesan(sesiAktif.getId(), inputPengguna, false);
        sesiAktif.getDaftarPesan().add(pesanUser);

        // Proses via engine
        Pesan pesanBot = engine.prosesPesan(inputPengguna, sesiAktif);

        // Tampilkan bubble bot
        if (pesanBot != null && pesanBot.getIsiPesan() != null) {
            tambahBubbleBot(pesanBot.getIsiPesan(), pesanBot.getWaktuKirim());
        }
    }

    /** Menampilkan salam awal bot saat sesi baru dimulai. */
    private void tampilkanSalamAwal() {
        String salam = "Halo! Selamat datang di WedMate Assistant.\n" +
                       "Saya siap membantu Anda menemukan busana pernikahan impian!\n" +
                       "Ada yang bisa saya bantu hari ini?";

        Pesan pesanSalam = new Pesan(sesiAktif.getId(), salam, true);
        sesiAktif.getDaftarPesan().add(pesanSalam);

        tambahBubbleBot(salam, LocalDateTime.now());
        tampilkanQuickReply();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Private — Bubble Builder
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Membuat dan menambahkan bubble pesan USER ke chatBox.
     * Rata kanan, warna coklat (#A0522D).
     */
    private void tambahBubbleUser(String teks, LocalDateTime waktu) {
        // Label isi pesan
        Label lblPesan = new Label(teks);
        lblPesan.setStyle("-fx-background-color: #2E3A3F; -fx-text-fill: white; -fx-padding: 12 18; -fx-background-radius: 16 16 0 16; -fx-font-size: 14px;");
        lblPesan.setMaxWidth(400);
        lblPesan.setWrapText(true);

        // Timestamp
        Label lblTime = new Label(waktu != null ? waktu.format(TIME_FMT) : "");
        lblTime.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-padding: 2 4 0 0;");

        // VBox: pesan + timestamp (keduanya rata kanan)
        VBox vBox = new VBox(2, lblPesan, lblTime);
        vBox.setAlignment(Pos.CENTER_RIGHT);

        // Wrapper rata kanan
        HBox wrapper = new HBox(vBox);
        wrapper.setStyle("-fx-padding: 0 0 0 50;");
        wrapper.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(vBox, Priority.SOMETIMES);

        chatBox.getChildren().add(wrapper);
    }

    /**
     * Membuat dan menambahkan bubble pesan BOT ke chatBox.
     * Rata kiri, warna abu gelap (#2C2C4A), dengan avatar kecil.
     */
    private void tambahBubbleBot(String teks, LocalDateTime waktu) {
        // Avatar inisial bot
        StackPane avatar = new StackPane(new Label("W"));
        avatar.setStyle("-fx-background-color: #D97706; -fx-background-radius: 18; -fx-min-width: 36; -fx-min-height: 36;");
        ((Label) avatar.getChildren().get(0)).setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        // Label isi pesan
        Label lblPesan = new Label(teks);
        lblPesan.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #1E293B; -fx-padding: 14 18; -fx-background-radius: 16 16 16 0; -fx-font-size: 14px; -fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-border-radius: 16 16 16 0;");
        lblPesan.setMaxWidth(420);
        lblPesan.setWrapText(true);

        // Timestamp
        Label lblTime = new Label(waktu != null ? waktu.format(TIME_FMT) : "");
        lblTime.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-padding: 2 0 0 4;");

        // VBox: pesan + timestamp
        VBox vBox = new VBox(2, lblPesan, lblTime);
        vBox.setAlignment(Pos.CENTER_LEFT);

        // Wrapper rata kiri: avatar + konten
        HBox wrapper = new HBox(12, avatar, vBox);
        wrapper.setStyle("-fx-padding: 0 50 0 0;");
        wrapper.setAlignment(Pos.TOP_LEFT);

        chatBox.getChildren().add(wrapper);
    }

    /** Membuat label "WedMate sedang mengetik..." sebagai placeholder. */
    private Label buatTypingLabel() {
        Label lbl = new Label("WedMate sedang mengetik...");
        lbl.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px; -fx-font-style: italic; -fx-padding: 0 0 0 48;");
        return lbl;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Private — Quick Reply
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Menampilkan 4 tombol quick reply di bawah pesan bot pertama.
     * Hanya muncul sekali per sesi.
     */
    private void tampilkanQuickReply() {
        if (quickReplyShown) return;
        quickReplyShown = true;

        quickReplyBox.getChildren().clear();

        for (String label : QUICK_REPLIES) {
            Button btn = new Button(label);
            btn.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #D97706; -fx-border-color: #D97706; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 6 15; -fx-font-size: 13px; -fx-cursor: hand;");
            btn.setOnAction(e -> {
                txtInput.setText(label);
                handleKirimPesan();
                sembunyikanQuickReply();
            });
            quickReplyBox.getChildren().add(btn);
        }

        quickReplyBox.setVisible(true);
        quickReplyBox.setManaged(true);
    }

    /** Menyembunyikan quick reply box setelah pengguna berinteraksi. */
    private void sembunyikanQuickReply() {
        quickReplyBox.setVisible(false);
        quickReplyBox.setManaged(false);
    }
}
