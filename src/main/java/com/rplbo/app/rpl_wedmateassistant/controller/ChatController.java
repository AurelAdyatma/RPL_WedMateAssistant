package com.rplbo.app.rpl_wedmateassistant.controller;

// Mengimpor kelas manajer dan seeder database
import com.rplbo.app.rpl_wedmateassistant.database.DatabaseManager;
import com.rplbo.app.rpl_wedmateassistant.database.DataSeeder;
// Mengimpor Data Access Object (DAO) yang diperlukan untuk mengambil data
import com.rplbo.app.rpl_wedmateassistant.database.KnowledgeBaseDAO;
import com.rplbo.app.rpl_wedmateassistant.database.PakaianDAO;
import com.rplbo.app.rpl_wedmateassistant.database.PaketDAO;
// Mengimpor engine chatbot dan kategori Regex
import com.rplbo.app.rpl_wedmateassistant.engine.ChatbotEngine;
import com.rplbo.app.rpl_wedmateassistant.engine.RegexMatcher.Kategori;
// Mengimpor entitas/model data
import com.rplbo.app.rpl_wedmateassistant.model.EntriKnowledge;
import com.rplbo.app.rpl_wedmateassistant.model.PakaianWedding;
import com.rplbo.app.rpl_wedmateassistant.model.PaketSewa;
import com.rplbo.app.rpl_wedmateassistant.model.Pesan;
import com.rplbo.app.rpl_wedmateassistant.model.Sesi;
import com.rplbo.app.rpl_wedmateassistant.model.User;

// Mengimpor library JavaFX untuk UI dan Animasi
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

// Mengimpor ControlsFX (opsional/ekstra library)
import org.controlsfx.control.Notifications;

