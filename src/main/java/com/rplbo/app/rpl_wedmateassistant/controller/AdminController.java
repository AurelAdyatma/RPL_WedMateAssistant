package com.rplbo.app.rpl_wedmateassistant.controller;

// Mengimpor kelas DAO untuk mengakses data di database
import com.rplbo.app.rpl_wedmateassistant.database.KnowledgeBaseDAO;
import com.rplbo.app.rpl_wedmateassistant.database.PakaianDAO;
import com.rplbo.app.rpl_wedmateassistant.database.PaketDAO;

// Mengimpor kelas Model yang merepresentasikan entitas bisnis
import com.rplbo.app.rpl_wedmateassistant.model.EntriKnowledge;
import com.rplbo.app.rpl_wedmateassistant.model.PakaianWedding;
import com.rplbo.app.rpl_wedmateassistant.model.PaketSewa;

// Mengimpor kelas Property JavaFX untuk binding data dengan TableView
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
// Mengimpor koleksi observable untuk daftar data di UI
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
// Mengimpor anotasi dan kelas FXML untuk memuat antarmuka
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
// Mengimpor kelas-kelas dasar JavaFX untuk kontrol UI, tata letak, dan scene
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controller Admin — mengelola CRUD knowledge base, katalog pakaian, dan paket sewa.
 *
 * <p>Menyediakan antarmuka dashboard admin dengan tab (seksi) yang berbeda:
 * <ul>
 *   <li>Knowledge Base: untuk mengatur aturan chatbot</li>
 *   <li>Pakaian Wedding: untuk mengatur stok dan harga busana</li>
 *   <li>Paket Sewa: untuk mengatur daftar paket penyewaan</li>
 * </ul>
 * </p>
 */
public class AdminController {

    // ── FXML ─────────────────────────────────────────────────────────────────
    
    /** Label untuk menampilkan judul seksi yang sedang aktif */
    @FXML private Label         lblSection;
    /** Tombol navigasi ke seksi Knowledge Base */
    @FXML private Button        btnSectionKnowledge;
    /** Tombol navigasi ke seksi Pakaian */
    @FXML private Button        btnSectionPakaian;
    /** Tombol navigasi ke seksi Paket */
    @FXML private Button        btnSectionPaket;
    /** Tombol untuk logout dan kembali ke halaman login */
    @FXML private Button        btnLogout;

    /** Container (layout) untuk tampilan tabel Knowledge Base */
    @FXML private VBox          sectionKnowledge;
    /** Container (layout) untuk tampilan tabel Pakaian */
    @FXML private VBox          sectionPakaian;
    /** Container (layout) untuk tampilan tabel Paket */
    @FXML private VBox          sectionPaket;

    /** Tabel untuk menampilkan data knowledge base chatbot */
    @FXML private TableView<EntriKnowledge>  tableKnowledge;
    /** Tabel untuk menampilkan data katalog pakaian wedding */
    @FXML private TableView<PakaianWedding>  tablePakaian;
    /** Tabel untuk menampilkan data paket sewa */
    @FXML private TableView<PaketSewa>       tablePaket;

    /** Field teks untuk melakukan pencarian / filter data di tabel yang sedang aktif */
    @FXML private TextField     txtCari;
    /** Tombol untuk memicu dialog tambah data baru */
    @FXML private Button        btnTambah;
    /** Tombol untuk memicu dialog edit data yang dipilih dari tabel */
    @FXML private Button        btnEdit;
    /** Tombol untuk menghapus data yang dipilih dari tabel */
    @FXML private Button        btnHapus;

    // ── DAO ───────────────────────────────────────────────────────────────────
    
    /** Objek Data Access Object untuk tabel knowledge_base */
    private final KnowledgeBaseDAO knowledgeDAO = new KnowledgeBaseDAO();
    /** Objek Data Access Object untuk tabel pakaian_wedding */
    private final PakaianDAO       pakaianDAO   = new PakaianDAO();
    /** Objek Data Access Object untuk tabel paket_sewa */
    private final PaketDAO         paketDAO     = new PaketDAO();

