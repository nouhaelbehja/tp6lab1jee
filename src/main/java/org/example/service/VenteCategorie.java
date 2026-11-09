package org.example.service;
import java.math.BigDecimal;
public class VenteCategorie {
    private final Long categorieId;
    private final String categorie;
    private final BigDecimal chiffreAffaires;
    public VenteCategorie(Long categorieId, String categorie, BigDecimal chiffreAffaires) {
        this.categorieId = categorieId; this.categorie = categorie; this.chiffreAffaires = chiffreAffaires;
    }
    public Long getCategorieId() { return categorieId; }
    public String getCategorie() { return categorie; }
    public BigDecimal getChiffreAffaires() { return chiffreAffaires; }
}

