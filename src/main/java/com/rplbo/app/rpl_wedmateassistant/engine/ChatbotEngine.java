package com.rplbo.app.rpl_wedmateassistant.engine;

import com.rplbo.app.rpl_wedmateassistant.engine.RegexMatcher.Kategori;
import com.rplbo.app.rpl_wedmateassistant.model.EntriKnowledge;
import com.rplbo.app.rpl_wedmateassistant.model.PakaianWedding;
import com.rplbo.app.rpl_wedmateassistant.model.PaketSewa;
import com.rplbo.app.rpl_wedmateassistant.model.Pesan;
import com.rplbo.app.rpl_wedmateassistant.model.Sesi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Orkestrator utama chatbot WedMate Assistant.
 *
 * <p>
 * Alur pemrosesan per pesan pengguna:
 * </p>
 * 
 * <pre>
 *   Input teks
 *      ↓
 *   RegexMatcher.deteksiKategori()   ← built-in regex per kategori
 *      ↓
 *   RegexMatcher.cocokkan()          ← cari entri spesifik dari DB
 *      ↓
 *   ResponseGenerator.generate()    ← prioritas: DB > default > fallback
 *      ↓
 *   Pesan (bot) → disimpan ke Sesi
 * </pre>
 *
 * <p>
 * Data knowledge base di-inject dari luar (Controller) via
 * {@link #setDaftarEntri(List)} agar engine tidak langsung bergantung pada DAO.
 * </p>
 */
public class ChatbotEngine {

    // ── Dependensi ────────────────────────────────────────────────────────────

    private final RegexMatcher regexMatcher;
    private final ResponseGenerator responseGenerator;

    /** Cache entri knowledge base dari database. */
    private List<EntriKnowledge> daftarEntri = new ArrayList<>();

    /** Cache data pakaian dari database. */
    private List<PakaianWedding> daftarPakaian = new ArrayList<>();

    /** Cache data paket sewa dari database. */
    private List<PaketSewa> daftarPaket = new ArrayList<>();

    // ── Constructor ───────────────────────────────────────────────────────────

    public ChatbotEngine() {
        this.regexMatcher = new RegexMatcher();
        this.responseGenerator = new ResponseGenerator();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Memproses satu pesan pengguna dan mengembalikan balasan dari bot.
     *
     * <ol>
     * <li>Normalisasi teks input</li>
     * <li>Deteksi kategori via built-in regex</li>
     * <li>Cari entri spesifik di knowledge base DB</li>
     * <li>Generate teks respons (DB &gt; default per kategori &gt; fallback)</li>
     * <li>Bungkus dalam objek {@link Pesan} dan tambahkan ke {@link Sesi}</li>
     * </ol>
     *
     * @param inputPengguna teks mentah yang dikirim pengguna
     * @param sesi          sesi percakapan aktif (boleh null jika tidak di-track)
     * @return {@link Pesan} balasan dari bot, tidak pernah null
     */
    public Pesan prosesPesan(String inputPengguna, Sesi sesi) {
        // ── 1. Guard: input kosong
        if (inputPengguna == null || inputPengguna.isBlank()) {
            return buatPesanBot("Silakan ketik pertanyaan Anda.", sesi);
        }

        // ── 2. Deteksi kategori
        Kategori kategori = regexMatcher.deteksiKategori(inputPengguna);

        // ── 3. Cari entri di knowledge base DB
        EntriKnowledge entriDB = regexMatcher.cocokkan(inputPengguna, daftarEntri);
        if (entriDB == null && kategori != Kategori.TIDAK_DIKENAL) {
            entriDB = regexMatcher.cariPerKategori(kategori, daftarEntri);
        }

        // ── 4. Coba generate respons dari pakaian DB jika kategori busana atau gender
        String responsPakaian = null;
        List<byte[]> attachedImages = new ArrayList<>();

        String warnaDicari = ekstrakWarna(inputPengguna);
        String ukuranDicari = ekstrakUkuran(inputPengguna);
        String jenisDicari = ekstrakJenisBusana(inputPengguna);
        long budgetBusana = extractBudget(inputPengguna);

        boolean isPencarianBusana = warnaDicari != null
                || ukuranDicari != null
                || jenisDicari != null
                || mengandungFilterHargaBusana(inputPengguna);

        boolean isDetailRequest = inputPengguna.toLowerCase().matches(".*\\b(detail|contoh|foto|gambar|spesifikasi|wujud|tampil|penampakan)\\b.*");

        if (isPencarianBusana) {
            responsPakaian = generateResponsPencarianKombinasi(inputPengguna, warnaDicari, ukuranDicari, jenisDicari, budgetBusana);
        } else if (isDetailRequest) {
            responsPakaian = generateDetailPakaian(inputPengguna, kategori, attachedImages);
        } else if (kategori == Kategori.BUSANA_PRIA || kategori == Kategori.BUSANA_WANITA) {
            responsPakaian = generateResponsGender(kategori);
        } else if (kategori == Kategori.REKOMENDASI_UKURAN) {
            responsPakaian = generateRekomendasiUkuran(inputPengguna);
        } else if (kategori == Kategori.HARGA_PAKET && mengandungBudget(inputPengguna)) {
            responsPakaian = generateRekomendasiPaketBudget(inputPengguna);
        } else if (kategori != null && (kategori.name().startsWith("BUSANA_") || kategori == Kategori.LIHAT_BUSANA)) {
            if (kategori == Kategori.LIHAT_BUSANA) {
                responsPakaian = null; // Biarkan fallback ke ResponseGenerator default
            } else {
                responsPakaian = generateResponsPakaian(inputPengguna, kategori);
            }
        }

        // ── 5. Generate respons final
        String teksRespons;
        if (responsPakaian != null) {
            teksRespons = responsPakaian;
        } else {
            teksRespons = responseGenerator.generate(entriDB, kategori);
        }

        System.out.printf("[ChatbotEngine] Input: \"%s\" -> Kategori: %s | EntriDB: %s%n",
                inputPengguna, kategori,
                entriDB != null ? "id=" + entriDB.getId() : "null");

        Pesan pesanBot = buatPesanBot(teksRespons, sesi);
        pesanBot.setImageDataList(attachedImages);
        return pesanBot;
    }

    /**
     * Menginjeksikan daftar entri knowledge base dari database.
     */
    public void setDaftarEntri(List<EntriKnowledge> daftarEntri) {
        this.daftarEntri = (daftarEntri != null) ? daftarEntri : new ArrayList<>();
        regexMatcher.bersihkanCache();
        System.out.println("[ChatbotEngine] Knowledge base dimuat: "
                + this.daftarEntri.size() + " entri.");
    }

    /**
     * Menginjeksikan daftar pakaian wedding dari database.
     */
    public void setDaftarPakaian(List<PakaianWedding> daftarPakaian) {
        this.daftarPakaian = (daftarPakaian != null) ? daftarPakaian : new ArrayList<>();
        regexMatcher.updateDynamicPatterns(this.daftarPakaian);
        System.out.println("[ChatbotEngine] Pakaian dimuat: "
                + this.daftarPakaian.size() + " item.");
    }

    /**
     * Menginjeksikan daftar paket sewa dari database.
     */
    public void setDaftarPaket(List<PaketSewa> daftarPaket) {
        this.daftarPaket = (daftarPaket != null) ? daftarPaket : new ArrayList<>();
        System.out.println("[ChatbotEngine] Paket sewa dimuat: "
                + this.daftarPaket.size() + " paket.");
    }

    /** Mengembalikan jumlah entri knowledge base yang saat ini di-cache. */
    public int getJumlahEntri() {
        return daftarEntri.size();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Membuat objek {@link Pesan} dari teks bot dan menambahkannya ke sesi aktif.
     *
     * @param teks teks balasan bot
     * @param sesi sesi percakapan (boleh null)
     * @return objek Pesan yang sudah dibuat
     */
    private Pesan buatPesanBot(String teks, Sesi sesi) {
        Pesan pesan = new Pesan();
        pesan.setIsiPesan(teks);
        pesan.setDariBot(true);

        if (sesi != null) {
            pesan.setSesiId(sesi.getId());
            sesi.getDaftarPesan().add(pesan);
        }

        return pesan;
    }

    /**
     * Menghasilkan respons pakaian secara dinamis dari database berdasarkan
     * kategori busana yang terdeteksi. Jika bukan kategori busana, kembalikan null.
     */
    private String generateResponsGender(Kategori kategori) {
        if (kategori == null)
            return null;

        String genderTarget = kategori == Kategori.BUSANA_PRIA ? "Pria" : "Wanita";

        List<PakaianWedding> cocok = daftarPakaian.stream()
                .filter(p -> p.getGender() != null &&
                        (p.getGender().equalsIgnoreCase(genderTarget) || p.getGender().equalsIgnoreCase("Unisex")))
                .toList();

        if (cocok.isEmpty()) {
            return "[ Koleksi Busana " + genderTarget + " ]\n\n" +
                    "Maaf, saat ini koleksi untuk busana " + genderTarget.toLowerCase()
                    + " sedang kosong atau belum tersedia.\n" +
                    "Silakan cek kategori lain atau hubungi admin kami.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[ Koleksi Busana ").append(genderTarget).append(" ]\n\n");
        sb.append("Berikut koleksi yang tersedia untuk ").append(genderTarget).append(":\n\n");

        for (PakaianWedding p : cocok) {
            sb.append("• ").append(p.getNama()).append(" (").append(p.getKategori()).append(")\n");
            sb.append("  Ukuran : ").append(p.getUkuranTersedia()).append("\n");
            sb.append("  Harga  : Rp ").append(String.format("%,d", (long) p.getHargaSewa())).append("/hari\n");
            if (!p.isTersedia()) {
                sb.append("  Status : SEDANG DISEWA\n");
            }
            sb.append("\n");
        }

        sb.append("Ketik 'detail [nama busana/kategori]' untuk melihat detail dan foto busana.");
        return sb.toString();
    }

    /**
     * Mengekstrak angka tinggi/berat dari input dan memberikan rekomendasi ukuran.
     */
    private String generateRekomendasiUkuran(String input) {
        String normal = input.toLowerCase().replaceAll("[^0-9a-z\\s]", "");

        // Cari angka tinggi badan (biasanya diikuti cm atau diawali "tinggi" atau "tb")
        Integer tinggi = extractNumber(normal, "tinggi|tb", "cm");
        // Cari angka berat badan (biasanya diikuti kg atau diawali "berat" atau "bb")
        Integer berat = extractNumber(normal, "berat|bb", "kg");

        // Coba cari angka saja untuk mengisi yang masih kosong (contoh: "170 60")
        if (tinggi == null || berat == null) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(normal);
            while (m.find()) {
                int val = Integer.parseInt(m.group());
                if (val > 100 && tinggi == null) {
                    tinggi = val; // Asumsi angka di atas 100 adalah tinggi
                } else if (val <= 100 && berat == null) {
                    berat = val; // Asumsi angka 100 ke bawah adalah berat
                }
            }
        }

        if (tinggi == null && berat == null) {
            return "Mohon maaf, saya belum bisa menentukan ukuran Anda. Bisa informasikan tinggi badan (cm) dan berat badan (kg) Anda?";
        }

        String sizeTinggi = null;
        if (tinggi != null) {
            if (tinggi < 155)
                sizeTinggi = "S";
            else if (tinggi < 170)
                sizeTinggi = "M";
            else if (tinggi < 185)
                sizeTinggi = "L";
            else
                sizeTinggi = "XL";
        }

        String sizeBerat = null;
        if (berat != null) {
            if (berat < 50)
                sizeBerat = "S";
            else if (berat < 65)
                sizeBerat = "M";
            else if (berat < 80)
                sizeBerat = "L";
            else
                sizeBerat = "XL";
        }

        // Tentukan ukuran final (ambil yang paling besar jika ada dua data)
        String finalSize = "M"; // default
        if (sizeTinggi != null && sizeBerat != null) {
            finalSize = compareSize(sizeTinggi, sizeBerat) >= 0 ? sizeTinggi : sizeBerat;
        } else if (sizeTinggi != null) {
            finalSize = sizeTinggi;
        } else if (sizeBerat != null) {
            finalSize = sizeBerat;
        }

        StringBuilder res = new StringBuilder();
        res.append("[ Rekomendasi Ukuran ]\n\n");
        res.append("Berdasarkan data yang Anda berikan:\n");
        if (tinggi != null)
            res.append("- Tinggi: ").append(tinggi).append(" cm\n");
        if (berat != null)
            res.append("- Berat: ").append(berat).append(" kg\n");
        res.append("\nKami merekomendasikan ukuran: ").append(finalSize).append("\n\n");
        res.append(
                "Catatan: Rekomendasi ini bersifat perkiraan. Kami sangat menyarankan Anda untuk melakukan fitting langsung di toko kami untuk kenyamanan maksimal.");

        return res.toString();
    }

    private Integer extractNumber(String input, String keyword, String unit) {
        // Pola: "tinggi 170" atau "170cm" atau "170 cm"
        String regex = "(?:(?:" + keyword + ")\\s*(\\d+))|(\\d+)\\s*(?:" + unit + ")";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(input);
        if (m.find()) {
            String val = m.group(1) != null ? m.group(1) : m.group(2);
            return Integer.parseInt(val);
        }
        return null;
    }

    private int compareSize(String s1, String s2) {
        List<String> order = List.of("XS", "S", "M", "L", "XL", "XXL");
        return order.indexOf(s1) - order.indexOf(s2);
    }

    /**
     * Mengecek apakah input pengguna memang membahas budget/dana.
     */
    private boolean mengandungBudget(String input) {
        if (input == null) return false;

        String normal = input.toLowerCase();
        return normal.matches(".*\\b(budget|dana|uang|modal|maksimal|max|dibawah|di bawah|sekitar|rekomendasi|cocok)\\b.*\\d+.*")
                || normal.matches(".*\\d+\\s*(juta|jt|ribu|rb|k).*");
    }

    /**
     * Menghasilkan rekomendasi paket sewa berdasarkan budget yang disebutkan pengguna.
     */
    private String generateRekomendasiPaketBudget(String input) {
        long budget = extractBudget(input);

        if (budget <= 0) {
            return "[ Rekomendasi Paket Berdasarkan Budget ]\n\n" +
                    "Boleh sebutkan budget Anda terlebih dahulu?\n" +
                    "Contoh: \"budget saya 3 juta\" atau \"paket di bawah 2500000\".";
        }

        if (daftarPaket == null || daftarPaket.isEmpty()) {
            return "[ Rekomendasi Paket Berdasarkan Budget ]\n\n" +
                    "Maaf, data paket sewa belum tersedia saat ini. Silakan hubungi admin untuk info paket terbaru.";
        }

        List<PaketSewa> paketSesuai = daftarPaket.stream()
                .filter(p -> p.getHargaTotal() <= budget)
                .sorted(Comparator.comparingDouble(PaketSewa::getHargaTotal).reversed())
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("[ Rekomendasi Paket Berdasarkan Budget ]\n\n");
        sb.append("Budget Anda: Rp ").append(String.format("%,d", budget)).append("\n\n");

        if (!paketSesuai.isEmpty()) {
            sb.append("Berikut paket yang cocok dengan budget Anda:\n\n");

            int limit = Math.min(3, paketSesuai.size());
            for (int i = 0; i < limit; i++) {
                PaketSewa p = paketSesuai.get(i);

                sb.append(i + 1).append(". ").append(p.getNamaPaket()).append("\n");
                sb.append("   Harga: Rp ").append(String.format("%,.0f", p.getHargaTotal())).append("\n");

                if (p.getDeskripsi() != null && !p.getDeskripsi().isBlank()) {
                    sb.append("   Detail: ").append(p.getDeskripsi()).append("\n");
                }

                sb.append("\n");
            }

            PaketSewa terbaik = paketSesuai.get(0);
            sb.append("Rekomendasi terbaik untuk budget Anda adalah ")
                    .append(terbaik.getNamaPaket())
                    .append(" karena paling mendekati budget yang Anda punya.\n\n");

            sb.append("Kalau tertarik, Anda bisa lanjut bertanya detail paket tersebut.");

            return sb.toString();
        }

        PaketSewa paketTermurah = daftarPaket.stream()
                .min(Comparator.comparingDouble(PaketSewa::getHargaTotal))
                .orElse(null);

        if (paketTermurah == null) {
            return "Maaf, paket sewa belum tersedia saat ini.";
        }

        sb.append("Maaf, belum ada paket yang sesuai dengan budget tersebut.\n\n");
        sb.append("Paket termurah saat ini:\n");
        sb.append("• ").append(paketTermurah.getNamaPaket()).append("\n");
        sb.append("  Harga: Rp ").append(String.format("%,.0f", paketTermurah.getHargaTotal())).append("\n");

        if (paketTermurah.getDeskripsi() != null && !paketTermurah.getDeskripsi().isBlank()) {
            sb.append("  Detail: ").append(paketTermurah.getDeskripsi()).append("\n");
        }

        double selisih = paketTermurah.getHargaTotal() - budget;
        if (selisih > 0) {
            sb.append("\nAnda bisa menaikkan budget sekitar Rp ")
                    .append(String.format("%,.0f", selisih))
                    .append(" untuk mengambil paket ini.");
        }

        return sb.toString();
    }

    /**
     * Mengekstrak nominal budget dari kalimat pengguna.
     * Mendukung format: 3 juta, 3jt, 2500000, 500 ribu, 500rb.
     */
    private long extractBudget(String input) {
        if (input == null || input.isBlank()) return 0;

        String normal = input.toLowerCase()
                .replace(",", ".")
                .replaceAll("[^a-z0-9.\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d+(?:\\.\\d+)?)\\s*(juta|jt|ribu|rb|k)?")
                .matcher(normal);

        long budgetTerbesar = 0;

        while (m.find()) {
            double angka = Double.parseDouble(m.group(1));
            String satuan = m.group(2);

            long nominal;
            if (satuan == null) {
                nominal = (long) angka;
            } else if (satuan.equals("juta") || satuan.equals("jt")) {
                nominal = (long) (angka * 1_000_000);
            } else {
                nominal = (long) (angka * 1_000);
            }

            if (nominal > budgetTerbesar) {
                budgetTerbesar = nominal;
            }
        }

        return budgetTerbesar;
    }

    /**
     * Menghasilkan respons pakaian secara dinamis dari database berdasarkan
     * kategori busana yang terdeteksi. Jika bukan kategori busana, kembalikan null.
     */
    private String generateResponsPakaian(String input, Kategori kategori) {
        if (kategori == null)
            return null;

        String dbKategori = switch (kategori) {
            case BUSANA_MODERN -> "Modern";
            case BUSANA_TRADISIONAL -> "Tradisional";
            case BUSANA_MUSLIM -> "Muslim";
            case BUSANA_INTERNASIONAL -> "Internasional";
            case BUSANA_BERTEMA -> "Bertema";
            case BUSANA_PREWEDDING -> "Pre-Wedding";
            case BUSANA_KELUARGA -> "Keluarga";
            case BUSANA_PESTA -> "Pesta";
            default -> null;
        };

        if (dbKategori == null)
            return null;

        List<PakaianWedding> cocok = daftarPakaian.stream()
                .filter(p -> p.getKategori().equalsIgnoreCase(dbKategori))
                .toList();

        if (cocok.isEmpty()) {
            return "[ Koleksi Busana " + dbKategori + " ]\n\n" +
                    "Maaf, saat ini koleksi untuk kategori ini sedang kosong atau belum tersedia.\n" +
                    "Silakan cek kategori lain atau hubungi admin kami.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[ Koleksi Busana ").append(dbKategori).append(" ]\n\n");
        sb.append("Berikut koleksi yang tersedia:\n\n");

        for (PakaianWedding p : cocok) {
            sb.append("• ").append(p.getNama()).append("\n");
            sb.append("  Ukuran : ").append(p.getUkuranTersedia()).append("\n");
            sb.append("  Harga  : Rp ").append(String.format("%,d", (long) p.getHargaSewa())).append("/hari\n");
            if (!p.isTersedia()) {
                sb.append("  Status : SEDANG DISEWA\n");
            }
            sb.append("\n");
        }

        sb.append("Ketik 'detail [nama busana/kategori]' untuk melihat detail dan foto busana.");
        return sb.toString();
    }

    private String ekstrakWarna(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String normal = input.toLowerCase()
                .replaceAll("[^a-z\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        List<String> daftarWarna = List.of(
                "putih", "hitam", "merah", "marun", "biru", "navy", "hijau",
                "toska", "emas", "gold", "champagne", "ivory", "cream", "krem",
                "pink", "blush", "coklat", "abu", "silver", "ungu",
                "kuning", "orange"
        );

        for (String warna : daftarWarna) {
            if (normal.matches(".*\\b" + java.util.regex.Pattern.quote(warna) + "\\b.*")) {
                return warna;
            }
        }

        return null;
    }

    private String generateResponsWarna(String input, String warna) {
        String normalInput = input == null ? "" : input.toLowerCase();

        List<PakaianWedding> cocok = daftarPakaian.stream()
                .filter(p -> cocokDenganWarna(p, warna))
                .filter(p -> cocokDenganJenisInput(p, normalInput))
                .toList();

        if (cocok.isEmpty()) {
            return "[ Koleksi Busana Warna " + kapitalisasi(warna) + " ]\n\n" +
                    "Maaf, saat ini belum ada busana dengan warna " + warna + " yang sesuai dengan pencarian Anda.\n" +
                    "Coba gunakan warna lain, misalnya putih, hitam, merah, biru, gold, atau champagne.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[ Koleksi Busana Warna ").append(kapitalisasi(warna)).append(" ]\n\n");
        sb.append("Berikut busana warna ").append(warna).append(" yang tersedia:\n\n");

        for (PakaianWedding p : cocok) {
            sb.append("• ").append(p.getNama()).append(" (").append(p.getKategori()).append(")\n");
            sb.append("  Ukuran : ").append(p.getUkuranTersedia()).append("\n");
            sb.append("  Harga  : Rp ").append(String.format("%,d", (long) p.getHargaSewa())).append("/hari\n");

            if (!p.isTersedia()) {
                sb.append("  Status : SEDANG DISEWA\n");
            }

            sb.append("\n");
        }

        sb.append("Ketik 'detail [nama busana]' untuk melihat detail dan foto busana.");
        return sb.toString();
    }

private String generateResponsPencarianKombinasi(String input, String warna, String ukuran, String jenis, long budget) {
    List<PakaianWedding> cocok = daftarPakaian.stream()
            .filter(p -> warna == null || cocokDenganWarna(p, warna))
            .filter(p -> ukuran == null || cocokDenganUkuran(p, ukuran))
            .filter(p -> jenis == null || cocokDenganJenis(p, jenis))
            .filter(p -> budget <= 0 || p.getHargaSewa() <= budget)
            .toList();

    if (cocok.isEmpty()) {
        StringBuilder kosong = new StringBuilder();
        kosong.append("[ Rekomendasi Busana Sesuai Pencarian ]\n\n");
        kosong.append("Maaf, belum ada busana yang cocok dengan kriteria berikut:\n");

        if (jenis != null) kosong.append("- Jenis: ").append(kapitalisasi(jenis)).append("\n");
        if (warna != null) kosong.append("- Warna: ").append(kapitalisasi(warna)).append("\n");
        if (ukuran != null) kosong.append("- Ukuran: ").append(ukuran).append("\n");
        if (budget > 0) kosong.append("- Harga maksimal: Rp ").append(String.format("%,d", budget)).append("\n");

        kosong.append("\nCoba longgarkan pencarian, misalnya tanpa ukuran atau naikkan budget.");
        return kosong.toString();
    }

    StringBuilder sb = new StringBuilder();
    sb.append("[ Rekomendasi Busana Sesuai Pencarian ]\n\n");
    sb.append("Kriteria yang saya tangkap:\n");

    if (jenis != null) sb.append("- Jenis: ").append(kapitalisasi(jenis)).append("\n");
    if (warna != null) sb.append("- Warna: ").append(kapitalisasi(warna)).append("\n");
    if (ukuran != null) sb.append("- Ukuran: ").append(ukuran).append("\n");
    if (budget > 0) sb.append("- Harga maksimal: Rp ").append(String.format("%,d", budget)).append("\n");

    sb.append("\nBerikut busana yang cocok:\n\n");

    for (PakaianWedding p : cocok) {
        sb.append("• ").append(p.getNama()).append(" (").append(p.getKategori()).append(")\n");
        sb.append("  Ukuran : ").append(p.getUkuranTersedia()).append("\n");
        sb.append("  Harga  : Rp ").append(String.format("%,d", (long) p.getHargaSewa())).append("/hari\n");

        if (!p.isTersedia()) {
            sb.append("  Status : SEDANG DISEWA\n");
        }

        sb.append("\n");
    }

    sb.append("Ketik 'detail [nama busana]' untuk melihat detail dan foto busana.");
    return sb.toString();
}

    private String ekstrakUkuran(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String normal = input.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\\b(xs|s|m|l|xl|xxl)\\b", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(normal);

        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }

        return null;
    }

    private String ekstrakJenisBusana(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String normal = input.toLowerCase();

        List<String> daftarJenis = List.of(
                "gaun", "kebaya", "jas", "tuxedo", "beskap", "batik",
                "kimono", "hanbok", "cheongsam", "koko", "blazer", "baju", "busana", "pakaian"
        );

        for (String jenis : daftarJenis) {
            if (normal.matches(".*\\b" + java.util.regex.Pattern.quote(jenis) + "\\b.*")) {
                return jenis;
            }
        }

        return null;
    }

    private boolean mengandungFilterHargaBusana(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }

        String normal = input.toLowerCase();

        boolean adaKataHarga = normal.matches(".*\\b(harga|budget|maksimal|max|dibawah|di bawah|kurang dari|under)\\b.*");
        boolean adaNominal = extractBudget(input) > 0;
        boolean konteksBusana = normal.matches(".*\\b(busana|pakaian|baju|gaun|kebaya|jas|tuxedo|beskap|batik|kimono|hanbok|cheongsam|koko|blazer)\\b.*");

        return adaKataHarga && adaNominal && konteksBusana;
    }

    private boolean cocokDenganUkuran(PakaianWedding pakaian, String ukuran) {
        if (pakaian.getUkuranTersedia() == null || ukuran == null) {
            return false;
        }

        String ukuranTersedia = pakaian.getUkuranTersedia().toUpperCase();

        return ukuranTersedia.matches(".*\\b" + java.util.regex.Pattern.quote(ukuran.toUpperCase()) + "\\b.*")
                || ukuranTersedia.contains(ukuran.toUpperCase());
    }

    private boolean cocokDenganJenis(PakaianWedding pakaian, String jenis) {
        if (jenis == null) {
            return true;
        }

        String teksPakaian = (
                nullToEmpty(pakaian.getNama()) + " " +
                        nullToEmpty(pakaian.getKategori()) + " " +
                        nullToEmpty(pakaian.getDeskripsi())
        ).toLowerCase();

        if (jenis.equals("baju") || jenis.equals("busana") || jenis.equals("pakaian")) {
            return true;
        }

        return teksPakaian.contains(jenis.toLowerCase());
    }

    private boolean cocokDenganWarna(PakaianWedding pakaian, String warna) {
        String teksPakaian = (
                nullToEmpty(pakaian.getNama()) + " " +
                        nullToEmpty(pakaian.getKategori()) + " " +
                        nullToEmpty(pakaian.getDeskripsi())
        ).toLowerCase();

        return teksPakaian.contains(warna.toLowerCase());
    }

    private boolean cocokDenganJenisInput(PakaianWedding pakaian, String input) {
        if (!input.matches(".*\\b(gaun|kebaya|jas|tuxedo|beskap|batik|kimono|hanbok|cheongsam|koko|blazer)\\b.*")) {
            return true;
        }

        String nama = nullToEmpty(pakaian.getNama()).toLowerCase();
        String kategori = nullToEmpty(pakaian.getKategori()).toLowerCase();
        String deskripsi = nullToEmpty(pakaian.getDeskripsi()).toLowerCase();
        String teksPakaian = nama + " " + kategori + " " + deskripsi;

        if (input.contains("gaun")) return teksPakaian.contains("gaun");
        if (input.contains("kebaya")) return teksPakaian.contains("kebaya");
        if (input.contains("jas")) return teksPakaian.contains("jas");
        if (input.contains("tuxedo")) return teksPakaian.contains("tuxedo");
        if (input.contains("beskap")) return teksPakaian.contains("beskap");
        if (input.contains("batik")) return teksPakaian.contains("batik");
        if (input.contains("kimono")) return teksPakaian.contains("kimono");
        if (input.contains("hanbok")) return teksPakaian.contains("hanbok");
        if (input.contains("cheongsam")) return teksPakaian.contains("cheongsam");
        if (input.contains("koko")) return teksPakaian.contains("koko");
        if (input.contains("blazer")) return teksPakaian.contains("blazer");

        return true;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String kapitalisasi(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    private String getDbKategori(Kategori kategori) {
        if (kategori == null) return null;
        return switch (kategori) {
            case BUSANA_MODERN -> "Modern";
            case BUSANA_TRADISIONAL -> "Tradisional";
            case BUSANA_MUSLIM -> "Muslim";
            case BUSANA_INTERNASIONAL -> "Internasional";
            case BUSANA_BERTEMA -> "Bertema";
            case BUSANA_PREWEDDING -> "Pre-Wedding";
            case BUSANA_KELUARGA -> "Keluarga";
            case BUSANA_PESTA -> "Pesta";
            default -> null;
        };
    }

    private String generateDetailPakaian(String input, Kategori kategori, List<byte[]> imageDataList) {
        List<PakaianWedding> cocok = new ArrayList<>();

        // ── PRIORITAS 1: Cari berdasarkan nama spesifik dari input pengguna ──
        // Ekstrak keyword dari input (hapus kata-kata umum/command)
        String normal = input.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String keyword = normal.replaceAll("\\b(detail|contoh|foto|gambar|spesifikasi|wujud|tampil|penampakan|busana|gaun|baju|pakaian|yang|dan|atau|untuk|dari|di|ke|ini|itu|nya|saya|mau|lihat|tolong|cari|tampilkan|tunjukkan|show)\\b", "").trim();
        // Kompres spasi ganda
        keyword = keyword.replaceAll("\\s+", " ").trim();

        if (!keyword.isEmpty()) {
            final String searchKeyword = keyword;

            // 1a. Coba cocokkan seluruh keyword dengan nama pakaian (match paling spesifik)
            cocok = daftarPakaian.stream()
                    .filter(p -> p.getNama().toLowerCase().contains(searchKeyword))
                    .toList();

            // 1b. Jika tidak ada, coba cocokkan tiap kata dalam keyword
            if (cocok.isEmpty()) {
                String[] words = searchKeyword.split("\\s+");
                cocok = daftarPakaian.stream()
                        .filter(p -> {
                            String namaLower = p.getNama().toLowerCase();
                            for (String w : words) {
                                if (w.length() >= 3 && namaLower.contains(w)) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .toList();
            }

            // 1c. Jika masih tidak ada, coba cocokkan keyword dengan kategori
            if (cocok.isEmpty()) {
                cocok = daftarPakaian.stream()
                        .filter(p -> p.getKategori().toLowerCase().contains(searchKeyword))
                        .toList();
            }
        }

        // ── PRIORITAS 2: Fallback ke kategori jika tidak ada match nama ──────
        if (cocok.isEmpty() && kategori != null && kategori.name().startsWith("BUSANA_")) {
            String dbKategori = getDbKategori(kategori);
            if (dbKategori != null) {
                cocok = daftarPakaian.stream().filter(p -> p.getKategori().equalsIgnoreCase(dbKategori)).toList();
            } else if (kategori == Kategori.BUSANA_PRIA || kategori == Kategori.BUSANA_WANITA) {
                String genderTarget = kategori == Kategori.BUSANA_PRIA ? "Pria" : "Wanita";
                cocok = daftarPakaian.stream().filter(p -> p.getGender() != null &&
                        (p.getGender().equalsIgnoreCase(genderTarget) || p.getGender().equalsIgnoreCase("Unisex"))).toList();
            }
        }

        if (cocok.isEmpty()) {
            return "Mohon maaf, saya belum menemukan detail foto untuk pencarian tersebut. Bisa sebutkan kategori atau nama busana yang lebih spesifik?";
        }

        int maxItems = 4;
        List<PakaianWedding> hasilLimit = cocok.size() > maxItems ? cocok.subList(0, maxItems) : cocok;

        StringBuilder sb = new StringBuilder();
        sb.append("[ Detail & Foto Busana ]\n\n");
        for (PakaianWedding p : hasilLimit) {
            sb.append("• ").append(p.getNama()).append("\n");
            sb.append("  Kategori : ").append(p.getKategori()).append("\n");
            if (p.getDeskripsi() != null && !p.getDeskripsi().isEmpty()) {
                sb.append("  Detail   : ").append(p.getDeskripsi()).append("\n");
            }
            sb.append("  Ukuran   : ").append(p.getUkuranTersedia()).append("\n");
            sb.append("  Harga    : Rp ").append(String.format("%,d", (long) p.getHargaSewa())).append("/hari");
            if (!p.isTersedia()) {
                sb.append(" (SEDANG DISEWA)");
            }
            sb.append("\n\n");

            if (p.hasImage()) {
                imageDataList.add(p.getImageData());
            }
        }

        if (cocok.size() > maxItems) {
            sb.append("... dan ").append(cocok.size() - maxItems).append(" busana lainnya. Ketik nama busana yang lebih spesifik untuk melihat detail lainnya.");
        }

        return sb.toString().trim();
    }
}
