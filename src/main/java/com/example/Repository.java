package ch.admin.blv.infofito.lexicon.language.repository;

import ch.admin.blv.infofito.lexicon.language.dto.KeySearchDTO;
import ch.admin.blv.infofito.lexicon.language.enums.LabelArea;
import ch.admin.blv.infofito.lexicon.language.mapper.DataMapper;
import ch.admin.blv.infofito.lexicon.language.model.Data;
import ch.admin.blv.infofito.lexicon.language.model.Key;
import ch.admin.blv.infofito.lexicon.language.view.KeyView;
import ch.admin.blv.infofito.lexicon.util.DataOrder;
import ch.admin.blv.infofito.lexicon.util.PageRequest;
import ch.admin.blv.infofito.lexicon.util.Pager;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.AREA;
import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.DE_LANG_SHORT_NAME;
import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.EN_LANG_SHORT_NAME;
import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.FR_LANG_SHORT_NAME;
import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.GROUP_NAME;
import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.HELP_TEXT;
import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.IT_LANG_SHORT_NAME;
import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.KEY_CODE;
import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.KEY_ID;
import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.LANGUAGE_SHORTNAME;
import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.PERCENTAGE_WILDCARD_CHAR;
import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.VALUE;

/**
 * The Key Repository
 */
@ApplicationScoped
public class KeyRepository implements PanacheRepositoryBase<Key, String> {

  @Inject
  DataMapper dataMapper;

  @Inject
  EntityViewManager evm;

  @Inject
  EntityManager em;

  @Inject
  CriteriaBuilderFactory cbf;

  /**
   * Fetches a persisted key based on code and group
   *
   * @param keyCode the key code
   * @param groupId the group id of key
   */
  public Key findByCodeAndGroupId(String keyCode, String groupId) {
    Map<String, Object> params = new HashMap<>();
    params.put("code", keyCode);
    params.put("groupId", groupId);
    return find("code = :code and group.id = :groupId", params).firstResult();
  }

  /**
   * Indicates weather a resource with the given criteria is not present in the DB
   *
   * @param query the query to be executed
   * @param params the parameters
   * @return {@code true} if the resource is present
   */
  public boolean notExists(String query, Map<String, Object> params) {
    return count(query, params) == 0;
  }

  /**
   * Finds all keys that match the given search criteria and returns the paged results
   *
   * @param pageRequest page request containing information for pagination (page size, order etc.)
   * @param searchDTO search DTO containing the filter criteria
   * @return {@code Pager<KeyView>} with the paged results of search
   */
  public Pager<KeyView> search(PageRequest pageRequest, KeySearchDTO searchDTO) {
    // init builder for keys
    CriteriaBuilder<Key> cb = cbf.create(em, Key.class, "key");
    // apply search and column filtering
    applyFiltering(searchDTO, cb);
    // apply ordering
    setOrdering(cb, pageRequest.getProperty(), pageRequest.getDirection());

    EntityViewSetting<KeyView, CriteriaBuilder<KeyView>> setting
        = EntityViewSetting.create(KeyView.class);
    setting.addOptionalParameter("dataMapper", dataMapper);
    var pagedList = evm.applySetting(setting, cb)
        .page(pageRequest.getPage() * pageRequest.getSize(), pageRequest.getSize())
        .getResultList();
    return new Pager<>(pagedList.getPage(), pagedList.getSize(), pagedList.getTotalSize(),
        pagedList.getTotalPages(), pagedList);
  }

