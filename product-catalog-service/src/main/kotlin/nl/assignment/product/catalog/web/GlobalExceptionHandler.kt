package nl.assignment.product.catalog.web

import jakarta.persistence.EntityNotFoundException
import nl.assignment.product.catalog.exception.QuantityUpdateException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFound(ex: EntityNotFoundException): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.message)

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDuplicateKey(ex: DataIntegrityViolationException): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body("Product with this SKU already exists")

    @ExceptionHandler(QuantityUpdateException::class)
    fun handleQuantityUpdate(ex: QuantityUpdateException): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ex.message)

    @ExceptionHandler(InvalidDataAccessApiUsageException::class)
    fun handleInvalidSearch(ex: InvalidDataAccessApiUsageException): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Invalid search query: ${ex.message}")
}