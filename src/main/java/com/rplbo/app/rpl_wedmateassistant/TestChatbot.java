package com.rplbo.app.rpl_wedmateassistant;

import com.rplbo.app.rpl_wedmateassistant.engine.ChatbotEngine;
import com.rplbo.app.rpl_wedmateassistant.model.Sesi;
import com.rplbo.app.rpl_wedmateassistant.model.Pesan;
import com.rplbo.app.rpl_wedmateassistant.model.PakaianWedding;
import com.rplbo.app.rpl_wedmateassistant.model.PaketSewa;
import java.util.ArrayList;
import java.util.List;

public class TestChatbot {
    public static void main(String[] args) {
        ChatbotEngine engine = new ChatbotEngine();
        
        List<PakaianWedding> pakaian = new ArrayList<>();
        pakaian.add(new PakaianWedding(1, "Kebaya Sunda Merah Marun", "Tradisional", "Kebaya Sunda bordir motif khas dengan aksen payet", 950000, "S, M, L", null, "Wanita", true));
        pakaian.add(new PakaianWedding(2, "Jas Formal Hitam", "Modern", "Jas formal", 500000, "M, L, XL", null, "Pria", true));
        engine.setDaftarPakaian(pakaian);
        
        List<PaketSewa> paket = new ArrayList<>();
        paket.add(new PaketSewa(1, "Paket Platinum", "1 busana pengantin, 6 keluarga, MUA", 4000000, 2, new ArrayList<>()));
        engine.setDaftarPaket(paket);
        
        Sesi sesi = new Sesi();
        sesi.setId(1);
        sesi.setDaftarPesan(new ArrayList<>());
        
        String[] inputs = {
            "Sewa baju adat Sunda ukuran L budget 2 juta tanggal 12 Juli, masih tersedia?",
            "Budget 5 juta untuk 2 acara, 4 pasang keluarga",
            "berapa denda kalau telat kembalikan?",
            "estimasi biaya sewa gaun 2 hari 3 item",
            "tema garden party outdoor",
            "gimana kalau DP?",
            "mau konsep adat jawa",
            "diskon member ada?"
        };
        
        for (String input : inputs) {
            System.out.println("==========================================");
            System.out.println("INPUT: " + input);
            Pesan pesan = engine.prosesPesan(input, sesi);
            System.out.println("OUTPUT:\n" + pesan.getIsiPesan());
        }
    }
}