    // ── State ─────────────────────────────────────────────────────────────────
    
    /** 
     * Enum yang mendefinisikan seksi atau tab yang tersedia pada dashboard admin.
     */
    private enum Seksi { KNOWLEDGE, PAKAIAN, PAKET }
    
    /** Menyimpan status seksi mana yang saat ini sedang dibuka/aktif */
    private Seksi seksiAktif = Seksi.KNOWLEDGE;

    // ── Init ──────────────────────────────────────────────────────────────────
    
    /**
     * Method inisialisasi yang dipanggil secara otomatis oleh JavaFX
     * setelah file FXML dimuat. 
     * Berfungsi untuk melakukan setup kolom tabel, memuat data awal,
     * serta mendengarkan (listen) input pencarian.
     */
    @FXML
    public void initialize() {
        setupKnowledgeTable();
        setupPakaianTable();
        setupPaketTable();
        tampilkanSeksi(Seksi.KNOWLEDGE);
        muatSemuaData();

        // Filter pencarian real-time ketika user mengetik di field pencarian
        txtCari.textProperty().addListener((obs, o, keyword) -> filterData(keyword));
    }

    // ── Sidebar handlers ──────────────────────────────────────────────────────
    
    /** Event handler ketika tombol "Knowledge Base" di sidebar ditekan */
    @FXML private void handleSeksiKnowledge() { tampilkanSeksi(Seksi.KNOWLEDGE); }
    
    /** Event handler ketika tombol "Pakaian" di sidebar ditekan */
    @FXML private void handleSeksiPakaian()   { tampilkanSeksi(Seksi.PAKAIAN);   }
    
    /** Event handler ketika tombol "Paket Sewa" di sidebar ditekan */
    @FXML private void handleSeksiPaket()     { tampilkanSeksi(Seksi.PAKET);     }

    /** 
     * Event handler untuk tombol Logout.
     * Berfungsi menutup halaman admin dan memuat ulang halaman Login.
     */
    @FXML
    private void handleLogout() {
        try {
            // Mengambil jendela (stage) saat ini dari salah satu elemen UI
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            // Memuat file FXML untuk halaman login
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/rplbo/app/rpl_wedmateassistant/view/Login.fxml")
            );
            Parent root = loader.load();
            // Menetapkan adegan baru pada stage
            stage.setScene(new Scene(root, 1280, 800));
            stage.setTitle("WedMate - Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            // Tampilkan alert error jika file FXML login gagal dimuat
            showAlert(Alert.AlertType.ERROR, "Gagal kembali ke login: " + e.getMessage());
        }
    }

    /**
     * Mengatur visibilitas dan gaya tampilan berdasarkan seksi yang dipilih pengguna.
     *
     * @param seksi Seksi (tab) yang ingin ditampilkan
     */
    private void tampilkanSeksi(Seksi seksi) {
        seksiAktif = seksi;
        // Mengatur visibility VBox tabel
        sectionKnowledge.setVisible(seksi == Seksi.KNOWLEDGE);
        sectionKnowledge.setManaged(seksi == Seksi.KNOWLEDGE);
        sectionPakaian.setVisible(seksi == Seksi.PAKAIAN);
        sectionPakaian.setManaged(seksi == Seksi.PAKAIAN);
        sectionPaket.setVisible(seksi == Seksi.PAKET);
        sectionPaket.setManaged(seksi == Seksi.PAKET);

        // Memperbarui teks pada label judul
        String[] judul = {"Knowledge Base (Chatbot)", "Katalog Pakaian Wedding", "Paket Sewa"};
        lblSection.setText(judul[seksi.ordinal()]);
        // Bersihkan text box pencarian ketika pindah tab
        txtCari.clear();

        // Variabel penyimpan inline CSS untuk tombol aktif dan tidak aktif
        String styleActive = "-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 13px; -fx-alignment: CENTER_LEFT; -fx-padding: 12 16; -fx-background-radius: 8; -fx-cursor: hand;";
        String styleInactive = "-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-size: 13px; -fx-alignment: CENTER_LEFT; -fx-padding: 12 16; -fx-background-radius: 8; -fx-cursor: hand;";

        // Terapkan style baru pada tombol navigasi sidebar
        btnSectionKnowledge.setStyle(seksi == Seksi.KNOWLEDGE ? styleActive : styleInactive);
        btnSectionPakaian.setStyle(seksi == Seksi.PAKAIAN ? styleActive : styleInactive);
        btnSectionPaket.setStyle(seksi == Seksi.PAKET ? styleActive : styleInactive);
    }

