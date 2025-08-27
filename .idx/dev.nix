# To learn more about how to use Nix to configure your environment,
# see: https://firebase.google.com/docs/studio/customize-workspace

{ pkgs, ... }: {
  channel = "stable-24.05";

  # 1. Installiamo solo gli strumenti di base di cui siamo sicuri
  packages = [
    pkgs.jdk17
    pkgs.wget
    pkgs.unzip
  ];

  # 2. Impostiamo JAVA_HOME
  env = {
    JAVA_HOME = "${pkgs.jdk17}/lib/openjdk";
  };

  idx = {
    workspace = {
      # 3. Usiamo l'hook 'onCreate' per eseguire uno script che
      # scarica e installa l'SDK Android manualmente.
      # Questo bypassa i pacchetti Nix corrotti.
      onCreate = {
        setup-android-sdk = ''
          echo "==== Inizio Setup Manuale SDK Android ===="
          
          # Definisce la home dell'SDK
          export ANDROID_HOME=$HOME/.android/sdk
          
          # Crea la cartella per l'SDK
          mkdir -p $ANDROID_HOME
          
          # Scarica gli strumenti a riga di comando
          wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O sdk-tools.zip
          
          # Estrai e organizza i file
          unzip -q sdk-tools.zip -d $ANDROID_HOME
          mkdir -p $ANDROID_HOME/cmdline-tools/latest
          mv $ANDROID_HOME/cmdline-tools/* $ANDROID_HOME/cmdline-tools/latest/
          rm sdk-tools.zip
          
          # Imposta le variabili d'ambiente per tutti i futuri terminali
          echo 'export ANDROID_HOME=$HOME/.android/sdk' >> $HOME/.bashrc
          echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' >> $HOME/.bashrc
          
          # Ricarica il bashrc per la sessione corrente
          source $HOME/.bashrc
          
          # Installa i componenti necessari
          echo "==== Accettazione Licenze e Installazione Componenti SDK ===="
          yes | sdkmanager --licenses > /dev/null
          sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools" > /dev/null
          
          echo "==== Setup SDK Android Completato ===="
        '';
      };
    };
    

  # Sezione IDX standard
    previews.enable = true;
    extensions = [];
  };
}