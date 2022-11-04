package ch.admin.blv.infofito.lexicon;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Lexicon Service main application start interface.
 */
@QuarkusMain
public class LexiconServiceApp {

  public static void main(String... args) {
    Quarkus.run(LexiconServiceRunner.class, args);
  }
}
