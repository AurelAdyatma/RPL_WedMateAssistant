package com.rplbo.app.rpl_wedmateassistant.controller;

import com.rplbo.app.rpl_wedmateassistant.database.KnowledgeBaseDAO;
import com.rplbo.app.rpl_wedmateassistant.database.PakaianDAO;
import com.rplbo.app.rpl_wedmateassistant.database.PaketDAO;
import com.rplbo.app.rpl_wedmateassistant.model.EntriKnowledge;
import com.rplbo.app.rpl_wedmateassistant.model.PakaianWedding;
import com.rplbo.app.rpl_wedmateassistant.model.PaketSewa;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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
 */
public class AdminController {

    // ── FXML ─────────────────────────────────────────────────────────────────
    @FXML private Label         lblSection;
    @FXML private Button        btnSectionKnowledge;
    @FXML private Button        btnSectionPakaian;
    @FXML private Button        btnSectionPaket;
    @FXML private Button        btnLogout;

    @FXML private VBox          sectionKnowledge;
    @FXML private VBox          sectionPakaian;
    @FXML private VBox          sectionPaket;

    @FXML private TableView<EntriKnowledge>  tableKnowledge;
    @FXML private TableView<PakaianWedding>  tablePakaian;
    @FXML private TableView<PaketSewa>       tablePaket;

    @FXML private TextField     txtCari;
    @FXML private Button        btnTambah;
    @FXML private Button        btnEdit;
    @FXML private Button        btnHapus;

    // ── DAO ───────────────────────────────────────────────────────────────────
    private final KnowledgeBaseDAO knowledgeDAO = new KnowledgeBaseDAO();
    private final PakaianDAO       pakaianDAO   = new PakaianDAO();
    private final PaketDAO         paketDAO     = new PaketDAO();

    // ── State ─────────────────────────────────────────────────────────────────
    private enum Seksi { KNOWLEDGE, PAKAIAN, PAKET }
    private Seksi seksiAktif = Seksi.KNOWLEDGE;

    // ── Init ──────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        setupKnowledgeTable();
        setupPakaianTable();
        setupPaketTable();
        tampilkanSeksi(Seksi.KNOWLEDGE);
        muatSemuaData();

