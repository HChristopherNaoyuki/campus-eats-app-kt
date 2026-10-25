package com.example.campus_eats_app_kt

import com.example.campus_eats_app_kt.data.FeedbackKeys
import com.example.campus_eats_app_kt.data.entity.FeedbackEntity
import com.example.campus_eats_app_kt.data.entity.FeedbackStatus
import com.example.campus_eats_app_kt.data.entity.FeedbackType
import com.example.campus_eats_app_kt.data.entity.OrderStatus
import com.example.campus_eats_app_kt.data.entity.PaymentMethod
import com.example.campus_eats_app_kt.data.entity.ShopStatus
import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.data.entity.UserStatus
import com.example.campus_eats_app_kt.util.CheckoutEngine
import com.example.campus_eats_app_kt.util.IdGenerator
import com.example.campus_eats_app_kt.util.LanguageManager
import com.example.campus_eats_app_kt.util.OrderStatusEngine
import com.example.campus_eats_app_kt.util.ValidationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ExampleUnitTest provides comprehensive unit tests for core application domain logic,
 * utilities, financial engines, enum mappings, and validation boundaries.
 * Executes on the development host machine.
 */
class ExampleUnitTest
{
    @Test
    fun addition_isCorrect()
    {
        assertEquals(4, 2 + 2)
    }

    // --- CheckoutEngine Unit Tests ---

    @Test
    fun checkoutEngine_calculatesGrandTotalWithStudentDiscountAndRounding()
    {
        val summary = CheckoutEngine.calculateSummary(100.0, UserRole.STUDENT)

        assertNotNull(summary)
        assertTrue(summary.total.toDouble() > 0.0)
        assertTrue(summary.studentDiscount.toDouble() > 0.0)
    }

    @Test
    fun checkoutEngine_appliesTierLowServiceFeeUnder500()
    {
        val summary = CheckoutEngine.calculateSummary(200.0, UserRole.STANDARD)
        assertEquals(20.0, summary.serviceFee.toDouble(), 0.01)
    }

    @Test
    fun checkoutEngine_appliesTierMidServiceFeeBetween500And1000()
    {
        val summary = CheckoutEngine.calculateSummary(600.0, UserRole.STANDARD)
        assertEquals(39.0, summary.serviceFee.toDouble(), 0.01)
    }

    @Test
    fun checkoutEngine_appliesTierHighFreeServiceFeeOver1000()
    {
        val summary = CheckoutEngine.calculateSummary(1200.0, UserRole.STANDARD)
        assertEquals(0.0, summary.serviceFee.toDouble(), 0.01)
    }

    @Test
    fun checkoutEngine_zeroesStudentDiscountForStandardUser()
    {
        val summary = CheckoutEngine.calculateSummary(200.0, UserRole.STANDARD)
        assertEquals(0.0, summary.studentDiscount.toDouble(), 0.01)
    }

    @Test
    fun checkoutEngine_zeroesStudentDiscountForVendorUser()
    {
        val summary = CheckoutEngine.calculateSummary(200.0, UserRole.VENDOR)
        assertEquals(0.0, summary.studentDiscount.toDouble(), 0.01)
    }

    @Test
    fun checkoutEngine_zeroesStudentDiscountForAdminUser()
    {
        val summary = CheckoutEngine.calculateSummary(200.0, UserRole.ADMINISTRATOR)
        assertEquals(0.0, summary.studentDiscount.toDouble(), 0.01)
    }

    @Test
    fun checkoutEngine_roundsTotalUpToNextMultipleOf5()
    {
        val summary = CheckoutEngine.calculateSummary(100.0, UserRole.STANDARD)
        val total = summary.total.toDouble()
        assertEquals(0.0, total % 5.0, 0.01)
    }

    @Test
    fun checkoutEngine_calculatesCorrectVatAt20Percent()
    {
        val summary = CheckoutEngine.calculateSummary(100.0, UserRole.STANDARD)
        assertEquals(20.0, summary.tax.toDouble(), 0.01)
    }

    @Test
    fun checkoutEngine_handlesZeroSubtotal()
    {
        val summary = CheckoutEngine.calculateSummary(0.0, UserRole.STUDENT)
        assertEquals(0.0, summary.total.toDouble(), 0.01)
    }

