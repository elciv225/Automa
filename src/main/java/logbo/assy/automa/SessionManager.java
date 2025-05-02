package logbo.assy.automa;

import logbo.assy.automa.models.Personnel;

public class SessionManager {
    private static Personnel utilisateurActuel;

    public static void setUtilisateurActuel(Personnel personnel) {
        utilisateurActuel = personnel;
    }

    public static Personnel getUtilisateurActuel() {
        return utilisateurActuel;
    }

    public static String getLogin() {
        return utilisateurActuel != null ? utilisateurActuel.getIdPersonnel() : "inconnu";
    }

    public static String getNom(){
        return utilisateurActuel.getNom();
    }

    public static String getPrenom(){
        return utilisateurActuel.getPrenom();
    }

    public static String getFonction(){
        return utilisateurActuel.getIdFonction().replace("FONC_", "");
    }
}