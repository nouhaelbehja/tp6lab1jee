package org.example.entities;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CommandeProduitPK implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "commande_id") private Long commandeId;
    @Column(name = "produit_id") private Long produitId;
    public CommandeProduitPK() {}
    public CommandeProduitPK(Long commandeId, Long produitId) { this.commandeId = commandeId; this.produitId = produitId; }
    public Long getCommandeId() { return commandeId; }
    public void setCommandeId(Long id) { commandeId = id; }
    public Long getProduitId() { return produitId; }
    public void setProduitId(Long id) { produitId = id; }
    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CommandeProduitPK)) return false;
        CommandeProduitPK other = (CommandeProduitPK) obj;
        return Objects.equals(commandeId, other.commandeId) && Objects.equals(produitId, other.produitId);
    }
    @Override public int hashCode() { return Objects.hash(commandeId, produitId); }
}

