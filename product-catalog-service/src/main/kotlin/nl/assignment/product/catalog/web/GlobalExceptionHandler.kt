package nl.assignment.product.catalog.web

import jakarta.persistence.EntityNotFoundException
import nl.assignment.product.catalog.exception.QuantityUpdateException
import org.springframework.dao.DataIntegrityViolationException
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

    @ExceptionHandler(org.springframework.data.elasticsearch.NoSuchIndexException::class)
    fun handleMissingIndex(ex: org.springframework.data.elasticsearch.NoSuchIndexException): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("Search index not found: ${ex.index}")

}