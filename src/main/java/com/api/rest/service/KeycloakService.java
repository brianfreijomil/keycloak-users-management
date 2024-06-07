package com.api.rest.service;

import com.api.rest.http.UserCreateRequest;
import com.api.rest.http.UserResponse;
import com.api.rest.util.KeycloakProvider;
import com.api.rest.util.Roles;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KeycloakService implements IKeycloakService {

    /**
     * find all users from the context, separating the currentUser
     * @param idCurrentUser
     * @return list of user response
     */
    @Override
    public List<UserResponse> findAllUsers(String idCurrentUser) {
        List<UserRepresentation> usersKeycloak = KeycloakProvider.getRealmResource().users().list();
        List<UserResponse> usersResponse = new ArrayList<>();
        if(!usersKeycloak.isEmpty()) {
            usersResponse = usersKeycloak.stream()
                    .filter(user -> !user.getId().equals(idCurrentUser))
                    .map(user -> UserResponse.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .lastName(user.getLastName())
                            .firstName(user.getFirstName())
                            .roles(this.getUserRoles(user.getId()))
                            .enabled(user.isEnabled())
                            .build()
                    ).collect(Collectors.toList());
        }

        return usersResponse;
    }

    /**
     * find user by username
     *
     * @param username
     * @return user response
     */
    @Override
    public UserResponse findUserByUsername(String username) {

        List<UserRepresentation> usersKeycloak = new ArrayList<>();

        usersKeycloak = KeycloakProvider.getRealmResource().users()
                .searchByUsername(username, true);

        List<UserResponse> usersList = new ArrayList<>();
        if(!usersKeycloak.isEmpty()) {
            usersList = usersKeycloak.stream()
                    .map(user -> UserResponse.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .lastName(user.getLastName())
                            .firstName(user.getFirstName())
                            .roles(this.getUserRoles(user.getId()))
                            .enabled(user.isEnabled())
                            .build()
            ).collect(Collectors.toList());
            //return user in first position, the only one
            return usersList.get(0);
        }
        throw new BadRequestException("no encontrado");
    }

    /**
     * create user
     *
     * @param user
     * @return status of action
     */
    @Override
    public ResponseEntity<?> create(UserCreateRequest user) {
        int httpStatus = 0;
        UsersResource usersResource = KeycloakProvider.getUserResource();

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setEmail(user.getEmail());
        userRepresentation.setUsername(user.getUsername());
        userRepresentation.setEmailVerified(true);
        userRepresentation.setEnabled(true);
        //create a user and get the status
        Response response = usersResource.create(userRepresentation);
        httpStatus = response.getStatus();

        //ask if the user was created (201 = created, 400 = user already created, 5xx = error server)
        if(httpStatus == 201) {
            //get userId which is at the end of the path
            String path = response.getLocation().getPath();
            String userId = path.substring(path.lastIndexOf("/") + 1);

            try {
                //set password
                CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
                credentialRepresentation.setTemporary(false);
                credentialRepresentation.setType(OAuth2Constants.PASSWORD);
                credentialRepresentation.setValue(user.getPassword());
                usersResource.get(userId).resetPassword(credentialRepresentation);

                //set roles
                RealmResource realmResource = KeycloakProvider.getRealmResource();
                List<RoleRepresentation> roleRepresentations = null;

                //if the request came without roles, set role "USER" by default
                if(user.getRoles() == null || user.getRoles().isEmpty()) {
                    roleRepresentations = List.of(realmResource.roles().get("USER").toRepresentation());
                }
                else {
                    roleRepresentations = realmResource.roles()
                            .list()
                            .stream()
                            .filter(role -> user.getRoles()
                                    .stream()
                                    .anyMatch(roleName -> roleName.equalsIgnoreCase(role.getName())))
                            .toList();
                }
                //add roles
                realmResource.users()
                        .get(userId)
                        .roles()
                        .realmLevel()
                        .add(roleRepresentations);

                return new ResponseEntity<>(userId, HttpStatus.CREATED);
            }
            catch (Exception ex) {
                log.error("error keycloak");
            }
        }
        else if (httpStatus == 409) //the user already exists (is not perfect)
            throw new BadRequestException("ya existe el usuario");
        else //keycloak error
            log.error("error keycloak");
            throw new ErrorResponseException(HttpStatus.CONFLICT);

    }

    /**
     * delete an user by provided id
     *
     * @param userId
     * @return status of action
     */
    @Override
    public ResponseEntity<?> delete(String userId) {
        try {
            UserRepresentation userRepresentation = KeycloakProvider.getUserResource().get(userId).toRepresentation();
            KeycloakProvider.getUserResource().get(userId).remove();

            return new ResponseEntity<>(true,HttpStatus.OK);
        }
        catch (Exception ex) {
            log.error("error keycloak");
            throw new ErrorResponseException(HttpStatus.CONFLICT);
        }
    }

    /**
     * update an user by provided id
     *
     * @param userId
     * @param user
     * @return status of action
     */
    @Override
    public ResponseEntity<?> update(String userId, UserCreateRequest user) {

        //THE USERNAME CANNOT BE UPDATED!!!

        //update password
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(OAuth2Constants.PASSWORD);
        credentialRepresentation.setValue(user.getPassword());

        //update user data
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setEmail(user.getEmail());
        userRepresentation.setUsername(user.getUsername());
        userRepresentation.setEmailVerified(true);
        userRepresentation.setEnabled(true);

        //set credentials
        userRepresentation.setCredentials(Collections.singletonList(credentialRepresentation));

        try {
            UserResource userResource = KeycloakProvider.getUserResource().get(userId);
            userResource.update(userRepresentation);

            return new ResponseEntity<>(true,HttpStatus.ACCEPTED);
        }
        catch (Exception ex) {
            log.error("error keycloak");
            throw new ErrorResponseException(HttpStatus.CONFLICT);
        }

    }

    /**
     * get all roles associated with the user
     *
     * @param userId
     * @return List of Strings
     */
    private List<String> getUserRoles(String userId) {
        //obtengo usuario
        UserResource userResource = KeycloakProvider.getUserResource().get(userId);
        //obtengo sus roles
        RoleMappingResource rolesResource = userResource.roles();
        // a nivel de realm (si "admin" / no "admin_client_role")
        List<RoleRepresentation> roles = rolesResource.realmLevel().listEffective();
        //devuelvo roles mapeados a una lista de Strings
        return roles.stream()
                .map(RoleRepresentation::getName)
                .filter(Roles::isAllowed)
                .collect(Collectors.toList());

        //keycloak tiene roles por default que no necesito
        // (solo quiero los que son parte del contexto = "admin", "supervisor", "user", "developer"....)
    }
}
