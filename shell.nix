{ pkgs ? import <nixpkgs> {}}:
pkgs.mkShell {
  nativeBuildInputs = [ pkgs.sbt ];

  shellHook = ''
    export PI4J_PLATFORM=Simulated
    export SimulatedPlatform="RaspberryPi GPIO Provider"
  '';
}

