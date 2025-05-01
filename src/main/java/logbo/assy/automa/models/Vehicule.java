package logbo.assy.automa.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;


public class Vehicule {
    private BooleanProperty selected = new SimpleBooleanProperty(false);
    private String idVehicule;
    String numeroChassis;
    String immatriculation;
    String marque;
    String modele;
    String energie;
    String puissance;
    String couleur;
    String prixAchat;
    String dateAchat;
    String dateMiseEnService;
    String dateAmmortissement;
    String idCategorie;

    public Vehicule(String numeroChassis, String immatriculation, String marque, String modele, String energie, String puissance, String couleur, String prixAchat, String dateAchat, String dateMiseEnService, String dateAmmortissement, String idCategorie) {
        this.numeroChassis = numeroChassis;
        this.immatriculation = immatriculation;
        this.marque = marque;
        this.modele = modele;
        this.energie = energie;
        this.puissance = puissance;
        this.couleur = couleur;
        this.prixAchat = prixAchat;
        this.dateAchat = dateAchat;
        this.dateMiseEnService = dateMiseEnService;
        this.dateAmmortissement = dateAmmortissement;
        this.idCategorie = idCategorie;
    }

    public Vehicule() {
    }

    public String getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(String idVehicule) {
        this.idVehicule = idVehicule;
    }

    public String getNumeroChassis() {
        return numeroChassis;
    }

    public void setNumeroChassis(String numeroChassis) {
        this.numeroChassis = numeroChassis;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public String getEnergie() {
        return energie;
    }

    public void setEnergie(String energie) {
        this.energie = energie;
    }

    public String getPuissance() {
        return puissance;
    }

    public void setPuissance(String puissance) {
        this.puissance = puissance;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public String getPrixAchat() {
        return prixAchat;
    }

    public void setPrixAchat(String prixAchat) {
        this.prixAchat = prixAchat;
    }

    public String getDateAchat() {
        return dateAchat;
    }

    public void setDateAchat(String dateAchat) {
        this.dateAchat = dateAchat;
    }

    public String getDateMiseEnService() {
        return dateMiseEnService;
    }

    public void setDateMiseEnService(String dateMiseEnService) {
        this.dateMiseEnService = dateMiseEnService;
    }

    public String getDateAmmortissement() {
        return dateAmmortissement;
    }

    public void setDateAmmortissement(String dateAmmortissement) {
        this.dateAmmortissement = dateAmmortissement;
    }

    public String getIdCategorie() {
        return idCategorie;
    }

    public void setIdCategorie(String idCategorie) {
        this.idCategorie = idCategorie;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

}
