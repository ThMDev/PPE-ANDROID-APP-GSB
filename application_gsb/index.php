<?php
// inclusion des fonctions et de la classe de connexion
// utilisation des fichiers existants pour l'application WEB

require_once '../includes/fct.inc.php';
require_once '../includes/class.pdogsb.inc.php';

// vérification des variables POST envoyées par l'application
if (isset($_POST['lesdonnees']) && is_string($_POST['lesdonnees']) 
        && isset($_POST['action']) && is_string($_POST['action'])) {
    // intialisation de la connexion
    $pdo = PdoGsb::getPdoGsb();
    // support de plusieurs actions
    switch ($_POST['action']) {
        // cas de l'enregistrement des frais
        case "enregistrer":
            $reponse['action'] = "enregistrer";
            $donneesApp = json_decode($_POST['lesdonnees']);
            // les index:
            //  0 : contient le login
            //  1 : contient le mot de passe
            //  2 : contient un JSONArray : à chaque index correspond un fraisMois
            // chaque fraisMois contient en index :
            //  0 : Mois, 1 : Annee, 2 : Km, 3 : Etape, 4 : Nuitee, 5 : Repas, 
            //  6 : contient un JSONArray : à chaque index correspond un fraisHf
            // chaque fraisHf contient en index : 
            // 0 : Jour, 1 : Montant, 2 : Motif
            $login = $donneesApp[0];
            $mdp = $donneesApp[1];
            // verification de l'existence de l'utilisateur
            $visiteur = $pdo->getInfosVisiteur($login, $mdp);
            // si il est connu on enregistre les frais sinon on arrête le traitement
            if (!is_array($visiteur)) {
                $reponse['estConnecte'] = "false";
            } else {
                $idVisiteur = $visiteur['id'];
                if(isset($donneesApp[2])){
                    $donneesFrais = json_decode($donneesApp[2]);
                    // boucle permettant l'ajout des frais
                    foreach ($donneesFrais as $unFrais) {
                        $unFrais = json_decode($unFrais);
                        // récupération du mois au format aaaamm
                        //formatage du mois
                        if(strlen($unFrais[0]) != 2){
                            $moisDeuxDigits = "0".$unFrais[0];
                        }else{
                            $moisDeuxDigits = $unFrais[0];
                        }
                        $mois = $unFrais[1].$moisDeuxDigits;
                        // création d'une nouvelle ligne de frais
                        $pdo->creeNouvellesLignesFrais($idVisiteur,$mois);
                        // association aux clés des fraisforfaits
                        $lesFrais = ["KM" => $unFrais[2],"ETP" => $unFrais[3],"NUI" => $unFrais[4],"REP" => $unFrais[5]];
                        // vérification des valeurs et ajout des valeurs
                        if (lesQteFraisValides($lesFrais)) {
                            $pdo->majFraisForfait($idVisiteur, $mois, $lesFrais);
                        }
                        // vérifcation de l'existence de frais Hf
                        if(isset($unFrais[6]) && !empty($unFrais[6])){
                            $donneesFraisHf = json_decode($unFrais[6]);
                            // traitement de tous les frais Hf
                            foreach ($donneesFraisHf as $unFraisHf) {
                                $unFraisHf = json_decode($unFraisHf);
                                // création de la date jj/mm/aaaa
                                // formatage du jour
                                if(strlen($unFraisHf[0]) != 2){
                                    $dateDeuxDigits = "0".$unFraisHf[0];
                                }else{
                                    $dateDeuxDigits = $unFraisHf[0];
                                }
                                $dateFraisHf = $dateDeuxDigits.'/'.$moisDeuxDigits.'/'.$unFrais[1];
                                // ajout du frais Hf
                                $pdo->creeNouveauFraisHorsForfait($idVisiteur,$mois,$unFraisHf[2],$dateFraisHf,$unFraisHf[1]);
                            }
                        }
                    }
                    $reponse['statut'] = "true";
                }else{
                    $reponse['statut'] = "false";
                }
                $reponse['estConnecte'] = "true";
            }
            break;
    }
    // affichage des données sous format JSON
    echo json_encode($reponse);
}