// Mengimpor library tanggal, waktu, dan list standar
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

    /** Layout utama yang membelah tampilan layar (opsional digunakan jika ada sidebar) */
    @FXML private SplitPane     splitPane;

    /** ScrollPane untuk membuat area percakapan bisa di-scroll ke bawah/atas */
    @FXML private ScrollPane    scrollChat;
    /** VBox utama tempat bubble-bubble chat (pesan user dan bot) dimasukkan */
    @FXML private VBox          chatBox;
    /** HBox untuk meletakkan tombol-tombol respon cepat (Quick Reply) */
    @FXML private HBox          quickReplyBox;
    /** Label status di bagian atas atau bawah (contoh: "Online") */
    @FXML private Label         lblStatus;

    /** Field input teks bagi pengguna untuk mengetik pesan ke bot */
    @FXML private TextField     txtInput;
    /** Tombol aksi pengiriman pesan */
    @FXML private Button        btnKirim;
    /** Tombol aksi untuk kembali ke halaman sebelumnya (Welcome) */
    @FXML private Button        btnKembali;
    /** Tombol aksi untuk membersihkan seluruh riwayat chat di layar */
    @FXML private Button        btnClearChat;

    // ── State ─────────────────────────────────────────────────────────────────

    /** Instansiasi mesin inti chatbot yang akan memproses input pengguna */
    private final ChatbotEngine      engine           = new ChatbotEngine();
    /** Instansiasi DAO knowledge base untuk mengambil data jawaban bot */
    private final KnowledgeBaseDAO   knowledgeDAO     = new KnowledgeBaseDAO();
    /** Instansiasi DAO pakaian untuk chatbot (bila bot merekomendasikan baju) */
    private final PakaianDAO         pakaianDAO       = new PakaianDAO();
    /** Instansiasi DAO paket sewa */
    private final PaketDAO           paketDAO         = new PaketDAO();
    
    /** Format standar jam:menit untuk ditampilkan pada bubble chat */
    private final DateTimeFormatter  TIME_FMT         =
            DateTimeFormatter.ofPattern("HH:mm");

    /** Sesi yang sedang aktif ditampilkan. Menampung riwayat pesan saat ini. */
    private Sesi sesiAktif;

    /** User yang sedang login (di-inject dari LoginController). */
    private User userLogin;

    /** True setelah quick reply sudah ditampilkan (agar hanya muncul sekali di awal/bawah). */
    private boolean quickReplyShown = false;

    // ── Format helper ─────────────────────────────────────────────────────────

    /** Kumpulan teks tetap (hard-coded) untuk tombol Quick Reply di UI */
    private static final String[] QUICK_REPLIES = {
        "Lihat Busana",
        "Harga & Paket",
        "Cek Ketersediaan",
        "Info Toko"
    };

    // ═════════════════════════════════════════════════════════════════════════
    // FXML initialize
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Dipanggil otomatis oleh JavaFX setelah UI di-load dari FXML.
     * Menginisialisasi DB, memuat data ke memori engine, membuat sesi, dll.
     */
    @FXML
    public void initialize() {
        // 1. Inisialisasi database & lakukan proses seeding (pengisian data awal) jika tabel kosong
        DatabaseManager.getInstance().initDB();
        new DataSeeder().seed();

        // 2. Load seluruh rules chatbot, pakaian, dan paket dari DB ke memori Engine
        muatKnowledgeBase();

        // 3. Buat sesi chat baru
        buatSesiBaru();

        // 4. Buat scrollbar otomatis turun (auto-scroll) setiap kali chatBox bertambah tingginya (ada pesan baru)
        chatBox.heightProperty().addListener((obs, oldH, newH) ->
            Platform.runLater(() ->
                scrollChat.setVvalue(1.0)
            )
        );

        // 5. Tampilkan sapaan awal bot ke UI
        tampilkanSalamAwal();

        // 6. Daftarkan event listener: menekan Enter pada field input akan mengirim pesan
        txtInput.setOnAction(e -> handleKirimPesan());
    }

    /** 
     * Event handler tombol kembali (Back). 
     * Menutup halaman chat dan membuka kembali halaman Welcome.
     */
    @FXML
    private void handleKembali() {
        try {
            // Ambil referensi Stage/Window dari tombol
            Stage stage = (Stage) btnKirim.getScene().getWindow();
            // Load file FXML tampilan Welcome
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rplbo/app/rpl_wedmateassistant/view/Welcome.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1280, 800);
            
            // Set layar dengan scene yang baru
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
     * Menerima user yang sudah login dari kontroller sebelumnya.
     * Harus dipanggil sebelum tampilan ditampilkan.
     *
     * @param user objek User yang berhasil login
     */
    public void setUserLogin(User user) {
        this.userLogin = user;
        // Set user ID ke sesi aktif jika sudah terbentuk
        if (sesiAktif != null && user != null) {
            sesiAktif.setUserId(user.getId());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // FXML Handlers
    // ═════════════════════════════════════════════════════════════════════════

    /** Kirim pesan saat tombol 'Kirim' diklik atau Enter ditekan. */
    @FXML
    private void handleKirimPesan() {
        String teks = txtInput.getText().trim();
        // Cegah pengiriman jika input kosong
        if (teks.isEmpty()) return;

        // 1. Tampilkan bubble chat milik pengguna di sisi kanan layar
        tambahBubbleUser(teks, LocalDateTime.now());
        txtInput.clear();

        // 2. Tampilkan indikator "WedMate sedang mengetik..." di sisi kiri layar
        Label typingLabel = buatTypingLabel();
        chatBox.getChildren().add(typingLabel);

        // 3. Buat jeda simulasi selama 800ms agar bot terasa lebih natural seperti manusia mengetik
        PauseTransition jeda = new PauseTransition(Duration.millis(800));
        jeda.setOnFinished(e -> {
            // Hapus label mengetik setelah jeda selesai
            chatBox.getChildren().remove(typingLabel);
            // Lakukan proses logika chatbot untuk mendapatkan dan menampilkan respon
            prosesInputDanBalas(teks);
        });
        jeda.play();
    }

    /** Menghapus seluruh isi riwayat chat yang tampil lalu menampilkan salam awal lagi. */
    @FXML
    private void handleClearChat() {
        // Hapus elemen visual UI chat
        chatBox.getChildren().clear();

        // Hapus daftar riwayat dari memori sesi
        if (sesiAktif != null) {
            sesiAktif.getDaftarPesan().clear();
        }

        // Sembunyikan quick reply
        quickReplyShown = false;
        quickReplyBox.getChildren().clear();
        quickReplyBox.setVisible(false);
        quickReplyBox.setManaged(false);

        // Bersihkan input dan tampilkan ulang sapaan pertama
        txtInput.clear();
        tampilkanSalamAwal();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Private — Sesi Management
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Memulai sesi percakapan baru untuk user yang login saat ini.
     * Mengosongkan UI untuk siap menerima chat baru.
     */
    private void buatSesiBaru() {
        sesiAktif = new Sesi();
        sesiAktif.setId(1); // Set ID statis sementara, bisa dibuat auto-increment via DB nanti
        if (userLogin != null) sesiAktif.setUserId(userLogin.getId());

        // Bersihkan area chat dan reset UI quick reply
        chatBox.getChildren().clear();
        quickReplyShown = false;
        quickReplyBox.getChildren().clear();
        quickReplyBox.setVisible(false);
        quickReplyBox.setManaged(false);
    }

    /** Memuat semua data terkait (knowledge, pakaian, paket) dari DAO DB ke memori Engine bot. */
    private void muatKnowledgeBase() {
        try {
            // Load rules knowledge base
            List<EntriKnowledge> entri = knowledgeDAO.findAll();
            engine.setDaftarEntri(entri != null ? entri : new ArrayList<>());
            System.out.println("[ChatController] Knowledge base dimuat: " + (entri != null ? entri.size() : 0) + " entri.");
        } catch (Exception e) {
            System.err.println("[ChatController] Gagal load knowledge base: " + e.getMessage());
            engine.setDaftarEntri(new ArrayList<>());
        }

        try {
            // Load data pakaian
            List<PakaianWedding> pakaian = pakaianDAO.findAll();
            engine.setDaftarPakaian(pakaian != null ? pakaian : new ArrayList<>());
            System.out.println("[ChatController] Pakaian dimuat: " + (pakaian != null ? pakaian.size() : 0) + " item.");
        } catch (Exception e) {
            System.err.println("[ChatController] Gagal load pakaian: " + e.getMessage());
            engine.setDaftarPakaian(new ArrayList<>());
        }

        try {
            // Load data paket
            List<PaketSewa> paket = paketDAO.findAll();
            engine.setDaftarPaket(paket != null ? paket : new ArrayList<>());
            System.out.println("[ChatController] Paket sewa dimuat: " + (paket != null ? paket.size() : 0) + " paket.");
        } catch (Exception e) {
            System.err.println("[ChatController] Gagal load paket sewa: " + e.getMessage());
            engine.setDaftarPaket(new ArrayList<>());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Private — Chatbot Processing
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Meneruskan teks yang diketik pengguna ke ChatbotEngine untuk diproses.
     * Mengambil balasan berupa objek Pesan dari Engine, lalu menampilkannya sebagai UI bubble bot.
     *
     * @param inputPengguna string pertanyaan user
     */
    private void prosesInputDanBalas(String inputPengguna) {
        // Simpan pesan teks user ke memori Sesi saat ini
        Pesan pesanUser = new Pesan(sesiAktif.getId(), inputPengguna, false);
        sesiAktif.getDaftarPesan().add(pesanUser);

        // Refresh/muat ulang basis data dari DB 
        // agar perubahan apapun di menu Admin Panel langsung ter-update di logika Chatbot
        muatKnowledgeBase();

        // ── Proses input via Engine ──
        Pesan pesanBot = engine.prosesPesan(inputPengguna, sesiAktif);

        // Tampilkan hasil di UI berupa bubble bot, termasuk bila ada gambar (imageDataList)
        if (pesanBot != null && pesanBot.getIsiPesan() != null) {
            tambahBubbleBot(pesanBot.getIsiPesan(), pesanBot.getWaktuKirim(), pesanBot.getImageDataList());
        }
    }

    /** Menampilkan salam awal pembuka bot ke UI saat sesi chat baru saja dimulai. */
    private void tampilkanSalamAwal() {
        String salam = "Halo! Selamat datang di WedMate Assistant.\n" +
                "Saya siap membantu Anda menemukan busana pernikahan impian!\n" +
                "Ada yang bisa saya bantu hari ini?";

        // Record ke Sesi
        Pesan pesanSalam = new Pesan(sesiAktif.getId(), salam, true);
        sesiAktif.getDaftarPesan().add(pesanSalam);

        // Render UI
        tambahBubbleBot(salam, LocalDateTime.now(), null);
        tampilkanQuickReply();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Private — Bubble Builder
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Membuat dan menambahkan bubble UI pesan dari pengguna (USER) ke dalam chatBox.
     * Posisi UI: rata kanan. Warna latar: coklat gelap (#2E3A3F).
     *
     * @param teks Teks yang diketik user
     * @param waktu Waktu pesan ini dikirim
     */
    private void tambahBubbleUser(String teks, LocalDateTime waktu) {
        // Pembuatan node untuk teks chat
        Label lblPesan = new Label(teks);
        lblPesan.setStyle("-fx-background-color: #2E3A3F; -fx-text-fill: white; -fx-padding: 12 18; -fx-background-radius: 16 16 0 16; -fx-font-size: 14px;");
        lblPesan.setMaxWidth(400); // Batas panjang bubble agar teks turun ke bawah jika lebih
        lblPesan.setWrapText(true);

        // Node timestamp untuk waktu chat
        Label lblTime = new Label(waktu != null ? waktu.format(TIME_FMT) : "");
        lblTime.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-padding: 2 4 0 0;");

        // VBox yang membungkus teks pesan dan waktunya
        VBox vBox = new VBox(2, lblPesan, lblTime);
        vBox.setAlignment(Pos.CENTER_RIGHT);

        // HBox utama pembungkus yang mengatur penempatan (Rata kanan)
        HBox wrapper = new HBox(vBox);
        wrapper.setStyle("-fx-padding: 0 0 0 50;");
        wrapper.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(vBox, Priority.SOMETIMES);

        // Menambahkan elemen ke VBox chat utama
        chatBox.getChildren().add(wrapper);
    }

    /**
     * Membuat dan menambahkan bubble UI pesan dari chatbot (BOT) ke dalam chatBox.
     * Posisi UI: rata kiri. Warna latar: putih/terang dengan pinggiran abu.
     *
     * @param teks Teks jawaban yang digenerate oleh engine
     * @param waktu Waktu pesan ini dibalas
     * @param images List of byte array (gambar), dapat bernilai null
     */
    private void tambahBubbleBot(String teks, LocalDateTime waktu, List<byte[]> images) {
        // Membuat node visual Avatar bot (ikon huruf "W" berwarna oranye)
        StackPane avatar = new StackPane(new Label("W"));
        avatar.setStyle("-fx-background-color: #D97706; -fx-background-radius: 18; -fx-min-width: 36; -fx-min-height: 36;");
        ((Label) avatar.getChildren().get(0)).setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        // Membuat node visual Label untuk teks balasan dari Bot
        Label lblPesan = new Label(teks);
        lblPesan.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #1E293B; -fx-padding: 14 18; -fx-background-radius: 16 16 16 0; -fx-font-size: 14px; -fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-border-radius: 16 16 16 0;");
        lblPesan.setMaxWidth(420);
        lblPesan.setWrapText(true);

        // Kontainer yang bisa memuat Teks sekaligus Gambar (kalau ada)
        VBox contentBox = new VBox(8);
        contentBox.getChildren().add(lblPesan);

        // Jika terdapat gambar (contoh: rekomendasi baju), tampilkan via ImageView
        if (images != null && !images.isEmpty()) {
            // FlowPane digunakan supaya gambar dapat membungkus (wrap/ke baris baru) jika lebih dari satu
            FlowPane imagePane = new FlowPane();
            imagePane.setHgap(8);
            imagePane.setVgap(8);
            imagePane.setMaxWidth(420);
            
            for (byte[] imgData : images) {
                try {
                    // Load raw BLOB bytes menjadi Stream input untuk Image JavaFX
                    java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(imgData);
                    javafx.scene.image.Image img = new javafx.scene.image.Image(bais, 280, 400, true, true);
                    javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
                    
                    // Set ukuran visual agar terlihat rapi (aspect ratio)
                    imgView.setFitWidth(280);
                    imgView.setFitHeight(400);
                    imgView.setPreserveRatio(true);
                    
                    // Memberikan efek shadow pada foto busana
                    imgView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
                    // Memberikan kursor klik tangan
                    imgView.setCursor(javafx.scene.Cursor.HAND);
                    
                    imagePane.getChildren().add(imgView);
                } catch (Exception e) {
                    System.err.println("Gagal memuat gambar dari BLOB data");
                }
            }
            if (!imagePane.getChildren().isEmpty()) {
                contentBox.getChildren().add(imagePane);
            }
        }

        // Node timestamp untuk waktu balasan
        Label lblTime = new Label(waktu != null ? waktu.format(TIME_FMT) : "");
        lblTime.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-padding: 2 0 0 4;");

        // VBox yang membungkus konten bot (teks + gambar) beserta waktunya
        VBox vBox = new VBox(4, contentBox, lblTime);
        vBox.setAlignment(Pos.CENTER_LEFT);

        // HBox utama pembungkus yang mengatur penempatan (Avatar di kiri + VBox isi di kanannya)
        HBox wrapper = new HBox(12, avatar, vBox);
        wrapper.setStyle("-fx-padding: 0 50 0 0;");
        wrapper.setAlignment(Pos.TOP_LEFT);

        // Menambahkan HBox ke dalam wadah utama chat UI
        chatBox.getChildren().add(wrapper);
    }

    /** 
     * Membuat dan mengembalikan label indikator pengetikan. 
     * Berguna sebagai animasi simulasi waktu mikir (delay) sebelum bot membalas.
     * 
     * @return Label node yang menampilkan teks "mengetik"
     */
    private Label buatTypingLabel() {
        Label lbl = new Label("WedMate sedang mengetik...");
        lbl.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px; -fx-font-style: italic; -fx-padding: 0 0 0 48;");
        return lbl;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Private — Quick Reply
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Menampilkan sederet tombol aksi cepat (Quick Reply) di bawah area chat.
     * Masing-masing tombol berfungsi mensimulasikan ketikan pengguna dan otomatis mengirim pesan
     * ke dalam sistem chat saat di-klik.
     */
    private void tampilkanQuickReply() {
        // Quick reply hanya perlu dimunculkan satu kali per-sesi untuk UX yang baik
        if (quickReplyShown) return;
        quickReplyShown = true;

        // Kosongkan HBox sebelum render baru
        quickReplyBox.getChildren().clear();

        for (String label : QUICK_REPLIES) {
            Button btn = new Button(label);
            btn.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #D97706; -fx-border-color: #D97706; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 6 15; -fx-font-size: 13px; -fx-cursor: hand;");
            // Set action jika tombol ditekan: Isi field teks dan paksa tekan handleKirimPesan
            btn.setOnAction(e -> {
                txtInput.setText(label);
                handleKirimPesan();
            });
            quickReplyBox.getChildren().add(btn);
        }

        // Tampilkan kotak yang berisi tombol-tombol tersebut
        quickReplyBox.setVisible(true);
        quickReplyBox.setManaged(true);
    }
}
