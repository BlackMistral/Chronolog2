{
 description = "A development environment for the ChronoLog Android app";

  inputs = {
 nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

  };

  outputs = { self, nixpkgs }:
    let
      system = "x86_64-linux"; # Assuming a common system for Android builds
      # Importa nixpkgs con la configurazione per permettere pacchetti non liberi
      pkgs = import nixpkgs {

 system = system;
 config = {
        allowUnfree = true;
        android_sdk.accept_license = true;
      }; }; in
    {
      # Questo definisce l'ambiente che viene attivato con 'nix develop'
      devShells.${system}.default = pkgs.mkShell {
        buildInputs = [
          pkgs.jdk17
          # L'SDK Android completo con tutti i suoi strumenti
          pkgs.androidsdk
        ];
      };
    };
}