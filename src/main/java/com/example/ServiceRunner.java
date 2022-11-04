package ch.admin.blv.infofito.lexicon;

import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.DE_TRANSLATIONS_YAML_PATH;
import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.EN_TRANSLATIONS_YAML_PATH;
import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.FR_TRANSLATIONS_YAML_PATH;
import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.IT_TRANSLATIONS_YAML_PATH;

import ch.admin.blv.infofito.lexicon.language.service.LexiconConfigService;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of the QuarkusApplication which allows performing logic on startup
 */
@Slf4j
public class LexiconServiceRunner implements QuarkusApplication {

  @Inject
  LexiconConfigService lexiconConfigService;

  @Override
  public int run(String... args) {
    log.info("The infofito-service-lexicon is starting...");
    // start process for updating translations
    updateAllTranslations();
    Quarkus.waitForExit();
    return 0;
  }

  /**
   * Updates all available translations for the system. Method processes yaml files for each
   * language and creates/ updates all necessary information.
   */
  public void updateAllTranslations() {
    log.debug("Update of lexicon translations started...");
    lexiconConfigService.updateTranslations(DE_TRANSLATIONS_YAML_PATH);
    lexiconConfigService.updateTranslations(EN_TRANSLATIONS_YAML_PATH);
    lexiconConfigService.updateTranslations(FR_TRANSLATIONS_YAML_PATH);
    lexiconConfigService.updateTranslations(IT_TRANSLATIONS_YAML_PATH);
  }
}
