{
  description = "A basic Flake template";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    systems.url = "github:nix-systems/default";
    flake-parts.url = "github:hercules-ci/flake-parts";
    treefmt-nix = {
      url = "github:numtide/treefmt-nix";
      inputs.nixpkgs.follows = "nixpkgs";
    };
    pre-commit-hooks-nix.url = "github:cachix/pre-commit-hooks.nix";
  };

  outputs = inputs @ {
    flake-parts,
    systems,
    treefmt-nix,
    pre-commit-hooks-nix,
    ...
  }:
    flake-parts.lib.mkFlake {inherit inputs;} {
      #see https://flake.parts/options/flake-parts.html
      imports = [
        inputs.treefmt-nix.flakeModule
        inputs.pre-commit-hooks-nix.flakeModule
      ];
      systems = import systems;

      perSystem = {
        config,
        self',
        inputs',
        pkgs,
        system,
        ...
      }: {
        devShells.default = pkgs.mkShell {
          inputsFrom = builtins.attrValues self'.checks ++ builtins.attrValues self'.packages;
          nativeBuildInputs = [
            pkgs.maven
            pkgs.jdk

            pkgs.z3

            (pkgs.python3.withPackages (python-pkgs: [
              python-pkgs.z3-solver
            ]))

            # make treefmt available in the shell
            config.treefmt.build.wrapper
          ];

          shellHook = "
              ${config.pre-commit.installationScript}

              echo Welcome to a generic Flake Project!
          ";

          #ENV_VAR="test";
        };

        treefmt = {
          projectRootFile = "./flake.nix";
          # Formatters (See https://flake.parts/options/treefmt-nix.html#options ):
          programs = {
            alejandra.enable = true; # Nix
            black.enable = true; # Python
            #shellcheck.enable = true; # Bash
          };
        };

        # Configure commit hooks (see https://flake.parts/options/pre-commit-hooks-nix.html)
        pre-commit = {
          check.enable = false;
          settings = {
            # Automagically uses the defined treefmt because of https://github.com/cachix/pre-commit-hooks.nix/blob/master/flake-module.nix#L71C13-L71C112
            hooks.treefmt.enable = true;
            hooks.commitizen.enable = true;

            # see https://github.com/cachix/pre-commit-hooks.nix#custom-hooks
            hooks.nix-flake-check = {
              enable = false;
              name = "nix-flake-check";
              entry = "nix flake check -L";
              pass_filenames = false;
            };
          };
        };
      };
    };
}
