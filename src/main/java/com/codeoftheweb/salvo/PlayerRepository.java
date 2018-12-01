package com.codeoftheweb.salvo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.repository.query.Param;
@RepositoryRestResource

    public interface PlayerRepository extends JpaRepository<Player, Long> {
    Player findByUserName (@Param("userName") String userName);

    }