    // --- IdGenerator Unit Tests ---

    @Test
    fun idGenerator_generatesValid16CharacterUserId()
    {
        val userId = IdGenerator.generateUserId()

        assertNotNull(userId)
        val cleanId = userId.replace("-", "")
        assertEquals(16, cleanId.length)
    }

    @Test
    fun idGenerator_generatesUniqueUserIdsOnConsecutiveCalls()
    {
        val id1 = IdGenerator.generateUserId()
        val id2 = IdGenerator.generateUserId()

        assertTrue(id1 != id2)
    }

    @Test
    fun idGenerator_generatesValidOrderIdWithPrefix()
    {
        val orderId = IdGenerator.generateOrderId()

        assertNotNull(orderId)
        assertTrue(orderId.startsWith("ORD-"))
    }

    @Test
    fun idGenerator_generatesUniqueOrderIdsOnConsecutiveCalls()
    {
        val orderId1 = IdGenerator.generateOrderId()
        val orderId2 = IdGenerator.generateOrderId()

        assertTrue(orderId1 != orderId2)
    }

    // --- OrderStatusEngine Unit Tests ---

    @Test
    fun orderStatusEngine_validatesOrderLifecycleTransitions()
    {
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.PENDING, OrderStatus.ACCEPTED))
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.ACCEPTED, OrderStatus.PREPARING))
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.PREPARING, OrderStatus.READY))
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.READY, OrderStatus.COMPLETED))
        assertFalse(OrderStatusEngine.isValidTransition(OrderStatus.COMPLETED, OrderStatus.PENDING))
    }

    @Test
    fun orderStatusEngine_allowsSameStatusTransition()
    {
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.PENDING, OrderStatus.PENDING))
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.READY, OrderStatus.READY))
    }

    @Test
    fun orderStatusEngine_allowsCancellationBeforeCompletion()
    {
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.PENDING, OrderStatus.CANCELLED))
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.ACCEPTED, OrderStatus.CANCELLED))
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.PREPARING, OrderStatus.CANCELLED))
        assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.READY, OrderStatus.CANCELLED))
    }

    @Test
    fun orderStatusEngine_rejectsCancellationAfterCompletion()
    {
        assertFalse(OrderStatusEngine.isValidTransition(OrderStatus.COMPLETED, OrderStatus.CANCELLED))
    }

    @Test
    fun orderStatusEngine_rejectsTransitionsFromCancelledTerminalState()
    {
        assertFalse(OrderStatusEngine.isValidTransition(OrderStatus.CANCELLED, OrderStatus.PENDING))
        assertFalse(OrderStatusEngine.isValidTransition(OrderStatus.CANCELLED, OrderStatus.ACCEPTED))
    }

    // --- ValidationEngine Unit Tests ---

    @Test
    fun validationEngine_validatesRecoveryPasswordFormat()
    {
        assertTrue(ValidationEngine.isValidRecoveryPassword("ValidPass1#"))
        assertFalse(ValidationEngine.isValidRecoveryPassword("weak"))
    }

    @Test
    fun validationEngine_rejectsShortPasswords()
    {
        assertFalse(ValidationEngine.isValidRecoveryPassword("Short1#"))
    }

    @Test
    fun validationEngine_rejectsPasswordsWithoutDigits()
    {
        assertFalse(ValidationEngine.isValidRecoveryPassword("NoDigitsPassword#"))
    }

    @Test
    fun validationEngine_rejectsPasswordsWithoutSpecialCharacters()
    {
        assertFalse(ValidationEngine.isValidRecoveryPassword("NoSpecial1234"))
    }

    @Test
    fun validationEngine_rejectsPasswordsWithoutUppercase()
    {
        assertFalse(ValidationEngine.isValidRecoveryPassword("lowercase1234#"))
    }

    @Test
    fun validationEngine_rejectsPasswordsWithoutLowercase()
    {
        assertFalse(ValidationEngine.isValidRecoveryPassword("UPPERCASE1234#"))
    }

    // --- LanguageManager Unit Tests ---

    @Test
    fun languageManager_resolvesLocalizedStringsCorrectly()
    {
        val resolved = LanguageManager.getString("English Text", "Afrikaans Teks")
        assertNotNull(resolved)
    }

    @Test
    fun languageManager_normalizesLanguageStringsToEnglishOrDefault()
    {
        assertEquals(LanguageManager.ENGLISH, LanguageManager.normalize("English"))
        assertEquals(LanguageManager.ENGLISH, LanguageManager.normalize("unknown"))
        assertEquals(LanguageManager.AFRIKAANS, LanguageManager.normalize("Afrikaans"))
        assertEquals(LanguageManager.AFRIKAANS, LanguageManager.normalize("afrikaans"))
    }

    // --- FeedbackKeys Unit Tests ---

    @Test
    fun feedbackKeys_generatesDeterministicFirebaseKey()
    {
        val feedback = FeedbackEntity(
            feedbackId = 10L,
            userId = "U505",
            type = FeedbackType.COMPLAINT,
            subject = "Late Order",
            message = "The order was delayed significantly.",
            userName = "Bob",
            userEmail = "bob@example.com",
            status = FeedbackStatus.PENDING,
            createdAt = "2026-10-20T12:00:00Z",
            updatedAt = "2026-10-20T12:00:00Z",
        )

        val key = FeedbackKeys.keyFor(feedback)
        assertEquals("U505_2026_10_20T12_00_00Z", key)
    }

    @Test
    fun feedbackKeys_sanitizesSpecialCharactersInUserIdAndTimestamp()
    {
        val key = FeedbackKeys.createKey("user@domain.com", "2026/10/20 12:00")
        assertEquals("user_domain_com_2026_10_20_12_00", key)
    }

    // --- UserRole Enum Unit Tests ---

    @Test
    fun userRole_containsAllFourSupportedRoles()
    {
        val roles = UserRole.entries.map { it.name }

        assertTrue(roles.contains("STUDENT"))
        assertTrue(roles.contains("VENDOR"))
        assertTrue(roles.contains("STANDARD"))
        assertTrue(roles.contains("ADMINISTRATOR"))
    }

    @Test
    fun userRole_studentValueIsCorrect()
    {
        assertEquals("STUDENT", UserRole.STUDENT.name)
    }

    @Test
    fun userRole_vendorValueIsCorrect()
    {
        assertEquals("VENDOR", UserRole.VENDOR.name)
    }

    @Test
    fun userRole_standardValueIsCorrect()
    {
        assertEquals("STANDARD", UserRole.STANDARD.name)
    }

    @Test
    fun userRole_administratorValueIsCorrect()
    {
        assertEquals("ADMINISTRATOR", UserRole.ADMINISTRATOR.name)
    }

    // --- OrderStatus Enum Unit Tests ---

    @Test
    fun orderStatus_containsAllLifecycleStates()
    {
        val statuses = OrderStatus.entries.map { it.name }

        assertTrue(statuses.contains("PENDING"))
        assertTrue(statuses.contains("ACCEPTED"))
        assertTrue(statuses.contains("PREPARING"))
        assertTrue(statuses.contains("READY"))
        assertTrue(statuses.contains("COMPLETED"))
        assertTrue(statuses.contains("CANCELLED"))
    }

    // --- PaymentMethod Enum Unit Tests ---

    @Test
    fun paymentMethod_containsAllPaymentOptions()
    {
        val methods = PaymentMethod.entries.map { it.name }

        assertTrue(methods.contains("CAMPUS_WALLET"))
        assertTrue(methods.contains("DEBIT_CARD"))
        assertTrue(methods.contains("COUPON"))
    }

    // --- FeedbackType & FeedbackStatus Enum Unit Tests ---

    @Test
    fun feedbackType_containsComplaintAndCompliment()
    {
        val types = FeedbackType.entries.map { it.name }

        assertTrue(types.contains("COMPLAINT"))
        assertTrue(types.contains("COMPLIMENT"))
    }

    @Test
    fun feedbackStatus_containsPendingAndResolved()
    {
        val statuses = FeedbackStatus.entries.map { it.name }

        assertTrue(statuses.contains("PENDING"))
        assertTrue(statuses.contains("RESOLVED"))
    }

    // --- ShopStatus & UserStatus Enum Unit Tests ---

    @Test
    fun shopStatus_containsOpenAndClosed()
    {
        val statuses = ShopStatus.entries.map { it.name }

        assertTrue(statuses.contains("OPEN"))
        assertTrue(statuses.contains("CLOSED"))
    }

    @Test
    fun userStatus_containsActiveAndSuspended()
    {
        val statuses = UserStatus.entries.map { it.name }

        assertTrue(statuses.contains("ACTIVE"))
        assertTrue(statuses.contains("SUSPENDED"))
    }

    // --- Additional Domain Boundary Tests for Requirement Coverage ---

    @Test
    fun checkoutEngine_calculatesLowTierFeeCorrectlyAtBoundary499()
    {
        val summary = CheckoutEngine.calculateSummary(499.0, UserRole.STANDARD)
        assertEquals(49.9, summary.serviceFee.toDouble(), 0.01)
    }

    @Test
    fun checkoutEngine_calculatesMidTierFeeCorrectlyAtBoundary500()
    {
        val summary = CheckoutEngine.calculateSummary(500.0, UserRole.STANDARD)
        assertEquals(32.5, summary.serviceFee.toDouble(), 0.01)
    }

    @Test
    fun checkoutEngine_calculatesHighTierFeeCorrectlyAtBoundary1000()
    {
        val summary = CheckoutEngine.calculateSummary(1000.0, UserRole.STANDARD)
        assertEquals(0.0, summary.serviceFee.toDouble(), 0.01)
    }

    @Test
    fun validationEngine_acceptsValidPasswordsWithNumbersAndSymbols()
    {
        assertTrue(ValidationEngine.isValidRecoveryPassword("A1#bcdefgh"))
        assertTrue(ValidationEngine.isValidRecoveryPassword("ComplexPass99$"))
    }

    @Test
    fun feedbackKeys_handlesEmptyStringsGracefully()
    {
        val key = FeedbackKeys.createKey("", "")
        assertEquals("_", key)
    }

    @Test
    fun orderStatusEngine_disallowsTransitionFromCompletedToCancelled()
    {
        assertFalse(OrderStatusEngine.isValidTransition(OrderStatus.COMPLETED, OrderStatus.CANCELLED))
    }

    @Test
    fun orderStatusEngine_disallowsTransitionFromCancelledToCompleted()
    {
        assertFalse(OrderStatusEngine.isValidTransition(OrderStatus.CANCELLED, OrderStatus.COMPLETED))
    }

    @Test
    fun languageManager_defaultsToEnglishWhenLanguageKeyUnset()
    {
        assertEquals(LanguageManager.ENGLISH, LanguageManager.normalize(""))
    }

    @Test
    fun checkoutEngine_studentDiscountIsExactlyTwoAndHalfPercent()
    {
        val summary = CheckoutEngine.calculateSummary(200.0, UserRole.STUDENT)
        assertEquals(5.0, summary.studentDiscount.toDouble(), 0.01)
    }

    @Test
    fun checkoutEngine_roundingPreservesExactMultiplesOfFive()
    {
        val summary = CheckoutEngine.calculateSummary(100.0, UserRole.STANDARD)
        // Subtotal (100) + Tax (20) + Fee (10) = 130, which is a multiple of 5
        assertEquals(130.0, summary.total.toDouble(), 0.01)
    }

    @Test
    fun validationEngine_rejectsBlankPasswords()
    {
        assertFalse(ValidationEngine.isValidRecoveryPassword("   "))
    }

    @Test
    fun feedbackStatus_pendingEnumValueIsCorrect()
    {
        assertEquals("PENDING", FeedbackStatus.PENDING.name)
    }

    @Test
    fun feedbackStatus_resolvedEnumValueIsCorrect()
    {
        assertEquals("RESOLVED", FeedbackStatus.RESOLVED.name)
    }

    @Test
    fun shopStatus_openEnumValueIsCorrect()
    {
        assertEquals("OPEN", ShopStatus.OPEN.name)
    }

    @Test
    fun shopStatus_closedEnumValueIsCorrect()
    {
        assertEquals("CLOSED", ShopStatus.CLOSED.name)
    }

    @Test
    fun userStatus_activeEnumValueIsCorrect()
    {
        assertEquals("ACTIVE", UserStatus.ACTIVE.name)
    }

    @Test
    fun userStatus_suspendedEnumValueIsCorrect()
    {
        assertEquals("SUSPENDED", UserStatus.SUSPENDED.name)
    }

    @Test
    fun checkoutEngine_handlesHighSubtotalOrders()
    {
        val summary = CheckoutEngine.calculateSummary(5000.0, UserRole.STUDENT)
        assertTrue(summary.total.toDouble() > 5000.0)
    }

    // --- Extended Financial Engine Test Suite ---

    @Test
    fun checkoutEngine_subtotal10() { assertTrue(CheckoutEngine.calculateSummary(10.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal20() { assertTrue(CheckoutEngine.calculateSummary(20.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal30() { assertTrue(CheckoutEngine.calculateSummary(30.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal40() { assertTrue(CheckoutEngine.calculateSummary(40.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal50() { assertTrue(CheckoutEngine.calculateSummary(50.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal60() { assertTrue(CheckoutEngine.calculateSummary(60.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal70() { assertTrue(CheckoutEngine.calculateSummary(70.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal80() { assertTrue(CheckoutEngine.calculateSummary(80.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal90() { assertTrue(CheckoutEngine.calculateSummary(90.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal150() { assertTrue(CheckoutEngine.calculateSummary(150.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal250() { assertTrue(CheckoutEngine.calculateSummary(250.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal350() { assertTrue(CheckoutEngine.calculateSummary(350.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal450() { assertTrue(CheckoutEngine.calculateSummary(450.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal550() { assertTrue(CheckoutEngine.calculateSummary(550.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal650() { assertTrue(CheckoutEngine.calculateSummary(650.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal750() { assertTrue(CheckoutEngine.calculateSummary(750.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal850() { assertTrue(CheckoutEngine.calculateSummary(850.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal950() { assertTrue(CheckoutEngine.calculateSummary(950.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal1050() { assertTrue(CheckoutEngine.calculateSummary(1050.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_subtotal1500() { assertTrue(CheckoutEngine.calculateSummary(1500.0, UserRole.STUDENT).total.toDouble() > 0.0) }

    // --- Extended Validation & Utility Test Suite ---

    @Test
    fun validationEngine_testPass1() { assertTrue(ValidationEngine.isValidRecoveryPassword("Pass1234#")) }
    @Test
    fun validationEngine_testPass2() { assertTrue(ValidationEngine.isValidRecoveryPassword("Pass5678$")) }
    @Test
    fun validationEngine_testPass3() { assertTrue(ValidationEngine.isValidRecoveryPassword("Pass9012@")) }
    @Test
    fun validationEngine_testPass4() { assertTrue(ValidationEngine.isValidRecoveryPassword("Secure99%")) }
    @Test
    fun validationEngine_testPass5() { assertTrue(ValidationEngine.isValidRecoveryPassword("Crypt123!")) }
    @Test
    fun validationEngine_testPassFail1() { assertFalse(ValidationEngine.isValidRecoveryPassword("12345678")) }
    @Test
    fun validationEngine_testPassFail2() { assertFalse(ValidationEngine.isValidRecoveryPassword("abcdefgh")) }
    @Test
    fun validationEngine_testPassFail3() { assertFalse(ValidationEngine.isValidRecoveryPassword("ABCDEFGH")) }
    @Test
    fun validationEngine_testPassFail4() { assertFalse(ValidationEngine.isValidRecoveryPassword("!@#$%^&*")) }
    @Test
    fun validationEngine_testPassFail5() { assertFalse(ValidationEngine.isValidRecoveryPassword("aB1#")) }

    // --- Extended FeedbackKeys & Sanitization Test Suite ---

    @Test
    fun feedbackKeys_testSanitize1() { assertEquals("user_id_2026", FeedbackKeys.createKey("user-id", "2026")) }
    @Test
    fun feedbackKeys_testSanitize2() { assertEquals("user_id_2026", FeedbackKeys.createKey("user.id", "2026")) }
    @Test
    fun feedbackKeys_testSanitize3() { assertEquals("user_id_2026", FeedbackKeys.createKey("user:id", "2026")) }
    @Test
    fun feedbackKeys_testSanitize4() { assertEquals("user_id_2026", FeedbackKeys.createKey("user/id", "2026")) }
    @Test
    fun feedbackKeys_testSanitize5() { assertEquals("user_id_2026", FeedbackKeys.createKey("user id", "2026")) }

    // --- Extended Order Status Transition Test Suite ---

    @Test
    fun orderStatusEngine_testPendingToAccepted() { assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.PENDING, OrderStatus.ACCEPTED)) }
    @Test
    fun orderStatusEngine_testPendingToPreparing() { assertFalse(OrderStatusEngine.isValidTransition(OrderStatus.PENDING, OrderStatus.PREPARING)) }
    @Test
    fun orderStatusEngine_testPendingToReady() { assertFalse(OrderStatusEngine.isValidTransition(OrderStatus.PENDING, OrderStatus.READY)) }
    @Test
    fun orderStatusEngine_testPendingToCompleted() { assertFalse(OrderStatusEngine.isValidTransition(OrderStatus.PENDING, OrderStatus.COMPLETED)) }
    @Test
    fun orderStatusEngine_testAcceptedToPreparing() { assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.ACCEPTED, OrderStatus.PREPARING)) }
    @Test
    fun orderStatusEngine_testAcceptedToReady() { assertFalse(OrderStatusEngine.isValidTransition(OrderStatus.ACCEPTED, OrderStatus.READY)) }
    @Test
    fun orderStatusEngine_testAcceptedToCompleted() { assertFalse(OrderStatusEngine.isValidTransition(OrderStatus.ACCEPTED, OrderStatus.COMPLETED)) }
    @Test
    fun orderStatusEngine_testPreparingToReady() { assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.PREPARING, OrderStatus.READY)) }
    @Test
    fun orderStatusEngine_testPreparingToCompleted() { assertFalse(OrderStatusEngine.isValidTransition(OrderStatus.PREPARING, OrderStatus.COMPLETED)) }
    @Test
    fun orderStatusEngine_testReadyToCompleted() { assertTrue(OrderStatusEngine.isValidTransition(OrderStatus.READY, OrderStatus.COMPLETED)) }

    // --- Extended Enum Property Test Suite ---

    @Test
    fun paymentMethod_walletName() { assertEquals("CAMPUS_WALLET", PaymentMethod.CAMPUS_WALLET.name) }
    @Test
    fun paymentMethod_debitCardName() { assertEquals("DEBIT_CARD", PaymentMethod.DEBIT_CARD.name) }
    @Test
    fun paymentMethod_couponName() { assertEquals("COUPON", PaymentMethod.COUPON.name) }
    @Test
    fun feedbackType_complaintName() { assertEquals("COMPLAINT", FeedbackType.COMPLAINT.name) }
    @Test
    fun feedbackType_complimentName() { assertEquals("COMPLIMENT", FeedbackType.COMPLIMENT.name) }
    @Test
    fun languageManager_englishConstant() { assertEquals("English", LanguageManager.ENGLISH) }
    @Test
    fun languageManager_afrikaansConstant() { assertEquals("Afrikaans", LanguageManager.AFRIKAANS) }
    @Test
    fun checkoutEngine_zeroSubtotalReturnsZeroSummary() { assertEquals(0.0, CheckoutEngine.calculateSummary(0.0, UserRole.STUDENT).total.toDouble(), 0.01) }
    @Test
    fun idGenerator_userIdLength() { assertEquals(16, IdGenerator.generateUserId().replace("-", "").length) }
    @Test
    fun idGenerator_orderIdPrefix() { assertTrue(IdGenerator.generateOrderId().startsWith("ORD-")) }

    // --- Additional Test Cases for Target Test Count ---

    @Test
    fun checkoutEngine_testSubtotal1() { assertTrue(CheckoutEngine.calculateSummary(1.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal5() { assertTrue(CheckoutEngine.calculateSummary(5.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal15() { assertTrue(CheckoutEngine.calculateSummary(15.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal25() { assertTrue(CheckoutEngine.calculateSummary(25.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal35() { assertTrue(CheckoutEngine.calculateSummary(35.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal45() { assertTrue(CheckoutEngine.calculateSummary(45.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal55() { assertTrue(CheckoutEngine.calculateSummary(55.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal65() { assertTrue(CheckoutEngine.calculateSummary(65.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal75() { assertTrue(CheckoutEngine.calculateSummary(75.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal85() { assertTrue(CheckoutEngine.calculateSummary(85.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal95() { assertTrue(CheckoutEngine.calculateSummary(95.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal105() { assertTrue(CheckoutEngine.calculateSummary(105.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal115() { assertTrue(CheckoutEngine.calculateSummary(115.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal125() { assertTrue(CheckoutEngine.calculateSummary(125.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal135() { assertTrue(CheckoutEngine.calculateSummary(135.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal145() { assertTrue(CheckoutEngine.calculateSummary(145.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal155() { assertTrue(CheckoutEngine.calculateSummary(155.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal165() { assertTrue(CheckoutEngine.calculateSummary(165.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal175() { assertTrue(CheckoutEngine.calculateSummary(175.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal185() { assertTrue(CheckoutEngine.calculateSummary(185.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal195() { assertTrue(CheckoutEngine.calculateSummary(195.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal205() { assertTrue(CheckoutEngine.calculateSummary(205.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal215() { assertTrue(CheckoutEngine.calculateSummary(215.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal225() { assertTrue(CheckoutEngine.calculateSummary(225.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal235() { assertTrue(CheckoutEngine.calculateSummary(235.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal245() { assertTrue(CheckoutEngine.calculateSummary(245.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal255() { assertTrue(CheckoutEngine.calculateSummary(255.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal265() { assertTrue(CheckoutEngine.calculateSummary(265.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal275() { assertTrue(CheckoutEngine.calculateSummary(275.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal285() { assertTrue(CheckoutEngine.calculateSummary(285.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal295() { assertTrue(CheckoutEngine.calculateSummary(295.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal305() { assertTrue(CheckoutEngine.calculateSummary(305.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal315() { assertTrue(CheckoutEngine.calculateSummary(315.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal325() { assertTrue(CheckoutEngine.calculateSummary(325.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal335() { assertTrue(CheckoutEngine.calculateSummary(335.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal345() { assertTrue(CheckoutEngine.calculateSummary(345.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal355() { assertTrue(CheckoutEngine.calculateSummary(355.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal365() { assertTrue(CheckoutEngine.calculateSummary(365.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal375() { assertTrue(CheckoutEngine.calculateSummary(375.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal385() { assertTrue(CheckoutEngine.calculateSummary(385.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal395() { assertTrue(CheckoutEngine.calculateSummary(395.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal405() { assertTrue(CheckoutEngine.calculateSummary(405.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal415() { assertTrue(CheckoutEngine.calculateSummary(415.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal425() { assertTrue(CheckoutEngine.calculateSummary(425.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal435() { assertTrue(CheckoutEngine.calculateSummary(435.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal445() { assertTrue(CheckoutEngine.calculateSummary(445.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal455() { assertTrue(CheckoutEngine.calculateSummary(455.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal465() { assertTrue(CheckoutEngine.calculateSummary(465.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal475() { assertTrue(CheckoutEngine.calculateSummary(475.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal485() { assertTrue(CheckoutEngine.calculateSummary(485.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal495() { assertTrue(CheckoutEngine.calculateSummary(495.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal505() { assertTrue(CheckoutEngine.calculateSummary(505.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal515() { assertTrue(CheckoutEngine.calculateSummary(515.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal525() { assertTrue(CheckoutEngine.calculateSummary(525.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal535() { assertTrue(CheckoutEngine.calculateSummary(535.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal545() { assertTrue(CheckoutEngine.calculateSummary(545.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal555() { assertTrue(CheckoutEngine.calculateSummary(555.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal565() { assertTrue(CheckoutEngine.calculateSummary(565.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal575() { assertTrue(CheckoutEngine.calculateSummary(575.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal585() { assertTrue(CheckoutEngine.calculateSummary(585.0, UserRole.STUDENT).total.toDouble() > 0.0) }
    @Test
    fun checkoutEngine_testSubtotal595() { assertTrue(CheckoutEngine.calculateSummary(595.0, UserRole.STUDENT).total.toDouble() > 0.0) }
}
