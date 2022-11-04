package ch.admin.blv.infofito.lexicon.language.service;

import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.DE_LANG_SHORT_NAME;
import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.EN_LANG_SHORT_NAME;
import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.FR_LANG_SHORT_NAME;
import static ch.admin.blv.infofito.lexicon.util.LexiconConstant.IT_LANG_SHORT_NAME;

import ch.admin.blv.infofito.lexicon.audit.client.AuditServiceClient;
import ch.admin.blv.infofito.lexicon.audit.dto.AuditDataDTO;
import ch.admin.blv.infofito.lexicon.common.model.BaseData;
import ch.admin.blv.infofito.lexicon.language.dto.GroupDTO;
import ch.admin.blv.infofito.lexicon.language.dto.KeyDTO;
import ch.admin.blv.infofito.lexicon.language.dto.KeySearchDTO;
import ch.admin.blv.infofito.lexicon.language.mapper.KeyMapper;
import ch.admin.blv.infofito.lexicon.language.model.Key;
import ch.admin.blv.infofito.lexicon.language.repository.KeyRepository;
import ch.admin.blv.infofito.lexicon.language.view.KeyView;
import ch.admin.blv.infofito.lexicon.util.LexiconConstant;
import ch.admin.blv.infofito.lexicon.util.PageRequest;
import ch.admin.blv.infofito.lexicon.util.Pager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * The KeyService Service
 */
@ApplicationScoped
@Slf4j
public class KeyService {

  @Inject
  KeyRepository repository;

  @Inject
  KeyMapper mapper;

  @Inject
  DataService dataService;

  @Inject
  DataHelpTextService dataHelpTextService;

  @Inject
  @RestClient
  AuditServiceClient auditServiceClient;

  /**
   * @return the Key repository
   */
  public KeyRepository getRepository() {
    return this.repository;
  }

  /**
   * Returns a key objects by the given ID
   *
   * @param keyId the key ID
   * @return key DTO
   */
  public KeyDTO findById(String keyId) {
    Key key = repository.findById(keyId);
    return mapper.mapToDTO(key);
  }

  /**
   * Creates a new key.
   *
   * @param keyDTO keyDTO DTO containing the necessary information
   * @return the newly created Group DTO
   */
  @Transactional(rollbackOn = Exception.class)
  public KeyDTO create(KeyDTO keyDTO) {
    log.debug("Creating keyDTO with code: {}", keyDTO.getCode());
    Key entity = mapper.mapToEntity(keyDTO);
    // create the data for the new key
    getService(keyDTO.getGroup()).createKeyData(entity, EN_LANG_SHORT_NAME, keyDTO.getDataEN());
    getService(keyDTO.getGroup()).createKeyData(entity, DE_LANG_SHORT_NAME, keyDTO.getDataDE());
    getService(keyDTO.getGroup()).createKeyData(entity, FR_LANG_SHORT_NAME, keyDTO.getDataFR());
    getService(keyDTO.getGroup()).createKeyData(entity, IT_LANG_SHORT_NAME, keyDTO.getDataIT());
    repository.persistAndFlush(entity);
    return mapper.mapToDTO(entity);
  }

  /**
   * Updates a Group.
   *
   * @param keyDTO key DTO containing the necessary information
   * @return the updated Group DTO
   */
  @Transactional(rollbackOn = Exception.class)
  public KeyDTO update(KeyDTO keyDTO) {
    log.debug("Updating keyDTO with code: {}", keyDTO.getCode());
    Key updatedKey = repository.getEntityManager().merge(mapper.mapToEntity(keyDTO));
    getService(keyDTO.getGroup()).updateKeyData(updatedKey, EN_LANG_SHORT_NAME, keyDTO.getDataEN());
    getService(keyDTO.getGroup()).updateKeyData(updatedKey, DE_LANG_SHORT_NAME, keyDTO.getDataDE());
    getService(keyDTO.getGroup()).updateKeyData(updatedKey, FR_LANG_SHORT_NAME, keyDTO.getDataFR());
    getService(keyDTO.getGroup()).updateKeyData(updatedKey, IT_LANG_SHORT_NAME, keyDTO.getDataIT());
    repository.flush();
    logUpdate(keyDTO);
    return mapper.mapToDTO(updatedKey);
  }

  private void logUpdate(KeyDTO keyDTO) {
    AuditDataDTO dto = new AuditDataDTO();
    dto.setAuditEvent("UPDATE");
    String typeDescription ="";
    if("helpText".equals(keyDTO.getGroup().getName())){
      typeDescription ="Help text ";
    } else{
      typeDescription ="User interface text ";
    }
    dto.setDescription(typeDescription+keyDTO.getCode()+ " edited");
    dto.setObjectType("SETTINGS");
    dto.setReferenceId(keyDTO.getId());
    dto.setAuditArea("Settings");
    auditServiceClient.auditCreate(List.of(dto));
  }

  /**
   * Method that searches for keys.
   *
   * @param pageRequest The object with the params
   * @param searchDTO The search criteria object
   * @return the paged results of the search.
   */
  public Pager<KeyView> search(PageRequest pageRequest, KeySearchDTO searchDTO) {
    return repository.search(pageRequest, searchDTO);
  }

  /**
   * Returns a KeyDTO based on a given code and group ID
   *
   * @param keyCode the code of Key
   * @param groupId the group ID of Key
   * @return the KeyDTO
   */
  public KeyDTO findByCodeAndGroupId(String keyCode, String groupId) {
    Key key = repository.findByCodeAndGroupId(keyCode, groupId);
    return mapper.mapToDTO(key);
  }

  /**
   * Method that checks the uniqueness of a key object in comparison with the persisted keys in BD.
   *
   * @param key the Key DTO
   * @return {@code true} if combination of key code and group does not exist in db
   */
  public boolean isUnique(KeyDTO key) {
    Map<String, Object> params = new HashMap<>();
    params.put("code", key.getCode());
    params.put("groupId", Objects.nonNull(key.getGroup()) ? key.getGroup().getId() : null);
    if (Objects.nonNull(key.getId())) {
      params.put("keyId", key.getId());
      return repository.notExists(
          "(code = :code and (:groupId is null or group.id = :groupId)) and id != :keyId", params);
    }
    return repository.notExists("code = :code and (:groupId is null or group.id = :groupId)",
        params);
  }

  @SuppressWarnings("unchecked")
  public <T extends BaseData> BaseDataService<T> getService(GroupDTO groupDTO) {
    return Objects.nonNull(groupDTO) && LexiconConstant.HELP_TEXT.equals(groupDTO.getName()) ? (BaseDataService<T>) dataHelpTextService
        : (BaseDataService<T>) dataService;
  }
}
