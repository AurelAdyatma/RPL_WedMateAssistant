package com.rplbo.app.rpl_wedmateassistant.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controller untuk panel Admin.
 * Mengelola CRUD knowledge base, katalog pakaian, dan paket sewa.
 */
public class AdminController {

    @FXML private TableView<?> tableKnowledge;
    @FXML private TableView<?> tablePakaian;
    @FXML private TableView<?> tablePaket;
    @FXML private Button btnTambah;
    @FXML private Button btnEdit;
    @FXML private Button btnHapus;
    @FXML private TextField txtCari;

    @FXML
    public void initialize() {
        // Muat data dari database
    }

    @FXML
    private void handleTambah() {
        // Logika penambahan data
    }

    @FXML
    private void handleEdit() {
        // Logika pengeditan data
    }

    @FXML
    private void handleHapus() {
        // Logika penghapusan data
    }
}
