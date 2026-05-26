package com.rplbo.app.rpl_wedmateassistant.model;

/**
 * Merepresentasikan satu entri detail busana yang terdiri dari:
 * <ul>
 *   <li>Teks deskripsi/detail busana</li>
 *   <li>Data gambar (BLOB) opsional yang bersesuaian dengan teks tersebut</li>
 * </ul>
 *
 * <p>Digunakan oleh {@link Pesan} untuk menyimpan pasangan detail+foto
 * sehingga tampilan bubble chat dapat menampilkan foto tepat di bawah
 * teks detail busana yang bersesuaian.</p>
 */
public class DetailItem {

    /** Teks deskripsi/detail untuk satu busana */
    private final String teks;

    /** Data gambar (BLOB) yang bersesuaian dengan teks ini; null jika tidak ada gambar */
    private final byte[] imageData;

    /**
     * Constructor untuk DetailItem dengan teks saja (tanpa gambar).
     *
     * @param teks teks deskripsi busana
     */
    public DetailItem(String teks) {
        this.teks      = teks;
        this.imageData = null;
    }

    /**
     * Constructor untuk DetailItem dengan teks dan gambar.
     *
     * @param teks      teks deskripsi busana
     * @param imageData data BLOB gambar busana; boleh null jika tidak ada
     */
    public DetailItem(String teks, byte[] imageData) {
        this.teks      = teks;
        this.imageData = imageData;
    }

    /** Mengambil teks deskripsi busana */
    public String getTeks() {
        return teks;
    }

    /** Mengambil data gambar (BLOB); null jika tidak ada gambar */
    public byte[] getImageData() {
        return imageData;
    }

    /** Mengecek apakah item ini memiliki gambar */
    public boolean hasImage() {
        return imageData != null && imageData.length > 0;
    }
}