        // Filter pencarian real-time
        txtCari.textProperty().addListener((obs, o, keyword) -> filterData(keyword));
    }

    // ── Sidebar handlers ──────────────────────────────────────────────────────
    @FXML private void handleSeksiKnowledge() { tampilkanSeksi(Seksi.KNOWLEDGE); }
    @FXML private void handleSeksiPakaian()   { tampilkanSeksi(Seksi.PAKAIAN);   }
    @FXML private void handleSeksiPaket()     { tampilkanSeksi(Seksi.PAKET);     }

    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/rplbo/app/rpl_wedmateassistant/view/Login.fxml")
            );
            Parent root = loader.load();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("WedMate - Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Gagal kembali ke login: " + e.getMessage());
        }
    }

    private void tampilkanSeksi(Seksi seksi) {
        seksiAktif = seksi;
        sectionKnowledge.setVisible(seksi == Seksi.KNOWLEDGE);
        sectionKnowledge.setManaged(seksi == Seksi.KNOWLEDGE);
        sectionPakaian.setVisible(seksi == Seksi.PAKAIAN);
        sectionPakaian.setManaged(seksi == Seksi.PAKAIAN);
        sectionPaket.setVisible(seksi == Seksi.PAKET);
        sectionPaket.setManaged(seksi == Seksi.PAKET);

        String[] judul = {"Knowledge Base (Chatbot)", "Katalog Pakaian Wedding", "Paket Sewa"};
        lblSection.setText(judul[seksi.ordinal()]);
        txtCari.clear();

        // Update button styles
        String styleActive = "-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 13px; -fx-alignment: CENTER_LEFT; -fx-padding: 12 16; -fx-background-radius: 8; -fx-cursor: hand;";
        String styleInactive = "-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-size: 13px; -fx-alignment: CENTER_LEFT; -fx-padding: 12 16; -fx-background-radius: 8; -fx-cursor: hand;";

        btnSectionKnowledge.setStyle(seksi == Seksi.KNOWLEDGE ? styleActive : styleInactive);
        btnSectionPakaian.setStyle(seksi == Seksi.PAKAIAN ? styleActive : styleInactive);
        btnSectionPaket.setStyle(seksi == Seksi.PAKET ? styleActive : styleInactive);
    }

    // ── CRUD handlers ─────────────────────────────────────────────────────────
    @FXML private void handleTambah() {
        switch (seksiAktif) {
            case KNOWLEDGE -> dialogKnowledge(null);
            case PAKAIAN   -> dialogPakaian(null);
            case PAKET     -> dialogPaket(null);
        }
    }

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

    @FXML private void handleHapus() {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION,
            "Yakin ingin menghapus data ini?", ButtonType.YES, ButtonType.NO);
        konfirmasi.setTitle("Konfirmasi Hapus");
        Optional<ButtonType> result = konfirmasi.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES) return;

        boolean ok = false;
        switch (seksiAktif) {
            case KNOWLEDGE -> {
                EntriKnowledge sel = tableKnowledge.getSelectionModel().getSelectedItem();
                if (sel == null) { showAlert(Alert.AlertType.WARNING, "Pilih entri yang ingin dihapus."); return; }
                ok = knowledgeDAO.delete(sel.getId());
                if (ok) muatKnowledge();
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
    @SuppressWarnings("unchecked")
    private void setupKnowledgeTable() {
        TableColumn<EntriKnowledge, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(40);

        TableColumn<EntriKnowledge, String> colPertanyaan = new TableColumn<>("Kata Kunci (Trigger)");
        colPertanyaan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPertanyaan().replace("|", ", ")));
        colPertanyaan.setPrefWidth(200);

        TableColumn<EntriKnowledge, String> colJawaban = new TableColumn<>("Jawaban");
        colJawaban.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJawaban()));
        colJawaban.setPrefWidth(300);

        TableColumn<EntriKnowledge, String> colKategori = new TableColumn<>("Kategori");
        colKategori.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKategori()));
        colKategori.setPrefWidth(120);

        TableColumn<EntriKnowledge, Boolean> colAktif = new TableColumn<>("Aktif");
        colAktif.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isAktif()));
        colAktif.setPrefWidth(60);

        tableKnowledge.getColumns().setAll(colId, colPertanyaan, colJawaban, colKategori, colAktif);
        tableKnowledge.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @SuppressWarnings("unchecked")
    private void setupPakaianTable() {
        TableColumn<PakaianWedding, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(40);

        TableColumn<PakaianWedding, String> colNama = new TableColumn<>("Nama");
        colNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNama()));
        colNama.setPrefWidth(200);

        TableColumn<PakaianWedding, String> colJenis = new TableColumn<>("Jenis");
        colJenis.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKategori()));
        colJenis.setPrefWidth(120);

        TableColumn<PakaianWedding, String> colUkuran = new TableColumn<>("Ukuran");
        colUkuran.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUkuranTersedia()));
        colUkuran.setPrefWidth(80);

        TableColumn<PakaianWedding, String> colGender = new TableColumn<>("Gender");
        colGender.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGender()));
        colGender.setPrefWidth(80);

        TableColumn<PakaianWedding, Double> colHarga = new TableColumn<>("Harga Sewa");
        colHarga.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getHargaSewa()).asObject());
        colHarga.setPrefWidth(110);

        TableColumn<PakaianWedding, Boolean> colTersedia = new TableColumn<>("Tersedia");
        colTersedia.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isTersedia()));
        colTersedia.setPrefWidth(70);

        tablePakaian.getColumns().setAll(colId, colNama, colJenis, colUkuran, colGender, colHarga, colTersedia);
        tablePakaian.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @SuppressWarnings("unchecked")
    private void setupPaketTable() {
        TableColumn<PaketSewa, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(40);

        TableColumn<PaketSewa, String> colNama = new TableColumn<>("Nama Paket");
        colNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNamaPaket()));
        colNama.setPrefWidth(150);

        TableColumn<PaketSewa, String> colDeskripsi = new TableColumn<>("Deskripsi");
        colDeskripsi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeskripsi()));
        colDeskripsi.setPrefWidth(260);

        TableColumn<PaketSewa, Double> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getHargaTotal()).asObject());
        colHarga.setPrefWidth(110);

        tablePaket.getColumns().setAll(colId, colNama, colDeskripsi, colHarga);
        tablePaket.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // ── Load Data ─────────────────────────────────────────────────────────────
    private void muatSemuaData() { muatKnowledge(); muatPakaian(); muatPaket(); }

    private void muatKnowledge() {
        List<EntriKnowledge> list = knowledgeDAO.findAll();
        tableKnowledge.setItems(FXCollections.observableArrayList(list));
    }

    private void muatPakaian() {
        List<PakaianWedding> list = pakaianDAO.findAll();
        tablePakaian.setItems(FXCollections.observableArrayList(list));
    }

    private void muatPaket() {
        List<PaketSewa> list = paketDAO.findAll();
        tablePaket.setItems(FXCollections.observableArrayList(list));
    }

    // ── Filter ────────────────────────────────────────────────────────────────
    private void filterData(String keyword) {
        String kw = keyword == null ? "" : keyword.toLowerCase();
        switch (seksiAktif) {
            case KNOWLEDGE -> {
                ObservableList<EntriKnowledge> filtered = FXCollections.observableArrayList(
                    knowledgeDAO.findAll().stream()
                        .filter(e -> e.getPertanyaan().toLowerCase().contains(kw)
                                  || e.getKategori().toLowerCase().contains(kw))
                        .toList()
                );
                tableKnowledge.setItems(filtered);
            }
            case PAKAIAN -> {
                ObservableList<PakaianWedding> filtered = FXCollections.observableArrayList(
                    pakaianDAO.findAll().stream()
                        .filter(p -> p.getNama().toLowerCase().contains(kw)
                                  || p.getKategori().toLowerCase().contains(kw))
                        .toList()
                );
                tablePakaian.setItems(filtered);
            }
            case PAKET -> {
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
        fJawaban.setPromptText("Teks yang akan dibalas oleh chatbot...");
        fJawaban.setPrefRowCount(5);
        fJawaban.setWrapText(true);
        
        ComboBox<String> cbKategori = new ComboBox<>();
        cbKategori.setEditable(true);
        cbKategori.getItems().addAll("INFO UMUM", "HARGA & PAKET", "RESERVASI", "LOKASI TOKO", "KETERSEDIAAN", "TENTANG BUSANA", "LAINNYA");
        cbKategori.setValue(isEdit ? existing.getKategori() : "INFO UMUM");
        
        CheckBox  cbAktif     = new CheckBox("Aktifkan Jawaban Ini");
        cbAktif.setSelected(!isEdit || existing.isAktif());

        grid.addRow(0, new Label("Kata Kunci\n(Pisahkan dgn koma):"), fPertanyaan);
        grid.addRow(1, new Label("Balasan Chatbot:"), fJawaban);
        grid.addRow(2, new Label("Kategori Topik:"), cbKategori);
        grid.addRow(3, new Label(""), cbAktif);
        dialog.getDialogPane().setContent(grid);

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
            return null;
        });

        dialog.showAndWait().ifPresent(e -> {
            if (e.getPertanyaan().isEmpty() || e.getJawaban().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Kata Kunci dan Balasan tidak boleh kosong!");
                return;
            }
            boolean ok = isEdit ? knowledgeDAO.update(e) : knowledgeDAO.save(e);
            if (ok) muatKnowledge();
            else showAlert(Alert.AlertType.ERROR, "Gagal menyimpan knowledge base.");
        });
    }

    private void dialogPakaian(PakaianWedding existing) {
        boolean isEdit = existing != null;
        Dialog<PakaianWedding> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Pakaian" : "Tambah Pakaian");

        ButtonType btnSimpan = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSimpan, ButtonType.CANCEL);

        GridPane grid = buatGrid();
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
        CheckBox  cbTersedia = new CheckBox("Tersedia");
        cbTersedia.setSelected(!isEdit || existing.isTersedia());

        grid.addRow(0, new Label("Nama:"),    fNama);
        grid.addRow(1, new Label("Jenis:"),   cbJenis);
        grid.addRow(2, new Label("Gender:"),  cbGender);
        grid.addRow(3, new Label("Ukuran:"),  fUkuran);
        grid.addRow(4, new Label("Harga:"),   fHarga);
        grid.addRow(5, new Label(""),         cbTersedia);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnSimpan) {
                PakaianWedding p = isEdit ? existing : new PakaianWedding();
                p.setNama(fNama.getText().trim());
                p.setKategori(cbJenis.getValue());
                p.setGender(cbGender.getValue());
                p.setUkuranTersedia(fUkuran.getText().trim());
                try { p.setHargaSewa(Double.parseDouble(fHarga.getText().trim())); } catch (NumberFormatException ex) { p.setHargaSewa(0); }
                p.setTersedia(cbTersedia.isSelected());
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> {
            boolean ok = isEdit ? pakaianDAO.update(p) : pakaianDAO.save(p);
            if (ok) muatPakaian();
            else showAlert(Alert.AlertType.ERROR, "Gagal menyimpan pakaian.");
        });
    }

    private void dialogPaket(PaketSewa existing) {
        boolean isEdit = existing != null;
        Dialog<PaketSewa> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Paket" : "Tambah Paket");

        ButtonType btnSimpan = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSimpan, ButtonType.CANCEL);

        GridPane grid = buatGrid();
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

        dialog.setResultConverter(btn -> {
            if (btn == btnSimpan) {
                PaketSewa p = isEdit ? existing : new PaketSewa();
                p.setNamaPaket(fNama.getText().trim());
                p.setDeskripsi(fDeskripsi.getText().trim());
                try { p.setHargaTotal(Double.parseDouble(fHarga.getText().trim())); } catch (NumberFormatException ex) { p.setHargaTotal(0); }
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
    private GridPane buatGrid() {
        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10);
        g.setPadding(new Insets(20));
        g.setPrefWidth(500);
        return g;
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg, ButtonType.OK).showAndWait();
    }
}
