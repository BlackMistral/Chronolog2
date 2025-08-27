# To learn more about how to use Nix to configure your environment,
# see: https://firebase.google.com/docs/studio/customize-workspace

{ pkgs, ... }: {
  channel = "stable-24.05";

  # 1. Installiamo solo gli strumenti di base universali
  packages = [
    pkgs.gradle
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
      # 3. Usiamo l'hook 'onCreate' per eseguire uno script che installa l'SDK
      # Questo script verrà eseguito UNA VOLTA, quando l'ambiente viene (ri)costruito.
      onCreate = {
        setup-android-sdk = ''
          echo "==== Inizio Setup SDK Android ===="
          
          # Definisce la home dell'SDK
          export ANDROID_HOME=$HOME/.android/sdk
          
          # Crea la cartella per l'SDK
          mkdir -p $ANDROID_HOME
          cd $ANDROID_HOME
          
          # Scarica gli strumenti a riga di comando
          wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O sdk-tools.zip
          
          # Estrai e organizza i file nella struttura moderna
          unzip -q sdk-tools.zip
          mkdir -p cmdline-tools/latest
          mv cmdline-tools/* cmdline-tools/latest/
          rm sdk-tools.zip
          
          # Imposta le variabili d'ambiente per tutti i futuri terminali
          echo 'export ANDROID_HOME=$HOME/.android/sdk' >> $HOME/.bashrc
          echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' >> $HOME/.bashrc
          
          # Installa i componenti necessari usando sdkmanager
          echo "==== Accettazione Licenze e Installazione Componenti SDK ===="
          yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses > /dev/null
          $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools" > /dev/null
          
          echo "==== Setup SDK Android Completato ===="
        '';
      };
    };
    
    # Sezione IDX standard (lasciare invariata)
    previews.enable = true;
    extensions = [];
  };
}