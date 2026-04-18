package com.rplbo.app.rpl_wedmateassistant.controller;

import com.rplbo.app.rpl_wedmateassistant.database.DatabaseManager;
import com.rplbo.app.rpl_wedmateassistant.database.KnowledgeBaseDAO;
import com.rplbo.app.rpl_wedmateassistant.engine.ChatbotEngine;
import com.rplbo.app.rpl_wedmateassistant.engine.RegexMatcher.Kategori;
import com.rplbo.app.rpl_wedmateassistant.model.EntriKnowledge;
import com.rplbo.app.rpl_wedmateassistant.model.Pesan;
import com.rplbo.app.rpl_wedmateassistant.model.Sesi;
import com.rplbo.app.rpl_wedmateassistant.model.User;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
    @FXML private ListView<String> listSesi;
    @FXML private Button        btnSesiBar;
    @FXML private Button        btnHapusSesi;

    @FXML private ScrollPane    scrollChat;
    @FXML private VBox          chatBox;
    @FXML private HBox          quickReplyBox;
    @FXML private Label         lblStatus;

    @FXML private TextField     txtInput;
    @FXML private Button        btnKirim;

    // ── State ─────────────────────────────────────────────────────────────────

    private final ChatbotEngine      engine           = new ChatbotEngine();
    private final KnowledgeBaseDAO   knowledgeDAO     = new KnowledgeBaseDAO();
    private final DateTimeFormatter  TIME_FMT         =
            DateTimeFormatter.ofPattern("HH:mm");

    /** Sesi yang sedang aktif ditampilkan. */
    private Sesi sesiAktif;

    /** Daftar semua sesi dalam run ini. */
    private final List<Sesi> daftarSesi = new ArrayList<>();

    /** User yang sedang login (di-inject dari LoginController). */
    private User userLogin;

    /** True setelah quick reply sudah ditampilkan (hanya muncul sekali). */
    private boolean quickReplyShown = false;

    // ── Format helper ─────────────────────────────────────────────────────────

    private static final String[] QUICK_REPLIES = {
        "👗 Lihat Busana",
        "💰 Harga & Paket",
        "📅 Cek Ketersediaan",
        "📍 Info Toko"
    };

    // ═════════════════════════════════════════════════════════════════════════
    // FXML initialize
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        // 1. Inisialisasi database
        DatabaseManager.getInstance().initDB();

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

    /** Membuat sesi chat baru dari sidebar. */
    @FXML
    private void handleSesiBar() {
        buatSesiBaru();
        tampilkanSalamAwal();

        Notifications.create()
            .title("Sesi Baru Dibuat")
            .text("Percakapan baru dimulai 💬")
            .showInformation();
    }

    /** Memilih sesi dari sidebar (tampilkan ulang riwayat). */
    @FXML
    private void handlePilihSesi() {
        int idx = listSesi.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= daftarSesi.size()) return;
        if (daftarSesi.get(idx) == sesiAktif) return;

        sesiAktif = daftarSesi.get(idx);
        muatUlangSesi(sesiAktif);
    }

    /** Menghapus sesi yang sedang aktif. */
    @FXML
    private void handleHapusSesi() {
        if (daftarSesi.size() <= 1) {
            Notifications.create()
                .title("Tidak Bisa Hapus")
                .text("Minimal satu sesi harus ada.")
                .showWarning();
            return;
        }

        int idx = daftarSesi.indexOf(sesiAktif);
        daftarSesi.remove(sesiAktif);
        listSesi.getItems().remove(idx);

        // Pilih sesi sebelumnya atau berikutnya
        int newIdx = Math.max(0, idx - 1);
        sesiAktif = daftarSesi.get(newIdx);
        listSesi.getSelectionModel().select(newIdx);
        muatUlangSesi(sesiAktif);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Private — Sesi Management
    // ═════════════════════════════════════════════════════════════════════════

    /** Membuat sesi baru, menambahkannya ke daftar dan sidebar. */
    private void buatSesiBaru() {
        sesiAktif = new Sesi();
        sesiAktif.setId(daftarSesi.size() + 1);
        if (userLogin != null) sesiAktif.setUserId(userLogin.getId());

        daftarSesi.add(sesiAktif);
        listSesi.getItems().add("💬 Sesi " + sesiAktif.getId());
        listSesi.getSelectionModel().selectLast();

        // Bersihkan area chat dan reset quick reply
        chatBox.getChildren().clear();
        quickReplyShown = false;
        quickReplyBox.getChildren().clear();
        quickReplyBox.setVisible(false);
        quickReplyBox.setManaged(false);
    }

    /** Memuat ulang riwayat pesan sesi ke dalam chatBox. */
    private void muatUlangSesi(Sesi sesi) {
        chatBox.getChildren().clear();
        quickReplyShown = true; // jangan tampilkan quick reply lagi
        quickReplyBox.setVisible(false);
        quickReplyBox.setManaged(false);

        for (Pesan p : sesi.getDaftarPesan()) {
            if (p.isDariBot()) {
                tambahBubbleBot(p.getIsiPesan(), p.getWaktuKirim());
            } else {
                tambahBubbleUser(p.getIsiPesan(), p.getWaktuKirim());
            }
        }
    }

    /** Memuat knowledge base dari DB dan inject ke engine. */
    private void muatKnowledgeBase() {
        try {
            List<EntriKnowledge> entri = knowledgeDAO.findAll();
            engine.setDaftarEntri(entri != null ? entri : new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[ChatController] Gagal load knowledge base: " + e.getMessage());
            engine.setDaftarEntri(new ArrayList<>());
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
        String salam = "Halo! Selamat datang di WedMate Assistant 💍\n" +
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
        lblPesan.getStyleClass().add("bubble-user");
        lblPesan.setMaxWidth(400);
        lblPesan.setWrapText(true);

        // Timestamp
        Label lblTime = new Label(waktu != null ? waktu.format(TIME_FMT) : "");
        lblTime.getStyleClass().add("timestamp-user");

        // VBox: pesan + timestamp (keduanya rata kanan)
        VBox vBox = new VBox(2, lblPesan, lblTime);
        vBox.setAlignment(Pos.CENTER_RIGHT);

        // Wrapper rata kanan dengan padding kiri 60
        HBox wrapper = new HBox(vBox);
        wrapper.getStyleClass().add("bubble-wrapper-user");
        wrapper.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(vBox, Priority.SOMETIMES);

        chatBox.getChildren().add(wrapper);
    }

    /**
     * Membuat dan menambahkan bubble pesan BOT ke chatBox.
     * Rata kiri, warna abu gelap (#2C2C4A), dengan avatar kecil.
     */
    private void tambahBubbleBot(String teks, LocalDateTime waktu) {
        // Avatar dot kecil
        StackPane avatar = new StackPane(new Label("💍"));
        avatar.getStyleClass().add("bot-dot");

        // Label isi pesan
        Label lblPesan = new Label(teks);
        lblPesan.getStyleClass().add("bubble-bot");
        lblPesan.setMaxWidth(420);
        lblPesan.setWrapText(true);

        // Timestamp
        Label lblTime = new Label(waktu != null ? waktu.format(TIME_FMT) : "");
        lblTime.getStyleClass().add("timestamp-bot");

        // VBox: pesan + timestamp
        VBox vBox = new VBox(2, lblPesan, lblTime);
        vBox.setAlignment(Pos.CENTER_LEFT);

        // Wrapper rata kiri: avatar + konten
        HBox wrapper = new HBox(8, avatar, vBox);
        wrapper.getStyleClass().add("bubble-wrapper-bot");
        wrapper.setAlignment(Pos.TOP_LEFT);

        chatBox.getChildren().add(wrapper);
    }

    /** Membuat label "WedMate sedang mengetik..." sebagai placeholder. */
    private Label buatTypingLabel() {
        Label lbl = new Label("WedMate sedang mengetik...");
        lbl.getStyleClass().add("typing-label");
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
            btn.getStyleClass().add("quick-reply-btn");
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