    // ── CRUD handlers ─────────────────────────────────────────────────────────
    
    /**
     * Handler untuk tombol "Tambah Data".
     * Memanggil dialog modal yang sesuai dengan seksi yang sedang aktif.
     */
    @FXML private void handleTambah() {
        switch (seksiAktif) {
            case KNOWLEDGE -> dialogKnowledge(null);
            case PAKAIAN   -> dialogPakaian(null);
            case PAKET     -> dialogPaket(null);
        }
    }

    /**
     * Handler untuk tombol "Edit Data".
     * Mengecek apakah ada data yang dipilih pada tabel seksi yang aktif, 
     * lalu memanggil dialog edit dengan menyertakan data yang dipilih.
     */
    @FXML private void handleEdit() {
        switch (seksiAktif) {
            case KNOWLEDGE -> {
                EntriKnowledge sel = tableKnowledge.getSelectionModel().getSelectedItem();
                if (sel == null) { showAlert(Alert.AlertType.WARNING, "Pilih entri yang ingin diedit."); return; }
                dialogKnowledge(sel);
            }
            case PAKAIAN -> {
                PakaianWedding sel = tablePakaian.getSelectionModel().getSelectedItem();
                if (sel == null) { showAlert(Alert.AlertType.WARNING, "Pilih pakaian yang ingin diedit."); return; }
                dialogPakaian(sel);
            }
            case PAKET -> {
                PaketSewa sel = tablePaket.getSelectionModel().getSelectedItem();
                if (sel == null) { showAlert(Alert.AlertType.WARNING, "Pilih paket yang ingin diedit."); return; }
                dialogPaket(sel);
            }
        }
    }

