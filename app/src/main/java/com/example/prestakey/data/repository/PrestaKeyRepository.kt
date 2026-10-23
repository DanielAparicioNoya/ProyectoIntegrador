package com.example.prestakey.data.repository

/**
 * Repositorio central de la aplicación PrestaKey.
 * TODO: Implementar objeto o clase repository que gestione el estado reactivo con StateFlow:
 * - StateFlow<List<KeyItem>> (aulas A01, B12, P02, BIB, PRO, C01)
 * - StateFlow<List<Loan>>
 * - StateFlow<List<Incident>>
 * - StateFlow<TeacherUser?>
 *
 * Métodos requeridos:
 * - login(email, password)
 * - logout()
 * - returnLoan(loanId)
 * - borrowKey(keyId, teacherName, teacherEmail, subject, pickupTime, returnDeadline)
 * - reportIncident(keyId, type, subject, description)
 * - findKeyByNfcTag(nfcTagId)
 */
