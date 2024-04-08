package ru.romanow.openapi.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.romanow.openapi.server.domain.Server

interface ServerRepository : JpaRepository<Server, Int>
