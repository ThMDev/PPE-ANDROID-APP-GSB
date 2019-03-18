package fr.cned.emdsgil.suividevosfrais;

import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import org.json.JSONArray;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;

abstract class Global {

    // tableau d'informations mémorisées
    public static Hashtable<Integer, FraisMois> listFraisMois = new Hashtable<>();
    /* Retrait du type de l'Hashtable (Optimisation Android Studio)
     * Original : Typage explicit =
	 * public static Hashtable<Integer, FraisMois> listFraisMois = new Hashtable<Integer, FraisMois>();
	*/

    // fichier contenant les informations sérialisées
    public static final String filename = "save.fic";

    /**
     * Modification de l'affichage de la date (juste le mois et l'année, sans le jour)
     */
    public static void changeAfficheDate(DatePicker datePicker, boolean afficheJours) {
        try {
            Field f[] = datePicker.getClass().getDeclaredFields();
            for (Field field : f) {
                int daySpinnerId = Resources.getSystem().getIdentifier("day", "id", "android");
                datePicker.init(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), null);
                if (daySpinnerId != 0)
                {
                    View daySpinner = datePicker.findViewById(daySpinnerId);
                    if (!afficheJours)
                    {
                        daySpinner.setVisibility(View.GONE);
                    }
                }
            }
        } catch (SecurityException | IllegalArgumentException e) {
            Log.d("ERROR", e.getMessage());
        }
    }

    /**
     * Méthode retournant la totalité des frais enregistrés dans l'application
     * @return ArrayList contenant les frais et frais Hf formatés dans une chaîne JSON
     */
    public static ArrayList convertList(){
        ArrayList fraisMois = new ArrayList<>();
        ArrayList lesLignes = new ArrayList();
        // traitement de tous les frais
        for (FraisMois unFrais:listFraisMois.values()) {
            ArrayList lesLignesHf = new ArrayList();
            ArrayList uneLigne = new ArrayList();
            uneLigne.add(unFrais.getMois());
            uneLigne.add(unFrais.getAnnee());
            uneLigne.add(unFrais.getKm());
            uneLigne.add(unFrais.getEtape());
            uneLigne.add(unFrais.getNuitee());
            uneLigne.add(unFrais.getRepas());
            // traitement de tous les les frais Hf du mois
            for (FraisHf unFraisHf: unFrais.getLesFraisHf()) {
                ArrayList uneLigneHf = new ArrayList();
                uneLigneHf.add(unFraisHf.getJour());
                uneLigneHf.add(unFraisHf.getMontant());
                uneLigneHf.add(unFraisHf.getMotif());
                // ajout de la ligne Hf contenant les informations dans une chaîne JSON
                String uneLigneHfStr = new JSONArray(uneLigneHf).toString();
                lesLignesHf.add(uneLigneHfStr);
            }
            uneLigne.add(new JSONArray(lesLignesHf).toString());
            // ajoute le frais sous format JSON pour faciliter le traitement côté serveur et être
            // certain d'associer les bonnes valeurs à chaque mois
            lesLignes.add(new JSONArray(uneLigne).toString());
        }
        fraisMois.add(new JSONArray(lesLignes).toString());
        return fraisMois;
    }
}