    /**
     * Handler untuk tombol "Hapus Data".
     * Menampilkan konfirmasi terlebih dahulu sebelum menghapus data 
     * dari database berdasarkan ID data yang dipilih.
     */
    @FXML private void handleHapus() {
        // Tampilkan dialog konfirmasi penghapusan
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION,
            "Yakin ingin menghapus data ini?", ButtonType.YES, ButtonType.NO);
        konfirmasi.setTitle("Konfirmasi Hapus");
        Optional<ButtonType> result = konfirmasi.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES) return; // Batal hapus

        boolean ok = false;
        switch (seksiAktif) {
            case KNOWLEDGE -> {
                EntriKnowledge sel = tableKnowledge.getSelectionModel().getSelectedItem();
                if (sel == null) { showAlert(Alert.AlertType.WARNING, "Pilih entri yang ingin dihapus."); return; }
                ok = knowledgeDAO.delete(sel.getId());
                if (ok) muatKnowledge(); // Refresh tabel jika berhasil
            }
            case PAKAIAN -> {
                PakaianWedding sel = tablePakaian.getSelectionModel().getSelectedItem();
                if (sel == null) { showAlert(Alert.AlertType.WARNING, "Pilih pakaian yang ingin dihapus."); return; }
                ok = pakaianDAO.delete(sel.getId());
                if (ok) muatPakaian();
            }
            case PAKET -> {
                PaketSewa sel = tablePaket.getSelectionModel().getSelectedItem();
                if (sel == null) { showAlert(Alert.AlertType.WARNING, "Pilih paket yang ingin dihapus."); return; }
                ok = paketDAO.delete(sel.getId());
                if (ok) muatPaket();
            }
        }
        if (!ok) showAlert(Alert.AlertType.ERROR, "Gagal menghapus data.");
    }

    // ── Setup Tabel ───────────────────────────────────────────────────────────
    
    /**
     * Mengatur kolom-kolom pada tabel Knowledge Base dan mengikatnya
     * dengan properti objek EntriKnowledge menggunakan CellValueFactory.
     */
    @SuppressWarnings("unchecked")
    private void setupKnowledgeTable() {
        // Kolom ID
        TableColumn<EntriKnowledge, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(40);

        // Kolom Pertanyaan (Regex Pattern)
        TableColumn<EntriKnowledge, String> colPertanyaan = new TableColumn<>("Kata Kunci (Trigger)");
        // Mengubah regex "|" menjadi "," agar lebih user friendly saat ditampilkan ke admin
        colPertanyaan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPertanyaan().replace("|", ", ")));
        colPertanyaan.setPrefWidth(200);

        // Kolom Jawaban Bot
        TableColumn<EntriKnowledge, String> colJawaban = new TableColumn<>("Jawaban");
        colJawaban.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJawaban()));
        colJawaban.setPrefWidth(300);

        // Kolom Kategori/Topik
        TableColumn<EntriKnowledge, String> colKategori = new TableColumn<>("Kategori");
        colKategori.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKategori()));
        colKategori.setPrefWidth(120);

        // Kolom Status Aktif
        TableColumn<EntriKnowledge, Boolean> colAktif = new TableColumn<>("Aktif");
        colAktif.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isAktif()));
        colAktif.setPrefWidth(60);

        // Menambahkan kolom-kolom ke dalam TableView
        tableKnowledge.getColumns().setAll(colId, colPertanyaan, colJawaban, colKategori, colAktif);
        tableKnowledge.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    /**
     * Mengatur kolom-kolom pada tabel Pakaian Wedding.
     */
    @SuppressWarnings("unchecked")
    private void setupPakaianTable() {
        // Kolom ID
        TableColumn<PakaianWedding, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(40);

        // Kolom Nama Pakaian
        TableColumn<PakaianWedding, String> colNama = new TableColumn<>("Nama");
        colNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNama()));
        colNama.setPrefWidth(200);

        // Kolom Jenis/Kategori Busana
        TableColumn<PakaianWedding, String> colJenis = new TableColumn<>("Jenis");
        colJenis.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKategori()));
        colJenis.setPrefWidth(120);

        // Kolom Ukuran yang tersedia
        TableColumn<PakaianWedding, String> colUkuran = new TableColumn<>("Ukuran");
        colUkuran.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUkuranTersedia()));
        colUkuran.setPrefWidth(80);

        // Kolom Gender (Pria/Wanita/Unisex)
        TableColumn<PakaianWedding, String> colGender = new TableColumn<>("Gender");
        colGender.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGender()));
        colGender.setPrefWidth(80);

        // Kolom Harga Sewa
        TableColumn<PakaianWedding, Double> colHarga = new TableColumn<>("Harga Sewa");
        colHarga.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getHargaSewa()).asObject());
        colHarga.setPrefWidth(110);

        // Kolom Ketersediaan (Sedang disewa atau tidak)
        TableColumn<PakaianWedding, Boolean> colTersedia = new TableColumn<>("Tersedia");
        colTersedia.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isTersedia()));
        colTersedia.setPrefWidth(70);

        // Menerapkan kolom
        tablePakaian.getColumns().setAll(colId, colNama, colJenis, colUkuran, colGender, colHarga, colTersedia);
        tablePakaian.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    /**
     * Mengatur kolom-kolom pada tabel Paket Sewa.
     */
    @SuppressWarnings("unchecked")
    private void setupPaketTable() {
        // Kolom ID
        TableColumn<PaketSewa, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(40);

        // Kolom Nama Paket
        TableColumn<PaketSewa, String> colNama = new TableColumn<>("Nama Paket");
        colNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNamaPaket()));
        colNama.setPrefWidth(150);

        // Kolom Deskripsi Singkat
        TableColumn<PaketSewa, String> colDeskripsi = new TableColumn<>("Deskripsi");
        colDeskripsi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeskripsi()));
        colDeskripsi.setPrefWidth(260);

        // Kolom Harga Total Paket
        TableColumn<PaketSewa, Double> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getHargaTotal()).asObject());
        colHarga.setPrefWidth(110);

        tablePaket.getColumns().setAll(colId, colNama, colDeskripsi, colHarga);
        tablePaket.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // ── Load Data ─────────────────────────────────────────────────────────────
    
    /** Memuat semua data (Knowledge, Pakaian, Paket) dari database ke dalam tabel. */
    private void muatSemuaData() { muatKnowledge(); muatPakaian(); muatPaket(); }

    /** Mengambil seluruh data Knowledge Base menggunakan DAO dan menampilkannya. */
    private void muatKnowledge() {
        List<EntriKnowledge> list = knowledgeDAO.findAll();
        tableKnowledge.setItems(FXCollections.observableArrayList(list));
    }

    /** Mengambil seluruh data Pakaian Wedding menggunakan DAO dan menampilkannya. */
    private void muatPakaian() {
        List<PakaianWedding> list = pakaianDAO.findAll();
        tablePakaian.setItems(FXCollections.observableArrayList(list));
    }

    /** Mengambil seluruh data Paket Sewa menggunakan DAO dan menampilkannya. */
    private void muatPaket() {
        List<PaketSewa> list = paketDAO.findAll();
        tablePaket.setItems(FXCollections.observableArrayList(list));
    }

    // ── Filter ────────────────────────────────────────────────────────────────
    
    /**
     * Memfilter data di tabel yang sedang aktif berdasarkan kata kunci input.
     * Filter bekerja secara dinamis (case-insensitive) mencocokkan field tertentu.
     *
     * @param keyword kata kunci pencarian dari TextField
     */
    private void filterData(String keyword) {
        String kw = keyword == null ? "" : keyword.toLowerCase();
        switch (seksiAktif) {
            case KNOWLEDGE -> {
                // Filter pertanyaan/keyword dan kategori
                ObservableList<EntriKnowledge> filtered = FXCollections.observableArrayList(
                    knowledgeDAO.findAll().stream()
                        .filter(e -> e.getPertanyaan().toLowerCase().contains(kw)
                                  || e.getKategori().toLowerCase().contains(kw))
                        .toList()
                );
                tableKnowledge.setItems(filtered);
            }
            case PAKAIAN -> {
                // Filter nama dan jenis busana
                ObservableList<PakaianWedding> filtered = FXCollections.observableArrayList(
                    pakaianDAO.findAll().stream()
                        .filter(p -> p.getNama().toLowerCase().contains(kw)
                                  || p.getKategori().toLowerCase().contains(kw))
                        .toList()
                );
                tablePakaian.setItems(filtered);
            }
            case PAKET -> {
                // Filter nama paket
                ObservableList<PaketSewa> filtered = FXCollections.observableArrayList(
                    paketDAO.findAll().stream()
                        .filter(p -> p.getNamaPaket().toLowerCase().contains(kw))
                        .toList()
                );
                tablePaket.setItems(filtered);
            }
        }
    }

    // ── Dialog CRUD ───────────────────────────────────────────────────────────
    
    /**
     * Menampilkan dialog untuk menambah atau mengedit entri Knowledge Base.
     *
     * @param existing objek entri yang ingin diedit. Jika null, berarti aksi "Tambah".
     */
    private void dialogKnowledge(EntriKnowledge existing) {
        boolean isEdit = existing != null;
        Dialog<EntriKnowledge> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Jawaban Chatbot" : "Tambah Jawaban Chatbot");
        dialog.setHeaderText("Tambahkan respons bot baru dengan memasukkan kata kunci yang biasa ditanyakan.");

        ButtonType btnSimpan = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSimpan, ButtonType.CANCEL);

        GridPane grid = buatGrid();
        
        // Tampilkan kata kunci menggunakan koma untuk admin (bukan regex)
        String kataKunciText = isEdit ? existing.getPertanyaan().replace("|", ", ") : "";
        TextField fPertanyaan = new TextField(kataKunciText);
        fPertanyaan.setPromptText("Misal: cara pesan, reservasi, booking");
        
        TextArea  fJawaban    = new TextArea(isEdit ? existing.getJawaban() : "");
        fJawaban.setPromptText("Teks yang akan dibalas oleh chatbot");
        fJawaban.setPrefRowCount(5);
        fJawaban.setWrapText(true);
        
        ComboBox<String> cbKategori = new ComboBox<>();
        cbKategori.setEditable(true); // Memungkinkan admin mengetik kategori baru
        cbKategori.getItems().addAll("INFO UMUM", "HARGA & PAKET", "RESERVASI", "LOKASI TOKO", "KETERSEDIAAN", "TENTANG BUSANA", "LAINNYA");
        cbKategori.setValue(isEdit ? existing.getKategori() : "INFO UMUM");
        
        CheckBox  cbAktif     = new CheckBox("Aktifkan Jawaban Ini");
        cbAktif.setSelected(!isEdit || existing.isAktif());

        // Menyusun UI pada GridPane
        grid.addRow(0, new Label("Kata Kunci\n(Pisahkan dgn koma):"), fPertanyaan);
        grid.addRow(1, new Label("Balasan Chatbot:"), fJawaban);
        grid.addRow(2, new Label("Kategori Topik:"), cbKategori);
        grid.addRow(3, new Label(""), cbAktif);
        dialog.getDialogPane().setContent(grid);

        // Konversi hasil inputan dialog menjadi objek EntriKnowledge saat tombol Simpan ditekan
        dialog.setResultConverter(btn -> {
            if (btn == btnSimpan) {
                // Ubah koma kembali menjadi regex | untuk ChatbotEngine
                String inputKunci = fPertanyaan.getText().trim();
                String regexPola = java.util.Arrays.stream(inputKunci.split(","))
                                         .map(String::trim)
                                         .filter(s -> !s.isEmpty())
                                         .collect(java.util.stream.Collectors.joining("|"));

                EntriKnowledge e = isEdit ? existing : new EntriKnowledge();
                e.setPertanyaan(regexPola.isEmpty() ? inputKunci : regexPola);
                e.setJawaban(fJawaban.getText().trim());
                e.setKategori(cbKategori.getValue() == null ? "UMUM" : cbKategori.getValue().toUpperCase());
                e.setAktif(cbAktif.isSelected());
                return e;
            }
            return null; // Aksi batal
        });

        // Menunggu user menutup dialog, dan menyimpan jika tombol Simpan ditekan
        dialog.showAndWait().ifPresent(e -> {
            if (e.getPertanyaan().isEmpty() || e.getJawaban().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Kata Kunci dan Balasan tidak boleh kosong!");
                return;
            }
            // Simpan perubahan ke database
            boolean ok = isEdit ? knowledgeDAO.update(e) : knowledgeDAO.save(e);
            if (ok) muatKnowledge(); // Refresh tabel
            else showAlert(Alert.AlertType.ERROR, "Gagal menyimpan knowledge base.");
        });
    }

    /**
     * Menampilkan dialog untuk menambah atau mengedit data Pakaian Wedding.
     * Termasuk fungsionalitas untuk memilih dan menyimpan foto dalam bentuk BLOB.
     *
     * @param existing objek pakaian yang ingin diedit. Jika null, berarti aksi "Tambah".
     */
    private void dialogPakaian(PakaianWedding existing) {
        boolean isEdit = existing != null;
        Dialog<PakaianWedding> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Pakaian" : "Tambah Pakaian");

        ButtonType btnSimpan = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSimpan, ButtonType.CANCEL);

        GridPane grid = buatGrid();
        
        // Inisialisasi komponen form
        TextField fNama    = new TextField(isEdit ? existing.getNama() : "");
        fNama.setPromptText("Nama pakaian");
        
        ComboBox<String> cbJenis = new ComboBox<>();
        cbJenis.getItems().addAll("Modern","Tradisional","Muslim","Internasional","Bertema","Pre-Wedding","Keluarga","Pesta");
        cbJenis.setValue(isEdit ? existing.getKategori() : "Modern");
        
        ComboBox<String> cbGender = new ComboBox<>();
        cbGender.getItems().addAll("Wanita", "Pria", "Unisex");
        cbGender.setValue(isEdit && existing.getGender() != null ? existing.getGender() : "Wanita");

        TextField fUkuran  = new TextField(isEdit ? existing.getUkuranTersedia() : "");
        fUkuran.setPromptText("S,M,L,XL");
        
        TextField fHarga   = new TextField(isEdit ? String.valueOf((long)existing.getHargaSewa()) : "");
        fHarga.setPromptText("Harga dalam Rupiah");
        
        TextArea fDeskripsi = new TextArea(isEdit && existing.getDeskripsi() != null ? existing.getDeskripsi() : "");
        fDeskripsi.setPromptText("Deskripsi detail busana");
        fDeskripsi.setPrefRowCount(3);
        fDeskripsi.setWrapText(true);

        // ── Foto: simpan sebagai byte[] BLOB, bukan path ──
        // Holder berukuran 1 untuk menyimpan referensi byte array dari callback
        final byte[][] selectedImageData = { isEdit ? existing.getImageData() : null };

        Label lblFotoStatus = new Label(isEdit && existing.hasImage() ? "✓ Foto tersimpan di database" : "Belum ada foto");
        lblFotoStatus.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");

        // Preview gambar jika ada data BLOB yang sudah tersimpan
        javafx.scene.image.ImageView imgPreview = new javafx.scene.image.ImageView();
        imgPreview.setFitWidth(120);
        imgPreview.setFitHeight(160);
        imgPreview.setPreserveRatio(true);
        if (isEdit && existing.hasImage()) {
            try {
                // Konversi byte array ke Image JavaFX
                java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(existing.getImageData());
                imgPreview.setImage(new javafx.scene.image.Image(bais));
            } catch (Exception ignored) {}
        }

        // Tombol untuk membuka File Chooser pemilihan foto
        Button btnBrowse = new Button("Pilih Foto");
        btnBrowse.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("Pilih Foto Busana");
            fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            java.io.File file = fc.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                try {
                    // Baca semua byte dari file gambar
                    selectedImageData[0] = java.nio.file.Files.readAllBytes(file.toPath());
                    lblFotoStatus.setText("✓ " + file.getName() + " (" + (selectedImageData[0].length / 1024) + " KB)");
                    lblFotoStatus.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px;");
                    // Update preview di UI
                    java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(selectedImageData[0]);
                    imgPreview.setImage(new javafx.scene.image.Image(bais));
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Gagal membaca file gambar: " + ex.getMessage());
                }
            }
        });

        // Tombol untuk menghapus foto yang ada
        Button btnHapusFoto = new Button("Hapus Foto");
        btnHapusFoto.setOnAction(e -> {
            selectedImageData[0] = null;
            lblFotoStatus.setText("Foto dihapus");
            lblFotoStatus.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
            imgPreview.setImage(null);
        });

        // Mengelompokkan tombol foto dan preview dalam HBox/VBox
        javafx.scene.layout.HBox boxButtons = new javafx.scene.layout.HBox(5, btnBrowse, btnHapusFoto);
        VBox boxFoto = new VBox(5, boxButtons, lblFotoStatus, imgPreview);

        CheckBox  cbTersedia = new CheckBox("Tersedia");
        cbTersedia.setSelected(!isEdit || existing.isTersedia());

        // Menyusun UI form pakaian
        grid.addRow(0, new Label("Nama:"),      fNama);
        grid.addRow(1, new Label("Jenis:"),     cbJenis);
        grid.addRow(2, new Label("Gender:"),    cbGender);
        grid.addRow(3, new Label("Ukuran:"),    fUkuran);
        grid.addRow(4, new Label("Harga:"),     fHarga);
        grid.addRow(5, new Label("Deskripsi:"), fDeskripsi);
        grid.addRow(6, new Label("Foto:"),      boxFoto);
        grid.addRow(7, new Label(""),           cbTersedia);
        dialog.getDialogPane().setContent(grid);

        // Konversi hasil input form ke objek Model PakaianWedding
        dialog.setResultConverter(btn -> {
            if (btn == btnSimpan) {
                PakaianWedding p = isEdit ? existing : new PakaianWedding();
                p.setNama(fNama.getText().trim());
                p.setKategori(cbJenis.getValue());
                p.setGender(cbGender.getValue());
                p.setUkuranTersedia(fUkuran.getText().trim());
                p.setDeskripsi(fDeskripsi.getText().trim());
                p.setImageData(selectedImageData[0]);
                try { 
                    p.setHargaSewa(Double.parseDouble(fHarga.getText().trim())); 
                } catch (NumberFormatException ex) { 
                    p.setHargaSewa(0); 
                }
                p.setTersedia(cbTersedia.isSelected());
                return p;
            }
            return null;
        });

        // Simpan data jika OK ditekan
        dialog.showAndWait().ifPresent(p -> {
            boolean ok = isEdit ? pakaianDAO.update(p) : pakaianDAO.save(p);
            if (ok) muatPakaian();
            else showAlert(Alert.AlertType.ERROR, "Gagal menyimpan pakaian.");
        });
    }

    /**
     * Menampilkan dialog untuk menambah atau mengedit data Paket Sewa.
     *
     * @param existing objek paket yang ingin diedit. Jika null, berarti aksi "Tambah".
     */
    private void dialogPaket(PaketSewa existing) {
        boolean isEdit = existing != null;
        Dialog<PaketSewa> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Paket" : "Tambah Paket");

        ButtonType btnSimpan = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSimpan, ButtonType.CANCEL);

        GridPane grid = buatGrid();
        
        // Komponen form Paket
        TextField fNama      = new TextField(isEdit ? existing.getNamaPaket() : "");
        fNama.setPromptText("Nama paket");
        
        TextArea  fDeskripsi = new TextArea(isEdit ? existing.getDeskripsi() : "");
        fDeskripsi.setPrefRowCount(3);
        
        TextField fHarga     = new TextField(isEdit ? String.valueOf((long)existing.getHargaTotal()) : "");
        fHarga.setPromptText("Harga total");

        grid.addRow(0, new Label("Nama:"),      fNama);
        grid.addRow(1, new Label("Deskripsi:"), fDeskripsi);
        grid.addRow(2, new Label("Harga:"),     fHarga);
        dialog.getDialogPane().setContent(grid);

        // Konversi dan validasi hasil
        dialog.setResultConverter(btn -> {
            if (btn == btnSimpan) {
                PaketSewa p = isEdit ? existing : new PaketSewa();
                p.setNamaPaket(fNama.getText().trim());
                p.setDeskripsi(fDeskripsi.getText().trim());
                try { 
                    p.setHargaTotal(Double.parseDouble(fHarga.getText().trim())); 
                } catch (NumberFormatException ex) { 
                    p.setHargaTotal(0); 
                }
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> {
            boolean ok = isEdit ? paketDAO.update(p) : paketDAO.save(p);
            if (ok) muatPaket();
            else showAlert(Alert.AlertType.ERROR, "Gagal menyimpan paket.");
        });
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    
    /**
     * Membuat GridPane dasar yang sudah diberi style untuk digunakan pada isi dialog.
     * Mengatur jarak, padding, dan lebar kolom agar seragam dan rapi.
     *
     * @return GridPane instance yang sudah diformat
     */
    private GridPane buatGrid() {
        GridPane g = new GridPane();
        g.setHgap(15); 
        g.setVgap(12);
        g.setPadding(new Insets(25));
        g.setPrefWidth(600); // Widened from 500

        // Explicitly set column widths to prevent truncation (...)
        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setMinWidth(120);
        col1.setPrefWidth(120);
        
        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        
        g.getColumnConstraints().addAll(col1, col2);
        
        return g;
    }

    /**
     * Helper untuk menampilkan kotak dialog notifikasi/alert kepada user.
     *
     * @param type Tipe alert (WARNING, ERROR, INFO)
     * @param msg Teks pesan yang ingin ditampilkan
     */
    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg, ButtonType.OK).showAndWait();
    }
}
