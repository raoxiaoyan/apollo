package com.ctrip.framework.apollo.portal.service;

import com.google.common.collect.Sets;

import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.portal.AbstractUnitTest;
import com.ctrip.framework.apollo.portal.constant.PermissionType;
import com.ctrip.framework.apollo.portal.entity.po.Permission;
import com.ctrip.framework.apollo.portal.entity.po.Role;
import com.ctrip.framework.apollo.portal.entity.po.UserInfo;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.util.RoleUtils;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RoleInitializationServiceTest extends AbstractUnitTest {

  private final String APP_ID = "1000";
  private final String APP_NAME = "app-test";
  private final String CLUSTER = "cluster-test";
  private final String NAMESPACE = "namespace-test";
  private final String CURRENT_USER = "user";

  @Mock
  private RolePermissionService rolePermissionService;
  @Mock
  private UserInfoHolder userInfoHolder;
  @InjectMocks
  private RoleInitializationService roleInitializationService;


  @Test
  public void testInitAppRoleHasInitBefore(){

    when(rolePermissionService.findRoleByRoleName(anyString())).thenReturn(mockRole(RoleUtils.buildAppMasterRoleName(APP_ID)));

    roleInitializationService.initAppRoles(mockApp());

    verify(rolePermissionService, times(1)).findRoleByRoleName(RoleUtils.buildAppMasterRoleName(APP_ID));
    verify(rolePermissionService, times(0)).assignRoleToUsers(anyString(), anySetOf(String.class), anyString());
  }

  @Test
  public void testInitAppRole(){

    when(rolePermissionService.findRoleByRoleName(anyString())).thenReturn(null);
    when(userInfoHolder.getUser()).thenReturn(mockUser());
    when(rolePermissionService.createPermission(any())).thenReturn(mockPermission());

    roleInitializationService.initAppRoles(mockApp());

    verify(rolePermissionService, times(3)).findRoleByRoleName(anyString());
    verify(rolePermissionService, times(1)).assignRoleToUsers(
        RoleUtils.buildAppMasterRoleName(APP_ID), Sets.newHashSet(CURRENT_USER), CURRENT_USER);
    verify(rolePermissionService, times(2)).createPermission(any());
    verify(rolePermissionService, times(3)).createRoleWithPermissions(any(), anySetOf(Long.class));
  }

  @Test
  public void testInitNamespaceRoleHasExisted(){

    String modifyNamespaceRoleName = RoleUtils.buildModifyNamespaceRoleName(APP_ID, NAMESPACE);
    when(rolePermissionService.findRoleByRoleName(modifyNamespaceRoleName)).
        thenReturn(mockRole(modifyNamespaceRoleName));

    String releaseNamespaceRoleName = RoleUtils.buildReleaseNamespaceRoleName(APP_ID, NAMESPACE);
    when(rolePermissionService.findRoleByRoleName(releaseNamespaceRoleName)).
        thenReturn(mockRole(releaseNamespaceRoleName));

    roleInitializationService.initNamespaceRoles(APP_ID, NAMESPACE);

    verify(rolePermissionService, times(2)).findRoleByRoleName(anyString());
    verify(rolePermissionService, times(0)).createPermission(any());
    verify(rolePermissionService, times(0)).createRoleWithPermissions(any(), anySetOf(Long.class));
  }

  @Test
  public void testInitNamespaceRoleNotExisted(){

    String modifyNamespaceRoleName = RoleUtils.buildModifyNamespaceRoleName(APP_ID, NAMESPACE);
    when(rolePermissionService.findRoleByRoleName(modifyNamespaceRoleName)).
        thenReturn(null);

    String releaseNamespaceRoleName = RoleUtils.buildReleaseNamespaceRoleName(APP_ID, NAMESPACE);
    when(rolePermissionService.findRoleByRoleName(releaseNamespaceRoleName)).
        thenReturn(null);

    when(userInfoHolder.getUser()).thenReturn(mockUser());
    when(rolePermissionService.createPermission(any())).thenReturn(mockPermission());

    roleInitializationService.initNamespaceRoles(APP_ID, NAMESPACE);

    verify(rolePermissionService, times(2)).findRoleByRoleName(anyString());
    verify(rolePermissionService, times(2)).createPermission(any());
    verify(rolePermissionService, times(2)).createRoleWithPermissions(any(), anySetOf(Long.class));
  }

  @Test
  public void testInitNamespaceRoleModifyNSExisted(){

    String modifyNamespaceRoleName = RoleUtils.buildModifyNamespaceRoleName(APP_ID, NAMESPACE);
    when(rolePermissionService.findRoleByRoleName(modifyNamespaceRoleName)).
        thenReturn(mockRole(modifyNamespaceRoleName));

    String releaseNamespaceRoleName = RoleUtils.buildReleaseNamespaceRoleName(APP_ID, NAMESPACE);
    when(rolePermissionService.findRoleByRoleName(releaseNamespaceRoleName)).
        thenReturn(null);

    when(userInfoHolder.getUser()).thenReturn(mockUser());
    when(rolePermissionService.createPermission(any())).thenReturn(mockPermission());

    roleInitializationService.initNamespaceRoles(APP_ID, NAMESPACE);

    verify(rolePermissionService, times(2)).findRoleByRoleName(anyString());
    verify(rolePermissionService, times(1)).createPermission(any());
    verify(rolePermissionService, times(1)).createRoleWithPermissions(any(), anySetOf(Long.class));
  }

  private App mockApp(){
    App app = new App();
    app.setAppId(APP_ID);
    app.setName(APP_NAME);
    app.setOrgName("xx");
    app.setOrgId("1");
    app.setOwnerName(CURRENT_USER);
    app.setDataChangeCreatedBy(CURRENT_USER);
    return app;
  }

  private Role mockRole(String roleName){
    Role role = new Role();
    role.setRoleName(roleName);
    return role;
  }

  private UserInfo mockUser(){
    UserInfo userInfo = new UserInfo();
    userInfo.setUserId(CURRENT_USER);
    return userInfo;
  }

  private Permission mockPermission(){
    Permission permission = new Permission();
    permission.setPermissionType(PermissionType.MODIFY_NAMESPACE);
    permission.setTargetId(RoleUtils.buildNamespaceTargetId(APP_ID, NAMESPACE));
    return permission;
  }


}
