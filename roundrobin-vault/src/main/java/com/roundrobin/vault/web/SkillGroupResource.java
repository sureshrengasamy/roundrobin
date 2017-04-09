package com.roundrobin.vault.web;

import com.roundrobin.core.api.Response;
import com.roundrobin.vault.api.SkillGroupTo;
import com.roundrobin.vault.groups.CreateSkillGroupValidator;
import com.roundrobin.vault.groups.UpdateSkillGroupValidator;
import com.roundrobin.vault.service.SkillGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "skill-group", produces = {"application/json"})
public class SkillGroupResource {
  @Autowired
  private SkillGroupService service;

  @RequestMapping(value = "{skillGroupId}", method = RequestMethod.GET)
  public Response<SkillGroupTo> read(@PathVariable String skillGroupId) {
    return new Response<>(service.read(skillGroupId));
  }

  @RequestMapping(method = RequestMethod.POST, consumes = {"application/json"})
  public Response<SkillGroupTo> create(
          @RequestBody @Validated(CreateSkillGroupValidator.class) SkillGroupTo skillGroupTo) {
    return new Response<>(service.create(skillGroupTo));
  }

  @RequestMapping(method = RequestMethod.PUT, consumes = {"application/json"})
  public Response<SkillGroupTo> update(
          @RequestBody @Validated(UpdateSkillGroupValidator.class) SkillGroupTo skillGroupTo) {
    return new Response<>(service.update(skillGroupTo));
  }

  @RequestMapping(value = "{skillGroupId}", method = RequestMethod.DELETE)
  public Response<Boolean> delete(@PathVariable String skillGroupId) {
    service.delete(skillGroupId);
    return new Response<>(true);
  }

  @RequestMapping(method = RequestMethod.GET)
  public Response<List<SkillGroupTo>> list() {
    return new Response<>(service.list());
  }
}
