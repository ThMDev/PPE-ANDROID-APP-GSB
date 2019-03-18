# PPE-ANDROID-APP-GSB
Description des différents fichiers:

- "app-debug.apk" contient le fichier APK de l'application GSB permettant la saisie des frais.
- "main" est un dossier contenant les sources java et xml de l'application. 
    Attention ! La copie des sources dans un nouveau projet ne permettra pas de faire fonctionner correctement l'application, 
      en particulier la gestion de la connexion à la base distante. Pour la faire fonctionner correctement,
        il sera nécessaire :
          - d'inclure les sources de l'application WEB GSB : https://github.com/ThMDev/PPE-GSB-WEB , au minimum le dossier "includes" 
          - d'inclure le dossier application_gsb
          - d'ajouter l'adresse de la base distante dans le fichier "AccessDistant.java" à la ligne 12.
- "application_gsb" est un dossier contenant le fichier php permettant de réaliser les actions sollicitées par l'application