  /**
   * Applies filtering based on filter criteria
   *
   * @param searchDTO rchDTO search DTO containing the filter criteria
   * @param cb the criteria builder
   */
  private void applyFiltering(KeySearchDTO searchDTO, CriteriaBuilder<Key> cb) {
    // filtering by code or key's data
    if (Objects.nonNull(searchDTO.getFilterCriteria())) {
      // apply filtering for code
      if (StringUtils.isNotBlank(searchDTO.getFilterCriteria().getCode())) {
        searchByCode(cb, searchDTO.getFilterCriteria().getCode());
      }
      // apply filtering for the data values of each language
      if (StringUtils.isNotBlank(searchDTO.getFilterCriteria().getDataEN())) {
        cb.where(KEY_ID).in(
            getKeysByDataValueAndLanguage(searchDTO.getFilterCriteria().getDataEN(),
                EN_LANG_SHORT_NAME));
      }
      if (StringUtils.isNotBlank(searchDTO.getFilterCriteria().getDataDE())) {
        cb.where(KEY_ID).in(
            getKeysByDataValueAndLanguage(searchDTO.getFilterCriteria().getDataDE(),
                DE_LANG_SHORT_NAME));
      }
      if (StringUtils.isNotBlank(searchDTO.getFilterCriteria().getDataFR())) {
        cb.where(KEY_ID).in(
            getKeysByDataValueAndLanguage(searchDTO.getFilterCriteria().getDataFR(),
                FR_LANG_SHORT_NAME));
      }
      if (StringUtils.isNotBlank(searchDTO.getFilterCriteria().getDataIT())) {
        cb.where(KEY_ID).in(
            getKeysByDataValueAndLanguage(searchDTO.getFilterCriteria().getDataIT(),
                IT_LANG_SHORT_NAME));
      }
      if (Objects.nonNull(searchDTO.getFilterCriteria().getArea())) {
        cb.whereOr()
            .where(AREA).in(searchDTO.getFilterCriteria().getArea())
            .where(AREA).in(LabelArea.BOTH)
            .endOr();
      }
      if (Objects.nonNull(searchDTO.getFilterCriteria().getGroup())) {
        cb.where(GROUP_NAME).in(searchDTO.getFilterCriteria().getGroup());
      }
      if (Objects.nonNull(searchDTO.getFilterCriteria().getModifiedOn()) && (
              Objects.nonNull(searchDTO.getFilterCriteria().getModifiedOn().getStart()) ||
                      Objects.nonNull(searchDTO.getFilterCriteria().getModifiedOn().getEnd()))) {
        // retrieve start and end of day for the modified on criteria
        if (Objects.nonNull(searchDTO.getFilterCriteria().getModifiedOn().getStart())) {
          Instant startOfDay = searchDTO.getFilterCriteria().getModifiedOn().getStart().atOffset(ZoneOffset.UTC)
                  .with(LocalTime.of(0, 0, 0, searchDTO.getFilterCriteria().getModifiedOn().getStart().getNano()))
                  .toInstant();
          cb.where("modifiedOn").gt(startOfDay);
        }
        if (Objects.nonNull(searchDTO.getFilterCriteria().getModifiedOn().getEnd())) {
          Instant endOfDay = searchDTO.getFilterCriteria().getModifiedOn().getEnd().atOffset(ZoneOffset.UTC)
                  .with(LocalTime.of(23, 59, 59, searchDTO.getFilterCriteria().getModifiedOn().getEnd().getNano()))
                  .toInstant();
          cb.where("modifiedOn").lt(endOfDay);
        }
      }
      // apply filtering for the group name
      if (StringUtils.isNotBlank(searchDTO.getFilterCriteria().getGroupName()) && searchDTO.getFilterCriteria().getGroupName().equals("HELP_TEXT")) {
        cb.where(GROUP_NAME).in(HELP_TEXT);
      } else {
        cb.where(GROUP_NAME).notIn(HELP_TEXT);
      }
    }
  }

  /**
   * Retrieves the key ID for a given data value and language
   *
   * @param dataValue the value to search
   * @param languageShortName the language short name
   * @return {@code List} with key IDs for the given value and language
   */
  private List<String> getKeysByDataValueAndLanguage(String dataValue, String languageShortName) {
    return cbf.create(em, String.class)
        .from(Data.class)
        .select(KEY_ID)
        .where(VALUE).like()
        .value(PERCENTAGE_WILDCARD_CHAR + dataValue + PERCENTAGE_WILDCARD_CHAR)
        .noEscape()
        .where(LANGUAGE_SHORTNAME).eq(languageShortName).getResultList();
  }


  /**
   * Search a key by its code
   */
  private void searchByCode(CriteriaBuilder<Key> cb, String code) {
    cb.where(KEY_CODE)
        .like().value(PERCENTAGE_WILDCARD_CHAR + code + PERCENTAGE_WILDCARD_CHAR)
        .noEscape();
  }

  /**
   * Sets ordering based on data language
   *
   * @param cb the criteria builder
   * @param property the property to sort with
   * @param direction the direction
   */
  private void setOrdering(CriteriaBuilder<Key> cb, String property, String direction) {
    boolean ascending = isAscending(direction);
    // order by key code. It's also the default sorting when property is null or empty
    if (StringUtils.isBlank(property) || "code".equalsIgnoreCase(property)) {
      cb.orderBy(KEY_CODE, ascending);
    } else {
      // order by keys' data values based on language
      DataOrder order = new DataOrder(property, ascending);
      applyDataOrdering(cb, order);
    }
    // default sorting by ID is required for pagination
    cb.orderBy(KEY_ID, ascending);
  }

  /**
   * Applies sorting based on the on key data value and language
   *
   * @param cb the criteria builder for key
   * @param order Order containing information for direction and
   */
  private void applyDataOrdering(CriteriaBuilder<Key> cb, DataOrder order) {
    cb.innerJoinOn("key.data", "d")
        .on(KEY_ID).eqExpression("d.key.id")
        .end()
        .where("d.language.shortName").eq(order.getLanguageShortName())
        .orderBy("d.value", order.isAscending(), order.isNullFirst());
  }

  /**
   * Coverts the String sorting direction to boolean
   *
   * @param direction the direction (e.g. ASC)
   * @return {@code true} if direction equals ASC or it's null
   */
  private boolean isAscending(String direction) {
    return Objects.isNull(direction) || "ASC".equalsIgnoreCase(direction);
  }
}
