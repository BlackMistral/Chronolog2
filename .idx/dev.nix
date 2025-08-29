# To learn more about how to use Nix to configure your environment,
# see: https://firebase.google.com/docs/studio/customize-workspace

{ pkgs, ... }: {
  channel = "stable-24.05";

  # 1. PACCHETTI DI BASE
  # Installiamo solo gli strumenti di sistema universali di cui abbiamo bisogno.
  packages = [
    pkgs.jdk17
    pkgs.wget
    pkgs.unzip
  ];

  # 2. VARIABILI D'AMBIENTE
  # Definiamo qui le variabili statiche. Questo è più robusto dell'uso di 'export'
  # perché Nix si assicura che siano disponibili in ogni terminale.
  env = {
    JAVA_HOME = "${pkgs.jdk17}/lib/openjdk";
    # Definiamo il percorso dell'SDK qui, dove verrà installato dallo script onCreate.
    ANDROID_HOME = "$HOME/.android/sdk";
  };

  idx = {
    workspace = {
      # 3. SCRIPT DI SETUP (HOOK ONCREATE)
      # Questo script viene eseguito una sola volta per preparare l'ambiente.
      onCreate = {
        setup-android-sdk = ''
          # Controlla se l'SDK è già stato installato per evitare di rieseguire
          if [ ! -d "$ANDROID_HOME/platforms" ]; then
            echo "==== SDK Android non trovato. Inizio installazione... ===="
          
            mkdir -p $ANDROID_HOME
            cd $ANDROID_HOME
          
            # Scarica gli strumenti a riga di comando
            wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O sdk-tools.zip
          
            # Estrai e organizza i file nella struttura moderna
            unzip -q sdk-tools.zip
            mkdir -p cmdline-tools/latest
            mv cmdline-tools/* cmdline-tools/latest/
            rm sdk-tools.zip
          
            # Imposta le variabili d'ambiente per il futuro (nel .bashrc)
          echo 'export ANDROID_HOME=$HOME/.android/sdk' >> $HOME/.bashrc
          echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' >> $HOME/.bashrc
            
          
          # Ricarica il bashrc per la sessione corrente
          source $HOME/.bashrc
          
          # Installa i componenti necessari
          echo "==== Accettazione Licenze e Installazione Componenti SDK ===="
          yes | sdkmanager --licenses > /dev/null
          sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools" > /dev/null
            
            echo "==== ✅ Setup SDK Android Completato. Ricarica il terminale per applicare le modifiche al PATH. ===="
          else
            echo "==== ✅ SDK Android già presente. Nessuna azione richiesta. ===="
          fi
        '';
      };
    };
  # Sezione IDX standard
    previews.enable = true;
    extensions = [];
  };
}