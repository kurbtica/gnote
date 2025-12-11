package org.openjfx.sio2E4;

public class Launcher {

    /**
     * En lançant Launcher (qui est une classe Java standard) la JVM démarre sans vérifier les composants JavaFX.
     * Une fois l'application lancée, le Launcher appelle App.main().
     * À ce moment-là, toutes les bibliothèques (incluses dans le Jar) sont chargées dans le Classpath
     * et sont disponibles, permettant à JavaFX de démarrer correctement.
     * @param args
     */
    public static void main(String[] args) {
        App.main(args);
    }
}
